package com.github.forge.addon.music.ui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
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
		final Map<String, Playlist> playlists = playlistManager.getPlaylists();
		List<String> playlistNames = new ArrayList<>(playlists.keySet());
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
				playlistManager.setCurrentPlaylist(playlists.get(selectedPlaylist));
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

	}

	private List<String> getFormatedPlaylistSongs(String playlistName) {
		Playlist currentPlaylist = playlistManager.getPlaylist(playlistName);
		List<Song> playlistSongs = currentPlaylist == null ? Collections.<Song> emptyList()
				: currentPlaylist.getSongs();
		List<String> songsUIValue = new ArrayList<>();
		for (Song playlistSong : playlistSongs) {
			String uiValue = (!"".equals(playlistSong.getArtist()) ? playlistSong.getArtist():"no artist") + " - " + (!"".equals(playlistSong.getAlbum()) ? playlistSong.getAlbum():"no album found") + " - "
					+ ((!"".equals(playlistSong.getTitle())) ? playlistSong.getTitle():"no title found for file "+playlistSong.getLocation());
			songsUIValue.add(uiValue);
		}
		Collections.sort(songsUIValue);
		return songsUIValue;
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
	   if(!uiExecutionContext.getUIContext().getProvider().isGUI()){
			 List<String> formatedPlaylistSongs = getFormatedPlaylistSongs(playlist.getValue());
			 PrintStream out = uiExecutionContext.getUIContext().getProvider().getOutput().out();
			 out.println(formatedPlaylistSongs.size() + " songs found on playlist: "+playlist.getValue());
			 int i = 1;
			 for (String song : formatedPlaylistSongs) {
				 out.println(i+"- "+song);
				  i++;
			 }
		 }
		return Results.success("Command executed successfully!");
	}

}
