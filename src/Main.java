import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.util.*;
import java.io.*;

public class Main extends JPanel implements ActionListener{
     public static long serialVersionUID = 0l;
     static JFrame frame;
     static final int WIDTH = 800;
     static final int HEIGHT = 600;
     static HashMap<Character, BufferedImage> font = new HashMap<>();
     static final int PIXEL = 10;
     static Timer timer;

     public void paintComponent(Graphics g){
          drawString(g, 10, 10, 5, "Diego like fat \n cock");
     }

     public Main(String title){
          //Initialize Font
          try {
               for (int i = 'a'; i <= 'z'; i++)
                    font.put((char) i, ImageIO.read(new File("font/"+(char) i + ".png")));
          } catch (IOException e){
               e.printStackTrace();
               System.exit(0);
          }

          //make frame and set up
          frame = new JFrame(title);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setResizable(false);
          this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
          frame.add(this);
          frame.pack();
          frame.setVisible(true);

          //Timer setup for repaint()
          timer = new Timer(20, this);
          timer.start();

     }
     public static void main(String[] a){
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
}