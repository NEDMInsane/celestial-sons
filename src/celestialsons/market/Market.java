package celestialsons.market;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Market {
    //Not all stations will have a traditional celestialsons.market.
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
        List<Contract> contracts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length < 6) {
                    continue;
                }

                String product = unquote(fields[2]);
                int quantity = Integer.parseInt(fields[3].trim());
                double priceEach = Double.parseDouble(fields[4].trim());
                String seller = unquote(fields[5]);

                contracts.add(new Contract(product, seller, quantity, priceEach));
            }
        }

        this.marketData = contracts.toArray(new Contract[0]);
    }

    public void printMarketData(){
        for(Contract contract : this.marketData){
            System.out.println(Arrays.toString(contract.toStringArray()));
        }
    }

    public Contract[] getMarketData(){
        return this.marketData;
    }

    private String unquote(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}
