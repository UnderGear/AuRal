package droids.foundout;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * This class is used for the creation of new polygon audio areas in Create Area Mode from the main menu
 * @author UnderGear
 *
 */
@SuppressWarnings({ "rawtypes" })
public class PolygonOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
	
	/**
	 * Constructor.
	 * 
	 * @param defaultMarker the standard marker that we will be drawing on the map
	 */
	public PolygonOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
	}
	
	/**
	 * Add an overlay to the map at point
	 * 
	 * @param overlay the OverlayItem to add
	 * @param point the GeoPoint to add it to
	 */
	public void addOverlay(OverlayItem overlay, GeoPoint point) {
	    mOverlays.add(overlay);
	    points.add(point);
	    populate();
	}

	/**
	 * Create an OverlayItem. Get it from our list.
	 * 
	 * @param i the index we want to pull from
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	/**
	 * Get the size of our list.
	 */
	@Override
	public int size() {
		return mOverlays.size();
	}
	
	/**
	 * Currently does nothing. May be changed in the future.
	 * 
	 * @param index the index of the item that has been tapped
	 */
	protected boolean onTap(int index) {
		//bring up edit menu or something.
		//TODO: remove the overlay if this is longpressed
		/**points.remove(index);
		mOverlays.remove(index);
		setLastFocusedIndex(-1);
		populate();
		owner.cartographer.mapView.invalidate();*/
		return true;
	}
	
	/**
	 * Draw this to the map.
	 * 
	 * @param canvas the map's canvas we're drawing to
	 * @param mapView the map
	 * @param shadow
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		int numberToDraw = mOverlays.size() - 1;
		Path p = new Path();
		for (int i = 0; i < numberToDraw; i++) {
		    Point from = new Point();
		    Point to = new Point();
		    projection.toPixels(points.get(i), from);
		    projection.toPixels(points.get(i + 1), to);
		    p.moveTo(from.x, from.y);
		    p.lineTo(to.x, to.y);
	    }
		Point from = new Point();
	    Point to = new Point();
	    projection.toPixels(points.get(0), from);
	    projection.toPixels(points.get(numberToDraw), to);
	    p.moveTo(from.x, from.y);
	    p.lineTo(to.x, to.y);
	    
	    Paint mPaint = new Paint();
	    mPaint.setStyle(Style.STROKE); //Style.FILL
	    mPaint.setColor(0xFFFF0000);
	    mPaint.setAntiAlias(true);
	    canvas.drawPath(p, mPaint);
		
		super.draw(canvas, mapView, shadow);
	}

}