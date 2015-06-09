package imageprocessing;

import java.awt.Polygon;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Allows detection of connected components (in terms of computer vision) on a surface
 * delimited by a Quad. These are named "blobs".
 */
public class BlobDetection {
	private final PApplet applet;
	private Polygon quad;

	public BlobDetection(PApplet applet, PVector c1, PVector c2, PVector c3, PVector c4){
		this.applet = applet;
		
		quad = new Polygon();
		quad.addPoint((int) c1.x, (int) c1.y);
		quad.addPoint((int) c2.x, (int) c2.y);
		quad.addPoint((int) c3.x, (int) c3.y);
		quad.addPoint((int) c4.x, (int) c4.y);

	}

	/**
	 * Returns true if coordinate is inside the plane given in the constructor.
	 * @param x abscissa of the point
	 * @param y ordinate of the point
	 * @return <b>true</b> if it is inside the plane, <b>false</b> otherwise.
	 */
	public boolean isInQuad(int x, int y){
		return quad.contains(x,y);
	}

	/**
	 * Finds all connected components in a image given.
	 * @param input PImage to check for blobs
	 * @return A list of coordinates of the different blob centers.
	 */
	public List<PVector> findConnectedComponents(PImage input){

		int[] labels = new int [input.width*input.height];
		List<TreeSet<Integer>> labelsEquivalences = new ArrayList<TreeSet<Integer>>();

		int currentLabel = 1;
		
		// First traversal
		for(int p = 0; p<labels.length; p++){
			if(applet.brightness(input.pixels[p]) == 255){
				// Check labels around
				int y = (int)(p/input.width);
				int x = p % input.width;
				
				if(!isInQuad(x, y))
					continue;

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
						values[i/2] = 0;
					} else {
						values[i/2] = labels[cx + input.width*cy];
					}
				}

				// Get minimum label of neighbors
				int minLabel = Integer.MAX_VALUE;
				boolean found = false;
				
				for(int i=0; i<values.length; i++) {
					if(values[i] != 0 && values[i] < minLabel) {
						minLabel = values[i];
						found = true;
					}
				}

				// Check if new label needed
				if(!found) {
					labels[p] = currentLabel++;
					labelsEquivalences.add(new TreeSet<Integer>());
				} else {
					labels[p] = minLabel;
					for(int z = 0; z<values.length; z++){
						if(values[z] != 0){
							labelsEquivalences.get(values[z]-1).add(minLabel);
							labelsEquivalences.get(minLabel-1).add(values[z]);
						}
					}
				}
			} else {
				labels[p] = 0;
			}
		}
		
		// Second traversal : relabel the pixels by their equivalent class
		for(int i = 0; i<labels.length; i++){
			final int l = labels[i];
			if(l != 0) {
				TreeSet<Integer> set = labelsEquivalences.get(l-1);
				int compVal = set.ceiling(0);
				labels[i] = compVal;
			}
		}
		
		PVector[] accu = new PVector[currentLabel];

		// Compute the average of equivalent labels
		for(int i = 0; i<labels.length; i++) {
			int y = i/input.width;
			int x = i % input.width;
			
			int l = labels[i];
			
			if(accu[l] == null) {
				accu[l] = new PVector(0, 0, 0);
			}
			
			accu[l].x += x;
			accu[l].y += y;
			accu[l].z ++;
		}
		
		// Create average list
		ArrayList<PVector> list = new ArrayList<>();
		for(int i = 1; i<currentLabel; i++) {
			if(accu[i] != null) {
				list.add(new PVector(accu[i].x/accu[i].z, accu[i].y/accu[i].z));
			}
		}
		
		return list;
		
	}
}
