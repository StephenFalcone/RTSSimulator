


import javax.imageio.ImageIO;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.*;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** * @author Tuba
 *
 */
public class MapApp extends JFrame implements ActionListener {

	/** Contains radio buttons for various RTS Items */
	JPanel selectionPanel;
	/** Map is drawn on this panel */
	JPanel drawingPanel;
	/** Contains the actionListPanel at the NORTH and the customPanel at the CENTER */
	JPanel actionPanel;
	/** Contains a JComboBox for actions and the UIelements for the parameters */
	JPanel actionListPanel;
	/** The contents of this panel changes based on the chosen RTS Item and the action*/
	JPanel customPanel; 

	/** All created RTS Items are stored in this data structure as (name of the object, the object) pair */
	HashMap<String, Location> points = new HashMap();	
	HashMap<String, Boolean> visible = new HashMap();
	HashMap<String, Line> lines = new HashMap();

	/** Each group of action parameter components is stored in a separate ArrayList of JComponents. 
	 *  This data structure stores (rtsItem + actionMode, ArrayList of JComponent)  pairs  
	 */
	HashMap<String, ArrayList<JComponent>> actionComponents = new HashMap();

	/** Array of action types for location (busstop etc) */ 
	String[] locationActions = {"New", "Show", "Hide", "Move", "Write Line Info"};
	/** Array of action types for line */
	String[] lineActions = {"New", "Add BusStop", "Write Schedule"};
	/** Array of action types for point of interest */
	String[] pofActions = {"New", "Move", "Show", "Hide"};
	/* Stores the RTS Item currently selected. Default is BusStop. */
	String rtsItem = "BusStop";
	/** Stores the selected action type. Default is New. */
	String actionMode = "New";

	/** This gets concatenated with rtsItem to store and fetch the relevant action list combo box */
	String actionListKeyword = "ActionList";

	/** Used to enter name of the map item */
	JTextField name;
	/** Used to enter x coordinate of the map item */
	JTextField x;
	/** Used to enter y coordinate of the map item */
	JTextField y;
	/** Used to enter the duration when adding next bus stop to a line */
	JTextField duration;
	/** Used to enter the hours and minutes of first and last service of a line */
	JTextField firstH, firstM, lastH, lastM;
	/** Used to enter the period by which the bus schedule changes */
	JTextField period;

	/** Used to specify whether a busstop is also a time point */
	JCheckBox timePoint;

	/** Action list combo box for location type map items */
	JComboBox locationActionList;	 
	/** Action list combo box for line */
	JComboBox lineActionList;
	JComboBox pofActionList;

	/** When a BusStop object gets created, its name is added to this list */
	JComboBox busStopList;
	/** When a BusStop object gets created, its name is added to this list */
	JComboBox busStopListDep;
	/** When a BusStop object gets created, its name is added to this list */
	JComboBox busStopListDest;
	/** When a Line object gets created, its name is added to this list */
	JComboBox lineList;
	JComboBox pofList;

	ArrayList<Integer> lineUpperX = new ArrayList<Integer>();
	ArrayList<Integer> lineUpperY = new ArrayList<Integer>();
	ArrayList<Integer> lineBottomX = new ArrayList<Integer>();
	ArrayList<Integer> lineBottomY = new ArrayList<Integer>();

	ArrayList<Polygon> polygons = new ArrayList<Polygon>();

	boolean drawingStarted = false;

	void clearPolygon()
	{
		lineUpperX.clear();
		lineUpperY.clear();
		lineBottomX.clear();
		lineBottomY.clear();
		drawingStarted = false;
		drawingPanel.repaint();
	}


	/** For schedule pane */
	Dimension paneSize = null;

	/** Holds the image of the map */
	BufferedImage mapImage;

	final static int bsImageWidth = 20;
	final static int bsImageHeight = 20;

	/** For handling moving bus stop objects around */
	int lastX, lastY;
	ArrayList<Location> movingObjs = new ArrayList();

	String fileName = null;
	JFileChooser fileChooser = new JFileChooser();
	private JTextField description;

	HashMap<String, Location> labels = new HashMap(); 

