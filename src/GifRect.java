import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GifRect extends ImageRect {
	ArrayList<BufferedImage> frames;
	private int index = 0;
	long time;
	private long lastTime;
	private boolean first = false;
	public GifRect(int x, int y, ArrayList<BufferedImage> frames, long time){
		super(x, y, frames.get(0).getWidth(), frames.get(0).getHeight());
		this.frames = frames;
		this.time = time;
		lastTime = System.currentTimeMillis();
	}
	public GifRect(int x, int y, int w, int h, long time){
		super(x, y, w, h);
		this.frames = new ArrayList<>();
		this.time = time;
		lastTime = System.currentTimeMillis();
	}
	protected void drawOff(Graphics g, int x, int y){
		if(!first){
			first = true;
			lastTime = System.currentTimeMillis();
		}
		if(frames != null) {
			BufferedImage img = frames.get(index);
			g.drawImage(img, (x + this.x) * PIXEL, (y + this.y) * PIXEL, img.getWidth() * PIXEL, img.getHeight() * PIXEL, null);
			if(System.currentTimeMillis() - lastTime >= time) {
				index++;
				index %= frames.size();
				lastTime = System.currentTimeMillis();
			}
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
