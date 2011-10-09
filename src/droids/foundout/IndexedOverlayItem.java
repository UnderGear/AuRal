package droids.foundout;

import com.google.android.maps.OverlayItem;


public class IndexedOverlayItem {
	OverlayItem item;
	int index;
	
	public IndexedOverlayItem(OverlayItem item, int index) {
		this.item = item;
		this.index = index;
	}
}