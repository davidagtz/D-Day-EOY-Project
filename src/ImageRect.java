import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageRect  implements Comparable<ImageRect>{
	BufferedImage img;
	String text, id;
	int x, y, h;
	protected boolean isBlack = false;
	static final int BLACK = 1;
	static final int WHITE = 0;
	final int PIXEL = Main.PIXEL;
	Rectangle bounds;
	ArrayList<ImageRect> children = new ArrayList<>();

	// Constructors
	public ImageRect(int x, int y, int w, int h){
		this.x = x;
		this.y = y;
		bounds = new Rectangle(x, y, w, h);
	}
	public ImageRect(int x, int y, BufferedImage img){
		this.img = img;
		this.x = x;
		this.y = y;
		bounds = new Rectangle(x, y, img.getWidth(), img.getHeight());
	}

	// Get and Set
	public int getWidth(){
		return bounds.width;
	}
	public int getHeight(){
		return bounds.height;
	}
	public Rectangle getBounds(){
		return bounds;
	}
	public BufferedImage getImg() {
		return img;
	}
	public String getId() {
		return id;
	}
	public ImageRect setId(String str){
		id = str;
		return this;
	}
	public ImageRect setPoint(int x, int y){
		setX(x);
		setY(y);
		return this;
	}
	public ArrayList<ImageRect> getChildren(){
		return children;
	}
	public ImageRect setText(String t, int i){
		text = t;
		h = i;
		return this;
	}
	public ImageRect setText(String t, int i, int color){
		text = t;
		h = i;
		if(color == BLACK)
			isBlack = true;
		else
			isBlack = false;
		return this;
	}
	public int getY(){
		return y;
	}
	public ImageRect setY(int y){
		this.y = y;
		bounds.y = y;
		return this;
	}
	public ImageRect setX(int x){
		this.x = x;
		bounds.x = x;
		return this;
	}
	public int getX(){
		return x;
	}
	public void setImg(BufferedImage img){
		this.img = img;
	}

	// Children Methods
	public ImageRect addChild(ImageRect iR){
		children.add(iR);
		return this;
	}
	public ImageRect addChild(int i, ImageRect iR){
		children.add(i, iR);
		return this;
	}
	public void remove(int x, int y){
		for(int i = children.size() - 1 ; i >= 0; i-- ){
			if(children.get(i).contains(x, y))
				children.remove(i);
		}
	}
	public void remove(int x, int y, String id){
		for(int i = children.size() - 1 ; i >= 0; i-- ){
			if(children.get(i).contains(x, y) && children.get(i).getId() != null && children.get(i).getId().matches(id))
				children.remove(i);
		}
	}
	public ArrayList<ImageRect> getById(String id){
		ArrayList<ImageRect> newOne = new ArrayList<>();
		for(int i = 0; i < children.size(); i++){
			if(getChildren().get(i).getId().matches(id)){
				newOne.add(getChildren().get(i));
			}
		}
		return newOne;
	}

	// Draw
	public void draw(Graphics g){
		drawOff(g, 0, 0);
	}
	protected void drawOff(Graphics g, int x, int y){
		if(img != null)
			g.drawImage(img, (x + this.x) * PIXEL, (y + this.y) * PIXEL, img.getWidth() * PIXEL, img.getHeight() * PIXEL, null);
		if(text != null) {
			if(isBlack)
				Main.drawString(g, x + this.x, y + this.y, h, text);
			else
				Main.drawStringW(g, x + this.x, y + this.y, h, text);
		}
		for(ImageRect iR : children){
			if(iR != null)
				iR.drawOff(g, x + this.x, y + this.y);
		}
	}
	public void draw(Graphics g, int xoff){
		drawOff(g, -xoff, 0);
	}

	// Bounds Methods
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
	public boolean contains(int x, int y){
		if(bounds.contains(x, y)){
			return true;
		}
		for(ImageRect iRc : getChildren()){
			if(iRc.contains(x + this.x, y + this.y))
				return true;
		}
		return false;
	}

	// Mouse Action Events
	public void clickAction(int x, int y){}
	public void hover(int x, int y){
		if(this instanceof HoverImage){
			((HoverImage)this).change(x, y);
		}
		for(ImageRect iRc : getChildren()){
			iRc.hover(x - this.x, y - this.y);
		}
	}
	public void click(int x, int y){
		if(bounds.contains(x, y)){
			clickAction(x - bounds.x, y - bounds.y);
		}
		for(int i = 0; i < getChildren().size(); i++){
			getChildren().get(i).click(x - this.x, y - this.y);
		}
	}

	// General Object Methods
	public int compareTo(ImageRect r){
		if(r.x == this.x)
			return this.y - r.y;
		return this.x - r.x;
	}
	public String toString() {
		return x + " " + y;
	}
}
