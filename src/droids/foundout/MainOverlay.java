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

@SuppressWarnings("unchecked")
public class MainOverlay extends ItemizedOverlay {
	
	private Map<Object, IndexedOverlayItem> overlays = new HashMap<Object, IndexedOverlayItem>();
	private AuRal owner;
	private int overlayIndex;
	
	public MainOverlay(Drawable defaultMarker, AuRal owner) {
		super(boundCenter(defaultMarker));
		this.owner = owner;
		OverlayItem me = new OverlayItem(new GeoPoint(0, 0), "", "");
		IndexedOverlayItem user = new IndexedOverlayItem(me, 0);
		overlayIndex = 0;
		overlays.put(overlayIndex, user);
		setLastFocusedIndex(-1);
		populate();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		for (IndexedOverlayItem indexedOverlayItem : overlays.values()) {
			Paint mPaint = new Paint();
		    mPaint.setStyle(Style.STROKE);
		    //mPaint.setStyle(Style.FILL);
		    mPaint.setColor(0xFFFF0000);
		    mPaint.setAntiAlias(true);
		    
		    //this is an area overlay item
			if (indexedOverlayItem.item instanceof AreaOverlayItem) {
				//we can set a marker here for areas
				//overlay.setMarker(Drawable marker); //keep in mind this will just give the center an image
				AreaOverlayItem areaOverlayItem = (AreaOverlayItem)indexedOverlayItem.item;
				//draw the area now
				int numberToDraw = areaOverlayItem.points.size() - 1;
				
				Path p = new Path();
			    for (int i = 0; i < numberToDraw; i++) {
				    Point from = new Point();
				    Point to = new Point();
				    projection.toPixels(areaOverlayItem.points.get(i), from);
				    projection.toPixels(areaOverlayItem.points.get(i + 1), to);
				    p.moveTo(from.x, from.y);
				    p.lineTo(to.x, to.y);
			    }
			    Point from = new Point();
			    Point to = new Point();
			    projection.toPixels(areaOverlayItem.points.get(0), from);
			    projection.toPixels(areaOverlayItem.points.get(numberToDraw), to);
			    p.moveTo(from.x, from.y);
			    p.lineTo(to.x, to.y);
			    canvas.drawPath(p, mPaint);
				
			}
			//this is the user.
			else if (indexedOverlayItem.index == 0) {
				//we can set a marker here for the user
				//overlay.setMarker(Drawable marker);
			}
			//this is a point location
			else { //it's a normal point overlay
				//we can set a marker here for pointplaces
				//overlay.setMarker(Drawable marker);
				OverlayItem item = (OverlayItem)indexedOverlayItem.item;
				PointPlace place = (PointPlace)owner.auraManager.destinations.get(indexedOverlayItem.index);
				int radius = metersToRadius((float)60.0, mapView, place.getLat());
				GeoPoint gP = item.getPoint();
				Point p = new Point();
				projection.toPixels(gP, p);
				canvas.drawCircle(p.x, p.y, radius, mPaint);
			}
		}
		
		super.draw(canvas, mapView, shadow);
	}
	
	//for determining the radius of a point location's circle on the mapView
	public static int metersToRadius(float meters, MapView map, double latitude) {
	    return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
	}
	
	public void moveMe(OverlayItem overlay) {
		//mOverlays.set(0, overlay);
		overlays.put(0, new IndexedOverlayItem(overlay, 0));
		setLastFocusedIndex(-1);
		populate();
	}
	
	//public void setOverlays(List<OverlayItem> overlays) {
	public void setOverlays(Map<Object, IndexedOverlayItem> o) {
		this.overlays = o;
	    setLastFocusedIndex(-1);
		populate();
		overlayIndex = o.size();
	}
	
	public void addOverlay(IndexedOverlayItem overlay) {
		overlayIndex++;
		overlays.put(overlayIndex, overlay);
		setLastFocusedIndex(-1);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i).item;
	}

	@Override
	public int size() {
		return overlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		if (index == 0) {
			Location currentLocation = owner.locationeer.getCurrentLocation();
			owner.cartographer.mapController.animateTo(new GeoPoint((int)(currentLocation.getLatitude()*1E6), (int)(currentLocation.getLongitude()*1E6)));
			return true;
		}
		else {
			editDialog(index);
			return true;
		}
	}
	
	private void editDialog(final int index) {
		new Thread() {
			@SuppressWarnings("static-access")
			LayoutInflater inflater = (LayoutInflater) owner.getSystemService(owner.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dialog, (ViewGroup) owner.findViewById(R.id.dialogroot));
			Place place = (Place) owner.auraManager.destinations.get(overlays.get(index).index);
			
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
                    		
                    		Toast.makeText(owner, "Removing Location: " + owner.auraManager.destinations.get(overlays.get(index).index).name , 3000).show();
                    		if (place.fromServer == true && place.play == true)
                    			owner.serverHook.notifyServerLocationExit(place);
                    		if (place.play == true)
                    			owner.scManager.stopAudio(place.index);
                    		owner.auraManager.destinations.remove(overlays.get(index).index);
                    		overlays.remove(index);
                    		setLastFocusedIndex(-1);
                    		owner.cartographer.mapView.invalidate();
                    		
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
                    		owner.cartographer.mapView.invalidate();
                    		
                    		owner.auraManager.testLocation(owner.locationeer.getCurrentLocation());
                    	}
                    	else {
                    		CheckBox submit = (CheckBox) layout.findViewById(R.id.dialogSubmit);
                    		if (submit.isChecked()) {
                    			if (place instanceof PointPlace)
                    				owner.serverHook.submitArea((PointPlace)place);
                    		}
	                    	place.setName(et.getText().toString());
	                    	place.setSynthDef(syn);
	                    	place.play = false;
	                    	owner.scManager.updateAudio(place.getIndex(), synOld.replace(".scsyndef", ""), place.getSynthDef().replace(".scsyndef", ""));
	                    	owner.auraManager.testLocation(owner.locationeer.getCurrentLocation());
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