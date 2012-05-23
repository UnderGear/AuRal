package droids.foundout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class DirectInput extends Activity {

	SCManager scManager;
	ServerHook serverHook;
	
	private String TAG = "AuRal Direct Input";
	
	private WebView webView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        scManager = SCManager.getInstance();
        serverHook = ServerHook.getInstance();
        
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
    
    final class AuRalJavaScriptInterface {

        AuRalJavaScriptInterface() {
        }

        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        public int get_id() {
        	return serverHook.getID();
        }
        
        public void slider_value(int id, String value) {
            //Log.e(TAG, "id: " + id + ", value: " + value);
            //serverHook.changeServerParams(id, value);
        }
        
    }

    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
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
