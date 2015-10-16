package com.github.forge.addon.music.player;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;

import java.util.List;

/**
 * Created by pestano on 15/10/15.
 */
public interface Player {

    /**
     * plays current song
     */
    void play();

    /**
     * resumes current song
     */
    void resume();

    /**
     * pauses current song
     */
    void pause();

    void Stop();

    /**
     * get next song and play it
     */
    void next();

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
    void setShuffle(boolean shuffle);

    /**
     * if <code>true<code/> will return Player.getCurrentSong() instead of Player.next() song after playing a song
     * @param repeat
     */
    void setRepeat(boolean repeat);
}
