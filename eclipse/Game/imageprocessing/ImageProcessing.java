package imageprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;


public class ImageProcessing extends PApplet{
	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = 1634966782356685343L;

	private Random random = new Random();

	private PImage camera, sobel, image, accuImg,hsb;
	private Capture cam;
	private ArrayList<PVector> returnedCorners;
	
	// Hough Values
	float discretizationStepPhi = 0.006f;
	float discretizationStepR = 2.5f;
	private int phiDim = (int)(Math.PI/discretizationStepPhi);
	private float[] tabSin = new float[phiDim];
	private float[] tabCos = new float[phiDim];

	/**
	 * Some graphic init
	 */
	private int accuWidth = 842;
	private boolean initialized = false;
	//	private final int shiftUnderNormal = 0;
	//	private final int shiftOnAccu = 100;
	private PImage backWhite = new PImage(200,200);
	private int accuHeight = 525;

	// The following is hard code which can't be changed during execution (yet)
	/**
	 * Set to true if you want to use the camera feed,
	 * false for static image.
	 */
	private final boolean useCamera = true;

	/**
	 * Which board to use when drawing the static image (must be between 1 and 4!).
	 */
	private final int board = 1;

	/**
	 * The scaling factor
	 */
	private final float scaling = 0.75f;

	/**
	 * Draw options
	 */
	private boolean plotLines = false;
	private boolean showQuads = true;

	public PVector boardRotations = new PVector(0,1,0);
	
	@Override
	public void setup() {
		tabInitialization();
		if(!useCamera) {
			image = loadImage("resources/boards/board" + board + ".jpg");
			image.resize((int)((image.width)*scaling), (int)((image.height)*scaling));
			size(640,480+accuHeight);
			//size(image.width+(accuWidth/2-shiftUnderNormal)+(image.width)-shiftOnAccu, (int)((image.height)));
			//bar1 = new HScrollbar(this, width-width/4-20, image.height+10, width/4, 20);
			//bar2 = new HScrollbar(this, width-width/4-20,image.height+40, width/4, 20);
		} else {
			camStart();
			if(cam.available()==true){
				cam.read();
			}
			backWhite = whiteImage();
			image = cam.get();
			size(640,480+accuHeight);

		}
		boardRotations = new PVector(0,0,0);
		returnedCorners = new ArrayList<PVector>();
		accuImg = new PImage(200,200);
		
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
		hsb = hsbFilter(camera);
		if(sobel.height>0 && sobel.width>0) initialized = true;
		else initialized = false;
		if(!initialized){
			sobel = new PImage(200,200);
			hsb = new PImage(200,200);
		}else{
			//sobel.resize(sobel.width/2,sobel.height/2);
			hsb.resize(sobel.width, sobel.height);
			TwoDThreeD dd = new TwoDThreeD(camera.width,camera.height);
			if(returnedCorners.size()!=0){
				PVector rotations = dd.get3DRotations(sortCorners(returnedCorners));
				boardRotations.set(rotations);
			}
		}
		BlobDetection blobD;
		if(returnedCorners.size()>=4){
			blobD = new BlobDetection(this,returnedCorners.get(0),returnedCorners.get(1),returnedCorners.get(2),returnedCorners.get(3));
		}else{
			blobD = new BlobDetection(this,new PVector(0,0),new PVector(0,0),new PVector(0,0),new PVector(0,0));
		}
		backWhite.resize(sobel.width+2, sobel.height+2);
		image(camera, 0, 0);
		image(accuImg,0, camera.height);
		image(backWhite,0,camera.height);
		image(sobel, 0, camera.height);
		hsb.resize(200, 200);
		image(hsb,sobel.width,0);
		returnedCorners = hough(sobel, 6, tabCos, tabSin);
		
		image(hsb,0,0);

		hsb = hsbFilter(camera);
		//image(BlobD.findConnectedComponents(hsb),0,0);

	}

