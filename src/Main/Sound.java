package Main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {
    Clip clip;
    URL[] soundUrl = new URL[30];

    public Sound() {
        soundUrl[0] = getClass().getResource("/music/TitleMusic.wav");
        soundUrl[1] = getClass().getResource("/music/Prontera.wav");
        soundUrl[2] = getClass().getResource("/soundeffects/sword-sound.wav");
    }

    public void setFile(int i) {
        try{
            if(soundUrl[i] == null) {
                System.err.println("Coudlnt load file at index " + i);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundUrl[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void play() {
        if (clip != null) {
            clip.start();
        } else {
            System.err.println("Clip is null. Cannot play.");
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            System.err.println("Clip is null. Cannot loop.");
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        } else {
            System.err.println("Clip is null. Cannot stop.");
        }
    }
}
