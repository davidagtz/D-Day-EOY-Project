import com.sun.javaws.util.JfxHelper;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.imageio.ImageIO;
import javax.sound.midi.SysexMessage;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystemNotFoundException;
import java.security.Key;
import java.util.*;

/*@davidagtz*/
public class Main extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener{
     public static long serialVersionUID = 0L;
     static JFrame frame;
     static final int WIDTH = 798, HEIGHT = 600, PIXEL = 6, PixHEIGHT = HEIGHT / PIXEL, PixWIDTH = WIDTH / PIXEL, sideAmount = 1;
	static Timer timer;

	//MENU
	static Color mainBack = new Color(26, 26, 26);

	//STORY
	static ArrayList<ImageRect> storyboards = new ArrayList<>();
	static int currBoard = 0;

	// LEVEL ONE
	// level 0 - Main Menu
	// level 1 - Game
	// level 2 - Controls
	// level 3 - Editor
	// level 4 - story
	static int xoff = 0, level = 0;
	static final double gravity = .9;
	static Player david, diego, jakob, richard;
	static ArrayList<ImageRect> stage = new ArrayList<>();
	static ArrayList<ImageRect> stagecut;
	// 1 - won
	// 0 - playing
	// -1 - lost
	static int winlose = 0;
	static Color won = new Color(0, 255, 0, 127);
	static String levelName = "res/levels/second.level";

	// EDITOR
	static int arrX = 1, arrY = 1, editOffX = 0, editOffXmax = 0, stageLength = 20, lastX, lastY, lastXD, lastYD;
	static boolean gridMode = false;

	// MISC
     static HashMap<Character, BufferedImage> font = new HashMap<>();
     static HashMap<Character, BufferedImage> fontW = new HashMap<>();
     static HashMap<String, Player> players = new HashMap<>();
     static final Color pause = new Color(255, 0, 0, 127);
     static BufferedImage background, ground, topGround, speaker, mute, speakerW, WIN;
	static Clip sound;
	static boolean forMute = false;
	static ImageRect menu, controls, editor, cursor;

	// KEY LISTENER
     static final Set<Integer> pressed = new HashSet<>();

