import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.util.*;

/*@davidagtz*/
public class Main extends JPanel implements ActionListener, KeyListener{
     public static long serialVersionUID = 0L;
     static JFrame frame;
     static final int WIDTH = 798, HEIGHT = 600, PIXEL = 6, PixHEIGHT = HEIGHT/PIXEL;
     static int xoff = 0;
     static final double gravity = .9;
     static HashMap<Character, BufferedImage> font = new HashMap<>();
     static Timer timer;
     static Player david, diego, jakob;
     static final Color pause = new Color(255, 0, 0, 127);
     static BufferedImage background, ground, topGround;
     static final Set<Integer> pressed = new HashSet<>();
     static ArrayList<ImageRect> stage = new ArrayList<>();
	static ArrayList<ImageRect> stagecut;
     public void paintComponent(Graphics g){
     	stagecut = get(stage, xoff, xoff + WIDTH / PIXEL);
     	int xofft = Math.min(david.c, diego.c);
     	xoff = xofft;
//     	xoff = Math.max(xoff, xofft);
          paintBackground(g, xoff);
          diego.draw(g, xoff);
//		g.setColor(Color.red);
//		g.drawRect(diego.getBounds().x* PIXEL, diego.getBounds().y* PIXEL, diego.getBounds().width* PIXEL, diego.getBounds().height * PIXEL);
//		g.drawLine(diego.getX() * PIXEL, diego.getBotCornerY() * PIXEL, diego.getX()* PIXEL, diego.getY()* PIXEL);
//		System.out.println(diego.getBounds());
          gravity(diego);
          david.draw(g, xoff);
          gravity(david);
          jakob.draw(g, xoff);
          gravity(jakob);
          for(ImageRect img : stagecut) {
			img.draw(g, xoff);
          }
          if(!timer.isRunning()){
          	g.setColor(pause);
          	g.fillRect(0, 0, WIDTH, HEIGHT);
		}
     }
	public ArrayList<ImageRect> get(ArrayList<ImageRect> list, int beg, int end){
     	ArrayList<ImageRect> newL = new ArrayList<>();
     	for(int i = 0; i < list.size(); i++){
     		if(list.get(i).getX() > end)
     			break;
     		if(list.get(i).getX() + list.get(i).getBounds().getWidth() >= beg)
     			newL.add(list.get(i));
		}
		return newL;
	}
     public boolean touching(Player p){
     	boolean inter = false;
		for(ImageRect img : stage) {
			Rectangle tr = (Rectangle) p.getBounds().clone();
			if(img.getBounds().intersects(tr)){
				p.setY(img.getBounds().y - p.getBounds().height);
				p.setVely(0);
				inter = true;
			}
			tr.y += p.getVely();
			if(img.getBounds().intersects(tr)){
				p.setY(img.getBounds().y - p.getBounds().height);
				p.setVely(0);
				inter = true;
			}
		}
		return inter;
	}
     public void gravity(Player p){
     	boolean inter = touching(p);
		if(inter) {
			return;
		}
		if(p.getY() > HEIGHT) {
			p.setY(PixHEIGHT-2);
			p.setVely(-1);
		}
		p.changeY();
		p.changeVely(gravity);
	}
	public boolean inside(int x, int y, ArrayList<ImageRect> s){
     	for(ImageRect img : s)
     		if(img.inside(x, y))
     			return true;
     	return false;
	}
	public void paintBackground(Graphics g){
		g.setColor(Color.CYAN);
		g.fillRect( 0, 0, WIDTH, HEIGHT);
	}
	public void paintBackground(Graphics g, int xoff){
		g.setColor(Color.CYAN);
		g.fillRect( 0, 0, WIDTH, HEIGHT);
	}
     public Main(String title){
          //get Background
          try{
//               background = ImageIO.read(new File("res/background.png"));
               //get ground
			ground = ImageIO.read(new File("res/ground.png"));
			topGround = copy(ground);

			//add blocks
			BufferedReader in = new BufferedReader(new FileReader("res/levels/first.level"));
			for(int i = 0; in.ready(); i+=0){
				StringTokenizer line = new StringTokenizer(in.readLine());
				int x = 0;
				int addy = 1;
				while (line.hasMoreTokens()){
					int amount = Integer.parseInt(line.nextToken());
					if(line.hasMoreTokens()) {
						String mod = line.nextToken();
						if (mod.matches("_\\+?")) {
							for (int j = 0; j < amount; j++) {
								stage.add(new ImageRect(x, i, copy(ground)));
								x += ground.getWidth();
							}
							if (mod.endsWith("+"))
								addy = Math.max(addy, ground.getHeight());
						}
						if(mod.matches("-")){
							x += amount;
						}
					} else {
						addy = amount;
					}
				}
				i += addy;
			}

			//sort ground by x
			Collections.sort(stage);

			//draw top layer
			for(int i = 0; i < topGround.getWidth(); i++)
				topGround.setRGB(i, 0, topGround.getRGB(0,0));
			if(stage.size() > 0)
				stage.get(0).setImg(copy(topGround));
			for(int i = 1; i < stage.size(); i++)
				if(stage.get(i - 1).getX() != stage.get(i).getX())
					stage.get(i).setImg(copy(topGround));

          } catch(IOException e){
               e.printStackTrace();
          }

          //Make Players
          try{
               david = new Player(ImageIO.read(new File("res/faces/david.png")));
               diego = new Player(ImageIO.read(new File("res/faces/diego.png")));
               jakob = new Player(ImageIO.read(new File("res/faces/jakob.png")));
               david.move( 0,  david.faces(0).getHeight());
               diego.move(0, 50);
          } catch(IOException e) {
               e.printStackTrace();
               System.exit(0);
          }

          //Initialize Font
          try {
               for (int i = 'a'; i <= 'z'; i++)
                    font.put((char) i, ImageIO.read(new File("res/font/"+(char) i + ".png")));
          } catch (IOException e){
               e.printStackTrace();
               System.exit(0);
          }

          //make frame and set up
          frame = new JFrame(title);
          try {
               BufferedImage ico = ImageIO.read(new File("res/icon.png"));
               frame.setIconImage(ico);
          } catch (IOException e){
               e.printStackTrace();
          }
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          frame.setResizable(false);
          this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
          frame.add(this);
          frame.addKeyListener(this);
          frame.pack();
          frame.setVisible(true);

          //Timer setup for repaint()
          timer = new Timer(30, this);
          timer.start();

     }
     public void keyReleased(KeyEvent e){
          if(pressed.remove(e.getKeyCode())){
               int c = e.getKeyCode();
               if(c == KeyEvent.VK_A || c == KeyEvent.VK_D) {
                    david.setRunning(0);
               }
               if(c == KeyEvent.VK_LEFT || c == KeyEvent.VK_RIGHT) {
                    diego.setRunning(0);
               }
          }

     }
     public void keyTyped(KeyEvent e){

     }
     public void keyPressed(KeyEvent e){
          pressed.add(e.getKeyCode());
          for(int code : pressed) {
          	if(code == 32){
          		if(timer.isRunning()) {
					timer.stop();
					repaint();
          		}
          		else
          			timer.start();
			}
               if (code == KeyEvent.VK_LEFT) {
                    diego.move(-1);
                    diego.setRunning(-1);
               }
               if (code == KeyEvent.VK_RIGHT) {
                    diego.move(1);
                    diego.setRunning(1);
               }
               if(code == KeyEvent.VK_UP && touching(diego)){
               	diego.setVely(-9);
			}
               if (code == KeyEvent.VK_A) {
                    david.move(-1);
                    david.setRunning(-1);
               }
               if (code == KeyEvent.VK_D) {
                    david.move(1);
                    david.setRunning(1);
               }
               if (code == KeyEvent.VK_W && touching(david))
               	david.setVely(-9);
          }
     }
     public static void main(String[] a){
//          SortedList<Integer> list = new SortedList<>();
//          list.add(2);
//          list.add(1);
//          list.add(3);
//          list.add(9);
//          list.add(2);
//          list.add(23);
//          list.add(4);
//          list.add(1);
//          System.out.println(list + " "+list.add(2));
     	//Create the Frame and Panel
          new Main("D-Day");
     }
     public void drawString(Graphics g, int x, int y, int h, String str) {
          str = str.toLowerCase();
          int xi = x;
          for(int i = 0; i<str.length(); i++){
               char a = str.charAt(i);
               if(font.containsKey(a)){
                    BufferedImage img = font.get(a);
                    int w = PIXEL * h * img.getWidth() / img.getHeight();
                    g.drawImage( img, x, y,  w, h * PIXEL, this);
                    x += w + PIXEL;
                    continue;
               }
               if( a == ' ' ) {
                    x += 3 * PIXEL;
                    continue;
               }
               if( a == 10){
                    y+= h * PIXEL + PIXEL;
                    x = xi;
                    continue;
               }
               System.out.println("Not a valid character - drawString()");
               System.exit(0);
          }
     }
     public void actionPerformed(ActionEvent e){
          repaint();
     }
     public static  BufferedImage reverse(BufferedImage img){
          BufferedImage rev = copy(img);
          for(int r = 0; r < img.getHeight(); r++){
               for(int c = 0; c < img.getWidth(); c++){
                    int col = img.getRGB(img.getWidth()-1-c, r);
                    rev.setRGB(c, r, col);
               }
          }
          return rev;
     }
     public static BufferedImage copy(BufferedImage source){
          BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
          Graphics g = b.getGraphics();
          g.drawImage(source, 0, 0, null);
          g.dispose();
          return b;
     }
}