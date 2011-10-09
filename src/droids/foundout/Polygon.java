package droids.foundout;
 
public class Polygon {
 
    private int[] polyY, polyX;
    private int polySides;
    
    public int[] getPolyX() {
    	return polyX;
    }
    
    public int[] getPolyY() {
    	return polyY;
    }
    
    public int size() {
    	return polySides;
    }
    
    public Polygon(int[] px, int[] py, int ps) {
        polyX = px;
        polyY = py;
        polySides = ps;
    }
    
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