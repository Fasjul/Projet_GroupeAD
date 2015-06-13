package gamepkg;


import ddf.minim.*;
import imageprocessing.ImageProcessing;
import processing.core.PApplet;

public class TangibleGame extends PApplet {
	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -869037209221970246L;
	
	private static ImageProcessing imageProc;
	private static GameApplet game;
	private Minim minim;
	private AudioPlayer player;
	private Boolean musicOn = false;
	
	@Override
	public void setup(){
		// Set multi-container size
		size(1190, 710);

		// Create Sub-Applets
		imageProc = new ImageProcessing();
		game = new GameApplet();
		game.setImageProcessing(imageProc);

		// Assign children
		this.add(imageProc);
		this.add(game);

		// initialize ImageProcessing frame
		imageProc.init();
		imageProc.start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// If Thread is interrupted (should not happen)
			e.printStackTrace();
			System.exit(-1);
		}
		// initialize Game frame
		game.init();
		game.start();
		minim = new Minim(this);
		player = minim.loadFile("resources/ThemeDaft.mp3");
	}
	@Override
	public void draw(){
		if(musicOn) player.play();
	}
	
}
