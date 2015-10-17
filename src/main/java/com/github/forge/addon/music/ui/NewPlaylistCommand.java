package com.github.forge.addon.music.ui;

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
public class NewPlaylistCommand extends AbstractUICommand {

	@Inject
	@WithAttributes(required = true, label = "Name", description = "Playlist name", requiredMessage = "Provide playlist name")
	private UIInput<String> name;


	@Inject
	PlaylistManager playlistManager;


	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(NewPlaylistCommand.class)
				.description("Creates a playlist").name("new playlist")
				.category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {
		uiBuilder.add(name);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext)
			throws Exception {

		String playlistName = name.getValue().toString();
		if(playlistManager.getPlaylist(playlistName) != null){
			return Results.fail("Playlist "+playlistName +" already exists");
		}
		playlistManager.createPlaylist(playlistName);
		return Results.success("Playlist "+playlistName +" created!");
	}



}
