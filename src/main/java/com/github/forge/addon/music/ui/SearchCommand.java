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
import com.github.forge.addon.music.model.SongsFilter;
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

	@Inject
	private SongsFilter songsFilter;

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
				filterSongs();
			}
		});

		artist.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent arg0) {
				filterSongs();

			}
		});

		album.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent arg0) {
				filterSongs();

			}
		});

		uiBuilder.add(artist).add(album).add(title).add(genre);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
		filterSongs();

		return null;

	}


	private synchronized void filterSongs() {
		songsFilter.getSongs().clear();
		for (Song song : allSongs) {
			// filter by title
			if (hasText(title.getValue())) {
				if (!hasText(song.getTitle())) {
					continue;// no title, can't match search criteria
				}
				if (!song.getTitle().toLowerCase().contains(title.getValue().toLowerCase())) {
					continue; // no match, ignore
				}
			}
			// filter by artist
			if (hasText(artist.getValue())) {
				if (!hasText(song.getArtist())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getArtist().toLowerCase().contains(artist.getValue().toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// filter by album
			if (hasText(album.getValue())) {
				if (!hasText(song.getAlbum())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getAlbum().toLowerCase().contains(album.getValue().toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// filter by genre
			if (hasText(genre.getValue())) {
				if (!hasText(song.getGenre())) {
					continue;// no artist, cant match search criteria
				}

				if (!song.getGenre().toLowerCase().contains(genre.getValue().toLowerCase())) {
					continue; // no match, ignore
				}

			}

			// if all criteria are satisfied
			songsFilter.addSong(song);
		}
	}

	@Override
	public NavigationResult next(UINavigationContext context) throws Exception {
		if (!songsFilter.hasSongs()) {
			return null;// go to next step only if there is filtered songs
		}
		return context.navigateTo(SearchCommandStep.class);
	}
}
