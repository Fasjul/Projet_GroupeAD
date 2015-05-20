package gamepkg;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

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
		size(1175,1190);
		
		imageProc = new ImageProcessing();
		game = new GameApplet();
		game.setImageProcessing(imageProc);
		this.add(imageProc);
		this.add(game);
		imageProc.init();
		imageProc.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		game.init();
		game.start();
		}
}
