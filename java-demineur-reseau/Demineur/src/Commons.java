
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.*;
import javax.sound.sampled.*;
/**
 * @author Quentin Chevalier
 */
public class Commons {
	public static enum difficulty {
		EASY, MEDIUM, HARD, CUSTOM
	}
	public static enum connectionState {
		NOT_CONNECTED, WAITING, PLAYING, SPECTATING
	}
	public enum playerState {
		CONNECTED, READY, PLAYING, SPECTATING, DISCONNECTED
	}

	public static int[] customDiff = { 20, 20, 100 };
	/**
	 * Returns a resized image of the inputted image.
	 * @param originalImage Image to rescale
	 * @param scaledWidth width
	 * @param scaledHeight height
	 * @param preserveAlpha true: no alpha, false: preserve alpha
	 * @return scaled Buffered Image.
	 */
	public static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
/**
 * This plays an audio file.
 * @param path path to audio file
 * @param loop should the audio file loop?
 */	
	public static void playSound(String path, boolean loop) {
		try {
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(new File("src/" + path).getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
			if (loop)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}
/**
 * Plays not looping audio.
 * @param path path to audio file
 */
	public static void playSound(String path) {
		playSound(path, false);
	}

}
