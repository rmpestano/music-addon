package com.github.forge.addon.music.model;

import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.addon.ui.cdi.CommandScoped;

@CommandScoped
public class SongsFilter {
	
	List<Song> songs = new ArrayList<>();
	
	public SongsFilter(){
	}

	public List<Song> getSongs() {
		return songs;
	}

	public void addSong(Song song){
		this.songs.add(song);
	}
	
	public void addSongs(List<Song> songs){
		this.songs.addAll(songs);
	}
	
	public boolean hasSongs(){
		return songs != null && !songs.isEmpty();
	}
	
	

}
