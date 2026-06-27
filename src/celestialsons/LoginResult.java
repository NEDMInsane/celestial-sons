package celestialsons;

public final class LoginResult {
    private final boolean success;
    private final String message;
    private final PlayerCharacter playerCharacter;

    private LoginResult(boolean success, String message, PlayerCharacter playerCharacter) {
        this.success = success;
        this.message = message;
        this.playerCharacter = playerCharacter;
    }

    public static LoginResult success(PlayerCharacter playerCharacter, String message) {
        return new LoginResult(true, message, playerCharacter);
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message, null);
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
