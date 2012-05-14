package droids.foundout;

import android.app.Activity;
import android.os.Bundle;

public class DirectInput extends Activity {

	SCManager scManager;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        scManager = SCManager.getInstance();
        //TODO: give me a view, serverhook access, interaction.
        //TODO: should the view be generated based on current audio being played or just a preset page? horizontal paging or maybe tabs for different running synths?
	}
}
