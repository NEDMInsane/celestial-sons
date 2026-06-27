package celestialsons;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orbitalbodies.Star;

public class GameServer implements GameServerConnection {
    private static final Path USERS_DATABASE = Paths.get("testing", "users.csv");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String serverName;
    private final Universe universe;
    private final NonPlayerCharacter[] nonPlayerCharacters;
    private final PlayerCharacter[] playerCharacters;
    private final Map<String, PlayerCharacter> activePlayers = new HashMap<>();

    public GameServer() {
        this("Local Game Server", loadDefaultUniverse(), loadDefaultNonPlayerCharacters(), loadDefaultPlayerCharacters());
    }

    public GameServer(Universe universe, NonPlayerCharacter[] nonPlayerCharacters, PlayerCharacter[] playerCharacters) {
        this("Local Game Server", universe, nonPlayerCharacters, playerCharacters);
    }

    public GameServer(String serverName, Universe universe, NonPlayerCharacter[] nonPlayerCharacters, PlayerCharacter[] playerCharacters) {
        this.serverName = serverName;
        this.universe = universe != null ? universe : new Universe(null, null, null);
        this.nonPlayerCharacters = nonPlayerCharacters != null ? nonPlayerCharacters : new NonPlayerCharacter[0];
        this.playerCharacters = playerCharacters != null ? playerCharacters : new PlayerCharacter[0];
        ensureUsersDatabase();
    }

    @Override
    public String getServerName() {
        return this.serverName;
    }

    @Override
    public LoginResult loginOrCreateUser(String username, char[] password) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername.isEmpty()) {
            return LoginResult.failure("Enter a username.");
        }
        if (password == null || password.length == 0) {
            return LoginResult.failure("Enter a password.");
        }

        try {
            List<UserRecord> records = readUserRecords();
            UserRecord existingRecord = findUser(records, normalizedUsername);
            if (existingRecord == null) {
                UserRecord createdRecord = createUserRecord(normalizedUsername, password);
                records.add(createdRecord);
                writeUserRecords(records);
                return LoginResult.success(activatePlayer(normalizedUsername), "Created new user.");
            }

            if (!existingRecord.matchesPassword(password)) {
                return LoginResult.failure("Invalid password.");
            }

            return LoginResult.success(activatePlayer(normalizedUsername), "Logged in.");
        } catch (IOException e) {
            return LoginResult.failure("Could not access user database.");
        }
    }

    @Override
    public Universe getUniverse() {
        return this.universe;
    }

    @Override
    public NonPlayerCharacter[] getNonPlayerCharacters() {
        return this.nonPlayerCharacters;
    }

    @Override
    public PlayerCharacter[] getPlayerCharacters() {
        return this.playerCharacters;
    }

    public PlayerCharacter getActivePlayer(String username) {
        return this.activePlayers.get(normalizeUsername(username));
    }

    private PlayerCharacter activatePlayer(String username) {
        String normalizedUsername = normalizeUsername(username);
        PlayerCharacter player = this.activePlayers.get(normalizedUsername);
        if (player == null) {
            player = new PlayerCharacter(normalizedUsername, 1);
            if (this.playerCharacters.length > 0) {
                this.playerCharacters[0] = player;
            }
            this.activePlayers.put(normalizedUsername, player);
        }

        if (player.getCurrentSystem() == null && this.universe != null) {
            try {
                Star starterSystem = this.universe.findSystem("Jita");
                if (starterSystem != null) {
                    player.setSystem(starterSystem);
                }
            } catch (NullPointerException ignored) {
                // The local universe may not have been loaded yet.
            }
        }
        if (player.getCurrentLocation() == null) {
            player.setLocation(new DimensionalPosition(0.0, 0.0, 0.0));
        }

        return player;
    }

    private static Universe loadDefaultUniverse() {
        try {
            return FileHandling.loadAlmanacData("testing");
        } catch (IOException e) {
            return new Universe(null, null, null);
        }
    }

    private static NonPlayerCharacter[] loadDefaultNonPlayerCharacters() {
        return new NonPlayerCharacter[] { new NonPlayerCharacter("Paul") };
    }

    private static PlayerCharacter[] loadDefaultPlayerCharacters() {
        return new PlayerCharacter[] { new PlayerCharacter("Player1", 1) };
    }

    private void ensureUsersDatabase() {
        try {
            if (USERS_DATABASE.getParent() != null) {
                Files.createDirectories(USERS_DATABASE.getParent());
            }
            if (Files.notExists(USERS_DATABASE)) {
                Files.writeString(USERS_DATABASE, "username,salt,passwordHash,createdAt\n", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize users database.", e);
        }
    }

    private List<UserRecord> readUserRecords() throws IOException {
        List<UserRecord> records = new ArrayList<>();
        List<String> lines = Files.readAllLines(USERS_DATABASE, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length >= 4) {
                records.add(new UserRecord(parts[0], parts[1], parts[2], parts[3]));
            }
        }
        return records;
    }

    private void writeUserRecords(List<UserRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(USERS_DATABASE, StandardCharsets.UTF_8)) {
            writer.write("username,salt,passwordHash,createdAt");
            writer.newLine();
            for (UserRecord record : records) {
                writer.write(record.toCsvLine());
                writer.newLine();
            }
        }
    }

    private UserRecord createUserRecord(String username, char[] password) {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String passwordHash = hashPassword(salt, password);
        return new UserRecord(username, salt, passwordHash, Instant.now().toString());
    }

    private UserRecord findUser(List<UserRecord> records, String username) {
        for (UserRecord record : records) {
            if (record.username.equalsIgnoreCase(username)) {
                return record;
            }
        }
        return null;
    }

    private static String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private static String hashPassword(String salt, char[] password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            digest.update(new String(password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

    private static final class UserRecord {
        private final String username;
        private final String salt;
        private final String passwordHash;
        private final String createdAt;

        private UserRecord(String username, String salt, String passwordHash, String createdAt) {
            this.username = username;
            this.salt = salt;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
        }

        private boolean matchesPassword(char[] password) {
            return this.passwordHash.equals(hashPassword(this.salt, password));
        }

        private String toCsvLine() {
            return String.join(",", this.username, this.salt, this.passwordHash, this.createdAt);
        }
    }
}
