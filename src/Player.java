import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Player {
     ArrayList<ArrayList<Rectangle>> body = new ArrayList<>();
     HashMap<Integer, BufferedImage> faces;
     HashMap<Integer, BufferedImage> bodies;
     int r, c;
     int dir = 0, leg = 1;
     int dc  = -12904;
     int dc2 = -1590900;
     double vely;
     int runspeed;
     public Player(BufferedImage f) throws IOException{
          //Makes faces for a dir
          faces = new HashMap<>();
          faces.put( 0, f.getSubimage( 0, 0, f.getWidth(), f.getHeight()/2));
          faces.put( 1, f.getSubimage( 0, f.getHeight()/2, f.getWidth(), f.getHeight()/2));
          faces.put(-1, Main.reverse(f.getSubimage( 0, f.getHeight()/2, f.getWidth(), f.getHeight()/2)));

          //Makes bodies for a dir
          bodies = new HashMap<>();
          bodies.put( 0, match(ImageIO.read(new File("res/body/body.png")), faces.get(0)));
          bodies.put( 1, match(ImageIO.read(new File("res/body/bodyright.png")), faces.get(0)));
          bodies.put( 2, match(ImageIO.read(new File("res/body/bodyleft.png")), faces.get(0)));
          bodies.put(-1, Main.reverse(match(ImageIO.read(new File("res/body/bodyright.png")), faces.get(0))));
          bodies.put(-2, Main.reverse(match(ImageIO.read(new File("res/body/bodyleft.png")), faces.get(0))));

          //set run speed
		runspeed = 3;
     }
     public Rectangle getBounds(){
		return new Rectangle(c + faces(0).getWidth()/2 - bodies.get(0).getWidth()/2, r, bodies.get(0).getWidth(), faces(0).getHeight() - 2 + bodies.get(0).getHeight());
	}
     public BufferedImage match(BufferedImage body, BufferedImage face){
          int tf = face.getRGB( face.getWidth() / 2, face.getHeight()-1);
          int tf2 = face .getRGB( face.getWidth() / 2, face.getHeight() / 2);
          for(int y = 0; y < body.getHeight(); y++)
               for(int x = 0; x < body.getWidth(); x++){
                    if(body.getRGB(x, y) == dc)
                         body.setRGB(x, y, tf);
                    else if ( body.getRGB(x, y) == dc2)
                         body.setRGB(x, y, tf2);
               }
          return body;
     }
     public void move(int dir){
     	if(dir > 0)
     		move(runspeed, 0);
     	else if(dir < 0)
     		move(-runspeed, 0);
	}
     public void move(int dirx, int diry){
          r+=diry;
          c+=dirx;
          if(c < 0)
          	c = 0;
		change();
     }
     public void change(){
          leg++;
          leg %= 2;
     }
     public BufferedImage faces(int i){
          return faces.get(i);
     }
     public HashMap<Integer, BufferedImage> faces(){
          return faces;
     }
     public void setRunning(int running) {
          dir = running;
     }
     public void draw(Graphics g){
          BufferedImage face = faces.get(dir);
          BufferedImage body = bodies.get((int) (Math.signum(dir) * (Math.abs(dir) + leg)));
          g.drawImage(body, (c + face.getWidth()/2 - body .getWidth() / 2) * Main.PIXEL,  (r + face.getHeight() - 2) * Main.PIXEL, body.getWidth() * Main.PIXEL, body.getHeight() * Main.PIXEL, null);
          g.drawImage(face, c * Main.PIXEL, r * Main.PIXEL, face.getWidth() * Main.PIXEL, face.getHeight() * Main.PIXEL, null);
     }
	public void draw(Graphics g, int xoff){
		BufferedImage face = faces.get(dir);
		BufferedImage body = bodies.get((int) (Math.signum(dir) * (Math.abs(dir) + leg)));
		g.drawImage(body, (c - xoff + face.getWidth()/2 - body .getWidth() / 2) * Main.PIXEL,  (r + face.getHeight() - 2) * Main.PIXEL, body.getWidth() * Main.PIXEL, body.getHeight() * Main.PIXEL, null);
		g.drawImage(face, (c - xoff) * Main.PIXEL, r * Main.PIXEL, face.getWidth() * Main.PIXEL, face.getHeight() * Main.PIXEL, null);
	}
	public void changeY(int dy){
     	this.c += dy;
	}
	public void changeY(){
		this.r += vely;
	}
	public void setVely(double vely) {
		this.vely = vely;
	}
	public int getY(){
     	return r;
	}
	public void setY(int y){
     	r = y;
	}
	public int getX(){
     	return c;
	}
	public double getVely(){
		return vely;
	}
	public void changeVely(double vely) {
		this.vely += vely;
	}
	public int getBotCornerY(){
     	return bodies.get(0).getHeight() + r + faces(0).getHeight() - 2;
	}
}
