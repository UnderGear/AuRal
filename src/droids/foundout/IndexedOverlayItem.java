package droids.foundout;

import com.google.android.maps.OverlayItem;

/**
 * This class simply associates an overlay item with an index. Useful when we need to have multiple IDs for SC synth, map overlay, etc.
 * @author UnderGear
 *
 */
public class IndexedOverlayItem {
	private OverlayItem item;
	int index;
	
	/**
	 * Constructor
	 * 
	 * @param item
	 * @param index
	 */
	public IndexedOverlayItem(OverlayItem item, int index) {
		this.setItem(item);
		this.index = index;
	}

	public OverlayItem getItem() {
		return item;
	}

	public void setItem(OverlayItem item) {
		this.item = item;
	}
}