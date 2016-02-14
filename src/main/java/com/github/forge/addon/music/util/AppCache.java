package com.github.forge.addon.music.util;

import com.github.forge.addon.music.event.AddSongEvent;
import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.forge.addon.music.util.Assert.hasText;

/**
 * Created by pestano on 02/11/15.
 */
@ApplicationScoped
public class AppCache {

    @Inject
    private PlaylistManager playlistManager;

    private Set<String> genres;

    private List<Song> allSongs; //all playlist songs


    public Set<String> getGenres() {
        if(genres == null){
            initGenresCache();
        }
        return genres;
    }

    public List<Song> getAllSongs() {
        if(allSongs == null){
            initSongsCache();
        }
        return allSongs;
    }

    private void initSongsCache() {
        allSongs = new ArrayList<>();
        for (Playlist playlist : playlistManager.getPlaylists().values()) {
            allSongs.addAll(playlist.getSongs());
        }
    }

    private void initGenresCache() {
        genres = new HashSet<>();
        genres.add("");
        for (Song song : getAllSongs()) {
            if(hasText(song.getGenre())){
                genres.add(song.getGenre());
            }
        }
    }

    public void onSongAdded(@Observes AddSongEvent addSongEvent){
        for (Song song : addSongEvent.getAddedSongs()) {
            if(!getAllSongs().contains(song)){
                allSongs.add(song);
                if(hasText(song.getGenre()))
                getGenres().add(song.getGenre());
            }
        }
    }
}