     public void paintComponent(Graphics g){
     	switch(level){
			case 0: mainMenu(g); break;
			case 1: levelOne(g); break;
			case 2: showControls(g); break;
			case 3: paintEditor(g); break;
			case 4: paintStory(g); break;
		}
		if (sound != null) {
     		if(level == 1 || level == 3 || level ==4)
				g.drawImage(speaker, WIDTH - (speaker.getWidth() + 1) * PIXEL, PIXEL, speaker.getWidth() * PIXEL, speaker.getHeight() * PIXEL, null);
			else
				g.drawImage(speakerW, WIDTH - (speaker.getWidth() + 1) * PIXEL, PIXEL, speaker.getWidth() * PIXEL, speaker.getHeight() * PIXEL, null);
     		if (!sound.isActive())
				g.drawImage(mute, WIDTH - (speaker.getWidth() + 1) * PIXEL, PIXEL, speaker.getWidth() * PIXEL, speaker.getHeight() * PIXEL, null);
			forMute = false;
		}
     }
     public void mainMenu(Graphics g){
     	g.setColor(mainBack);
     	g.fillRect(0, 0, WIDTH, HEIGHT);
     	menu.draw(g);
	}
     public void levelOne(Graphics g){
		//see how much to offset
		xoff = Math.min(david.getXR(), diego.getXR()) - sideAmount;
		if(diego.isDead())
			xoff = david.getXR() - 1;
		if (david.isDead())
			xoff = diego.getXR() - 1;

		if(winlose > 0){
			if(winlose == 1){
				g.setColor(won);
				g.fillRect(0, 0, WIDTH, HEIGHT);
				drawStringW(g, 3, PixHEIGHT - 7, 5, "YOU WIN");
				winlose++;
			}
			return;
		} else if(winlose < 0){
			if(winlose == -1) {
				winlose--;
			}
			else if(winlose == -2){
				g.setColor(pause);
				g.fillRect(0, 0, WIDTH, HEIGHT);
				drawString(g, 3, PixHEIGHT - 7, 5, "YOU LOSE");
				winlose--;
				return;
			}
			else if(winlose == -3){
				return;
			}
		}

		stagecut = get(stage, xoff, xoff + WIDTH / PIXEL);
		paintBackground(g, xoff);
		for (Player p : players.values()) {
			if(timer.isRunning())
				gravity(p);
			if(p.isAlive()) {
				p.draw(g, xoff);
//				g.setColor(Color.red);
//				g.drawRect((p.getXR() - xoff) * PIXEL, p.getY() * PIXEL, p.getWidth() * PIXEL, p.getHeight() * PIXEL);
			}
		}

		if(timer.isRunning()) {
			richard.setRunning(1);
			richard.moveNR(2, 0);
		}

		for (ImageRect img : stagecut) {
			img.draw(g, xoff);
		}

		if(david.isAlive() && david.intersectsArray(getById(stagecut, "flag"))){
			winlose = 1;
		}
		if(diego.isAlive() && diego.intersectsArray(getById(stagecut, "flag"))){
			winlose = 1;
		}

		david.drawLives(g, 1, 1);
		diego.drawLives(g, 1, david.getHeart().getHeight() + 2);

		if(richard.touching(david) && david.isAlive())
			david.takeLife(1);
		if(richard.touching(diego) && diego.isAlive())
			diego.takeLife(1);

		if(!timer.isRunning()){
			g.setColor(pause);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			drawString(g, 3, PixHEIGHT - 7, 5, "PAUSED");
		}

		if(winlose == 0 && david.isDead() && diego.isDead()){
			winlose = -1;
		}
	}
	public void paintStory(Graphics g){
     	super.paintComponent(g);
		storyboards.get(currBoard).draw(g);
	}
	public ArrayList<ImageRect> getById(ArrayList<ImageRect> children, String id){
		ArrayList<ImageRect> newOne = new ArrayList<>();
		for(int i = 0; i < children.size(); i++){
			if(children.get(i).getId().matches(id)){
				newOne.add(children.get(i));
			}
		}
		return newOne;
	}
	public static ArrayList<ImageRect> get(ArrayList<ImageRect> list, int beg, int end){
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
			if(img.getId().matches("flag"))
				continue;

			Rectangle tr = (Rectangle) p.getBounds().clone();
			Rectangle ir = img.getBounds();
			if(p.getVely() < 0){
				if(tr.y <= ir.height + ir.y && tr.intersects(ir)){
					p.setVely(0);
					p.setY(ir.height + ir.y);
				}
			}
			else{
				if(tr.intersects(ir)){
					p.setY(ir.y - p.getBounds().height);
					p.setVely(0);
					inter = true;
				}
			}
			tr.y += p.getVely();
			double vy = p.getVely();
			if(p.getVely() <= 0){
				if(tr.y <= ir.height + ir.y && tr.intersects(ir)){
					p.setVely(-p.getVely());
					p.setY(ir.height + ir.y);
				}
			}
			if(vy >= 0){
				if(tr.intersects(ir)){
					p.setY(ir.y - p.getBounds().height);
					p.setVely(0);
					inter = true;
				}
			}
		}
		return inter;
	}
	//Tells if the sprite is touching
	public static boolean isTouching(Player p){
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
	public static int[] touching(Player p, int vel){
     	// first int is 0 if not intersecting and 1 if
		// it is touching. If they are touching then
		// second int is the dist between them
		int[] inter = { 0, 0 };
		Rectangle tr = (Rectangle) p.getBounds().clone();
		tr.x += vel;
		boolean first = true;
		for(ImageRect img : stage) {
			Rectangle r = img.getBounds();
			if(r.intersects(tr)){// && !img.getId().matches("flag")) {
				inter[0] = 1;
				if(first) {
					if (vel < 0)
						inter[1] = r.x + r.width - p.getXR();
					else
						inter[1] = r.x - p.getXR() - p.getWidth();
					first = false;
				}
				else{
					if (vel < 0)
						inter[1] = Math.max(r.x + r.width - p.getXR(), inter[1]);
					else
						inter[1] = Math.min(r.x - p.getXR() - p.getWidth(), inter[1]);
				}
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
			p.respawn();
		}
		p.changeY();
		p.changeVely(gravity);
	}
	public static Player other(Player p){
     	if(p.equals(david))
     		return diego;
     	else if(p.equals(diego))
     		return david;
     	else
     		return null;
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
		BufferedImage bi = background.getSubimage(xoff, 0, PixWIDTH * background.getHeight() / PixHEIGHT, background.getHeight());
		g.drawImage(bi, 0, 0, WIDTH, HEIGHT, null);
	}
	public void showControls(Graphics g){
     	g.setColor(mainBack);
     	g.fillRect(0, 0, WIDTH, HEIGHT);
     	int h = 10;
		int wid = stringWidth("CONTROLS", h);
		drawStringW(g, PixWIDTH / 2 - wid / 2, 3, h, "CONTROLS");
		controls.draw(g);
	}
	public void paintEditor(Graphics g){
     	paintBackground(g, editOffX);
//     	g.setColor(Color.RED);
//     	g.drawLine(ground.getWidth() * PIXEL * PIXEL, 0, ground.getWidth() * PIXEL * PIXEL, HEIGHT);
		g.setColor(Color.DARK_GRAY);
		int off = getOff();
		if(gridMode){
			for(int c = 0; c < PixHEIGHT; c += ground.getWidth()){
				g.drawLine(0, c * PIXEL, WIDTH, c * PIXEL);
			}
			for(int r = -off; r < PixWIDTH; r += ground.getHeight()){
				g.drawLine(r * PIXEL, 0, r * PIXEL, HEIGHT);
			}
		}
		stageLength = Math.max(5 * editOffXmax / PixWIDTH + 1, stageLength);
		g.fillRect((PixWIDTH / 2 - stageLength / 2) * PIXEL, 3 * PIXEL,  stageLength * PIXEL, PIXEL);
		g.setColor(Color.red);
		g.fillRect((PixWIDTH / 2 - stageLength / 2  + 5 * editOffX / PixWIDTH) * PIXEL, 3 * PIXEL, PIXEL, PIXEL );
		editor.draw(g);
		if(cursor != null) {
			cursor.draw(g);
			if(cursor.getId().equals("erase")){
				g.setColor(Color.RED);
				g.fillRect(lastXD * PIXEL, lastYD * PIXEL, PIXEL, PIXEL);
			}
		}
	}
	public int getOff(){
		int off = editOffX % WIDTH;
		off %= ground.getWidth();
		return off;
	}
	public int getOffWGround(int mouse){
		int x = mouse / PIXEL + getOff();
		x /= ground.getWidth();
		x *= ground.getWidth();
		x -= getOff();
		return x;
	}

	//CONSTRUCTOR
     public Main(String title){
          try{
			//play music
			AudioInputStream inp = AudioSystem.getAudioInputStream(new File("res/audio/music.wav"));
			sound = AudioSystem.getClip();
			sound.open(inp);
			sound.loop(Clip.LOOP_CONTINUOUSLY);
			sound.stop();

			//load speaker icon
			speaker = ImageIO.read(new File("res/speaker/speaker.png"));
			mute = ImageIO.read(new File("res/speaker/mute.png"));
			speakerW = white(copy(speaker));
		} catch (IOException e) {
          	e.printStackTrace();
		} catch (LineUnavailableException e){
          	e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
          	e.printStackTrace();
		}

    		try{
			//get Backgrounds
               background = ImageIO.read(new File("res/background.png"));
//               menu = ImageIO.read(new File("res/Menu/menu.png"));
               //get ground
			ground = ImageIO.read(new File("res/ground.png"));
			topGround = copy(ground);
			WIN = ImageIO.read(new File("res/editor/flag.png"));

			//add blocks
			parse("res/levels/second.level");

          } catch(IOException e){
               e.printStackTrace();
          }

          //Make Players
          try{
               players.put("david", new Player(ImageIO.read(new File("res/faces/david.png"))));
			players.put("diego", new Player(ImageIO.read(new File("res/faces/diego.png"))));
			players.put("jakob", new Player(ImageIO.read(new File("res/faces/jakob.png"))));
			players.put("richard", new Player(ImageIO.read(new File("res/faces/richard.png"))));
               p("richard").moveNR( - 40 * PIXEL, 0);
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

		//make a menu
		menu = new ImageRect(0, 0, WIDTH, HEIGHT);
		try {
			menu.addChild(new ImageRect(7, 7, ImageIO.read(new File("res/menu/title.png"))));
			menu.addChild(new HoverImage(14, 47, ImageIO.read(new File("res/menu/controls.png"))){
				public void clickAction(int x, int y){
					Main.level = 2;
				}
			});
			menu.addChild(new HoverImage(46, 29, ImageIO.read(new File("res/menu/start.png"))){
				public void clickAction(int x, int y){
					if(storyboards.isEmpty())
						Main.level = 1;
					else
						Main.level = 4;
				}
			});
			menu.addChild(new HoverImage(35, 65, ImageIO.read(new File("res/menu/editor.png"))){
				public void clickAction(int x, int y){
					Main.level = 3;
				}
			});
			menu.addChild(new ImageRect(16, 65, jakob.faces(0)));
			menu.addChild(new ImageRect(18, 31, david.faces(0)));
			menu.addChild(new ImageRect(76, 47, diego.faces(0)));
		} catch (IOException e){
			e.printStackTrace();
		}

		//Initialize Editor
		try {
			editor = new ImageRect(0, 0, WIDTH, HEIGHT){
				protected void drawOff(Graphics g, int x, int y){
					if(img != null)
						g.drawImage(img, (x + this.x) * PIXEL, (y + this.y) * PIXEL, img.getWidth() * PIXEL, img.getHeight() * PIXEL, null);
					if(text != null)
						Main.drawStringW(g, x + this.x, y + this.y, h, text);
					for(ImageRect iR : children){
						if(iR != null) {
							if(iR.getId() != null && iR.getId().matches("ground|flag"))
								iR.drawOff(g, x + this.x - editOffX, y + this.y);
							else
								iR.drawOff(g, x + this.x, y + this.y);
						}
					}
				}
			};
			BufferedImage arr = ImageIO.read(new File("res/menu/arrow.png"));
			editor.addChild(new HoverImage(arrX, arrY, arr) {
				public void clickAction(int x, int y) {
					if(cursor != null && !cursor.getId().matches("erase"))
						editor.getChildren().remove(0);
					Main.level = 0;
				}
			});
			editor.addChild(new HoverImage(arrX + arr.getWidth() + 1, arrY, ImageIO.read(new File("res/editor/darrow.png"))) {
				public void clickAction(int x, int y) {
					try {
						if(cursor != null && !cursor.getId().matches("erase"))
							editor.getChildren().remove(0);
						save("res/levels/second.level");
						parse("res/levels/second.level");
					} catch(IOException e){
						e.printStackTrace();
					}
				}
			});
			editor.addChild(new HoverImage(arrX + arr.getWidth() + 10 + 2, arrY, ImageIO.read(new File("res/editor/open.png"))) {
				public void clickAction(int x, int y) {
					if(cursor != null && !cursor.getId().matches("erase"))
						editor.getChildren().remove(0);
					JFileChooser f = new JFileChooser("res/levels");
					f.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							if(f.isDirectory())
								return true;
							if(f.getName().endsWith(".level"))
								return true;
							return false;
						}
						public String getDescription() {
							return "Accepts Level Files";
						}
					});
					int res = f.showOpenDialog(frame);
					try {
						if (res == JFileChooser.APPROVE_OPTION) {
							parse(f.getSelectedFile().getAbsolutePath());
							levelName = f.getSelectedFile().getAbsolutePath();
							ArrayList<ImageRect> iRarr = new ArrayList<>();
							parse(f.getSelectedFile().getAbsolutePath(), iRarr);
							for(int i = 0; i < iRarr.size(); i++){
								if(iRarr.get(i).getId().equals("ground")){
									iRarr.set(i, new HoverImage(iRarr.get(i), ImageIO.read(new File("res/editor/groundRed.png"))) {
										public void change(int x, int y) {
											if (bounds.contains(x + editOffX, y) && cursor != null && Main.cursor.getId().equals("erase")) {
												onTop = true;
											} else {
												onTop = false;
											}
										}
									}.setId("ground"));
								}
								else if(iRarr.get(i).getId().equals("flag")){
									iRarr.set(i, new HoverImage(cursor, ImageIO.read(new File("res/editor/flagRed.png"))) {
										public void change(int x, int y) {
											if (bounds.contains(x + editOffX, y) && cursor != null && Main.cursor.getId().equals("erase")) {
												onTop = true;
											} else {
												onTop = false;
											}
										}
									}.setId("flag"));
								}
							}
							editor.getChildren().addAll(0, iRarr);
						}
					} catch (IOException e){
						e.printStackTrace();
					}
				}
			});
			BufferedImage img = ImageIO.read(new File("res/editor/drag.png"));
			ImageRect drag = new ImageRect(PixWIDTH - 6, 0, img){
				int dragW = 20;
				Rectangle drag = new Rectangle(0, img.getHeight() / 2 - dragW / 2, 6, dragW);
				boolean out = false;
				public void clickAction(int x, int y){
					if(cursor != null && !cursor.getId().matches("erase"))
						editor.getChildren().remove(0);
					if(drag.contains(x, y)){
						if(out) {
							setX(PixWIDTH - 6);
						}
						else {
							setX(PixWIDTH - img.getWidth());
						}
						out = !out;
					}
				}
			};
			int offset = 10;
			int yff = 4;
			editor.addChild(drag.addChild(new ImageRect(offset, yff, copy(ground)){
				public void clickAction(int x, int y){
					setCursor("ground");
				}
			}
			));
			yff += ground.getWidth();
			drag.addChild(new ImageRect(offset, yff + 2, copy(mute)){
				public void clickAction(int x, int y){
					setCursor("erase");
				}
			});
			yff += 2 + mute.getHeight();
			drag.addChild(new ImageRect(offset, yff + 2, copy(WIN)){
				public void clickAction(int x, int y){
					setCursor("flag");
				}
			});
			yff += WIN.getHeight() + 4;
			drag.addChild(new ImageRect(offset, yff, david.getWidth(), david.getHeight()){
				public void drawOff(Graphics g, int x, int y){
					g.setColor(Color.RED);
					g.fillRect((this.x + x) * PIXEL, (y + this.y) * PIXEL, PIXEL, david.getHeight() * PIXEL);
					g.fillRect((this.x + x + david.getWidth()) * PIXEL, (y + this.y) * PIXEL, PIXEL, PIXEL * david.getHeight());
					g.fillRect((this.x + x) * PIXEL, ((y + this.y) + david.getHeight() - 1) * PIXEL, PIXEL * david.getWidth(), PIXEL);
					g.fillRect((this.x + x) * PIXEL, (y + this.y) * PIXEL, PIXEL * david.getWidth(), PIXEL);
					for(ImageRect iR : children){
						if(iR != null)
							iR.drawOff(g, x + this.x, y + this.y);
					}
				}
				public void clickAction(int x, int y){
					setCursor("boundary");
				}
			});
			BufferedImage img2 = ImageIO.read(new File("res/editor/save.png"));
			drag.addChild(new HoverImage(8, drag.getHeight() - img2.getHeight() / 2 - 2, img2){
				public void clickAction(int x, int y) {
					JFileChooser f = new JFileChooser("res/levels");
					f.setApproveButtonText("Save As");
					f.setSelectedFile(new File(levelName));
					f.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							if(f.isDirectory())
								return true;
							if(f.getName().endsWith(".level"))
								return true;
							return false;
						}
						public String getDescription() {
							return "Accepts Level Files";
						}
					});
					int res = f.showOpenDialog(frame);
					if (res == JFileChooser.APPROVE_OPTION){
						String s = f.getSelectedFile().getAbsolutePath();
						if (!s.endsWith(".level"))
							s += ".level";
						levelName = s;
						save(s);
					}
				}
			});
		}catch (IOException e){
			e.printStackTrace();
		}

          //Initialize Font
          try {
               for (int i = 'a'; i <= 'z'; i++) {
				font.put((char) i, ImageIO.read(new File("res/font/" + (char) i + ".png")));
				fontW.put((char) i, white(copy(font.get((char) i))));
               }
               font.put('-', ImageIO.read(new File("res/font/dash.png")));
               fontW.put('-', white(copy(font.get('-'))));
               font.put('\'', ImageIO.read(new File("res/font/apos.png")));
               fontW.put('\'', white(copy(font.get('\''))));
          } catch (IOException e){
               e.printStackTrace();
               System.exit(0);
          }

          // Story
		try{
			BufferedImage timg = reverse(ImageIO.read(new File("res/menu/arrow.png")));
			for(int i = 1;; i++){
				File dir = new File("res/boards/board" + i);
				if(dir.exists() && dir.isDirectory()){
					GifRect board = (GifRect) new GifRect(0, 0, WIDTH, HEIGHT, 1000).addChild(
						   new HoverImage(PixWIDTH - timg.getWidth() - 1, PixHEIGHT - timg.getHeight() / 2 - 1, copy(timg)) {
							   public void clickAction(int x, int y) {
								   if (currBoard >= storyboards.size() - 1) {
									   level = 1;
									   currBoard = 0;
									   return;
								   }
								   currBoard++;
							   }
						   }
					);
					try{
						for(int j = 1;; j++){
							board.addFrame(ImageIO.read(new File(dir.getPath() + "/board" + j+".png")));
						}
					}catch(IOException e){
						System.out.println("Out of boards for gif " + i);
						board.addChild(new ImageRect((PixWIDTH - stringWidth("SWOLE", 10)) / 2, 10, stringWidth("SWOLE", 10), 10).setText("SWOLE", 10, ImageRect.BLACK));
						storyboards.add(board);
					}
				}
				else {
					storyboards.add(new ImageRect(0, 0, ImageIO.read(new File("res/boards/board" + i + ".png"))).addChild(
						   new HoverImage(PixWIDTH - timg.getWidth() - 1, PixHEIGHT - timg.getHeight() / 2 - 1, copy(timg)) {
							   public void clickAction(int x, int y) {
								   if (currBoard >= storyboards.size() - 1) {
									   level = 1;
									   currBoard = 0;
									   return;
								   }
								   currBoard++;
							   }
						   }
					));
				}
			}
		}catch(IOException e){
			System.out.println("No more boards.");
		}
