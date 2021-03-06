package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import backend.City;
import backend.Link;
import backend.Map;
import backend.POI;
import backend.Place;
import utils.RoadType;

/**
 * MapFrame class extends JFrame and displays GUI of navigation system.
 * 
 * FRAME_WIDTH - width of the frame
 * FRAME_HEIGHT - height of the frame
 * FRAME_TITLE - title of the frame
 * CITY_SIZE - size of city displayed on the MapPanel
 * mp - Map panel
 * selectedPlaces - stack of places that the user has selected
 * currentMap - variable to represent Map class
 */
public class MapFrame extends JFrame{
	// constants
	private static final int FRAME_WIDTH = 1100;
	private static final int FRAME_HEIGHT = 930;
	private static final String FRAME_TITLE = "Kansas";
	private static final int CITY_SIZE = 20;
	
	// fields
	private MapPanel mp;
	private final Stack<Place> selectedPlaces;
	private Map currentMap;
	/**
	 * The constructor initialize the frame and field variables.
	 * @param map Map to display in the frame
	 */
	public MapFrame(Map map){
		super();
		currentMap = map;
		Dimension d = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
		this.setTitle(FRAME_TITLE);
		this.setMinimumSize(d);
		this.setPreferredSize(d);
		this.setMaximumSize(d);
		this.setResizable(false);
		
		selectedPlaces = new Stack<Place>();
		
		mp = new MapPanel();
		this.add(mp);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null); // Centers it
		
		mp.mdp.drawCityToMap();

