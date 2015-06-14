package gamepkg;

import java.util.ArrayList;
import java.util.List;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import imageprocessing.BlobDetection;
import imageprocessing.ImageProcessing;
import processing.core.*;
import tools.HScrollbar;
import objects.*;


public class GameApplet extends PApplet {
	/**
	 * Generated random serial ID.
	 */
	private static final long serialVersionUID = 7478142574824696410L;

	public GameManager game;

	private PGraphics gameGraphics;
	private PGraphics statGraphics;

	private ImageProcessing imageProc;
	private int frame = 0;
	
	private TangibleGame tangParent;
	private PImage sound, no_sound;
	
	HScrollbar hscrollbar;
	
	Minim minim ;
	AudioPlayer player;

	@Override
	public void setup() {

		size(1190,710, P2D);
		noStroke();

		gameGraphics = createGraphics(600, 600, P3D);
		statGraphics = createGraphics(600, 110, P2D);
		
		hscrollbar = new HScrollbar(this, 200, gameGraphics.height+93, 340, 10);

		// Initialize some constants
		final int boxWidth = 400;
		final int boxHeight = 20;
		final int boxDepth = 400;

		final int ballRadius = 10;

		final int cylinderBaseRadius = 30;
		final int cylinderBaseHeight = 30;
		final int cylinderResolution = 40;
		
		Box box = new Box(gameGraphics, boxWidth, boxHeight, boxDepth);
		Mover mover = new Mover(this, gameGraphics, ballRadius);
		ObstacleManager obstacles = new ObstacleManager(this, gameGraphics, cylinderBaseRadius, cylinderBaseHeight, cylinderResolution,new Minim(this));
		
		game = new GameManager(this, gameGraphics, statGraphics, box, mover, obstacles, imageProc);
		sound = loadImage("resources/sound_icon.png");
		no_sound = loadImage("resources/no_sound_icon.png");
	}

	@Override
	public void draw() {
		if(!game.hold) frame++;
		pushMatrix();
			translate(640,0);
			game.draw();
			hscrollbar.display();
			if(tangParent != null && tangParent.musicOn) {
				image(sound, 20, 20);
			} else {
				image(no_sound, 20, 20);
			}
		popMatrix();
	}

	@Override
	public void keyPressed() {
		if(key == CODED) {
			if(keyCode == SHIFT) {
				game.hold = true;	
				//Run the blob detection
				game.obstacles.detectedObstacles = runBlobDetection();
			}
		}else if(game.hold){
			if(key == 88){
				game.obstacles.clearObstacles();
				System.out.println("All obstacles have been removed!");
			}
		}
	}

	@Override
	public void keyReleased(){
		if(key == CODED) {
			if(keyCode == SHIFT) {
				game.hold = false;
				game.obstacles.addDetectedToList();
			}
		}
	}

	
	public List<PVector> runBlobDetection(){
		ArrayList<PVector> corners = imageProc.returnedCorners;
		List<PVector> obstaclesPos = new ArrayList<PVector>();
		PImage hsb = imageProc.hsbFilterRed;
		BlobDetection blobD;
		
		if(hsb.width>0 && hsb.height>0 && corners.size()>=4){
			blobD = new BlobDetection(this, corners.get(0), corners.get(1), corners.get(2), corners.get(3));
			obstaclesPos = blobD.findConnectedComponents(hsb);
		}
		return obstaclesPos;
	}

	@Override
	public void mousePressed() {
		hscrollbar.update(mouseX-640, mouseY);
		game.drawBarChart();
	}

	/**
	 * Adds a cylinder to the game when the mouse is clicked.
	 */
	@Override
	public void mouseClicked(){
		Bottle ghost = game.obstacles.ghost;
		if(game.hold && (!ghost.collisionWithMover(game.mover)) && ghost.validGhost()) {
			if(mouseButton == LEFT) {
				Bottle toAdd = game.obstacles.add(ghost.location);
				toAdd.setColor(255, 255, 51);

			} else if(mouseButton == RIGHT) {
				System.out.println("Feature not implemented yet");
				
				for(Bottle c : game.obstacles.obstacleList) {
					if(ghost.collisionWith(c)) {
						game.obstacles.obstacleList.remove(c);
					}
				}
				 
			}
		} else {
			noFill();
			
			hscrollbar.update(mouseX-640, mouseY);
			game.drawBarChart();
		}
		
		checkSoundClick();
	}
	
	private void checkSoundClick() {
		if(tangParent != null) {
			boolean musicOn = tangParent.musicOn;
			int mouseOffset = 640;
			
			int xChk = mouseX - mouseOffset - 20;
			int yChk = mouseY - 20;
			
			if(0 <= xChk && xChk < sound.width && 0 <= yChk && yChk < sound.height) {
				tangParent.musicOn = !musicOn;
			}
		}
	}

	@Override
	public void mouseReleased() {
		hscrollbar.update(mouseX-640, mouseY);
		game.drawBarChart();
	}

	@Override
	public void mouseDragged() {
		hscrollbar.update(mouseX-640, mouseY);
		game.drawBarChart();
	}

	public int frame() {
		return frame;
	}

	/**
	 * Transforms the mouse position on the drawing surface to the mouse position in the game coordinates.
	 * @param mouseX x-coordinate of the mouse
	 * @param mouseY y-coordinate of the mouse
	 * @return A <d>PVector</d> of the 2D coordinate on the game plane.
	 */
	public PVector getGamePos(int mouseX, int mouseY) {
		float eZ = 300f;
		float pZ = game.box.height/2;

		float x = game.box.width*(mouseX-width/2f)/(width/2)*(eZ-pZ)/eZ;
		float y = game.box.depth*(mouseY-height/2f)/(height/2)*(eZ-pZ)/eZ;

		return new PVector(x,y);
	}

	public PVector getGamePos(PVector screenPos) {
		return getGamePos((int)screenPos.x, (int)screenPos.y);
	}

	public void setImageProcessing(ImageProcessing input){
		imageProc = input;
	}
	
	public void setParent(TangibleGame applet) {
		this.tangParent = applet;
	}

}
