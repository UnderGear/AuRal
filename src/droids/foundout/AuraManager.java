package droids.foundout;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.maps.OverlayItem;

import droids.foundout.R.id;

/**
 * This class links geo locations to SC synths
 * @author UnderGear
 *
 */
public class AuraManager {
	private AuRal owner;
	private int playCount = 0; //gives SC synths an index corresponding to the number of locations
	private Map<Object, GeoSynth> destinations = new HashMap<Object, GeoSynth>(); //this maps index to place - supercollider to location interface.
	
	/**
	 * Constructor
	 * 
	 * @param owner the main AuRal activity
	 */
	public AuraManager(AuRal owner) {
		this.owner = owner;
	}
	
	/**
	 * The user finalized a polygon audio location creation. Give them the dialog to select name, synth, etc.
	 * 
	 * @return success or failure of creation
	 */
	public boolean createAreaLocation() {
    	owner.runOnUiThread(new Runnable() {
			String name, syn = "Micromoog.scsyndef";
    		public void run() {
    			//directory of all synthdefs
            	File f=new File("/sdcard/supercollider/synthdefs");
            	//TODO: we need a check box for sending to server or not?
            	
            	//Inflate the dialog
            	AlertDialog.Builder dialog = new AlertDialog.Builder(owner);
            	LayoutInflater inflater = (LayoutInflater) owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	final View layout = inflater.inflate(R.layout.dialog, (ViewGroup) owner.findViewById(R.id.dialogroot));
            	dialog.setView(layout);
            	
            	//Set up drop-down menu for synths available
            	Spinner spinner = (Spinner) layout.findViewById(R.id.SynthSpin);
            	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
						TextView tvx = (TextView) selectedItemView;
						syn = tvx.getText().toString();
					}

					public void onNothingSelected(AdapterView<?> parentView) { }
            	});
            	
            	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(owner, android.R.layout.simple_spinner_item, f.list());
            	
            	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            	spinner.setAdapter(adapter);
            	
            	int spinnerPosition = adapter.getPosition("default.scsyndef");
            	spinner.setSelection(spinnerPosition);
            	
            	//Options to submit to server/remove the place
            	CheckBox submit = (CheckBox) layout.findViewById(id.dialogSubmit);
            	submit.setVisibility(0);
            	CheckBox check = (CheckBox) layout.findViewById(R.id.dialogRemove);
            	check.setVisibility(4);
            	
            	dialog.setTitle("Add Area");
            	dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	//Try to create the area!
                    	if (owner.getCartographer().getAreaPoints().size() <3) { //aww, how sweet. Check to make sure that we actually have a polygon.
                    		dialog.dismiss();
                    		owner.createAreaMode = true;
                    		Toast.makeText(owner, "Area must have at least three vertices!", 3000).show();
                    		return; //back to createAreaMode so they can add more vertices.
                    	}
                    	EditText et = (EditText) layout.findViewById(R.id.dialogEditName);
                    	name = et.getText().toString();
                    	owner.areaCreated = addAreaDestination(owner.getCartographer().getAreaPoints(), syn, name);
                    	Toast.makeText(owner, "Added Area Location: " + name + "\nSynth: " + syn, 3000).show();
                    	owner.getCartographer().getMapView().invalidate();
                	}
            	});
            	dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                	}
            	});
            	AlertDialog d = dialog.create();
            	d.show();
    		}
    	});
    	return owner.areaCreated;
    }
    
	/**
	 * The user long-pressed on the map. Give them the dialog to create a synth/location mapping there
	 * NOTE: most of this is similar to createAreaLocation.
	 * TODO: we should add in an option for the radius of this synth here.
	 * @param point at which we are adding a mapping
	 */
    public void createPointLocation(final GeoPoint point) {
    	owner.runOnUiThread(new Runnable() {
        	String name, syn = "Micromoog.scsyndef";
            public void run() {
            	File f=new File("/sdcard/supercollider/synthdefs");
            	AlertDialog.Builder dialog = new AlertDialog.Builder(owner);
            	LayoutInflater inflater = (LayoutInflater) owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	final View layout = inflater.inflate(R.layout.dialog, (ViewGroup) owner.findViewById(R.id.dialogroot));
            	dialog.setView(layout);
            	
            	Spinner spinner = (Spinner) layout.findViewById(R.id.SynthSpin);
            	
            	CheckBox check = (CheckBox) layout.findViewById(R.id.dialogRemove);
            	check.setVisibility(4);
            	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
						TextView tvx = (TextView) selectedItemView;
						syn = tvx.getText().toString();
					}

					public void onNothingSelected(AdapterView<?> parentView) { }
            	});
            	
            	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(owner, android.R.layout.simple_spinner_item, f.list());
            	
            	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            	spinner.setAdapter(adapter);
            	
            	int spinnerPosition = adapter.getPosition("default.scsyndef");
            	spinner.setSelection(spinnerPosition);
            	
            	dialog.setTitle("Add Destination");
            	dialog.setMessage("Latitude: " + (double)(point.getLatitudeE6() * 1E-6) + "\nLongitude: " + (double)(point.getLongitudeE6() * 1E-6));
            	dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	EditText et = (EditText) layout.findViewById(R.id.dialogEditName);
                    	name = et.getText().toString();
                    	PointGeoSynth p = addDestination(point, syn, name);
                    	Toast.makeText(owner, "Added Point Location: " + name + "\nSynth: " + syn + "\nLatitude: " + (double)(point.getLatitudeE6() * 1E-6) + "\nLongitude: " + 
                    			(double)(point.getLongitudeE6() * 1E-6), 3000).show();
                    	owner.getCartographer().getMapView().invalidate();
                    	CheckBox submit = (CheckBox) layout.findViewById(R.id.dialogSubmit);
                    	if (submit.isChecked()) {
                    		owner.getServerHook().submitPoint(p);
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
        });
    }
    
    /**
     * This adds a point to the vertex list on an area location.
     * 
     * @param point to add.
     */
    public void createAreaLocationPoint(final GeoPoint point) {
    	owner.getCartographer().getAreaPoints().add(point);
    	owner.getCartographer().getAreaCreationOverlay().addOverlay(new OverlayItem(point, null, null), point);
    }
    
    /**
     * Add a location/synth mapping onto the map.
     * 
     * @param p point
     * @param syn synth name
     * @param name location name
     * @return the PointPlace created
     */
    public PointGeoSynth addDestination(GeoPoint p, String syn, String name) {
        return addDestination(p.getLatitudeE6() * 1E-6, p.getLongitudeE6() * 1E-6, syn, name);
    }
    
    /**
     * Add a location/synth mapping onto the map.
     * 
     * @param p PointPlace at which we're adding the mapping
     * @return sending p on through
     */
    public PointGeoSynth addDestination(PointGeoSynth p) {
    	playCount++;
    	getDestinations().put(playCount, p);
    	owner.getCartographer().getItemizedOverlay().addOverlay(new IndexedOverlayItem(p.getOverlayItem(), playCount));
        Location dest = new Location("From List");
		dest.setLongitude(p.getLon());
		dest.setLatitude(p.getLat());
		testLocation(owner.getLocationeer().getCurrentLocation());
		return p;
    }
    
    /**
     * IMPORTANT! links a physical location with an SC synth and puts it on the map. starts audio if appropriate. IMPORTANT!
     * this will be used for users and normal locations and will take some modification to determine which is used
     * 
     * @param lat of the location
     * @param lon of the location
     * @param syn synth name
     * @param name mapping name
     * @return created PointPlace
     */
    public PointGeoSynth addDestination(double lat, double lon, String syn, String name) {
    	playCount++;
    	PointGeoSynth addPlace = new PointGeoSynth(lat, lon, playCount, syn, name, false);
    	getDestinations().put(playCount, addPlace);
        owner.getCartographer().getItemizedOverlay().addOverlay(new IndexedOverlayItem(addPlace.getOverlayItem(), playCount));
		testLocation(owner.getLocationeer().getCurrentLocation());
		return addPlace;
    }
    
    /** 
     * Call this function on location's parts
     * 
     * @param l location to add the destination
     * @param syn synth name
     * @param name mapping name
     * @return created PointPlace
     */
    public PointGeoSynth addDestination(Location l, String syn, String name) {
    	return addDestination(l.getLatitude(), l.getLongitude(), syn, name);
    }
    
    /**
     * Add a polygon area to the map/mappings
     * 
     * @param points vertices of the polygon
     * @param syn synth name
     * @param name mapping name
     * @return pass or fail - failure is probably from having too few points to be a polygon.
     */
    public boolean addAreaDestination(List<GeoPoint> points, String syn, String name) {
    	playCount++;
    	PolygonGeoSynth addArea = new PolygonGeoSynth(points, playCount, name, syn, false);
    	getDestinations().put(playCount, addArea);
    	owner.getCartographer().getItemizedOverlay().addOverlay(new IndexedOverlayItem(addArea.getOverlayItem(), playCount));
    	owner.getCartographer().setMapOverlays(owner.getCartographer().getMapView().getOverlays());
		owner.getCartographer().getMapOverlays().remove(owner.getCartographer().getAreaCreationOverlay());
		testLocation(owner.getLocationeer().getCurrentLocation());
    	return true;
    }
    
    //determines which destinations should be playing after a new location fix or a resume/preference change
    /**
     * Play audio from all mappings that location l is contained within. Notify the server of changes if locations are downloaded.
     * 
     * @param l the location to test
     */
    public void testLocation(Location l) {
    	if (owner.getPreferences().getBoolean("play_other_audio", true)) {
    		Collection<GeoSynth> places = getDestinations().values();
    		for (GeoSynth place : places) {
    			switch (place.testPoint(l)) {
    				case 1:
    					owner.getScManager().startAudio(place.index, place.getSynthDef().replace(".scsyndef", ""));
    					if (place.fromServer) {
    						owner.getServerHook().notifyServerLocationEnter(place);
    					}
    					break;
    				case 0:
    					owner.getScManager().stopAudio(place.index);
    					if (place.fromServer) {
    						owner.getServerHook().notifyServerLocationExit(place);
    					}
    					break;
    				default: //this is probably case -1, which means no change.
    					break;
    			}
    		}
    	}
    	else owner.getScManager().stopAllOutsideAudio();
    }

	public Map<Object, GeoSynth> getDestinations() {
		return destinations;
	}

	public void setDestinations(Map<Object, GeoSynth> destinations) {
		this.destinations = destinations;
	}
}