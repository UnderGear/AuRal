package droids.foundout;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * This class manages the Google Maps map view and its overlays.
 * @author UnderGear
 *
 */
public class Cartographer {

	private AuRal owner;

	//for the Google Maps view
	private MyMapView mapView;
	private List<Overlay> mapOverlays;
	private MainOverlay itemizedOverlay; //standard overlay to use
	private PolygonOverlay areaCreationOverlay;
	private MapController mapController;
	private List<GeoPoint> areaPoints; //used when constructing
	
	/**
	 * Constructor
	 * 
	 * @param owner AuRal main activity
	 */
	public Cartographer(AuRal owner) {
		this.owner = owner;
		setUpMap();
	}
	
	/**
	 * Sets the map controller, a longpress listener, and the overlays.
	 */
    public void setUpMap() {
    	setMapView((MyMapView) owner.findViewById(R.id.mapview));
    	setMapController(getMapView().getController());
    	getMapView().setOnLongpressListener(new MyMapView.OnLongpressListener() {
    		//Longpress pulls up dialog for adding destinations
            public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
            	owner.runOnUiThread(new Runnable() {
            		public void run() {
                    	if (owner.createAreaMode) {
                    		owner.getAuraManager().createAreaLocationPoint(longpressLocation);
                    		owner.getAuraManager().testLocation(owner.getLocationeer().getCurrentLocation());
                    		setMapOverlays(getMapView().getOverlays());
                    		if (!getMapOverlays().contains(getAreaCreationOverlay())) {
                    			getMapOverlays().add(getAreaCreationOverlay());
                    		}
                    	}
                    	else owner.getAuraManager().createPointLocation(longpressLocation);
            		}
            	});
            }
    	});

    	setMapOverlays(getMapView().getOverlays());
    	getMapOverlays().clear();
    	Drawable drawable = owner.getResources().getDrawable(R.drawable.circle);
    	setItemizedOverlay(new MainOverlay(drawable, owner));
    	getMapOverlays().add(getItemizedOverlay());
    	getMapView().invalidate();
    }

	public MyMapView getMapView() {
		return mapView;
	}

	public void setMapView(MyMapView mapView) {
		this.mapView = mapView;
	}

	public List<Overlay> getMapOverlays() {
		return mapOverlays;
	}

	public void setMapOverlays(List<Overlay> mapOverlays) {
		this.mapOverlays = mapOverlays;
	}

	public MapController getMapController() {
		return mapController;
	}

	public void setMapController(MapController mapController) {
		this.mapController = mapController;
	}

	public List<GeoPoint> getAreaPoints() {
		return areaPoints;
	}

	public void setAreaPoints(List<GeoPoint> areaPoints) {
		this.areaPoints = areaPoints;
	}

	public PolygonOverlay getAreaCreationOverlay() {
		return areaCreationOverlay;
	}

	public void setAreaCreationOverlay(PolygonOverlay areaCreationOverlay) {
		this.areaCreationOverlay = areaCreationOverlay;
	}

	public MainOverlay getItemizedOverlay() {
		return itemizedOverlay;
	}

	public void setItemizedOverlay(MainOverlay itemizedOverlay) {
		this.itemizedOverlay = itemizedOverlay;
	}
}