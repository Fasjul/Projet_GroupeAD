package gamepkg;

import imageprocessing.ImageProcessing;
import processing.core.PApplet;

public class TheGame extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ImageProcessing imageProc;
	private static GameApplet game;

	@Override
	public void setup(){
		// Set multi-container size
		size(1175,1190);

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// initialize Game frame
		game.init();
		game.start();
	}
}