	private void drawPic(){
		sobel = applyAll(image);
		if(initialized){
			accuImg.resize(accuWidth, image.height);
			image(accuImg, 0, image.height);
		}
		image(image, 0, 0);
		ArrayList<PVector> returnedCorners = hough(sobel, 100, tabCos, tabSin);
		sobel.resize(sobel.width/2, sobel.height/2);
		backWhite = whiteImage();
		backWhite.resize(sobel.width+6, sobel.height+6);
		image(backWhite,0,image.height);
		image(sobel, 3, image.height+3);
		accuWidth = accuImg.width;
		initialized = true;
		TwoDThreeD dd = new TwoDThreeD(image.width,image.height);
		if(returnedCorners.size()!=0){
			PVector rotations = dd.get3DRotations(sortCorners(returnedCorners));
			//println(rad2Deg(rotations));
			boardRotations.set(rotations);
		}
		/*
		bar1.display();
		bar2.display();
		bar1.update();
		bar2.update();
		 */
	}

	public void camStart(){
		String[] cameras = Capture.list();
		
		if(cameras.length == 0){
			println("There are no camera available for capture.");
			exit();
		} else {
			int selected = -1;
			for(int i=0; i<cameras.length; i++) {
				String[] params = cameras[i].split(",");
				String name = params[0].substring(params[0].indexOf('=')+1);
				String size = params[1].substring(params[1].indexOf('=')+1);
				String fps  = params[2].substring(params[2].indexOf('=')+1);
				
				if(size.equals("640x480") && fps.equals("30")) {
					selected = i;
				}
			}
			
			if(selected != -1) {
				cam = new Capture(this, cameras[selected]);
				println("Camera " + selected + " selected: " + cameras[selected]);
			} else {
				cam = new Capture(this, cameras[0]);
				println("No optimal camera found, using first camera: " + cameras[0]);
			}

			cam.start();
			if(cam.available()==true){
				cam.read();
			}
			image = cam.get();
		}
	}

	public void tabInitialization(){

		//DONE OPTIMISATION (step 4 week 10) -> A Placer hors de la fonction, par exemple au setup, afin d'éviter de devoir recalculer
		tabSin = new float[phiDim];
		tabCos = new float[phiDim];

		float ang = 0;
		float inverseR = 1.f/discretizationStepR;

		for(int accPhi = 0; accPhi <phiDim ; ang+=discretizationStepPhi, accPhi ++){
			tabSin[accPhi] = (float)(Math.sin(ang)*inverseR);
			tabCos[accPhi] = (float)(Math.cos(ang)*inverseR);
		}
	}



	public PImage applyAll(PImage img) {
		PImage inter1 = hsbFilter(img);
		PImage inter2 = gaussianBlur(inter1);
		PImage inter3 = intensityFilter(inter2);
		PImage inter4 = sobel(inter3);
		return inter4;
	}



	public PImage gaussianBlur(PImage img) {
		final float[][] gaussianKernel = new float[][] { {9, 12, 9}, {12, 15, 12}, {9, 12, 9} };
		final float sum = 99f; // sum of the kernel

		final int border = 2;
		final int width = img.width;
		final int height = img.height;
		final int totPixels = width*height;

		PImage result = createImage(width, height, RGB);

		for(int i=0; i<totPixels; i++) {
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

			result.pixels[i] = color(r/sum, g/sum, b/sum);
		}

		return result;
	}


	public PImage hsbFilter(PImage img) {
		float hueMin =  80f; // hue minimum
		float hueMax = 140f; // hue maximum

		float bMin =  20f;  // brightness minimum
		float bMax = 240f;  // brightness maximum

		float sMin = 60f;  // saturation minimum

		final int width = img.width;
		final int height = img.height;

		final int totPixels = width*height;

		PImage result = createImage(width, height, RGB);

		for(int i=0; i<totPixels; i++) {
			int c = img.pixels[i];

			float h = hue(c);
			float s = saturation(c);
			float b = brightness(c);

			if(h >= hueMin && h < hueMax && b >= bMin && b < bMax && s >= sMin) {
				result.pixels[i] = color(2*s); // TODO: review this
				// does seem to work pretty well with camera feed
			} else {
				result.pixels[i] = color(0);
			}
		}

		return result;
	}

