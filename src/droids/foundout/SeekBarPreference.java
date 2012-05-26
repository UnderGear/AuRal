package droids.foundout;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * Seek bar in the preference menu.
 * @author UnderGear
 *
 */
public class SeekBarPreference extends DialogPreference {
	
     private Context context;
     private SeekBar seek;

     /**
      * Constructor
      * 
      * @param context
      * @param attrs
      */
     public SeekBarPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.context = context;
     }

     /**
      * Set up the seek bar for use
      * 
      * @param builder
      */
     protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

         LinearLayout layout = new LinearLayout(context);
         layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
         layout.setMinimumWidth(400);
         layout.setPadding(20, 20, 20, 20);

         seek = new SeekBar(context);
         seek.setMax(1000);
         seek.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         seek.setProgress(getPersistedInt(0)); // You might want to fetch the value here from sharedpreferences
         
         layout.addView(seek);

         builder.setView(layout);

         super.onPrepareDialogBuilder(builder);
     }

     /**
      * put the value into the preferences.
      * 
      * @param isPositive
      */
     protected void onDialogClosed(boolean isPositive) {
         if (isPositive) {
             persistInt(seek.getProgress());
         }
     }
 }