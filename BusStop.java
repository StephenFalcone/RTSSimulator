
import java.util.ArrayList;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Serializable;


/** Class that represents a BusStop on a 2D map */
public class BusStop extends Location
{
  /** Stores a list of lines that stop at this busstop */
  protected ArrayList<Line> lines;
  private static Image busStopImage;
  
  /** Initializes lines to an empty list */
  public BusStop()
  {
    lines = new ArrayList<Line>();     
  }
   
   
  /** Updates name, x, and y and initializes lines to an empty list */ 
  public BusStop(String name, int x, int y)
  {
     super(name, x, y);
     lines = new ArrayList<Line>();
  }

  /** Updates name, x, and y and initializes lines with numLines Line objects in array lines */ 
  public BusStop(String name, int x, int y, Line[] lines, int numLines)
  {
     super(name, x, y);
     this.lines = new ArrayList<Line>();
     for(int i=0; i<numLines; i++)
        this.lines.add(lines[i]);
  }
  
  /** Adds line l to lines */
  public void addLine(Line l)
  {
	 if (lines == null)
		lines = new ArrayList<Line>(); 
     if (!lines.contains(l))
        lines.add(l);
  } 
  
  /** Prints the info of the lines that stop at this busstop */
  public void printLineInfo(OutputStream o) throws IOException
  {
     o.write(getLineInfo().getBytes());
  }
 
  /** Returns a string that contains information about all the lines 
    * that stop at this bus stop
    */
  public String getLineInfo()
  {
     ByteArrayOutputStream outputString = new ByteArrayOutputStream();
     PrintWriter output = new PrintWriter(outputString, true);
     String s1 = super.toString();
     output.println("Following lines stop at this bus stop");
     for(int i=0; i<lines.size(); i++)
        output.print(lines.get(i) + ((i == lines.size()-1) ? "\n" : ","));
     output.println();   
     return s1 + "\n" + outputString.toString();   
  }
  
  public static void setImage(Image image)
  {
     busStopImage = image;
  }
  
  public Image getImage() 
  { 
	  System.out.println("BusStop's getImage");
	  return busStopImage; 
  }
  
  
}
