package objects;

import processing.core.PGraphics;

public class Box implements Drawable {
	public final int width;
	public final int height;
	public final int depth;
	
	public final PGraphics GAMEGFX;

	public Box(PGraphics gameGraphics, int width, int height, int depth) {
		this.GAMEGFX = gameGraphics;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	public void draw() {
		GAMEGFX.box(width, height, depth);
	}
}