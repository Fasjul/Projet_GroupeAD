GameManager game;

// Mouse variables
int mX = 0;
int mY = 0;
int bX = 0;
int bY = 0;
int mouseScroll = 0;

int zoom = -150;

// Shape and Mover
float c2 = 255;
float c3 = 255;

boolean mouseClick =  false;

void setup() {
  size(600, 600, P3D);
  noStroke();

  int boxWidth = 400;
  int boxHeight = 20;
  int boxDepth = 400;

  int ballRadius = 10;

  int cylinderBaseRadius = 20;
  int cylinderBaseHeight = 50;
  int cylinderResolution = 40;

  Box box = new Box(boxWidth, boxHeight, boxDepth);
  Mover mover = new Mover(ballRadius);
  ObstacleManager obstacles = new ObstacleManager(cylinderBaseRadius, cylinderBaseHeight, cylinderResolution);
  game = new GameManager(box, mover, obstacles);

  bX = width/2;
  bY = height/2;
}

void draw() {
  game.draw();
}

/*
==============================================
=                                            =
=            KEYBOARD EVENTS                 =
=                                            =
==============================================
*/
void keyPressed() {
  if(key == CODED) {
    if(keyCode == SHIFT){
      game.hold = true;
    }
  }
}

void keyReleased(){
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
void mousePressed() {
  // Save the coordinates of the 
  // start of the drag.
  if(!game.hold){
    mX = mouseX - bX;
    mY = mouseY - bY;
  }
}

void mouseClicked(){
  ClosedCylinder ghost = game.obstacles.ghost;
  if(game.hold && (!ghost.collisionWithMover(game.mover))) {
    if(mouseButton == LEFT) {
      ClosedCylinder toAdd = game.obstacles.add(getGamePos(mouseX, mouseY));
      toAdd.setColor(255, 255, 51);
      mouseClick = true;
   
    } else if(mouseButton == RIGHT) {
      if(!game.obstacles.obstacleList.isEmpty()) {
        for(int i = 0; i<game.obstacles.obstacleList.size();i++) {
          if(ghost.collisionWithCylinder(game.obstacles.obstacleList.get(i))) {
            game.obstacles.obstacleList.remove(i);

          }
        }
      }
    }
  } else {
    noFill();
    mouseClick = false;
  }
}

void mouseReleased() {
  if(!game.hold){
    bX = Math.round(clamp(bX, 0, width));
    bY = Math.round(clamp(bY, 0, height)); 
    noStroke();
  }
}

void mouseWheel(MouseEvent event){
  float e = event.getCount();
  mouseScroll += e;
  mouseScroll = (int)clamp(mouseScroll,-100,100);
  game.speed = (float)Math.pow(2f, mouseScroll/25f);
}

void mouseDragged() {
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

boolean clampBool(float a, float min, float max) {
  return (a < min || a > max);
}

float clamp(float a, float min, float max) {
  if(a < min) {
    return min;
  } else if(a > max) {
    return max;
  } else {
    return a;
  }

}

PVector getGamePos(int mouseX, int mouseY) {
  float x = clamp(mouseX, 124, 600-124);
  float y = clamp(mouseY, 124, 600-124);

  x = map(x, 124, 600-124, -game.box.width/2, game.box.width/2);
  y = map(y, 124, 600-124, -game.box.depth/2, game.box.depth/2);

  return new PVector(x,y);
}
PVector getGamePos(PVector screenPos) {
  return getGamePos((int)screenPos.x, (int)screenPos.y);
}

class Mover {
  final float GRAVITY_CONSTANT = 0.1;
  final float BOUNCING_COEFF = 1;
  final float NORMALFORCE_COEFF = 1;
  final float MU = 0.05;

  float radius;

  PVector location;
  PVector velocity;
  PVector gravityForce;
  PVector frictionForce;
  
  Mover(int radius) {
    location = new PVector(0,0);
    velocity = new PVector(0,0);
    gravityForce = new PVector(0,0);
    frictionForce = new PVector(0,0);

    this.radius = radius;
  }
  
  void update(){
    gravityForce.x = sin(game.rotZ)*GRAVITY_CONSTANT;
    gravityForce.y = sin(game.rotX)*GRAVITY_CONSTANT*(-1);
    
    float frictionMagnitude = NORMALFORCE_COEFF*MU;
    PVector frictionForce = velocity.get();
    
    frictionForce.mult(-1);
    frictionForce.normalize();
    frictionForce.mult(frictionMagnitude);
    
    velocity.add(frictionForce);
    velocity.add(gravityForce);
    location.add(velocity);
  }
  
  void draw(){
    noStroke();
    directionalLight(102,102,102,102,102,102);
    fill(127);
    sphere(this.radius);
  }
  
  
  void checkEdges(){
    if(location.x>=game.box.width/2){
      location.x = game.box.width/2;
      velocity.x = velocity.x*(-1)*BOUNCING_COEFF;
    }
    else if(location.x<=-game.box.width/2){
      location.x = -game.box.width/2;
      velocity.x = velocity.x*(-1)*BOUNCING_COEFF;
    }
    if(location.y>=game.box.depth/2){
      location.y = game.box.depth/2;
      velocity.y = velocity.y*(-1)*BOUNCING_COEFF;
    }
    if(location.y<=-game.box.depth/2){
      location.y = -game.box.depth/2;
      velocity.y = velocity.y*(-1)*BOUNCING_COEFF;
    }
  }

  void checkCylinderCollision(ObstacleManager obstacles){
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

class GameManager {
  Box box;
  Mover mover;  
  ObstacleManager obstacles;

  float rotX = 0f;
  float rotY = 0f;
  float rotZ = 0f;

  float speed = 1f;
  boolean hold = false;

  GameManager(Box box, Mover mover, ObstacleManager obstacles) {
    this.box = box;
    this.mover = mover;
    this.obstacles = obstacles;
  }

  void draw() {
    if(hold==false) {
      background(200);
      camera(0, zoom, 600, 0, 0, 0, 0, 1, 0);
      directionalLight(102,102,102,102,102,102);
      ambientLight(100,100,100);

      pushMatrix();
        rotateY(game.rotY);
        rotateX(game.rotX);
        rotateZ(game.rotZ);

        fill(80, 80, 80);
        
        box.draw();
        obstacles.draw();

        pushMatrix();
          translate(mover.location.x, -box.height/2-mover.radius, mover.location.y);
          mover.update();
          mover.checkEdges();
          mover.checkCylinderCollision(obstacles);
          mover.draw();
        popMatrix();
      popMatrix();
    } else {
      background(200);
      camera(0,-600, 1, 0, 0, 0, 0, 1, 0);

      pushMatrix();
        directionalLight(102,102,102,102,102,102);
        ambientLight(100,100,100);

        noStroke();
        fill(80,80,80);
        box.draw();
        obstacles.draw();

        translate(mover.location.x, -20, mover.location.y);
        mover.draw();
      popMatrix();
      
      ClosedCylinder ghost = game.obstacles.ghost;

      ghost.move(getGamePos(mouseX, mouseY));
      if(ghost.collisionWithMover(mover)){
        stroke(color(230,0,0));
      } else {
        noStroke();
      }

      ghost.draw();
    }
  }
}

class Box implements Drawable {
  int width;
  int height;
  int depth;

  Box(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  void draw() {
    box(width, height, depth);
  }
}

class ObstacleManager implements Drawable {
  final ArrayList<ClosedCylinder> obstacleList;
  final ClosedCylinder ghost;

  final float BASE_HEIGHT;
  final float BASE_RADIUS;
  final int BASE_RESOLUTION;

  ObstacleManager(float baseHeight, float baseRadius, int baseResolution) {
    this.obstacleList = new ArrayList<ClosedCylinder>();

    this.BASE_HEIGHT = baseHeight;
    this.BASE_RADIUS = baseRadius;
    this.BASE_RESOLUTION = baseResolution;

    ghost = new ClosedCylinder(new PVector(0,0), BASE_HEIGHT, BASE_RADIUS, BASE_RESOLUTION);
    ghost.setGhost();
  }
  
  ClosedCylinder add(ClosedCylinder obstacle) {
    obstacleList.add(obstacle);
    return obstacle;
  }

  ClosedCylinder add(PVector position) {
    ClosedCylinder cyl = new ClosedCylinder(position, BASE_HEIGHT, BASE_RADIUS, BASE_RESOLUTION);
    obstacleList.add(cyl);
    return cyl;
  }
  
  boolean contains(ClosedCylinder c){
    return obstacleList.contains(c);
  }
  
  void remove(ClosedCylinder c){
    if(contains(c)){
      obstacleList.remove(c);
    }
  }
  
  public void draw() {
    for(ClosedCylinder obj : obstacleList) {
      obj.draw();
    }
  }
}

interface Drawable {
  void draw();
}

class ClosedCylinder implements Drawable {
  PVector location;
  float radius;
  float height;
  int resolution;

  boolean ghost = false;
  float R = 0;
  float G = 0;
  float B = 0;
  
  ClosedCylinder() {
    this.location = new PVector(0,0,0);
  }

  ClosedCylinder(PVector location, float radius, float height, int resolution){
    this.location = new PVector(location.x, location.y);
    this.radius = radius;
    this.height = height;
    this.resolution = resolution;
  }
  
  void move(PVector newLocation){
     location = new PVector(newLocation.x, newLocation.y);
  }
  
  boolean collisionWithMover(Mover mover){
    float distX = (mover.location.x - this.location.x);
    float distY = (mover.location.y - this.location.y);
    float squareDist = distX*distX + distY*distY;

    float totRadius = this.radius + mover.radius;
    float squareRadius = totRadius*totRadius;

    return squareDist <= squareRadius;
  }

  boolean collisionWithCylinder(ClosedCylinder that){
    float distX = (that.location.x - this.location.x);
    float distY = (that.location.y - this.location.y);
    float squareDist = distX*distX + distY*distY;

    float totRadius = this.radius + that.radius;
    float squareRadius = totRadius*totRadius;

    return squareDist <= squareRadius;
  }
  
  void setColor(float R, float G, float B){
    this.R = R;
    this.G = G;
    this.B = B;
  }
  
  void setGhost(){
    this.ghost = true;
  }
  
  void draw(){
    pushMatrix();
      translate(location.x, -game.box.height/2, location.y);
      rotateX(PI/2);

      float angle;
      float[] x = new float [resolution + 1];
      float[] y = new float [resolution + 1];
      
      //get the x and y position
      for(int i = 0; i< x.length; i++){
        angle = (TWO_PI / resolution)*i;
        x[i] = sin(angle) * radius;
        y[i] = cos(angle) * radius;
      }

      beginShape(QUAD_STRIP);
        if(ghost) {
          fill(color(255, 255, 224, 100));
        } else {
          fill(color(R, G, B));
        }

        //draw the border of the cylinder
        for(int i = 0; i<x.length; i++){
          vertex(x[i], y[i], 0);
          vertex(x[i], y[i], height);
        }
        vertex(x[0], y[0], 0);
        vertex(x[0], y[0], height);

        //top surface
        for(int i = 0; i<x.length; i++){
          vertex(   0,    0, height);
          vertex(x[i], y[i], height);
        }
        vertex(   0,    0, height);
        vertex(x[0], y[0], height);

        //bottom surface
        for(int i = 0; i<x.length; i++){
          vertex(   0,    0, 0);
          vertex(x[i], y[i], 0);
        }
        vertex(   0,    0, 0);
        vertex(x[0], y[0], 0);
      endShape();

      noFill();
    popMatrix();
  }
}
