package imageprocessing;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class BlobDetection {
	private final PApplet applet;
	
	private Polygon quad = new Polygon();

	public BlobDetection(PApplet applet, PVector c1, PVector c2, PVector c3, PVector c4){
		this.applet = applet;
		
		quad.addPoint((int) c1.x, (int) c1.y);
		quad.addPoint((int) c2.x, (int) c2.y);
		quad.addPoint((int) c3.x, (int) c3.y);
		quad.addPoint((int) c4.x, (int) c4.y);

	}

	public boolean isInQuad(int x, int y){
		return quad.contains(x,y);
	}

	public PImage findConnectedComponents(PImage input){

		int[] labels = new int [input.width*input.height];
		List<TreeSet<Integer>> labelsEquivalences = new ArrayList<TreeSet<Integer>>();

		int currentLabel = 1;
		
		// First traversal
		for(int p = 0; p<labels.length; p++){
			if(applet.brightness(input.pixels[p]) == 255){
				// Check labels around
				int y = (int)(p/input.width);
				int x = p % input.width;
				
				// Cell neighbors
				int[] nghbrs = new int[] { 
					x-1, y-1,
					x,   y-1,
					x+1, y-1,
					x-1, y
				};
				
				// Get 4 neighbors labels
				int[] values = new int[4];

				for(int i = 0; i<nghbrs.length; i+=2) {
					int cx = nghbrs[i];
					int cy = nghbrs[i+1];
					
					if(cx < 0 || cy < 0) {
						values[i >> 1] = 0;
					} else {
						values[i >> 1] = labels[cx + input.width*cy];
					}
				}
				
				// Get minimum label of neighbors
				int minLabel = Integer.MAX_VALUE;
				for(int i=0; i<values.length; i++) {
					if(values[i] != 0 && values[i] < minLabel) {
						minLabel = values[i];
					}
				}

				if(currentLabel < minLabel) {
					labels[p] = currentLabel++;
					labelsEquivalences.add(new TreeSet<Integer>());
				} else {
					labels[p] = minLabel;
					for(int z = 0; z<values.length; z++){
						if(values[z] != 0){
							labelsEquivalences.get(values[z]-1).add(minLabel);
						}
					}
				}
			} else {
				labels[p] = 0;
			}
		}
		TreeSet<Integer> zero = new TreeSet<Integer>();
		zero.add(0);
		labelsEquivalences.add(0, zero);

		//Second traversal : relabel the pixels by their equivalent class
		for(int i = 0; i<labels.length; i++){
			if(labels[i] >= Integer.MAX_VALUE){
				labels[i] = 0;
			}
			labels[i] = labelsEquivalences.get(labels[i]).first();
		}

		//TODO!!

		//Finally, output an image with each blob colored in one uniform color.p
		PImage image = new PImage(input.width, input.height);
		for(int i = 0; i<labels.length;i++){
			int y = i/input.width;
			int x = i % input.width;
			image.set(x, y, (int)0b10010010100100010010011);
		}
		image.updatePixels();
		return image;
		//TODO!
	}
}
