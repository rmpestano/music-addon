package com.github.forge.addon.music.playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.*;

import org.jboss.forge.addon.resource.*;

import com.github.forge.addon.music.model.Current;
import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.util.Utils;

/**
 * Created by pestano on 21/08/15.
 */
@Singleton
public class PlaylistManagerImpl implements PlaylistManager {

    @Inject
    private Utils utils;

    @Inject
    private ResourceFactory resourceFactory;

    private Map<String, Playlist> playlists;

    private Playlist currentPlaylist;


    @Override
    public Map<String, Playlist> getPlaylists() {
        if(playlists == null || playlists.isEmpty()){
            initPlayLists();
        }
        return playlists;
    }

	/**
	 * initialize the Map of playlists based on playlist FORGE_HOME/playlists/
	 * folder where each json file is a playlist.
	 */
	public void initPlayLists() {
    playlists = new ConcurrentHashMap<>();
		createPlaylist(DEFAULT_PLAYLIST);
		List<JsonObject> playListsJson = getAllPlaylists();
		for (JsonObject jsonObject : playListsJson) {
			Playlist playlist = parsePlaylist(jsonObject);
      if (playlist != null) {
        playlists.put(playlist.getName(), playlist);
      }
		}
	}

  @Override
  public void initPlaylist(String name) {
      playlists.remove(name);
      Playlist playlist = parsePlaylist(loadPlaylist(name));
      if (playlist != null) {
        playlists.put(name, playlist);
      } else {
        Logger.getLogger(getClass().getName()).warning("Playlist " + name + " not initialized due to parse errors");
      }
  }

  public Playlist parsePlaylist(JsonObject jsonObject){
        if(jsonObject == null){
            return null;
        }
        try {
          Playlist playlist = new Playlist(jsonObject.getString("name"));
          JsonArray jsonSongs = jsonObject.getJsonArray("songs");
          List<Song> songs = new ArrayList<>();
          for (JsonValue jsonSong : jsonSongs) {
            JsonObject songObject = (JsonObject) jsonSong;
            Song song = new Song(songObject);
            songs.add(song);
          }
          playlist.addSongs(songs);

          return playlist;
         }catch (Exception e){
          Logger.getLogger(getClass().getName()).warning("Problems for parsing playlist" + " - " +e.getMessage() + " - "+e.getCause());
      }
    return null;
}


    @Override
    public Playlist getPlaylist(String name) {
        return getPlaylists().get(name);
    }

    @Override
    public void createPlaylist(String name) {
        if (!hasPlaylist(name)) {
            DirectoryResource playlistHomeDir = getPlayListHomeDir();
            try {
                JsonObject playlistJson = Json.createObjectBuilder()
                        .add("name", name)
                        .add("songs", Json.createArrayBuilder().build()).build();
                FileResource<?> playListFile = playlistHomeDir.getChild(name + ".json").reify(FileResource.class);
                playListFile.createNewFile();
                playListFile.setContents(playlistJson.toString());
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not create playlist.", e);
            }
            getPlaylists().putIfAbsent(name, new Playlist(name));
        }
    }

    @Override
    public void savePlaylist(Playlist playlist) {
        FileResource<?> playListFile = getPlayListHomeDir().getChild(playlist.getName() + ".json").reify(FileResource.class);
        if (!playListFile.exists()) {
            playListFile.createNewFile();
        }
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Song song : playlist.getSongs()) {
            arrayBuilder.add(Json.createObjectBuilder().
                            add("location", song.getLocation()).
                            add("title",song.getTitle()).
                            add("album",song.getAlbum()).
                            add("artist",song.getArtist()).
                            add("genre",song.getGenre()).
                            add("year",song.getYear()).
                            add("duration",song.getDuration())
            );
        }
        JsonArray songs = arrayBuilder.build();
        playListFile.setContents(Json.createObjectBuilder().add("name", playlist.getName()).add("songs", songs).build().toString());
    }

    @Override
    public JsonObject loadPlaylist(String name) {
        FileResource<?> playListFile = getPlayListHomeDir().getChild(name + ".json").reify(FileResource.class);
        if (playListFile.exists()) {
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


    @Override
    public Playlist getCurrentPlaylist() {
        if (currentPlaylist == null) {
            currentPlaylist = getPlaylists().get(DEFAULT_PLAYLIST);
            if(currentPlaylist == null){
                if(!playlists.isEmpty()){
                    currentPlaylist = playlists.values().iterator().next();
                } else{
                    initPlayLists();
                }
            }
        }
        return currentPlaylist;
    }

    @Override
    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
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
        if (playlists != null) {
            playlists.clear();
        }
    }

    @Override
    public void removePlaylist(String name) {
        Resource<?> playList = getPlayListHomeDir().getChild(name + ".json");
      boolean deleted = false;
        if (playList.exists()) {
            deleted = playList.delete();
        }
        if (deleted && playlists != null) {
            playlists.remove(name);
        }
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
