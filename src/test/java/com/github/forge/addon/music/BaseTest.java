package com.github.forge.addon.music;

import java.nio.file.Paths;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.github.forge.addon.music.playlist.PlaylistManager;
import com.github.forge.addon.music.util.AudioControl;

/**
 * Created by pestano on 17/10/15.
 */
public abstract class BaseTest {

    @Inject
    protected PlaylistManager playlistManager;

    protected static String defaulUserHome;
    
	private static float masterVolume;

	private static boolean masterMute;

    @BeforeClass
    public static void beforeClass() {
        defaulUserHome = System.getProperty("user.home");
        System.setProperty("user.home", Paths.get("").toAbsolutePath().toString() + "/target");
        setupVolume();
    }


    @AfterClass
    public static void afterClass() {
        System.setProperty("user.home", defaulUserHome);
        resetVolume();
    }
    
	public static void setupVolume() {
		try {
			if (AudioControl.isAudioEnabled()) {
				masterVolume = AudioControl.getMasterOutputVolume();
				masterMute = AudioControl.getMasterOutputMute();
				AudioControl.setMasterOutputMute(false);
				AudioControl.setMasterOutputVolume(0.30f);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	 
	public static void resetVolume() {
		try {
			if (AudioControl.isAudioEnabled()) {
				masterVolume = AudioControl.getMasterOutputVolume();
				masterMute = AudioControl.getMasterOutputMute();
				AudioControl.setMasterOutputMute(masterMute);
				AudioControl.setMasterOutputVolume(masterVolume);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
