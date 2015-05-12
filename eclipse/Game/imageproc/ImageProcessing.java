package imageproc;

import java.awt.Button;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

public class ImageProcessing extends PApplet {
	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = 1634966782356685343L;

	private PImage camera, sobel, image, accuImg;
	private Capture cam;
	float discretizationStepPhi = 0.006f;
	float discretizationStepR = 2.5f;
	boolean cameraBool = false;
	private int phiDim = (int)(Math.PI/discretizationStepPhi);
	private float[] tabSin = new float[phiDim];
	private float[] tabCos = new float[phiDim];
	private boolean plotLines = false;
	
	// The following is hard code which can't be changed during execution.
	/**
	 * Set to true if you want to use the camera feed,
	 * false for static image.
	 */
	private final boolean useCamera = false;
	/**
	 * Which board to use when drawing the static image (must be between 1 and 4!).
	 */
	private final int board = 4;

	@Override
	public void setup() {
		tabInitialization();
		if(!useCamera) {
			image = loadImage("resources/boards/board" + board + ".jpg");
			size((image.width), image.height);
			drawPic();
		} else {
			camStart();
			if(cam.available()==true){
				cam.read();
			}
			image = cam.get();
			size(1200,480);
		}
		test();
	}

	@Override
	public void draw(){
		if(useCamera) {
			drawCam();
			
		}else{
			drawPic();
		}
	}

	private void drawCam(){
		if(cam.available()==true){
			cam.read();
		}
		camera = cam.get();
		sobel = applyAll(camera);
		image(camera, 0, 0);
		hough(sobel, 4, tabCos, tabSin);
		accuImg = loadImage("resources/boards/Accumulator.png");
		image(accuImg, 0, camera.height);
		image(sobel, camera.width, 0);
	}
	
