package gamepkg;

import java.util.LinkedList;

import objects.*;
import processing.core.PGraphics;
import processing.core.PVector;

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
	/**
	 * Top View Graphics
	 */
	private final PGraphics topView;
	private PVector oldPos;
	private LinkedList<ClosedCylinder> oldObstacles;

	float speed = 1f;
	boolean hold = false;

	GameManager(GameApplet game, PGraphics gameGraphics, PGraphics statGraphics, Box box, Mover mover, ObstacleManager obstacles) {
		this.GAME = game;
		this.GAMEGFX = gameGraphics;
		this.STATGFX = statGraphics;
		
		oldObstacles = new LinkedList<>();
		topView = GAME.createGraphics(100, 100);
		initTopView();
		
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
			GAMEGFX.background(255);
			GAMEGFX.camera(0, -150, 600, 0, 0, 0, 0, 1, 0);
			GAMEGFX.pushMatrix();
				GAMEGFX.translate(0, -300, 0);
				GAMEGFX.directionalLight(255, 255, 255, 0, 1, 0);
				GAMEGFX.ambientLight(100,100,100);
			GAMEGFX.popMatrix();

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
			updateTopView();
			STATGFX.image(topView, 5, 5);
		STATGFX.endDraw();
		GAME.image(STATGFX, 0, 600);
	}
	
	private void initTopView() {
		topView.beginDraw();
			topView.noStroke();
			topView.background(topView.color(100, 100, 255));
			oldPos = new PVector(0, 0);
		topView.endDraw();
	}
	
	private void updateTopView() {
		topView.beginDraw();
			topView.noStroke();
			Mover mv = GAME.game.mover;
			float factor = GAME.game.box.width/100;
			PVector relativePos = mv.location.get();
				relativePos.mult(1/factor);
			float relativeSize = 2*mv.radius/factor;
			
			topView.pushMatrix();
				topView.translate(50, 50);
				topView.fill(90, 90, 255);
				topView.ellipse(oldPos.x, oldPos.y, relativeSize+1f, relativeSize+1f);
				topView.fill(255, 0, 0);
				topView.ellipse(relativePos.x, relativePos.y, relativeSize, relativeSize);
				
				for(ClosedCylinder c : oldObstacles) {
					topView.fill(100, 100, 255);
					float radius = 2*c.radius/factor + 1;
					topView.ellipse(c.location.x/factor, c.location.y/factor, radius, radius);
				}
				
				for(ClosedCylinder c : obstacles.obstacleList) {
					topView.fill(255);
					float radius = 2*c.radius/factor;
					topView.ellipse(c.location.x/factor, c.location.y/factor, radius, radius);
				}
				
				oldObstacles.clear();
				for(ClosedCylinder c : obstacles.obstacleList) {
					oldObstacles.add(c);
				}
				
				oldPos.x = relativePos.x;
				oldPos.y = relativePos.y;
			topView.popMatrix();
		topView.endDraw();
	}
}