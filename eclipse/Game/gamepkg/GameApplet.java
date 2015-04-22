package gamepkg;

import processing.core.*;
import processing.event.*;
import objects.*;


public class GameApplet extends PApplet {
	/**
	 * Generated random serial ID.
	 */
	private static final long serialVersionUID = 7478142574824696410L;
	
	public GameManager game;

	// Mouse variables
	private int mX = 0;
	private int mY = 0;
	private int bX = 0;
	private int bY = 0;
	
	private PGraphics gameGraphics;
	private PGraphics statGraphics;
	
	/**
	 * Mouse scroll amount (between -100 and 100)
	 */
	private int mouseScroll = 0;

	public void setup() {
		size(600, 800, P2D);
		noStroke();
		
		gameGraphics = createGraphics(600, 600, P3D);
		statGraphics = createGraphics(600, 200, P3D);
		
		bX = width/2;
		bY = height/2;

		// Initialize some constants
		final int boxWidth = 400;
		final int boxHeight = 20;
		final int boxDepth = 400;

		final int ballRadius = 10;

		final int cylinderBaseRadius = 20;
		final int cylinderBaseHeight = 50;
		final int cylinderResolution = 40;
		
		// TODO: Create and send Graphics variables here:
		Box box = new Box(gameGraphics, boxWidth, boxHeight, boxDepth);
		Mover mover = new Mover(this, gameGraphics, ballRadius);
		ObstacleManager obstacles = new ObstacleManager(this, gameGraphics, cylinderBaseRadius, cylinderBaseHeight, cylinderResolution);
		game = new GameManager(this, gameGraphics, statGraphics, box, mover, obstacles);
	}

	public void draw() {
		game.draw();
	}

	/*
	==============================================
	=                                            =
	=            KEYBOARD EVENTS                 =
	=                                            =
	==============================================
	 */
	public void keyPressed() {
		if(key == CODED) {
			if(keyCode == SHIFT){
				game.hold = true;
			}
		}
	}

	public void keyReleased(){
		if(key == CODED) {
			if(keyCode == SHIFT) {
				game.hold = false;
			}
		}
	}



	/*
	==============================================
	=                                            =
	=              MOUSE EVENTS                  =
	=                                            =
	==============================================
	 */
	
	/**
	 * Saves coordinates of the mouse when pressed down for difference (used for mouse drag)
	 */
	public void mousePressed() {
		// Save the coordinates of the 
		// start of the drag.
		if(!game.hold){
			mX = mouseX - bX;
			mY = mouseY - bY;
		}
	}

	/**
	 * Adds a cylinder to the game when the mouse is clicked.
	 */
	public void mouseClicked(){
		ClosedCylinder ghost = game.obstacles.ghost;
		if(game.hold && (!ghost.collisionWithMover(game.mover))) {
			if(mouseButton == LEFT) {
				ClosedCylinder toAdd = game.obstacles.add(getGamePos(mouseX, mouseY));
				toAdd.setColor(255, 255, 51);

			} else if(mouseButton == RIGHT) {
				for(int i = 0; i<game.obstacles.obstacleList.size(); i++) {
					if(ghost.collisionWithCylinder(game.obstacles.obstacleList.get(i))) {
						game.obstacles.obstacleList.remove(i);
					}
				}
			}
		} else {
			noFill();
		}
	}

	/**
	 * Saves coordinates of the mouse when released for difference (used for mouse drag).
	 */
	public void mouseReleased() {
		if(!game.hold){
			bX = Math.round(clamp(bX, 0, width));
			bY = Math.round(clamp(bY, 0, height)); 
			noStroke();
		}
	}

	public void mouseWheel(MouseEvent event){
		float e = event.getCount();
		mouseScroll += e;
		mouseScroll = (int)clamp(mouseScroll,-100,100);
		game.speed = (float)Math.pow(2f, mouseScroll/25f);
	}

	/**
	 * <P>Rotates the plane along the mouse drag with the other variables.<br/>
	 * <I>Happens when mouse is pressed and moves.</I></P>
	 */
	public void mouseDragged() {
		if(!game.hold){
			float PoT = PI/3; // PI over Three
			bX = mouseX - mX;
			bY = mouseY - mY; 
			game.rotX = map(bY, 0, height, PoT, -PoT);
			game.rotZ = map(bX, 0,  width, -PoT, PoT);  
			noStroke();
			game.rotX = clamp(game.rotX, -PoT, PoT);
			game.rotZ = clamp(game.rotZ, -PoT, PoT);
		}
	}

	/**
	 * Tells whether a value is getting clamped for parameters val, min and max.
	 * @param val Value to consider
	 * @param min Minimum the value is allowed to be
	 * @param max Maximum the value is allowed to be
	 * @return <b style="color: blue">true</b> when the value is less than <b>min</b> or greater than <b>max</b>, otherwise <b style="color: blue">false</b>.
	 */
	public boolean clampBool(float val, float min, float max) {
		return (val < min || val > max);
	}

	/**
	 * Clamps a value between a minimum value and a maximum value.
	 * @param val Value to clamp
	 * @param min Minimum of the value
	 * @param max Maximum of the value
	 * @return Returns : 
	 * <ul>
	 *  <li><b>min</b> if the value is less than <b>min</b>;</li>
	 *  <li><b>max</b> if the value is greater than <b>max</b>;</li>
	 *  <li><b>val</b> otherwise.</li>
	 * </ul>
	 */
	public float clamp(float val, float min, float max) {
		if(val < min) {
			return min;
		} else if(val > max) {
			return max;
		} else {
			return val;
		}

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
		
//		float x = clamp(mouseX, 124, 600-124);
//		float y = clamp(mouseY, 124, 600-124);

//		x = map(x, 124, 600-124, -game.box.width/2, game.box.width/2);
//		y = map(y, 124, 600-124, -game.box.depth/2, game.box.depth/2);

		return new PVector(x,y);
	}
	
	public PVector getGamePos(PVector screenPos) {
		return getGamePos((int)screenPos.x, (int)screenPos.y);
	}
	
}
