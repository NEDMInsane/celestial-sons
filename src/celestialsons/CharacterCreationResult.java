package celestialsons;

public final class CharacterCreationResult {
    private final boolean success;
    private final String message;
    private final PlayerCharacter playerCharacter;

    private CharacterCreationResult(boolean success, String message, PlayerCharacter playerCharacter) {
        this.success = success;
        this.message = message;
        this.playerCharacter = playerCharacter;
    }

    public static CharacterCreationResult success(PlayerCharacter playerCharacter, String message) {
        return new CharacterCreationResult(true, message, playerCharacter);
    }

    public static CharacterCreationResult failure(String message) {
        return new CharacterCreationResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public PlayerCharacter getPlayerCharacter() {
        return playerCharacter;
    }
}
