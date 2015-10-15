package com.github.forge.addon.music.player;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;

/**
 * Created by pestano on 15/10/15.
 */
public interface Player {

    /**
     * plays current song
     */
    void play();

    /**
     * pauses current song
     */
    void pause();

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
    void initQueue();

    void setShuffle(boolean shuffle);

    void setRepeat(boolean repeat);
}
