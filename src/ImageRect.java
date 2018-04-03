import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageRect  implements Comparable<ImageRect>{
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
	public void draw(Graphics g, int xoff){
		g.drawImage(img, (x - xoff) * Main.PIXEL, y * Main.PIXEL, img.getWidth() * Main.PIXEL, img.getHeight() * Main.PIXEL, null);
	}
	public boolean inside(int x, int range, int y){
		boolean f = false;
		for(int i = x; i < x + range; i++)
			if(bounds.contains(i, y))
				f = true;
		return f;
	}
	public boolean inside(int x, int y){
		return bounds.contains(x, y);
	}
	public boolean insidez(int x, int y){
		return bounds.contains(x, y) && y != bounds.y;
	}
	public boolean insidez(int x, int range, int y){
		boolean f = false;
		for(int i = x; i < x + range; i++)
			if(bounds.contains(i, y) && y != bounds.y)
				f = true;
		return f;
	}
	public int compareTo(ImageRect r){
		if(r.x == this.x)
			return this.y - r.y;
		return this.x - r.x;
	}
	public int getX(){
		return x;
	}
	public void setImg(BufferedImage img){
		this.img = img;
	}
}
