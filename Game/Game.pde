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
int cylinderBaseSize = 20;
int cylinderHeight = 50;
int cylinderResolution = 40;

boolean mouseClick =  false;
ClosedCylinder ghost = new ClosedCylinder();

void setup() {
  size(600, 600, P3D);
  noStroke();

  int boxWidth = 400;
  int boxHeight = 20;
  int boxDepth = 400;

  int ballRadius = 10;

  Box box = new Box(boxWidth, boxHeight, boxDepth);
  Mover mover = new Mover(ballRadius);
  ObstacleManager obstacles = new ObstacleManager();
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
    } /*else if(keyCode == LEFT) {
      game.rotY -= PI/32;
      game.rotY %= 2*PI;
    } else if(keyCode == RIGHT) {
      game.rotY += PI/32;
      game.rotY %= 2*PI;
    }*/
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
  if(game.hold && (!ghost.collisionWithMover(game.mover))){
    if(mouseButton == LEFT){
      float x = clamp(mouseX, 124, 600-124);
      float y = clamp(mouseY, 124, 600-124);

      x = map(x, 124, 600-124, -game.box.width/2, game.box.width/2);
      y = map(y, 124, 600-124, -game.box.depth/2, game.box.depth/2);

      ClosedCylinder toAdd = new ClosedCylinder(new PVector(x,0,y),cylinderBaseSize,cylinderHeight,cylinderResolution);
      toAdd.setColor(255,255,51);
      game.obstacles.add(toAdd);
      mouseClick = true;
   
    }else if(mouseButton == RIGHT){
      if(!game.obstacles.obstacleList.isEmpty()){
        for(int i = 0; i<game.obstacles.obstacleList.size();i++){
          if(ghost.collisionWithCylinder(game.obstacles.obstacleList.get(i))){
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
  mouseScroll = (int)clamp(mouseScroll,-99,99);
  //game.speed = (float)Math.pow(2f, mouseScroll/25f);
}

void mouseDragged() {
  if(!game.hold){
  float PoT = PI/3; // PI over Three
  bX = mouseX - mX;
  bY = mouseY - mY; 
  game.rotX = map(bY, 0, height, PoT, -PoT);
  game.rotZ = map(bX, 0,  width, -PoT, PoT);  
  if(clampBool(bX, 0, width) || clampBool(bY, 0, height)) {
    //stroke(200, 50, 50);
  } else {
    noStroke();
  }
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
    location = new PVector(0,0,0);
    velocity = new PVector(0,0,0);
    gravityForce = new PVector(0,0,0);
    frictionForce = new PVector(0,0,0);

    this.radius = radius;
  }
  
  void update(){
    gravityForce.x = sin(game.rotZ)*GRAVITY_CONSTANT;
    gravityForce.z = sin(game.rotX)*GRAVITY_CONSTANT*(-1);
    //gravityForce.y = (-1)*GRAVITY_CONSTANT;
    
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
    if(location.z>=game.box.depth/2){
      location.z = game.box.depth/2;
      velocity.z = velocity.z*(-1)*BOUNCING_COEFF;
    }
    if(location.z<=-game.box.depth/2){
      location.z = -game.box.depth/2;
      velocity.z = velocity.z*(-1)*BOUNCING_COEFF;
    }
  }

  void checkCylinderCollision(ObstacleManager obstacles){
    for(ClosedCylinder cyl : obstacles.obstacleList){
      if(cyl.collisionWithMover(this)){
        PVector normal = new PVector(cyl.location.x- this.location.x, 0 , cyl.location.z-this.location.z);

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
          translate(mover.location.x, -box.height/2-mover.radius, mover.location.z);
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

        translate(mover.location.x, -20, mover.location.z);
        mover.draw();
      popMatrix();
      
      ghost.setGhost();

      float x = clamp(mouseX, 124, 600-124);
      float y = clamp(mouseY, 124, 600-124);

      x = map(x, 124, 600-124, -game.box.width/2, game.box.width/2);
      y = map(y, 124, 600-124, -game.box.depth/2, game.box.depth/2);

      ghost.move(new PVector(x,0,y));
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
  ArrayList<ClosedCylinder> obstacleList;

  ObstacleManager() {
    this.obstacleList = new ArrayList<ClosedCylinder>();
  }
  
  void add(ClosedCylinder obstacle) {
    obstacleList.add(obstacle);
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
  float cylHeight;
  int resolution;

  boolean ghost = false;
  float R = 0;
  float G = 0;
  float B = 0;
  
  ClosedCylinder() {
    this.location = new PVector(0,0,0);
  }

  ClosedCylinder(PVector location, float radius, float cylHeight, int resolution){
    this.location = new PVector(location.x, location.y, location.z);
    this.radius = radius;
    this.cylHeight = cylHeight;
    this.resolution = resolution;
  }
  
  void move(PVector newLocation){
     location = new PVector(newLocation.x, newLocation.y, newLocation.z);
  }
  
  boolean collisionWithMover(Mover mover){
    float distX = (location.x-mover.location.x);
    float distY = (location.z-mover.location.z);
    float squareDist = distX*distX + distY*distY;
    float squareRadius = (radius+mover.radius)*(radius+mover.radius);
    
    return squareDist <= squareRadius;
  }
  
  boolean collisionWithCylinder(ClosedCylinder cyl){
    float distX = (location.x-cyl.location.x);
    float distY = (location.z-cyl.location.z);
    float squareDist = distX*distX + distY*distY;
    float squareRadius = (radius+cyl.radius)*(radius+cyl.radius);
    
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
    translate(location.x,-game.box.height+10,location.z);
    rotateX(PI/2);
    float angle;
    float [] x = new float [cylinderResolution + 1];
    float [] y = new float [cylinderResolution + 1];
    
    //get the x and y position
    for(int i = 0; i< x.length; i++){
      angle = (TWO_PI / cylinderResolution)*i;
      x[i] = sin(angle) * cylinderBaseSize;
      y[i] = cos(angle) * cylinderBaseSize;
    }
    PShape o = createShape();
    beginShape(QUAD_STRIP);
    if(ghost) {
      fill(color(255,255,224,100));
    } else {
      fill(color(R,G,B));
    }
    //draw the border of the cylinder
    for(int i = 0; i<x.length; i++){
      vertex(x[i], y[i], 0);
      vertex(x[i], y[i], cylinderHeight);
    }
      vertex(x[0],y[0],0);
      vertex(x[0], y[0], cylinderHeight);
    //top surface
    for(int i = 0; i<x.length; i++){
      vertex(0,0,cylinderHeight);
      vertex(x[i],y[i],cylinderHeight);
    }
      vertex(0,0,cylinderHeight);
      vertex(x[0],y[0],cylinderHeight);
    //bottom surface
   for(int i = 0; i<x.length; i++){
      vertex(0,0,0);
      vertex(x[i],y[i],0);
    }
      vertex(0,0,0);
      vertex(x[0],y[0],0);
    endShape();
    noFill();
    popMatrix();
  }
}
