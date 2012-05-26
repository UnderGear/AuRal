package droids.foundout;

import android.location.Location;

import com.google.android.maps.OverlayItem;

public abstract class GeoSynth {

	boolean fromServer = false;
	boolean play;
	int index;
	protected String synthDef;
	protected String name;
	private OverlayItem overlayItem;
	
	public String getSynthDef() {
		return synthDef;
	}
	
	public String getName() {
		return name;
	}
	
	public void setSynthDef(String s) {
		synthDef = s;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int testPoint(Location l) {
		return -1;
	}

	public OverlayItem getOverlayItem() {
		return overlayItem;
	}

	public void setOverlayItem(OverlayItem overlayItem) {
		this.overlayItem = overlayItem;
	}
}
