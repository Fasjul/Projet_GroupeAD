//--------------------------//
//      VARIABLES           //
//--------------------------//
 
   //Image
   int img_w = 500;
   int img_h = 500;
   //origin
   int ox = 0;
   int oy = 0;
   int oz = 0;
   //eye
   int eyex = 0;
   int eyey = 0;
   //zoom
   float zoom = 1;
   float zoomx = 1;
   float zoomy = 1;
   float zoomz = 1;
   //angles
   float anglex = 0;
   float angley = 0;
   //Box size
   int sizeX = 100;
   int sizeY = 150;
   int sizeZ = 300;
   //mouse
   int clickedX = img_w/2-sizeX/2;
   int clickedY = img_h/2-sizeY/2;
//////////////////////////

void setup() {
  size(img_w,img_h,P2D);
  frameRate(20);
}

void draw() {
    background(255, 255, 255);
    translate(clickedX,clickedY);
    My3DPoint eye = new My3DPoint(eyex, eyey, -5000);
    My3DPoint origin = new My3DPoint(ox, oy, oz);
    My3DBox input3DBox = new My3DBox(origin, sizeX,sizeY,sizeZ);
    //rotated around x
    float[][] rotX = rotateXMatrix(anglex);
    float[][] rotY = rotateYMatrix(angley);
    float[][] scaling = scaleMatrix(zoom,zoom,zoom);
    input3DBox = transformBox(input3DBox, rotX);
    input3DBox = transformBox(input3DBox, rotY);
    input3DBox = transformBox(input3DBox, scaling);
    projectBox(eye, input3DBox).render();
  }
 //------------------------------//
//     Methodes d'interaction   //
//------------------------------//

void keyPressed() {
  // Les touches flechées servent à pivoter la forme
  if (key == CODED) {
    if (keyCode == UP) {
      anglex = anglex+PI/64;
    } else if (keyCode == DOWN) {
      anglex = anglex-PI/64;
    }
     
    if(keyCode == LEFT){
       angley = angley+PI/64;
    } else if (keyCode == RIGHT){
       angley = angley-PI/46; 
    }
  }
  // Les touches + et - servent à zoomer
    if(key == 43){
      if(zoom+0.1 >5) zoom = 5;
      else if(zoom>=0 && zoom<0.4) zoom = zoom+0.05;
      else zoom = zoom+0.1;
    }else if(key == 45){
       if(zoom-0.05<0)zoom = 0;
       else if(zoom>=0 && zoom<0.4) zoom = zoom-0.05;
       else zoom = zoom-0.1; 
  }
}
 //Move the box with the mouse by clicking 
 //on the position of the future origin
 void mouseClicked(){
   clickedX = mouseX;
   clickedY = mouseY;
 }
 
 void mouseDragged(){
   clickedX = mouseX;
   clickedY = mouseY;
 }
//--------------------------//
//       Point Classes      //
//--------------------------//

class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y) {
 this.x = x;
 this.y = y;
  }
}

class My3DPoint {
    float x, y, z;
  My3DPoint(float x, float y, float z) {
   this.x = x;
   this.y = y;
   this.z = z;
  }
}

My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
  float xp = ((p.x-eye.x)*eye.z)/(eye.z-p.z);
  float yp = ((p.y-eye.y)*eye.z)/(eye.z-p.z);
  return new My2DPoint(xp, yp);
}

//--------------------------//
//       Box Classes        //
//--------------------------//

class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
     this.s = s;
   }
  void render() {
   line(s[0].x, s[0].y, s[1].x, s[1].y);
   line(s[0].x, s[0].y, s[3].x, s[3].y);
   line(s[0].x, s[0].y, s[4].x, s[4].y);
   line(s[1].x, s[1].y, s[2].x, s[2].y);
   line(s[1].x, s[1].y, s[5].x, s[5].y);
   line(s[2].x, s[2].y, s[3].x, s[3].y);
   line(s[2].x, s[2].y, s[6].x, s[6].y);
   line(s[3].x, s[3].y, s[7].x, s[7].y);
   line(s[4].x, s[4].y, s[5].x, s[5].y);
   line(s[4].x, s[4].y, s[7].x, s[7].y);
   line(s[5].x, s[5].y, s[6].x, s[6].y);
   line(s[6].x, s[6].y, s[7].x, s[7].y);
  }
}

class My3DBox{
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ) {
       float x = origin.x;
       float y = origin.y;
       float z = origin.z;
       this.p = new My3DPoint[] {
           new My3DPoint(x, y+dimY, z+dimZ), 
           new My3DPoint(x, y, z+dimZ), 
           new My3DPoint(x+dimX, y, z+dimZ), 
           new My3DPoint(x+dimX, y+dimY, z+dimZ), 
           new My3DPoint(x, y+dimY, z), 
           origin, 
           new My3DPoint(x+dimX, y, z), 
           new My3DPoint(x+dimX, y+dimY, z)
     };
  }
  My3DBox(My3DPoint[] p) {
           this.p = p;
  }
}

My2DBox projectBox(My3DPoint eye, My3DBox box) {
  My2DPoint[] twoDp = new My2DPoint[box.p.length];
  for (int i = 0; i<box.p.length; i++) {
     twoDp[i] = projectPoint(eye, box.p[i]);
    }
  return new My2DBox(twoDp);
}


//-----------------//
// TRANSFORMATIONS //
//-----------------//


float[][] rotateXMatrix(float angle) {
  return(new float[][] {
         {1, 0, 0, 0 }, 
         {0, cos(angle), sin(angle), 0}, 
         {0, -sin(angle), cos(angle), 0}, 
         {0, 0, 0, 1}
   });
}
float [][] rotateYMatrix(float angle) {
  return(new float[][] { 
          {cos(angle), 0, sin(angle), 0 },
          {0, 1, 0, 0},
          {-sin(angle), 0, cos(angle), 0},
          {0, 0, 0, 1}
    });
}
float [][] rotateZMatrix(float angle) {
  return(new float[][] {
          {cos(angle), -sin(angle), 0, 0},
          {sin(angle), cos(angle), 0, 0},
          { 0, 0, 1, 0}, 
          {0, 0, 0, 1} });
}
float[][] scaleMatrix(float x, float y, float z) {
  return (new float[][] {
          { x, 0, 0, 0},
          { 0, y, 0, 0},
          { 0, 0, z, 0},
          { 0, 0, 0, 1}
    });
  }
float[][] translationMatrix(float x, float y, float z) {
  return (new float[][] {
          {1, 0, 0, x}, 
          {0, 1, 0, y},
          {0, 0, 1, z},
          {0, 0, 0, 1}
  });
}

//--------------------------//
//METHODES DE TRANSFORMATION//
//--------------------------//

float[] matrixProduct(float[][] a, float[] b) {
  float[] c = new float[]{ 0.0f, 0.0f, 0.0f, 0.0f };
  for (int i = 0; i<4; i++) {
     for (int j = 0; j<4; j++) {
      c[i] += a[i][j]*b[j];
     }
    }
  return c;
}

float[] homogeneous3DPoint(My3DPoint p){
  return new float[]{p.x, p.y, p.z, 1};
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  My3DPoint[] newPoints = new My3DPoint[box.p.length];
  for(int i=0; i<box.p.length; i++) {
    float[] p = homogeneous3DPoint(box.p[i]);
    p = matrixProduct(transformMatrix, p);
    My3DPoint newPoint = euclidian3DPoint(p);
    newPoints[i] = newPoint;
  }
  return new My3DBox(newPoints);
}

