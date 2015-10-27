package com.github.forge.addon.music.model;

import java.util.ArrayList;
import java.util.List;

import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.ui.cdi.CommandScoped;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.github.forge.addon.music.util.Assert.hasText;

@CommandScoped
public class SongsFilter {
	
	private List<Song> filteredSongs;
	private List<Song> allSongs;
 

	@Inject
	PlaylistManager playlistManager;
	
	@PostConstruct
	public void init(){
		allSongs = new ArrayList<>();
		for (Playlist playlist : playlistManager.getPlaylists().values()) {
			allSongs.addAll(playlist.getSongs());
		}

		filteredSongs = new ArrayList<>();
	}


	public void filter(String artist, String title, String album, String genre){
		filteredSongs.clear();
		for (Song song : allSongs) {
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
		return allSongs;
	}
}