	public PImage intensityFilter(PImage img) {
		float threshold = 230;

		final int width = img.width;
		final int height = img.height;
		final int totPixels = img.width*img.height;

		PImage result = createImage(width, height, RGB);

		for(int i=0; i<totPixels; i++) {
			int c = img.pixels[i];
			float b = brightness(c);

			if(b > threshold) {
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
		final int totPixels = width*height;
		final int border = 2;

		float max = 0f;
		float[] buffer = new float[width * height];

		// Double convolution into buffer
		for(int p=0; p<totPixels; p++) {

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
		for(int i=0; i<totPixels; i++) {
			if (buffer[i] > (max * 0.3f)) { // 30% of the max
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}

		return result;
	}

	public ArrayList<PVector> hough(PImage edgeImg, int nLines, float[] tabCos, float[] tabSin){

		int rDim = (int)(((edgeImg.width+edgeImg.height)*2+1)/discretizationStepR);
		// Dimensions of the accumulator
		// (accumulator with a 1pix margin around)
		int[] accumulator = new int[(phiDim+2)*(rDim+2)];


		// Fill the accumulator: on edge point (white pixel of the edgeImg), store all 
		// possible (r,phi) pair describing lines going through this point
		for(int y = 0; y<edgeImg.height; y++){
			for(int x = 0; x<edgeImg.width; x++){
				// Are we on an edge?
				if(brightness(edgeImg.pixels[y*edgeImg.width+x])!=0){

					// Determine all the lines (r,phi) passing through pixel (x,y), convert (r, phi) 
					// to coordinates in accumulator, increment accumulator:
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
		
		accuImg = createImage(rDim+2, phiDim+2, ALPHA);
		for(int i=0; i<accumulator.length; i++) {
			accuImg.pixels[i] = color(min(255, accumulator[i]));
		}
		accuImg.updatePixels();


		// Select the candidates
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
		final int minVotes = 200;
		final int neighbourhood = 10;

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
						for(int dR = -(neighbourhood/2); dR<(neighbourhood/2)+1; dR++){
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
		// Cartesian equation of a line : y = ax+b
		// in polar : y = (-cos(phi)/sin(phi))x+(r/sin(phi)
		// => y = 0 : x = r/cos(phi)
		// => x = 0 : y = r/sin(phi)

		// Plot
		Collections.sort(bestCandidates,new HoughComparator(accumulator));
		ArrayList<PVector> lines = new ArrayList<PVector>();

		for(int i=0; i<nLines;i++){
			if(i<bestCandidates.size()){

				// Compute the intersection of this  line with the 4 borders of the image
				int idx = bestCandidates.get(i);
				int accPhi = (int)(idx/(rDim+2)) -1;
				int accR = idx - (accPhi+1) * (rDim+2)-1;
				float r = (accR - (rDim-1)*0.5f) * discretizationStepR;	 					
				float phi = accPhi*discretizationStepPhi;

//				accVectors.add(new PVector(r,accPhi));
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
					if(y0 > 0){
						if(x1 > 0){
							line(x0, y0, x1, y1);
						} else if(y2 > 0) {
							line(x0, y0, x2, y2);
						} else {
							line(x0, y0, x3, y3);
						}
					} else {
						if(x1 > 0){
							if(y2 > 0){
								line(x1, y1, x2, y2);
							} else {
								line(x1, y1, x3, y3);
							}
						} else {
							line(x2, y2, x3, y3);
						}
					}
				}
			}
		}

		QuadGraph quadgraph = new QuadGraph();
		quadgraph.build(lines, edgeImg.width, edgeImg.height);
		List<int[]> quads = quadgraph.findCycles();
		ArrayList<PVector> selectedVertices = new ArrayList<PVector>();

		// Plot the quads 
		for(int[] quad : quads){
			PVector l1= lines.get(quad[0]);
			PVector l2= lines.get(quad[1]);
			PVector l3= lines.get(quad[2]);
			PVector l4= lines.get(quad[3]);

			// Convert for use of intersection
			l1 = new PVector(l1.x,l1.y/discretizationStepPhi);
			l2 = new PVector(l2.x,l2.y/discretizationStepPhi);
			l3 = new PVector(l3.x,l3.y/discretizationStepPhi);
			l4 = new PVector(l4.x,l4.y/discretizationStepPhi);

			PVector c12 = intersection(l1,l2,tabCos,tabSin);
			PVector c23 = intersection(l2,l3,tabCos,tabSin);
			PVector c34 = intersection(l3,l4,tabCos,tabSin);
			PVector c41 = intersection(l4,l1,tabCos,tabSin);
			// Choose a random semi-transparent color

			PVector c1 = new PVector(c12.x,c12.y);
			PVector c2 = new PVector(c23.x,c23.y);
			PVector c3 = new PVector(c34.x,c34.y);
			PVector c4 = new PVector(c41.x,c41.y);

			if(QuadGraph.isConvex(c1, c2, c3, c4)&&QuadGraph.validArea(c1, c2, c3, c4, 1000000 , 8000) && QuadGraph.nonFlatQuad(c1, c2, c3, c4)){
				// Draw options
				if(showQuads){
					/*
					if(board==4){
						fill(color(min(255,random.nextInt(300)),150,150,30));				
							}else
					 */
					if(useCamera){
						fill(color(35, 15, 170, 140));
					} else {
						fill(color(min(255,random.nextInt(300)), min(255,random.nextInt(300)), min(255,random.nextInt(255)),30));
					}
					quad(c1.x, c1.y, c2.x, c2.y, c3.x, c3.y, c4.x, c4.y);
				}
				//				
				selectedVertices.add(c1);
				selectedVertices.add(c2);
				selectedVertices.add(c3);
				selectedVertices.add(c4);
			}

		}
		ArrayList<PVector> corners2D = new ArrayList<PVector>();
		// Plot intersections
		int Lsize = Math.min(selectedVertices.size(), 4);
		
		for(int i = 0; i<Lsize; i++){
			PVector v = selectedVertices.get(i);
			corners2D.add(v);
			fill(255,128,0);
			ellipse(v.x,v.y,10,10);
		}
		return corners2D;
	}

	private PVector intersection(PVector line1, PVector line2,float[]tabCos, float[] tabSin){
		float d = tabCos[(int)line2.y]*tabSin[(int)line1.y]- tabCos[(int)line1.y]*tabSin[(int)line2.y];
		float x = (line2.x*tabSin[(int)line1.y]-line1.x*tabSin[(int)line2.y])/d;
		float y = (-line2.x*tabCos[(int)line1.y]+line1.x*tabCos[(int)line2.y])/d;
		return new PVector(x,y);
	}

	public static List<PVector> sortCorners(List<PVector> quad){
		//Sort corners so that they are ordered clockwise
		PVector a = quad.get(0);
		PVector b = quad.get(2);
		PVector center = new PVector((a.x+b.x)/2,(a.y+b.y)/2);

		Collections.sort(quad, new CWComparator(center));

		//TODO:
		//Re-order the corners so that the first one is the closest to the origin(0,0) of the image.
		//You can Use Collections.rotate to shift the corners inside the quad.size()
		PVector upperLeft = quad.get(0);
		for(int i = 0; i<4; i++){
			if(quad.get(i).x<=upperLeft.x && quad.get(i).y<upperLeft.y){
				upperLeft = quad.get(i);
			}
		}
		while(quad.indexOf(upperLeft)!=0){
			Collections.rotate(quad,0);
		}

		return quad;
	}
	
	public static PVector rad2Deg(PVector p){
		float radFactor = (float) (180/Math.PI);
		return new PVector(p.x*radFactor,p.y*radFactor,p.z*radFactor);
	}
	
	public PImage whiteImage(){
		PImage image = new PImage(200,200);
		for(int i = 0; i<200;i++){
			for(int j = 0; j<200;j++){
				image.set(i, j, color(255,255,255,255));
			}
		}
		return image;
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
