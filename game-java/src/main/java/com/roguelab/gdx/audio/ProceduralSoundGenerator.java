package com.roguelab.gdx.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generates procedural 8-bit style sound effects.
 * Inspired by sfxr/bfxr retro sound generators.
 */
public class ProceduralSoundGenerator {

    private static final int SAMPLE_RATE = 22050;
    private static final Random rand = new Random();

    /**
     * Sound parameters for generation.
     */
    public static class SoundParams {
        // Wave type: 0=square, 1=sawtooth, 2=sine, 3=noise
        public int waveType = 0;
        
        // Envelope
        public float attackTime = 0.01f;
        public float sustainTime = 0.1f;
        public float decayTime = 0.1f;
        public float sustainLevel = 0.7f;
        
        // Frequency
        public float startFrequency = 440f;
        public float endFrequency = 440f;
        public float frequencySlide = 0f;
        
        // Effects
        public float vibratoDepth = 0f;
        public float vibratoSpeed = 0f;
        
        // Volume
        public float masterVolume = 0.5f;
        
        public SoundParams copy() {
            SoundParams p = new SoundParams();
            p.waveType = waveType;
            p.attackTime = attackTime;
            p.sustainTime = sustainTime;
            p.decayTime = decayTime;
            p.sustainLevel = sustainLevel;
            p.startFrequency = startFrequency;
            p.endFrequency = endFrequency;
            p.frequencySlide = frequencySlide;
            p.vibratoDepth = vibratoDepth;
            p.vibratoSpeed = vibratoSpeed;
            p.masterVolume = masterVolume;
            return p;
        }
    }

    // === PRESET SOUNDS ===

    public static SoundParams attackSword() {
        SoundParams p = new SoundParams();
        p.waveType = 3; // noise
        p.attackTime = 0.0f;
        p.sustainTime = 0.05f;
        p.decayTime = 0.1f;
        p.sustainLevel = 0.8f;
        p.startFrequency = 800f;
        p.endFrequency = 200f;
        p.masterVolume = 0.4f;
        return p;
    }

    public static SoundParams hitImpact() {
        SoundParams p = new SoundParams();
        p.waveType = 3; // noise
        p.attackTime = 0.0f;
        p.sustainTime = 0.02f;
        p.decayTime = 0.15f;
        p.sustainLevel = 1.0f;
        p.startFrequency = 300f;
        p.endFrequency = 80f;
        p.masterVolume = 0.5f;
        return p;
    }

    public static SoundParams playerHurt() {
        SoundParams p = new SoundParams();
        p.waveType = 1; // sawtooth
        p.attackTime = 0.0f;
        p.sustainTime = 0.1f;
        p.decayTime = 0.2f;
        p.sustainLevel = 0.6f;
        p.startFrequency = 400f;
        p.endFrequency = 150f;
        p.masterVolume = 0.35f;
        return p;
    }

