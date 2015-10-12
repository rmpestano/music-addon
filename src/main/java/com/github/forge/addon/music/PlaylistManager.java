package com.github.forge.addon.music;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistProvider;
import com.github.forge.addon.music.util.Utils;
import org.jboss.forge.addon.parser.json.resource.JsonResource;
import org.jboss.forge.addon.resource.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
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
public class PlaylistManager implements PlaylistProvider {

    public static final String DEFAULT_PLAYLIST = "default";

    public String playListHome;

    @Inject
    private Utils utils;

    @Inject
    private ResourceFactory resourceFactory;

    private Map<String, Playlist> playlists;
    private List<JsonObject> allPlaylists;


    public Map<String, Playlist> getPlaylists() {
        if (playlists == null) {
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
                Song song = new Song();
                song.location(songObject.getString("location"));
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
            FileResource<?> defaultPlayListFile = playlistHomeDir.getChild(DEFAULT_PLAYLIST + ".json").reify(FileResource.class);
            defaultPlayListFile.createNewFile();
            defaultPlayListFile.setContents(defaultPlaylistJson.toString());

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not create playlist index.", e);
        }
    }

    @Override
    public void savePlayList(String name) {
        Playlist playlist = playlists.get(name);
        FileResource<?> playListFile = getPlayListHomeDir().getChild(playlist.getName()+".json").reify(FileResource.class);
        if(!playListFile.exists()){
            playListFile.createNewFile();
        }
        JsonArray songs = Json.createArrayBuilder().build();
        for (Song song : playlist.getSongs()) {
            songs.add(Json.createObjectBuilder().
                    add("title",song.getTitle()).
                    add("album",song.getAlbum()).
                    add("artist",song.getArtist()).
                    add("genre",song.getGenre()).
                    add("location",song.getLocation()).
                    add("year",song.getYear()).build()
            );
        }
        playListFile.setContents(Json.createObjectBuilder().add("name",name).add("songs",songs).toString());
    }

    @Override
    public JsonObject loadPlayList(String name) {
        FileResource<?> playListFile = getPlayListHomeDir().getChild(name+".json").reify(FileResource.class);
        if(playListFile.exists()){
            JsonObject jsonObject = playListFile.reify(JsonResource.class).getJsonObject();
            return jsonObject;
        }
        return null;

    }

    @Override
    public void addSong(String playlistName, Song song) {
    }

    @Override
    public void addSongs(String playlistName, List<Song> songs) {
    }

    @Override
    public void removeSong(String playlistName, Song song) {
    }


    /**
     * if there is no playlist index, creates a playlists.json in FORGE_HOME with default playlist:
     * [
     * {
     * "name" : "default",
     * "songs": []
     * }
     * ]
     */
    public void createDefaultPlaylist() {
        if (!hasDefaultPlaylist()) {
           createPlaylist(DEFAULT_PLAYLIST);
        }

    }

    private DirectoryResource getPlayListHomeDir() {
        return utils.getForgeHome().getOrCreateChildDirectory("playlists");
    }

    public boolean hasDefaultPlaylist() {
        FileResource<?> defaultPlaylist = getPlayListHomeDir().getChild(DEFAULT_PLAYLIST + ".json").reify(FileResource.class);
        return defaultPlaylist.exists();
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
            JsonResource jsonResource = resource.reify(JsonResource.class);
            JsonObject jsonObject = jsonResource.getJsonObject();
            playListsObject.add(jsonObject);
        }
        return playListsObject;
    }
}
