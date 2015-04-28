package imageproc;
import processing.core.PImage;
import processing.core.PApplet;
import processing.video.Capture;	

public class HoughTransform extends PApplet{

	Capture cam;
	PImage img;
	public void setup(){
		size(640,480);
		String[] cameras = Capture.list();
		if(cameras.length == 0){
			println("There are no camera available for capture.");
			exit();
		}else{
			println("Available cameras :");
			for(int i =0 ; i<cameras.length;i++){
				println(i+". "+cameras[i]);
			}
			if(cameras.length>26){
			cam = new Capture(this,cameras[1]);
			println("Selected : "+cameras[1]);
			}else{
			cam = new Capture(this,cameras[0]);
			}
			cam.start();
			}
		}
	public void draw(){
		if(cam.available()==true){
			cam.read();
		}
		img = cam.get();
		image(img,0,0);
	}
}
