package objects;

import gamepkg.GameApplet;
import processing.core.*;

public class ClosedCylinder implements Drawable {
	public final static int DEFAULT_RADIUS = 20;
	public final static int DEFAULT_HEIGHT = 40;
	public final static int DEFAULT_RESOLUTION = 10;

	public PVector location;
	public final float radius;
	public final float height;
	public final int resolution;

	private boolean ghost = false;
	private float R = 0;
	private float G = 0;
	private float B = 0;
	
	public final GameApplet GAME;
	public final PGraphics GAMEGFX;
	public final PShape CYLINDER;

	/*public ClosedCylinder() {
		this.location = new PVector(0,0);
		this.radius = DEFAULT_RADIUS;
		this.height = DEFAULT_HEIGHT;
		this.resolution = DEFAULT_RESOLUTION;
	}*/

	public ClosedCylinder(GameApplet game, PGraphics gameGraphics, PVector location, float radius, float height, int resolution){
		this.GAME = game;
		this.GAMEGFX = gameGraphics;
		this.CYLINDER = GAMEGFX.loadShape("resources/objects/cylinder.obj");
		
		this.location = new PVector(location.x, location.y);
		this.radius = radius;
		this.height = height;
		this.resolution = resolution;
	}

	public void move(PVector newLocation){
		location = new PVector(newLocation.x, newLocation.y);
	}

	public boolean collisionWithMover(Mover mover){
		float distX = (mover.location.x - this.location.x);
		float distY = (mover.location.y - this.location.y);
		float squareDist = distX*distX + distY*distY;

		float totRadius = this.radius + mover.radius;
		float squareRadius = totRadius*totRadius;

		return squareDist <= squareRadius;
	}

	public boolean collisionWithCylinder(ClosedCylinder that){
		float distX = (that.location.x - this.location.x);
		float distY = (that.location.y - this.location.y);
		float squareDist = distX*distX + distY*distY;

		float totRadius = this.radius + that.radius;
		float squareRadius = totRadius*totRadius;

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
	
	public void draw(){
		GAMEGFX.fill(GAMEGFX.color(R,G,B));
		GAMEGFX.shape(CYLINDER);
	}

	/*public void draw(){
		GAME.pushMatrix();
		GAME.translate(location.x, -GAME.game.box.height/2, location.y);
		GAME.rotateX(GAME.PI/2);

		float angle;
		float[] x = new float [resolution + 1];
		float[] y = new float [resolution + 1];

		//get the x and y position
		for(int i = 0; i< x.length; i++){
			angle = (PConstants.TWO_PI / resolution)*i;
			x[i] = PApplet.sin(angle) * radius;
			y[i] = PApplet.cos(angle) * radius;
		}

		GAME.beginShape(PConstants.QUAD_STRIP);
		if(ghost) {
			GAME.fill(GAME.color(255, 255, 224, 100));
		} else {
			GAME.fill(GAME.color(R, G, B));
		}

		//draw the border of the cylinder
		for(int i = 0; i<x.length; i++){
			GAME.vertex(x[i], y[i], 0f);
			GAME.vertex(x[i], y[i], height);
		}
		GAME.vertex(x[0], y[0], 0);
		GAME.vertex(x[0], y[0], height);

		//top surface
		for(int i = 0; i<x.length; i++){
			GAME.vertex(   0,    0, height);
			GAME.vertex(x[i], y[i], height);
		}
		GAME.vertex(   0,    0, height);
		GAME.vertex(x[0], y[0], height);

		//bottom surface
		for(int i = 0; i<x.length; i++){
			GAME.vertex(   0,    0, 0);
			GAME.vertex(x[i], y[i], 0);
		}
		GAME.vertex(   0,    0, 0);
		GAME.vertex(x[0], y[0], 0);
		GAME.endShape();

		GAME.noFill();
		GAME.popMatrix();
	}*/
}