package droids.foundout;

import java.util.List;

import android.location.Location;

import com.google.android.maps.GeoPoint;

/**
 * Used for polygon audio locations.
 * @author UnderGear
 *
 */
public class PolygonGeoSynth extends GeoSynth {
	private List<GeoPoint> points;
	private Polygon polygon;
	private GeoPoint center;
	
	/**
	 * Getter for polygon
	 * 
	 * @return polygon
	 */
	public Polygon getPolygon() {
		return polygon;
	}
	
	/**
	 * Constructor
	 * 
	 * @param points vertices of the polygon
	 * @param index of the area on the map
	 * @param name of the area
	 * @param syn name of the SC synth associated with this area
	 * @param fromServer whether it is downloaded or user-created
	 */
	public PolygonGeoSynth(List<GeoPoint> points, int index, String name, String syn, boolean fromServer) {
		this.index = index;
		this.name = name;
		this.synthDef = syn;
		this.setPoints(points);
		this.fromServer = fromServer;
		createPolygon(points); //initializes center in this call
		setOverlayItem(new PolygonOverlayItem(center, name, syn, points)); //creates the overlayItem that will go to the map
	}
	
	/**
	 * Creates a polygon for this area from a list of vertices
	 * 
	 * @param points list of vertices.
	 * 
	 * @return whether or not the polygon was successfully created - requires at least 3 points to make.
	 */
	public boolean createPolygon(List<GeoPoint> points) {
		
		int size = points.size();
		if (size <3)
			return false;
		
		int[] xComps = new int[size];
		int[] yComps = new int[size];
		int xTotal = 0, yTotal = 0, i = 0;
		for (GeoPoint p : points) {
			xComps[i] = p.getLongitudeE6();
			yComps[i] = p.getLatitudeE6();
			xTotal += xComps[i];
			yTotal += yComps[i];
			i++;
		}
		center = new GeoPoint(yTotal/size, xTotal/size);
		
		polygon = new Polygon(xComps, yComps);
		
		return true;
	}

	/**
	 * Setter for points
	 * 
	 * @param points
	 */
	public void setPoints(List<GeoPoint> points) {
		this.points = points;
	}

	/**
	 * Getter for points
	 * 
	 * @return points
	 */
	public List<GeoPoint> getPoints() {
		return points;
	}
	
	/**
	 * Determines if the location is within the polygon.
	 * 
	 * @param l the location to be tested
	 * 
	 * @return val 1 for start audio, 0 for stop audio, -1 for don't change
	 */
	@Override
	public int testPoint(Location l) {
		int lat = (int)(l.getLatitude() * 1E6);
		int lon = (int)(l.getLongitude() * 1E6);
		int val = -1;
		if (getPolygon().contains(lon, lat)) {
			if (play == false) {
				val = 1;
				play = true;
			}
		}
		else {
			if (play == true) {
				val = 0;
				play = false;
			}
		}
		return val;
	}
}
