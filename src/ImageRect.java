import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageRect {
	BufferedImage img;
	int x, y;
	public ImageRect(int x, int y, BufferedImage img){
		this.img = img;
		this.x = x;
		this.y = y;
	}
	public void draw(Graphics g){
		g.drawImage(img, x * Main.PIXEL, y * Main.PIXEL, img.getWidth() * Main.PIXEL, img.getHeight() * Main.PIXEL, null);
	}
}
