package com.github.forge.addon.music.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;

/**
 * Created by pestano on 15/10/15.
 */
@Singleton
public class Mp3Player implements Player {

    @Inject
    private PlaylistManager playlistManager;

    private boolean shuffle;

    private boolean repeat;

    private List<Song> songQueue;

    private Song currentSong;

    @Override
    public void play() {
        if(currentSong == null){
           next();
        }
        //play current song

    }

    @Override
    public void pause() {
        //pause current song
    }

    @Override
    public void next() {
        if(songQueue == null || songQueue.isEmpty()){
            initQueue();
        }
        if(!repeat){
            if(shuffle){
                shuffle();
            }
            currentSong = songQueue.get(0);
            songQueue.remove(currentSong);
        }

        play();
    }


    @Override
    public void shuffle() {
        if(songQueue == null || songQueue.isEmpty()){
            initQueue();
        }
        Collections.shuffle(songQueue);
    }

    @Override
    public void initQueue() {
        if (playlistManager.getCurrentPlaylist() != null) {
            songQueue = new LinkedList<>(playlistManager.getCurrentPlaylist().getSongs());
        } else {
            songQueue = new LinkedList<>(getAllSongs());
        }
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


}
