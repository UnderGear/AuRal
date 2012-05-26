package droids.foundout;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * This is the map overlay used on the Google Maps map view
 * @author UnderGear
 *
 */
@SuppressWarnings("unchecked")
public class MainOverlay extends ItemizedOverlay {
	
	private Map<Object, IndexedOverlayItem> overlays = new HashMap<Object, IndexedOverlayItem>();
	private AuRal owner;
	private int overlayIndex;
	
	/**
	 * Constructor
	 * 
	 * @param defaultMarker default icon to place on overlays
	 * @param owner AuRal main activity
	 */
	public MainOverlay(Drawable defaultMarker, AuRal owner) {
		super(boundCenter(defaultMarker));
		this.owner = owner;
		
		//The index 0 is reserved for the user.
		OverlayItem me = new OverlayItem(new GeoPoint(0, 0), "", "");
		IndexedOverlayItem user = new IndexedOverlayItem(me, 0);
		overlayIndex = 0;
		overlays.put(overlayIndex, user);
		setLastFocusedIndex(-1);
		populate();
	}
	
	/**
	 * Draw the overlay!
	 * 
	 * @param canvas the map's canvas to draw to
	 * @param mapView the map
	 * @param shadow
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		for (IndexedOverlayItem indexedOverlayItem : overlays.values()) {
			Paint mPaint = new Paint();
		    mPaint.setStyle(Style.STROKE);
		    mPaint.setColor(0xFFFF0000); //red.
		    mPaint.setAntiAlias(true);
		    
		    //This is an area overlay item. Draw the red bounding polygon around it.
			if (indexedOverlayItem.getItem() instanceof PolygonOverlayItem) {
				//we can set a marker here for areas
				//overlay.setMarker(Drawable marker); //keep in mind this will just give the center an image
				PolygonOverlayItem areaOverlayItem = (PolygonOverlayItem)indexedOverlayItem.getItem();
				//draw the area now
				int numberToDraw = areaOverlayItem.getPoints().size() - 1;
				
				Path p = new Path();
			    for (int i = 0; i < numberToDraw; i++) {
				    Point from = new Point();
				    Point to = new Point();
				    projection.toPixels(areaOverlayItem.getPoints().get(i), from);
				    projection.toPixels(areaOverlayItem.getPoints().get(i + 1), to);
				    p.moveTo(from.x, from.y);
				    p.lineTo(to.x, to.y);
			    }
			    Point from = new Point();
			    Point to = new Point();
			    projection.toPixels(areaOverlayItem.getPoints().get(0), from);
			    projection.toPixels(areaOverlayItem.getPoints().get(numberToDraw), to);
			    p.moveTo(from.x, from.y);
			    p.lineTo(to.x, to.y);
			    canvas.drawPath(p, mPaint);
				
			}
			//This is the user.
			else if (indexedOverlayItem.index == 0) {
				//TODO: we can set a marker here for the user
				//overlay.setMarker(Drawable marker);
			}
			//This is a point location. Draw its red bounding circle around it.
			else {
				OverlayItem item = (OverlayItem)indexedOverlayItem.getItem();
				PointGeoSynth place = (PointGeoSynth)owner.getAuraManager().getDestinations().get(indexedOverlayItem.index);
				int radius = metersToRadius((float)60.0, mapView, place.getLat());
				GeoPoint gP = item.getPoint();
				Point p = new Point();
				projection.toPixels(gP, p);
				canvas.drawCircle(p.x, p.y, radius, mPaint);
			}
		}
		super.draw(canvas, mapView, shadow);
	}
	
	/**
	 * Converts the radius of a location to the map view system based on latitude
	 * 
	 * @param meters radius in meters
	 * @param map
	 * @param latitude of the point
	 * @return radius converted to the map's system from meters
	 */
	public static int metersToRadius(float meters, MapView map, double latitude) {
	    return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
	}
	
	/**
	 * Move the user's marker to the new position.
	 * 
	 * @param overlay
	 */
	public void moveMe(OverlayItem overlay) {		
		overlays.put(0, new IndexedOverlayItem(overlay, 0));
		setLastFocusedIndex(-1);
		populate();
	}
	
	/**
	 * Setter for the map
	 * 
	 * @param o new map to be set
	 */
	public void setOverlays(Map<Object, IndexedOverlayItem> o) {
		this.overlays = o;
	    setLastFocusedIndex(-1);
		populate();
		overlayIndex = o.size();
	}
	
	/**
	 * Increment the index and add an overlay to the map
	 * 
	 * @param overlay to be added
	 */
	public void addOverlay(IndexedOverlayItem overlay) {
		overlayIndex++;
		overlays.put(overlayIndex, overlay);
		setLastFocusedIndex(-1);
	    populate();
	}

	/**
	 * For drawing the overlays. Called on each one by map view
	 * 
	 * @param i index
	 * @return the overlay item at index i
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i).getItem();
	}

	/**
	 * The size of the map
	 * 
	 * @return size of the map
	 */
	@Override
	public int size() {
		return overlays.size();
	}
	
