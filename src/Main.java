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
     static final int WIDTH = 798, HEIGHT = 600, PIXEL = 6, gravity = 1, PixHEIGHT = HEIGHT/PIXEL;
     static HashMap<Character, BufferedImage> font = new HashMap<>();
     static Timer timer;
     static Player david, diego, jakob;
     static BufferedImage background, ground, topGround;
     static final Set<Integer> pressed = new HashSet<>();
     static ArrayList<ImageRect> stage = new ArrayList<>();
     public void paintComponent(Graphics g){
          paintBackground(g);
          diego.draw(g);
          gravity(diego);
          david.draw(g);
          gravity(david);
          jakob.draw(g);
          for(ImageRect img : stage) {
			img.draw(g);
          }
     }
     public void gravity(Player p){
		for(ImageRect img : stage)
			if(img.inside(p.getX(), p.getBotCornerY() + p.getVely()) || img.insidez(p.getX(), p.getBotCornerY())) {
				p.setY(img.getBounds().y - (p.getBotCornerY() - p.getY()));
				p.setVely(0);
				return;
			}
		if(p.getY() > HEIGHT) {
			p.setY(PixHEIGHT-2);
			p.setVely(-1);
		}
		p.changeY();
		p.changeVely(gravity);
		System.out.println(p.getY());
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
								System.out.println(x + " " + i);
							}
							if (mod.endsWith("+"))
								addy = Math.max(addy, ground.getHeight());
						}
					} else {
						addy = amount;
					}
				}
				i += addy;
			}

			//draw top layer
			for(int i = 0; i < topGround.getWidth(); i++)
				topGround.setRGB(i, 0, topGround.getRGB(0,0));

          } catch(IOException e){
               e.printStackTrace();
          }

          //Make Players
          try{
               david = new Player(ImageIO.read(new File("res/faces/david.png")));
               diego = new Player(ImageIO.read(new File("res/faces/diego.png")));
               jakob = new Player(ImageIO.read(new File("res/faces/jakob.png")));
               david.move(0,david.faces(0).getHeight());
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
//               ico = ico.getSubimage(0,0, ico.getWidth(), ico.getHeight() / 2);
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
               if (code == KeyEvent.VK_LEFT) {
                    diego.move(-1);
                    diego.setRunning(-1);
               }
               else if (code == KeyEvent.VK_RIGHT) {
                    diego.move(1);
                    diego.setRunning(1);
               }
               else if(code == KeyEvent.VK_UP && diego.getVely() == 0){
               	diego.setVely(-9);
			}
               if (code == KeyEvent.VK_A) {
                    david.move(-1);
                    david.setRunning(-1);
               }
               else if (code == KeyEvent.VK_D) {
                    david.move(1);
                    david.setRunning(1);
               }
               else if (code == KeyEvent.VK_W && david.getVely() == 0)
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