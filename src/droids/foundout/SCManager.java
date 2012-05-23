package droids.foundout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import net.sf.supercollider.android.OscMessage;
import net.sf.supercollider.android.SCAudio;

import android.util.Log;
import android.widget.Toast;

//This class handles all SuperCollider interactions.
public class SCManager {
	
	static private SCManager _instance;
	
	private AuRal owner;
	//SuperCollider stuff
	private static final String dllDirStr = "/data/data/net.sf.supercollider.android/lib";
	SCAudio sc;
	String selectedSynth;
	
	public SCManager(AuRal g) {
		owner = g;

        //adding in all of the user's synthDefs
        deliverSynthDefs("");
        //start up the SC audio
    	new Thread() {
    		public void run() {
	    		sc = new SCAudio(dllDirStr);
	            sc.start();
    		}
    	}.run();
    	selectedSynth = owner.preferences.getString("listPref", "default");
    	if (owner.preferences.getBoolean("play_personal_audio", false) == true)
    		sc.sendMessage(new OscMessage( new Object[] {"s_new", selectedSynth.replace(".scsyndef", ""), 100, 0, 1}));
    	else 
    		sc.sendMessage(new OscMessage( new Object[] {"s_new", selectedSynth.replace(".scsyndef", ""), 100, 0, 0}));
    		
	}
	
	private SCManager() {
		
	}
	
	public void setOwner(AuRal owner) {
		this.owner = owner;
		//adding in all of the user's synthDefs
        deliverSynthDefs("");
        //start up the SC audio
    	new Thread() {
    		public void run() {
	    		sc = new SCAudio(dllDirStr);
	            sc.start();
    		}
    	}.run();
    	selectedSynth = owner.preferences.getString("listPref", "default");
    	if (owner.preferences.getBoolean("play_personal_audio", false) == true)
    		sc.sendMessage(new OscMessage( new Object[] {"s_new", selectedSynth.replace(".scsyndef", ""), 100, 0, 1}));
    	else 
    		sc.sendMessage(new OscMessage( new Object[] {"s_new", selectedSynth.replace(".scsyndef", ""), 100, 0, 0}));
	}
	
	static synchronized public SCManager getInstance() 
	{
	    if (_instance == null) 
	      _instance = new SCManager();
	    return _instance;
	 }
	
	
	//called when the preference box for personal is changed: play or pause it
    public void startPersonal(Boolean b) {
    	if (b) {
    		sc.sendMessage(new OscMessage( new Object[] {"n_run", 100, 1})); //running
    	}
    	else {
    		sc.sendMessage(new OscMessage( new Object[] {"n_run", 100, 0})); //not running
    	}
    }
    
    //frees personal audio synth
    public void stopPersonal() {
    	sc.sendMessage(new OscMessage( new Object[] {"n_free", 100}));
    }
    
    //generating a new SC synth here.
    public void startAudio(int n, String synth) {
    	sc.sendMessage(new OscMessage( new Object[] {"s_new", synth, 100+n, 0, 1}));
    	
    	/**if (synth.equals("test_nime_mel_drum")) {
    		sc.sendMessage(new OscMessage( new Object[] {"n_set", 100+n, "param_1", 0, "param_2", 0, "param_3", 0} ));
    	}*/
    }
    
    //frees an individual audio completely
    public void stopAudio(int n) {
    	sc.sendMessage(new OscMessage( new Object[] {"n_free", 100+n}));
    }
    
    //TODO: we should be taking in an index and a list of objects to update all the params.
    public void updateParams(int index, Object o, Object o2, Object o3) { 
    	sc.sendMessage(new OscMessage( new Object[] {"n_set", 100+index, "param_1", o, "param_2", o2, "param_3", o3} ));
    	Log.e("SCManager", "param_1: "+ o + ", param_2: " + o2 + ", param_3: " + o3);
    	//sc.sendMessage(new OscMessage( new Object[] {"n_set", 100+index, "r", o, "v", o2, "m", o3} ));
    }
    
    //called if an audio synth should be replaced for a given destination (by index)
    public void updateAudio(int n, String current, String synth) {
    	if (! current.equals(synth)) {
	    	stopAudio(n);
	    	//startAudio(n, synth);
    	}
    }
    
    //calls stopAudio on every synth
    public void stopAllOutsideAudio() {
    	Collection<Place> places = owner.auraManager.destinations.values();
    	for (Place place : places) {
	    	if (place.play == true) {
				stopAudio(place.index);
				place.play = false;
	    	}
    	}
    }

	//put all of the definitions in the filesystem
	public void deliverSynthDefs(String directory) { // "" defaults to assets folder
		try {
			String[] names = owner.getAssets().list(directory);
			for (String filename : names) {
				if (filename.contains(".scsyndef")) { //we only want .scsyndef files
					deliverSynthDef(filename);
					//Toast.makeText(this, filename, 3000).show();
				}
			}
		} catch (IOException e) {
			Toast.makeText(owner, e.getMessage(), 3000).show();
		}
	}

	//put the new definition in the filesystem
	public void deliverSynthDef(String s) { //expecting s to be of form: *.scsyndef
		try {
			InputStream is = owner.getAssets().open(s);
			OutputStream os = new FileOutputStream("/sdcard/supercollider/synthdefs/" + s);
			byte[] buf = new byte[1024];
			int bytesRead = 0;
			while (-1 != (bytesRead = is.read(buf))) {
				os.write(buf,0,bytesRead);
			}
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}