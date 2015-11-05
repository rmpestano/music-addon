
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

    @Inject
    @WithAttributes(label = "Select playlist", description = "Current playlist", type = InputType.DROPDOWN)
    private UISelectOne<String> playlist;

    @Inject
    @WithAttributes(label = "Random", description = "Plays songs in random order")
    private UIInput<Boolean> random;

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
    public void initializeUI(final UIBuilder uiBuilder) throws Exception {
        List<String> playlistNames = new ArrayList();
        playlistNames.add("");
        playlistNames.addAll(playlistManager.getPlaylists().keySet());
        Collections.sort(playlistNames);
        playlist.setValueChoices(playlistNames);
        if (playlistManager.getCurrentPlaylist() != null) {
            playlist.setDefaultValue(playlistManager.getCurrentPlaylist().getName());
        }

        random.setDefaultValue(player.isRandom());

        repeat.setDefaultValue(player.isRepeat());

        songStatistics.setDefaultValue(player.isGenerateStatistics());

        uiBuilder.add(playlist).add(random).add(repeat).add(songStatistics);
    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext)
            throws Exception {

        String currentPlaylistName = playlistManager.getCurrentPlaylist() != null ? playlistManager.getCurrentPlaylist().getName() : "";
        boolean playlistChanged = !playlist.getValue().equals(currentPlaylistName);//do not use valueChangeListener
        if (playlistChanged) {
           if("".equals(playlist.getValue())){
               playlistManager.setCurrentPlaylist(null);//will enqueue all songs from all playlists
           } else{
               playlistManager.setCurrentPlaylist(playlistManager.getPlaylist(playlist.getValue()));
           }
            if(player.isPlaying()){
                //if it is playing and playlist has changed then start to play new playlist songs
                playlistEvent.fire(new ChangePlaylistEvent(playlistManager.getCurrentPlaylist()));
            }
        }

        player.setRepeat(repeat.getValue());
        player.setRandom(random.getValue());
        player.setGenerateStatistics(songStatistics.getValue());

        return Results.success("Updated player configuration successfully!");

    }


}
