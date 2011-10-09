package droids.foundout;

import java.io.File;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    ListPreference lP = (ListPreference) findPreference("listPref");
	    File f=new File("/sdcard/supercollider/synthdefs");
	    lP.setEntryValues(f.list());
	    String[] fileNames = f.list();
	    try {
		    for (String name : fileNames) {
		    	name = name.replace(".scsyndef", "");
		    }
	    }
	    catch (Exception e) {
	    }
	    lP.setEntries(fileNames);
	}
}