package objects;

import gamepkg.*;
import processing.core.*;

public class Mover {
	public final float GRAVITY_CONSTANT = 0.1f;
	public final float BOUNCING_COEFF = 1;
	public final float NORMALFORCE_COEFF = 1;
	public final float MU = 0.05f;

	public final float radius;

	public PVector location;
	public PVector velocity;
	public PVector gravityForce;
	public PVector frictionForce;
	
	public final GameApplet GAME;
	public final PGraphics GAMEGFX;

	public Mover(GameApplet game, PGraphics gameGraphics, int radius) {
		this.GAME = game;
		this.GAMEGFX = gameGraphics;
		location = new PVector(0,0);
		velocity = new PVector(0,0);
		gravityForce = new PVector(0,0);
		frictionForce = new PVector(0,0);

		this.radius = radius;
	}

	public void update(){
		gravityForce.x = PApplet.sin(GAME.game.rotZ)*GRAVITY_CONSTANT;
		gravityForce.y = PApplet.sin(GAME.game.rotX)*GRAVITY_CONSTANT*(-1);

		float frictionMagnitude = NORMALFORCE_COEFF*MU;
		PVector frictionForce = velocity.get();

		frictionForce.mult(-1);
		frictionForce.normalize();
		frictionForce.mult(frictionMagnitude);

		velocity.add(frictionForce);
		velocity.add(gravityForce);
		location.add(velocity);
	}

	public void draw(){
		GAMEGFX.noStroke();
		GAMEGFX.fill(127);
		GAMEGFX.sphere(this.radius);
	}


	public void checkEdges(){
		if(location.x >= GAME.game.box.width/2){
			location.x = GAME.game.box.width/2;
			velocity.x = velocity.x*(-1)*BOUNCING_COEFF;
		}
		else if(location.x <=- GAME.game.box.width/2){
			location.x = -GAME.game.box.width/2;
			velocity.x = velocity.x*(-1)*BOUNCING_COEFF;
		}
		if(location.y >= GAME.game.box.depth/2){
			location.y = GAME.game.box.depth/2;
			velocity.y = velocity.y*(-1)*BOUNCING_COEFF;
		}
		if(location.y <= -GAME.game.box.depth/2){
			location.y = -GAME.game.box.depth/2;
			velocity.y = velocity.y*(-1)*BOUNCING_COEFF;
		}
	}

	public void checkCylinderCollision(ObstacleManager obstacles){
		for(ClosedCylinder cyl : obstacles.obstacleList){
			if(cyl.collisionWithMover(this)){
				PVector normal = new PVector(cyl.location.x - this.location.x, cyl.location.y - this.location.y);

				PVector n = normal.get();
				this.location.add(n);
				n.normalize();
				n.mult(this.radius+cyl.radius+0.01f);
				this.location.sub(n);

				normal.normalize();
				normal.mult(velocity.dot(normal));
				normal.mult(2);
				velocity.sub(normal);

			}
		}
	}
}