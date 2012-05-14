package droids.foundout;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

public class ServerHook {
	
	static private ServerHook _instance;

	private AuRal owner;
	//for connection to server
	private String url;
    private int port;
    //tools for parsing XML from server
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser parser;
    XMLReader reader;
    XmlHandler myXmlHandler;
    //for incoming OSC
	OSCPortIn oscReceiver;
	OSCListener listener;
	
	private int userID;
	private String username;
	private String password;
	
	public int getID() {
		return userID;
	}
	
	static synchronized public ServerHook getInstance() 
	{
	    if (_instance == null) 
	      _instance = new ServerHook();
	    return _instance;
	 }
	
	public void setOwner(final AuRal owner) {
		this.owner = owner;
		//giving the address and port some values from preferences
        try {
			url = InetAddress.getByName(owner.preferences.getString("ip_number", "127.0.0.1")).toString();
			port = Integer.parseInt(owner.preferences.getString("port_number", "80"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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
		try {
			oscReceiver = new OSCPortIn(8000); //change me from default (8000?) if you need to
		} catch (SocketException e) {
			Log.e(AuRal.TAG, e.getMessage());
		}
		
		listener = new OSCListener() {
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				Object o[] = message.getArguments();
				for (Place place : owner.auraManager.destinations.values()) {
					if (place.name.equals(o[0].toString())) {
						Log.e(AuRal.TAG, "got it");
						owner.scManager.updateParams(place.index, o[1], o[2], o[3]);
						break;
					}
				}
			}
		};
		oscReceiver.addListener("/Server", listener); //more osc messages!
		oscReceiver.startListening();
		
	}
	
	public ServerHook(AuRal g) {
		owner = g;
		//giving the address and port some values from preferences
        try {
			url = InetAddress.getByName(owner.preferences.getString("ip_number", "127.0.0.1")).toString();
			port = Integer.parseInt(owner.preferences.getString("port_number", "80"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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
		try {
			oscReceiver = new OSCPortIn(8000); //change me from default (8000?) if you need to
		} catch (SocketException e) {
			Log.e(AuRal.TAG, e.getMessage());
		}
		
		listener = new OSCListener() {
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				Object o[] = message.getArguments();
				for (Place place : owner.auraManager.destinations.values()) {
					if (place.name.equals(o[0].toString())) {
						Log.e(AuRal.TAG, "got it");
						owner.scManager.updateParams(place.index, o[1], o[2], o[3]);
						break;
					}
				}
			}
		};
		oscReceiver.addListener("/Server", listener); //more osc messages!
		oscReceiver.startListening();
	}
	
	public ServerHook() {
		// TODO Auto-generated constructor stub
	}

	public void logIn() {
		if (userID == 0) {
			final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_login");
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	List<NameValuePair> hash = new ArrayList<NameValuePair>(2);
		    hash.add(new BasicNameValuePair("name", username));
	        hash.add(new BasicNameValuePair("password", password));
	        
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			try {
				put.setEntity(new UrlEncodedFormEntity(hash));
				HttpResponse response = client.execute(put);
				HttpEntity r_entity = response.getEntity();
		        String responseString = EntityUtils.toString(r_entity);
		        if (responseString.equals(""))
		        	userID = 0;
		        else userID = Integer.parseInt(responseString);
		        if (userID != 0)
		        	Toast.makeText(owner, "Logged in as " + username, 3000).show();
		        else Toast.makeText(owner, "Authentication Failed", 3000).show();
		        //InputSource inStream = new InputSource();
		        //inStream.setCharacterStream(new StringReader(xmlString));
				//reader.parse(inStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void modifyUser() {
		//similar to sendMyLocation
		if (userID != 0) {
	    	final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_edit/"+userID);
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
	    		        HttpResponse response = client.execute(put);
	    		        //TODO: do something with response?
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
    	}
    }
	
	public void createUser() {
		if (userID == 0) {
			final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_create/");
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
	    				Location current = owner.locationeer.getCurrentLocation();
	    			    hash.add(new BasicNameValuePair("name", username));
	    		        hash.add(new BasicNameValuePair("password", password));
	    		        hash.add(new BasicNameValuePair("latitude", ""+current.getLatitude()));
	    		        hash.add(new BasicNameValuePair("longitude", ""+current.getLongitude()));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        // Execute HTTP Put Request
	    		        HttpResponse response = client.execute(put);
	    		        HttpEntity r_entity = response.getEntity();
	    		        String responseString = EntityUtils.toString(r_entity);
	    		        if (responseString.equals("")) {
	    		        	userID = 0;
	    		        	Toast.makeText(owner, "Create User Failed", 3000).show();
	    		        }
	    		        else userID = Integer.parseInt(responseString);
	    		        Toast.makeText(owner, "Create User Successful", 3000).show();
	    		        Toast.makeText(owner, "Logged in as " + username, 3000).show();
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}
	
	public void logOut() {
		if (userID != 0) {
			final HttpGet get = new HttpGet("http:/"+url+":"+port+"/users/device_logout/"+userID);
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
		        userID = Integer.parseInt(responseString);
	
		        Toast.makeText(owner, "Logged Out", 3000).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//this will send an OSC Message. Get some freaking meds for your ADD so you can finish a damn comment.
    /**public void sendOSCMessage(Location l) {
    	if (userID != 0) {
	    	Object args0[] = new Object[1];
	        Object args1[] = new Object[1];
	        args0[0] = (Object)((float)(l.getLatitude()));
	        args1[0] = (Object)((float)(l.getLongitude()));
	        OSCMessage messageLat = new OSCMessage("/Server",  args0);
	        OSCMessage messageLong = new OSCMessage("/Android/Location/Longitude",  args1);
	        OSCBundle bundle = new OSCBundle();
	        bundle.addPacket(messageLat);
	        bundle.addPacket(messageLong);
	        try {
				OSCPortOut oscSender = new OSCPortOut(/** InetAddress Address, int Port InetAddress.getByName("173.253.154.222"), OSCPort.defaultSCOSCPort()); //empty goes to localhost
	        	oscSender.send(messageLat);
				//Toast.makeText(owner, "Location sent: \nLatitude: " + mostReliable.getLatitude() + "\nLongitude: " + mostReliable.getLongitude(), 2000).show();
			} catch (IOException e) {
				Log.e(GotFound.TAG, e.getMessage());
			}
    	}
    }*/
	
	//changed the connection IP, fix stuff up here.
    public void updateIP(String s, int p) {
    	port = p;
    	try {
			url = (InetAddress.getByName(s)).toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }
    
    public void updateUser(String name, String pass) {
    	username = name;
    	password = pass;
    }
    
    //dealing with connection to server here
    public void getLocationsFromServer() {
    	final HttpGet get = new HttpGet("http:/"+url+":"+port+"/locations.xml");
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
		//TODO: work on this - must be faster and must handle areas as well
		boolean contained = false;
		Collection<Place> places = owner.auraManager.destinations.values();
		for (PointPlace p : myXmlHandler.getPlaces()) {
			for (Place pl : places) {
				if (/**pl.synthDef.equals(p.synthDef) &&*/ pl.name.equals(p.name)) {
					contained = true;
					break;
				}
			}
			if (contained == false) {
				owner.auraManager.addDestination(p);
			}	
			contained = false;
		}
    }
    
    public void sendMyLocation() {
    	if (userID != 0) {
	    	final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_update/"+userID);
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
	    				Location current = owner.locationeer.getCurrentLocation();
	    			    hash.add(new BasicNameValuePair("latitude", ""+current.getLatitude()));
	    		        hash.add(new BasicNameValuePair("longitude", ""+current.getLongitude()));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        // Execute HTTP Put Request
	    		        HttpResponse response = client.execute(put);
	    		        //TODO: do something with response? - let's get nearby locations from the server.
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
    	}
    }

	public void notifyServerLocationExit(final Place place) {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_exit_location/"+userID);
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
	    		        
	    		        HttpResponse response = client.execute(put);
	    		        //TODO: do something with response? - let's get nearby locations from the server.
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}

	public void notifyServerLocationEnter(final Place place) {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_enter_location/"+userID);
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
	    		        
	    		        HttpResponse response = client.execute(put);
	    		        //TODO: do something with response? - let's get nearby locations from the server.
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}
	
	public void changeServerParams() {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http:/"+url+":"+port+"/users/device_update_params/"+userID);
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 7000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 7000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    	final HttpClient client = new DefaultHttpClient(httpParameters);
			new Thread() {
	        	public void run() {
	    			try {
	    				List<NameValuePair> hash = new ArrayList<NameValuePair>(3);
	    			    hash.add(new BasicNameValuePair("param_1", ""+(owner.preferences.getInt("slider1", 0)/1000.0)));
	    		        hash.add(new BasicNameValuePair("param_2", ""+(owner.preferences.getInt("slider2", 0)/1000.0)));
	    		        hash.add(new BasicNameValuePair("param_3", ""+(owner.preferences.getInt("slider3", 0)/1000.0)));
	    		        put.setEntity(new UrlEncodedFormEntity(hash));
	
	    		        HttpResponse response = client.execute(put);
	    		        //TODO: do something with response? - let's get nearby locations from the server.
					}catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			}.run();
		}
	}

	public void submitArea(final PointPlace place) {
		if (userID != 0) {
			final HttpPut put = new HttpPut("http:/"+url+":"+port+"/locations/device_create");
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
	    			    hash.add(new BasicNameValuePair("latitude", ""+place.lat));
	    		        hash.add(new BasicNameValuePair("longitude", ""+place.lon));
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
}