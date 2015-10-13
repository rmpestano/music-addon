/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.github.forge.addon.music;

import com.github.forge.addon.music.playlist.PlaylistManagerImpl;
import com.github.forge.addon.music.ui.NewPlaylistCommand;
import org.hamcrest.core.Is;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
@RunWith(Arquillian.class)
@Ignore
public class NewPlaylistCommandTest
{

   @Inject
   private UITestHarness uiTestHarness;

   @Inject
   private ShellTest shellTest;

   private Project project;

   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap.create(AddonArchive.class).
              addPackages(true, PlaylistManagerImpl.class.getPackage().getName()).addBeansXML();
   }


   @Test
   public void checkCommandMetadata() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(NewPlaylistCommand.class))
      {
         controller.initialize();
         // Checks the command metadata
         assertTrue(controller.getCommand() instanceof NewPlaylistCommand);
         assertTrue(controller.canExecute());
         Result result = controller.execute();
         assertThat(result.getMessage(), Is.is("done"));
      }
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