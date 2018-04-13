import java.awt.*;
import java.awt.image.BufferedImage;

public class HoverImage extends ImageRect{
	BufferedImage top, bottom;
	boolean onTop = false;
	public HoverImage(int x, int y, BufferedImage img){
		super(x, y, img.getWidth(), img.getHeight() / 2);
		top = img.getSubimage(0, 0, img.getWidth(), img.getHeight() / 2);
		bottom = img.getSubimage(0, img.getHeight() / 2, img.getWidth(), img.getHeight() / 2);
	}
	public void drawOff(Graphics g, int x, int y){
		if(onTop)
			g.drawImage(top, (x + this.x) * PIXEL, (y + this.y) * PIXEL, top.getWidth() * PIXEL, top.getHeight() * PIXEL, null);
		else
			g.drawImage(bottom, (x + this.x) * PIXEL, (y + this.y) * PIXEL, bottom.getWidth() * PIXEL, bottom.getHeight() * PIXEL, null);
		for(ImageRect iR : children)
			iR.drawOff(g, x + this.x, y + this.y);
	}
	public void change(int x, int y){
		if(bounds.contains(x, y)) {
			onTop = true;
		}
		else {
			onTop = false;
		}
	}
}
