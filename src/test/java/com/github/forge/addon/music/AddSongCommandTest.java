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
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@RunWith(Arquillian.class)
public class AddSongCommandTest {

    private static final String TEST_PLAY_LIST_NAME = "test-playlist";


    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private ShellTest shellTest;

    @Inject
    PlaylistManager playlistManager;

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }


    @Before
    public void before() {
        playlistManager.createPlaylist(TEST_PLAY_LIST_NAME);
    }

    @Test
    public void shouldAddNewPlaylist() throws Exception {
        Result result = shellTest.execute("add-song --playlist " + TEST_PLAY_LIST_NAME +" --dir " + Paths.get("target/test-classes").toAbsolutePath(), 25, TimeUnit.SECONDS);
        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(playlistManager.hasPlaylist(TEST_PLAY_LIST_NAME), is(true));
        Playlist playlist = playlistManager.getPlaylist(TEST_PLAY_LIST_NAME);
        assertNotNull(playlist);
        Song song = new Song(Paths.get("target/test-classes").toAbsolutePath() +"/axe.mp3");
        assertThat(playlist.getSongs(),hasItem(song));

    }


}