	private void drawPic(){
		sobel = applyAll(image);
		image(image, 0, 0);
		hough(sobel, 100, tabCos, tabSin);
		accuImg = loadImage("resources/boards/Accumulator.png");
		image(sobel, image.width - 170, 0);
		image(accuImg, image.width + sobel.width - 340, 0);
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

	public void tabInitialization(){
		//TODO OPTIMISATION (step 4 week 10) -> A Placer hors de la fonction, par exemple au setup, afin d'�viter de devoir recalculer
		tabSin = new float[phiDim];
		tabCos = new float[phiDim];

		float ang = 0;
		float inverseR = 1.f/discretizationStepR;

		for(int accPhi = 0; accPhi <phiDim ; ang+=discretizationStepPhi, accPhi ++){
			tabSin[accPhi] = (float)(Math.sin(ang)*inverseR);
			tabCos[accPhi] = (float)(Math.cos(ang)*inverseR);
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



	public PImage applyAll(PImage img) {
		PImage inter1 = gaussianBlur(img);
		PImage inter2 = hsbFilter(inter1);
		PImage inter3 = sobel(inter2);
		return inter3;
	}



	public PImage gaussianBlur(PImage img) {
		final float[][] gaussianKernel = new float[][] { {9, 12, 9}, {12, 15, 12}, {9, 12, 9} };
		final float s = 99f;
		final int border = 2;

		PImage result = createImage(img.width, img.height, RGB);

		final int width = img.width;
		final int height = img.height;
		final int nPixels = width*height;

		for(int i=0; i<nPixels; i++) {
			int x = i % width;
			int y = i / width;

			if(x < border || x >= (width-border) || y < border || y >= (height-border))
				continue;

			int r = 0;
			int g = 0;
			int b = 0;

			for(int k1=0; k1<3; k1++) {
				for(int k2=0; k2<3; k2++) {
					int x1 = x + k1 - 1;
					int y1 = y + k2 - 1;

					int c = img.pixels[y1*width+x1];

					r += gaussianKernel[k1][k2] * red(c);
					g += gaussianKernel[k1][k2] * green(c);
					b += gaussianKernel[k1][k2] * blue(c);
				}
			}

			result.pixels[i] = color(r/s, g/s, b/s);
		}

		return result;
	}



	public PImage hsbFilter(PImage img) {
		float hueMin =  80f; // hue minimum
		float hueMax = 140f; // hue maximum

		float bMin =  30f;  // brightness minimum
		float bMax = 200f;  // brightness maximum

		float sMin = 80f;  // saturation minimum

		PImage result = createImage(img.width, img.height, RGB);

		final int nPixels = img.width*img.height;

		for(int i=0; i<nPixels; i++) {
			int c = img.pixels[i];
			float h = hue(c);
			float b = brightness(c);
			float s = saturation(c);
			if(h > hueMin && h < hueMax && b > bMin && b < bMax && s > sMin) {
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}

		return result;
	}

	public PImage sobel(PImage img) {
		final float[][] hKernel = {
				{ 0,  1, 0 },
				{ 0,  0, 0 },
				{ 0, -1, 0 }
		};
		final float[][] vKernel = {
				{ 0, 0,  0 },
				{ 1, 0, -1 },
				{ 0, 0,  0 }
		};

		final int width = img.width;
		final int height = img.height;
		final int nPixels = width*height;

		final int border = 2;

		float max = 0f;
		float[] buffer = new float[img.width * img.height];

		// Double convolution into buffer
		for(int p=0; p<nPixels; p++) {

			int x = p % width;
			int y = p / width;

			if(x < border || x >= (width-border) || y < border || y >= (height-border)) {
				buffer[p] = 0f;
				continue;
			}

			float sum_h = 0f;
			float sum_v = 0f;

			for(int i=0; i<3; i++) {
				for(int j=0; j<3; j++) {
					float b = brightness(img.pixels[(y+j-1) * width + (x+i-1)]);

					sum_h += b*hKernel[i][j];
					sum_v += b*vKernel[i][j];
				}
			}

			float val = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
			buffer[p] = val;

			if(val > max) max = val;
		}

		PImage result = createImage(width, height, RGB);

		// Create from buffer
		for(int i=0; i<nPixels; i++) {
			if (buffer[i] > (int)(max * 0.3f)) { // 30% of the max
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}

		return result;
	}

	public ArrayList<PVector> hough(PImage edgeImg, int nLines, float[] tabCos, float[] tabSin){

		int rDim = (int)(((edgeImg.width+edgeImg.height)*2+1)/discretizationStepR);
		//dimensions of the accumulator
		////

		//accumulator with a 1pix margin around
		int[] accumulator = new int[(phiDim+2)*(rDim+2)];


		//Fill the accumulator: on edge point (white pixel of the edgeImg), store all possible (r,phi) pair discribing 
		//lines going through this point
		for(int y = 0; y<edgeImg.height;y++){
			for(int x = 0; x<edgeImg.width;x++){
				//are we on edge?
				if(brightness(edgeImg.pixels[y*edgeImg.width+x])!=0){

					//...determine all the lines (r,phi) passing through
					// pixel(x,y), convert(r, phi) to coordinates in accumulator, increment accumulator;

					for(int accPhi = 0; accPhi<phiDim; accPhi++){

						float r = (x)*tabCos[accPhi]+(y)*tabSin[accPhi];
						float idx = (accPhi+1)*(rDim+2);
						float accR = r/discretizationStepR + (rDim-1)*0.5f;
						idx = accR+(accPhi+1)*(rDim+2)+1;

						accumulator[Math.round(idx)] ++;

					}
				}
			}		
		}	




		PImage houghImg = createImage(rDim+2,phiDim+2, ALPHA);
		for(int i = 0; i<accumulator.length;i++){
			houghImg.pixels[i] = color(min(255, accumulator[i]));
		}
		houghImg.updatePixels();
		houghImg.save("resources/boards/accumulator.png");


		// Select the candidates
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
		int minVotes = 200;
		int neighbourhood = 10;

		for(int accR = 0; accR< rDim; accR++){
			for(int accPhi = 0; accPhi< phiDim; accPhi++){

				//compute current index in the accumulator
				int idx = (accPhi+1) *(rDim +2) + accR +1;

				if(accumulator[idx] > minVotes){
					boolean bestCandidate = true;

					//iterate over the neighbourhood
					for(int dPhi =- neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++){
						//check we are not outside the image

						if(accPhi + dPhi <0 || accPhi+dPhi >= phiDim) continue;
						for(int dR =- neighbourhood/2 ; dR<neighbourhood/2 +1; dR++){
							if(accR+dR<0 || accR+dR>= rDim) continue;

							int neighbourIdx = (accPhi + dPhi +1) * (rDim+2) + accR + dR +1;

							if(accumulator[idx]< accumulator[neighbourIdx]){
								//the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if(!bestCandidate) break;
					}
					if(bestCandidate){
						//the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}
		}
		//Cartesian equation of a line : y = ax+b
		// in polar : y = (-cos(phi)/sin(phi))x+(r/sin(phi)
		// => y = 0 : x = r/cos(phi)
		// => x = 0 : y = r/sin(phi)

		//plot
		Collections.sort(bestCandidates,new HoughComparator(accumulator));
	//	ArrayList<PVector> accVectors = new ArrayList<PVector>();
		ArrayList<PVector> lines = new ArrayList<PVector>();
		
		for(int i = 0; i<nLines;i++){
			if(i<bestCandidates.size()){

				//compute the intersection of this  line with the 4 borders of the image
				int idx = bestCandidates.get(i);
				int accPhi = (int)(idx/(rDim+2))-1;
				int accR = idx - (accPhi+1)*(rDim+2)-1;
				float r = (accR - (rDim-1)*0.5f)*discretizationStepR;	 					
				float phi = accPhi*discretizationStepPhi;

				//accVectors.add(new PVector(r,accPhi));
				lines.add(new PVector(r,phi));

				int x0 = 0;
				int y0 = (int)(r/tabSin[accPhi]);
				int x1 = (int)(r/tabCos[accPhi]);
				int y1 = 0;
				int x2 = edgeImg.width;
				int y2 = (int)(-tabCos[accPhi]/tabSin[accPhi] * x2+r/tabSin[accPhi]);
				int y3 = edgeImg.height;
				int x3 = (int)(-(y3-r/tabSin[accPhi])*(tabSin[accPhi]/tabCos[accPhi]));

				if(plotLines){
				//Finally, plot the lines
				stroke(204,102,0);
				if(y0>0){
					if(x1>0){
						line(x0,y0,x1,y1);
					}else if(y2>0) {
						line(x0,y0,x2,y2);
					}else {
						line(x0,y0,x3,y3);
					}
				}else{
					if(x1>0){
						if(y2>0){
							line(x1,y1,x2,y2);
						}else {
							line(x1,y1,x3,y3);
						}
					}else{
						line(x2,y2,x3,y3);
					}
					}
				}
			}
		}
				
		QuadGraph quadgraph = new QuadGraph();
		quadgraph.build(lines, edgeImg.width, edgeImg.height);
		List<int[]> quads = quadgraph.findCycles();
		ArrayList<PVector> selectedVertices = new ArrayList<PVector>();
		
		//plot the quads 
		for(int[] quad : quads){
			PVector l1= lines.get(quad[0]);
			PVector l2= lines.get(quad[1]);
			PVector l3= lines.get(quad[2]);
			PVector l4= lines.get(quad[3]);
			
			//Convert for use of intersection
			l1 = new PVector(l1.x,l1.y/discretizationStepPhi);
			l2 = new PVector(l2.x,l2.y/discretizationStepPhi);
			l3 = new PVector(l3.x,l3.y/discretizationStepPhi);
			l4 = new PVector(l4.x,l4.y/discretizationStepPhi);
			
			PVector c12 = intersection(l1,l2,tabCos,tabSin);
			PVector c23 = intersection(l2,l3,tabCos,tabSin);
			PVector c34 = intersection(l3,l4,tabCos,tabSin);
			PVector c41 = intersection(l4,l1,tabCos,tabSin);
			//Choose a random semi-transparent color
			Random random = new Random();
			fill(color(min(255,random.nextInt(300)),min(255,random.nextInt(300)),min(255,random.nextInt(255)),50));
			
			PVector c1 = new PVector(c12.x,c12.y);
			PVector c2 = new PVector(c23.x,c23.y);
			PVector c3 = new PVector(c34.x,c34.y);
			PVector c4 = new PVector(c41.x,c41.y);
			
			if(QuadGraph.isConvex(c1, c2, c3, c4)&&QuadGraph.validArea(c1, c2, c3, c4, 1000000 , 0) && QuadGraph.nonFlatQuad(c1, c2, c3, c4)){
				selectedVertices.clear();
				quad(c12.x,c12.y,c23.x,c23.y,c34.x,c34.y,c41.x,c41.y);
				selectedVertices.add(c1);
				selectedVertices.add(c2);
				selectedVertices.add(c3);
				selectedVertices.add(c4);
			}
			//if none is selected, choose the last graph
			if(selectedVertices.isEmpty()){
				selectedVertices.add(c1);
				selectedVertices.add(c2);
				selectedVertices.add(c3);
				selectedVertices.add(c4);
			}
		}
		//and plot intersections
				//ArrayList<PVector> intersections = getIntersections(accVectors,tabCos,tabSin);
				
				for(int i = 0; i<selectedVertices.size();i++){
					PVector v = selectedVertices.get(i);
					fill(255,128,0);
					ellipse(v.x,v.y,10,10);
				}
		
		return selectedVertices;
	}

	/*private ArrayList<PVector> getIntersections(List<PVector> lines, float[] tabCos, float[] tabSin){
		ArrayList<PVector> intersections = new ArrayList<PVector>();

		for(int i = 0; i<lines.size() -1; i++){
			PVector line1 = lines.get(i);

			for(int j = i+1;j<lines.size();j++){
				PVector line2 = lines.get(j);

				float d = tabCos[(int)line2.y]*tabSin[(int)line1.y]- tabCos[(int)line1.y]*tabSin[(int)line2.y];
				float x = (line2.x*tabSin[(int)line1.y]-line1.x*tabSin[(int)line2.y])/d;
				float y = (-line2.x*tabCos[(int)line1.y]+line1.x*tabCos[(int)line2.y])/d;

				intersections.add(new PVector(x,y));
			}
		}

		return intersections;
	}
*/
	private PVector intersection(PVector line1, PVector line2,float[]tabCos, float[] tabSin){
		float d = tabCos[(int)line2.y]*tabSin[(int)line1.y]- tabCos[(int)line1.y]*tabSin[(int)line2.y];
		float x = (line2.x*tabSin[(int)line1.y]-line1.x*tabSin[(int)line2.y])/d;
		float y = (-line2.x*tabCos[(int)line1.y]+line1.x*tabCos[(int)line2.y])/d;
		return new PVector(x,y);
	}
	
	public void test() {
		long start, stop;

		// Gaussian
		// Gaussian sequential
		start = System.currentTimeMillis();
		gaussianBlur(image);
		stop = System.currentTimeMillis();

		System.out.println("Sequential gaussian took " + (stop-start) + "ms");

		// HSB Filter
		start = System.currentTimeMillis();
		hsbFilter(image);
		stop = System.currentTimeMillis();

		System.out.println("Sequential HSB took " + (stop-start) + "ms");

		// Sobel
		// Sobel sequential
		start = System.currentTimeMillis();
		sobel(image);
		stop = System.currentTimeMillis();

		System.out.println("Sequential sobel took " + (stop-start) + "ms");

		// ALL
		// ALL sequential
		start = System.currentTimeMillis();
		applyAll(image);

		stop = System.currentTimeMillis();

		System.out.println("Sequential all took " + (stop-start) + "ms");

		// Draw
		sobel = applyAll(image);
		image(sobel, 0, 0);

	}
}
