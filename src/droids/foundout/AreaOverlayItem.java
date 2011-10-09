package droids.foundout;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class AreaOverlayItem extends OverlayItem {

	List<GeoPoint> points;
	
	public AreaOverlayItem(GeoPoint center, String title, String snippet, List<GeoPoint> points) {
		super(center, title, snippet);
		this.points = points;
	}

}