	/** Inner class to handle events fired from RTS Item radio buttons */
	class RtsItemListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			// If change in selection
			if (rtsItem.compareTo(e.getActionCommand()) != 0)
			{	 
				actionMode = "New";
				if (rtsItem.equals("BusStop"))
					locationActionList.setSelectedIndex(0);
				if (rtsItem.equals("Line"))
					lineActionList.setSelectedIndex(0);	
				if (rtsItem.equals("Point Of Interest"))
					pofActionList.setSelectedIndex(0);
			}		
			// Stores the type of the object that is chosen
			rtsItem = e.getActionCommand();
			System.out.println(rtsItem);
			updateCustomComponent();
		}
	}

	/** Inner class to handle events fired from action list */
	class RtsItemActionListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() == ItemEvent.SELECTED) 
			{	
				actionMode = (String) e.getItem();
				System.out.println(actionMode);
				updateCustomComponent();
			}   
		}
	}

	private boolean covers(Location loc, int x, int y)
	{
		return (x>= loc.getX() && x <= loc.getX() + bsImageWidth) && 
				(y>= loc.getY() && y <= loc.getY() + bsImageHeight); 
	}


	class RTSMouseListener extends MouseAdapter
	{

		public void mouseClicked(MouseEvent e)
		{
			Set<Map.Entry<String, Boolean>> visibleSet = visible.entrySet();
			for(Map.Entry m: visibleSet)
			{
				if ((Boolean)m.getValue())
				{
					Location loc = points.get(m.getKey());   
					if (covers(loc, lastX, lastY))				 
					{
						if (loc instanceof BusStop) 
							busStopList.setSelectedItem(m.getKey());
						else if (loc instanceof PointOfInterest)
						{
							JOptionPane.showMessageDialog(MapApp.this, ((PointOfInterest)loc).getDescription());
						}
					}

				}
			}
			if (drawingStarted && e.getButton() == MouseEvent.BUTTON3)
			{
				polygons.add(generatePolygon());
				clearPolygon();
			}
		}



		public void mousePressed(MouseEvent e)
		{ // drawMode
			System.out.println("BUTTON1=" + MouseEvent.BUTTON1 + " currently=" + e.getButton()); 
			if (e.getButton() == MouseEvent.BUTTON1)
			{	
				drawingStarted = true;	
				System.out.println("Drawing starts at (" + lastX + "," + lastY + ")");
				lineUpperX.add(lastX); 
				lineUpperY.add(lastY - 5);
				//lineX.add(lastX);
				//lineY.add(lastY);
				lineBottomX.add(lastX);
				lineBottomY.add(lastY + 5);}

			lastX = e.getX();
			lastY = e.getY();
			System.out.println("Let's move starting at (" + lastX + "," + lastY + ")");
			Set<Map.Entry<String, Boolean>> visibleSet = visible.entrySet();
			for(Map.Entry m: visibleSet)
			{
				if ((Boolean)m.getValue())
				{
					Location loc = points.get(m.getKey());   
					if (covers(loc, lastX, lastY))				 
						movingObjs.add(loc);

				}
			}	

		}

		public void mouseReleased(MouseEvent e)
		{
			System.out.println("Move ended at (" + e.getX() + "," + e.getY() + ")");
			movingObjs.clear();

			if (e.getButton() == MouseEvent.BUTTON1)
			{
				clearPolygon();
			}
		}
	}
	Polygon generatePolygon()
	{
		if (lineUpperX.size() > 0)
		{	   
			int[] xP = new int[2 * lineUpperX.size() + 1];
			int[] yP = new int[2 * lineUpperY.size() + 1];
			int i;
			for(i=0; i < lineUpperX.size(); i++)
			{
				xP[i] = lineUpperX.get(i);
				yP[i] = lineUpperY.get(i);       		
			}
			for(int j=i=lineBottomX.size() - 1; i >= 0; i--)
			{
				xP[j + lineBottomX.size() - i] = lineBottomX.get(i);
				yP[j + lineBottomX.size() - i] = lineBottomY.get(i); 
			}
			xP[2 * lineUpperX.size()] = xP[0];
			yP[2 * lineUpperX.size()] = yP[0];

			return new Polygon(xP, yP, xP.length);
		}
		else return null;
	}
	class RTSMouseMotionListener extends MouseMotionAdapter
	{
		public void mouseDragged(MouseEvent e)
		{

			int x = e.getX();
			int y = e.getY();
			for(int i=0; i < movingObjs.size(); i++)
			{
				Location loc = movingObjs.get(i);
				loc.setX(loc.getX() + x - lastX);
				loc.setY(loc.getY() + y - lastY);
			}

			//drawMode
			if (drawingStarted=true) 
				System.out.println("Dragging at (" + x + "," + y);
			if (Math.abs(lastX - x) == 0)
			{
				lineUpperX.add(x + 5);
				lineUpperY.add(y);
				lineBottomX.add(x - 5);
				lineBottomY.add(y);					 
			}
			else {
				lineUpperX.add(x);
				lineUpperY.add(y - 5);
				lineBottomX.add(x);
				lineBottomY.add(y + 5);
			}	

			lastX = x;
			lastY = y;
			drawingPanel.repaint();

		}

		public void mouseMoved(MouseEvent e)
		{
			lastX = e.getX();
			lastY = e.getY();
			System.out.println("Mouse moving starting at (" + lastX + "," + lastY + ")");
			Set<Map.Entry<String, Location>> rtsTupleSet = points.entrySet();
			int change = 0;
			for(Map.Entry m: rtsTupleSet)
				if (covers((Location)m.getValue(), lastX, lastY))
				{  
					change++;  
					labels.put((String)m.getKey(), (Location)m.getValue());
				}  
				else 
				{		 
					if (labels.remove(m.getKey()) != null)
						change++; 
				}		 
			if (change > 0)
				drawingPanel.repaint(); 


		}

	}


	class RTSLoadButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int value = fileChooser.showOpenDialog(MapApp.this);
			if (value == JFileChooser.APPROVE_OPTION)
			{
				System.out.println("Reading RTS Items from " + fileChooser.getSelectedFile().getName());
				readRTSItems(fileChooser.getSelectedFile().getName());
				drawingPanel.repaint();
			}
		}
	}

	class RTSSaveButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			try { 
				int value = fileChooser.showSaveDialog(MapApp.this);
				if (value == JFileChooser.APPROVE_OPTION)
				{
					System.out.println("Writing RTS Items to " + fileChooser.getSelectedFile().getName());
					writeRTSItems(fileChooser.getSelectedFile().getName());
				}
			}
			catch(IOException exc)
			{
				System.out.println("Error saving RTS Items");
				exc.printStackTrace();
			}
		}
	}

	class RTSUpdateButtonListener implements ActionListener
	{
		JPanel panel;
		String lineName;

		public RTSUpdateButtonListener(String lineName, JPanel panel)
		{
			this.lineName = lineName; 
			this.panel = panel;    
		}

		public void actionPerformed(ActionEvent e)
		{
			System.out.println("Updating view to display line " + lineName + "'s schedule");
			panel.removeAll();
			Line line = lines.get(lineName);			 
			JScrollPane pane = new JScrollPane(new JTextArea(line.getSchedule()), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			pane.setPreferredSize(paneSize);
			panel.add(pane);
			panel.revalidate();
			panel.repaint();
		}
	}

	/** Creates a UI that consistes of 3 panels.
	 * The left panel provides choices for RTS Items.
	 * The middle panel provides choices for action types and the relevant data fields.
	 * The right panel draws the map items based on the chosen map item and action type.
	 */ 
	public MapApp(String s)  {
		super(s);

		fileChooser.setCurrentDirectory(new File("C:\\Users\\Tuba\\courses\\cop3503SP13\\projects\\project5\\RTSMapApp"));

		// Create the left panel that holds radio buttons for shape types
		selectionPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		selectionPanel.setBorder(new TitledBorder("Items"));
		// By default rectangle button will be selected, determined by the boolean parameter to the constructor
		JRadioButton busStopButton = new JRadioButton("BusStop", true);
		busStopButton.setMnemonic('B');
		JRadioButton lineButton = new JRadioButton("Line", false);
		lineButton.setMnemonic('L');
		JRadioButton pofButton = new JRadioButton("Point Of Interest", false);
		pofButton.setMnemonic('P');
		// ButtonGroup class helps us ensure only one shape can be selected at a time
		ButtonGroup group = new ButtonGroup();
		group.add(busStopButton);
		group.add(lineButton);
		group.add(pofButton);

		RtsItemListener rtsItemListener = new RtsItemListener();
		/** rtsItemListener object receives events from busStopButton */
		busStopButton.addActionListener(rtsItemListener);
		/** rtsItemListener object receives events from lineButton */
		lineButton.addActionListener(rtsItemListener);
		pofButton.addActionListener(rtsItemListener);
		selectionPanel.add(busStopButton); 
		selectionPanel.add(lineButton);
		selectionPanel.add(pofButton);

		try {
			mapImage  = ImageIO.read(new File("campusMap.jpg"));
			Image busStopImage = ImageIO.read(new File("RTS.jpg"));
			BusStop.setImage(busStopImage);
			Image timePointImage = ImageIO.read(new File("circles.png"));
			TimePoint.setImage(timePointImage);
			Image pofImage = ImageIO.read(new File("pointOfInterest.jpg"));
			PointOfInterest.setImage(pofImage); 
		}
		catch (IOException e) { System.out.println("Could not load the map image");}

		// A panel for drawing the shapes that are created and for redrawing any changes related to them
		drawingPanel = new JPanel() {
			public void paintComponent(Graphics g)
			{
				Color bfd = g.getColor();
				g.setColor(Color.black);
				for(Polygon p: polygons)
					g.fillPolygon(p);
				Polygon currentPolygon = generatePolygon();
				if (currentPolygon != null)
					g.fillPolygon(currentPolygon);
				g.setColor(bfd);

				super.paintComponent(g);

				int width = getWidth();
				int height = getHeight(); 

				if (width == 0 || height == 0)
				{
					width = (int)getPreferredSize().getWidth();
					height = (int)getPreferredSize().getHeight();
				}
				g.clearRect(0,  0,  getWidth(),  getHeight());
				g.drawImage(mapImage, 0, 0, width, height, null);

				Set<Map.Entry<String, Boolean>> visibleSet = visible.entrySet();
				for(Map.Entry tuple: visibleSet)
					if ((Boolean)tuple.getValue()) 
					{	 
						System.out.println(tuple.getKey()); 
						points.get((String)tuple.getKey()).draw(g, bsImageWidth, bsImageHeight);

					}   

				FontMetrics fontMetrics = g.getFontMetrics();
				Color prev = g.getColor();
				Set<Map.Entry<String, Location>> labelSet = labels.entrySet();
				for(Map.Entry m: labelSet)
				{ 	 
					int h = fontMetrics.getHeight();
					int w = fontMetrics.stringWidth((String)m.getKey());
					int x = ((Location)m.getValue()).getX();
					int y = ((Location)m.getValue()).getY();
					if (y > getHeight())
						y =  getHeight();
					else if (y - h < 0)
						y = h;
					if (x + w > getWidth())
						x = getWidth() - w;
					else if (x < 0)
						x = 0;
					g.setColor(new Color(255, 215, 0));
					g.fillRect(x, y - h, w, h);
					g.setColor(Color.black);
					g.drawString((String)m.getKey(), x, y);

				} 	 
				g.setColor(prev);
			}

			public Dimension getPreferredSize()
			{
				return new Dimension(600, 500);
			}


		};
		drawingPanel.setBorder(new TitledBorder("Map"));
		drawingPanel.add(new JLabel("Map"));
		drawingPanel.setPreferredSize(new Dimension(50, 50)); 
		drawingPanel.addMouseListener(new RTSMouseListener());
		drawingPanel.addMouseMotionListener(new RTSMouseMotionListener());

		JPanel northPanel = new JPanel();
		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(new RTSLoadButtonListener());
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new RTSSaveButtonListener());
		northPanel.add(loadButton);
		northPanel.add(saveButton);

		// This middle panel lets users choose an action on the RTS Items
		actionPanel = new JPanel(new BorderLayout(5,5));
		actionPanel.setBorder(new TitledBorder("Actions"));
		actionListPanel = new JPanel();
		customPanel = new JPanel();
		actionPanel.add(actionListPanel, BorderLayout.NORTH);
		actionPanel.add(customPanel, BorderLayout.CENTER);
		/** Initializes the customPanel that holds the fields for parameters */

		JPanel centerPanel = new JPanel(new BorderLayout(5,5));
		centerPanel.add(actionPanel, BorderLayout.CENTER);
		centerPanel.add(selectionPanel, BorderLayout.WEST); 
		centerPanel.add(drawingPanel, BorderLayout.EAST);

		setLayout(new BorderLayout(5,5));
		add(northPanel, BorderLayout.NORTH);   
		add(centerPanel, BorderLayout.CENTER);
		makeCustomPanelComponents();      

	}


	public void makeCustomPanelComponents()
	{	

		/** For displaying the actions for Location type objects */
		locationActionList = new JComboBox(locationActions); 
		/** an RtsItemActionListener object receives events from locationActionList */
		locationActionList.addItemListener(new RtsItemActionListener());
		JPanel locActionPanel = new JPanel();
		locActionPanel.add(locationActionList);
		ArrayList<JComponent> bsActionList = new ArrayList();
		bsActionList.add(locActionPanel);        
		actionComponents.put("BusStop" + actionListKeyword, bsActionList);

		/** For diplaying the actions for Line objects */ 
		lineActionList = new JComboBox(lineActions);
		/** an RtsItemActionListener object receives events from lineActionList */
		lineActionList.addItemListener(new RtsItemActionListener());
		JPanel lineActionPanel = new JPanel();
		lineActionPanel.add(lineActionList);
		ArrayList<JComponent> lnActionList = new ArrayList();
		lnActionList.add(lineActionPanel);
		actionComponents.put("Line" + actionListKeyword, lnActionList);

		/** For diplaying the actions for Point of Interest objects */ 
		pofActionList = new JComboBox(pofActions);
		/** an RtsItemActionListener object receives events from pofActionList */
		pofActionList.addItemListener(new RtsItemActionListener());
		JPanel pofActionPanel = new JPanel();
		pofActionPanel.add(pofActionList);
		ArrayList<JComponent> pofSActionList = new ArrayList();
		pofSActionList.add(pofActionPanel);
		actionComponents.put("Point Of Interest" + actionListKeyword, pofSActionList);


		/** For entering the name of the map item */
		JLabel nLabel = new JLabel("Name");
		name = new JTextField(10);
		JPanel nPanel = new JPanel();
		nPanel.add(nLabel);
		nPanel.add(name);

		JLabel dLabel = new JLabel("Duration");
		duration = new JTextField("Duration", 3);
		JPanel dPanel = new JPanel();
		dPanel.add(dLabel);
		dPanel.add(duration);




		/** For entering the x and y coordinates */
		JLabel xLabel = new JLabel("x");
		x = new JTextField(4);
		JPanel xPanel = new JPanel();
		xPanel.add(xLabel);
		xPanel.add(x);
		JLabel yLabel = new JLabel("y");
		y = new JTextField(4);
		JPanel yPanel = new JPanel();
		yPanel.add(yLabel);
		yPanel.add(y);

		firstH = new JTextField(4);
		firstM = new JTextField(4);
		JPanel firstPanel = new JPanel();
		firstPanel.add(new JLabel("First"));
		firstPanel.add(firstH);
		firstPanel.add(new JLabel(":"));
		firstPanel.add(firstM);

		lastH = new JTextField(4);
		lastM = new JTextField(4);
		JPanel lastPanel = new JPanel();
		lastPanel.add(new JLabel("Last"));
		lastPanel.add(lastH);
		lastPanel.add(new JLabel(":"));
		lastPanel.add(lastM);


		period = new JTextField(4);
		JPanel periodPanel = new JPanel();
		periodPanel.add(new JLabel("Period"));
		periodPanel.add(period);


		/** For checking or unchecking timePoint feature */
		JLabel timePointLabel = new JLabel("Time Point?");
		timePoint = new JCheckBox("", false);
		timePoint.addActionListener((ActionListener)this);
		JPanel tPanel = new JPanel();
		tPanel.add(timePointLabel);
		tPanel.add(timePoint);

		/** For doing the chosen action on the selected object based on the entered parameter values */
		JButton ok = new JButton("OK");
		ok.setBackground(Color.blue);
		/** See actionPerformed method to see the action taken */
		ok.addActionListener((ActionListener)this);

		/** As objects get created their names will be added to the corresponding list */
		String[] busStopNames = {};
		busStopList = new JComboBox(busStopNames);
		busStopListDep = new JComboBox(busStopNames);
		busStopListDest = new JComboBox(busStopNames);
		String[] lineNames = {};
		lineList = new JComboBox(lineNames);
		JPanel lineListPanel = new JPanel();
		lineListPanel.add(new JLabel("Line"));
		lineListPanel.add(lineList);
		JPanel bsListPanel = new JPanel();
		bsListPanel.add(new JLabel("BusStop"));
		bsListPanel.add(busStopList);
		JPanel bsListPanelDep = new JPanel();
		bsListPanelDep.add(new JLabel("Departure"));
		bsListPanelDep.add(busStopListDep);
		JPanel bsListPanelDest = new JPanel();
		bsListPanelDest.add(new JLabel("Destination"));
		bsListPanelDest.add(busStopListDest);
		JPanel pofListPanel = new JPanel();
		String[] pofNames = {};
		pofList = new JComboBox(pofNames);
		pofListPanel.add(new JLabel("Point Of Interests"));
		pofListPanel.add(pofList);     

		if ((BusStop)points.get((String)busStopList.getSelectedItem()) instanceof TimePoint)
			dPanel.setVisible(true);
		//else dPanel.setVisible(false);

		String lol= null;
		if(busStopNames.length>=2){
			lol = busStopNames[busStopNames.length-1];}
		else lol = "DepartureLocation";
		JLabel aLabel= new JLabel("Will be added after " + lol);
		JPanel aPanel = new JPanel();
		aPanel.add(aLabel);


		/** Custom panel components for creating a new busstop */
		ArrayList<JComponent> newBSComponents = new ArrayList();
		newBSComponents.add(nPanel);
		newBSComponents.add(xPanel);
		newBSComponents.add(yPanel);
		newBSComponents.add(tPanel);      
		newBSComponents.add(ok);
		actionComponents.put("BusStopNew", newBSComponents);

		/** Custom panel components for moving a busstop */
		ArrayList<JComponent> moveBSComponents = new ArrayList();
		moveBSComponents.add(bsListPanel);
		moveBSComponents.add(xPanel);
		moveBSComponents.add(yPanel);
		moveBSComponents.add(ok);
		actionComponents.put("BusStopMove", moveBSComponents);     

		/** Custom panel components for showing a busstop on the map */
		ArrayList<JComponent> showBSComponents = new ArrayList();
		showBSComponents.add(bsListPanel);
		showBSComponents.add(ok);
		actionComponents.put("BusStopShow", showBSComponents);

		/** Custom panel components for hiding a bus stop from the map display */
		ArrayList<JComponent> hideBSComponents = new ArrayList();
		hideBSComponents.add(bsListPanel);
		hideBSComponents.add(ok);
		actionComponents.put("BusStopHide", hideBSComponents);

		/** Custom panel components for showing the lines that stop at a busstop */
		ArrayList<JComponent> showLnsComponents = new ArrayList();
		showLnsComponents.add(bsListPanel);
		showLnsComponents.add(ok);
		actionComponents.put("BusStopWrite Line Info", showLnsComponents);       

		/** Custom panel components for creating a bus line */
		ArrayList<JComponent> newLNComponents = new ArrayList();
		newLNComponents.add(nPanel);
		newLNComponents.add(bsListPanelDep);
		newLNComponents.add(bsListPanelDest);
		newLNComponents.add(firstPanel);
		newLNComponents.add(lastPanel);
		newLNComponents.add(periodPanel);
		newLNComponents.add(ok);
		actionComponents.put("LineNew", newLNComponents);

		/** Custom panel components for adding a bus stop to a line */
		ArrayList<JComponent> addBSComponents = new ArrayList();
		addBSComponents.add(lineListPanel);
		addBSComponents.add(bsListPanel);
		addBSComponents.add(dPanel);
		addBSComponents.add(aPanel);
		addBSComponents.add(ok);
		actionComponents.put("LineAdd BusStop", addBSComponents); 

		/** Custom panel components for writing the schedule of a line to a file */
		ArrayList<JComponent> writeSchComponents = new ArrayList();
		writeSchComponents.add(lineListPanel);
		writeSchComponents.add(ok);
		actionComponents.put("LineWrite Schedule", writeSchComponents);           

		JPanel descPanel = new JPanel();
		descPanel.add(new JLabel("Description "));
		description = new JTextField("                     ");
		descPanel.add(description);

		ArrayList<JComponent> newPOFComponents = new ArrayList();
		newPOFComponents.add(nPanel);
		newPOFComponents.add(xPanel);
		newPOFComponents.add(yPanel);
		newPOFComponents.add(descPanel);
		newPOFComponents.add(ok);
		actionComponents.put("Point Of InterestNew", newPOFComponents);

		ArrayList<JComponent> movePOFComponents = new ArrayList();
		movePOFComponents.add(pofListPanel);
		movePOFComponents.add(xPanel);
		movePOFComponents.add(yPanel);
		movePOFComponents.add(ok);
		actionComponents.put("Point Of InterestMove", movePOFComponents);     

		/** Custom panel components for showing a point of interest on the map */
		ArrayList<JComponent> showPOFComponents = new ArrayList();
		showPOFComponents.add(pofListPanel);
		showPOFComponents.add(ok);
		actionComponents.put("Point Of InterestShow", showPOFComponents);

		/** Custom panel components for hiding a point of interest from the map display */
		ArrayList<JComponent> hidePOFComponents = new ArrayList();
		hidePOFComponents.add(pofListPanel);
		hidePOFComponents.add(ok);
		actionComponents.put("Point Of InterestHide", hidePOFComponents);       

	}

	private void resetFields()
	{
		name.setText("");
		x.setText("");
		y.setText("");
		duration.setText("");
		period.setText("");
		firstH.setText("");
		firstM.setText("");
		lastH.setText("");
		lastM.setText("");
		timePoint.setSelected(false);

	}

	/** Removes the UI components in customPanel and adds the new components based on the selected shape and the action type */
	private void updateCustomComponent()
	{
		resetFields();	

		actionListPanel.removeAll();
		actionListPanel.add(actionComponents.get(rtsItem + actionListKeyword).get(0), BorderLayout.NORTH);
		actionListPanel.revalidate();
		actionListPanel.repaint();

		customPanel.removeAll();
		ArrayList<JComponent> list = actionComponents.get(rtsItem + actionMode);
		for(int i=0; i < list.size(); i++)
			customPanel.add(list.get(i));
		// Updates the view of the customPanel with new components
		customPanel.revalidate();
		customPanel.repaint();


	}


	/** Handles the OK button events */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().compareTo("OK") == 0)
			doAction();
	}

	private int grabInt(JTextField field)
	{
		return Integer.parseInt(field.getText().trim());
	}

	public void doAction()
	{
		System.out.println(rtsItem + actionMode);	   
		String command = rtsItem + actionMode;
		if (command.equals("BusStopNew"))
		{
			String bsName = name.getText().trim();
			BusStop bs;
			if (timePoint.isSelected())
				bs = new TimePoint(bsName, grabInt(x), grabInt(y)); 	
			else
				bs = new BusStop(bsName, grabInt(x), grabInt(y));
			points.put(bsName, bs);
			visible.put(bsName, true);
			busStopList.addItem(bsName);
			busStopListDep.addItem(bsName);
			busStopListDest.addItem(bsName);
			drawingPanel.repaint();
		}
		if (command.equals("Point Of InterestNew"))
		{
			String pofName = name.getText().trim();
			String desc = description.getText().trim();
			PointOfInterest pof = new PointOfInterest(pofName, grabInt(x), grabInt(y), desc);
			points.put(pofName, pof);
			visible.put(pofName, true);
			pofList.addItem(pofName);
			drawingPanel.repaint();
		}

		else if (command.equals("BusStopMove") || command.equals("Point Of InterestMove"))
		{
			Location loc; 
			if (command.equals("BusStopMove"))
				loc = points.get((String)busStopList.getSelectedItem());
			else // Point Of InterestMove
				loc = points.get((String)pofList.getSelectedItem());	
			loc.setX(grabInt(x));
			loc.setY(grabInt(y));
			drawingPanel.repaint();
		}
		else if (command.equals("BusStopShow") || command.equals("Point Of InterestShow"))
		{
			String locName;
			if (command.equals("BusStopShow"))
				locName = (String)busStopList.getSelectedItem();
			else // Point Of InterestMove
				locName = (String)pofList.getSelectedItem();
			System.out.println("Showing "  + locName);
			if (visible.containsKey(locName))
				visible.remove(locName);         	
			visible.put(locName,  true);
			drawingPanel.repaint();  	
		}
		else if (command.equals("BusStopHide") || command.equals("Point Of InterestHide"))
		{
			String locName;
			if (command.equals("BusStopHide"))
				locName = (String)busStopList.getSelectedItem();
			else // command.equals("Point Of InterestHide")
				locName = (String)pofList.getSelectedItem();
			System.out.println("Hiding "  + locName);
			if (visible.containsKey(locName)) 	
				visible.remove(locName);
			visible.put(locName, false);
			drawingPanel.repaint();

		}
		else if (command.equals("BusStopWrite Line Info"))
		{
			String bsName = (String)busStopList.getSelectedItem();
			BusStop bs = (BusStop)points.get(bsName);
			JTextArea infoText = new JTextArea(bs.getLineInfo());
			JScrollPane infoPane = new JScrollPane(infoText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JOptionPane.showMessageDialog(this, infoPane);
		}
		else if (command.equals("LineNew"))
		{
			String lnName = name.getText().trim();
			String depBSName = (String)busStopListDep.getSelectedItem();
			Location depBS = points.get(depBSName);
			String destBSName = (String)busStopListDest.getSelectedItem();
			Location destBS = points.get(destBSName);
			Time first = new Time(grabInt(firstH), grabInt(firstM));
			Time last = new Time(grabInt(lastH), grabInt(lastM));
			Line ln = new Line(lnName, depBS, destBS, first, last, grabInt(period));
			lines.put(lnName, ln);
			lineList.addItem(lnName);
			ln.addNextBusStop((BusStop)destBS, grabInt(duration));

		}
		else if (command.equals("LineAdd BusStop"))
		{
			BusStop bs = (BusStop)points.get((String)busStopList.getSelectedItem());
			System.out.println("Chosen " + busStopList.getSelectedItem());
			Line ln = lines.get((String)lineList.getSelectedItem());
			System.out.println("Chosen " + lineList.getSelectedItem());
			ln.addNextBusStop(bs, grabInt(duration));
		}
		else if (command.equals("LineWrite Schedule"))
		{
			Line line = lines.get((String)lineList.getSelectedItem());			 
			JFrame scheduleFrame = new JFrame();
			scheduleFrame.setSize(200,300);
			scheduleFrame.setLayout(new BorderLayout(3,3));
			JPanel schedulePanel = new JPanel();
			JScrollPane scrollPane = new JScrollPane(new JTextArea(line.getSchedule()), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			schedulePanel.add(scrollPane);
			JButton updateButton = new JButton("Update");
			updateButton.addActionListener(new RTSUpdateButtonListener((String)lineList.getSelectedItem(), schedulePanel));
			scheduleFrame.add(schedulePanel, BorderLayout.EAST);
			scheduleFrame.add(updateButton, BorderLayout.NORTH);
			schedulePanel.setPreferredSize(new Dimension(200, 300 - (updateButton.getHeight() + 5)));
			if (paneSize == null)
				paneSize = new Dimension(150, 300 - (updateButton.getHeight() + 100));
			scrollPane.setPreferredSize(paneSize);        			 
			scheduleFrame.setVisible(true);
		}

	}

	public void readRTSItems(String fileName) 
	{
		try { 
			this.fileName = fileName;
			if (!new File(fileName).exists())
			{	 
				new File(fileName).createNewFile();			   	
				return;
			}	
			else {			    
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(fileName));

				points = (HashMap<String, Location>)input.readObject();
				lines = (HashMap<String, Line>)input.readObject();
				visible = (HashMap<String, Boolean>)input.readObject();

				Set<Map.Entry<String, Location>> locSet = points.entrySet();
				for(Map.Entry<String, Location> m: locSet)
				{
					String locName = m.getKey();
					busStopList.addItem(locName);
					busStopListDep.addItem(locName);
					busStopListDest.addItem(locName);
				}

				Set<Map.Entry<String, Line>> lineSet = lines.entrySet();
				for(Map.Entry<String, Line> m: lineSet)
				{
					String lineName = m.getKey();
					lineList.addItem(lineName);
				}   

			}
		}	 		
		catch(IOException e) { e.printStackTrace(); System.out.println("Problem reading shapes");}
		catch(ClassNotFoundException e) { System.out.println("Class couldn't be loaded when reading Shape and subclass objects");}
	}

	public void writeRTSItems(String fileName) throws IOException
	{
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName));
		output.writeObject(points);
		output.writeObject(lines);
		output.writeObject(visible);
		output.close();
	}


	/**
	 *  Demonstrates use of various GUI elements and event handling via Listeners.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MapApp myapp = new MapApp("The BusStop on the Map!"); 
		myapp.setSize(1200, 600);
		myapp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myapp.setVisible(true);
		myapp.updateCustomComponent();
	}

}
