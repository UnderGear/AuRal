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
	SharedPreferences preferences;
	static final String TAG = "AuRal"; //for logs
	TextView tv1;
	TextView tv2;
	
	Locationeer locationeer;
	AuraManager auraManager;
	SCManager scManager;
	ServerHook serverHook;
	Cartographer cartographer;
	
	//states
	boolean createAreaMode = false;
	boolean areaCreated = true;
    
	//TODO: PERFORMANCE! multithreading. - started on this, add priority
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	setContentView(R.layout.main);
    	tv1 = (TextView) findViewById(R.id.latView);
    	tv2 = (TextView) findViewById(R.id.longView);
        
    	locationeer = new Locationeer(this);
    	auraManager = new AuraManager(this);
    	scManager = SCManager.getInstance();
    	scManager.setOwner(this);
    	//scManager = new SCManager(this);
    	serverHook = ServerHook.getInstance();
    	serverHook.setOwner(this);
    	//serverHook = new ServerHook(this);
    	cartographer = new Cartographer(this);
    }
    
    //makes things work again. used when changing preferences as well as coming back to the app
    public void onResume() {
    	super.onResume();
		serverHook.updateIP(preferences.getString("ip_number", "127.0.0.1"), Integer.parseInt(preferences.getString("port_number", "3000"))); //set up IP based on preferences
		serverHook.updateUser(preferences.getString("username", ""), preferences.getString("password", ""));
		locationeer.startLocationUpdates(); //get the locationListener up and running again
		String s = preferences.getString("listPref", "Micromoog");
		s = s.replace(".scsyndef", "");
		if (!scManager.selectedSynth.replace(".scsyndef", "").equals(s)) {
			scManager.selectedSynth = s;
			scManager.stopPersonal();
			scManager.sc.sendMessage(new OscMessage( new Object[] {"s_new", scManager.selectedSynth.replace(".scsyndef", ""), 100, 0, 0}));
		}
		scManager.startPersonal(preferences.getBoolean("play_personal_audio", false)); //play or pause personal audio based on preferences
		
		if (preferences.getBoolean("play_personal_audio", false) == true) {
			scManager.sc.sendMessage(new OscMessage(new Object[] { "n_set", 100, "r", (float)(preferences.getInt("Pslider1", 0)/1000.0) }));
		}
		if (preferences.getBoolean("play_other_audio", false) == true) {
			serverHook.changeServerParams();
		}
		auraManager.testLocation(locationeer.getCurrentLocation()); //determine which tracks (if any) to play and start them
    }
    
    //TODO: Android Life Cycle!
    @Override
	public void onPause() {
		super.onPause();
		locationeer.stopLocationUpdates();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		locationeer.stopLocationUpdates();
	}
	
	@Override
	public void onDestroy() {
		//if (scManager.sc!=null) scManager.sc.stop();
		scManager.stopPersonal();
		scManager.sc.sendQuit();
		scManager.sc.closeUDP();
		serverHook.oscReceiver.close();
		locationeer.stopLocationUpdates();
		serverHook.logOut();
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		//if (scManager.sc!=null) scManager.sc.stop();
		scManager.stopPersonal();
		scManager.sc.sendQuit();
		scManager.sc.closeUDP();
		serverHook.oscReceiver.close();
		locationeer.stopLocationUpdates();
		serverHook.logOut();
		super.finish();
	}
    
    //sets up our menu for first use
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

    //called every time the hardware menu button is pressed
    public boolean onPrepareOptionsMenu(Menu menu) {
		if (serverHook.getID() == 0) {
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
		
		//TODO: new menu option for direct input. this launches a new activity with a view filled with sliders, buttons, etc. should have access to serverhook.
		
		switch (item.getItemId()) {
		case R.id.preferences:
			Intent i = new Intent(AuRal.this, Preferences.class);
			startActivity(i);
			break;
		case R.id.connect:
			new Thread() {
	        	public void run() {
	        		serverHook.getLocationsFromServer();
	        	}
			}.run();
    		cartographer.mapView.invalidate();
    		auraManager.testLocation(locationeer.getCurrentLocation());
			break;
		case R.id.createArea:
			cartographer.areaPoints = new ArrayList<GeoPoint>();
			Drawable drawable = this.getResources().getDrawable(R.drawable.circlered);
			cartographer.areaCreationOverlay = new AreaOverlay(drawable/**, this*/);
			createAreaMode = true;
			break;
		case R.id.finalizeCreate:
			areaCreated = auraManager.createAreaLocation();
			auraManager.testLocation(locationeer.getCurrentLocation());
			createAreaMode = !areaCreated;
			cartographer.mapView.invalidate();
			break;
		case R.id.cancelCreate:
			cartographer.mapOverlays = cartographer.mapView.getOverlays();
			cartographer.mapOverlays.remove(cartographer.areaCreationOverlay);
			createAreaMode = false;
			cartographer.mapView.invalidate();
			break;
		case R.id.login:
			serverHook.logIn();
			break;
		case R.id.logout:
			serverHook.logOut();
			break;
		case R.id.newUser:
			serverHook.createUser();
			break;
		case R.id.editUser:
			serverHook.modifyUser();
			break;
		}
		return true;
	}

	//needed for extending MapActivity to not complain. not sure what it does.
	protected boolean isRouteDisplayed() {
		return false;
	}
}