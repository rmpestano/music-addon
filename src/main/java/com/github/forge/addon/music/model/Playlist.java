package com.github.forge.addon.music.model;

import javax.enterprise.inject.Vetoed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pestano on 21/08/15.
 */
@Vetoed
public class Playlist implements Serializable{

    private String name;
    private List<Song> songs;

    public Playlist() {
    }

    public Playlist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song){
        if(songs == null){
            songs = new ArrayList<>();
        }
        if(!songs.contains(song)){
            songs.add(song);
        }
    }

    public void addSongs(List<Song> songs) {
        if(songs == null ){
            return;
        }
        for (Song song : songs) {
            addSong(song);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Playlist playlist = (Playlist) o;

        return name.equals(playlist.name);

    }

    @Override
    public int hashCode() {
        return 32;
    }


}
