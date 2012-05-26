package droids.foundout;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Used for polygon areas.
 * @author UnderGear
 *
 */
public class PolygonOverlayItem extends OverlayItem {

	private List<GeoPoint> points;
	
	/**
	 * Constructor.
	 * 
	 * @param center the center of the polygon
	 * @param title the name of the area
	 * @param snippet text description
	 * @param points the vertices of the polygon
	 */
	public PolygonOverlayItem(GeoPoint center, String title, String snippet, List<GeoPoint> points) {
		super(center, title, snippet);
		this.setPoints(points);
	}
	
	/**
	 * Getter for points.
	 * 
	 * @return points list of vertices of the polygon
	 */
	public List<GeoPoint> getPoints() {
		return points;
	}

	/**
	 * Setter for points
	 * 
	 * @param points
	 */
	public void setPoints(List<GeoPoint> points) {
		this.points = points;
	}
}