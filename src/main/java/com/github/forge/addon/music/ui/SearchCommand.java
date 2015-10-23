package com.github.forge.addon.music.ui;

import static com.github.forge.addon.music.util.Assert.hasText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.input.events.ValueChangeEvent;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.ui.wizard.UIWizard;

/**
 * Created by pestano on 16/08/15.
 */
public class SearchCommand extends AbstractUICommand implements UIWizard {

	@Inject
	Player player;

	@Inject
	PlaylistManager playlistManager;

	@Inject
	@WithAttributes(label = "Title", description = "Filter by song title", type = InputType.DEFAULT)
	private UIInput<String> title;

	@Inject
	@WithAttributes(label = "Artist", description = "Filter by song artist", type = InputType.DEFAULT)
	private UIInput<String> artist;

	@Inject
	@WithAttributes(label = "Album", description = "Filter by song album", type = InputType.DEFAULT)
	private UIInput<String> album;
	
	@Inject
	@WithAttributes(label = "Genre", description = "Filter by song genre", type = InputType.DEFAULT)
	private UIInput<String> genre;


	private List<Song> allSongs;

	private List<Song> filteredSongs;

	private String artistFilter;

	private String titleFilter;

	private String albumFilter;

	private String genreFilter;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(SearchCommand.class).description("Search songs in any playlists")
				.name("Music: Search").category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {
		allSongs = new ArrayList<>();
		for (Playlist playlist : playlistManager.getPlaylists().values()) {
			allSongs.addAll(playlist.getSongs());
		}

		title.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent arg0) {
				titleFilter = arg0.getNewValue().toString();
				filterSongs();
			}
		});

		artist.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent arg0) {
				artistFilter = arg0.getNewValue().toString();
				filterSongs();

			}
		});

		album.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent arg0) {
				albumFilter = arg0.getNewValue().toString();
				filterSongs();

			}
		});

		genre.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent arg0) {
				genreFilter = arg0.getNewValue().toString();
				filterSongs();
			}
		});
		uiBuilder.add(artist).add(album).add(title).add(genre);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
			return playFilteredSongs();

	}

	private Result playFilteredSongs() {
		if (filteredSongs != null && !filteredSongs.isEmpty()) {
			List<Song> newPlayQueue = new ArrayList<>();
			for (Song song : filteredSongs) {
				if (!newPlayQueue.contains(song)) {
					newPlayQueue.add(song);
				}
			}

			if (!newPlayQueue.isEmpty()) {
				player.getPlayQueue().clear();
				player.getPlayQueue().addAll(newPlayQueue);
				if (player.isPlaying()) {
					player.next();
				} else {
					player.play();
				}
				return Results.success("Found " + newPlayQueue.size() + " songs to play. Now playing "
						+ player.getCurrentSong().info());
			}
		}

		return Results.success("No songs found.");

	}

	private void filterSongs() {
		filteredSongs = new ArrayList<>();
		for (Song song : allSongs) {
			// filter by title
			if (hasText(titleFilter)) {
				if (!hasText(song.getTitle())) {
					continue;// no title, can't match search criteria
				}
				if (!song.getTitle().toLowerCase().contains(titleFilter.toLowerCase())) {
					continue; // no match, ignore
				}
			}
			// filter by artist
			if (hasText(artistFilter)) {
				if (!hasText(song.getArtist())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getArtist().toLowerCase().contains(artistFilter.toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// filter by album
			if (hasText(albumFilter)) {
				if (!hasText(song.getAlbum())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getAlbum().toLowerCase().contains(albumFilter.toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// filter by genre
			if (hasText(genreFilter)) {
				if (!hasText(song.getGenre())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getGenre().toLowerCase().contains(genreFilter.toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// if all criteria are satisfied
			filteredSongs.add(song);
		}
	}


	@Override
	public NavigationResult next(UINavigationContext context) throws Exception {
		if(filteredSongs.isEmpty()){
			return null;
		}
		context.getUIContext().getAttributeMap().put("songs",filteredSongs);
		return context.navigateTo(SearchCommandStep.class);
	}
}
