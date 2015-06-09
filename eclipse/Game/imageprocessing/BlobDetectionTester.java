package imageprocessing;

import java.util.List;

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
		
		image(img, 0, 0);
		
		List<PVector> ls = blob.findConnectedComponents(img);
		
		for(PVector p : ls) {
			drawCross(p.x, p.y);
			System.out.println(p);
		}
	}
	
	public void drawCross(float x, float y) {
		stroke(color(255, 0, 0));
		line(x-2, y, x+2, y);
		line(x, y-2, x, y+2);
	}
	
}
