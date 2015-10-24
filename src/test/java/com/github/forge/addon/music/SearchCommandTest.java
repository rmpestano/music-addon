/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.playlist.PlaylistManager;
import com.github.forge.addon.music.ui.SearchCommand;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.forge.addon.music.util.Utils.newLine;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@RunWith(Arquillian.class)
public class SearchCommandTest extends BaseTest {

	@Inject
	private PlaylistManager manager;

	@Inject
	private ShellTest shellTest;

	@Inject
	private UITestHarness uiTestHarness;


	@Deployment
	@AddonDependencies
	public static AddonArchive getDeployment() {
		return ShrinkWrap.create(AddonArchive.class).addClass(BaseTest.class).addBeansXML();
	}

	@Before
	public void before() throws TimeoutException {
		shellTest.execute("music-add-songs --targetPlaylist " + PlaylistManager.DEFAULT_PLAYLIST
				+ " --dir " + Paths.get("target/test-classes").toAbsolutePath(), 5, TimeUnit.SECONDS);
	}



	@Test
	public void shouldSearchByArtist() throws Exception {

		try (WizardCommandController controller = uiTestHarness.createWizardController(SearchCommand.class))
		{
			controller.initialize();
			assertFalse(controller.canMoveToNextStep());
			controller.setValueFor("artist", "ensi");
			assertTrue(controller.canMoveToNextStep());
			Result result = controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
		}

	}

	@Test
	public void shouldSearchByTitle() throws Exception {

		try (WizardCommandController controller = uiTestHarness.createWizardController(SearchCommand.class))
		{
			controller.initialize();
			assertFalse(controller.canMoveToNextStep());
			controller.setValueFor("title", "judge");
			assertTrue(controller.canMoveToNextStep());
			Result result = controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
		}

	}


	@Test
	public void shouldSearchByGenreAndArtist() throws Exception {

		try (WizardCommandController controller = uiTestHarness.createWizardController(SearchCommand.class))
		{
			controller.initialize();
			assertFalse(controller.canMoveToNextStep());
			controller.setValueFor("artist", "ferum");
			controller.setValueFor("genre", "metal");
			assertTrue(controller.canMoveToNextStep());
			Result result = controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
		}

	}


	@Test
	public void shouldNotGoToNextStepIfNoSongIfFound() throws Exception {

		try (WizardCommandController controller = uiTestHarness.createWizardController(SearchCommand.class))
		{
			controller.initialize();
			assertFalse(controller.canMoveToNextStep());
			controller.setValueFor("artist", "nonexistingsong");
			assertFalse(controller.canMoveToNextStep());
			controller.setValueFor("title", "axe");//even if one criteria match
			assertFalse(controller.canMoveToNextStep());
			controller.setValueFor("artist", "ensi"); //now both criteria match
			assertTrue(controller.canMoveToNextStep());
			Result result = controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
		}

	}

}