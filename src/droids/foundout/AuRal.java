package droids.foundout;

import java.util.ArrayList;

import net.sf.supercollider.android.OscMessage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

public class AuRal extends MapActivity {
	
	//generally useful stuff across the board
	private SharedPreferences preferences;
	private static final String TAG = "AuRal"; //for logs
	private TextView tv1, tv2;
	
	private Locationeer locationeer;
	private AuraManager auraManager;
	private SCManager scManager;
	private ServerHook serverHook;
	private Cartographer cartographer;
	
	//states
	boolean createAreaMode = false;
	boolean areaCreated = true;
    
	/**
	 * Set up the main view, initialize all the moving parts.
	 * 
	 * @param savedInstanceState
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setPreferences(PreferenceManager.getDefaultSharedPreferences(this));
    	setContentView(R.layout.main);
    	setTv1((TextView) findViewById(R.id.latView));
    	setTv2((TextView) findViewById(R.id.longView));
        
    	setLocationeer(new Locationeer(this));
    	setAuraManager(new AuraManager(this));
    	setScManager(SCManager.getInstance());
    	getScManager().setOwner(this);
    	//scManager = new SCManager(this);
    	setServerHook(ServerHook.getInstance());
    	getServerHook().setOwner(this);
    	//serverHook = new ServerHook(this);
    	setCartographer(new Cartographer(this));
    }
    
    /**
     * Update preferences and server connection in serverHook. Start listening for location updates. Play appropriate audio.
     */
    public void onResume() {
    	super.onResume();
    	
    	getServerHook().updateIP(getPreferences().getString("ip_number", "aural.allisonic.com"));
		getServerHook().updateUser(getPreferences().getString("username", ""), getPreferences().getString("password", ""));
		getLocationeer().startLocationUpdates(); //get the locationListener up and running again
		
		String s = getPreferences().getString("listPref", "Micromoog");
		s = s.replace(".scsyndef", "");
		if (!getScManager().getSelectedSynth().replace(".scsyndef", "").equals(s)) {
			getScManager().setSelectedSynth(s);
			getScManager().stopPersonal();
			getScManager().getSc().sendMessage(new OscMessage( new Object[] {"s_new", getScManager().getSelectedSynth().replace(".scsyndef", ""), 100, 0, 0}));
		}
		getScManager().playPersonal(getPreferences().getBoolean("play_personal_audio", false)); //play or pause personal audio based on preferences
		
		if (getPreferences().getBoolean("play_personal_audio", false) == true) {
			getScManager().getSc().sendMessage(new OscMessage(new Object[] { "n_set", 100, "r", (float)(getPreferences().getInt("Pslider1", 0)/1000.0) }));
		}
		/**if (preferences.getBoolean("play_other_audio", false) == true) {
			serverHook.changeServerParams();
		}*/
		getAuraManager().testLocation(getLocationeer().getCurrentLocation()); //determine which tracks (if any) to play and start them
    }
    
    /**
     * Stop location updates, call super.onPause
     */
    @Override
	public void onPause() {
    	getLocationeer().stopLocationUpdates();
		super.onPause();
	}
	
    /**
     * Stop SC server, close off the ServerHook, stop location updates, call super.destroy
     */
	@Override
	public void onDestroy() {
		//if (scManager.sc!=null) scManager.sc.stop();
		getScManager().stopPersonal();
		getScManager().getSc().sendQuit();
		getScManager().getSc().closeUDP();
		getServerHook().closeOSC();
		getLocationeer().stopLocationUpdates();
		getServerHook().logOut();
		super.onDestroy();
	}
	
	/**
	 * Stop SC server, close off the ServerHook, stop location updates, call super.finish
	 */
	@Override
	public void finish() {
		//if (scManager.sc!=null) scManager.sc.stop();
		getScManager().stopPersonal();
		getScManager().getSc().sendQuit();
		getScManager().getSc().closeUDP();
		getServerHook().closeOSC();
		//serverHook.oscReceiver.stopListening();
		getLocationeer().stopLocationUpdates();
		getServerHook().logOut();
		super.finish();
	}
    
