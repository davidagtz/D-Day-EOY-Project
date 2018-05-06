import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GifRect extends ImageRect {
	ArrayList<BufferedImage> frames;
	int index = 0;
	long time;
	long lastTime;
	public GifRect(int x, int y, ArrayList<BufferedImage> frames, long time){
		super(x, y, null);
		this.frames = frames;
		this.time = time;
		lastTime = System.currentTimeMillis();
	}
	protected void drawOff(Graphics g, int x, int y){
		if(img != null) {
			g.drawImage(frames.get(index), (x + this.x) * PIXEL, (y + this.y) * PIXEL, img.getWidth() * PIXEL, img.getHeight() * PIXEL, null);
			if(System.currentTimeMillis() - lastTime >= time)
				index++;
		}
		if(text != null)
			Main.drawStringW(g, x + this.x, y + this.y, h, text);
		for(ImageRect iR : children){
			if(iR != null)
				iR.drawOff(g, x + this.x, y + this.y);
		}
	}
	public void addFrame(BufferedImage img){
		frames.add(img);
	}
}
