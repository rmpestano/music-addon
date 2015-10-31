package com.github.forge.addon.music.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.model.SongsFilter;
import com.github.forge.addon.music.player.Player;

/**
 * Created by rafael-pestano on 23/10/2015.
 */
public class SearchCommandStep extends AbstractUICommand implements UIWizardStep {

  @Inject
  Player player;

  @Inject
  SongsFilter songsFilter;
  
  @Inject
  @WithAttributes(label = "Songs found", description = "artist - title (duration) - album", note = "Selected songs will be added to play queue and played after command execution")
  private UISelectMany<Song> songsFound;
  
  @Inject
  @WithAttributes(label = "Reset queue", description = "Will reset play queue and add found songs to its beggining. If false found songs will be append to the end of play queue", defaultValue="true")
  private UIInput<Boolean> resetQueue;

  @Override
  public Metadata getMetadata(UIContext context){
    return Metadata.from(super.getMetadata(context), getClass()).description("Select and play songs ")
        .name("Music: Search").category(Categories.create("Music"));
  }


  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    songsFound.setValueChoices(songsFilter.getFilteredSongs());
    songsFound.setValue(songsFilter.getFilteredSongs());
    songsFound.setNote("Found "+songsFilter.getFilteredSongs().size() + " song(s).");
    if(System.getProperty("resetqueue") != null){
    	resetQueue.setValue(Boolean.valueOf(System.getProperty("resetqueue")));
    }
    
    builder.add(songsFound).add(resetQueue);
  }

    @Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
		if (songsFound.hasValue()) {
			Set<Song> newPlayQueue = new HashSet<>();
			for (Song song : songsFound.getValue()) {
				if (!newPlayQueue.contains(song)) {
					newPlayQueue.add(song);
				}
			}

			if (!newPlayQueue.isEmpty()) {
				return playSongs(newPlayQueue);
			}
		}

		return Results.success("No songs found.");

	}

    /**
     * if reset flag is set the play queue must be reseted
     * and searched songs must be played immediately 
     * 
     * 
     * @param newPlayQueue
     * @return
     */
	private Result playSongs(Set<Song> newPlayQueue) {
		Boolean reset = resetQueue.getValue(); 
		if(reset){
			player.getPlayQueue().clear();	
		}
		player.getPlayQueue().addAll(newPlayQueue);
		if (player.isPlaying()) {
			if(reset){
				//only go to next if reset is set otherwise just enqueue found songs
				player.next();
			}
		} else {
			//if not paused starts playing found songs
			if(!player.isPaused()){
				player.play();
			}
		}
		//remember me till forge is restarted
		System.setProperty("resetqueue", reset.toString());
		return Results.success(
				"Added " + newPlayQueue.size() + " song(s) to play queue. Now playing " + player.getCurrentSong());

	}


	@Override
	public NavigationResult next(UINavigationContext context) throws Exception {
		songsFound.setNote("Found "+songsFilter.getFilteredSongs().size() + " song(s).");
		return null;//last step
	}
}
