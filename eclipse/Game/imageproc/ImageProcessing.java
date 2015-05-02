package imageproc;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class ImageProcessing extends PApplet {
	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = 1634966782356685343L;
	
	private PImage image;
	private PImage result;
	private Capture cam;
	float discretizationStepPhi = 0.006f;
	float discretizationStepR = 2.5f;

	@Override
	public void setup() {
	//	size(640,480);
		image = loadImage("resources/boards/board2.jpg");
		/*camStart();
		if(cam.available()==true){
			cam.read();
		}
		image = cam.get();
		*/
		size(image.width, image.height);
	}

	@Override
	public void draw(){
		//drawCam();
		drawPic();
	}
	private void drawCam(){
		if(cam.available()==true){
			cam.read();
		}
		image = cam.get();
		result = sobel(hsbFilter(gaussianBlur(image)));
		image(result,0,0);
		hough(result);
	}
	private void drawPic(){
		image = loadImage("resources/boards/board3.jpg");
		result = sobel(hsbFilter(gaussianBlur(image)));
		image(image,0,0);
		hough(result);
	}
	
	public boolean camStart(){
		String[] cameras = Capture.list();
		if(cameras.length == 0){
			println("There are no camera available for capture.");
			image = loadImage("resources/boards/board1.jpg");
			exit();
			return true;
		}else{
			println("Available cameras :");
			for(int i =0 ; i<cameras.length;i++){
				println(i+". "+cameras[i]);
			}
			if(cameras.length>26){
				cam = new Capture(this,cameras[1]);
				println("Selected : "+cameras[1]);
			} else {
				cam = new Capture(this,cameras[0]);
			}
			cam.start();
			if(cam.available()==true){
				cam.read();
			}
			image = cam.get();
			return true;
		}
	}

	
	public int clamp(int val, int min, int max) {
		if(val < min) {
			return min;
		} else if(val > max) {
			return max;
		} else {
			return val;
		}
	}

	public PImage gaussianBlur(PImage img) {
		PImage res = createImage(img.width, img.height, RGB);

		float[][] gaussianKernel = new float[][] { {9, 12, 9}, {12, 15, 12}, {9, 12, 9} };

		for(int i=0; i<img.width*img.height; i++) {
			int x = i%img.width;
			int y = i/img.width;

			int r = 0;
			int g = 0;
			int b = 0;

			for(int k1=0; k1<3; k1++) {
				for(int k2=0; k2<3; k2++) {
					int x1 = clamp(x+k1-1, 0, img.width-1);
					int y1 = clamp(y+k2-1, 0, img.height-1);

					r += gaussianKernel[k1][k2] * red(img.pixels[y1*img.width + x1]);
					g += gaussianKernel[k1][k2] * green(img.pixels[y1*img.width + x1]);
					b += gaussianKernel[k1][k2] * blue(img.pixels[y1*img.width + x1]);
				}
			}

			int s = 0;
			for(int j=0; j<gaussianKernel.length; j++) {
				for(int jj=0; jj<gaussianKernel[j].length; jj++) {
					s += gaussianKernel[j][jj];
				}
			}

			res.pixels[i] = color(r/s, g/s, b/s);
		}
		
		return res;
	}

	public PImage hsbFilter(PImage img) {
		float hueMin = 80f;
		float hueMax = 140f;
		
		float bMin = 20f;
		float bMax = 220;
		
		float sMin = 80f;

		PImage res = createImage(img.width, img.height, RGB);

		for(int i=0; i<img.width*img.height; i++) {
			float h = hue(img.pixels[i]);
			float b = brightness(img.pixels[i]);
			float s = saturation(img.pixels[i]);
			if(h > hueMin && h < hueMax && b > bMin && b < bMax && s > sMin) {
				res.pixels[i] = color(255);
			} else {
				res.pixels[i] = color(0);
			}
		}
		
		return res;
	}

	public PImage sobel(PImage img) {
		float[][] hKernel = {
				{ 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, -1, 0 }
			};
		float[][] vKernel = {
				{ 0, 0, 0 },
				{ 1, 0, -1 },
				{ 0, 0, 0 }
			};
		
		PImage res = createImage(img.width, img.height, ALPHA);
		
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			res.pixels[i] = color(0);
		}
		
		float max = 0;
		float[] buffer = new float[img.width * img.height];
		
		// Double convolution into buffer
		for(int x=2; x<img.width-2; x++) {
			for(int y=2; y<img.height-2; y++) {
				
				float sum_h = 0f;
				float sum_v = 0f;
				
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						float b = brightness(img.pixels[(y+j-1) * img.width + (x+i-1)]);
						sum_h += b*hKernel[i][j];
						sum_v += b*vKernel[i][j];
					}
				}				
				float val = sqrt(pow(sum_h, 2) + pow(sum_v, 2));			
				if(val > max) max = val;		
				buffer[y*img.width + x] = val;		
			}
		}		
		// Create from buffer
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int)(max * 0.3f)) { // 30% of the max
					res.pixels[y * img.width + x] = color(255);
				} else {
					res.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return res;
	}
	
	
	public void hough(PImage edgeImg){
		
			//dimensions of the accumulator
			int phiDim = (int)(Math.PI/discretizationStepPhi);
			int rDim = (int)(((edgeImg.width+edgeImg.height)*2+1)/discretizationStepR);
			
			//accumulator with a 1pix margin around
			int[] accumulator = new int[(phiDim+2)*(rDim+2)];
			int rMax = rDim+2;
			
			//Fill the accumulator: on edge point (white pixel of the edgeImg), store all possible (r,phi) pair discribing 
			//lines going through this point
			for(int y = 0; y<edgeImg.height;y++){
				for(int x = 0; x<edgeImg.width;x++){
					//are we on edge?
					if(brightness(edgeImg.pixels[y*edgeImg.width+x])!=0){
						//...determine all the lines (r,phi) passing through
						// pixel(x,y), convert(r, phi) to coordinates in accumulator, increment accumulator;
						int i = 0;
						for(float phi = 0; phi<Math.PI; phi+=discretizationStepPhi){
							float r = (x)*cos(phi)+(y)*sin(phi);
							
							int index = (int)(r/discretizationStepR + (rDim-1)*0.5 + ((phi/discretizationStepPhi)+1)*(rDim+2)+1);
							int index2 = (int)(1+phi/discretizationStepPhi)*(rDim+2);
							
							accumulator[(int)(index)] ++;
							i++;
							}
						
						}
					}
						
				}	
			PImage houghImg = createImage(rDim+2,phiDim+2, ALPHA);
			for(int i = 0; i<accumulator.length;i++){
				houghImg.pixels[i] = color(min(255,accumulator[i]));
			}
			houghImg.updatePixels();
			houghImg.save("resources/boards/Accumulator.png");
	/////plotting the lines
			for(int idx = 0; idx<accumulator.length;idx++){
				if(accumulator[idx]>200){
					int accPhi = (int)(idx/(rDim+2))-1;
					int accR = idx - (accPhi+1)*(rDim+2)-1;
					float r = (accR - (rDim-1)*0.5f)*discretizationStepR;
					float phi = accPhi*discretizationStepPhi;
					
					
				
					//Cartesian equation of a line : y = ax+b
					// in polar : y = (-cos(phi)/sin(phi))x+(r/sin(phi)
					// => y = 0 : x = r/cos(phi)
					// => x = 0 : y = r/sin(phi)
					
					//compute the intersection of this  line with the 4 borders of the image
					
					int x0 = 0;
					int y0 = (int)(r/sin(phi));
					int x1 = (int)(r/cos(phi));
					int y1 = 0;
					int x2 = edgeImg.width;
					int y2 = (int)(-cos(phi)/sin(phi) * x2+r/sin(phi));
					int y3 = edgeImg.height;
					int x3 = (int)(-(y3-r/sin(phi))*(sin(phi)/cos(phi)));
			
					int px = (int)(r*cos(phi));
					int py = (int)(r*sin(phi));
				//Finally, plot the lines
					stroke(204,102,0);
					//noStroke();
					if(y0>0){
						if(x1>0){
							line(x0,y0,x1,y1);
						}
						else if(y2>0) {
							 line(x0*cos(phi),y0*sin(phi),x2*cos(phi),y2*sin(phi));
						}
						else {
							line(x0,y0,x3,y3);
						}
					}else{
						if(x1>0){
							if(y2>0){
								line(x1,y1,x2,y2);
							}
							else {
								line(x1,y1,x3,y3);
							}
						}else{
							
							line(x2,y2,x3,y3);
						}
					}
				}
			}
	}
}
