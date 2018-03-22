import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player {
     ArrayList<ArrayList<Rectangle>> body = new ArrayList<>();
     BufferedImage face;
     ArrayList<BufferedImage> bodies = new ArrayList<>();
     int r;
     int c;
     public Player(BufferedImage f){
          face = f;
          bodies.add();
          bodies.add();
          bodies.add();
     }
     int move(int dirx, int diry){
          r+=diry;
          c+=dirx;
     }
     
}
