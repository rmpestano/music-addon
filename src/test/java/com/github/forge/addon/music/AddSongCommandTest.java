/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.playlist.PlaylistManager;

/**
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@RunWith(Arquillian.class)
public class AddSongCommandTest extends BaseTest {

	@Inject
	private PlaylistManager manager;

	@Inject
	private ShellTest shellTest;

	@Deployment
	@AddonDependencies
	public static AddonArchive getDeployment() {
		return ShrinkWrap.create(AddonArchive.class).addClass(BaseTest.class).addBeansXML();
	}

	@Test
	public void shouldAddNewPlaylist() throws Exception {
		manager.createPlaylist(PlaylistManager.DEFAULT_PLAYLIST);
		Result result = shellTest.execute("music-add-songs --targetPlaylist " + PlaylistManager.DEFAULT_PLAYLIST
				+ " --dir " + Paths.get("target/test-classes").toAbsolutePath(), 5, TimeUnit.SECONDS);
		assertThat(result, not(instanceOf(Failed.class)));
		assertThat(result.getMessage(),
				is(equalTo("1 song(s) added to playlist: " + PlaylistManager.DEFAULT_PLAYLIST)));
		assertThat(playlistManager.hasPlaylist(PlaylistManager.DEFAULT_PLAYLIST), is(true));
		Playlist playlist = playlistManager.getPlaylist(PlaylistManager.DEFAULT_PLAYLIST);
		assertNotNull(playlist);
		Song song = new Song(Paths.get("target/test-classes").toAbsolutePath() + "/axe.mp3");
		assertThat(playlist.getSongs(), hasItem(song));

	}

}