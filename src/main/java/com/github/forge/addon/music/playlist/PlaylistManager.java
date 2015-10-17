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
public interface PlaylistManager extends Serializable{

    String DEFAULT_PLAYLIST = "default";

    Map<String, Playlist> getPlaylists();

    Playlist getCurrentPlaylist();

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
     * save current state of playlist (which is memory) into FORGE_HOME/playlists/playlistName.json
     * @param playlist
     * @return
     */
    void savePlaylist(Playlist playlist);


    /**
     * get playList json object from playlists folder (FORGE_HOME/playlists)
     * @param name
     * @return
     */
    JsonObject loadPlaylist(String name);

    /**
     * * add song and persist to FORGE_HOME/playlists/playlistName.json
     * @param playlist
     * @param song
     */
    void addSong(Playlist playlist, Song song);

    /**
     * * add songs and persist to FORGE_HOME/playlists/playlistName.json
     * @param playlist
     * @param songs
     */
    void addSongs(Playlist playlist, List<Song> songs);

    /**
     * remove song and persist to FORGE_HOME/playlists/playlistName.json
     * @param playlist
     * @param song
     */
    void removeSong(Playlist playlist, Song song);

    void setCurrentPlaylist(Playlist currentPlaylist);

    boolean hasPlaylist(String name);

    void removePlaylists();

    void removePlaylist(String name);
}
