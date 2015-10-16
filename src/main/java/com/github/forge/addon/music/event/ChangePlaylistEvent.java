package com.github.forge.addon.music.event;

import com.github.forge.addon.music.model.Playlist;

/**
 * Created by rafael-pestano on 15/10/2015.
 */
public class ChangePlaylistEvent {

  private Playlist playlist;

  public ChangePlaylistEvent(Playlist playlist) {
    this.playlist = playlist;
  }

  public Playlist getPlaylist() {
    return playlist;
  }
}
