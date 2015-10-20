package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by pestano on 16/08/15.
 */
public class StatusCommand extends AbstractUICommand {


	@Inject
	Player player;

	@Inject
	PlaylistManager playlistManager;



	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(StatusCommand.class)
				.name("Music: Status")
				.description("Show which song is playing (if any), song length, time left and play queue")
				.category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext)
			throws Exception {


		if(!player.isPlaying()){
			return Results.success("player is stopped");
		} else{
			Song song = player.getCurrentSong();
			return Results.success(song.info() + ". Played time: "+player.getPlayingTime() + ". Playlist: "+playlistManager.getCurrentPlaylist().getName() + getNextSongInfo());
		}

	}

	private String getNextSongInfo() {
		List<Song> playQueue = player.getPlayQueue();
		if(playQueue != null && !playQueue.isEmpty()){
			Song nextSong = player.getPlayQueue().get(0);
			return (". Next song: "+ nextSong.getTitle() + " ("+nextSong.getDuration()+")");
		}

		return "";

	}


}
