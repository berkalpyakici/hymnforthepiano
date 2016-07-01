/**
 * 
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.*;

import acm.graphics.GMath;

/**
 *  Class: Audio
 *  -------------
 *  Warning: You don't have to read this file!
 *  
 *  Standard audio. This class provides a basic capability for
 *  creating, reading, and saving audio. The audio format uses a 
 *  sampling rate of 44,100 (CD quality audio), 16-bit, monaural.
 *
 *  For additional documentation, see
 *  http://introcs.cs.princeton.edu/15inout, Section 1.5 of
 *  Introduction to Programming in Java: An Interdisciplinary Approach
 *  by Robert Sedgewick and Kevin Wayne.
 *  
 *  This file is based on a modified version of 
 *  http://introcs.cs.princeton.edu/java/stdlib/StdAudio.java.html
 *  Parts of this class written by Keith Schwarz and Chris Piech.
 */
public final class Audio {

	/**  The sample rate - 44,100 Hz for CD quality audio. **/
	public static final int SAMPLES_PER_SECOND = 44100;

	private static final int BYTES_PER_SAMPLE = 2;                // 16-bit audio
	private static final int BITS_PER_SAMPLE = 16;                // 16-bit audio
	private static final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767
	private static final int SAMPLE_BUFFER_SIZE = 4096;


	private static SourceDataLine line;   // to play the sound
	private static byte[] buffer;         // our internal buffer
	private static int bufferSize = 0;    // number of samples currently in internal buffer

	// static initializer
	static { 
		init(); 
	}

	/**
	 * Method: Play
	 * ------------
	 * This method takes in a list of doubles that represent an audio sample to be played,
	 * and the
	 */
	public static void play(ArrayList<Double> list) {
		for (int i = 0; i < list.size(); i++) {
			play(list.get(i));
		}
		flush();
	}

	public static ArrayList<String> getNotes(String songName) {
		Scanner s;
		try {
			File songDir = new File("songs");
			s = new Scanner(new File(songDir, songName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Song not found!");
		}
		ArrayList<String> song = new ArrayList<String>();
		while (s.hasNext()) {
			String line = s.nextLine();
			song.add(line);
		}
		return song;
	}

	public static ArrayList<Double> getDurations(String songName) {
		Scanner s;
		try {
			s = new Scanner(new File(songName));
		} catch (FileNotFoundException e) {
			return null;
		}
		ArrayList<Double> durations = new ArrayList<Double>();
		while (s.hasNext()) {
			String line = s.nextLine().substring(1);
			if (line.isEmpty()) {
				line = "1";
			}
			durations.add(Double.parseDouble(line));
		}
		return durations;
	}

	/**
	 * Method: GetNoteSamples
	 * ----------------------
	 * This method takes in a frequency (in hertz) and a length (in samples... there are 44,100
	 * samples in a second) and returns a list of doubles that represent the sound.
	 */
	public static ArrayList<Double> getNoteSamples(double frequency, int length) {

		/* Generate a sine wave at that frequency. */
		double[] triangle = sineWave(frequency);

		/* Replicate that wave as many times as is necessary to fill in the sample. */
		double[] generated = new double[length];
		for (int x = 0; x < generated.length; x++) {
			generated[x] = triangle[x % triangle.length];
		}

		/* Ease out of the note. */
		double attenuationFactor = -1.0 / generated.length;
		for (int x = 0; x < generated.length; x++) {
			generated[x] *= (1.0 + x * attenuationFactor);
		}

		/* Ease into the note. */
		double leadInFactor = 1.0 / (generated.length / 64);
		for (int x = 0; x < generated.length / 64; x++) {
			generated[x] *= leadInFactor * x * x / (generated.length / 64);
		}

		ArrayList<Double> toReturn = new ArrayList<Double>();
		for(int i = 0; i < generated.length; i++) {
			toReturn.add(generated[i]);
		}
		return toReturn;
	}

	// open up an audio stream
	private static void init() {
		try {
			// 44,100 samples per second, 16-bit audio, mono, signed PCM, little Endian
			AudioFormat format = new AudioFormat((float) SAMPLES_PER_SECOND, BITS_PER_SAMPLE, 1, true, false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format, SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE);

			// the internal buffer is a fraction of the actual buffer size, this choice is arbitrary
			// it gets divided because we can't expect the buffered data to line up exactly with when
			// the sound card decides to push out its samples.
			buffer = new byte[SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE/3];
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}

		// no sound gets made before this call
		line.start();
	}

	/**
	 * Write one sample (between -1.0 and +1.0) to standard audio. If the sample
	 * is outside the range, it will be clipped.
	 */
	private static void play(double in) {

		// clip if outside [-1, +1]
		if (in < -1.0) in = -1.0;
		if (in > +1.0) in = +1.0;

		// convert to bytes
		short s = (short) (MAX_16_BIT * in);
		buffer[bufferSize++] = (byte) s;
		buffer[bufferSize++] = (byte) (s >> 8);   // little Endian

		// send to sound card if buffer is full        
		if (bufferSize >= buffer.length) {
			line.write(buffer, 0, buffer.length);
			bufferSize = 0;
		}
	}

	private static void flush() {
		bufferSize = 0;
	}

	private static double[] sineWave(double frequency) {
		double[] result = new double[periodOf(frequency)];
		for (int i = 0; i < result.length; i++) {
			result[i] = Math.sin(2.0 * Math.PI * i / result.length);
		}
		return result;
	}

	private static int periodOf(double frequency) {
		return GMath.round(Audio.SAMPLES_PER_SECOND / frequency);
	}
}