package com.github.forge.addon.music;

import com.github.forge.addon.music.playlist.PlaylistManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import javax.inject.Inject;
import java.nio.file.Paths;

/**
 * Created by pestano on 17/10/15.
 */
@Ignore
public class BaseTest {

    protected static final String TEST_PLAY_LIST_NAME = "test-playlist";

    @Inject
    protected PlaylistManager playlistManager;

    protected String defaulUserHome;

    @Before
    public void before() {
        defaulUserHome = System.getProperty("user.home");
        System.setProperty("user.home", Paths.get("").toAbsolutePath().toString() + "/target");
        playlistManager.removePlaylists();
    }

    @After
    public void after() {
        System.setProperty("user.home", defaulUserHome);
    }
}
