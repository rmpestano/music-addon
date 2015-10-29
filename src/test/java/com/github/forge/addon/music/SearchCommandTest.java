/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import com.github.forge.addon.music.playlist.PlaylistManager;
import com.github.forge.addon.music.ui.SearchCommand;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.result.CompositeResult;
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
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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
			assertTrue(controller.canMoveToNextStep());
			controller.setValueFor("artist", "ensi");
			assertTrue(controller.canMoveToNextStep());
			CompositeResult result = (CompositeResult) controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
		}

	}

	@Test
	public void shouldSearchByTitle() throws Exception {

		try (WizardCommandController controller = uiTestHarness.createWizardController(SearchCommand.class))
		{
			controller.initialize();
			assertTrue(controller.canMoveToNextStep());
			controller.setValueFor("title", "judge");
			CompositeResult result = (CompositeResult) controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
			//assertThat(result.getMessage(), is(equalTo("Found 1 song(s) to play. Now playing " )));
			//assertThat(result.getResults().get(0).getMessage(), is(equalTo("Found 1 song(s) to play. Now playing " )));

		}

	}

	@Test
	public void shouldSearchByGenreAndArtist() throws Exception {

		try (WizardCommandController controller = uiTestHarness.createWizardController(SearchCommand.class))
		{
			controller.initialize();
			assertTrue(controller.canMoveToNextStep());
			controller.setValueFor("artist", "ferum");
			controller.setValueFor("genre", "metal");
			CompositeResult result = (CompositeResult) controller.next().execute();
			assertThat(result, not(instanceOf(Failed.class)));
			//assertThat(result.getResults().get(0).getMessage(), is(equalTo("Found 1 song(s) to play. Now playing " )));
		}

	}

	@Test
	public void shouldSearchByArtistAndTitle() throws Exception {

		Result result = shellTest.execute("music-search --artist ensi --title judge", 5, TimeUnit.SECONDS);

		assertThat(result, not(instanceOf(Failed.class)));
		//assertThat(result.getMessage(), is(equalTo("Found 1 song(s) to play. Now playing " )));
	}


}