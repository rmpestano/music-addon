package com.github.forge.addon.music.model;


import com.mpatric.mp3agic.Mp3File;
import org.apache.commons.lang.time.DurationFormatUtils;

import javax.enterprise.inject.Vetoed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pestano on 21/08/15.
 */
@Vetoed
public class Song implements Serializable{

    private String location;
    private String title;
    private String artist;
    private String album;
    private String year;
    private String genre;
    public String duration;

    public Song() {
    }

    private transient Mp3File mp3File;

    public String getLocation() {
        return location;
    }

    public Song(String location) {
        this.location = location;
    }

    public String getTitle() {
        if (title == null) {
            try {
                title = loadTitle();
            } catch (Exception e) {
                title = "";
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read song title from file:" + location, e);
            }
        }
        return title;
    }

    public String getYear() {
        if (year == null) {
            try {
                year = loadYear();
            } catch (Exception e) {
                year = "";
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read song year from file:" + location, e);
            }
        }
        return year;
    }

    public String getGenre() {
        if (genre == null) {
            try {
                genre = loadGenre();
            } catch (Exception e) {
                genre = "";
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read song genre from file:" + location, e);
            }
        }
        return genre;
    }

    public String getAlbum() {
        if (album == null) {
            try {
                album = loadAlbum();
            } catch (Exception e) {
                album = "";
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read song album from file:" + location, e);
            }
        }
        return album;
    }

    public String getDuration() {
        if (duration == null) {
            try {
                duration = loadDuration();
            } catch (Exception e) {
                duration = "";
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read song duration from file:" + location, e);
            }
        }
        return duration;
    }


    private String loadGenre() {
        Mp3File mp3 = getMp3File();
        if(mp3 == null){
            return "Song not found at location "+location;
        }
        if (mp3.hasId3v1Tag()) {
            return mp3.getId3v1Tag().getGenreDescription();
        } else {
            return mp3.getId3v2Tag().getGenreDescription();
        }
    }

    private String loadTitle() {
        Mp3File mp3 = getMp3File();
        if(mp3 == null){
            return "Song not found at location "+location;
        }
        if (mp3.hasId3v1Tag()) {
            return mp3.getId3v1Tag().getTitle();
        } else {
            return mp3.getId3v2Tag().getTitle();
        }
    }

    private String loadYear() {
        Mp3File mp3 = getMp3File();
        if(mp3 == null){
            return "Song not found at location "+location;
        }
        if (mp3.hasId3v1Tag()) {
            return mp3.getId3v1Tag().getYear();
        } else {
            return mp3.getId3v2Tag().getYear();
        }
    }

    public String getArtist() {
        if (artist == null) {
            try {
                artist = loadArtist();
            } catch (Exception e) {
                artist = "";
            }
        }
        return artist;
    }

    private String loadArtist() {
        Mp3File mp3 = getMp3File();
        if(mp3 == null){
            return "Song not found at location "+location;
        }
        if (mp3.hasId3v1Tag()) {
            return mp3.getId3v1Tag().getArtist();
        } else {
            return mp3.getId3v2Tag().getArtist();
        }
    }

    private String loadAlbum() {
        Mp3File mp3 = getMp3File();
        if(mp3 == null){
            return "Song not found at location "+location;
        }
        if (mp3.hasId3v1Tag()) {
            return mp3.getId3v1Tag().getAlbum();
        } else {
            return mp3.getId3v2Tag().getAlbum();
        }
    }

    private String loadDuration() {
        Mp3File mp3 = getMp3File();
        if(mp3 == null){
            return "Song not found at location "+location;
        }
        return DurationFormatUtils.formatDuration(mp3.getLengthInMilliseconds(),"m:ss");
    }

    public Mp3File getMp3File() {
        if (mp3File == null) {
            try {
                mp3File = new Mp3File(location);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read mp3 file from:" + location, e);
                return null;
            }
        }
        return mp3File;
    }

    @Override
    public String toString() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        return location.equals(song.location);

    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode():31;
    }
}
