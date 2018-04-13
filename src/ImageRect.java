import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageRect  implements Comparable<ImageRect>{
	BufferedImage img;
	String text;
	int x, y, h;
	final int PIXEL = Main.PIXEL;
	Rectangle bounds;
	ArrayList<ImageRect> children = new ArrayList<>();
	public ImageRect(int x, int y, int w, int h){
		this.x = x;
		this.y = y;
		bounds = new Rectangle(x, y, w, h);
	}
	public int getWidth(){
		return bounds.width;
	}
	public void setX(int x){
		this.x = x;
		bounds.x = x;
	}
	public boolean addChild(ImageRect iR){
		return children.add(iR);
	}
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
		drawOff(g, 0, 0);
	}
	public ArrayList<ImageRect> getChildren(){
		return children;
	}
	protected void drawOff(Graphics g, int x, int y){
		if(img != null)
			g.drawImage(img, (x + this.x) * PIXEL, (y + this.y) * PIXEL, img.getWidth() * PIXEL, img.getHeight() * PIXEL, null);
		if(text != null)
			Main.drawStringW(g, x + this.x, y + this.y, h, text);
		for(ImageRect iR : children){
			iR.drawOff(g, x + this.x, y + this.y);
		}
	}
	public ImageRect setText(String t, int i){
		text = t;
		h = i;
		return this;
	}
	public void draw(Graphics g, int xoff){
		drawOff(g, -xoff, 0);
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
	public void clickAction(int x, int y){}
	public void setImg(BufferedImage img){
		this.img = img;
	}
	public void hover(int x, int y){
		if(this instanceof HoverImage){
			((HoverImage)this).change(x, y);
		}
		for(ImageRect iRc : getChildren()){
			iRc.hover(x, y);
		}
	}
	public void click(int x, int y){
		if(bounds.contains(x, y)){
			clickAction(x - bounds.x, y - bounds.y);
		}
		for(ImageRect iRc : getChildren()){
			iRc.click(x, y);
		}
	}
}
