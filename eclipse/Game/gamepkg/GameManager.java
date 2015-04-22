package gamepkg;

import objects.*;
import processing.core.PGraphics;

/**
 * Class which manages most of the game functionality and acts as the core of the program.<br/>
 * Connected to Processing through the GameApplet instance.
 */
public class GameManager {
	/**
	 * Box object representing the game plane.
	 */
	public Box box;
	/**
	 * Object representing the ball.
	 */
	public Mover mover;
	/**
	 * Container which holds all obstacles (cylinders/trees).
	 */
	public ObstacleManager obstacles;

	/**
	 * Current x-rotation of the game plane.
	 */
	public float rotX = 0f;
	/**
	 * Current y-rotation of the game plane.
	 */
	public float rotY = 0f;
	/**
	 * Current z-rotation of the game plane.
	 */
	public float rotZ = 0f;
	
	/**
	 * Processing Applet Variable.
	 */
	public final GameApplet GAME;
	/**
	 * Game draw variable.
	 */
	public final PGraphics GAMEGFX;
	/**
	 * Data visualisation variable.
	 */
	public final PGraphics STATGFX;

	float speed = 1f;
	boolean hold = false;

	GameManager(GameApplet game, PGraphics gameGraphics, PGraphics statGraphics, Box box, Mover mover, ObstacleManager obstacles) {
		this.GAME = game;
		this.GAMEGFX = gameGraphics;
		this.STATGFX = statGraphics;
		
		this.box = box;
		this.mover = mover;
		this.obstacles = obstacles;
	}

	public void draw() {
		drawGame();
		drawStats();
	}
	
	private void drawGame() {
		GAMEGFX.beginDraw();
		if(hold==false) {
			GAMEGFX.background(200);
			GAMEGFX.camera(0, -150, 600, 0, 0, 0, 0, 1, 0);
			GAMEGFX.directionalLight(102,102,102,102,102,102);
			GAMEGFX.ambientLight(100,100,100);

			GAMEGFX.pushMatrix();
			GAMEGFX.rotateY(GAME.game.rotY);
			GAMEGFX.rotateX(GAME.game.rotX);
			GAMEGFX.rotateZ(GAME.game.rotZ);

			GAMEGFX.fill(80, 80, 80);

			box.draw();
			obstacles.draw();

			GAMEGFX.pushMatrix();
			GAMEGFX.translate(mover.location.x, -box.height/2-mover.radius, mover.location.y);
			mover.update();
			mover.checkEdges();
			mover.checkCylinderCollision(obstacles);
			mover.draw();
			GAMEGFX.popMatrix();
			GAMEGFX.popMatrix();
		} else {
			GAMEGFX.background(200);
			GAMEGFX.camera(0,-600, 1, 0, 0, 0, 0, 1, 0);

			GAMEGFX.pushMatrix();
			GAMEGFX.directionalLight(102,102,102,102,102,102);
			GAMEGFX.ambientLight(100,100,100);

			GAMEGFX.noStroke();
			GAMEGFX.fill(80,80,80);
			box.draw();
			obstacles.draw();

			GAMEGFX.translate(mover.location.x, -20, mover.location.y);
			mover.draw();
			GAMEGFX.popMatrix();

			ClosedCylinder ghost = GAME.game.obstacles.ghost;

			ghost.move(GAME.getGamePos(GAME.mouseX, GAME.mouseY));
			if(ghost.collisionWithMover(mover)){
				GAMEGFX.stroke(GAMEGFX.color(230,0,0));
			}
			ghost.draw();
			
			GAMEGFX.noStroke();
		}
		GAMEGFX.endDraw();
		GAME.image(GAMEGFX, 0, 0);
	}
	
	private void drawStats() {
		STATGFX.beginDraw();
		STATGFX.background(255,0,0);
		STATGFX.endDraw();
		GAME.image(STATGFX, 0, 600);
	}
}