import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*@davidagtz*/
public class Main extends JPanel implements ActionListener, KeyListener{
     public static long serialVersionUID = 0L;
     static JFrame frame;
     static final int WIDTH = 798, HEIGHT = 600, PIXEL = 6;
     static HashMap<Character, BufferedImage> font = new HashMap<>();
     static Timer timer;
     static Player david, diego, jakob;
     static BufferedImage background;
     static final Set<Integer> pressed = new HashSet<>();
     public void paintComponent(Graphics g){
          paintBackground(g);
          diego.draw(g);
          david.draw(g);
          jakob.draw(g);
     }
     public void paintBackground(Graphics g){
          g.setColor(Color.CYAN);
          g.fillRect( 0, 0, WIDTH, HEIGHT);
     }
     public Main(String title){
          //get Background
          try{
               background = ImageIO.read(new File("res/background.png"));
          } catch(IOException e){
               e.printStackTrace();
          }

          //Make Players
          try{
               david = new Player(ImageIO.read(new File("res/faces/david.png")));
               diego = new Player(ImageIO.read(new File("res/faces/diego.png")));
               jakob = new Player(ImageIO.read(new File("res/faces/jakob.png")));
               david.move(0,david.faces(0).getHeight());
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
                    diego.move(-1, 0);
                    diego.setRunning(-1);
               }
               else if (code == KeyEvent.VK_RIGHT) {
                    diego.move(1, 0);
                    diego.setRunning(1);
               }
               if (code == KeyEvent.VK_A) {
                    david.move(-1, 0);
                    david.setRunning(-1);
               }
               else if (code == KeyEvent.VK_D) {
                    david.move(1, 0);
                    david.setRunning(1);
               }
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