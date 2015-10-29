package com.github.forge.addon.music.ui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UISelectMany;
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
	@WithAttributes(label = "Playlist songs", description = "artist - title (album)")
	private UISelectMany<Song> songs;

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
			List<Song> playlistSongs = currentPlaylist.getSongs();
			Collections.sort(playlistSongs);
			songs.setValue(playlistSongs);
			songs.setValueChoices(playlistSongs);
			songs.setNote(playlistSongs.size() + " songs found.");
		}
		builder.add(playlist).add(songs);

		playlist.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				String selectedPlaylist = valueChangeEvent.getNewValue().toString();
				List<Song> playlistSongs = playlistManager.getPlaylist(selectedPlaylist).getSongs();
				Collections.sort(playlistSongs);
				songs.setValueChoices(playlistSongs);
				songs.setValue(playlistSongs);
				songs.setNote(playlistSongs.size() + " song found.");
				playlistManager.setCurrentPlaylist(playlistManager.getPlaylist(selectedPlaylist));
			}
		});
		

	}


	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
	   if(!uiExecutionContext.getUIContext().getProvider().isGUI()){
			 List<Song> playlistSongs = playlistManager.getPlaylist(playlist.getValue()).getSongs();
			 PrintStream out = uiExecutionContext.getUIContext().getProvider().getOutput().out();
			 out.println(playlistSongs.size() + " songs found on playlist: "+playlist.getValue());
			 int i = 1;
			 for (Song song : playlistSongs) {
				 out.println(i+"- "+song);
				  i++;
			 }
		 }
		return Results.success("Command executed successfully!");
	}

}
