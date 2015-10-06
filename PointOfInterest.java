

import java.awt.Image;

public class PointOfInterest extends Location {

	 protected String description;
	 private static Image pOfImage;
	 
	 public PointOfInterest()
	 {
		
	 }
	 
	 public PointOfInterest(String name, int x, int y, String description)
	 {
		 super(name, x, y);
		 this.description = description;
	 }
	 
	 public Image getImage()
	 {
		 return pOfImage;
	 }
	 
	 public static void setImage(Image image)
	 {
	     pOfImage = image;
	 }
	 
	 public String getDescription() 
	 {
		 return description;
	 }
	 
	 public void setDescription(String description)
	 {
		 this.description = description;
	 }
}
