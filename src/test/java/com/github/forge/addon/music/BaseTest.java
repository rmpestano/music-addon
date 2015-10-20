package com.github.forge.addon.music;

import com.github.forge.addon.music.playlist.PlaylistManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.inject.Inject;
import java.nio.file.Paths;

/**
 * Created by pestano on 17/10/15.
 */
public abstract class BaseTest {

    @Inject
    protected PlaylistManager playlistManager;

    protected static String defaulUserHome;

    @BeforeClass
    public static void beforeClass() {
        defaulUserHome = System.getProperty("user.home");
        System.setProperty("user.home", Paths.get("").toAbsolutePath().toString() + "/target");
    }

    @Before
    public void before() {
        playlistManager.removePlaylists();
    }

    @AfterClass
    public static void afterClass() {
        System.setProperty("user.home", defaulUserHome);
    }
}
