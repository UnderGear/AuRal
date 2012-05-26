package droids.foundout;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

/**
 * This class handles connections to and from the server - both HTTP and OSC
 * @author UnderGear
 *
 */
public class ServerHook {
	
	static private ServerHook _instance;

	private AuRal owner;
	//for connection to server
	private String url; //This should be the path to the server. aural.allisonic.com or SERVER_IP:PORT_NUMBER formats
    //private int port;
    //tools for parsing XML from server
    private SAXParserFactory spf = SAXParserFactory.newInstance();
    private SAXParser parser;
    private XMLReader reader;
    private XmlHandler myXmlHandler;
    //for incoming OSC
    private OSCPortIn oscReceiver;
    private OSCListener listener;
	
	private int userID;
	private String username;
	private String password;
	
	private String TAG = "ServerHook";
	
	/**
	 * The plain text string that 
	 * 
	 * @return
	 */
	public String getURL() {
		return url;
	}
	
	/**
	 * The ID of the account the user is logged in as. 0 signifies that the user is NOT logged into the server.
	 * 
	 * @return userID
	 */
	public int getID() {
		return userID;
	}
	
	/**
	 * Get a static instance of the ServerHook
	 * 
	 * @return static instance
	 */
	static synchronized public ServerHook getInstance() 
	{
	    if (_instance == null) 
	      _instance = new ServerHook();
	    return _instance;
	 }
	
	/**
	 * Setting the owner of this static instance to the main AuRal activity.
	 * This also sets up the URL from preferences, an XML parser, and an OSC listener
	 * 
	 * @param owner main AuRal activity
	 */
	public void setOwner(final AuRal owner) {
		this.owner = owner;
		
		//Set the server URL
    	url = owner.getPreferences().getString("ip_number", "aural.allisonic.com");
    	url = url.toUpperCase();
    	url.replace("HTTP://", "");
    	
    	//Set up the XML parser
		try {
			parser = spf.newSAXParser();
			reader = parser.getXMLReader();
			myXmlHandler = new XmlHandler();
			reader.setContentHandler(myXmlHandler);
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
		
		//Set up OSC listener
		try {
			oscReceiver = new OSCPortIn(8000); //change me from default (8000?) if you need to
		} catch (SocketException e) {
			Log.e(TAG, e.getMessage());
		}
		listener = new OSCListener() {
			
			/**
			 * receiving an OSC message matching our listener "/Server". Adjust the params of our mappings.
			 * TODO: bundle along the mapping's information to make this only adjust one location.
			 * 
			 * @param time message's timestamp
			 * @param message received
			 */
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				Object o[] = message.getArguments();
				for (GeoSynth place : owner.getAuraManager().getDestinations().values()) { //TODO: will be tossed once we know exactly which location was changed.
					if (place.name.equals(o[0].toString())) {
						owner.getScManager().updateParams(place.index, o[1], o[2], o[3]);
						break;
					}
				}
			}
		};
		oscReceiver.addListener("/Server", listener);
		oscReceiver.startListening();
	}
	
	/**
	 * Constructor
	 */
	public ServerHook() { }

	/**
	 * Close the OSC port.
	 */
	public void closeOSC() {
		oscReceiver.close();
	}
	
