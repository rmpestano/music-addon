package com.github.forge.addon.music.event;

import com.github.forge.addon.music.model.Song;

import java.util.List;

/**
 * Created by rafael-pestano on 15/10/2015.
 */
public class AddSongEvent {

    private List<Song> addedSongs;

    public AddSongEvent() {
    }

    public AddSongEvent(List<Song> addedSongs) {
        this.addedSongs = addedSongs;
    }

    public List<Song> getAddedSongs() {
        return addedSongs;
    }
}
