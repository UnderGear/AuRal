package droids.foundout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * This Activity is used to control the synths at your current locations.
 * It pulls a page down from the server which updates your params there.
 */
public class DirectInput extends Activity {

	private ServerHook serverHook;
	private String TAG = "AuRal Direct Input";
	private WebView webView;
	
	/**
	 * Set up the Web View and its JS interface.
	 * 
	 * @param savedInstanceState
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        serverHook = ServerHook.getInstance(); //we need to communicate with the server.
        
        setContentView(R.layout.web_input);
        webView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setUserAgentString("AuRal");

        webView.setWebChromeClient(new MyWebChromeClient());

        webView.addJavascriptInterface(new AuRalJavaScriptInterface(), "aural_send");
        
        try {
        	webView.loadUrl("http://" + serverHook.getURL() + "/ui/AuRal_default_ui.html");
        }
        catch (Exception e) {
        	Log.e(TAG, e.getMessage());
        }
    }
    
	/**
	 * Hooks for the server to use on your device. This is safer than passing it the entire Context. The JS side has less access to your code.
	 */
    final class AuRalJavaScriptInterface {

    	/**
    	 * Constructor
    	 */
        AuRalJavaScriptInterface() { }

        /**
         * This allows the server to find out your account ID.
         */
        public int get_id() {
        	return serverHook.getID();
        }
        
        /** Deprecated. The server side is now handling this for us instead.
        public void slider_value(int id, String value) {
            Log.d(TAG, "id: " + id + ", value: " + value);
            serverHook.changeServerParams(id, value);
        }*/
        
    }

    /**
     * Provides a hook for calling "alert" from javascript. Useful for debugging your javascript.
     * 
     * This code came from Jesse Allison, I think from an example he found somewhere. I know nothing about it.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(TAG, message);
            result.confirm();
            return true;
        }
        
        public boolean setUserID(WebView view, String url, String message, JsResult result) {
        	return true;
        }
    }
}
