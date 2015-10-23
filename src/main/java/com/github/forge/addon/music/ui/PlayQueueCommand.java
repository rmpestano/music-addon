package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by pestano on 16/08/15.
 */
public class PlayQueueCommand extends AbstractUICommand {

	@Inject
	Player player;

	@Inject
	@WithAttributes(label = "Songs", description = "Enqueue songs", enabled = false, note = "If you have random configured the order will not be respected")
	private UISelectMany<Song> songs;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(PlayQueueCommand.class).description("Show enqueued songs that will be played")
				.name("Music: Play queue").category(Categories.create("Music"));

	}

	@Override
	public void initializeUI(final UIBuilder builder) throws Exception {
		List<Song> enqueuedSongs = player.getPlayQueue();
		Collections.sort(enqueuedSongs);
		songs.setValueChoices(enqueuedSongs);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
		if (!uiExecutionContext.getUIContext().getProvider().isGUI()) {
			List<Song> enqueuedSongs = player.getPlayQueue();
			Collections.sort(enqueuedSongs);
			PrintStream out = uiExecutionContext.getUIContext().getProvider().getOutput().out();
			out.println(enqueuedSongs.size() + " songs found in playqueue:");
			int i = 1;
			for (Song song : enqueuedSongs) {
				out.println(i + "- " + song);
				i++;
			}
		}
		return Results.success("Command executed successfully!");
	}

}
