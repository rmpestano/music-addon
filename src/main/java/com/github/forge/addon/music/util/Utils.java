package com.github.forge.addon.music.util;

import com.github.forge.addon.music.model.Song;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.furnace.util.OperatingSystemUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;

import static org.jboss.forge.furnace.util.Assert.notNull;

/**
 * Created by pestano on 21/08/15.
 */
@ApplicationScoped
public class Utils {

    @Inject
    ResourceFactory resourceFactory;


    public FileResource<?> getMusicAsResource(Song song){
        notNull(song,"song must not be null");
        notNull(song.getLocation(),"song location must not be null");
        return resourceFactory.create(FileResource.class, new File(song.getLocation()));
    }

    public DirectoryResource getForgeHome()
    {
        DirectoryResource forgeHome = resourceFactory.create(DirectoryResource.class,
                OperatingSystemUtils.getUserForgeDir());
        return forgeHome;
    }

}
