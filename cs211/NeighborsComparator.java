package cs211.imageprocessing;

import java.util.Comparator;

import processing.core.PImage;
import processing.core.PVector;

public class NeighborsComparator implements Comparator<PVector> {
	private PImage sobel;
	private int radius;
	
	public NeighborsComparator(PImage sobel,int radius){
		this.sobel = sobel;
		this.radius = radius;
	}

	@Override
	public int compare(PVector o1, PVector o2) {
		if((countWhite(o1)> countWhite(o2))|| (countWhite(o1) == countWhite(o2) && o1.x<o2.x)) return -1;
		return 1;
	}
	
	public int countWhite(PVector v){
		int count = 0;
		int lastUsedX = 0;
		int lastUsedY = 0;
		for(int i = 0; i<2*radius;i++){
			for(int j = 0; j<2*radius;j++){
				int valX = (int)v.x-radius+i;
				int valY = (int)v.y-radius+j;
				if(valX<0 || valX>sobel.width) valX = lastUsedX;
				if(valY<0 || valY>sobel.height) valY = lastUsedY;
				
				if(sobel.get(valX,valY)>0) count++;
				lastUsedX = valX;
				lastUsedY = valY;			
			}
		}
		return count;
	}
}
