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
		translate(640,0);
		imageProc.init();
		imageProc.start();
		game.init();
		game.start();
		}
	@Override
	public void draw(){
		pushMatrix();
		this.add(game);
		translate(640,0);
		game.game.draw();
		popMatrix();
	}
	
}