	/**
	 * Log the user into the server - this uses the username and password in preferences.
	 */
	public void logIn() {
		//if we aren't 0, we are already logged in. this should never occur, but better safe than sorry.
		if (userID == 0) {
			//forming an HTTP request to the server.
			final HttpPut put = new HttpPut("http://"+url+"/users/device_login");
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	List<NameValuePair> hash = new ArrayList<NameValuePair>(2);
		    hash.add(new BasicNameValuePair("name", username));
	        hash.add(new BasicNameValuePair("password", password));
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
	    	//Send it off!
			try {
				put.setEntity(new UrlEncodedFormEntity(hash));
				HttpResponse response = client.execute(put);
				HttpEntity r_entity = response.getEntity();
		        String responseString = EntityUtils.toString(r_entity);
		        //Parse the response. We are expecting an int for the account ID that we will be using.
		        if (responseString.equals(""))
		        	userID = 0;
		        else {
		        	try {
		        		userID = Integer.parseInt(responseString);
		        	}
		        	catch (Exception e) {
		        		Log.e("ServerHook", e.getMessage());
		        	}
		        }
		        if (userID != 0)
		        	Toast.makeText(owner, "Logged in as " + username, 3000).show();
		        else Toast.makeText(owner, "Authentication Failed", 3000).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Modify existing user.
	 */
	public void modifyUser() {
		//similar to sendMyLocation
		if (userID != 0) {
	    	final HttpPut put = new HttpPut("http://"+url+"/users/device_edit/"+userID);
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			new Thread() {
	        	public void run() {
	    			try {
	    				//This part is going to be updating the server with your new information
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(1);
	    			    hash.add(new BasicNameValuePair("name", username));
	    		        hash.add(new BasicNameValuePair("password", password));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        // Execute HTTP Put Request
	    		        /**HttpResponse response = */client.execute(put);
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
    	}
    }
	
	/**
	 * Create a new user with current preferences.
	 */
	public void createUser() {
		if (userID == 0) {
			final HttpPut put = new HttpPut("http://"+url+"/users/device_create/");
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			new Thread() {
	        	public void run() {
	    			try {
	    				//This part is going to be updating the server with your new information
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(4);
	    				Location current = owner.getLocationeer().getCurrentLocation();
	    			    hash.add(new BasicNameValuePair("name", username));
	    		        hash.add(new BasicNameValuePair("password", password));
	    		        hash.add(new BasicNameValuePair("latitude", ""+current.getLatitude()));
	    		        hash.add(new BasicNameValuePair("longitude", ""+current.getLongitude()));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        // Execute HTTP Put Request
	    		        HttpResponse response = client.execute(put);
	    		        HttpEntity r_entity = response.getEntity();
	    		        String responseString = EntityUtils.toString(r_entity);
	    		        //Like logging in, we are expecting a user ID response
	    		        if (responseString.equals("")) {
	    		        	userID = 0;
	    		        	Toast.makeText(owner, "Create User Failed", 3000).show();
	    		        }
	    		        else {
	    		        	try {
	    		        		userID = Integer.parseInt(responseString);
	    		        	}
	    		        	catch (Exception e) {
	    		        		Log.e("ServerHook", e.getMessage());
	    		        	}
	    		        }
	    		        Toast.makeText(owner, "Create User Successful", 3000).show();
	    		        Toast.makeText(owner, "Logged in as " + username, 3000).show();
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}
	
	/**
	 * Log out of the account
	 */
	public void logOut() {
		//0 means that we are already logged out. Should never occur.
		if (userID != 0) {
			final HttpGet get = new HttpGet("http://"+url+"/users/device_logout/"+userID);
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	        
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			try {
				HttpResponse response = client.execute(get);
				HttpEntity r_entity = response.getEntity();
		        String responseString = EntityUtils.toString(r_entity);
		        userID = Integer.parseInt(responseString); //Expecting a 0 here.
		        Toast.makeText(owner, "Logged Out", 3000).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * changed the connection hub location.
	 * 
	 * @param s the new URL
	 */
    public void updateIP(String s) {
		url = s;
    }
    
    /**
     * changed the user preferences
     * 
     * @param name new username input
     * @param pass new password input
     */
    public void updateUser(String name, String pass) {
    	username = name;
    	password = pass;
    }
    
    /**
     * Download the instrument/geo locations from the server and put them on the map
     * TODO: this should also pull synthdefs from the server if the client doesn't have the necessary files
     * TODO: this should also handle polygonal areas as well as points.
     * The server needs to be upgraded to handle both of these tasks.
     */
    public void getLocationsFromServer() {
    	final HttpGet get = new HttpGet("http://"+url+"/locations.xml");
    	HttpParams httpParameters = new BasicHttpParams();
    	int timeoutConnection = 7000;
    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    	int timeoutSocket = 7000;
    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    	final HttpClient client = new DefaultHttpClient(httpParameters);
		try {
			HttpResponse response = client.execute(get);
			HttpEntity r_entity = response.getEntity();
	        String xmlString = EntityUtils.toString(r_entity);
	        //Toast.makeText(owner, xmlString, 3000).show();
	        InputSource inStream = new InputSource();
	        inStream.setCharacterStream(new StringReader(xmlString));
			reader.parse(inStream);
			//TODO: now find all the synths you need and grab ones you don't have from server
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Iterate through current places, checking for repeats from the server. Add non-duplicates to the map.
		//TODO: work on this - must be faster and must handle polygon areas as well. Speed up with a sorting algorithm, maybe?
		boolean contained = false;
		Collection<GeoSynth> places = owner.getAuraManager().getDestinations().values();
		for (PointGeoSynth p : myXmlHandler.getPlaces()) {
			for (GeoSynth pl : places) {
				if (/**pl.synthDef.equals(p.synthDef) &&*/ pl.name.equals(p.name)) {
					contained = true;
					break;
				}
			}
			if (contained == false) {
				owner.getAuraManager().addDestination(p);
			}	
			contained = false;
		}
    }
    
    /**
     * Send the user's current location to the server. Do this on good location fixes, log in, create account, etc.
     */
    public void sendMyLocation() {
    	if (userID != 0) {
	    	final HttpPut put = new HttpPut("http://"+url+"/users/device_update/"+userID);
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			new Thread() {
	        	public void run() {
	    			try {
	    				//This part is going to be updating the server with your new information
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(1);
	    				Location current = owner.getLocationeer().getCurrentLocation();
	    			    hash.add(new BasicNameValuePair("latitude", ""+current.getLatitude()));
	    		        hash.add(new BasicNameValuePair("longitude", ""+current.getLongitude()));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        /**HttpResponse response = */client.execute(put);
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
    	}
    }

    /**
     * Tell the server that the user has entered an instrumented geo location. It will add you to the associations. Test on log in, creation, and good location fixes.
     * 
     * @param place that you have entered
     */
	public void notifyServerLocationExit(final GeoSynth place) {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http://"+url+"/users/device_exit_location/"+userID);
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			new Thread() {
	        	public void run() {
	    			try {
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(1);
	    			    hash.add(new BasicNameValuePair("location_name", place.name));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	    		        
	    		        /**HttpResponse response = */client.execute(put);
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}

	/**
	 * Tell the server that the user has exited an instrumented geo location. It will remove you from the associations. Test on log out and good location fixes.
	 * 
	 * @param place that has been exited
	 */
	public void notifyServerLocationEnter(final GeoSynth place) {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http://"+url+"/users/device_enter_location/"+userID);
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			new Thread() {
	        	public void run() {
	    			try {
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(1);
	    			    hash.add(new BasicNameValuePair("location_name", place.name));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	    		        
	    		        /**HttpResponse response = */client.execute(put);
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}

	/**
	 * Submit a created Point mapping to the server.
	 * 
	 * @param place to be submitted
	 */
	public void submitPoint(final PointGeoSynth place) {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http://"+url+"/locations/device_create");
			HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
	    	new Thread() {
	        	public void run() {
	    			try {
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(4);
	    			    hash.add(new BasicNameValuePair("latitude", ""+place.getLat()));
	    		        hash.add(new BasicNameValuePair("longitude", ""+place.getLon()));
	    		        hash.add(new BasicNameValuePair("name", place.name));
	    		        hash.add(new BasicNameValuePair("synth", place.synthDef));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        HttpResponse response = client.execute(put);
	    		        HttpEntity r_entity = response.getEntity();
	    		        String responseString = EntityUtils.toString(r_entity);
	    		        int id = Integer.parseInt(responseString);
	    		        if (id == 0) {
	    		        	Toast.makeText(owner, "Submission Failed. That name is most likely taken.", 3000).show();
	    		        }
	    		        else {
	    		        	Toast.makeText(owner, "Area successfully submitted to server.", 3000).show();
	    		        	place.fromServer = true;
	    		        }
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}
	
	/** TODO: submit polygon mappings to the server once it can handle them.
	 * 
	 */
}