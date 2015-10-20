package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
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
import java.util.List;

public class RemovePlaylistCommand extends AbstractUICommand {

    @Inject
    PlaylistManager playlistManager;

    @Inject
    @WithAttributes(label = "Name", description = "Playlist to be deleted",
            required = true, requiredMessage = "Select a playlist to remove", type = InputType.DROPDOWN)
    private UISelectOne<String> name;


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(RemovePlaylistCommand.class).name("Music: Remove playlist")
            .description("Removes a playlist")
            .category(Categories.create("Music"));
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        List<String> playlistNames = new ArrayList(playlistManager.getPlaylists().keySet());
        Collections.sort(playlistNames);
        name.setValueChoices(playlistNames);

        builder.add(name);

    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        boolean remove = context.getPrompt().promptBoolean("Remove playlist " + name.getValue() + "?");
        if(remove){
            playlistManager.removePlaylist(name.getValue());
        }
        return Results.success(name.getValue() + " removed with success!");
    }


}