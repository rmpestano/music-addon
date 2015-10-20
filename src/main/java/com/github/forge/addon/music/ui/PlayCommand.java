package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;

/**
 * Created by pestano on 16/08/15.
 */
public class PlayCommand extends AbstractUICommand {


	@Inject
	Player player;


	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(PlayCommand.class)
				.description("Plays songs from current playlist").name("Music: Play")
				.category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext)
			throws Exception {

		if(player.isPlaying()){
			return Results.success("Player is already playing. Use next command to change song.");
		}else{
			if(player.getPlayQueue() == null || player.getPlayQueue().isEmpty()){
				return Results.fail("No songs to play, use music-add-songs command");
			}
			player.setUiContext(uiExecutionContext.getUIContext());
			player.play();
			Song song = player.getCurrentSong();
			return Results.success("Now playing: "+song.info());

		}
	}



}
