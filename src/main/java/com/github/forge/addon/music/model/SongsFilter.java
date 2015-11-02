package com.github.forge.addon.music.model;

import java.util.ArrayList;
import java.util.List;

import com.github.forge.addon.music.playlist.PlaylistManager;
import com.github.forge.addon.music.util.AppCache;
import org.jboss.forge.addon.ui.cdi.CommandScoped;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.github.forge.addon.music.util.Assert.hasText;

@CommandScoped
public class SongsFilter {
	
	private List<Song> filteredSongs;

	@Inject
	private AppCache appCache;

	@Inject
	PlaylistManager playlistManager;
	
	@PostConstruct
	public void init(){
		filteredSongs = new ArrayList<>();
	}
	
	public Song findSong(String artist, String title, String album) {
		for (Song song : appCache.getAllSongs()) {
			if(song.getArtist().toLowerCase().equals(artist.toLowerCase())){
				if(song.getAlbum().toLowerCase().equals(album.toLowerCase())){
					if(song.getTitle().toLowerCase().equals(title.toLowerCase())){
						return song;
					}
					
				}
				
			}
			
		}	
		return null;
		
	}


	public void filter(String artist, String title, String album, String genre){
		filteredSongs.clear();
		for (Song song : appCache.getAllSongs()) {
			// filter by title
			if (hasText(title)) {
				if (!hasText(song.getTitle())) {
					continue;// no title, can't match search criteria
				}
				if (!song.getTitle().toLowerCase().contains(title.toLowerCase())) {
					continue; // no match, ignore
				}
			}
			// filter by artist
			if (hasText(artist)) {
				if (!hasText(song.getArtist())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getArtist().toLowerCase().contains(artist.toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// filter by album
			if (hasText(album)) {
				if (!hasText(song.getAlbum())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getAlbum().toLowerCase().contains(album.toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// filter by genre
			if (hasText(genre)) {
				if (!hasText(song.getGenre())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getGenre().toLowerCase().contains(genre.toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// if all criteria are satisfied
			filteredSongs.add(song);
		}
	}
	
	public List<Song> getFilteredSongs() {
		return filteredSongs;
	}

	public List<Song> getAllSongs() {
		return appCache.getAllSongs();
	}
}
