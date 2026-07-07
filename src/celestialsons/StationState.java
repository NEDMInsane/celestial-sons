package celestialsons;

import celestialsons.market.Contract;

public class StationState {
    private String serverName;
    private String systemName;
    private String stationName;
    private boolean characterCreated;
    private boolean docked;
    private String playerName;
    private String shipType;
    private Contract[] marketContracts;
    private String[] shipOptions;

    public StationState(String serverName, String systemName, String stationName, boolean characterCreated,
                        boolean docked, String playerName, String shipType, Contract[] marketContracts,
                        String[] shipOptions) {
        this.serverName = serverName;
        this.systemName = systemName;
        this.stationName = stationName;
        this.characterCreated = characterCreated;
        this.docked = docked;
        this.playerName = playerName;
        this.shipType = shipType;
        this.marketContracts = marketContracts;
        this.shipOptions = shipOptions;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getStationName() {
        return stationName;
    }

    public boolean isCharacterCreated() {
        return characterCreated;
    }

    public boolean isDocked() {
        return docked;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getShipType() {
        return shipType;
    }

    public Contract[] getMarketContracts() {
        return marketContracts;
    }

    public String[] getShipOptions() {
        return shipOptions;
    }
}
