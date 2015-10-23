package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.event.ChangePlaylistEvent;
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

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pestano on 16/08/15.
 */
public class ConfigCommand extends AbstractUICommand {


    @Inject
    Player player;

    @Inject
    PlaylistManager playlistManager;

    @Inject
    Event<ChangePlaylistEvent> playlistEvent;

    private boolean playlstChanged;


    @Inject
    @WithAttributes(label = "Select playlist", description = "Current playlist", type = InputType.DROPDOWN)
    private UISelectOne<String> playlist;

    @Inject
    @WithAttributes(label = "Shuffle", description = "Plays songs in random order" )
    private UIInput<Boolean> shuffle;

    @Inject
    @WithAttributes(label = "Repeat", description = "if true the play queue will not remove played songs from the queue")
    private UIInput<Boolean> repeat;

    @Inject
    @WithAttributes(label = "Generate statistics", description = "Generate statistics of played songs at FORGE_HOME that can be shared using 'Music: export statistics' command")
    private UIInput<Boolean> songStatistics;


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(ConfigCommand.class)
                .description("Configures the player").name("Music: Config")
                .category(Categories.create("Music"));
    }

    @Override
    public void initializeUI(UIBuilder uiBuilder) throws Exception {
        List<String> playlistNames = new ArrayList(playlistManager.getPlaylists().keySet());
        Collections.sort(playlistNames);
        playlist.setValueChoices(playlistNames);
        playlist.setDefaultValue(playlistManager.getCurrentPlaylist().getName());

        playlist.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent valueChangeEvent) {
                if (valueChangeEvent.getNewValue() != null && !valueChangeEvent.getNewValue().toString().equals(playlistManager.getCurrentPlaylist().getName())) {
                    playlistManager.setCurrentPlaylist(playlistManager.getPlaylist(valueChangeEvent.getNewValue().toString()));
                    playlstChanged = true;//if playlist has changed fire event
                }
            }
        });

        shuffle.setDefaultValue(player.isShuffle());

        repeat.setDefaultValue(player.isRepeat());

        songStatistics.setDefaultValue(player.isGenerateStatistics());


        uiBuilder.add(playlist).add(shuffle).add(repeat).add(songStatistics);
    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext)
            throws Exception {

        if(playlstChanged && player.isPlaying()){
            playlistEvent.fire(new ChangePlaylistEvent(playlistManager.getCurrentPlaylist()));
        }

        player.setRepeat(repeat.getValue());
        player.setShuffle(shuffle.getValue());
        player.setGenerateStatistics(songStatistics.getValue());

        return Results.success("Updated player configuration successfully!");

    }


}
