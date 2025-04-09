package Main;

import javax.sound.sampled.*;
import java.net.URL;

public class Sound {
    // Preload a fixed number of clips for your audio files.
    // Adjust the size if you expect more sounds.
    private Clip[] clips = new Clip[30];
    URL[] soundUrl = new URL[30];

    // Variables for pause/resume support (if you still need them)
    private long pausePosition = 0;
    private boolean isPaused = false;
    private boolean forceStopped = false;

    public Sound() {
        // Initialize your sound URL resources once
        soundUrl[0] = getClass().getResource("/music/TitleMusic.wav");
        soundUrl[1] = getClass().getResource("/music/Prontera.wav");
        soundUrl[2] = getClass().getResource("/soundeffects/sword-sound.wav");
        soundUrl[3] = getClass().getResource("/soundeffects/male_hurt.wav");
        soundUrl[4] = getClass().getResource("/soundeffects/goblin_attack.wav");
        soundUrl[5] = getClass().getResource("/soundeffects/goblin_hit.wav");
        soundUrl[6] = getClass().getResource("/soundeffects/goblin_death.wav");
        // Preload the clips once; this method runs at startup
        initClips();
    }

    /**
     * Preloads all non-null audio clips into the clips array.
     */
    public void initClips() {
        for (int i = 0; i < soundUrl.length; i++) {
            if (soundUrl[i] == null)
                continue;
            try {
                System.out.println("Preloading file at index " + i);
                AudioInputStream originalStream = AudioSystem.getAudioInputStream(soundUrl[i]);
                AudioFormat originalFormat = originalStream.getFormat();
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        originalFormat.getSampleRate(),
                        16,
                        originalFormat.getChannels(),
                        originalFormat.getChannels() * 2,
                        originalFormat.getSampleRate(),
                        false
                );

                Clip clip;
                if (!originalFormat.matches(targetFormat)) {
                    System.out.println("Converting audio format for index " + i);
                    AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);
                    clip = AudioSystem.getClip();
                    clip.open(convertedStream);
                } else {
                    clip = AudioSystem.getClip();
                    clip.open(originalStream);
                }
                clips[i] = clip;
                System.out.println("Preloaded audio file at index " + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Plays the clip at the given index from the start.
     * For looping music, you can call loopClip instead.
     */
    public void playClip(int i) {
        if (i < 0 || i >= clips.length) {
            System.err.println("Invalid clip index: " + i);
            return;
        }
        if (clips[i] != null) {
            // Restart the clip from beginning
            clips[i].setFramePosition(0);
            clips[i].start();
            System.out.println("Playing clip at index " + i);
        } else {
            System.err.println("Clip at index " + i + " is not loaded.");
        }
    }

    /**
     * Loops the clip continuously.
     */
    public void loopClip(int i) {
        if (i < 0 || i >= clips.length) {
            System.err.println("Invalid clip index: " + i);
            return;
        }
        if (clips[i] != null) {
            // Restart the clip and loop continuously
            clips[i].setFramePosition(0);
            clips[i].loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Looping clip at index " + i);
        } else {
            System.err.println("Clip at index " + i + " is not loaded.");
        }
    }

    /**
     * Stops the clip at the given index.
     */
    public void stopClip(int i) {
        if (i < 0 || i >= clips.length) {
            System.err.println("Invalid clip index: " + i);
            return;
        }
        if (clips[i] != null && clips[i].isRunning()) {
            clips[i].stop();
            clips[i].flush();
            System.out.println("Stopped clip at index " + i);
        }
    }

    /**
     * Stops all playing clips.
     */
    public void stopAll() {
        for (int i = 0; i < clips.length; i++) {
            if (clips[i] != null && clips[i].isRunning()) {
                clips[i].stop();
                clips[i].flush();
                System.out.println("Stopped clip at index " + i);
            }
        }
    }

    // The pause, resume, and forceStop methods can be adapted similarly if needed.
    public void pauseClip(int i) {
        if (i < 0 || i >= clips.length) {
            System.err.println("Invalid clip index: " + i);
            return;
        }
        if (clips[i] != null && clips[i].isRunning()) {
            pausePosition = clips[i].getMicrosecondPosition();
            clips[i].stop();
            isPaused = true;
            forceStopped = false;
            System.out.println("Paused clip at index " + i);
        }
    }

    public void resumeClip(int i) {
        if (i < 0 || i >= clips.length) {
            System.err.println("Invalid clip index: " + i);
            return;
        }
        if (isPaused && !forceStopped && clips[i] != null) {
            clips[i].setMicrosecondPosition(pausePosition);
            clips[i].start();
            isPaused = false;
            pausePosition = 0;
            System.out.println("Resumed clip at index " + i);
        }
    }

    public void forceStopClip(int i) {
        stopClip(i);
        isPaused = false;
        forceStopped = true;
        pausePosition = 0;
        System.out.println("Force stopped clip at index " + i);
    }
}
