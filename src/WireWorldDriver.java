import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.io.File;

public class WireWorldDriver extends JFrame {

    public static WireWorld screen; // contains WireWorld object
    static ImageIcon icon = new ImageIcon("frameicon.png"); // icon for window

    public WireWorldDriver() {
	}

	// plays groovy tunes
    public static void playSound(String filename) {
	    try {
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filename).getAbsoluteFile()); // start audio input stream
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        clip.start(); // play to frame
	    } catch(Exception ex) {
	        System.out.println("Error Playing Sound. The file may not exist or be in the proper directory.");
	        ex.printStackTrace();
	    }
	}

	public static void main(String[] args) {
		screen = new WireWorld(); // frame displays WireWorld object
		JFrame frame = new JFrame("WireWorld"); frame.setSize(516, 538); frame.setIconImage(icon.getImage()); frame.setContentPane(screen); frame.setDefaultCloseOperation(EXIT_ON_CLOSE); frame.setVisible(true); //Sizing & visibility TODO: make size correspond to number of rows and coluumns
		playSound("tunes.wav");
	}
	
}
