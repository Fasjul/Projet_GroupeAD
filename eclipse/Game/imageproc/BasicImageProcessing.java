package imageproc;

import processing.core.PApplet;
import processing.core.PImage;
import tools.HScrollbar;

@SuppressWarnings("serial")
public class BasicImageProcessing extends PApplet {

	private PImage img;
	private PImage result;

	private HScrollbar bar1;
	private HScrollbar bar2;

	private float[][] kernel1 = new float[][] { {0, 0, 0}, {0, 2, 0}, {0, 0, 0} };
	private float[][] kernel2 = new float[][] { {0, 1, 0}, {1, 0, 1}, {0, 1, 0} };
	private float[][] kernel3 = new float[][] { {3, 1, 3}, {1, 0, 1}, {3, 1, 3} };
	private float[][] gaussianKernel = new float[][] { {9, 12, 9}, {12, 15, 12}, {9, 12, 9} };


	@Override
	public void setup() {
		img = loadImage("resources/boards/board1.jpg");
		size(img.width, img.height);

		bar1 = new HScrollbar(this, 0, height-50, width, 20);
		bar2 = new HScrollbar(this, 0, height-20, width, 20);
		result = sobel(img);
		hough(result);
	}

	@Override
	public void draw() {
		image(result, 0, 0);
		bar1.display();
		bar2.display();
	}

	@Override
	public void mousePressed() {
		bar1.update();
		bar2.update();
	}

	@Override
	public void mouseReleased() {
		//updateResult();
	}

	@Override
	public void mouseMoved() {
		bar1.update();
		bar2.update();
	}

	@Override
	public void mouseDragged() {
		bar1.update();
		bar2.update();
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
		
		PImage result = createImage(img.width, img.height, ALPHA);
		
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		
		float max = 0;
		float[] buffer = new float[img.width * img.height];
		
		// Double convolution into buffer
		
		for(int x=2; x<img.width-2; x++) {
			for(int y=2; y<img.height-2; y++) {
		
				float sum_h = 0;
				float sum_v = 0;
				
				for(int i=0; i<3; i++) {
					for(int j=0; j<3; j++) {
						float b = brightness(img.pixels[(y+j-1)*img.width + (x+i-1)]);
						sum_h += hKernel[i][j] * b;
						sum_v += vKernel[i][j] * b;
					}
				}
				
				float val = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
				
				buffer[y*img.width + x] = val;
				
				if(val > max) max = val;
				
				
			}
		}
			
		// Create from buffer
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int) (max * 0.3f)) { // 30% of the max
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return result;

	}

	private void convolute() {
		result = createImage(img.width, img.height, RGB);

		float[][] matrix = gaussianKernel;

		for(int i=0; i<img.width*img.height; i++) {
			int x = i%img.width;
			int y = i/img.width;

			int r = 0;
			int g = 0;
			int b = 0;

			for(int k1=0; k1<matrix.length; k1++) {
				for(int k2=0; k2<matrix[k1].length; k2++) {
					int x1 = clamp(x+k1-1, 0, width-1);
					int y1 = clamp(y+k2-1, 0, height-1);

					r += matrix[k1][k2] * red(img.pixels[y1*width + x1]);
					g += matrix[k1][k2] * green(img.pixels[y1*width + x1]);
					b += matrix[k1][k2] * blue(img.pixels[y1*width + x1]);
				}
			}

			int s = 0;
			for(int j=0; j<matrix.length; j++) {
				for(int jj=0; jj<matrix[j].length; jj++) {
					s += matrix[j][jj];
				}
			}

			result.pixels[i] = color(r/s, g/s, b/s);
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

	private void updateResult() {
		float val1 = bar1.getPos()*256;
		float val2 = bar2.getPos()*256;

		result = createImage(img.width, img.height, RGB);

		for(int i=0; i<img.width*img.height; i++) {
			float hue = hue(img.pixels[i]);
			if(val1 <= hue && hue < val2 ) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = 0;
			}
		}
	}
	
	
	public void hough(PImage edgeImg){
		float discretizationStepPhi = 0.006f;
		float discretizationStepR = 2.5f;
		
		
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
						float r = x*cos(phi)+y*sin(phi);
						accumulator[i*rMax + floor(r)] ++;
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
	/////plotting the lines
			for(int idx = 0; idx<accumulator.length;idx++){
				if(accumulator[idx]>50){
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
					int y3 = edgeImg.width;
					int x3 = (int)(-(y3-4/sin(phi))*(sin(phi)/cos(phi)));
				
				//Finally, plot the lines
					stroke(204,102,0);
					if(y0>0){
						if(x1>0) line(x0,y0,x1,y1);
						else if (y2>0) line(x0,y0,x2,y2);
						else line(x0,y0,x3,y3);
					}else{
						if(x1>0){
							if(y2>0) line(x1,y1,x2,y2);
							else line(x1,y1,x3,y3);
						}else{
							line(x2,y2,x3,y3);
						}
					}
				}
			}
	}

}
