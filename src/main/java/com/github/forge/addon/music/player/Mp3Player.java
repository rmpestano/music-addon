package com.github.forge.addon.music.player;

import com.github.forge.addon.music.event.AddSongEvent;
import com.github.forge.addon.music.event.ChangePlaylistEvent;
import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;
import com.github.forge.addon.music.statistics.StatisticsManager;
import com.github.forge.addon.music.util.AudioControl;
import com.github.forge.addon.music.util.StopWatch;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Equalizer;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.jboss.forge.addon.ui.context.UIContext;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 15/10/15.
 */
@Singleton
public class Mp3Player implements Player {

    @Inject
    private PlaylistManager playlistManager;

    @Inject
    private StatisticsManager statisticsManager;

    @Inject
    StopWatch stopWatch;

    private boolean random;

    private boolean repeat;

    private boolean generateStatistics = true;

    private List<Song> playQueue;

    private Song currentSong;

    private AdvancedPlayer jplayer;

    private FileInputStream songStream;

    private long songTotalLength;

    private long pauseLocation;

    private ExecutorService executor;

    private Future<?> playingSong;

    private AudioDevice device;

    private Thread playerThread;

    private UIContext uiContext;

    @Override
    public void play() {
        if(!AudioControl.isAudioEnabled()){
            return;
        }
        if (!isPlaying()) {
            if (currentSong == null) {
                next();
                return;
            }
            try {
                if (pauseLocation > 0){
                    stopWatch.resume();
                } else{
                    stopWatch.start();
                }
                createAudioDevice();
                runPlayerThread();
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).warning("Could not play song at location: " + currentSong.getLocation() + " \nmessage:" + e.getMessage() + "\ncause:" + e.getMessage());
                playQueue.remove(currentSong);
                next();
            }
        }

    }

    private void createAudioDevice() {
        String path = currentSong.getLocation();
        try {
            songStream = new FileInputStream(path);
            songTotalLength = songStream.available();

            if (pauseLocation > 0) {
                songStream.skip(songTotalLength - pauseLocation);
                pauseLocation = 0;
            }
            Decoder decoder = new Decoder();
            decoder.setEqualizer(new Equalizer(EqualizerPresets.ROCK.getValue()));
            device = FactoryRegistry.systemRegistry().createAudioDevice();
            //device.open(decoder); //FIXME
            if (jplayer != null) {
                jplayer.close();
            }
            jplayer = new AdvancedPlayer(songStream, device);
            jplayer.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    try {
                        if (isGenerateStatistics()) {
                            statisticsManager.addStatistic(currentSong);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    next();
                    if (uiContext != null && currentSong != null) {
                        String msg = "Now playing: " + currentSong;
                        if (!uiContext.getProvider().isGUI()) {
                            uiContext.getProvider().getOutput().out().println(msg);
                        }

                    }
                }
            });
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not play song " + path, e);
            throw new RuntimeException("Could not play song " + path + " " + e.getMessage(), e);
        }
    }

    @Override
    public void setUiContext(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    private void runPlayerThread() throws JavaLayerException {
        if (executor == null || executor.isTerminated() || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(Executors.privilegedThreadFactory());
        } else {
            cancelPlayingSong();
            executor.shutdownNow();
        }

        playingSong = executor.submit(getPlayerThread());

    }

    private Thread getPlayerThread() {
        if (playerThread == null) {
            playerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if(AudioControl.isAudioEnabled()){
                            jplayer.play();
                        }else{
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Audio device was not found, no song will be played");
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Problems while playing song " + currentSong, ex);
                        throw new RuntimeException(ex);
                    }
                }

            });
            playerThread.setDaemon(true);
        }


        return playerThread;
    }

    private void cancelPlayingSong() {
        if (playingSong != null && !playingSong.isDone()) {
            playingSong.cancel(true);
        }

    }

    @Override
    public void pause() {
        if (jplayer != null) {
            try {
                pauseLocation = songStream.available();
                stopWatch.pause();
                jplayer.close();
                songStream.close();
                executor.shutdownNow();
            } catch (Exception e) {
                //FIXME log ex
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        cancelPlayingSong();
        if(playQueue != null){
        	playQueue.clear();
        }
        if (jplayer != null) {
            jplayer.close();
            if (device != null) {
                device.close();
                device = null;
            }
            pauseLocation = 0;
            songTotalLength = 0;
            try {
                songStream.close();
                if (generateStatistics) {
                    statisticsManager.persistStatistics(true);
                }
            } catch (Exception e) {
                //FIXME log ex
                e.printStackTrace();
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Override
    public void next() {
        if (playQueue == null || playQueue.isEmpty()) {
            initPlayQueue();
        }
        int index = 0;
        if (isRandom()) {
            index = new Random(System.currentTimeMillis()).nextInt(playQueue.size());
        }
        currentSong = playQueue.get(index);
        if (!isRepeat()) {
            playQueue.remove(index);
        }
        if (isPlaying()) {
            try {
                pauseLocation = 0;
                songStream.close();
                jplayer.close();
                executor.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
                //FIXME log ex e show msg
            }
        }
        play();
    }

    @Override
    public boolean isRepeat() {
        return repeat;
    }

    @Override
    public boolean isRandom() {
        return random;
    }

    @Override
    public boolean isGenerateStatistics() {
        return this.generateStatistics;
    }


    @Override
    public void shuffle() {
        if (playQueue == null || playQueue.isEmpty()) {
            initPlayQueue();
        }
        Collections.shuffle(playQueue);
    }

    @Override
    public void initPlayQueue() {
        if (playlistManager.getCurrentPlaylist() != null) {
            playQueue = new ArrayList<>(playlistManager.getCurrentPlaylist().getSongs());
        } else {
            playQueue = new ArrayList<>(getAllSongs());
        }
    }

    @Override
    public List<Song> getPlayQueue() {
        if (playQueue == null) {
            initPlayQueue();
        }
        return playQueue;
    }

    @Override
    public Song getCurrentSong() {
        return currentSong;
    }

    @Override
    public void setRandom(boolean random) {
        this.random = random;
    }

    @Override
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    @Override
    public void setGenerateStatistics(boolean generateStatistics) {
        this.generateStatistics = generateStatistics;
    }

    private List<Song> getAllSongs() {
        List<Song> allSongs = new ArrayList<>();
        for (Playlist playlist : playlistManager.getPlaylists().values()) {
            allSongs.addAll(playlist.getSongs());
        }
        return allSongs;
    }

    @Override
    public String getPlayingTime() {
        if (isPlaying()) {
            return DurationFormatUtils.formatDuration(stopWatch.getMilliseconds(), "m:ss");
        } else {
            return "";
        }
    }

    @Override
    public boolean isPlaying() {
        return (currentSong != null && executor != null && (!executor.isTerminated() && !executor.isShutdown()));
    }


    public void onPlaylistChanged(@Observes ChangePlaylistEvent playlistEvent) {
        playQueue = null;
        next();
    }

    public void onSongAdded(@Observes AddSongEvent addSongEvent) {
        playQueue = null;
    }
    
   @Override
   public boolean isPaused(){
    	return pauseLocation > 0;
    }

}
