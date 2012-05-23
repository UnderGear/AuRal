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

public class AuraManager {
	private AuRal owner;
	private int playCount = 0; //gives SC synths an index corresponding to the number of locations
	Map<Object, Place> destinations = new HashMap<Object, Place>(); //this maps index to place - supercollider to location interface.
	
	public AuraManager(AuRal g) {
		owner = g;
	}
	
	public boolean createAreaLocation() {
    	owner.runOnUiThread(new Runnable() {
			String name, syn = "Micromoog.scsyndef";
    		public void run() {
    			//directory of all synthdefs
            	File f=new File("/sdcard/supercollider/synthdefs");
            	//TODO: we need a check box for sending to server or not?
            	AlertDialog.Builder dialog = new AlertDialog.Builder(owner);
            	LayoutInflater inflater = (LayoutInflater) owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	final View layout = inflater.inflate(R.layout.dialog, (ViewGroup) owner.findViewById(R.id.dialogroot));
            	dialog.setView(layout);
            	
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
            	
            	CheckBox submit = (CheckBox) layout.findViewById(id.dialogSubmit);
            	submit.setVisibility(0);
            	
            	CheckBox check = (CheckBox) layout.findViewById(R.id.dialogRemove);
            	check.setVisibility(4);
            	
            	dialog.setTitle("Add Area");
            	dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	if (owner.cartographer.areaPoints.size() <3) { //aww, how sweet.
                    		dialog.dismiss();
                    		owner.createAreaMode = true;
                    		Toast.makeText(owner, "Area must have at least three vertices!", 3000).show();
                    		return;
                    	}
                    	EditText et = (EditText) layout.findViewById(R.id.dialogEditName);
                    	name = et.getText().toString();
                    	owner.areaCreated = addAreaDestination(owner.cartographer.areaPoints, syn, name);
                    	Toast.makeText(owner, "Added Area Location: " + name + "\nSynth: " + syn, 3000).show();
                    	owner.cartographer.mapView.invalidate();
                	}
            	});
            	dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();
                        dialog.dismiss();
                	}
            	});
            	AlertDialog d = dialog.create();
            	d.show();
    		}
    	});
    	return owner.areaCreated;
    }
    
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
                    	PointPlace p = addDestination(point, syn, name);
                    	Toast.makeText(owner, "Added Point Location: " + name + "\nSynth: " + syn + "\nLatitude: " + (double)(point.getLatitudeE6() * 1E-6) + "\nLongitude: " + 
                    			(double)(point.getLongitudeE6() * 1E-6), 3000).show();
                    	owner.cartographer.mapView.invalidate();
                    	CheckBox submit = (CheckBox) layout.findViewById(R.id.dialogSubmit);
                    	if (submit.isChecked()) {
                    		owner.serverHook.submitArea(p);
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
    
    public void createAreaLocationPoint(final GeoPoint point) {
    	owner.cartographer.areaPoints.add(point);
    	owner.cartographer.areaCreationOverlay.addOverlay(new OverlayItem(point, null, null), point);
    }
    
    //must convert to Location style lat, long from Maps style - can be reworked later for efficiency
    public PointPlace addDestination(GeoPoint p, String syn, String name) {
        return addDestination(p.getLatitudeE6() * 1E-6, p.getLongitudeE6() * 1E-6, syn, name);
    }
    
    public PointPlace addDestination(PointPlace p) {
    	playCount++;
    	destinations.put(playCount, p);
    	owner.cartographer.itemizedOverlay.addOverlay(new IndexedOverlayItem(p.overlayItem, playCount));
        Location dest = new Location("From List");
		dest.setLongitude(p.lon);
		dest.setLatitude(p.lat);
		testLocation(owner.locationeer.getCurrentLocation());
		return p;
    }
    
    //IMPORTANT! links a physical location with an SC synth and puts it on the map. starts audio if appropriate. IMPORTANT!
    //this will be used for users and normal locations and will take some modification to determine which is used
    public PointPlace addDestination(double lat, double lon, String syn, String name) {
    	playCount++;
    	PointPlace addPlace = new PointPlace(lat, lon, playCount, syn, name, false);
    	destinations.put(playCount, addPlace);
        owner.cartographer.itemizedOverlay.addOverlay(new IndexedOverlayItem(addPlace.overlayItem, playCount));
		testLocation(owner.locationeer.getCurrentLocation());
		return addPlace;
    }
    
    //call this function on location's parts
    public PointPlace addDestination(Location l, String syn, String name) {
    	return addDestination(l.getLatitude(), l.getLongitude(), syn, name);
    }
    
    public boolean addAreaDestination(List<GeoPoint> points, String syn, String name) {
    	playCount++;
    	Area addArea = new Area(points, playCount, name, syn, false);
    	destinations.put(playCount, addArea);
    	owner.cartographer.itemizedOverlay.addOverlay(new IndexedOverlayItem(addArea.overlayItem, playCount));
    	owner.cartographer.mapOverlays = owner.cartographer.mapView.getOverlays();
		owner.cartographer.mapOverlays.remove(owner.cartographer.areaCreationOverlay);
		testLocation(owner.locationeer.getCurrentLocation());
    	return true;
    }
    
    //determines which destinations should be playing after a new location fix or a resume/preference change
    public void testLocation(Location l) {
    	if (owner.preferences.getBoolean("play_other_audio", false)) {
    		Collection<Place> places = destinations.values();
    		for (Place place : places) {

    			switch (place.testPoint(l)) {
    				case 1:
    					owner.scManager.startAudio(place.index, place.synthDef.replace(".scsyndef", ""));
    					if (place.fromServer) {
    						owner.serverHook.notifyServerLocationEnter(place);
    					}
    					break;
    				case 0:
    					owner.scManager.stopAudio(place.index);
    					if (place.fromServer) {
    						owner.serverHook.notifyServerLocationExit(place);
    					}
    					break;
    				default:
    					break;
    			}
    		}
    	}
    	else owner.scManager.stopAllOutsideAudio();
    }
}