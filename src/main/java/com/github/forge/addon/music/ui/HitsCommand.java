
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
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import com.github.forge.addon.music.model.PlayStatistic;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.model.SongsFilter;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.statistics.StatisticsManager;

/**
 * Created by pestano on 16/08/15.
 */
public class HitsCommand extends AbstractUICommand {

	@Inject
	@WithAttributes(label = "Hits", description = "count - artist - title - album", note = "Selected songs will be added to play queue")
	private UISelectMany<PlayStatistic> songHits;

	@Inject
	@WithAttributes(label = "Size", description = "Number of hits to display", defaultValue = "10", required = true)
	private UIInput<Integer> size;

	@Inject
	@WithAttributes(label = "Reset queue", description = "Will reset play queue and add found songs to its beggining. If false found songs will be append to the end of play queue", defaultValue = "true")
	private UIInput<Boolean> resetQueue;
	
	@Inject
	private StatisticsManager statisticsManager; 

	@Inject
	private Player player;
	
	@Inject
	private SongsFilter songsFilter;


	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(HitsCommand.class).description("Plays most played songs based on your play statistics").name("Music: Hits")
				.category(Categories.create("Music"));
	}

	@Override
	public void initializeUI(UIBuilder uiBuilder) throws Exception {
		List<PlayStatistic> playHits = statisticsManager.getHitsFromStatistics(size.getValue());	
		songHits.setValueChoices(playHits);
		songHits.setValue(playHits);
		songHits.setNote("Found " + playHits.size() + " song(s).");
		if (System.getProperty("resetqueue") != null) {
			resetQueue.setValue(Boolean.valueOf(System.getProperty("resetqueue")));
		}
		if (System.getProperty("hitsSize") != null) {
			size.setValue(Integer.parseInt(System.getProperty("hitsSize")));
		}
		uiBuilder.add(songHits).add(resetQueue);
	}

	@Override
	public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
		if (songHits.hasValue()) {
			Set<Song> newPlayQueue = new HashSet<>();
			for (PlayStatistic playStatistic : songHits.getValue()) {
				Song song = songsFilter.findSong(playStatistic.getArtist(), playStatistic.getTitle(), playStatistic.getAlbum());	
				if(song != null){
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
	 * if reset flag is set the play queue must be reseted and searched songs
	 * must be played immediately
	 * 
	 * 
	 * @param newPlayQueue
	 * @return
	 */
	private Result playSongs(Set<Song> newPlayQueue) {
		Boolean reset = resetQueue.getValue();
		List<Song> playQueue = player.getPlayQueue();
		if (reset) {
			playQueue.clear();
		}
		playQueue.addAll(newPlayQueue);
		if (player.isPlaying()) {
			if (reset) {
				// only go to next if reset is set otherwise just enqueue found
				// songs
				player.next();
			}
		} else {
			// if not paused starts playing found songs
			if (!player.isPaused()) {
				player.play();
			}
		}
		// remember me till forge is restarted
		System.setProperty("resetqueue", reset.toString());
		System.setProperty("hitsSize", size.getValue().toString());
		return Results.success(
				"Added " + newPlayQueue.size() + " song(s) to play queue. Now playing " + player.getCurrentSong());

	}
}
