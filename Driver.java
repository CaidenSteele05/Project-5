import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import java.awt.*;
import java.awt.event.*;

public class Driver{
	private static boolean readyToRun = true;
	
    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	TripPoint.readFile("triplog.csv");
    	TripPoint.h1StopDetection();
    	
    	JFrame frame = new JFrame("Trip Viewer");
		frame.setSize(1000,800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);
		
		JMapViewer viewer = new JMapViewer();
		viewer.setBounds(0,0,1000,800);
		viewer.setTileSource(new OsmTileSource.TransportMap());
		
		Coordinate coord = new Coordinate(34.207218499999996, -97.438866);
		viewer.setDisplayPosition(coord, 5);
		
		Image image = ImageIO.read(new File("./raccoon.png"));
		IconMarker marker = new IconMarker(coord, image);
		viewer.addMapMarker(marker);
		
		
		JPanel mainPanel = new JPanel(new FlowLayout());
		mainPanel.setOpaque(false);
		mainPanel.setBounds(10,10,400,50);
		
		String[] dropdownOptions = {"Animation Time", "15", "30", "60", "90"};
		JComboBox<String> dropdown = new JComboBox<>(dropdownOptions);
		mainPanel.add(dropdown);
		
		JCheckBox checkbox = new JCheckBox("Include Stops");
		mainPanel.add(checkbox);
		
		Button playButton = new Button("Play");
		playButton.setBackground(Color.LIGHT_GRAY);
		mainPanel.add(playButton);
		
		ArrayList<Coordinate> pathCoordinates = new ArrayList<>();
		MapPolygonImpl polygon = new MapPolygonImpl(pathCoordinates);
		polygon.setColor(Color.RED);
		polygon.setBackColor(null);
		viewer.addMapPolygon(polygon);
		
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(readyToRun == false) {
					return;
				}
				readyToRun = false;
				
				ArrayList<TripPoint> tripPoints;
				if(checkbox.isSelected())
					tripPoints = TripPoint.getTrip();
				else
					tripPoints = TripPoint.getMovingTrip();
				String dropdownText = dropdown.getSelectedItem().toString();
				int dropdownOption = dropdownText.equals("Animation Time") ? 15 : Integer.parseInt(dropdownText);
				
				int delay = (int)  (dropdownOption * 1000 / TripPoint.getTrip().size());
				
				pathCoordinates.clear();
				
				Timer timer = new Timer(delay, new ActionListener() {
					private int index = 0;
					
					public void actionPerformed(ActionEvent e) {
						if(index < tripPoints.size()) {
							TripPoint p = tripPoints.get(index);
							marker.setLat(p.getLat());
							marker.setLon(p.getLon());
							//viewer.repaint();
							
							//viewer.removeMapPolygon(polygon);
							pathCoordinates.add(new Coordinate(p.getLat(), p.getLon()));
							//polygon = new MapPolygonImpl(pathCoordinates);
							
							//viewer.addMapPolygon(polygon);
							viewer.repaint();
							
							index++;
						}else {
							((Timer) e.getSource()).stop();
							readyToRun = true;
						}
					}
					
				});
				
				timer.setRepeats(true);
				timer.start();
			}
		});
		
		layeredPane.add(viewer, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);
		frame.add(layeredPane, BorderLayout.CENTER);
		
		frame.setVisible(true);
        
        
    }
    
}