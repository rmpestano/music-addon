package com.github.forge.addon.music.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.input.events.ValueChangeEvent;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.validate.UIValidator;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;

/**
 * Created by pestano on 16/08/15.
 */
public class ViewPlaylistCommand extends AbstractUICommand {

	@Inject
	PlaylistManager playlistManager;

	@Inject
	@WithAttributes(label = "Select playlist", description = "Playlist to visualize", required = true, type = InputType.DROPDOWN)
	private UISelectOne<String> playlist;

	@Inject
	@WithAttributes(label = "Playlist songs (artist - album - title)", type = InputType.TEXTBOX, enabled = false)
	private UIInputMany<String> songs;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(ViewPlaylistCommand.class).description("View playlist songs")
				.name("Music: View playlist").category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(final UIBuilder builder) throws Exception {
		List<String> playlistNames = new ArrayList<>(playlistManager.getPlaylists().keySet());
		Collections.sort(playlistNames);
		playlist.setValueChoices(playlistNames);
		Playlist currentPlaylist = playlistManager.getCurrentPlaylist();
		if (currentPlaylist != null) {
			playlist.setDefaultValue(currentPlaylist.getName());
			List<String> playlistSongs = getFormatedPlaylistSongs(currentPlaylist.getName());
			songs.setDefaultValue(playlistSongs);
			songs.setNote(playlistSongs.size() + " songs found.");
		}
		builder.add(playlist).add(songs);

		playlist.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				String selectedPlaylist = valueChangeEvent.getNewValue().toString();
				List<String> formatedPlaylistSongs = getFormatedPlaylistSongs(selectedPlaylist);
				songs.setValue(formatedPlaylistSongs);
				songs.setNote(formatedPlaylistSongs.size() + " songs found.");
				playlistManager.setCurrentPlaylist(playlistManager.getPlaylist(selectedPlaylist));
			}
		});

		songs.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				List<String> oldValue = (List<String>) valueChangeEvent.getOldValue();
				Collections.sort(oldValue);
				songs.setValue(oldValue);
				songs.setDefaultValue(oldValue);
			}
		});

		songs.addValidator(new UIValidator() {
			@Override
			public void validate(UIValidationContext uiValidationContext) {
				uiValidationContext.addValidationError(songs,
						"Any change to the list will not be persisted.\n Use 'playlist edit' command instead.");
			}
		});

	}

	private List<String> getFormatedPlaylistSongs(String playlistName) {
		Playlist currentPlaylist = playlistManager.getPlaylist(playlistName);
		List<Song> playlistSongs = currentPlaylist == null ? Collections.<Song> emptyList()
				: currentPlaylist.getSongs();
		List<String> songsUIValue = new LinkedList<>();
		for (Song playlistSong : playlistSongs) {
			String uiValue = playlistSong.getArtist() + " - " + playlistSong.getAlbum() + " - "
					+ playlistSong.getTitle();
			songsUIValue.add(uiValue);
		}
		Collections.sort(songsUIValue);
		return songsUIValue;
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
		return Results.success("View playlist command executed with success!");
	}

}