	/**
	 * The user has tapped on an overlay icon. If it's 0, animate to center the user. Otherwise, we want to edit the overlay
	 * 
	 * @param index of the tapped overlay icon
	 * @return true - we handle all possibilities here, so the event is finished
	 */
	@Override
	protected boolean onTap(int index) {
		if (index == 0) {
			Location currentLocation = owner.getLocationeer().getCurrentLocation();
			owner.getCartographer().getMapController().animateTo(new GeoPoint((int)(currentLocation.getLatitude()*1E6), (int)(currentLocation.getLongitude()*1E6)));
			return true;
		}
		else {
			editDialog(index);
			return true;
		}
	}
	
	/**
	 * The user has tapped on an overlay that isn't his/her own. We want to edit the location associated with it.
	 * This is much like creation of a synth/geo location mapping.
	 * 
	 * @param index of the tapped overlay
	 */
	private void editDialog(final int index) {
		new Thread() {
			@SuppressWarnings("static-access")
			LayoutInflater inflater = (LayoutInflater) owner.getSystemService(owner.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dialog, (ViewGroup) owner.findViewById(R.id.dialogroot));
			GeoSynth place = (GeoSynth) owner.getAuraManager().getDestinations().get(overlays.get(index).index);
			
			//PointPlace place = (PointPlace) owner.destinations.get(index); //TODO: convert this to a Place first, then we can filter into subclasses
			EditText et = (EditText) layout.findViewById(R.id.dialogEditName);
			String syn = "";
            public void run() {
            	//directory of all synthdefs
            	File f=new File("/sdcard/supercollider/synthdefs");
            	
            	//TODO: we need a check box for sending to server or not
            	final AlertDialog.Builder dialog = new AlertDialog.Builder(owner);
            	dialog.setView(layout);
            	CheckBox submit = (CheckBox) layout.findViewById(R.id.dialogSubmit);
            	if (place.fromServer == true) {
                	submit.setVisibility(4);
            	}
            	
            	Spinner spinner = (Spinner) layout.findViewById(R.id.SynthSpin);
            	if (place.fromServer == false) {
            	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
						TextView tvx = (TextView) selectedItemView;
						syn = tvx.getText().toString();
					}

					public void onNothingSelected(AdapterView<?> parentView) { }
            	});
            	}
            	
            	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(owner, android.R.layout.simple_spinner_item, f.list());
            	
            	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            	spinner.setAdapter(adapter);
            	final String synOld = place.getSynthDef();
            	et.setText(place.getName());
            	int spinnerPosition = adapter.getPosition(place.getSynthDef());
            	spinner.setSelection(spinnerPosition);
            	dialog.setTitle("Edit Destination");
            	dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                		CheckBox remove = (CheckBox) layout.findViewById(R.id.dialogRemove);
                    	if (remove.isChecked()) {
                    		
                    		Toast.makeText(owner, "Removing Location: " + owner.getAuraManager().getDestinations().get(overlays.get(index).index).getName() , 3000).show();
                    		if (place.fromServer == true && place.play == true)
                    			owner.getServerHook().notifyServerLocationExit(place);
                    		if (place.play == true)
                    			owner.getScManager().stopAudio(place.index);
                    		owner.getAuraManager().getDestinations().remove(overlays.get(index).index);
                    		overlays.remove(index);
                    		setLastFocusedIndex(-1);
                    		owner.getCartographer().getMapView().invalidate();
                    		
                    		Object[] keys = overlays.keySet().toArray();
                    		Collection<IndexedOverlayItem> itemSet = (Collection<IndexedOverlayItem>) overlays.values();
                    		IndexedOverlayItem[] items = itemSet.toArray(new IndexedOverlayItem[itemSet.size()]);
                    		
                    		Map<Object, IndexedOverlayItem> newOverlays = new HashMap<Object, IndexedOverlayItem>();
                    		for (int i = 0; i < overlays.size(); i++) {
                    			keys[i] = i;
                    			newOverlays.put((Object)i, items[i]);
                    		}
                    		overlayIndex--;
                    		overlays = newOverlays;
                    		setLastFocusedIndex(-1);
                    		populate();
                    		owner.getCartographer().getMapView().invalidate();
                    		
                    		owner.getAuraManager().testLocation(owner.getLocationeer().getCurrentLocation());
                    	}
                    	else {
                    		CheckBox submit = (CheckBox) layout.findViewById(R.id.dialogSubmit);
                    		if (submit.isChecked()) {
                    			if (place instanceof PointGeoSynth)
                    				owner.getServerHook().submitPoint((PointGeoSynth)place);
                    		}
	                    	place.setName(et.getText().toString());
	                    	place.setSynthDef(syn);
	                    	place.play = false;
	                    	owner.getScManager().updateAudio(place.getIndex(), synOld.replace(".scsyndef", ""), place.getSynthDef().replace(".scsyndef", ""));
	                    	owner.getAuraManager().testLocation(owner.getLocationeer().getCurrentLocation());
	                    }
                    	
                	}
            	});
            	dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                	}
            	});
            	AlertDialog d = dialog.create();
            	d.show();
            }
        }.run();
	}
}