package com.github.forge.addon.music.playlist;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;

import javax.json.JsonObject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by pestano on 03/10/15.
 */
public interface PlaylistProvider extends Serializable{

    Map<String, Playlist> getPlaylists();

    /**
     * get playList object in memory
     * @param name
     * @return
     */
    Playlist getPlaylist(String name);

    /**
     * creates a empty playlist @param name.json in FORGE_HOME/playlists folder
     * @param name
     * @return
     */
    void createPlaylist(String name);


    /**
     * save current state of playlist of @param name which is memory into FORGE_HOME/playlists
     * @param name
     * @return
     */
    void savePlayList(String name);


    /**
     * get playList json object from playlists folder (FORGE_HOME/playlists)
     * @param name
     * @return
     */
    JsonObject loadPlayList(String name);

    /**
     *
     * @param playlistName
     * @param song
     */
    void addSong(String playlistName, Song song);

    void addSongs(String playlistName, List<Song> songs);

    void removeSong(String playlistName, Song song);

    boolean hasDefaultPlaylist();


    /**
     *
     * if there is no playlist index, creates a playlists.json in FORGE_HOME with default playlist:
     * [
     *  {
     *    "name" : "default",
     *    "songs": []
     *  }
     * ]
     *
     */
     void createDefaultPlaylist();
}
