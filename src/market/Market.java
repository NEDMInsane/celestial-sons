package market;

import java.util.HashMap;

public class Market {
    //Not all stations will have a traditional market.
    private String marketStationName;
    private String marketLocation;

    private HashMap<String, Contract> marketData;

    public Market() {
        this.marketStationName = null;
        this.marketLocation = null;
        this.marketData = new HashMap<>();

        System.out.println("Market Created w/o passing arguments");
    }

    public Market(String marketStationName, String marketLocation) {
        this.marketStationName = marketStationName;
        this.marketLocation = marketLocation;
        this.marketData = new HashMap<>();

        System.out.println("Market Created: " + marketStationName + " " + marketLocation);
    }
}