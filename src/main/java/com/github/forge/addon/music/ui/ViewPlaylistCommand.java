package com.github.forge.addon.music.ui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.github.forge.addon.music.util.AppCache;
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
	AppCache appCache;

	@Inject
	@WithAttributes(label = "Playlist", description = "Playlist to visualize", type = InputType.DROPDOWN)
	private UISelectOne<String> playlist;

	@Inject
	@WithAttributes(label = "Songs", description = "artist - title (duration) - album")
	private UISelectMany<Song> songs;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(ViewPlaylistCommand.class).description("View playlist songs")
				.name("Music: View playlist").category(Categories.create("Music"));

	}

	@Override
	public void initializeUI(final UIBuilder builder) throws Exception {
		List<String> playlistNames = new ArrayList<>(playlistManager.getPlaylists().keySet());
		playlistNames.add("");
		Collections.sort(playlistNames);
		playlist.setValueChoices(playlistNames);
		Playlist currentPlaylist = playlistManager.getCurrentPlaylist();
		if (currentPlaylist != null) {
			playlist.setDefaultValue(currentPlaylist.getName());
		} else{
			playlist.setDefaultValue("");
		}
		builder.add(playlist).add(songs);

		playlist.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				String selectedPlaylist = valueChangeEvent.getNewValue() == null ? "":valueChangeEvent.getNewValue().toString();
				List<Song> playlistSongs = null;
				if("".equals(selectedPlaylist)){
					//no playlist selected then all songs in all playlists
					playlistSongs = appCache.getAllSongs();
				} else{
					playlistSongs = playlistManager.getPlaylist(selectedPlaylist).getSongs();
				}

				if(songs != null){
					Collections.sort(playlistSongs);
					songs.setValueChoices(playlistSongs);
					songs.setValue(playlistSongs);
					songs.setNote(playlistSongs.size() + " song(s) found.");
				}

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
