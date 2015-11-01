package com.github.forge.addon.music.model;

import javax.enterprise.inject.Vetoed;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.forge.addon.music.util.Assert.hasText;

/**
 * Created by pestano on 18/10/15.
 */
@Vetoed
public class PlayStatistic {

    private Integer hash;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private String year;
    private AtomicLong playCount;

    public Integer getHash() {
        if(hash == null){
            hash = calculateHash();
        }
        return hash;
    }

    private Integer calculateHash() {
        //only calculate hash for songs with title and artist
        if(title == null || artist == null){
            return null;
        }

        StringBuilder hashSource = new StringBuilder();
        if(hasText(title)){
            hashSource.append(title);
        }
        if(hasText(artist)){
            hashSource.append(artist);
        }
        if(hasText(album)){
            hashSource.append(album);
        }

        return hashSource.toString().hashCode();
    }

    public void setHash(Integer hash) {
        this.hash = hash;
    }

    public AtomicLong getPlayCount() {
        return playCount;
    }

    public void setPlayCount(AtomicLong playCount) {
        this.playCount = playCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(playCount).append(") ").append(getArtist()).append(" - ").append(getTitle()).append(" - ").append(getAlbum());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayStatistic that = (PlayStatistic) o;

        return getHash().equals(that.getHash());

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        return result;
    }
}
