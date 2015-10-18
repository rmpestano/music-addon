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
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class AddSongsCommand extends AbstractUICommand {

    @Inject
    PlaylistManager playlistManager;


    private DirectoryResource lastSelectedDir;


    @Inject
    @WithAttributes(label = "Target playlist", description = "Playlist which songs will be added", required = true, type = InputType.DROPDOWN)
    private UISelectOne<String> targetPlaylist;

    @Inject
    @WithAttributes(label = "Select dir", description = "Add songs by dir")
    private UIInput<DirectoryResource> dir;


    @Inject
    @WithAttributes(label = "Select songs", description = "Add songs from any dir")
    private UIInputMany<FileResource<?>> songs;


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(AddSongsCommand.class).name("Music: Add songs")
                .description("Add songs into a playlist")
                .category(Categories.create("music"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        List<String> playlistNames = new ArrayList(playlistManager.getPlaylists().keySet());
        Collections.sort(playlistNames);
        targetPlaylist.setValueChoices(playlistNames);
        targetPlaylist.setDefaultValue(playlistManager.getCurrentPlaylist().getName());
        if (lastSelectedDir != null) {
            dir.setDefaultValue(lastSelectedDir);
        }
        builder.add(targetPlaylist).add(dir).add(songs);

        dir.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent valueChangeEvent) {
                if (valueChangeEvent.getNewValue() != null) {
                    lastSelectedDir = (DirectoryResource) valueChangeEvent.getNewValue();
                }
            }
        });

        targetPlaylist.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent valueChangeEvent) {
                String selectedPlaylist = valueChangeEvent.getNewValue().toString();
                playlistManager.setCurrentPlaylist(playlistManager.getPlaylist(selectedPlaylist));
            }
        });
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        List<Song> songsToAdd = new LinkedList<>();
        if (dir.getValue() != null) {
            addSongsFromDir(dir.getValue(), songsToAdd);
        }

        if (songs.getValue() != null) {
            addSongFiles((List<FileResource<?>>) songs.getValue(), songsToAdd);
        }

        Playlist plList = playlistManager.getPlaylist(targetPlaylist.getValue().toString());
        plList.addSongs(songsToAdd);
        playlistManager.savePlaylist(plList);
        int numSongsAdded = songsToAdd.size();
        songsToAdd.clear();
        songs.setValue(null);
        dir.setValue(null);
        return Results.success(numSongsAdded + " song(s) added to playlist: " + plList.getName());
    }

    private void addSongFiles(List<FileResource<?>> songFiles, List<Song> songsToAdd) {
        for (FileResource<?> songFile : songFiles) {
            if (songFile.getName().toLowerCase().endsWith(".mp3") && songFile.exists()) {
                Song song = new Song(songFile.getFullyQualifiedName());
                if (!songsToAdd.contains(song)) {
                    songsToAdd.add(song);
                }
            }
        }
    }

    private void addSongsFromDir(DirectoryResource root, List<Song> songsToAdd) {
        for (Resource<?> resource : root.listResources()) {
            if (resource instanceof DirectoryResource) {
                addSongsFromDir(resource.reify(DirectoryResource.class
                ), songsToAdd);
            } else {
                if (resource.getName().toLowerCase().endsWith(".mp3")) {
                    songsToAdd.add(new Song(resource.getFullyQualifiedName()));
                }
            }
        }
    }

    @Override
    public void validate(UIValidationContext validator) {
        super.validate(validator);
        for (FileResource<?> fileResource : songs.getValue()) {
            if (!fileResource.getName().toLowerCase().endsWith(".mp3")) {
                validator.addValidationError(songs,
                        "Songs must be in mp3 format");
            }
        }

    }
}