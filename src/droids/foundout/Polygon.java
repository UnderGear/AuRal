package droids.foundout;

/**
 * Turns out Android didn't have a polygon class. This reminds me of my first semester in CSC.
 * @author UnderGear
 *
 */
public class Polygon {
 
    private int[] polyY, polyX;
    private int polySides;
    
    public int[] getPolyX() {
    	return polyX;
    }
    
    public int[] getPolyY() {
    	return polyY;
    }
    
    /**
     * Count of the sides of the polygon
     * 
     * @return number of sides
     */
    public int size() {
    	return polySides;
    }
    
    /**
     * Constructor
     * 
     * @param px list of x-components of all vertices
     * @param py list of y-components of all vertices
     */
    public Polygon(int[] px, int[] py) {
        polyX = px;
        polyY = py;
        polySides = px.length;
    }
    
    /**
     * Determines if the point (x, y) is contained within the polygon
     * 
     * @param x coordinate
     * @param y coordinate
     * @return whether or not the point is inside the polygon
     */
    public boolean contains(int x, int y) {
        boolean oddTransitions = false;
        for(int i = 0, j = polySides -1; i < polySides; j = i++) {
            if((polyY[i] < y && polyY[j] >= y) || (polyY[j] < y && polyY[i] >= y)) {
                if(polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x) {
                    oddTransitions = !oddTransitions;          
                }
            }
        }
        return oddTransitions;
    }
}