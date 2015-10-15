package com.github.forge.addon.music.playlist;

import com.github.forge.addon.music.event.ChangePlaylistEvent;
import com.github.forge.addon.music.model.Current;
import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.util.Utils;
import org.jboss.forge.addon.resource.*;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 21/08/15.
 */
@Singleton
public class PlaylistManagerImpl implements PlaylistManager {

    @Inject
    private Utils utils;

    @Inject
    private ResourceFactory resourceFactory;

    @Inject
    private Event<ChangePlaylistEvent> changePlaylistEvent;

    private Map<String, Playlist> playlists;

    private Playlist currentPlaylist;

    public Map<String, Playlist> getPlaylists() {
        if (playlists == null || playlists.isEmpty()) {
            playlists = initPlayLists();
        }
        return playlists;
    }

    /**
     * initialize the Map of playlists based on playlist FORGE_HOME/playlists/ folder
     * where each json file is a playlist.
     */
    private Map<String, Playlist> initPlayLists() {
        createDefaultPlaylist();
        List<JsonObject> playListsJson = getAllPlaylists();
        playlists = new HashMap<>();
        for (JsonObject jsonObject : playListsJson) {
            Playlist playlist = new Playlist(jsonObject.getString("name"));
            JsonArray jsonSongs = jsonObject.getJsonArray("songs");
            List<Song> songs = new ArrayList<>();
            for (JsonValue jsonSong : jsonSongs) {
                JsonObject songObject = (JsonObject) jsonSong;
                Song song = new Song(songObject.getString("location"));
                songs.add(song);
            }
            playlist.addSongs(songs);
            playlists.put(playlist.getName(), playlist);
        }
        return playlists;
    }


    @Override
    public Playlist getPlaylist(String name) {
        return getPlaylists().get(name);
    }

    @Override
    public void createPlaylist(String name) {
        DirectoryResource playlistHomeDir = getPlayListHomeDir();
        try {
            JsonObject defaultPlaylistJson = Json.createObjectBuilder()
                                    .add("name", name)
                                    .add("songs", Json.createArrayBuilder().build()).build();
            FileResource<?> defaultPlayListFile = playlistHomeDir.getChild(name + ".json").reify(FileResource.class);
            defaultPlayListFile.createNewFile();
            defaultPlayListFile.setContents(defaultPlaylistJson.toString());

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not create playlist.", e);
        }
    }

    @Override
    public void savePlaylist(Playlist playlist) {
        FileResource<?> playListFile = getPlayListHomeDir().getChild(playlist.getName()+".json").reify(FileResource.class);
        if(!playListFile.exists()){
            playListFile.createNewFile();
        }
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Song song : playlist.getSongs()) {
            arrayBuilder.add(Json.createObjectBuilder().
                    add("title",song.getTitle()).
                    add("album",song.getAlbum()).
                    add("artist",song.getArtist()).
                    add("genre",song.getGenre()).
                    add("location",song.getLocation()).
                    add("year",song.getYear()).build()
            );
        }
        JsonArray songs = arrayBuilder.build();
        playListFile.setContents(Json.createObjectBuilder().add("name",playlist.getName()).add("songs",songs).build().toString());
    }

    @Override
    public JsonObject loadPlaylist(String name) {
        FileResource<?> playListFile = getPlayListHomeDir().getChild(name+".json").reify(FileResource.class);
        if(playListFile.exists()){
            JsonObject jsonObject = Json.createReader(playListFile.getResourceInputStream()).readObject();
            return jsonObject;
        }
        return null;

    }

    @Override
    public void addSong(Playlist playlist, Song song) {
        playlist.addSong(song);
        savePlaylist(playlist);
    }

    @Override
    public void addSongs(Playlist playlist, List<Song> songs) {
        playlist.addSongs(songs);
        savePlaylist(playlist);
    }

    @Override
    public void removeSong(Playlist playlist, Song song) {
        playlist.getSongs().remove(song);
        savePlaylist(playlist);
    }


    public void createDefaultPlaylist() {
        if (!hasDefaultPlaylist()) {
           createPlaylist(DEFAULT_PLAYLIST);
        }

    }

    @Override
    @Produces
    @Current
    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    @Override
    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
        changePlaylistEvent.fire(new ChangePlaylistEvent(currentPlaylist));
    }

    private DirectoryResource getPlayListHomeDir() {
        return utils.getForgeHome().getOrCreateChildDirectory("playlists");
    }

    @Override
    public boolean hasPlaylist(String name) {
        FileResource<?> playlistFile = getPlayListHomeDir().getChild(name + ".json").reify(FileResource.class);
        return playlistFile.exists();
    }

    @Override
    public void removePlaylists() {
        List<Resource<?>> playLists = getPlayListHomeDir().listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof FileResource && resource.getName().endsWith(".json");
            }
        });
        for (Resource<?> playList : playLists) {
            playList.delete();
        }
        if(playlists != null){
            playlists.clear();
        }
    }

    public boolean hasDefaultPlaylist() {
        return hasPlaylist(DEFAULT_PLAYLIST);
    }


    public List<JsonObject> getAllPlaylists() {
        List<JsonObject> playListsObject = new ArrayList<>();
        List<Resource<?>> playListsFound = getPlayListHomeDir().listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof FileResource && resource.getName().endsWith(".json");
            }
        });
        for (Resource<?> resource : playListsFound) {
            JsonObject jsonObject = Json.createReader(resource.getResourceInputStream()).readObject();
            playListsObject.add(jsonObject);
        }
        return playListsObject;
    }
}