    public static SoundParams enemyDeath() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.15f;
        p.decayTime = 0.3f;
        p.sustainLevel = 0.5f;
        p.startFrequency = 600f;
        p.endFrequency = 50f;
        p.masterVolume = 0.4f;
        return p;
    }

    public static SoundParams goldPickup() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.05f;
        p.decayTime = 0.1f;
        p.sustainLevel = 0.6f;
        p.startFrequency = 600f;
        p.endFrequency = 1200f;
        p.masterVolume = 0.3f;
        return p;
    }

    public static SoundParams itemPickup() {
        SoundParams p = new SoundParams();
        p.waveType = 2; // sine
        p.attackTime = 0.0f;
        p.sustainTime = 0.08f;
        p.decayTime = 0.15f;
        p.sustainLevel = 0.7f;
        p.startFrequency = 500f;
        p.endFrequency = 800f;
        p.vibratoDepth = 0.1f;
        p.vibratoSpeed = 20f;
        p.masterVolume = 0.35f;
        return p;
    }

    public static SoundParams levelUp() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.3f;
        p.decayTime = 0.3f;
        p.sustainLevel = 0.6f;
        p.startFrequency = 300f;
        p.endFrequency = 900f;
        p.vibratoDepth = 0.05f;
        p.vibratoSpeed = 8f;
        p.masterVolume = 0.4f;
        return p;
    }

    public static SoundParams menuSelect() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.03f;
        p.decayTime = 0.05f;
        p.sustainLevel = 0.5f;
        p.startFrequency = 600f;
        p.endFrequency = 700f;
        p.masterVolume = 0.25f;
        return p;
    }

    public static SoundParams menuConfirm() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.05f;
        p.decayTime = 0.1f;
        p.sustainLevel = 0.6f;
        p.startFrequency = 500f;
        p.endFrequency = 800f;
        p.masterVolume = 0.3f;
        return p;
    }

    public static SoundParams footstep() {
        SoundParams p = new SoundParams();
        p.waveType = 3; // noise
        p.attackTime = 0.0f;
        p.sustainTime = 0.02f;
        p.decayTime = 0.08f;
        p.sustainLevel = 0.4f;
        p.startFrequency = 150f;
        p.endFrequency = 80f;
        p.masterVolume = 0.2f;
        return p;
    }

    public static SoundParams doorOpen() {
        SoundParams p = new SoundParams();
        p.waveType = 3; // noise
        p.attackTime = 0.0f;
        p.sustainTime = 0.1f;
        p.decayTime = 0.2f;
        p.sustainLevel = 0.3f;
        p.startFrequency = 200f;
        p.endFrequency = 100f;
        p.masterVolume = 0.3f;
        return p;
    }

    public static SoundParams stairsDescend() {
        SoundParams p = new SoundParams();
        p.waveType = 3; // noise
        p.attackTime = 0.0f;
        p.sustainTime = 0.3f;
        p.decayTime = 0.5f;
        p.sustainLevel = 0.4f;
        p.startFrequency = 300f;
        p.endFrequency = 50f;
        p.masterVolume = 0.35f;
        return p;
    }

    public static SoundParams heal() {
        SoundParams p = new SoundParams();
        p.waveType = 2; // sine
        p.attackTime = 0.05f;
        p.sustainTime = 0.2f;
        p.decayTime = 0.3f;
        p.sustainLevel = 0.5f;
        p.startFrequency = 400f;
        p.endFrequency = 600f;
        p.vibratoDepth = 0.15f;
        p.vibratoSpeed = 6f;
        p.masterVolume = 0.35f;
        return p;
    }

    public static SoundParams victory() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.4f;
        p.decayTime = 0.5f;
        p.sustainLevel = 0.6f;
        p.startFrequency = 400f;
        p.endFrequency = 1000f;
        p.vibratoDepth = 0.1f;
        p.vibratoSpeed = 4f;
        p.masterVolume = 0.4f;
        return p;
    }

    public static SoundParams defeat() {
        SoundParams p = new SoundParams();
        p.waveType = 1; // sawtooth
        p.attackTime = 0.0f;
        p.sustainTime = 0.3f;
        p.decayTime = 0.6f;
        p.sustainLevel = 0.5f;
        p.startFrequency = 500f;
        p.endFrequency = 80f;
        p.masterVolume = 0.4f;
        return p;
    }

    public static SoundParams bossAppear() {
        SoundParams p = new SoundParams();
        p.waveType = 1; // sawtooth
        p.attackTime = 0.1f;
        p.sustainTime = 0.3f;
        p.decayTime = 0.4f;
        p.sustainLevel = 0.7f;
        p.startFrequency = 100f;
        p.endFrequency = 250f;
        p.vibratoDepth = 0.2f;
        p.vibratoSpeed = 5f;
        p.masterVolume = 0.45f;
        return p;
    }

    public static SoundParams shopBuy() {
        SoundParams p = new SoundParams();
        p.waveType = 2; // sine
        p.attackTime = 0.0f;
        p.sustainTime = 0.05f;
        p.decayTime = 0.1f;
        p.sustainLevel = 0.6f;
        p.startFrequency = 800f;
        p.endFrequency = 1000f;
        p.masterVolume = 0.3f;
        return p;
    }

    public static SoundParams error() {
        SoundParams p = new SoundParams();
        p.waveType = 0; // square
        p.attackTime = 0.0f;
        p.sustainTime = 0.1f;
        p.decayTime = 0.1f;
        p.sustainLevel = 0.5f;
        p.startFrequency = 200f;
        p.endFrequency = 150f;
        p.masterVolume = 0.3f;
        return p;
    }

    // === GENERATION ===

    /**
     * Generate raw PCM samples for a sound.
     */
    public static short[] generateSamples(SoundParams params) {
        float duration = params.attackTime + params.sustainTime + params.decayTime;
        int numSamples = (int) (SAMPLE_RATE * duration);
        short[] samples = new short[numSamples];

        float phase = 0;
        float frequency = params.startFrequency;
        float freqDelta = (params.endFrequency - params.startFrequency) / numSamples;

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / SAMPLE_RATE;
            
            // Envelope
            float envelope = calculateEnvelope(t, params);
            
            // Frequency with slide
            frequency += freqDelta;
            
            // Vibrato
            float vibrato = 1f;
            if (params.vibratoDepth > 0) {
                vibrato = 1f + params.vibratoDepth * (float) Math.sin(2 * Math.PI * params.vibratoSpeed * t);
            }
            
            // Generate waveform
            float sample = generateWave(params.waveType, phase);
            
            // Apply envelope and volume
            sample *= envelope * params.masterVolume;
            
            // Convert to 16-bit
            samples[i] = (short) (sample * 32767);
            
            // Advance phase
            phase += frequency * vibrato / SAMPLE_RATE;
            while (phase >= 1) phase -= 1;
        }

        return samples;
    }

    private static float calculateEnvelope(float t, SoundParams params) {
        if (t < params.attackTime) {
            // Attack
            return t / params.attackTime;
        } else if (t < params.attackTime + params.sustainTime) {
            // Sustain
            return params.sustainLevel;
        } else {
            // Decay
            float decayT = t - params.attackTime - params.sustainTime;
            float decayProgress = decayT / params.decayTime;
            return params.sustainLevel * (1 - decayProgress);
        }
    }

    private static float generateWave(int waveType, float phase) {
        return switch (waveType) {
            case 0 -> // Square
                phase < 0.5f ? 1f : -1f;
            case 1 -> // Sawtooth
                2f * phase - 1f;
            case 2 -> // Sine
                (float) Math.sin(2 * Math.PI * phase);
            case 3 -> // Noise
                rand.nextFloat() * 2f - 1f;
            default -> 0f;
        };
    }

    /**
     * Create a WAV file in memory and load as LibGDX Sound.
     */
    public static Sound createSound(SoundParams params) {
        short[] samples = generateSamples(params);
        byte[] wavData = createWavBytes(samples);
        
        try {
            // Write to temp file (LibGDX needs a file handle)
            File tempFile = File.createTempFile("roguelab_sfx_", ".wav");
            tempFile.deleteOnExit();
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(wavData);
            }
            
            FileHandle handle = Gdx.files.absolute(tempFile.getAbsolutePath());
            return Gdx.audio.newSound(handle);
            
        } catch (IOException e) {
            Gdx.app.error("Sound", "Failed to create sound: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create WAV file bytes from samples.
     */
    private static byte[] createWavBytes(short[] samples) {
        int dataSize = samples.length * 2;
        int fileSize = 44 + dataSize;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        try {
            // RIFF header
            dos.writeBytes("RIFF");
            writeIntLE(dos, fileSize - 8);
            dos.writeBytes("WAVE");
            
            // Format chunk
            dos.writeBytes("fmt ");
            writeIntLE(dos, 16); // Chunk size
            writeShortLE(dos, (short) 1); // PCM format
            writeShortLE(dos, (short) 1); // Mono
            writeIntLE(dos, SAMPLE_RATE);
            writeIntLE(dos, SAMPLE_RATE * 2); // Byte rate
            writeShortLE(dos, (short) 2); // Block align
            writeShortLE(dos, (short) 16); // Bits per sample
            
            // Data chunk
            dos.writeBytes("data");
            writeIntLE(dos, dataSize);
            
            // Sample data
            for (short sample : samples) {
                writeShortLE(dos, sample);
            }
            
            dos.flush();
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeIntLE(DataOutputStream dos, int value) throws IOException {
        dos.writeByte(value & 0xFF);
        dos.writeByte((value >> 8) & 0xFF);
        dos.writeByte((value >> 16) & 0xFF);
        dos.writeByte((value >> 24) & 0xFF);
    }

    private static void writeShortLE(DataOutputStream dos, short value) throws IOException {
        dos.writeByte(value & 0xFF);
        dos.writeByte((value >> 8) & 0xFF);
    }
}
