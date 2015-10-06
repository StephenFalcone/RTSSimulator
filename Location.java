
import java.lang.Math;
import java.io.Serializable;
import java.awt.Graphics;
import java.awt.Image;

/** Class that represents a location on a 2D map */
public abstract class Location implements Drawable, Serializable
{
  protected int x;
  protected int y;
  protected String name;
  private static Image image;

 
  /** Set x and y to 0 and name to empty String */
  public Location()
  {
    x = 0;
    y = 0;
    name = "";
  }
  
  public Location(String name, int x, int y)
  {
    this.name = name;
    this.x = x;
    this.y = y;
  }
  
  public String getName() { return name; }
  
  public void setName(String name) { this.name = name; }
  
  public int getX() { return x; }

  public void setX(int x) { this.x = x; } 
    
  public int getY() { return y; }
  
  public void setY(int y) { this.y = y; }
  
  public static void setImage(Image image) { Location.image = image;}
  
  public abstract Image getImage();
  
  /*
  public double distance(Location other)
  {
     double xdiff = this.x - other.x;
     double ydiff = this.y - other.y;
     return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
  }*/

 
  /** Returns the String "name at (x,y)" */
  public String toString()
  {
    return name + " at (" + x +"," + y + ")"; 
  }

  /** Prints "name at (x,y)" */
  public void printInfo()
  {
     System.out.println(name + " at (" + x +"," + y + ")");
  }
  
  public void draw(Graphics g, int width, int height)
  {
	  g.drawImage(getImage(), x,  y, width,  height, null);
  }
     
}
