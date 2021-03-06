package droids.foundout;

import java.util.List;

import android.location.Location;

import com.google.android.maps.GeoPoint;

public class Area extends Place {
	private List<GeoPoint> points;
	private Polygon polygon;
	private GeoPoint center;
	
	public Polygon getPolygon() {
		return polygon;
	}
	
	public Area(List<GeoPoint> points, int index, String name, String syn, boolean server) {
		this.index = index;
		this.name = name;
		this.synthDef = syn;
		this.setPoints(points);
		this.fromServer = server;
		createPolygon(points); //initializes center in this call
		overlayItem = new AreaOverlayItem(center, name, syn, points);
	}
	
	public void createPolygon(List<GeoPoint> points) {
		int size = points.size();
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
		
		polygon = new Polygon(xComps, yComps, size);
	}

	public void setPoints(List<GeoPoint> points) {
		this.points = points;
	}

	public List<GeoPoint> getPoints() {
		return points;
	}
	
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
