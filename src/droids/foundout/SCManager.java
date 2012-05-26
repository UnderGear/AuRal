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

/**
 * This class handles all SuperCollider interactions.
 * @author UnderGear
 *
 */
public class SCManager {
	
	static private SCManager _instance;
	
	private AuRal owner;
	private static final String dllDirStr = "/data/data/net.sf.supercollider.android/lib";
	private SCAudio sc;
	private String selectedSynth;
	
	/**
	 * Constructor
	 */
	private SCManager() { }
	
	/**
	 * Sets up the SC Manager - could not be called in constructor because this is a static instance.
	 * 
	 * @param owner AuRal main activity
	 */
	public void setOwner(AuRal owner) {
		this.owner = owner;
		//adding in all of the user's synthDefs
        deliverSynthDefs("");
        //start up the SC audio
    	new Thread() {
    		public void run() {
	    		setSc(new SCAudio(dllDirStr));
	            getSc().start();
    		}
    	}.run();
    	setSelectedSynth(owner.getPreferences().getString("listPref", "default"));
    	if (owner.getPreferences().getBoolean("play_personal_audio", false) == true)
    		getSc().sendMessage(new OscMessage( new Object[] {"s_new", getSelectedSynth().replace(".scsyndef", ""), 100, 0, 1}));
    	else 
    		getSc().sendMessage(new OscMessage( new Object[] {"s_new", getSelectedSynth().replace(".scsyndef", ""), 100, 0, 0}));
	}
	
	/**
	 * Static instance access.
	 * 
	 * @return static instance of this class
	 */
	static synchronized public SCManager getInstance() 
	{
	    if (_instance == null) 
	      _instance = new SCManager();
	    return _instance;
	 }
	
	/**
	 * Called when the preference box for personal is changed: play or pause it
	 * 
	 * @param b whether to play or pause the synth.
	 */
    public void playPersonal(Boolean b) {
    	if (b) 
    		getSc().sendMessage(new OscMessage( new Object[] {"n_run", 100, 1})); //running
    	else 
    		getSc().sendMessage(new OscMessage( new Object[] {"n_run", 100, 0})); //not running
    }
    
    /**
     * frees personal audio synth
     */
    public void stopPersonal() {
    	getSc().sendMessage(new OscMessage( new Object[] {"n_free", 100}));
    }
    
    /**
     * generating a new SC synth here.
     * 
     * @param n + 100 is the index we will place this synth at
     * @param synth name of the synth to add
     */
    public void startAudio(int n, String synth) {
    	getSc().sendMessage(new OscMessage( new Object[] {"s_new", synth, 100+n, 0, 1}));
    }
    
    /**
     * frees a particular synth completely
     * 
     * @param n + 100 is the synth to free
     */
    public void stopAudio(int n) {
    	getSc().sendMessage(new OscMessage( new Object[] {"n_free", 100+n}));
    }
    
    //TODO: we should be taking in an index and a list of objects to update all the params.
    /**
     * Send in new parameters to a particular synth
     * 
     * @param index + 100 is the synth to modify
     * @param o1 param 1 value
     * @param o2 param 2 value
     * @param o3 param 3 value
     */
    public void updateParams(int index, Object o1, Object o2, Object o3) { 
    	getSc().sendMessage(new OscMessage( new Object[] {"n_set", 100+index, "param_1", o1, "param_2", o2, "param_3", o3} ));
    	Log.e("SCManager", "param_1: "+ o1 + ", param_2: " + o2 + ", param_3: " + o3);
    }
    
    /** TODO: look at this and make sure it is correct. I have my doubts.
     * called if an audio synth should be replaced for a given destination (by index)
     * 
     * @param n
     * @param current
     * @param synth
     */
    public void updateAudio(int n, String current, String synth) {
    	if (! current.equals(synth)) {
	    	stopAudio(n + 100);
	    	//startAudio(n + 100, synth);
    	}
    }
    
    /**
     * Stop all synths other than personal.
     */
    public void stopAllOutsideAudio() {
    	Collection<GeoSynth> places = owner.getAuraManager().getDestinations().values();
    	for (GeoSynth place : places) {
	    	if (place.play == true) {
				stopAudio(place.index);
				place.play = false;
	    	}
    	}
    }

	/**
	 * put all of the compiled synth definitions from assets in the filesystem
	 * 
	 * @param directory to place synths
	 */
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

	/**
	 * put the new definition in the filesystem
	 * 
	 * @param s synth to open
	 */
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

	public SCAudio getSc() {
		return sc;
	}

	public void setSc(SCAudio sc) {
		this.sc = sc;
	}

	public String getSelectedSynth() {
		return selectedSynth;
	}

	public void setSelectedSynth(String selectedSynth) {
		this.selectedSynth = selectedSynth;
	}
}