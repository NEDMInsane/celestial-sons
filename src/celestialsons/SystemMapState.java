package celestialsons;

public class SystemMapState {
    private String serverName;
    private String systemName;
    private DimensionalPosition playerPosition;
    private String playerFlightPlan;
    private MapMarker[] orbitalBodies;
    private CharacterMarker[] characters;
    private FlightPlanMarker[] flightPlans;

    public SystemMapState(String serverName, String systemName, DimensionalPosition playerPosition,
                             String playerFlightPlan, MapMarker[] orbitalBodies, CharacterMarker[] characters,
                             FlightPlanMarker[] flightPlans) {
        this.serverName = serverName;
        this.systemName = systemName;
        this.playerPosition = playerPosition;
        this.playerFlightPlan = playerFlightPlan;
        this.orbitalBodies = orbitalBodies;
        this.characters = characters;
        this.flightPlans = flightPlans;
    }

    public String getServerName() {
        return serverName;
    }

    public String getSystemName() {
        return systemName;
    }

    public DimensionalPosition getPlayerPosition() {
        return playerPosition;
    }

    public String getPlayerFlightPlan() {
        return playerFlightPlan;
    }

    public MapMarker[] getOrbitalBodies() {
        return orbitalBodies;
    }

    public CharacterMarker[] getCharacters() {
        return characters;
    }

    public FlightPlanMarker[] getFlightPlans() {
        return this.flightPlans;
    }
}
