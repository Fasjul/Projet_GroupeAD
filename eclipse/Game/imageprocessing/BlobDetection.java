package imageprocessing;
import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList; 
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class BlobDetection {
	PApplet applet;
	Polygon quad = new Polygon();
	
	public BlobDetection(PApplet applet, PVector c1, PVector c2, PVector c3, PVector c4){
		quad.addPoint((int) c1.x, (int) c1.y);
		quad.addPoint((int) c2.x, (int) c2.y);
		quad.addPoint((int) c3.x, (int) c3.y);
		quad.addPoint((int) c4.x, (int) c4.y);
		
	}
	
	public boolean isInQuad(int x, int y){
		return quad.contains(x,y);
	}
	
	public PImage findConnectedComponents(PImage input){
		
		//PImage input = hsbFilter(input,colorOfBlob);
		
		int[] labels = new int [input.width*input.height];
		List<TreeSet<Integer>> labelsEquivalences = new ArrayList<TreeSet<Integer>>();
		
		int currentLabel = 0;
		
		//TODO!!
		for(int i = 0; i<labels.length;i++){
			if(input.pixels[i]==255 ){
				//check labels around
				int y = (int) Math.floor(i/input.width);
				int x = i-y*input.width;
//				if(quad.contains(x,y)){
				PVector[] nghbrs = new PVector[4];
				nghbrs[0] = new PVector(x-1,y-1);
				nghbrs[1] = new PVector(x,y-1);
				nghbrs[2] = new PVector(x+1,y-1);
				nghbrs[3] = new PVector(x-1,y);
				
				int[] values = new int[4];
																					///Problème ici
				for(int j = 0; j<4;j++){
					PVector n = nghbrs[j];
					if(n.x<0 || n.y<0) values[j] = currentLabel;
					else{ values[j] = labels[(int)(n.x+n.y*input.width)];
					}
				}
				Arrays.sort(values);
				int minLabel = values[0];
				if(currentLabel<minLabel) labels[i] = currentLabel++;
				else{
					labels[i] = minLabel;
					for(int z = 0; z<4;z++){
						if(values[z]!=0);
						labelsEquivalences.get(values[z]).add(minLabel);
						}
//					}
				}
			}else{
				labels[i] = 0;
			}
		}
		TreeSet<Integer> zero = new TreeSet<Integer>();
		zero.add(0);
		labelsEquivalences.add(0, zero);
		
		//Second Pass : relabel the pixels by their equivalent class
		for(int i = 0;i<labels.length;i++){
			if(labels[i]>=Integer.MAX_VALUE){
				labels[i] = 0;
			}
			labels[i] = labelsEquivalences.get(labels[i]).first();
		}
		
		//TODO!!
		
		//Finally, output an image with each blob colored in one uniform color.p
		PImage image = new PImage(input.width,input.height);
		for(int i = 0; i<labels.length;i++){
				int y = (int) Math.floor(i/input.width);
				int x = i-y*input.width;
				image.set(x, y, 24);
		}
		image.updatePixels();
		return image;
		//TODO!
	}
}
