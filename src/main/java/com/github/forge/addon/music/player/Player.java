package com.github.forge.addon.music.player;

import com.github.forge.addon.music.model.Song;
import org.jboss.forge.addon.ui.context.UIContext;

import java.util.List;

/**
 * Created by pestano on 15/10/15.
 */
public interface Player {

    /**
     * plays or resumes current song
     */
    void play();

    void setUiContext(UIContext uiContext);

    /**
     * pauses current song
     */
    void pause();

    void stop();

    /**
     * get next song and play it
     */
    void next();

    boolean isRepeat();

    boolean isRandom();

    boolean isGenerateStatistics();

    /**
     * shuffles queued songs
     */
    void shuffle();

    /**
     * recreates song queue
     */
    void initPlayQueue();

    /**
     *
     * @return queued songs to be played
     */
    List<Song> getPlayQueue();

    /**
     *
     * @return current ready to play song
     */
    Song getCurrentSong();

    /**
     * if <code>true<code/> will shuffle queue every time Player#next() song is request
     * @param shuffle
     */
    void setRandom(boolean shuffle);

    /**
     * if <code>true<code/> will return Player.getCurrentSong() instead of Player.next() song after playing a song
     * @param repeat
     */
    void setRepeat(boolean repeat);


    void setGenerateStatistics(boolean generateStatistics);

    /**
     *
     * @return current song playing time
     */
    String getPlayingTime();

    boolean isPlaying();

	boolean isPaused();

}
