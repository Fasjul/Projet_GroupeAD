package gamepkg;

import imageprocessing.ImageProcessing;

import java.util.LinkedList;

import objects.*;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Class which manages most of the game functionality and acts as the core of the program.<br/>
 * Connected to Processing through the GameApplet instance.
 */
public class GameManager {
	/** Box object representing the game plane. */
	public Box box;
	/** Object representing the ball. */
	public Mover mover;
	/** Container which holds all obstacles (cylinders/trees). */
	public ObstacleManager obstacles;

	/** Current x-rotation of the game plane. */
	public float rotX = 0f;
	/** Current y-rotation of the game plane. */
	public float rotY = 0f;
	/**
	 * Current z-rotation of the game plane.
	 */
	public float rotZ = 0f;//ImageProcessing.boardRotations.z;

	private PVector oldPos;
	private LinkedList<Bottle> oldObstacles;

	/** Processing Applet Variable. */
	public final GameApplet GAME;
	/** Game draw variable. */
	public final PGraphics GAMEGFX;
	/** Data visualisation variable. */
	public final PGraphics STATGFX;
	
	// Internal variables to the data visualisation
	/** Top View Graphics */
	private final PGraphics topView;
	/** Textual score board */
	private final PGraphics scoreBoard;
	/** Bar chart of WHAT? */
	private final PGraphics barChart;
	/** Points over time stored in this list (each entry corresponds to a refresh duration) */
	private LinkedList<Float> pot;
	/** Refresh rate of the bar chart (as to each how many frames to update) */
	private final int refreshRate;
	/** Max recorded score for displaying */
	private float maxScore;
	// Internal variables for the style of the data visualisation
	private int statBgColor;
	private int statSpacing;
	private int statBarWidth;
	private int statBarSpacing;

	private final ImageProcessing input;

	boolean hold = false;

	GameManager(GameApplet game, PGraphics gameGraphics, PGraphics statGraphics, Box box, Mover mover, ObstacleManager obstacles, ImageProcessing input) {
		this.GAME = game;
		this.GAMEGFX = gameGraphics;
		this.STATGFX = statGraphics;
		
		refreshRate = 60;

		statBgColor = STATGFX.color(200);
		statSpacing = 5;
		statBarWidth = 3;
		statBarSpacing = 1;
		
		oldObstacles = new LinkedList<>();
		
		topView = GAME.createGraphics(100, 100);
		initTopView();

		scoreBoard = GAME.createGraphics(80, 100);
		
		maxScore = 1;
		pot = new LinkedList<>();
		barChart = GAME.createGraphics(340, 80);

		this.input = input;
		this.box = box;
		this.mover = mover;
		this.obstacles = obstacles;
	}

	public void draw() {
		drawGame();
		drawStats();
	}

	private void updateRot(){
		float diffX = Math.abs(rotX-input.boardRotations.x);
		float diffY = Math.abs(rotY-input.boardRotations.y);
		float diffZ = Math.abs(rotZ-input.boardRotations.z);
		//update position only if rotation is not too small or too big
		if(diffX>0.01 && diffX<Math.PI/2) rotX = input.boardRotations.x;
		if(diffY>0.01 && diffY<Math.PI/2) rotZ = -input.boardRotations.y;
		if(diffZ>0.01 && diffZ<Math.PI/2) rotY = input.boardRotations.z;
		
	}

