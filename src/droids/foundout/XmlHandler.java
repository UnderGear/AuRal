package droids.foundout;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {
	private boolean in_location = false;
    private boolean in_id = false;
    private boolean in_latitude = false;
    private boolean in_longitude = false;
    private boolean in_name = false;
    private boolean in_synth = false;
    
    private ArrayList<PointPlace> places = new ArrayList<PointPlace>();
    private PointPlace currentPlace;
    private String syn = "Micromoog.scsyndef";
    private String name = "";
    private double lat, lon;
    
    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }
    
    //TODO: make this whole thing parse areas as well. DB should hold many lats and lons. we should check for 1, 2 (error) and 3+
    public ArrayList<PointPlace> getPlaces() {
    	ArrayList<PointPlace> ps = places;
    	places = new ArrayList<PointPlace>();
    	return ps;
    }
    
    /** Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("location")) {
            this.in_location = true;
        }else if (localName.equals("id")) {
            this.in_id = true;
        }else if (localName.equals("latitude")) {
        	lat = 0.0;
            this.in_latitude = true;
        }else if (localName.equals("longitude")) {
        	lon = 0.0;
    		this.in_longitude = true;
        }else if (localName.equals("name")) {
        	name = "Name Unknown";
    		this.in_name = true;
        }else if (localName.equals("synth")) {
        	syn = "CDell_01.scsyndef";
        	this.in_synth = true;
        }
    }
   
    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("location")) {
            this.in_location = false;
            currentPlace = new PointPlace(lat, lon, -1, syn, name, true);
            places.add(currentPlace);
        }else if (localName.equals("id")) {
            this.in_id = false;
        }else if (localName.equals("latitude")) {
            this.in_latitude = false;
        }else if (localName.equals("longitude")) {
            this.in_longitude = false;
        }else if (localName.equals("name")) {
    		this.in_name = false;
        }else if (localName.equals("synth")) {
        	this.in_synth = false;
        }
    }
   
    /** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
    public void characters(char ch[], int start, int length) {
        if(this.in_location){
        	if (this.in_latitude)
        		lat = Double.parseDouble(new String(ch, start, length));
        	if (this.in_longitude)
        		lon = Double.parseDouble(new String(ch, start, length));
        	if (this.in_name)
        		name = new String(ch, start, length);
        	if (this.in_synth)
        		syn = new String(ch, start, length);
        }
    }
}
