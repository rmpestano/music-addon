package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.PlaylistManager;
import com.github.forge.addon.music.model.Song;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.input.events.ValueChangeEvent;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.jboss.forge.addon.ui.annotation.Command;

/**
 * Created by pestano on 16/08/15.
 */
@Singleton
public class NewPlaylistCommand extends AbstractUICommand {

	@Inject
	@WithAttributes(required = true, label = "Name", description = "Playlist title")
	private UIInput<String> name;

	@Inject
	@WithAttributes(label = "Select dir", description = "Add songs to playlist by dir")
	private UIInput<DirectoryResource> dir;

	@Inject
	@WithAttributes(label = "Songs")
	private UIInputMany<FileResource<?>> songs;

	@Inject
	PlaylistManager manager;

	List<Song> playlist;

	UIOutput out;

	@PostConstruct
	public void initializePlaylist() {
		playlist = new ArrayList<>();
		//TODO load songs from external resource e.g: playlists.json
	}

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		out = context.getProvider().getOutput();
		return Metadata.forCommand(NewPlaylistCommand.class)
				.description("Configure a playlist").name("playlist")
				.category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {

		dir.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent event) {
				List<FileResource<?>> fileList = (List<FileResource<?>>) songs
						.getValue();

				DirectoryResource selectedDir = (DirectoryResource) event
						.getNewValue();
				for (Resource<?> resource : selectedDir.listResources()) {
					FileResource<?> fileToAdd = resource
							.reify(FileResource.class);
					if (fileToAdd != null && fileToAdd.exists()
							&& !fileList.contains(fileToAdd)) {
						fileList.add(fileToAdd);
					}
				}
				//files.setValue(fileList);
				for (FileResource<?> fileResource : ((List<FileResource<?>>) songs
						.getValue())) {
					out.out().println(fileResource.getName());
				}

			}

		});
		uiBuilder.add(name).add(dir).add(songs);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext)
			throws Exception {
		return Results.success("done!");
	}

	@Command(value = "test", categories = "music")
	public String test() {
		return "Command test executed";
	}

}
