package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AddSongsCommand extends AbstractUICommand {

	@Inject
	PlaylistManager playlistManager;


	@Inject
	@WithAttributes(label = "Select dir", description = "Add songs by dir")
	private UIInput<DirectoryResource> dir;

	@Inject
	@WithAttributes(label = "Songs", description = "Add songs from any dir")
	private UIInputMany<FileResource<?>> songs;

	@Inject
	@WithAttributes(label = "Target playlist", description = "Playlist which songs will be added", required = true, type = InputType.DROPDOWN)
	private UISelectOne<String> playlist
			;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(AddSongsCommand.class).name("add")
				.description("Add songs into a playlist")
				.category(Categories.create("music"));
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		List<String> playlistNames = new ArrayList(playlistManager.getPlaylists().keySet());
		Collections.sort(playlistNames);
		playlist.setValueChoices(playlistNames);
		playlist.setDefaultValue(playlistManager.getCurrentPlaylist().getName());

		builder.add(playlist).add(dir).add(songs);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		List<Song> songsToAdd = new LinkedList<>();
		if(dir.getValue() != null){
			addSongsFromDir(dir.getValue(),songsToAdd);
		}
		Playlist plList = playlistManager.getPlaylist(playlist.getValue().toString());
		plList.addSongs(songsToAdd);
		playlistManager.savePlaylist(plList);
		return Results.success(songsToAdd.size()+" songs added to playlist "+playlist.getValue());
	}

	private void addSongsFromDir(DirectoryResource root, List<Song> songsToAdd) {
		for (Resource<?> resource : root.listResources()) {
			if(resource instanceof DirectoryResource){
				addSongsFromDir(resource.reify(DirectoryResource.class
				),songsToAdd);
			} else{
				if(resource.getName().toLowerCase().endsWith(".mp3")){
					songsToAdd.add(new Song(resource.getFullyQualifiedName()));
				}
			}
		}
	}
}