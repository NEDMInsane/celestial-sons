package celestialsons;

public interface GameServerConnection {
    String getServerName();

    LoginResult loginOrCreateUser(String username, char[] password);

    Universe getUniverse();

    NonPlayerCharacter[] getNonPlayerCharacters();

    PlayerCharacter[] getPlayerCharacters();
}
