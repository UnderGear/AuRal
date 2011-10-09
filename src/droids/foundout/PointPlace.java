package droids.foundout;

import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PointPlace extends Place {
	double lat, lon;
	int range = 30;
	//TODO: we can save range on server and edit it now that it's inside this class
	
	public double getLat() {
		return lat;
	}
	
	public double getLon() {
		return lon;
	}
	
	//constructor
	public PointPlace(double lat, double lon, int index, String syn, String name, boolean server) {
		this.lat = lat;
		this.lon = lon;
		this.index = index;
		this.synthDef = syn;
		this.name = name;
		this.fromServer = server;
		GeoPoint p = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
		overlayItem = new OverlayItem(p, name, syn);
	}
	
	@Override
	public int testPoint(Location l) {
		Location dest = new Location("From List");
		dest.setLongitude(lon);
		dest.setLatitude(lat);
		int val = -1;
		if (l.distanceTo(dest) <= range) {
			if (play == false) {
				val = 1;
				play = true;
			}
		}
		else if (l.distanceTo(dest) > range * 1.5) {
			if (play == true) {
				val = 0;
				play = false;
			}
		}
		return val;
	}
}