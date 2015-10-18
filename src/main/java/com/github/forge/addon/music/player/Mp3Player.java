package com.github.forge.addon.music.player;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Equalizer;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
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

    private boolean shuffle = true;

    private boolean repeat;

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


    @Override
    public void play() {
        if (!isPlaying()) {
            if (currentSong == null) {
                next();
                return;
            }
            try {
                createAudioDevice();
                runPlayerThread();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
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
            }
            Decoder decoder = new Decoder();
            decoder.setEqualizer(new Equalizer(EqualizerPresets.ROCK.getValue()));
            device = FactoryRegistry.systemRegistry().createAudioDevice();
            //device.open(decoder); //FIXME
            jplayer = new AdvancedPlayer(songStream, device);
            jplayer.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    if (!repeat) {
                        next();//get next song and play
                    } else {
                        play(); //play current song again
                    }
                }
            });
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not play song " + path, e);
            throw new RuntimeException("Could not play song " + path + " " + e.getMessage(), e);
        }
    }

    public void resume() {
        try {
            createAudioDevice();
            runPlayerThread();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }


    private void runPlayerThread() throws JavaLayerException {
        if (executor == null || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor(Executors.privilegedThreadFactory());
        } else {
            cancelPlayingSong();
            executor.shutdownNow();
        }


        if (playerThread == null) {
            playerThread = createPlayerThread();
        }

        playingSong = executor.submit(playerThread);
    }

    private Thread createPlayerThread() {
        playerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    jplayer.play();
                } catch (JavaLayerException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Problems while playing song " + currentSong, ex);
                    throw new RuntimeException(ex);
                }
            }

        });
        playerThread.setDaemon(true);
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
                jplayer.close();
                executor.shutdownNow();
            } catch (IOException e) {
                //FIXME log ex
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        cancelPlayingSong();
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
                executor.shutdownNow();
            } catch (IOException e) {
                //FIXME log ex
                e.printStackTrace();
            }
        }
    }

    @Override
    public void next() {
        if (playQueue == null || playQueue.isEmpty()) {
            initPlayQueue();
        }
        int index = 0;
        if (shuffle) {
            index = new Random(System.currentTimeMillis()).nextInt(playQueue.size());
        }
        currentSong = playQueue.get(index);
        playQueue.remove(index);

        play();
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
        if (playQueue.isEmpty()) {
            throw new RuntimeException("No songs to play");
        }
    }

    @Override
    public List<Song> getPlayQueue() {
        return playQueue;
    }

    @Override
    public Song getCurrentSong() {
        return currentSong;
    }

    @Override
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    @Override
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    private List<Song> getAllSongs() {
        List<Song> allSongs = new ArrayList<>();
        for (Playlist playlist : playlistManager.getPlaylists().values()) {
            allSongs.addAll(playlist.getSongs());
        }
        return allSongs;
    }


    @Override
    public boolean isPlaying() {
        return (executor != null && (!executor.isTerminated() && !executor.isShutdown()));
    }

    @Override
    public String getCurrentSongTime() {
        if (currentSong == null) {
            return "";
        }
        //DurationFormatUtils.formatDuration(mp3.getLengthInMilliseconds(), "m:ss");
        return "" + device.getPosition();
    }
}
