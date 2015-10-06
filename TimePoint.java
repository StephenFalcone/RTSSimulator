import java.awt.Image;


/** Class that represents a bus stop that is associated with a time point in the line schedule */
public class TimePoint extends BusStop 
{
  private static Image timePointImage;
    
  public TimePoint()
  {     
  }
  
  /** Initializes name, x, and y using the parameters by calling super*/ 
  public TimePoint(String name, int x, int y)
  {
     super(name, x, y);
  }

  /** Initializes name, x, and y, and lines using the parameters by calling super*/ 
  public TimePoint(String name, int x, int y, Line[] lines, int numLines)
  {
     super(name, x, y, lines, numLines);
  }
  
  public static void setImage(Image image)
  {
	  timePointImage = image;
  }
  
  public Image getImage()
  {
	  System.out.println("Timpoint's getImage");
      return timePointImage;
  }
  
}
