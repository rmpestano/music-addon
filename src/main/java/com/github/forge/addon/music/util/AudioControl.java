package com.github.forge.addon.music.util;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.FactoryRegistry;

import javax.sound.sampled.*;
import javax.sound.sampled.Control.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by pestano on 16/10/15.
 */
public class AudioControl {
    private static Boolean isAudioEnabled;

    public static void setMasterOutputVolume(float value) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException(
                    "Volume can only be set to a value from 0 to 1. Given value is illegal: " + value);
        Line line = getMasterOutputLine();
        if (isAudioEnabled()) {
            boolean opened = open(line);
            try {
                FloatControl control = getVolumeControl(line);
                if (control == null)
                    throw new RuntimeException("Volume control not found in master port: " + toString(line));
                control.setValue(value);
            } finally {
                if (opened) line.close();
            }
        } else {
            Logger.getLogger(AudioControl.class.getName()).warning("Audio is not enabled in your devide");
        }
    }

    public static boolean isAudioEnabled(){
        try {
            if(isAudioEnabled == null){
                FactoryRegistry.systemRegistry().createAudioDevice();
                isAudioEnabled = true;
            }
        }catch (Exception e){
            isAudioEnabled = false;
        }
        return isAudioEnabled;
    }


    public static Float getMasterOutputVolume() {
        Line line = getMasterOutputLine();
        if (line == null) return null;
        boolean opened = open(line);
        try {
            FloatControl control = getVolumeControl(line);
            if (control == null) return null;
            return control.getValue();
        } finally {
            if (opened) line.close();
        }
    }

    public static void setMasterOutputMute(boolean value) {
        if (isAudioEnabled()) {
            Line line = getMasterOutputLine();
            boolean opened = open(line);
            try {
                BooleanControl control = getMuteControl(line);
                if (control == null)
                    throw new RuntimeException("Mute control not found in master port: " + toString(line));
                control.setValue(value);
            } finally {
                if (opened) line.close();
            }
        } else {
            Logger.getLogger(AudioControl.class.getName()).warning("Audio is not enabled in your devide");
        }
    }

    public static Boolean getMasterOutputMute() {
        Line line = getMasterOutputLine();
        if (line == null) return null;
        boolean opened = open(line);
        try {
            BooleanControl control = getMuteControl(line);
            if (control == null) return null;
            return control.getValue();
        } finally {
            if (opened) line.close();
        }
    }

    public static Line getMasterOutputLine() {
        Line backupLine = null;
        for (Mixer mixer : getMixers()) {
            for (Line line : getAvailableOutputLines(mixer)) {
                if (line.getLineInfo().toString().toLowerCase().contains("master")) {
                    return line;
                }else if(backupLine == null && line.getControls() != null){
                    backupLine = line;
                }
            }
        }
        //no master was found then return backup line
        return backupLine;
    }

    public static FloatControl getVolumeControl(Line line) {
        if (!line.isOpen()) throw new RuntimeException("Line is closed: " + toString(line));
        return (FloatControl) findControl(FloatControl.Type.VOLUME, line.getControls());
    }

    public static BooleanControl getMuteControl(Line line) {
        if (!line.isOpen()) throw new RuntimeException("Line is closed: " + toString(line));
        return (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());
    }

    private static Control findControl(Type type, Control... controls) {
        if (controls == null || controls.length == 0) return null;
        for (Control control : controls) {
            if (control.getType().equals(type)) return control;
            if (control instanceof CompoundControl) {
                CompoundControl compoundControl = (CompoundControl) control;
                Control member = findControl(type, compoundControl.getMemberControls());
                if (member != null) return member;
            }
        }
        return null;
    }

    public static List<Mixer> getMixers() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        List<Mixer> mixers = new ArrayList<Mixer>(infos.length);
        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            mixers.add(mixer);
        }
        return mixers;
    }

    public static List<Line> getAvailableOutputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getTargetLineInfo());
    }

    public static List<Line> getAvailableInputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getSourceLineInfo());
    }

    private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
        List<Line> lines = new ArrayList<Line>(lineInfos.length);
        for (Line.Info lineInfo : lineInfos) {
            Line line;
            line = getLineIfAvailable(mixer, lineInfo);
            if (line != null) lines.add(line);
        }
        return lines;
    }

    public static Line getLineIfAvailable(Mixer mixer, Line.Info lineInfo) {
        try {
            return mixer.getLine(lineInfo);
        } catch (LineUnavailableException ex) {
            return null;
        }
    }

    public static String getHierarchyInfo() {
        StringBuilder sb = new StringBuilder();
        for (Mixer mixer : getMixers()) {
            sb.append("Mixer: ").append(toString(mixer)).append("\n");

            for (Line line : getAvailableOutputLines(mixer)) {
                sb.append("  OUT: ").append(toString(line)).append("\n");
                boolean opened = open(line);
                for (Control control : line.getControls()) {
                    sb.append("    Control: ").append(toString(control)).append("\n");
                    if (control instanceof CompoundControl) {
                        CompoundControl compoundControl = (CompoundControl) control;
                        for (Control subControl : compoundControl.getMemberControls()) {
                            sb.append("      Sub-Control: ").append(toString(subControl)).append("\n");
                        }
                    }
                }
                if (opened) line.close();
            }

            for (Line line : getAvailableOutputLines(mixer)) {
                sb.append("  IN: ").append(toString(line)).append("\n");
                boolean opened = open(line);
                for (Control control : line.getControls()) {
                    sb.append("    Control: ").append(toString(control)).append("\n");
                    if (control instanceof CompoundControl) {
                        CompoundControl compoundControl = (CompoundControl) control;
                        for (Control subControl : compoundControl.getMemberControls()) {
                            sb.append("      Sub-Control: ").append(toString(subControl)).append("\n");
                        }
                    }
                }
                if (opened) line.close();
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    public static boolean open(Line line) {
        if (line.isOpen()) return false;
        try {
            line.open();
        } catch (LineUnavailableException ex) {
            return false;
        }
        return true;
    }

    public static String toString(Control control) {
        if (control == null) return null;
        return control.toString() + " (" + control.getType().toString() + ")";
    }

    public static String toString(Line line) {
        if (line == null) return null;
        Line.Info info = line.getLineInfo();
        return info.toString();// + " (" + line.getClass().getSimpleName() + ")";
    }

    public static String toString(Mixer mixer) {
        if (mixer == null) return null;
        StringBuilder sb = new StringBuilder();
        Mixer.Info info = mixer.getMixerInfo();
        sb.append(info.getName());
        sb.append(" (").append(info.getDescription()).append(")");
        sb.append(mixer.isOpen() ? " [open]" : " [closed]");
        return sb.toString();
    }
}
