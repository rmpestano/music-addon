package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditPlaylistCommand extends AbstractUICommand {

	@Inject
	PlaylistManager playlistManager;

	@Inject
	ResourceFactory resourceFactory;


	@Inject
	@WithAttributes(label = "Target playlist", description = "Playlist to edit", required = true, type = InputType.DROPDOWN)
	private UISelectOne<String> targetPlaylist;


	@Inject
	@WithAttributes(label = "Playlist songs", description = "Playlist songs")
	private UIInputMany<FileResource<?>> songs;


	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(EditPlaylistCommand.class).name("Music: Edit playlist")
				.description("Add or remove songs of selected playlist")
				.category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		List<String> playlistNames = new ArrayList(playlistManager.getPlaylists().keySet());
		Collections.sort(playlistNames);
		targetPlaylist.setValueChoices(playlistNames);
		targetPlaylist.setDefaultValue(playlistManager.getCurrentPlaylist().getName());

		builder.add(targetPlaylist).add(songs);

		targetPlaylist.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				String selectedPlaylist = valueChangeEvent.getNewValue().toString();
				Playlist playlist = playlistManager.getPlaylist(selectedPlaylist);
				songs.setValue(createSongResources(playlist.getSongs()));
			}
		});

		List<Song> playlistSongs = playlistManager.getCurrentPlaylist().getSongs();

		songs.setDefaultValue(createSongResources(playlistSongs));
	}

	private List<FileResource<?>> createSongResources(List<Song> playlistSongs) {
		List<FileResource<?>> songsResources = new ArrayList<>();
		for (Song playlistSong : playlistSongs) {
			File file = new File(playlistSong.getLocation());
			if (file.exists()) {
				songsResources.add(resourceFactory.create(FileResource.class, file));
			}
		}
		return songsResources;
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {


		if(songs.getValue() != null){
			List<Song> playlistSongs = new ArrayList<>();
			for (FileResource<?> songFile : songs.getValue()) {
				if(songFile.getName().toLowerCase().endsWith(".mp3")){
					if(songFile.exists()){
						Song song = new Song(songFile.getFullyQualifiedName());
						playlistSongs.add(song);
					}
				}
			}
			Playlist playlist = playlistManager.getPlaylist(targetPlaylist.getValue());
			playlist.getSongs().clear();
			playlist.addSongs(playlistSongs);
			playlistManager.savePlaylist(playlist);

		}

		return Results.success("Playlist updated successfully");
	}


	@Override
	public void validate(UIValidationContext validator) {
		super.validate(validator);
		for (FileResource<?> fileResource : songs.getValue()) {
			if(!fileResource.getName().toLowerCase().endsWith(".mp3")){
				validator.addValidationError(songs,
						"Songs must be in mp3 format");
			}
		}

	}
}