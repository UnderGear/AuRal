package droids.foundout;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class Cartographer {

	AuRal owner;

	//for the Google Maps view
	MyMapView mapView;
	List<Overlay> mapOverlays;
	MainOverlay itemizedOverlay; //standard overlay to use
	AreaOverlay areaCreationOverlay;
	MapController mapController;
	List<GeoPoint> areaPoints; //used when constructing
	
	public Cartographer(AuRal g) {
		owner = g;
		setUpMap();
	}
	//setting up the map
    public void setUpMap() {
    	mapView = (MyMapView) owner.findViewById(R.id.mapview);
    	mapController = mapView.getController();
    	mapView.setOnLongpressListener(new MyMapView.OnLongpressListener() {
    		//Longpress pulls up dialog for adding destinations
            public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
            	owner.runOnUiThread(new Runnable() {
            		public void run() {
                    	if (owner.createAreaMode) {
                    		owner.auraManager.createAreaLocationPoint(longpressLocation);
                    		owner.auraManager.testLocation(owner.locationeer.getCurrentLocation());
                    		mapOverlays = mapView.getOverlays();
                    		if (!mapOverlays.contains(areaCreationOverlay)) {
                    			mapOverlays.add(areaCreationOverlay);
                    		}
                    	}
                    	else owner.auraManager.createPointLocation(longpressLocation);
            		}
            	});
            }
    	});

    	mapOverlays = mapView.getOverlays();
    	mapOverlays.clear();
    	Drawable drawable = owner.getResources().getDrawable(R.drawable.circle);
    	itemizedOverlay = new MainOverlay(drawable, owner);
    	mapOverlays.add(itemizedOverlay);
    	mapView.invalidate();
    }
}