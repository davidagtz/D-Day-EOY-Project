import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.util.*;

public class Main extends JPanel{
     public static long serialVersionUID = 0l;
     static JFrame frame;
     static final int WIDTH = 800;
     static final int HEIGHT = 600;
     static HashMap<Character, BufferedReader> font = new HashMap<>();
     public void paintComponent(Graphics g){

     }
     public Main(String title){
          frame = new JFrame(title);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setResizable(false);
          this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
          frame.add(this);
          frame.pack();
          frame.setVisible(true);
     }
     public static void main(String[] a){
          new Main("D-Day");
     }
     public void drawString(Graphics g, int h, String str){

     }
}