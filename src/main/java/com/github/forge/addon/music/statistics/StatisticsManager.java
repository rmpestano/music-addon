package com.github.forge.addon.music.statistics;

import com.github.forge.addon.music.model.PlayStatistic;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.util.Utils;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 18/10/15.
 */
@Singleton
public class StatisticsManager {

    public static final String STATISTICS_FILE = "plays-statistics.json";

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

        persistStatistics();
    }


    public List<PlayStatistic> getPlayStatistics() {
        return playStatistics;
    }

    /**
     * persists play statistics into forge_home folder
     */
    public void persistStatistics() {
        //persist after 10 plays
        if (shouldPersistStatistics()) {
            FileResource<?> statisticsFile = getStatisticsFile();
            JsonArray persistedJsonArray = Json.createReader(statisticsFile.getResourceInputStream()).readObject().getJsonArray("statistics");
            JsonArrayBuilder newStatisticsArray = Json.createArrayBuilder();
            List<PlayStatistic> persistedStatistics = readFromStatisticsFile(persistedJsonArray);//already persisted statistics
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

    /**
     * should persist to local json statistics file on every 10 played songs
     */
    private boolean shouldPersistStatistics() {
        if(playStatistics.size() >=1){
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
