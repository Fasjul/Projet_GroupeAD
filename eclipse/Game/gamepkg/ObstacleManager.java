package gamepkg;

import java.util.ArrayList;

import objects.*;
import processing.core.*;
import ddf.minim.*;

public class ObstacleManager implements Drawable {
	public final ArrayList<Bottle> obstacleList;
	public final Bottle ghost;

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

		this.obstacleList = new ArrayList<Bottle>();

		this.BASE_HEIGHT = baseHeight;
		this.BASE_RADIUS = baseRadius;
		this.BASE_RESOLUTION = baseResolution;

		ghost = new Bottle(GAME, GAMEGFX, new PVector(0,0), BASE_HEIGHT, BASE_RADIUS, BASE_RESOLUTION);
		ghost.setGhost(true);
		
		minim = minArg;
		playerBreak = minim.loadFile("resources/Glass_Break.mp3");
		playerBlop = minim.loadFile("resources/Blop.mp3");
	}

	public Bottle add(Bottle obstacle) {
		obstacleList.add(obstacle);
		return obstacle;
	}

	public Bottle add(PVector position) {
		Bottle cyl = new Bottle(GAME, GAMEGFX, position, BASE_HEIGHT, BASE_RADIUS, BASE_RESOLUTION);
		obstacleList.add(cyl);
		playerBlop.play();
		playerBlop.rewind();
		return cyl;
	}

	public boolean contains(Bottle c){
		return obstacleList.contains(c);
	}

	public void remove(Bottle c){
		if(contains(c)){
			obstacleList.remove(c);
		}
	}
	public void destroy(Bottle c){
		if(contains(c)){
			playerBreak.play();
			playerBreak.rewind();
			remove(c);
		}
	}

	public void draw() {
		for(Bottle obj : obstacleList) {
			obj.draw();
		}
	}
}