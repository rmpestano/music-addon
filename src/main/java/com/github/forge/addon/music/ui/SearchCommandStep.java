package com.github.forge.addon.music.ui;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rafael-pestano on 23/10/2015.
 */
public class SearchCommandStep extends AbstractUICommand implements UIWizardStep {

  @Inject
  Player player;

  @Inject
  @WithAttributes(label = "Songs found", description = "artist - title (album)", note = "Selected songs will be added to play queue and played after command execution")
  private UISelectMany<Song> songsFound;

  @Override
  public Metadata getMetadata(UIContext context)
  {
    return Metadata.from(super.getMetadata(context), getClass()).description("Select and play songs ")
        .name("Music: Search").category(Categories.create("Music"));
  }

  @Override
  public NavigationResult next(UINavigationContext context) throws Exception {
    return null;//last step
  }

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    List<Song> songs = (List<Song>) builder.getUIContext().getAttributeMap().get("songs");
    Collections.sort(songs);
    songsFound.setValueChoices(songs);
    songsFound.setDefaultValue(songs);
    songsFound.setNote(songs.size() + " song found.");
    builder.add(songsFound);
  }

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    if (songsFound.getValue() != null) {
      List<Song> newPlayQueue = new ArrayList<>();
      for (Song song : songsFound.getValue()) {
        if (!newPlayQueue.contains(song)) {
          newPlayQueue.add(song);
        }
      }

      if (!newPlayQueue.isEmpty()) {
        player.getPlayQueue().clear();
        player.getPlayQueue().addAll(newPlayQueue);
        if (player.isPlaying()) {
          player.next();
        } else {
          player.play();
        }
        return Results.success("Found " + newPlayQueue.size() + " songs to play. Now playing "
            + player.getCurrentSong().info());
      }
    }
    return Results.success("No songs selected or found.");
  }
}