//		storyboards.clear();

          // Controls
		try {
			controls = new ImageRect(0, 0, WIDTH, HEIGHT);
			controls.addChild(new HoverImage(arrX, arrY, ImageIO.read(new File("res/menu/arrow.png"))){
				public void clickAction(int x, int y){
					Main.level = 0;
				}
			});
			ImageRect diegoControls = new ImageRect(10, 20, PixWIDTH - 10, PixHEIGHT / 2 - 16);
			int h = 5;
			int off = 10;
			diegoControls.setText("- Diego's Controls", h);
			diegoControls.addChild(new ImageRect(off, h + 1, diegoControls.getWidth() - off, h).setText("- Up Arrow - Jump", h));
			diegoControls.addChild(new ImageRect(off, (h + 1) * 2, diegoControls.getWidth() - off, h).setText("- Left Arrow - Left", h));
			diegoControls.addChild(new ImageRect(off, (h + 1) * 3, diegoControls.getWidth() - off, h).setText("- Right Arrow - Right", h));
			controls.addChild(diegoControls);
			ImageRect davidControls = new ImageRect(10, PixHEIGHT / 2, PixWIDTH - 10, PixHEIGHT / 2 - 16);
			davidControls.setText("- David's Controls", 5);
			davidControls.addChild(new ImageRect(off, h + 1, diegoControls.getWidth() - off, h).setText("- W - Jump", h));
			davidControls.addChild(new ImageRect(off, (h + 1) * 2, diegoControls.getWidth() - off, h).setText("- A - Left", h));
			davidControls.addChild(new ImageRect(off, (h + 1) * 3, diegoControls.getWidth() - off, h).setText("- D - Right", h));
			controls.addChild(davidControls);
		} catch (IOException e){
			e.printStackTrace();
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
          frame.getContentPane().addMouseListener(this);
          frame.getContentPane().addMouseMotionListener(this);
          frame.pack();
          frame.setVisible(true);

          //Timer setup for repaint()
          timer = new Timer(30, this);
          timer.start();
     }

     //FOR LEVEL PARSING
     public void save(String name){
		try {
			PrintWriter out = new PrintWriter(new FileWriter(name));
			ArrayList<ImageRect> stage2 = ((ArrayList<ImageRect>) editor.getChildren().clone());
			stage2.sort(new Comparator<ImageRect>() {
				public int compare(ImageRect r, ImageRect r2) {
					if (r.y == r2.y)
						return r.x - r2.x;
					return r.y - r2.y;
				}
			});
			ImageRect prev = null;
			boolean hasSeenGround = false;
			for (int i = 0; i < stage2.size(); i++) {
				ImageRect r = stage2.get(i);
				if (r.id == null || !r.getId().matches("ground|flag")) {
					continue;
				}
				String f = r.getId();
				switch(f){
					case "ground" : f = "_"; break;
					case "flag" : f = "f"; break;
				}
				if(!hasSeenGround) {
					out.println(r.getY());
					out.print("-40 -+ 40 _+ ");
					out.print(r.getX() + " - ");
					out.print(1 + " " + f + " ");
					hasSeenGround = true;
				}
				else if (prev != null && prev.getY() != r.y) {
					out.println();
					out.println(r.getY() - prev.getY() - 1);
					out.print(r.getX() + " - ");
					out.print(1 + " " + f + " ");
				} else {
					out.print(r.getX() - prev.getX() - 1 + " - ");
					out.print(1 + " " + f + " ");
				}
				prev = stage2.get(i);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
     public void parse(String name) throws IOException{
		if(stagecut != null)
			stagecut.clear();
		parse(name, stage);
	}
	public void parse(String name, ArrayList<ImageRect> stg) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(name));
		stg.clear();
		for(int i = 0; in.ready(); i+=0){
			StringTokenizer line = new StringTokenizer(in.readLine());
			int x = 0;
			int addy = 1;
			while (line.hasMoreTokens()){
				int amount = Integer.parseInt(line.nextToken());
				if(line.hasMoreTokens()) {
					String mod = line.nextToken();
					if (mod.matches("_[\\^\\+]*")) {
						for (int j = 0; j < amount; j++) {
							stg.add(new ImageRect(x, i, copy(ground)).setId("ground"));
							if(!mod.contains("+"))
								x += 1;
							else
								x += ground.getWidth();
						}
						if (mod.contains("^"))
							addy = Math.max(addy, ground.getHeight());
					}
					else if(mod.matches("-\\+?")){
						if(!mod.matches("-\\+"))
							x += amount;
						else
							x += amount * ground.getWidth();
					}
					else if(mod.matches("f[\\^\\+]*")){
						stage.add(new ImageRect(x, i, copy(WIN)).setId("flag"));
						if(mod.contains("+"))
							x += WIN.getWidth();
						if(mod.contains("^"))
							addy = Math.max(addy, WIN.getHeight());
					}
				} else {
					addy = amount;
				}
			}
			i += addy;
		}

		//sort ground by x
		Collections.sort(stg);

		//draw top layer
		for(int i = 0; i < topGround.getWidth(); i++)
			topGround.setRGB(i, 0, topGround.getRGB(0,0));
		ArrayList<ImageRect> stageg = getById(stg, "ground");
		if(stageg.size() > 0)
			stageg.get(0).setImg(copy(topGround));
		for(int i = 1; i < stageg.size(); i++)
			if(stageg.get(i - 1).getX() != stageg.get(i).getX())
				stageg.get(i).setImg(copy(topGround));

		in.close();
	}

	//USELESS RN
     public Player p(String p){
     	return players.get(p);
	}

	//KEY METHODS
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
     	if(pressed.contains(KeyEvent.VK_SEMICOLON) || pressed.contains(KeyEvent.VK_Q))
     		System.exit(0);
          pressed.add(e.getKeyCode());
          switch(level){
			case 1 : keyLevelOne(e); break;
			case 3 : keyEditor(e); break;
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
     public void keyEditor(KeyEvent e){
     	if (pressed.contains(KeyEvent.VK_E)) {
     		setCursor("erase");
		}
		else if(pressed.contains(KeyEvent.VK_G)){
     		if(pressed.contains(KeyEvent.VK_CONTROL))
     			gridMode = !gridMode;
     		else
     			setCursor("ground");
		}
		else if(pressed.contains(KeyEvent.VK_F))
			setCursor("flag");
     	else if(pressed.contains(KeyEvent.VK_B)){
     		setCursor("boundary");
		}
		System.out.println(getOff());

		if(pressed.contains(KeyEvent.VK_RIGHT)) {
//     		if(gridMode) {
//				if(editOffX + PixWIDTH + ground.getWidth() <= background.getWidth() * PixHEIGHT / background.getHeight())
//     				editOffX += ground.getWidth();
//				else
//					editOffX = background.getWidth() * PixHEIGHT / background.getHeight();
//			}
//			else
				editOffX += 1;
			editOffXmax = Math.max(editOffX, editOffXmax);
     	}
     	if(pressed.contains(KeyEvent.VK_LEFT)) {
//			if(gridMode) {
//				if(ground.getWidth() <= editOffX)
//					editOffX -= ground.getWidth();
//				else
//					editOffX = 0;
//			}
//			else if(editOffX > 0)
     			editOffX -= 1;
		}
     	editor.hover(lastXD, lastYD);
	}
     public void keyLevelOne(KeyEvent e){
		int close = Math.abs(diego.getXR() - david.getXR()) + diego.getWidth();
		boolean daveInFront = diego.getX() - david.getX() > 0;
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
				david.setCrown(david.crown == null);
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
	}

	//MAIN
     public static void main(String[] a){
     	//Create the Frame and Panel
          new Main("D-Day");
     }

     //GRAPHIC STRING METHODS
     //NO NEW LINES
     public int stringWidth(String str, int h){
     	int w = 0;
     	BufferedImage img = font.get('a');
     	for(int i = 0; i < str.length(); i++){
     		if(str.charAt(i) == ' ')
     			w += 3 * PIXEL;
     		else
     			w += h * img.getWidth() / img.getHeight() + 1;
		}
		return w;
	}
	public static void drawString(Graphics g, int x, int y, int h, String str) {
		str = str.toLowerCase();
		x *= PIXEL;
		int xi = x;
		y *= PIXEL;
		for(int i = 0; i<str.length(); i++){
			char a = str.charAt(i);
			if(font.containsKey(a)){
				BufferedImage img = font.get(a);
				int w = PIXEL * h * img.getWidth() / img.getHeight();
				g.drawImage( img, x, y,  w, h * PIXEL, null);
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
     public static void drawStringW(Graphics g, int x, int y, int h, String str) {
		str = str.toLowerCase();
		x *= PIXEL;
		int xi = x;
		y *= PIXEL;
		for(int i = 0; i<str.length(); i++){
			char a = str.charAt(i);
			if(fontW.containsKey(a)){
				BufferedImage img = fontW.get(a);
				int w = PIXEL * h * img.getWidth() / img.getHeight();
				g.drawImage( img, x, y,  w, h * PIXEL, null);
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

     //MOUSE METHODS
	public void mouseClicked(MouseEvent e) {
     	int x = e.getX() / PIXEL;
     	int y = e.getY() / PIXEL;
     	lastX = x;
     	lastY = y;
     	if(level == 0) {
			menu.click(x, y);
		}
		else  if(level == 2){
			controls.click(x, y);
		}
		else if(level == 3){
			if(cursor != null){
				try {
					if(cursor.getId().equals("ground")) {
						editor.addChild(0, new HoverImage(cursor, ImageIO.read(new File("res/editor/groundRed.png"))) {
							public void change(int x, int y) {
								if (bounds.contains(x + editOffX, y) && cursor != null && Main.cursor.getId().equals("erase")) {
									onTop = true;
								} else {
									onTop = false;
								}
							}
						}.setId("ground").setX(editOffX + cursor.getX()));
					}
					else if(cursor.getId().equals("erase")){
						editor.remove(x + editOffX, y, ".{1,}");
					}
					else if(cursor.getId().equals("flag")){
						editor.addChild(0, new HoverImage(cursor, ImageIO.read(new File("res/editor/flagRed.png"))) {
							public void change(int x, int y) {
								if (bounds.contains(x + editOffX, y) && cursor != null && Main.cursor.getId().equals("erase")) {
									onTop = true;
								} else {
									onTop = false;
								}
							}
						}.setId("flag").setX(editOffX + cursor.getX()));
					}
					else if(cursor.getId().matches("boundary")){
						editor.addChild(0, new ImageRect(lastXD, lastYD, david.getWidth(), david.getHeight()){
							public void drawOff(Graphics g, int x, int y){
								g.setColor(Color.RED);
								g.fillRect((this.x + x) * PIXEL, (y + this.y) * PIXEL, PIXEL, david.getHeight() * PIXEL);
								g.fillRect((this.x + x + david.getWidth()) * PIXEL, (y + this.y) * PIXEL, PIXEL, PIXEL * david.getHeight());
								g.fillRect((this.x + x) * PIXEL, ((y + this.y) + david.getHeight() - 1) * PIXEL, PIXEL * david.getWidth(), PIXEL);
								g.fillRect((this.x + x) * PIXEL, (y + this.y) * PIXEL, PIXEL * david.getWidth(), PIXEL);
								for(ImageRect iR : children){
									if(iR != null)
										iR.drawOff(g, x + this.x, y + this.y);
								}
							}
						}.setId("boundary"));
					}
				} catch(IOException ep){
					ep.printStackTrace();
				}
			}
			editor.click(x - editOffX, y);
		}
		else if(level == 4){
     		storyboards.get(currBoard).click(x , y);
		}
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseDragged(MouseEvent e){
		int x = e.getX() / PIXEL;
		int y = e.getY() / PIXEL;
		lastXD = x;
		lastYD = y;
		int off = getOff();
		if(level == 3){
			if(gridMode && cursor != null && cursor.getId().equals("ground")){
				x /= ground.getWidth();
				y /= ground.getHeight();
				x *= ground.getWidth();
				y *= ground.getHeight();
			}
			if(cursor != null){
				cursor.setPoint(x - off, y);
				mouseClicked(e);
			}
		}
	}
	public void mouseMoved(MouseEvent e){
     	int x = e.getX() / PIXEL;
     	int y = e.getY() / PIXEL;
     	lastXD = x;
     	lastYD = y;
     	if(level == 0) {
			menu.hover(x, y);
		}
		else if(level == 2){
			controls.hover(x, y);
		}
		else if(level == 3) {
			editor.hover(x, y);
			if (cursor != null) {
				if (gridMode && cursor.getId().equals("ground")) {
					x = getOffWGround(e.getX());
					cursor.setPoint(x, y / ground.getWidth() * ground.getWidth());
				}else{
					cursor.setPoint(x, y);
				}
			}
		}
		else if(level == 4){
			storyboards.get(currBoard).hover(x , y);
		}
	}
	public static ImageRect setCursor(String str){
     	str = str.toLowerCase();
     	if(str == null){
     		cursor = null;
		}
		else if(str.equals("ground")){
     		cursor = new ImageRect(lastXD, lastYD, ground).setId("ground");
		}
		else if(str.equals("erase")){
     		cursor = new ImageRect(lastXD,lastYD,0,0).setId("erase");
		}
		else if(str.equals("flag")){
     		cursor = new ImageRect(lastXD, lastYD, WIN).setId("flag");
		}
		else if(str.equals("boundary")){
     		cursor = new ImageRect(lastXD, lastYD, david.getWidth(), david.getHeight()){
				public void drawOff(Graphics g, int x, int y){
					g.setColor(Color.RED);
					g.fillRect((this.x + x) * PIXEL, (y + this.y) * PIXEL, PIXEL, david.getHeight() * PIXEL);
					g.fillRect((this.x + x + david.getWidth()) * PIXEL, (y + this.y) * PIXEL, PIXEL, PIXEL * david.getHeight());
					g.fillRect((this.x + x) * PIXEL, ((y + this.y) + david.getHeight() - 1) * PIXEL, PIXEL * david.getWidth(), PIXEL);
					g.fillRect((this.x + x) * PIXEL, (y + this.y) * PIXEL, PIXEL * david.getWidth(), PIXEL);
					for(ImageRect iR : children){
						if(iR != null)
							iR.drawOff(g, x + this.x, y + this.y);
					}
				}
			}.setId("boundary");
		}
		return cursor;
	}

	//IMAGE METHODS
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
	public BufferedImage white(BufferedImage img){
     	int black = 0xFF000000;
     	int white = 0xFFFFFFFF;
     	for(int r = 0; r < img.getHeight(); r ++){
     		for(int c = 0; c < img.getWidth(); c++ ){
     			if(img.getRGB(c, r) == black) {
					img.setRGB(c, r, white);
     			}
			}
		}
		return img;
	}

	//ACTION LISTENER
	public void actionPerformed(ActionEvent e){
		repaint();
	}
}