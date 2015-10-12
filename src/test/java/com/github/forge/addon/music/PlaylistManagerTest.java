/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.playlist.PlaylistProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;

/**
 *
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@RunWith(Arquillian.class)
public class PlaylistManagerTest {

    private static final String ORIGINAL_FORGE_HOME = System.getProperty("forge.home");

    @Inject
    private PlaylistProvider playlistManager;

    @Inject
    private ProjectFactory projectFactory;

    private Project project;

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {

        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Before
    public void setUp() {
        project = projectFactory.createTempProject();
        System.setProperty("forge.home", project.getRoot().getFullyQualifiedName());

    }


    @Test
    public void shouldCreateDefaultPlayList() throws Exception {
        Assert.assertThat(playlistManager.hasDefaultPlaylist(),is(false));
        playlistManager.createDefaultPlaylist();
        Assert.assertThat(playlistManager.hasDefaultPlaylist(),is(true));
        Playlist playlist = playlistManager.getPlaylist("default");
        Assert.assertNotNull(playlist);
        Assert.assertEquals("default", playlist.getName());
    }

 /*  @Test
   public void checkCommandShell() throws Exception
   {
      shellTest.getShell().setCurrentResource(project.getRoot());
      Result result = shellTest.execute("soap-new-service --named Dummy", 10, TimeUnit.SECONDS);

      assertThat(result, not(instanceOf(Failed.class)));
      assertTrue(project.hasFacet(JAXWSFacet.class));
   }

   @Test
   public void testCreateNewSoapService() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(JAXWSNewServiceCommand.class,
               project.getRoot()))
      {
         controller.initialize();
         controller.setValueFor("named", "MySoapWebService");
         controller.setValueFor("targetPackage", "org.jboss.forge.test");
         assertTrue(controller.isValid());
         assertTrue(controller.canExecute());
         Result result = controller.execute();
         assertThat(result, is(not(instanceOf(Failed.class))));
      }

      JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
      JavaResource javaResource = facet.getJavaResource("org.jboss.forge.test.MySoapWebService");
      assertNotNull(javaResource);
      assertThat(javaResource.getJavaType(), is(instanceOf(JavaClass.class)));
      assertTrue(javaResource.getJavaType().hasAnnotation(WebService.class));
      assertFalse(((JavaClass<?>) javaResource.getJavaType()).hasInterface(Serializable.class));
      assertEquals(0, ((JavaClass<?>) javaResource.getJavaType()).getFields().size());
      assertEquals(0, ((JavaClass<?>) javaResource.getJavaType()).getMethods().size());
   }*/
}