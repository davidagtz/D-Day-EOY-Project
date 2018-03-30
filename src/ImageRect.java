import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageRect {
	BufferedImage img;
	int x, y;
	Rectangle bounds;
	public ImageRect(int x, int y, BufferedImage img){
		this.img = img;
		this.x = x;
		this.y = y;
		bounds = new Rectangle(x, y, img.getWidth(), img.getHeight());
	}
	public Rectangle getBounds(){
		return bounds;
	}
	public void draw(Graphics g){
		g.drawImage(img, x * Main.PIXEL, y * Main.PIXEL, img.getWidth() * Main.PIXEL, img.getHeight() * Main.PIXEL, null);
	}
	public boolean inside(int x, int y){
		return bounds.contains(x, y);
	}
	public boolean insidez(int x, int y){
		return bounds.contains(x, y) && x != bounds.x && x != bounds.x + bounds.width && y != bounds.y && y != bounds.height + bounds.y;
	}
}
