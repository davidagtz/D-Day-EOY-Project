import javax.imageio.ImageIO;
import javax.sound.sampled.*;
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
import java.nio.file.FileSystemNotFoundException;
import java.security.Key;
import java.util.*;

/*@davidagtz*/
public class Main extends JPanel implements ActionListener, KeyListener{
     public static long serialVersionUID = 0L;
     static JFrame frame;
     static final int WIDTH = 798, HEIGHT = 600, PIXEL = 6, PixHEIGHT = HEIGHT / PIXEL, PixWIDTH = WIDTH / PIXEL, sideAmount = 1;
     static int xoff = 0, level = 1;
     static final double gravity = .9;
     static HashMap<Character, BufferedImage> font = new HashMap<>();
     static Timer timer;
     static Player david, diego, jakob, richard;
     static HashMap<String, Player> players = new HashMap<>();
     static final Color pause = new Color(255, 0, 0, 127);
     static BufferedImage background, ground, topGround, speaker, mute, menu;
     static final Set<Integer> pressed = new HashSet<>();
     static ArrayList<ImageRect> stage = new ArrayList<>();
	static ArrayList<ImageRect> stagecut;
	static Clip sound;
	static boolean forMute = false;
     public void paintComponent(Graphics g){
     	switch(level){
			case 0: mainMenu(g); break;
			case 1: levelOne(g); break;
		}
     }
     public void mainMenu(Graphics g){
     	g.drawImage(menu, 0, 0, WIDTH, HEIGHT, null);
	}
     public void levelOne(Graphics g){
		//see how much to offset
		xoff = Math.min(david.getXR(), diego.getXR()) - sideAmount;
		if(diego.isDead())
			xoff = david.getXR() - 1;
		if (david.isDead())
			xoff = diego.getXR() - 1;

		stagecut = get(stage, xoff, xoff + WIDTH / PIXEL);
		paintBackground(g, xoff);
		for (Player p : players.values()) {
			if(timer.isRunning())
				gravity(p);
			if(p.isAlive())
				p.draw(g, xoff);
		}

		if(timer.isRunning()) {
			richard.setRunning(1);
			richard.move(2, 0);
		}

		for (ImageRect img : stagecut)
			img.draw(g, xoff);

		david.drawLives(g, 1, 1);
		diego.drawLives(g, 1, david.getHeart().getHeight() + 2);

		if(richard.touching(david) && david.isAlive())
			david.takeLife(1);
		if(richard.touching(diego) && diego.isAlive())
			diego.takeLife(1);

		if(!timer.isRunning()){
			g.setColor(pause);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			drawString(g, 3 * PIXEL, HEIGHT - 7 * PIXEL, 5, "PAUSED");
		}

		if(david.isDead() && diego.isDead()){
			g.setColor(pause);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			drawString(g, 3 * PIXEL, HEIGHT - 7 * PIXEL, 5, "YOU LOST");
		}

		if (sound != null) {
			g.drawImage(speaker, WIDTH - (speaker.getWidth() + 1) * PIXEL, PIXEL, speaker.getWidth() * PIXEL, speaker.getHeight() * PIXEL, null);
			if (!sound.isActive())
				g.drawImage(mute, WIDTH - (speaker.getWidth() + 1) * PIXEL, PIXEL, speaker.getWidth() * PIXEL, speaker.getHeight() * PIXEL, null);
			forMute = false;
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
		ArrayList<ImageRect> partStage = get(stage, p.getXR(), p.getXR() + p.getWidth());
		for(ImageRect img : partStage) {
			Rectangle tr = (Rectangle) p.getBounds().clone();
			Rectangle ir = img.getBounds();
			if(tr.y == ir.height + ir.y)
				p.setVely(0);
			if(ir.intersects(tr)){
				p.setY(ir.y - p.getBounds().height);
				p.setVely(0);
				inter = true;
			}
			tr.y += p.getVely();
			if(tr.y == ir.height + ir.y)
				p.setVely(0);
			if(ir.intersects(tr)){
				p.setY(ir.y - p.getBounds().height);
				p.setVely(0);
				inter = true;
			}
		}
		return inter;
	}
	//Tells if the sprite is touching
	public boolean isTouching(Player p){
		ArrayList<ImageRect> partStage = get(stage, p.getXR(), p.getXR() + p.getWidth());
		for(ImageRect img : partStage) {
			Rectangle tr = (Rectangle) p.getBounds().clone();
			Rectangle ir = img.getBounds();
			if(tr.y + tr.height == ir.y)
				return true;
			else if(tr.y == ir.height + ir.y)
				return true;
		}
		return false;
	}
	public int[] touching(Player p, int vel){
     	// first int is 0 if not intersecting and 1 if
		// it is touching. If they are touching then
		// second int is the dist between them
		int[] inter = { 0, 0 };
		Rectangle tr = (Rectangle) p.getBounds().clone();
		tr.x += vel;
		for(ImageRect img : stage) {
			Rectangle r = img.getBounds();
			if(r.intersects(tr)) {
				inter[0] = 1;
				if(vel < 0)
					inter[1] = r.x + r.width - p.getXR();
				else
					inter[1] = r.x - p.getXR() - p.getWidth();
			}
		}
		return inter;
	}
     public void gravity(Player p){
     	boolean inter = touching(p);
		if(inter) {
			p.setVely(0);
			return;
		}
		if(p.getY() > HEIGHT && !p.equals(richard)) {
			p.takeLife(1);
			if (stagecut.size() == 0) {
				p.setX(stage.get(stage.size() - 1).x);
				p.setY(stage.get(stage.size() - 1).y - p.getBounds().height);
			} else {
				p.setX(stagecut.get(0).x + 1);
				p.setY(stagecut.get(0).y - p.getBounds().height);
			}
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
          try{
			//play music
			AudioInputStream inp = AudioSystem.getAudioInputStream(new File("res/audio/music.wav"));
			sound = AudioSystem.getClip();
			sound.open(inp);
			sound.loop(Clip.LOOP_CONTINUOUSLY);

			//load speaker icon
			speaker = ImageIO.read(new File("res/speaker/speaker.png"));
			mute = ImageIO.read(new File("res/speaker/mute.png"));
		} catch (IOException e) {
          	e.printStackTrace();
		} catch (LineUnavailableException e){
          	e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
          	e.printStackTrace();
		}

    		try{
			//get Backgrounds
//               background = ImageIO.read(new File("res/background.png"));	tr
//               menu = ImageIO.read(new File("res/Menu/menu.png"));
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
						if(mod.matches("-\\+?")){
							if(!mod.matches("-\\+"))
								x += amount;
							else
								x += amount * ground.getWidth();
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
               players.put("david", new Player(ImageIO.read(new File("res/faces/david.png"))));
			players.put("diego", new Player(ImageIO.read(new File("res/faces/diego.png"))));
			players.put("jakob", new Player(ImageIO.read(new File("res/faces/jakob.png"))));
			players.put("richard", new Player(ImageIO.read(new File("res/faces/richard.png"))));
               p("richard").move(8 * 5, 0);
			p("david").move( 5, 10);
			p("david").setCrown(true);
			p("diego").move(5, 50);
			richard = p("richard");
			david = p("david");
			jakob = p("jakob");
			diego = p("diego");
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
     public Player p(String p){
     	return players.get(p);
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
     	int close = Math.abs(diego.getXR() - david.getXR()) + diego.getWidth();
     	boolean daveInFront = diego.getX() - david.getX() > 0;
     	if(pressed.contains((Integer) KeyEvent.VK_SEMICOLON) || pressed.contains((Integer) KeyEvent.VK_Q))
     		System.exit(0);
          pressed.add(e.getKeyCode());
		if(pressed.contains(32)){
			if(timer.isRunning()) {
				timer.stop();
				repaint();
			}
			else
				timer.start();
		}

		if(!timer.isRunning())
			return;

		// Diego's controls
		if(diego.isAlive()) {
			boolean isClose = close + diego.runspeed < PixWIDTH;
			if (pressed.contains(KeyEvent.VK_LEFT) && (isClose || daveInFront || david.isDead())) {
				int[] touch = touching(diego, -diego.runspeed);
				if (touch[0] == 1)
					diego.move(touch[1], 0);
				else
					diego.move(-1);
				diego.setRunning(-1);
			}
			if (pressed.contains(KeyEvent.VK_RIGHT) && (isClose || !daveInFront || david.isDead())) {
				int[] touch = touching(diego, diego.runspeed);
				if (touch[0] == 1)
					diego.move(touch[1], 0);
				else
					diego.move(1);
				diego.setRunning(1);
			}
			if (pressed.contains(KeyEvent.VK_UP) && isTouching(diego)) {
				diego.setVely(-9);
			}
		}

		// david's controls
		if(david.isAlive()) {
			if (pressed.contains(KeyEvent.VK_C))
				david.setCrown(david.crown == null ? true : false);
			boolean isClose = close + david.runspeed < PixWIDTH;
			if (pressed.contains(KeyEvent.VK_A) && (isClose || !daveInFront || diego.isDead())) {
				int[] touch = touching(david, -david.runspeed);
				if (touch[0] == 1)
					david.move(touch[1], 0);
				else
					david.move(-1);
				david.setRunning(-1);
			}
			if (pressed.contains(KeyEvent.VK_D) && (isClose || daveInFront || diego.isDead())) {
				int[] touch = touching(david, david.runspeed);
				if (touch[0] == 1)
					david.move(touch[1], 0);
				else
					david.move(1);
				david.setRunning(1);
			}
			if (pressed.contains(KeyEvent.VK_W) && isTouching(david)) {
				david.setVely(-9);
			}
		}

		if(pressed.contains(KeyEvent.VK_M)){
			if(sound.isRunning())
				sound.stop();
			else
				sound.start();
			forMute = true;
			repaint();
          }
     }
     public static void main(String[] a){
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