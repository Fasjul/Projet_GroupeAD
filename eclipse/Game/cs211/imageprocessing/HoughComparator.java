package cs211.imageprocessing;

import java.util.Comparator;

public class HoughComparator implements Comparator<Integer> {
	
	private int[] accumulator;
	
	public HoughComparator(int[] accumulator){
		this.accumulator = accumulator;
	}
	
	@Override
	public int compare(Integer i1, Integer i2) {
		if((accumulator[i1]> accumulator[i2])|| (accumulator[i1] == accumulator[i2] && i1<i2)) return -1;
		return 1;
	}

}
