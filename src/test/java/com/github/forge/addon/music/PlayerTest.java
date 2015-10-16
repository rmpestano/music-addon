package com.github.forge.addon.music;

import com.github.forge.addon.music.model.Playlist;
import com.github.forge.addon.music.model.Song;
import com.github.forge.addon.music.player.Player;
import com.github.forge.addon.music.playlist.PlaylistManager;
import com.github.forge.addon.music.util.AudioControl;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Paths;

/**
 * Created by pestano on 15/10/15.
 */
@RunWith(Arquillian.class)
public class PlayerTest {

    @Inject
    PlaylistManager playlistManager;

    @Inject
    Player player;

    Song song;

    private static float masterVolume;
    private static boolean masterMute;


    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {

        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @BeforeClass
    public static void setUp() {
        System.setProperty("forge.home", Paths.get("").toAbsolutePath().toString()+"/target");
        if(AudioControl.isAudioEnabled()){
            masterVolume = AudioControl.getMasterOutputVolume();
            masterMute = AudioControl.getMasterOutputMute();
            AudioControl.setMasterOutputMute(false);
            AudioControl.setMasterOutputVolume(0.25f);
        }
    }

    @AfterClass
    public static void after(){
        if(AudioControl.isAudioEnabled()) {
            //AudioControl.setMasterOutputVolume(masterVolume);
            //AudioControl.setMasterOutputMute(masterMute);
        }
    }

    @Before
    public void init(){
        playlistManager.removePlaylists();
        Song song = getSampleMp3();
        Playlist playlist = playlistManager.getPlaylist("default");
        playlist.addSong(song);
        playlistManager.savePlaylist(playlist);
    }

    @Test
    public void shouldPlaySong(){
        player.play();
    }

    @Test
    public void shouldPlayAndResume(){
        player.play();
        player.pause();
        player.resume();
    }



    private Song getSampleMp3() {
        if(song == null){
            song = new Song(Paths.get("target/test-classes").toAbsolutePath()+ "/axe.mp3");
        }
        return song;
    }
}