	private void drawGame() {
		GAMEGFX.pushMatrix();
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
			updateRot();
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
			mover.checkCollision(obstacles);
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

			Bottle ghost = GAME.game.obstacles.ghost;
			
			boolean ghostValid = true;
			
			ghost.move(new PVector(GAME.mouseX-640-320+20,GAME.mouseY-240-20-20));
			if(ghost.collisionWithMover(mover) || !ghostValid){
				GAMEGFX.stroke(GAMEGFX.color(230,0,0));
			}
			GAMEGFX.pushMatrix();
			GAMEGFX.translate(-ghost.radius,0,-ghost.radius);
			ghost.draw();
			GAMEGFX.popMatrix();

			GAMEGFX.noStroke();
		}
		GAMEGFX.endDraw();
		GAME.image(GAMEGFX, 0, 0);
		GAMEGFX.popMatrix();
	}

	private void drawStats() {
		STATGFX.beginDraw();
		STATGFX.background(statBgColor);
		STATGFX.stroke(150);
		STATGFX.line(0, 0, STATGFX.width, 0);
		STATGFX.stroke(180);
		STATGFX.line(0, 1, STATGFX.width, 1);

		// update top view
		updateTopView();
		STATGFX.image(topView, statSpacing, statSpacing);

		// update score board
		updateScoreBoard();
		STATGFX.image(scoreBoard, statSpacing+topView.width+statSpacing, statSpacing);

		// update bar chart
		updateBarChart();
		STATGFX.image(barChart, statSpacing+topView.width+statSpacing+scoreBoard.width+statSpacing, statSpacing);
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

		for(Bottle c : oldObstacles) {
			topView.fill(100, 100, 255);
			float radius = 2*c.radius/factor + 1;
			topView.ellipse(c.location.x/factor, c.location.y/factor, radius, radius);
		}

		for(Bottle c : obstacles.obstacleList) {
			topView.fill(255);
			float radius = 2*c.radius/factor;
			topView.ellipse(c.location.x/factor, c.location.y/factor, radius, radius);
		}

		oldObstacles.clear();
		for(Bottle c : obstacles.obstacleList) {
			oldObstacles.add(c);
		}

		oldPos.x = relativePos.x;
		oldPos.y = relativePos.y;
		topView.popMatrix();
		topView.endDraw();
	}

	private void updateScoreBoard() {
		scoreBoard.beginDraw();
		scoreBoard.stroke(100);
		scoreBoard.fill(statBgColor);
		scoreBoard.rect(0, 0, scoreBoard.width-1, scoreBoard.height-1);

		scoreBoard.textSize(10);
		scoreBoard.fill(0);

		scoreBoard.text("Total score:", 4, 13);
		scoreBoard.text(mover.totalScore,  8, 24);

		scoreBoard.text("Velocity:", 4, 35);
		scoreBoard.text(mover.velocity.mag(), 8, 48);

		scoreBoard.text("Last score:", 4, 60);
		scoreBoard.text(mover.lastScore, 8, 70);
		scoreBoard.endDraw();
	}

	private void updateBarChart() {
		if(GAME.frame() % refreshRate == 0) {
			float score = mover.totalScore;
			if(score > maxScore) {
				maxScore = score;
			}
			pot.add(score);
			drawBarChart();
		}
	}
	
	/**
	 * Quadratic factor variation function.</br>
	 * 0.0 -> 0.1 ;
	 * 0.5 -> 1.0 ;
	 * 1.0 -> 3.0
	 * @return The wanted factor given as input the scroll-bar position in [0,1].
	 */
	private float factor() {
		float pos = GAME.hscrollbar.getPos();
		
		float a = 11/5f;
		float b = 7/5f;
		float c = 0.1f;
		
		return a*pos*pos + b*pos + c;
	}
	
	/**
	 * Refreshes the bar chart in the data-visualization.
	 */
	public void drawBarChart() {
		barChart.rectMode(PConstants.CORNER);
		barChart.beginDraw();
			barChart.background(statBgColor);
			barChart.noStroke();
			barChart.fill(50, 50, 200);
			
			int i = 0;
			for(Float f : pot) {
				float calc = f/maxScore*70;
				float factor = factor();
				barChart.rect((i++) * factor * (statBarWidth+statBarSpacing), barChart.height-calc-1, statBarWidth*factor, calc+10);
			}
		barChart.endDraw();
	}

}
