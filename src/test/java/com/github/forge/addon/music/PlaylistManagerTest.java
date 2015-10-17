/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="rmpestano@gmail.com">Rafael Pestano</a>
 */
@RunWith(Arquillian.class)
public class PlaylistManagerTest extends BaseTest {


    private Song song;


    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {

        return ShrinkWrap.create(AddonArchive.class).addClass(BaseTest.class).addBeansXML();
    }


    @Test
    public void shouldCreateDefaultPlayList() throws Exception {
        assertThat(playlistManager.hasPlaylist(PlaylistManager.DEFAULT_PLAYLIST), is(false));
        playlistManager.getPlaylists();//should trigger the creation of default playlist
        assertThat(playlistManager.hasPlaylist(PlaylistManager.DEFAULT_PLAYLIST), is(true));
        Playlist playlist = playlistManager.getPlaylist(PlaylistManager.DEFAULT_PLAYLIST);
        assertNotNull(playlist);
        assertEquals("default", playlist.getName());
    }

    @Test
    public void shouldCreatePlayList() throws Exception {
        assertThat(playlistManager.hasPlaylist("myplaylist"), is(false));
        playlistManager.createPlaylist("myplaylist");//should trigger the creation of default playlist
        assertThat(playlistManager.hasPlaylist("myplaylist"), is(true));
        Playlist playlist = playlistManager.getPlaylist("myplaylist");
        assertNotNull(playlist);
        assertEquals("myplaylist", playlist.getName());
    }

    @Test
    public void shouldLoadSongProperties() {
        Playlist playlist = playlistManager.getPlaylist("default");
        assertThat(playlist.getName(), is(equalTo("default")));
        Song sample = getSampleMp3();
        assertThat(sample.getMp3File(), is(notNullValue()));
        assertThat(sample.getTitle(), is(equalTo("Axe Of Judgement")));
        assertThat(sample.getArtist(), is(equalTo("Ensiferum")));
        assertThat(sample.getAlbum(), is(equalTo("One Man Army")));
        assertThat(sample.getYear(), is(equalTo("2015")));
        assertThat(sample.getGenre(), is(equalTo("Metal")));
        assertThat(sample.getDuration(), is(equalTo("0:14")));
    }

    @Test
    public void shouldSaveSong() {
        Song song = getSampleMp3();
        Playlist playlist = playlistManager.getPlaylist("default");
        playlist.addSong(song);
        playlistManager.savePlaylist(playlist);
        JsonObject defaultPlayList = playlistManager.loadPlaylist("default");
        assertThat(defaultPlayList.getString("name"), is(equalTo("default")));
        JsonArray songsArray = defaultPlayList.getJsonArray("songs");
        assertThat(songsArray, is(notNullValue()));
        assertThat(songsArray.size(), is(equalTo(1)));
        assertThat(((JsonObject) songsArray.get(0)).getString("title"), is(equalTo("Axe Of Judgement")));
    }

    @Test
    public void shouldRemoveSong() {
        shouldSaveSong();
        Playlist playlist = playlistManager.getPlaylist("default");
        playlistManager.removeSong(playlist, getSampleMp3());
        JsonObject defaultPlayList = playlistManager.loadPlaylist("default");
        assertThat(defaultPlayList.getString("name"), is(equalTo("default")));
        JsonArray songsArray = defaultPlayList.getJsonArray("songs");
        assertThat(songsArray, is(notNullValue()));
        assertThat(songsArray.size(), is(equalTo(0)));
    }

    private Song getSampleMp3() {
        if (song == null) {
            song = new Song(Paths.get("target/test-classes").toAbsolutePath() + "/axe.mp3");
        }
        return song;
    }
}