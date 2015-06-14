package objects;

import gamepkg.GameApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class Bottle implements Drawable{
	public final static int DEFAULT_RADIUS = 20;
	public final static int DEFAULT_HEIGHT = 40;
	public final static int DEFAULT_RESOLUTION = 10;

	public PVector location;
	public final float radius;
	public final float height;
	public final int resolution;

	private boolean ghost = false;
	private boolean validGhost = true;
	private float R = 0;
	private float G = 0;
	private float B = 0;
	
	public final GameApplet GAME;
	public final PGraphics GAMEGFX;
	public final PShape BOTTLE;
	private final PShape BOTTLE_ERROR;


	public Bottle(GameApplet game, PGraphics gameGraphics, PVector location, float radius, float height, int resolution){
		this.GAME = game;
		this.GAMEGFX = gameGraphics;
		this.BOTTLE = GAMEGFX.loadShape("resources/bottle.obj");
		this.BOTTLE_ERROR = GAMEGFX.loadShape("resources/bottle.obj");
		PImage error_texture = GAME.loadImage("resources/error_texture.png");
		BOTTLE_ERROR.setTexture(error_texture);
		

		this.radius = BOTTLE.getWidth()/2.0f;
		
		this.location = new PVector(location.x-this.radius,location.y-this.radius);
		
		this.height = BOTTLE.getHeight();
		this.resolution = resolution;
	}

	public void move(PVector newLocation){
		this.location = new PVector(newLocation.x, newLocation.y);
	}

	public boolean collisionWithMover(Mover mover){
		float distX = (mover.location.x - this.location.x);
		float distY = (mover.location.y - this.location.y);
		float squareDist = distX*distX + distY*distY;

		float totRadius = this.radius + mover.radius;
		float squareRadius = totRadius*totRadius;

		return squareDist <= squareRadius;
	}

	public boolean collisionWith(Bottle that){
		float distX = (that.location.x - this.location.x);
		float distY = (that.location.y - this.location.y);
		float squareDist = distX*distX + distY*distY;

		float totRadius = this.radius + that.radius;
		float squareRadius = totRadius*totRadius;

		System.out.println("Collision!");
		
		return squareDist <= squareRadius;
	}

	public void setColor(float R, float G, float B){
		this.R = R;
		this.G = G;
		this.B = B;
	}

	public void setGhost(boolean g){
		this.ghost = g;
	}
	
	public boolean isGhost() {
		return this.ghost;
	}
	
	public boolean validGhost(){
		if(isGhost()){
			validGhost = true;
			for(Bottle c : GAME.game.obstacles.obstacleList){
				if(collisionWith(c)){
					validGhost = false;
				}
			}
		}
		return validGhost;
	}
	
	public void draw(){
		GAMEGFX.pushMatrix();
		GAMEGFX.translate(location.x+this.radius, -GAME.game.box.height, location.y+this.radius);
		
		GAMEGFX.fill(100,50,50);
		
		if(ghost) {
			GAMEGFX.fill(GAMEGFX.color(255, 255, 224, 100));
		} else {
			GAMEGFX.fill(GAMEGFX.color(R, G, B));
		}
		if(!validGhost()){
			GAMEGFX.shape(BOTTLE_ERROR,0,0);
		}else{
			GAMEGFX.shape(BOTTLE,0,0);
		}
		GAMEGFX.popMatrix();
	}

}
