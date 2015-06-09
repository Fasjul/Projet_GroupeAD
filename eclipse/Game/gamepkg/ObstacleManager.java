package gamepkg;

import java.util.ArrayList;

import objects.*;
import processing.core.*;
import ddf.minim.*;

public class ObstacleManager implements Drawable {
	public final ArrayList<ClosedCylinder> obstacleList;
	public final ClosedCylinder ghost;

	public final float BASE_HEIGHT;
	public final float BASE_RADIUS;
	public final int BASE_RESOLUTION;

	public final GameApplet GAME;
	public final PGraphics GAMEGFX;
	private Minim minim;
	private AudioPlayer playerBlop;
	private AudioPlayer playerBreak;
	
	public ObstacleManager(GameApplet game, PGraphics gameGraphics, float baseHeight, float baseRadius, int baseResolution, Minim minArg) {
		this.GAME = game;
		this.GAMEGFX = gameGraphics;

		this.obstacleList = new ArrayList<ClosedCylinder>();

		this.BASE_HEIGHT = baseHeight;
		this.BASE_RADIUS = baseRadius;
		this.BASE_RESOLUTION = baseResolution;

		ghost = new ClosedCylinder(GAME, GAMEGFX, new PVector(0,0), BASE_HEIGHT, BASE_RADIUS, BASE_RESOLUTION);
		ghost.setGhost(true);
		
		minim = minArg;
		playerBreak = minim.loadFile("resources/Glass_Break.mp3");
		playerBlop = minim.loadFile("resources/Blop.mp3");
	}

	public ClosedCylinder add(ClosedCylinder obstacle) {
		obstacleList.add(obstacle);
		return obstacle;
	}

	public ClosedCylinder add(PVector position) {
		ClosedCylinder cyl = new ClosedCylinder(GAME, GAMEGFX, position, BASE_HEIGHT, BASE_RADIUS, BASE_RESOLUTION);
		obstacleList.add(cyl);
		playerBlop.play();
		playerBlop.rewind();
		return cyl;
	}

	public boolean contains(ClosedCylinder c){
		return obstacleList.contains(c);
	}

	public void remove(ClosedCylinder c){
		if(contains(c)){
			obstacleList.remove(c);
		}
	}
	public void destroy(ClosedCylinder c){
		if(contains(c)){
			playerBreak.play();
			playerBreak.rewind();
			remove(c);
		}
	}

	public void draw() {
		for(ClosedCylinder obj : obstacleList) {
			obj.draw();
		}
	}
}