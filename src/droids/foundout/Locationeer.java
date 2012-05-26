package droids.foundout;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * The Locationeer class handles the user's current location, listens for updates, and determines if they are more or less trustworthy than the current one
 * @author UnderGear
 *
 */
public class Locationeer {

	private AuRal owner;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location currentLocation;
	
	private String TAG = "Locationeer";
	
	public Location getCurrentLocation() { return currentLocation; }
	
	/**
	 * Constructor
	 * 
	 * @param owner AuRal main activity
	 */
	public Locationeer(final AuRal owner) {
		this.owner = owner;
		
		//for getting location fixes
		//TODO: maybe this should be an asynctask.
        new Thread() {
        	public void run() {
        		locationManager = (LocationManager) owner.getSystemService(Context.LOCATION_SERVICE);
        		locationListener = new myLocationListener();
        	}
        }.run();
        currentLocation = new Location("Server"); //IMPORTANT! to be used as current location from now on: IMPORTANT!
	}
	
	/**
	 * The myLocationListener class is used to determine if new locations fixes are good. It then moves the marker on the map and sends locations to the server via ServerHook
	 * @author UnderGear
	 *
	 */
    //TODO: refine the provider lose/find cases
    public class myLocationListener implements LocationListener {
    	
    	/**
    	 * Check if the new location is more trustworthy than the old. Update our location and tell the server if so.
    	 * 
    	 * @param location to be tested.
    	 */
		public void onLocationChanged(final Location location) {
			//TODO: again, maybe an asynctask.
			new Thread() {
				public void run() {
					// determine if the new location fix is better than the current one.
		    		if (isBetterLocation(location, currentLocation)) {
		    			if (location.distanceTo(currentLocation) >= 5) {
			    			currentLocation = location;
			    			//mapController.animateTo(new GeoPoint((int)(mostReliable.getLongitude()*1E6), (int)(mostReliable.getLatitude()*1E6)));
			    			GeoPoint p = new GeoPoint((int)(currentLocation.getLatitude()*1E6), (int)(currentLocation.getLongitude()*1E6));
			    	    	OverlayItem overlayitem = new OverlayItem(p, "", "");
			    			owner.getCartographer().getItemizedOverlay().moveMe(overlayitem);
			    	        Log.d(TAG, currentLocation.getLatitude() + "");
			    	        Log.d(TAG, currentLocation.getLongitude() + "");
			    	        owner.getTv1().setText("Current Latitude: " + currentLocation.getLatitude());
			    	        owner.getTv2().setText("Current Longitude: " + currentLocation.getLongitude());
			    	        owner.getAuraManager().testLocation(currentLocation);
			    	        owner.getCartographer().getMapView().invalidate();
			    	        owner.getServerHook().sendMyLocation();
		    			}
		    		}
				}
			}.run();
    	}

		public void onProviderDisabled(String provider) { }

		public void onProviderEnabled(String provider) { }

		public void onStatusChanged(String provider, int status, Bundle extras) { }
    };
    
    /**
     * Start checking for location updates from Network and GPS if available
     */
	public void startLocationUpdates() {
    	if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        else {
        	Toast.makeText(owner, "No network Location provider", 3000).show();
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else {
        	Toast.makeText(owner, "GPS provider is disabled", 3000).show();
        }
        //Update the location textviews to show the current location.
        //0, 0 means we have yet to get a read. show ? instead of 0.
        if (currentLocation.getLatitude() == 0) owner.getTv1().setText("Current Latitude: ?");
        else owner.getTv1().setText("Current Latitude: " + currentLocation.getLatitude());
        if (currentLocation.getLongitude() == 0) owner.getTv2().setText("Current Longitude: ?");
        else owner.getTv2().setText("Current Longitude: " + currentLocation.getLongitude());
    }

	/**
	 * Stop listening for updates.
	 */
    public void stopLocationUpdates() {
    	locationManager.removeUpdates(locationListener);
    }
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * NOTE: most of this method is from developer.android.com
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    
    // Checks whether two providers are the same
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}