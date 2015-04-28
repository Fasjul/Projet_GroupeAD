package imageproc;

import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet {
	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = 1634966782356685343L;
	
	private PImage image;
	private PImage result;

	@Override
	public void setup() {
		image = loadImage("resources/boards/board4.jpg");
		size(image.width, image.height);
	}

	@Override
	public void draw() {
		result = sobel(hsbFilter(gaussianBlur(image)));
		image(result, 0, 0);
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

	public PImage gaussianBlur(PImage img) {
		PImage res = createImage(img.width, img.height, RGB);

		float[][] gaussianKernel = new float[][] { {9, 12, 9}, {12, 15, 12}, {9, 12, 9} };

		for(int i=0; i<img.width*img.height; i++) {
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

			int s = 0;
			for(int j=0; j<gaussianKernel.length; j++) {
				for(int jj=0; jj<gaussianKernel[j].length; jj++) {
					s += gaussianKernel[j][jj];
				}
			}

			res.pixels[i] = color(r/s, g/s, b/s);
		}
		
		return res;
	}

	public PImage hsbFilter(PImage img) {
		float hueMin = 80f;
		float hueMax = 140f;
		
		float bMin = 20f;
		float bMax = 220;
		
		float sMin = 80f;

		PImage res = createImage(img.width, img.height, RGB);

		for(int i=0; i<img.width*img.height; i++) {
			float h = hue(img.pixels[i]);
			float b = brightness(img.pixels[i]);
			float s = saturation(img.pixels[i]);
			if(h > hueMin && h < hueMax && b > bMin && b < bMax && s > sMin) {
				res.pixels[i] = color(255);
			} else {
				res.pixels[i] = color(0);
			}
		}
		
		return res;
	}

	public PImage sobel(PImage img) {
		float[][] hKernel = {
				{ 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, -1, 0 }
			};
		float[][] vKernel = {
				{ 0, 0, 0 },
				{ 1, 0, -1 },
				{ 0, 0, 0 }
			};
		
		PImage res = createImage(img.width, img.height, ALPHA);
		
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			res.pixels[i] = color(0);
		}
		
		float max = 0;
		float[] buffer = new float[img.width * img.height];
		
		// Double convolution into buffer
		for(int x=2; x<img.width-2; x++) {
			for(int y=2; y<img.height-2; y++) {
				
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
				
				buffer[y*img.width + x] = val;
				
			}
		}
		
		// Create from buffer
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int)(max * 0.3f)) { // 30% of the max
					res.pixels[y * img.width + x] = color(255);
				} else {
					res.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return res;
	
	}
}