    /**
     * Inflates the menu from xml
     * 
     * @param menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

    /**
     * Called when the menu button is pressed. Populate the menu with the appropriate options.
     * 
     * @param menu
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
    	//ID 0 means that the user is not logged into the server.
		if (getServerHook().getID() == 0) {
			menu.setGroupVisible(R.id.offlineGroup, true);
			menu.setGroupVisible(R.id.onlineGroup, false);
		}
		else {
			menu.setGroupVisible(R.id.offlineGroup, false);
			menu.setGroupVisible(R.id.onlineGroup, true);
		}
		if (createAreaMode) {
			menu.setGroupVisible(R.id.createAreaGroup, true);
			menu.setGroupVisible(R.id.standardGroup, false);
			menu.setGroupVisible(R.id.offlineGroup, false);
			menu.setGroupVisible(R.id.onlineGroup, false);
		}
		else {
			menu.setGroupVisible(R.id.createAreaGroup, false);
			menu.setGroupVisible(R.id.standardGroup, true);
		}
    	return true;
    }
    
    //handles menu item selection
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.preferences: //launch the preferences Activity
			Intent i = new Intent(AuRal.this, Preferences.class);
			startActivity(i);
			break;
		case R.id.connect: //download all locations form server
			new Thread() {
	        	public void run() {
	        		getServerHook().getLocationsFromServer();
	        	}
			}.run();
    		getCartographer().getMapView().invalidate();
    		getAuraManager().testLocation(getLocationeer().getCurrentLocation());
			break;
		case R.id.createArea: //start create area mode. 
			getCartographer().setAreaPoints(new ArrayList<GeoPoint>());
			Drawable drawable = this.getResources().getDrawable(R.drawable.circlered);
			getCartographer().setAreaCreationOverlay(new PolygonOverlay(drawable/**, this*/));
			createAreaMode = true;
			break;
		case R.id.finalizeCreate: //create the area and drop back into normal mode
			areaCreated = getAuraManager().createAreaLocation();
			getAuraManager().testLocation(getLocationeer().getCurrentLocation());
			createAreaMode = !areaCreated;
			getCartographer().getMapView().invalidate();
			break;
		case R.id.cancelCreate: //drop the area data and go back to normal mode
			getCartographer().setMapOverlays(getCartographer().getMapView().getOverlays());
			getCartographer().getMapOverlays().remove(getCartographer().getAreaCreationOverlay());
			createAreaMode = false;
			getCartographer().getMapView().invalidate();
			break;
		case R.id.login: //log into the server
			getServerHook().logIn();
			break;
		case R.id.logout: //log out of the server
			getServerHook().logOut();
			break;
		case R.id.newUser: //create a new user on the server
			getServerHook().createUser();
			break;
		case R.id.editUser: //modify existing user on the server
			getServerHook().modifyUser();
			break;
		case R.id.instrument: //launch the instrument control Activity
			Intent instrumentIntent = new Intent(AuRal.this, DirectInput.class);
			startActivity(instrumentIntent);
			break;
		}
		return true;
	}

	//needed for extending MapActivity to not complain.
	protected boolean isRouteDisplayed() { return false; }

	public Cartographer getCartographer() {
		return cartographer;
	}

	public void setCartographer(Cartographer cartographer) {
		this.cartographer = cartographer;
	}

	public ServerHook getServerHook() {
		return serverHook;
	}

	public void setServerHook(ServerHook serverHook) {
		this.serverHook = serverHook;
	}

	public Locationeer getLocationeer() {
		return locationeer;
	}

	public void setLocationeer(Locationeer locationeer) {
		this.locationeer = locationeer;
	}

	public SharedPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	public SCManager getScManager() {
		return scManager;
	}

	public void setScManager(SCManager scManager) {
		this.scManager = scManager;
	}

	public AuraManager getAuraManager() {
		return auraManager;
	}

	public void setAuraManager(AuraManager auraManager) {
		this.auraManager = auraManager;
	}

	public TextView getTv1() {
		return tv1;
	}

	public void setTv1(TextView tv1) {
		this.tv1 = tv1;
	}

	public TextView getTv2() {
		return tv2;
	}

	public void setTv2(TextView tv2) {
		this.tv2 = tv2;
	}
}