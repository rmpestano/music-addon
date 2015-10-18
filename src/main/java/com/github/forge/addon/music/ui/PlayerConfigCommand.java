package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pestano on 16/08/15.
 */
public class PlayerConfigCommand extends AbstractUICommand {


    @Inject
    Player player;

    @Inject
    PlaylistManager playlistManager;


    @Inject
    @WithAttributes(label = "Select playlist", description = "Playlist to edit", required = true, type = InputType.DROPDOWN)
    private UISelectOne<String> targetPlaylist;

    @Inject
    @WithAttributes(label = "Shuffle", description = "Plays songs in random order", defaultValue = "false")
    private UIInput<Boolean> shuffle;

    @Inject
    @WithAttributes(label = "Repeat", description = "if true the play queue will not removed played songs from the queue", defaultValue = "false")
    private UIInput<Boolean> repeat;


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(PlayerConfigCommand.class)
                .description("Configures the player").name("Music: Player config")
                .category(Categories.create("Music"));
    }

    @Override
    public void initializeUI(UIBuilder uiBuilder) throws Exception {
        List<String> playlistNames = new ArrayList(playlistManager.getPlaylists().keySet());
        Collections.sort(playlistNames);
        targetPlaylist.setValueChoices(playlistNames);
        targetPlaylist.setDefaultValue(playlistManager.getCurrentPlaylist().getName());

        targetPlaylist.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent valueChangeEvent) {
                playlistManager.setCurrentPlaylist(playlistManager.getPlaylist(valueChangeEvent.getNewValue().toString()));
            }
        });

        shuffle.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent valueChangeEvent) {
                player.setShuffle((Boolean) valueChangeEvent.getNewValue());
            }
        });

        repeat.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent valueChangeEvent) {
                player.setRepeat((Boolean) valueChangeEvent.getNewValue());
            }
        });

        uiBuilder.add(targetPlaylist).add(shuffle).add(repeat);
    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext)
            throws Exception {



        return Results.success("Updated player configuration successfully!");

    }


}
