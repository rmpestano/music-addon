package com.github.forge.addon.music.player;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;
import com.google.common.util.concurrent.Monitor;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Equalizer;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 15/10/15.
 */
@Singleton
public class Mp3Player implements Player  {

    @Inject
    private PlaylistManager playlistManager;

    private boolean shuffle;

    private boolean repeat;

    private List<Song> playQueue;

    private Song currentSong;

    private javazoom.jl.player.Player jplayer;

    private FileInputStream songStream;

    private long songTotalLength;

    private long pauseLocation;
    private PausableExecutor executor;

    @Override
    public void play() {
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

    private void createAudioDevice() {
        String path = currentSong.getLocation();
        try {
            songStream = new FileInputStream(path);
            songTotalLength = songStream.available();
            Decoder decoder = new Decoder();
            decoder.setEqualizer(new Equalizer(EqualizerPresets.ROCK.getValue()));
            AudioDevice device = FactoryRegistry.systemRegistry().createAudioDevice();
            //device.open(decoder); //FIXME
            jplayer = new javazoom.jl.player.Player(songStream, device);
        }catch (Exception e){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not play song " + path, e);
            throw new RuntimeException("Could not play song " + path, e);
        }
    }

    public void resume() {
        try {
            createAudioDevice();
            executor.resume();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private void runPlayerThread() throws JavaLayerException {
        if(executor == null || executor.isTerminated()){
            executor = new PausableExecutor(1, Executors.privilegedThreadFactory());
        }
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    jplayer.play();
                    System.out.println("play");
                    if (jplayer.isComplete()) {
                        System.out.println("complete");
                        executor.shutdown();
                        if (!repeat) {
                            next();//get next song and play
                        } else {
                            play(); //play current song again
                        }
                    }
                } catch (JavaLayerException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Problems while playing song " + currentSong, ex);
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public void pause() {
        if (jplayer != null) {
            try {
                pauseLocation = songStream.available();
                jplayer.close();
                executor.pause();
            } catch (IOException e) {
                //FIXME log ex
                e.printStackTrace();
            }
        }
    }

    @Override
    public void Stop() {
        if (jplayer != null) {
            jplayer.close();
            pauseLocation = 0;
            songTotalLength = 0;
            try {
                songStream.close();
                executor.shutdown();
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




    private class PausableExecutor extends ScheduledThreadPoolExecutor {

        private boolean isPaused;

        private final Monitor monitor = new Monitor();
        private final Monitor.Guard paused = new Monitor.Guard(monitor) {
            @Override
            public boolean isSatisfied() {
                return isPaused;
            }
        };

        private final Monitor.Guard notPaused = new Monitor.Guard(monitor) {
            @Override
            public boolean isSatisfied() {
                return !isPaused;
            }
        };

        public PausableExecutor(int corePoolSize, ThreadFactory threadFactory) {
            super(corePoolSize, threadFactory);
        }

        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            monitor.enterWhenUninterruptibly(notPaused);
            try {
                monitor.waitForUninterruptibly(notPaused);
            } finally {
                monitor.leave();
            }
        }

        public void pause() {
            monitor.enterIf(notPaused);
            try {
                isPaused = true;
            } finally {
                monitor.leave();
            }
        }

        public void resume() {
            monitor.enterIf(paused);
            try {
                isPaused = false;
            } finally {
                monitor.leave();
            }
        }
    }
}
