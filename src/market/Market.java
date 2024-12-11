package market;

import celestialsons.*;

import java.io.IOException;
import java.util.Arrays;

public class Market {
    //Not all stations will have a traditional market.
    private String marketStationName;
    private String marketLocation;

    private Contract[] marketData;

    public Market() {
        this.marketStationName = null;
        this.marketLocation = null;
        this.marketData = null;

        System.out.println("Market Created w/o passing arguments");
    }

    public Market(String marketLocation, String marketStationName) {
        this.marketStationName = marketStationName;
        this.marketLocation = marketLocation;
        this.marketData = null;

        System.out.println("Market Created: " + marketStationName + " " + marketLocation);
    }

    public Market(String marketLocation, String marketStationName, Contract[] marketData) {
        this.marketStationName = marketStationName;
        this.marketLocation = marketLocation;
        this.marketData = marketData;
        System.out.println("Market Created: " + marketStationName + " " + marketLocation);
    }

    public void fromCSV(String filename) throws IOException {
        String[] rawMarketData = FileHandling.convertFromCSV(filename);
        // rawMarketData is providing erroneous data. "\n" and adding spaces and commas. needs fixed.
        String[] marketDataList = new String[(rawMarketData.length / 6)];
        for(int i = 0; i < marketDataList.length; i++){
            int start = i * 6;
            int stop = start + 6;
            marketDataList[i] = Arrays.toString(Arrays.copyOfRange(rawMarketData, start, stop));
        }
        Contract[] marketData = new Contract[marketDataList.length];
        for(int i = 0; i < marketDataList.length; i++){
            String[] temp = marketDataList[i].split(",");
            System.out.println(Arrays.toString(temp));
            marketData[i] = new Contract(temp[2], temp[5], Integer.parseInt(temp[3]), Double.parseDouble(temp[4]));
        }
        // Fields for the CSV: Location, Station Name, Product, Quantity, Price Each, Comp/Person selling
        this.marketData = marketData;
    }

    public void printMarketData(){
        for(Contract contract : this.marketData){
            System.out.println(Arrays.toString(contract.toStringArray()));
        }
    }
}
