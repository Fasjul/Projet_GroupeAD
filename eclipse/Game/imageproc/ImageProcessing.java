package imageproc;

import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet {
	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = 1634966782356685343L;
	
	private PImage image;
	private PImage drawing;

	@Override
	public void setup() {
		image = loadImage("resources/boards/board1.jpg");
		size(image.width, image.height);
		
		test();
	}
	
	public void test() {
		long start, stop;
		
	// Gaussian
		// Gaussian sequential
		start = System.currentTimeMillis();
		gaussianBlur(image, 1);
		stop = System.currentTimeMillis();
		
		System.out.println("Sequential gaussian took " + (stop-start) + "ms");
		
		// Gaussian parallel
		start = System.currentTimeMillis();
		gaussianBlur(image);
		stop = System.currentTimeMillis();
		
		System.out.println("Parallel gaussian took " + (stop-start) + "ms");
		
	// HSB Filter
		start = System.currentTimeMillis();
		hsbFilter(image);
		stop = System.currentTimeMillis();
		
		System.out.println("Sequential HSB took " + (stop-start) + "ms");
		
	// Sobel
		// Sobel sequential
		start = System.currentTimeMillis();
		sobel(image, 1);
		stop = System.currentTimeMillis();
		
		System.out.println("Sequential sobel took " + (stop-start) + "ms");
		
		// Sobel parallel
		start = System.currentTimeMillis();
		sobel(image, 1);
		stop = System.currentTimeMillis();
		
		System.out.println("Parallel sobel took " + (stop-start) + "ms");
		
	// ALL
		// ALL sequential
		start = System.currentTimeMillis();
		sobel(hsbFilter(gaussianBlur(image, 1)), 1);
		stop = System.currentTimeMillis();
				
		System.out.println("Sequential all took " + (stop-start) + "ms");
		
		// ALL parallel
		start = System.currentTimeMillis();
		applyAll(image);
		stop = System.currentTimeMillis();
		
		System.out.println("\"Parallel\" all took " + (stop-start) + "ms");
		
	// Draw
		drawing = applyAll(image);
		image(drawing, 0, 0);
		
	}
	
	
	
	@Override
	public void draw() {
//		result = sobel(hsbFilter(gaussianBlur(image)));
//		image(result, 0, 0);
//		hough(result);
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
	
	
	
	public PImage applyAll(PImage img) {
		PImage inter1 = gaussianBlur(img);
		PImage inter2 = hsbFilter(inter1);
		PImage inter3 = sobel(inter2,1);
		return inter3;
	}
	
	
	
	public PImage gaussianBlur(PImage img) {
		int nCores = Runtime.getRuntime().availableProcessors();
		if(nCores < 1) {
			System.out.println("Is your JVM correctly set up?");
			throw new RuntimeException("Runtime.availableProcessors() did not give a valid answer!");
		}
		return gaussianBlur(img, Math.min(nCores, 4));
	}
	
	public PImage gaussianBlur(final PImage img, int nOfThreads) {
		Thread[] threads = new Thread[nOfThreads];
		
		final PImage result = createImage(img.width, img.height, RGB);
		
		int totalPixels = img.width*img.height;
		int threadStep  = totalPixels/nOfThreads;
		
		for(int i=0; i<nOfThreads; i++) {
			final int tFrom = i*threadStep;
			final int tTo = (i+1)*threadStep;
			
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					gaussianBlurOn(img, result, tFrom, tTo);
				}
			});
		}
		
		for(int i=0; i<nOfThreads; i++) {
			threads[i].start();
		}
		
		try {
			for(int i=0; i<nOfThreads; i++) {
				threads[i].join();
			}
		} catch(InterruptedException e) {
			System.out.println("Could not join all Threads in gaussianBlur... Did the system kill one?");
		}
		
		return result;
	}

	private PImage gaussianBlurOn(PImage img, PImage res, int from, int to) {
		float[][] gaussianKernel = new float[][] { {9, 12, 9}, {12, 15, 12}, {9, 12, 9} };
		float s = 99f;

		for(int i=from; i<to; i++) {
			int x = i%img.width;
			int y = i/img.width;

			int r = 0;
			int g = 0;
			int b = 0;

			for(int k1=0; k1<3; k1++) {
				for(int k2=0; k2<3; k2++) {
					int x1 = clamp(x+k1-1, 0, img.width-1);
					int y1 = clamp(y+k2-1, 0, img.height-1);

					r += gaussianKernel[k1][k2] * red(img.pixels[y1*width + x1]);
					g += gaussianKernel[k1][k2] * green(img.pixels[y1*width + x1]);
					b += gaussianKernel[k1][k2] * blue(img.pixels[y1*width + x1]);
				}
			}

			res.pixels[i] = color(r/s, g/s, b/s);
		}
		
		return res;
	}
	
	
	
	public PImage hsbFilter(PImage img) {
		float hueMin =  80f; // hue minimum
		float hueMax = 140f; // hue maximum
		
		float bMin =  30f;  // brightness minimum
		float bMax = 200f;  // brightness maximum
		
		float sMin = 80f;  // saturation minimum
		
		PImage result = createImage(img.width, img.height, RGB);

		for(int i=0; i<img.width*img.height; i++) {
			float h = hue(img.pixels[i]);
			float b = brightness(img.pixels[i]);
			float s = saturation(img.pixels[i]);
			if(h > hueMin && h < hueMax && b > bMin && b < bMax && s > sMin) {
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}
		
		return result;
	}
	
	
	
	public PImage sobel(PImage img) {
		int nCores = Runtime.getRuntime().availableProcessors();
		if(nCores < 1) {
			System.out.println("Is your JVM correctly set up?");
			throw new RuntimeException("Runtime.availableProcessors() did not give a valid answer!");
		}
		return sobel(img, nCores);
	}
	
	public PImage sobel(final PImage img, int nOfThreads) {
		Thread[] threads = new Thread[nOfThreads];
		
		final PImage res = createImage(img.width, img.height, RGB);
		
		int totalPixels = img.width*img.height;
		int threadStep  = totalPixels/nOfThreads;
		
		for(int i=0; i<nOfThreads; i++) {
			final int tFrom = i*threadStep;
			final int tTo = (i+1)*threadStep;
			
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					sobelOn(img, res, tFrom, tTo);
				}
			});
		}
		
		for(int i=0; i<nOfThreads; i++) {
			threads[i].start();
		}
		
		for(int i=0; i<nOfThreads; i++) {
			try {
				threads[i].join();
			} catch(InterruptedException e) {
				System.out.println("Could not join all Threads in sobel... Did the system kill one?");
			}
		}
		
		return res;
	}

	public PImage sobelOn(PImage img, PImage res, int from, int to) {
		float[][] hKernel = {
				{ 0,  1, 0 },
				{ 0,  0, 0 },
				{ 0, -1, 0 }
			};
		float[][] vKernel = {
				{ 0, 0,  0 },
				{ 1, 0, -1 },
				{ 0, 0,  0 }
			};
		
		float max = 0f;
		float[] buffer = new float[img.width * img.height];
		
		// Double convolution into buffer
		for(int p=from; p<to; p++) {
			
			int x = p%img.width;
			int y = p/img.width;
			
			if(x < 2 || x >= (img.width-2) || y < 2 || y >= (img.height-2)) {
				buffer[p] = 0f;
				continue;
			}
			
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
			buffer[p] = val;
		}
		
		// Create from buffer
		for(int i=from; i<to; i++) {
			if (buffer[i] > (int)(max * 0.3f)) { // 30% of the max
				res.pixels[i] = color(255);
			} else {
				res.pixels[i] = color(0);
			}
		}
		
		return res;
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
						float r = (x)*cos(phi)+(y)*sin(phi);
						if(r<0) r += (rDim - 1) / 2;
						accumulator[(int)(i*rMax+floor(r))] ++;
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
					
					// !!!!!!	A FIXER !!!!!!!
					r = idx%rMax;
					phi = idx/rMax;
					
				
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
			
					int px = (int) (r*cos(phi));
					int py = (int) (r*sin(phi));
					
					stroke(56,54,170);
					noFill();
					ellipse(px,py,25,25);
				/*	System.out.println("========NOUVEAU POINT LUMINEUX========");
					System.out.println("Phi : "+phi);
					System.out.println("R : "+r);
					
					System.out.println("(x0,y0,x1,y1,x2,y2,x3,y3) = ("
					+x0+","+y0+","+x1+","+y1+","+x2+","+y2+","+x3+","+y3+")");
					
					System.out.println(" ");
				*/
				//Finally, plot the lines
					stroke(204,102,0);
					//noStroke();
					if(y0>0){
						if(x1>0){
							System.out.println("Selected = 1");
							line(x0,y0,x1,y1);
						}
						else if(y2>0) {
							System.out.println("Selected = 2");
							 line(x0,y0,x2,y2);
						}
						else {
							System.out.println("Selected = 3");
							line(x0,y0,x3,y3);
						}
					}else{
						if(x1>0){
							if(y2>0){
								System.out.println("Selected = 4");
								line(x1,y1,x2,y2);
							}
							else {
								System.out.println("Selected =5");
								line(x1,y1,x3,y3);
							}
						}else{
							System.out.println("Selected = 6");
							line(x2,y2,x3,y3);
						}
					}
				}
			}
	}
}
