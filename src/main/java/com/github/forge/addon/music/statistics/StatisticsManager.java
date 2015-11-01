package com.github.forge.addon.music.statistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;

import com.github.forge.addon.music.model.PlayStatistic;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.util.Utils;

/**
 * Created by pestano on 18/10/15.
 */
@Singleton
public class StatisticsManager {

    public static final String STATISTICS_FILE = "plays-statistics.json";

	private static final Comparator<? super PlayStatistic> hitsComparator = new Comparator<PlayStatistic>() {

		@Override
		public int compare(PlayStatistic o1, PlayStatistic o2) {
			Long countO1 = o1.getPlayCount().longValue();	
			Long countO2 = o2.getPlayCount().longValue();
			return countO2.compareTo(countO1);
		}
	};

    @Inject
    private Utils utils;
    

    private List<PlayStatistic> playStatistics;

    @PostConstruct
    public void init() {
        playStatistics = new ArrayList<>();
        if (!getStatisticsFile().exists()) {
            createStatisticsFile();
        }
    }

    private void createStatisticsFile() {
        DirectoryResource playlistHomeDir = utils.getForgeHome();
        try {
            FileResource<?> playListFile = playlistHomeDir.getChild(STATISTICS_FILE).reify(FileResource.class);
            playListFile.setContents(Json.createObjectBuilder().add("statistics", Json.createArrayBuilder().build()).build().toString());
            playListFile.createNewFile();

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not create playlist.", e);
        }
    }


    public void addStatistic(Song playedSong) {
        if (playedSong == null) {
            return;
        }
        synchronized (playStatistics) {
            PlayStatistic playStatistic = new PlayStatistic();
            playStatistic.setTitle(playedSong.getTitle());
            playStatistic.setAlbum(playedSong.getAlbum());
            playStatistic.setArtist(playedSong.getArtist());

            int indexOf = playStatistics.indexOf(playStatistic);
            if (indexOf == -1) {
                playStatistic.setGenre(playedSong.getGenre());
                playStatistic.setPlayCount(new AtomicLong(1));
                playStatistic.setYear(playedSong.getYear());
                playStatistics.add(playStatistic);
            } else {
                //already exists, just increment
                AtomicLong playCount = playStatistics.get(indexOf).getPlayCount();
                playCount.incrementAndGet();
            }
        }

        persistStatistics(shouldPersistStatistics());
    }


    public List<PlayStatistic> getPlayStatistics() {
    	 
    	
        return playStatistics;
    }

    /**
     * persists play statistics into forge_home folder
     */
    public void persistStatistics(boolean shouldPersist) {
        //persist after 10 plays
        if (shouldPersist) {
            FileResource<?> statisticsFile = getStatisticsFile();
            JsonArrayBuilder newStatisticsArray = Json.createArrayBuilder();
            List<PlayStatistic> persistedStatistics = readPersistedStatistics();//already persisted statistics
            for (PlayStatistic playStatistic : playStatistics) {
                int indexOf = persistedStatistics.indexOf(playStatistic);
                if (indexOf != -1) {
                    persistedStatistics.get(indexOf).getPlayCount().getAndAdd(playStatistic.getPlayCount().get());
                } else {//statistic doesn't exist, create entry
                    persistedStatistics.add(playStatistic);
                }
            }
            //FIXME find a better way to (re)create/copy the statistics array (note that JsonArray is immutable)
            for (PlayStatistic playStatistic : persistedStatistics) {
                JsonObject newStatisticObject = Json.createObjectBuilder()
                        .add("hash", playStatistic.getHash())
                        .add("title", playStatistic.getTitle())
                        .add("artist", playStatistic.getArtist())
                        .add("album", playStatistic.getAlbum())
                        .add("genre", playStatistic.getGenre())
                        .add("year", playStatistic.getYear())
                        .add("count", playStatistic.getPlayCount().longValue()).build();
                newStatisticsArray.add(newStatisticObject);
            }
            statisticsFile.setContents(Json.createObjectBuilder().add("statistics",newStatisticsArray.build()).build().toString());
            playStatistics.clear();
        }
    }

    private List<PlayStatistic> readPersistedStatistics() {
    	FileResource<?> statisticsFile = getStatisticsFile();
    	JsonArray persistedJsonArray = Json.createReader(statisticsFile.getResourceInputStream()).readObject().getJsonArray("statistics");
        List<PlayStatistic> persistedStatistics = readFromStatisticsFile(persistedJsonArray);
		return persistedStatistics;
	}

	/**
     * should persist to local json statistics file on every 10 played songs
     */
    private boolean shouldPersistStatistics() {
        if(playStatistics.size() >=10){
            return true;
        }
        int countSum = 0;
        for (PlayStatistic playStatistic : playStatistics) {
            countSum = countSum + playStatistic.getPlayCount().intValue();
        }
        if(countSum >= 10){
            return true;

        }
        return false;
    }

    private List<PlayStatistic> readFromStatisticsFile(JsonArray jsonArray) {
        List<PlayStatistic> playStatistics = new ArrayList<>();
        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = (JsonObject) jsonValue;
            PlayStatistic playStatistic = new PlayStatistic();
            playStatistic.setHash(jsonObject.getInt("hash"));
            playStatistic.setPlayCount(new AtomicLong(jsonObject.getInt("count")));
            playStatistic.setTitle(jsonObject.getString("title"));
            playStatistic.setArtist(jsonObject.getString("artist"));
            playStatistic.setAlbum(jsonObject.getString("album"));
            playStatistic.setGenre(jsonObject.getString("genre"));
            playStatistic.setYear(jsonObject.getString("year"));
            playStatistics.add(playStatistic);
        }
        return playStatistics;

    }
    
    /**
     * 
     * @param size number of top most played songs to return
     * @return 
     */
    public List<PlayStatistic> getHitsFromStatistics(int size) {
		List<PlayStatistic> mostPlayedSongs = new ArrayList<>();
		List<PlayStatistic> persistedStatistics = readPersistedStatistics();
		persistedStatistics.sort(hitsComparator);
		
		if(persistedStatistics.size() <= size){
			mostPlayedSongs = persistedStatistics.subList(0, size-1);
		} else{
			mostPlayedSongs = persistedStatistics;
		}
		
		return mostPlayedSongs;
		
	}


    /**
     * share your play statistics with the world
     * persist local statistics into an application in the cloud (openshift)
     */
    public void exportStatistics() {

    }


    public FileResource<?> getStatisticsFile() {
        FileResource<?> playlistFile = utils.getForgeHome().getChild(STATISTICS_FILE).reify(FileResource.class);
        return playlistFile;
    }
}
