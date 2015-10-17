/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@RunWith(Arquillian.class)
public class NewPlaylistCommandTest extends BaseTest{

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private ShellTest shellTest;


    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addClass(BaseTest.class).addBeansXML();
    }


    @Test
    public void shouldAddNewPlaylist() throws Exception {
        Result result = shellTest.execute("music-new-playlist --name " + TEST_PLAY_LIST_NAME, 25, TimeUnit.SECONDS);
        if(result instanceof Failed){
            Logger.getLogger(getClass().getName()).severe(result.getMessage());
        }

        assertThat(result, not(instanceOf(Failed.class)));
        assertThat(playlistManager.hasPlaylist(TEST_PLAY_LIST_NAME), is(true));

    }


}