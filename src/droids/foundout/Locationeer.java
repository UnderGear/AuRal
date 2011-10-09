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

public class Locationeer {

	private AuRal owner;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location mostReliable;
	
	public Location getCurrentLocation() { return mostReliable; }
	
	public Locationeer(AuRal g) {
		owner = g;
		//for getting location fixes
        new Thread() {
        	public void run() {
        		locationManager = (LocationManager) owner.getSystemService(Context.LOCATION_SERVICE);
        		locationListener = new myLocationListener();
        	}
        }.run();
        mostReliable = new Location("Server"); //IMPORTANT! to be used as current location from now on: IMPORTANT!
	}
	
	//listens for location fixes
    //TODO: refine the provider lose/find cases
    public class myLocationListener implements LocationListener {
    	//TODO: make location fixes eat less battery
		public void onLocationChanged(final Location location) {
			new Thread() {
				public void run() {
		    		if (isBetterLocation(location, mostReliable)) {
		    			if (location.distanceTo(mostReliable) >= 5) {
			    			mostReliable = location;
			    			//mapController.animateTo(new GeoPoint((int)(mostReliable.getLongitude()*1E6), (int)(mostReliable.getLatitude()*1E6)));
			    			GeoPoint p = new GeoPoint((int)(mostReliable.getLatitude()*1E6), (int)(mostReliable.getLongitude()*1E6));
			    	    	OverlayItem overlayitem = new OverlayItem(p, "", "");
			    			owner.cartographer.itemizedOverlay.moveMe(overlayitem);
			    	        Log.d(AuRal.TAG, mostReliable.getLatitude() + "");
			    	        Log.d(AuRal.TAG, mostReliable.getLongitude() + "");
			    	        owner.tv1.setText("Current Latitude: " + mostReliable.getLatitude());
			    	        owner.tv2.setText("Current Longitude: " + mostReliable.getLongitude());
			    	        owner.auraManager.testLocation(mostReliable);
			    	        owner.cartographer.mapView.invalidate();
			    	        owner.serverHook.sendMyLocation();
		    			}
		    		}
				}
			}.run();
    	}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
    };
    
  //set up the locationManager
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
        if (mostReliable.getLatitude() == 0) owner.tv1.setText("Current Latitude: ?");
        else owner.tv1.setText("Current Latitude: " + mostReliable.getLatitude());
        if (mostReliable.getLongitude() == 0) owner.tv2.setText("Current Longitude: ?");
        else owner.tv2.setText("Current Longitude: " + mostReliable.getLongitude());
    }

	//stop the locationManager
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
    
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}