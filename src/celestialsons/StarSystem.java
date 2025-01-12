
package celestialsons;

import orbitalbodies.*;


public class StarSystem {
    /**Star systems are a part of a larger constellation, then region.
     * Star systems contain 1 star or a binary star, asteroid belts, planets, moons, etc.
     * Planets contain most of the objects as they are a gravity well.
     * Anomalies are different.
     * each star system is connected by HyperSpace Gates
     */
    
    // The grid is the size of the solar system which is 100k AU^3
    private DimensionalPosition grid = new DimensionalPosition(100000.0, 100000.0, 100000.0); // This is in AU
    // The player grid is the grid when a player drops out of hyperspace/cruise.
    private DimensionalPosition playerGrid = new DimensionalPosition(400000.0, 400000.0, 400000.0); // This in in KM
    
    private Planet[] planets;
    private Anomaly[] anomalies;
    private Star star;
    
    
    
}