		this.setVisible(true);
	}
	/**
	 * This method helps to store the data and prints out the places that user selected
	 * @param p place that is selected
	 */
	private void placeSelected(Place p) {
		selectedPlaces.push(p);
		// Set the to and from fields 
		if (mp.sfp.lockFrom.isSelected()) {
			if (!mp.sfp.lockTo.isSelected()) {
				// Updating To city
				mp.sfp.to.setText(p.getName());
			}
		} else {
			// Updating From city
			mp.sfp.from.setText(p.getName());
		}
		mp.ldp.drawList();
	}
	/**
	 * MapPanel is the main Panel for the Frame.
	 * This panel contains following four panels. 
	 *  MapDisplayPanel mdp
	 *	ListDisplayPanel ldp
	 *	SearchFormPanel sfp
	 *	EditButtonPanel ebp
	 */
	public class MapPanel extends JPanel {
		private MapDisplayPanel mdp;
		private ListDisplayPanel ldp;
		private SearchFormPanel sfp;
		private EditButtonPanel ebp;
		
		/**
		 * The constructor initialize all the panels inside the MapPanel
		 */
		public MapPanel() {
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			// Inserting MapDisplayPanel
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0.50;
			c.weighty = 0.50;
			c.ipadx = 50;
			c.ipady = 50;
			mdp = new MapDisplayPanel();
			this.add(mdp, c);
			// Inserting ListDisplayPanel
			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 0.50;
			c.weighty = 0.50;
			c.ipadx = 0;
			c.ipady = 0;
			ldp = new ListDisplayPanel();
			this.add(ldp, c);
			// Inserting SearchFormPanel
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 0.50;
			c.weighty = 0.50;
			c.ipadx = 50;
			c.ipady = 50;
			sfp = new SearchFormPanel();
			this.add(sfp, c);
			// Inserting EditButtonPanel
			c.gridx = 1;
			c.gridy = 1;
			c.weightx = 0.50;
			c.weighty = 0.50;
			c.ipadx = 50;
			c.ipady = 50;
			ebp = new EditButtonPanel();
			this.add(ebp, c);
		}
		
		/**
		 * MapDisplayPanel displays map of cities and their links.
		 * circleLabels - ArrayList of CircleLabel components
		 * roads - ArraysList of RoadLine components
		 * g2 - graphic to display the map 
		 */
		public class MapDisplayPanel extends JPanel {
			private ArrayList<CircleLabel> circleLabels;
			private ArrayList<RoadLine> roads;
			private Graphics2D g2;
			
			/**
			 * the constructor initialize the panel and displays the map and the links
			 */
			public MapDisplayPanel() {
				super();
				this.setBackground(new Color(0,128,0));
				Dimension d = new Dimension(650, 650);
				this.setMinimumSize(d);
				this.setPreferredSize(d);
				this.setMaximumSize(d);
				
				this.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						for (CircleLabel circleLabel : circleLabels) {
							if (circleLabel.contains(e.getX(), e.getY())) {
								placeSelected(circleLabel.getCity());
								return;
							}
						}
						selectedPlaces.clear();
						ldp.drawList();
						System.out.println("Mouse click detected on map!");
					}

					public void mouseEntered(MouseEvent e) {
					}

					public void mouseExited(MouseEvent e) {
					}

					public void mousePressed(MouseEvent e) {
					}

					public void mouseReleased(MouseEvent e) {					
					}
					
				});
			}
			/**
			 * remove the previous route that is searched when next route is searched
			 */
			public void resetRoute(){
				int index= roads.size()-1;
				while(roads.get(index).getRtype().equals(RoadType.ROUTE)){
					roads.remove(index);
					index--;
				}
				updateUI();
			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g2 = (Graphics2D) g;
				for (RoadLine rd : roads) {
					if(rd.getRtype().equals(RoadType.INTERSTATE)){
						g2.setPaint(Color.BLUE);
						g2.setStroke(new BasicStroke(3));
					}
					else if(rd.getRtype().equals(RoadType.HIGHWAY)){
						g2.setPaint(Color.BLACK);
						g2.setStroke(new BasicStroke(2));
					}
					else if(rd.getRtype().equals(RoadType.ROUTE)){
						g2.setPaint(Color.RED);
						g2.setStroke(new BasicStroke(5));
					}
					else{
						g2.setPaint(new Color(192,192,192));
					}
					g2.draw(rd);
					g2.fill(rd);
				}
				for (CircleLabel cl : circleLabels) {
					g2.setPaint(new Color(255,215,0));
					g2.fill(cl);
					g2.setPaint(Color.WHITE);
					g2.drawString(cl.getLabel(), (float) cl.getMaxX(), (float) cl.getCenterY());
				}
			}
			
			/**
			 * this method draws the route on the map with red lines 
			 * @param p ArrayList of the places for the route
			 */
			protected void drawRoute(ArrayList<Place> p){
				// get the route to be drawn
				ArrayList<Place> drawRoute = p;
				// get hash map of cities 
				HashMap<String,City> cities = currentMap.getCities();
				// if route is longer than one add the road lines to the list of roads to be drawn
				if(drawRoute.size()>1){
					City from = cities.get(drawRoute.get(0).getName());
					City to = from;
					for(int i=1;i<drawRoute.size();i++){
						from = to;
						to = cities.get(drawRoute.get(i).getName());
						mp.mdp.roads.add(new RoadLine(RoadType.ROUTE,from.getMapLoc().getX()+(CITY_SIZE/2),from.getMapLoc().getY()+(CITY_SIZE/2),to.getMapLoc().getX()+(CITY_SIZE/2),to.getMapLoc().getY()+(CITY_SIZE/2)));
					}
				}
				this.updateUI();
			}
			
			/**
			 * Draw cities and their roads on the map
			 */
			private void drawCityToMap(){
				circleLabels = new ArrayList<CircleLabel>();
				roads = new ArrayList<RoadLine>();
				ArrayList<Link> links;
				Iterator<City> i = currentMap.getPopTree().iterator();
				int x;
				int y;
				City temp;
				Point location;
				for (City c : currentMap.getPopTree()) {
					temp = i.next();
					location = temp.getMapLoc();
					x = (int)location.getX();
					y = (int)location.getY();
					circleLabels.add(new CircleLabel(temp.getName(), x, y, CITY_SIZE, c));
					links = temp.getNeighbors();
					for(int t=0;t<links.size();t++){
						roads.add(new RoadLine(links.get(t).getRoadType(),
								location.getX()+(CITY_SIZE/2),
								location.getY()+(CITY_SIZE/2), 
								links.get(t).getPlace().getMapLoc().getX()+(CITY_SIZE/2), 
								links.get(t).getPlace().getMapLoc().getY()+(CITY_SIZE/2)));
					}
				}
			}
	
			/**
			 * This class represents the lines of roads to be drawn 
			 * type - type of the road to be drawn
			 */
			public class RoadLine extends Line2D.Double{
				private final RoadType type;
				
				public RoadLine(RoadType type,double w,double x,double y,double z){
					super(w,x,y,z);
					this.type = type;
				}
				
				/**
				 * Returns the type of the road
				 * @return the road type
				 */
				public RoadType getRtype(){
					return type;
				}
			}
			/**
			 * This class helps to display the location of cities and their names
			 * name - name of the city
			 * city - city to be drawn 
			 */
			public class CircleLabel extends Ellipse2D.Double {
				private final String name;
				private final City city;
				
				/**
				 * 
				 * @param name
				 *            name of the city to be written
				 * @param x
				 *            x coordinate of the center point of the city
				 * @param y
				 *            y coordinate of the center point of the city
				 * @param size
				 *            size of the circle to represent the city
				 * @param city
				 *            city to be drawn
				 */
				public CircleLabel (String name, int x, int y, int size, City city) {
					super(x, y, size,size);
					this.name = name;
					this.city = city;
				}
				/**
				 * return the name of the string to be labeled
				 * @return
				 */
				public String getLabel() {
					return name;
				}
				/**
				 * return the city to be drawn
				 * @return
				 */
				public City getCity() {
					return city;
				}
			}
		}
		/**
		 * ListDisplacePanel is an inner class of MapFrame class
		 * This class is a panel to show the list of Cities, POIs, information about the Places, and the route searched. 
		 * txt - Text field of information to be displayed
		 * back - Button that goes back to the list of cities
		 * list - the list that displays items
		 * orderOptions - a small panel with radio buttons to choose the ordering
		 * orders - The group of radio buttons
		 * alp - Buttons in alphabetical order
		 * pop - Buttons by order of population
		 * rat - Buttons by order of ratings
		 */
		public class ListDisplayPanel extends JPanel{
			private final InfoArea txt;
			private final BackButton back;
			private final JPanel list;
			private final JPanel orderOptions;
			private final ButtonGroup orders;
			private final JRadioButton alp;
			private final JRadioButton pop;
			private final JRadioButton rat;
			
			
			/**
			 * The constructor initialize the variables and displays the list panel with the list of cities.
			 */
			public ListDisplayPanel() {//there is going to be parameter of some data structure of cities.
				super();
				Dimension d = new Dimension(250, 650);
				this.setMinimumSize(d);
				this.setPreferredSize(d);
				this.setMaximumSize(d);
				 
				txt = new InfoArea();
				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				back = new BackButton();

				// Initialize List Panel
				list = new JPanel();
				list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
				// Create a Scroll Pane for the list and set the scrollbar to enable as needed.
				JScrollPane sp = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				sp.setMinimumSize(new Dimension(250, 630));
				sp.setPreferredSize(d);
				sp.getVerticalScrollBar().setUnitIncrement(16); // Make scrolling faster
				this.add(sp);
				
				// Create gap to place options at the bottom.
				this.add(Box.createVerticalGlue());
				
				// Initialize radio buttons
				orderOptions = new JPanel();
				orderOptions.setLayout(new BoxLayout(orderOptions, BoxLayout.X_AXIS));
				orders = new ButtonGroup();
				ActionListener rbl = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// This is the listener that tells the panel to redraw the list
						drawList();
					}
				};
				alp = new JRadioButton("Alphabetical");
				alp.addActionListener(rbl);
				pop = new JRadioButton("Population");
				pop.addActionListener(rbl);
				rat = new JRadioButton("Rating");
				rat.addActionListener(rbl);
				// Adding to the panel
				orderOptions.add(alp);
				orderOptions.add(pop);
				orderOptions.add(rat);
				// Adding to the ButtonGroup
				orders.add(alp);
				orders.add(pop);
				orders.add(rat);
				alp.setSelected(true);
				
				this.add(orderOptions);
				
				drawList();
			}
			/**
			 * This method empties the list field and changes what needs to be drawn next.
			 */
			private void drawList() {
				list.removeAll();
				if (selectedPlaces.isEmpty()) {
					for (City c : getCityList()) {
						list.add(new PlaceButton(c.getName(), c));
					}
				} else if (selectedPlaces.peek() instanceof City) {
					City c = (City) selectedPlaces.peek();
					txt.setPlace(c);
					list.add(txt);
					for (POI p : getPOIList(c)) {
						list.add(new PlaceButton(p.getName(), p));
					}
					list.add(back);
				} else {
					POI poi = (POI) selectedPlaces.pop();
					City c = (City) selectedPlaces.peek();
					selectedPlaces.push(poi);
					txt.setPlace(poi);
					list.add(txt);
					for (POI p : getPOIList(c)) {
						list.add(new PlaceButton(p.getName(), p));
					}
					list.add(back);
				}
				orderOptions.setVisible(true);
				updateUI();
			}
			/**
			 * This method prints out the route in the text area.
			 * @param r ArrayList of places that needs to be visited for the route
			 */
			private void drawRouteList(ArrayList<Place> r) {
				list.removeAll();
				txt.setRoute(r);
				list.add(txt);
				orderOptions.setVisible(false);
				updateUI();
			}
			/**
			 * return the group of buttons of city list based on the order selected
			 * @return
			 */
			public ArrayList<City> getCityList() {
				// Gets the list in the selected order
				if (orders.getSelection().equals(alp.getModel())) {
					return currentMap.getAlpCityList();
				} else if (orders.getSelection().equals(rat.getModel())) {
					return currentMap.getRatCityList();
				} else {
					return currentMap.getPopCityList();
				}
			}
			/**
			 * return the group of buttons of POI either by alphabetical or by their ratings
			 * @param c
			 * @return
			 */
			public ArrayList<POI> getPOIList(City c) {
				// Gets the list in the selected order
				if (orders.getSelection().equals(alp.getModel())) {
					return c.getAlpPOITree().toArrayList();
				} else {
					return c.getRatPOITree().toArrayList();
				}
			}

			/**
			 * InfoArea class represents the text area that displays the
			 * information about the place or the route that is searched by an
			 * user.
			 */
			public class InfoArea extends JTextArea {
				
				/**
				 * The constructor initialize the text area of the information
				 */
				public InfoArea() {
					super();
					Dimension d = new Dimension(247, 300);
					this.setMinimumSize(new Dimension(230, 300));
					this.setPreferredSize(d);
					this.setMaximumSize(d);
					this.setEditable(false);
					this.setAlignmentX(CENTER_ALIGNMENT);
				}

				/**
				 * This method sets the text area with the information of place
				 * that is given as parameter.
				 * 
				 * @param p
				 *            place to show the information
				 */
				public void setPlace(Place p) {
					setText(null);
					append(p.getName() + '\n');
					append("Rating: " + p.getRating() + " / 5.0\n");
					if (p instanceof City) {
						append("Population: " + ((City) p).getPopulation());
					} else {
						append("Type: " + ((POI) p).getType() + '\n');
						append("Estimated Cost: " + ((POI) p).getCost());
					}					
				}

				/**
				 * This method sets the text area with the route that is
				 * searched by an user.
				 * 
				 * @param r
				 */
				public void setRoute(ArrayList<Place> r) {
					setText(null);
					int i = 0;
					append((i+1) + ". Start from: " + r.get(i).getName() + '\n');
					for (i++;i<r.size()-1;i++) {
						append((i+1) + ". Go to: " + r.get(i).getName() + '\n');
					}
					append((i+1) + ". You will then arrive at: " + r.get(i).getName());
				}
			}
			/**
			 * PlaceButton class represents the buttons for the places.
			 * 
			 * p - place that represents that button
			 */
			public class PlaceButton extends JButton {
				private final Place p;
				
				/**
				 * The constructor initialize the button for the place.
				 * 
				 * @param s
				 *            name of the button in String
				 * @param place
				 *            place that represents the button
				 */
				public PlaceButton(String s, Place place) {
					super(s);
					Dimension d = new Dimension(247, 50);
					this.setMinimumSize(new Dimension(230, 50));
					this.setPreferredSize(d);
					this.setMaximumSize(d);
					this.setAlignmentX(CENTER_ALIGNMENT);
					
					p = place;
					
					this.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (!selectedPlaces.isEmpty() && selectedPlaces.peek() instanceof POI) {
								selectedPlaces.pop();
								} else {
								if (pop.isSelected()) alp.setSelected(true);
									pop.setEnabled(false);
							}
							placeSelected(p);
						}
					});
				}

				/**
				 * return the place for the button
				 * @return
				 */
				public Place getPlace() {
					return p;
				}
			}
			/**
			 * BackButton class represents the button that goes back to the list of the cities 
			 */
			public class BackButton extends JButton {
				/**
				 * The contstructor initialize the back button
				 */
				public BackButton() {
					super("Back");
					Dimension d = new Dimension(247, 50);
					this.setMinimumSize(new Dimension(230, 50));
//					this.setPreferredSize(d);
					this.setMaximumSize(d);
					this.setAlignmentX(CENTER_ALIGNMENT);
					
					this.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							selectedPlaces.clear();
							pop.setEnabled(true);
							drawList();
						}
					});
				}
			}
		}
		
		/**
		 * SearchFormPanel is an inner class of MapFrame class
		 * This class is a panel to with options for selecting from, to places, cost 
		 * functions, finding routes and resetting the form and the UI.
		 * from - A label + text field for from
		 * to - A label + text field for to
		 * lockFrom - A check box to lock from in run time
		 * lockTo - A check box to lock to in run time
		 * options - the ButtonGroup for time and distance buttons
		 * time - RadioButton to select time as the cost function
		 * distance - RadioButton to select distance as the cost function
		 */
		public class SearchFormPanel extends JPanel {
			
			private JTextField from;
			private JTextField to;
			private JCheckBox lockFrom;
			private JCheckBox lockTo;
			private ButtonGroup options;
			private JRadioButton time;
			private JRadioButton distance;
			
			public SearchFormPanel() {
				super();
				GroupLayout sl = new GroupLayout(this);
				
				// Some configuration of the GroupLayout
				this.setLayout(sl);
				sl.setAutoCreateGaps(true);
				sl.setAutoCreateContainerGaps(true);
				
				Dimension d = new Dimension(650, 80);
				this.setMinimumSize(d);
				this.setPreferredSize(d);
				this.setMaximumSize(d);
				
				// Start initializing screen elements
				JLabel fromLabel = new JLabel("From: ");
				JLabel toLabel = new JLabel("To: ");
				from = new JTextField() {
					// Override this method because if it is not enabled we don't want to change text;
					public void setText(String t) {
						if (this.isEnabled()) {
							super.setText(t);
						}
					}
				};
				
				// To is disabled until from is locked.
				to = new JTextField() {
					// Override this method because if it is not enabled we don't want to change text;
					public void setText(String t) {
						if (this.isEnabled()) {
							super.setText(t);
						}
					}
				};
				to.setEnabled(false);
				
				lockFrom = new JCheckBox("lock this selection");
				lockFrom.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							// Lock From field
							from.setEnabled(false);
							
							// Enable changing of To field
							to.setEnabled(true);
							lockTo.setEnabled(true);
						} else {
							// Unlock From field
							from.setEnabled(true);
							
							// Disable To field
							to.setEnabled(false);
							lockTo.setEnabled(false);
						}
					}
				});
				
				// To is disabled until from is locked.
				lockTo = new JCheckBox("lock this selection");
				lockTo.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							// Lock To field
							to.setEnabled(false);
						} else {
							// Unlock To field
							to.setEnabled(true);
						}
					}
				});
				lockTo.setEnabled(false);
				
				// Radio buttons belong to a ButtonGroup
				options = new ButtonGroup();
				time = new JRadioButton("time");
				time.setSelected(true);
				distance = new JRadioButton("distance");
				options.add(time);
				options.add(distance);
				
				JButton findRoute = new JButton("Find Route");
				findRoute.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						Place fromP = currentMap.getPlaces().get(from.getText());
						Place toP = currentMap.getPlaces().get(to.getText());
						if (fromP == null) {
							JOptionPane.showMessageDialog(MapFrame.this, "Your input for from isn't valid", "Invalid Input", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (toP == null) {
							JOptionPane.showMessageDialog(MapFrame.this, "Your input for to isn't valid", "Invalid Input", JOptionPane.ERROR_MESSAGE);
							return;
						}
						currentMap.getRoute(fromP, toP, (time.isSelected() ? "time" : "distance"));
						MapPanel.this.ldp.drawRouteList(currentMap.returnRoute());
						mdp.drawRoute(currentMap.returnRoute());
					}
				});
				JButton reset = new JButton("Reset");
				reset.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						reset();
					}
				});
				
				// Start adding elements, too complicated to actually explain what's going on here
				// Basically, we describe the relative locations of each item both horizontally and vertically.
				// For more information, please consult the documentation of GroupLayout.
				sl.setHorizontalGroup(
						sl.createSequentialGroup()
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(fromLabel)
									.addComponent(toLabel)
									)
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(from)
									.addComponent(to)
									)
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(lockFrom)
									.addComponent(lockTo)
									)
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(time)
									.addComponent(distance)
									)
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addComponent(findRoute, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(reset, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									)
						);
				
				sl.setVerticalGroup(
						sl.createSequentialGroup()
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(fromLabel)
									.addComponent(from)
									.addComponent(lockFrom)
									.addGroup(sl.createSequentialGroup()
											.addComponent(time)
											)
									.addComponent(findRoute)
									)
							.addGroup(sl.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(toLabel)
									.addComponent(to)
									.addComponent(lockTo)
									.addGroup(sl.createSequentialGroup()
											.addComponent(distance)
											)
									.addComponent(reset)
									)
							
							
						);
			}
			
			private void reset() {
				// Clears the form
				from.setEnabled(true);
				from.setText(null);
				lockFrom.setEnabled(true);
				lockFrom.setSelected(false);
				lockTo.setEnabled(false);
				lockTo.setSelected(false);
				to.setEnabled(true);
				to.setText(null);
				to.setEnabled(false);
				time.setSelected(true);
				
				// Remove the routes drawn
				MapPanel.this.mdp.resetRoute();
				
				// Reset selected
				selectedPlaces.clear();
				
				// Reset ListDisplayPanel
				MapPanel.this.ldp.alp.setSelected(true);
				MapPanel.this.ldp.pop.setEnabled(true);
				MapPanel.this.ldp.drawList();
			}
		}
		
		/**
		 * This class contains only the edit button which will create an EditFrame.
		 * 
		 */
		public class EditButtonPanel extends JPanel {
			
			public EditButtonPanel() {
				super();
				Dimension d = new Dimension(250, 80);
				this.setMinimumSize(d);
				this.setPreferredSize(d);
				this.setMaximumSize(d);
				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				
				// Sets up the button
				Dimension de = new Dimension(200, 64);
				JButton edit = new JButton("Edit Selection");
				edit.setMinimumSize(de);
				edit.setPreferredSize(de);
				edit.setMaximumSize(de);
				edit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						
						if (!selectedPlaces.isEmpty()) {
							// Creates the Edit dialogue box if there is a selected place
							if (selectedPlaces.peek() instanceof City) {
								new EditFrame((City) selectedPlaces.peek(), currentMap.getAlpCityList(), currentMap);
							} else {
								POI poi = (POI) selectedPlaces.pop();
								City c = (City) selectedPlaces.peek();
								selectedPlaces.push(poi);
								new EditFrame(poi, c, currentMap);
							}
						} else {
							// Or prompts the user that no place is selected.
							JOptionPane.showMessageDialog(MapFrame.this, "Please select a place first to edit its properties", "No Place Selected", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				edit.setAlignmentX(CENTER_ALIGNMENT);
				edit.setAlignmentY(CENTER_ALIGNMENT);
				
				// Adding glue to center the button
				this.add(Box.createVerticalGlue());
				this.add(edit);
				this.add(Box.createVerticalGlue());
			}
		}
	}
}
