package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;

/**
 * Created by pestano on 16/08/15.
 */
public class ResumeCommand extends AbstractUICommand {


    @Inject
    Player player;


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(ResumeCommand.class)
                .description("Resumes a paused song").name("Music: Resume")
                .category(Categories.create("Music"));
    }

    @Override
    public void initializeUI(UIBuilder uiBuilder) throws Exception {
    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext)
            throws Exception {


        Song song = player.getCurrentSong();
        if (song == null) {
            return Results.fail("Cannot resume because there is no song in queue");
        }
        if (player.isPlaying()) {
            return Results.fail("Cannot resume because player is playing");
        }
        player.resume();
        return Results.success("Resumed: " + song.info());

    }


}
