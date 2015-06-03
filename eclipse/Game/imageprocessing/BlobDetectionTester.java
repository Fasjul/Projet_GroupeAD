package imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class BlobDetectionTester extends PApplet {
	
	/**
	 * Generated serial ID.
	 */
	private static final long serialVersionUID = 2607116476385304269L;
	
	private PImage img;
	private BlobDetection blob;
	
	public void setup() {
		size(200, 200);
		
		blob = new BlobDetection(this, new PVector(0,0), new PVector(200,0), new PVector(200,200), new PVector(0,200));
		img = loadImage("./resources/blob-test.png");
	}
	
	public void draw() {
		image(blob.findConnectedComponents(img), 0, 0);
	}
	
}
