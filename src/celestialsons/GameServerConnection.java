package celestialsons;

public interface GameServerConnection {
    String getServerName();

    LoginResult loginOrCreateUser(String username, char[] password);

    CharacterCreationResult createCharacter(String username, String characterName, String shipType);

    StationState getStationState(String username);

    void undock(String username);

    boolean dockAtStation(String username, String stationName);

    boolean fileFlightPlan(String username, DimensionalPosition destination);

    SystemMapState getSystemMapState(String username);

    Universe getUniverse();

    NonPlayerCharacter[] getNonPlayerCharacters();

    PlayerCharacter[] getPlayerCharacters();
}
