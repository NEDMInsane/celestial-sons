package celestialsons;

import celestialsons.market.Contract;
import celestialsons.market.Market;
import celestialsons.orbitalbodies.Moon;
import celestialsons.orbitalbodies.Planet;
import celestialsons.orbitalbodies.SpaceStation;
import celestialsons.orbitalbodies.Star;
import celestialsons.ship.Ship;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer implements GameServerConnection {
    private static final Path USERS_DATABASE = Paths.get("testing", "users.csv");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String[] SHIP_OPTIONS = {"Shuttle", "Frigate", "Miner", "Hauler"};
    private static final double DOCKING_RADIUS_KM = 30.0;
    private static final double OBSTACLE_MARGIN_KM = 12.0;
    private static final double MAX_SPEED_KM_PER_SEC = 140.0;
    private static final double ACCELERATION_KM_PER_SEC2 = 42.0;

    private final String serverName;
    private final Universe universe;
    private final NonPlayerCharacter[] nonPlayerCharacters;
    private final PlayerCharacter[] playerCharacters;
    private final Map<String, PlayerCharacter> activePlayers = new HashMap<>();
    private final Market stationMarket;

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
        this.stationMarket = loadStationMarket();
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
            List<AccountRecord> records = readUserRecords();
            AccountRecord record = findUser(records, normalizedUsername);
            if (record == null) {
                record = createAccountRecord(normalizedUsername, password);
                records.add(record);
                writeUserRecords(records);
                return LoginResult.success(null, "Account created. Character creation required.");
            }

            if (!record.matchesPassword(password)) {
                return LoginResult.failure("Invalid password.");
            }

            if (!record.hasCharacter()) {
                return LoginResult.success(null, "Character creation required.");
            }

            PlayerCharacter player = activatePlayer(record);
            return LoginResult.success(player, record.docked ? "Logged in docked." : "Logged in.");
        } catch (IOException e) {
            return LoginResult.failure("Could not access user database.");
        }
    }

    @Override
    public CharacterCreationResult createCharacter(String username, String characterName, String shipType) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedCharacterName = normalizeUsername(characterName);
        String normalizedShipType = normalizeUsername(shipType);
        if (normalizedCharacterName.isEmpty()) {
            return CharacterCreationResult.failure("Enter a character name.");
        }
        if (normalizedShipType.isEmpty()) {
            normalizedShipType = SHIP_OPTIONS[0];
        }

        try {
            List<AccountRecord> records = readUserRecords();
            AccountRecord record = findUser(records, normalizedUsername);
            if (record == null) {
                return CharacterCreationResult.failure("Account not found.");
            }

            record.characterName = normalizedCharacterName;
            record.shipType = normalizedShipType;
            record.docked = true;
            records = replaceRecord(records, record);
            writeUserRecords(records);

            PlayerCharacter player = activatePlayer(record);
            player.setName(normalizedCharacterName);
            player.setShipType(normalizedShipType);
            player.setCurrentHull(new Ship(normalizedShipType, 0, 0, 0));
            player.setDocked(true);
            player.setCurrentStationName(record.stationName);
            player.setLocation(new DimensionalPosition(0.0, 0.0, 0.0));
            return CharacterCreationResult.success(player, "Character created.");
        } catch (IOException e) {
            return CharacterCreationResult.failure("Could not create character.");
        }
    }

    @Override
    public StationState getStationState(String username) {
        try {
            AccountRecord record = findUser(readUserRecords(), normalizeUsername(username));
            if (record == null) {
                return defaultStationState();
            }

            return new StationState(
                    this.serverName,
                    record.systemName,
                    record.stationName,
                    record.hasCharacter(),
                    record.docked,
                    record.characterName,
                    record.shipType,
                    this.stationMarket == null ? new Contract[0] : this.stationMarket.getMarketData(),
                    SHIP_OPTIONS
            );
        } catch (IOException e) {
            return defaultStationState();
        }
    }

    @Override
    public void undock(String username) {
        String normalizedUsername = normalizeUsername(username);
        try {
            List<AccountRecord> records = readUserRecords();
            AccountRecord record = findUser(records, normalizedUsername);
            if (record == null || !record.hasCharacter()) {
                return;
            }
            record.docked = false;
            records = replaceRecord(records, record);
            writeUserRecords(records);

            PlayerCharacter player = activatePlayer(record);
            player.setDocked(false);
            player.setCurrentStationName("");
            if (player.getCurrentSystem() == null) {
                player.setSystem(resolveSystem(record.systemName));
            }
            DimensionalPosition stationPosition = resolveStationPosition(record.systemName, record.stationName);
            if (stationPosition == null) {
                stationPosition = new DimensionalPosition(0.0, 0.0, 0.0);
            }
            double bearing = SECURE_RANDOM.nextDouble() * Math.PI * 2.0;
            double distance = 10.0 + (SECURE_RANDOM.nextDouble() * 15.0);
            DimensionalPosition spawnPosition = new DimensionalPosition(
                    stationPosition.getX() + Math.cos(bearing) * distance,
                    stationPosition.getY() + Math.sin(bearing) * distance,
                    stationPosition.getZ() + ((SECURE_RANDOM.nextDouble() - 0.5) * 6.0)
            );
            DimensionalPosition velocity = new DimensionalPosition(
                    Math.cos(bearing) * (0.15 + (SECURE_RANDOM.nextDouble() * 0.20)),
                    Math.sin(bearing) * (0.15 + (SECURE_RANDOM.nextDouble() * 0.20)),
                    (SECURE_RANDOM.nextDouble() - 0.5) * 0.05
            );
            player.setLocation(spawnPosition);
            player.setVelocity(velocity);
            player.setFlightPlan(new DimensionalPosition[0]);
            player.setFlightPlanLabel("No filed flight plan");
            player.setCurrentSpeed(0.0);
            player.setFlightPlanIndex(0);
            player.setLastMotionUpdateMillis(System.currentTimeMillis());
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean dockAtStation(String username, String stationName) {
        String normalizedUsername = normalizeUsername(username);
        try {
            List<AccountRecord> records = readUserRecords();
            AccountRecord record = findUser(records, normalizedUsername);
            if (record == null || !record.hasCharacter()) {
                return false;
            }

            SpaceStation station = findStation(record.systemName, stationName);
            if (station == null || station.getPosition() == null) {
                return false;
            }

            PlayerCharacter player = activatePlayer(record);
            if (distanceSquared(player.getCurrentLocation(), station.getPosition()) > (DOCKING_RADIUS_KM * DOCKING_RADIUS_KM)) {
                return false;
            }

            record.docked = true;
            record.stationName = station.getName();
            records = replaceRecord(records, record);
            writeUserRecords(records);

            player.setDocked(true);
            player.setCurrentStationName(station.getName());
            player.setLocation(station.getPosition());
            player.setVelocity(new DimensionalPosition(0.0, 0.0, 0.0));
            player.setFlightPlan(new DimensionalPosition[0]);
            player.setFlightPlanLabel("No filed flight plan");
            player.setCurrentSpeed(0.0);
            player.setFlightPlanIndex(0);
            player.setLastMotionUpdateMillis(System.currentTimeMillis());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean fileFlightPlan(String username, DimensionalPosition destination) {
        String normalizedUsername = normalizeUsername(username);
        if (destination == null) {
            return false;
        }
        try {
            List<AccountRecord> records = readUserRecords();
            AccountRecord record = findUser(records, normalizedUsername);
            if (record == null || !record.hasCharacter()) {
                return false;
            }

            PlayerCharacter player = activatePlayer(record);
            Star system = player.getCurrentSystem();
            if (system == null) {
                system = resolveSystem(record.systemName);
            }
            if (system == null) {
                return false;
            }

            DimensionalPosition safeDestination = resolveSafeDestination(system, player.getCurrentLocation(), destination);
            DimensionalPosition[] route = buildFlightRoute(system, player.getCurrentLocation(), safeDestination);
            if (route.length < 2) {
                return false;
            }

            player.setDocked(false);
            player.setCurrentStationName("");
            player.setFlightPlan(route);
            player.setFlightPlanLabel(buildFlightPlanLabel(route));
            player.setCurrentSpeed(0.0);
            player.setLastMotionUpdateMillis(System.currentTimeMillis());
            record.docked = false;
            records = replaceRecord(records, record);
            writeUserRecords(records);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public SystemMapState getSystemMapState(String username) {
        PlayerCharacter player = getActivePlayer(username);
        if (player == null) {
            try {
                AccountRecord record = findUser(readUserRecords(), normalizeUsername(username));
                if (record != null && record.hasCharacter()) {
                    player = activatePlayer(record);
                }
            } catch (IOException ignored) {
            }
        }
        if (player == null) {
            return new SystemMapState(
                    this.serverName,
                    "Unknown System",
                    new DimensionalPosition(0, 0, 0),
                    "No filed flight plan",
                    new MapMarker[0],
                    new CharacterMarker[0],
                    new FlightPlanMarker[0]
            );
        }

        advancePlayerMotion(player);

        Star system = player.getCurrentSystem();
        String systemName = system == null ? "Unknown System" : system.getName();

        List<MapMarker> bodyMarkers = new ArrayList<>();
        if (system != null) {
            layoutStarSystem(system);
            bodyMarkers.add(new MapMarker(system.getName(), "Star", system.getPosition()));
            Planet[] planets = system.getPlanets();
            if (planets != null) {
                for (Planet planet : planets) {
                    bodyMarkers.add(new MapMarker(planet.getName(), "Planet", planet.getPosition()));
                    Moon[] moons = planet.getMoons();
                    if (moons != null) {
                        for (Moon moon : moons) {
                            bodyMarkers.add(new MapMarker(moon.getName(), "Moon", moon.getPosition()));
                        }
                    }
                }
            }
            SpaceStation[] stations = this.universe == null ? null : this.universe.getStationList();
            if (stations != null) {
                for (SpaceStation station : stations) {
                    if (station == null || station.getPosition() == null) {
                        continue;
                    }
                    Star stationSystem = resolveNearestStar(station.getPosition());
                    if (stationSystem != null && stationSystem.getName().equals(system.getName())) {
                        bodyMarkers.add(new MapMarker(station.getName(), "Station", station.getPosition()));
                    }
                }
            }
        }

        List<CharacterMarker> characterMarkers = new ArrayList<>();
        characterMarkers.add(new CharacterMarker(
                player.getName(),
                player.getCurrentLocation(),
                player.isTransponderActive(),
                player.getFlightPlanLabel()
        ));
        for (NonPlayerCharacter npc : this.nonPlayerCharacters) {
            if (npc == null || !npc.isTransponderActive()) {
                continue;
            }
            characterMarkers.add(new CharacterMarker(
                    npc.getName(),
                    npc.getCurrentLocation(),
                    true,
                    npc.getFlightPlanLabel()
            ));
        }

        List<FlightPlanMarker> flightPlans = new ArrayList<>();
        if (player.getFlightPlan().length > 0) {
            flightPlans.add(new FlightPlanMarker(player.getName(), player.getFlightPlanLabel(), player.getFlightPlan()));
        }
        for (NonPlayerCharacter npc : this.nonPlayerCharacters) {
            if (npc != null && npc.getFlightPlan().length > 0) {
                flightPlans.add(new FlightPlanMarker(npc.getName(), npc.getFlightPlanLabel(), npc.getFlightPlan()));
            }
        }

        return new SystemMapState(
                this.serverName,
                systemName,
                player.getCurrentLocation(),
                player.getFlightPlanLabel(),
                bodyMarkers.toArray(new MapMarker[0]),
                characterMarkers.toArray(new CharacterMarker[0]),
                flightPlans.toArray(new FlightPlanMarker[0])
        );
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

    private PlayerCharacter activatePlayer(AccountRecord record) {
        String normalizedUsername = normalizeUsername(record.username);
        PlayerCharacter player = this.activePlayers.get(normalizedUsername);
        if (player == null) {
            player = new PlayerCharacter(record.hasCharacter() ? record.characterName : normalizedUsername, 1);
            if (this.playerCharacters.length > 0) {
                this.playerCharacters[0] = player;
            }
            this.activePlayers.put(normalizedUsername, player);
        }

        Star system = resolveSystem(record.systemName);
        if (system != null) {
            player.setSystem(system);
        }
        if (record.hasCharacter()) {
            player.setName(record.characterName);
            player.setShipType(record.shipType);
            player.setCurrentHull(new Ship(record.shipType, 0, 0, 0));
        }
        player.setDocked(record.docked || !record.hasCharacter());
        player.setCurrentStationName(record.stationName);
        player.setLocation(resolveDockedLocation(record.systemName, record.stationName));
        player.setVelocity(new DimensionalPosition(0.0, 0.0, 0.0));
        player.setFlightPlanIndex(player.getFlightPlan().length > 1 ? 1 : 0);
        player.setLastMotionUpdateMillis(System.currentTimeMillis());
        ensureDemoNpcState();
        return player;
    }

    private StationState defaultStationState() {
        return new StationState(
                this.serverName,
                "Unknown System",
                "Unknown Station",
                false,
                true,
                "",
                "",
                this.stationMarket == null ? new Contract[0] : this.stationMarket.getMarketData(),
                SHIP_OPTIONS
        );
    }

    private void ensureDemoNpcState() {
        for (int i = 0; i < this.nonPlayerCharacters.length; i++) {
            NonPlayerCharacter npc = this.nonPlayerCharacters[i];
            if (npc == null) {
                continue;
            }
            npc.setCurrentLocation(new DimensionalPosition(140.0 + (i * 180.0), 80.0 + (i * 90.0), 0.0));
            npc.setTransponderActive(true);
            if (npc.getFlightPlan().length == 0) {
                npc.setFlightPlanLabel("Filed route: waypoint A -> waypoint B");
                npc.setFlightPlan(new DimensionalPosition[] {
                        npc.getCurrentLocation(),
                        new DimensionalPosition(npc.getCurrentLocation().getX() + 220.0, npc.getCurrentLocation().getY() - 110.0, 0.0)
                });
            }
        }
    }

    private void advancePlayerMotion(PlayerCharacter player) {
        if (player == null || player.isDocked()) {
            return;
        }
        DimensionalPosition[] route = player.getFlightPlan();
        if (route.length < 2) {
            return;
        }

        long now = System.currentTimeMillis();
        long last = player.getLastMotionUpdateMillis();
        if (last <= 0L) {
            player.setLastMotionUpdateMillis(now);
            return;
        }

        double dt = (now - last) / 1000.0;
        if (dt <= 0.0) {
            return;
        }
        player.setLastMotionUpdateMillis(now);

        int routeIndex = Math.max(1, Math.min(player.getFlightPlanIndex(), route.length - 1));
        DimensionalPosition current = player.getCurrentLocation();
        while (routeIndex < route.length) {
            DimensionalPosition target = route[routeIndex];
            double distanceToTarget = distance(current, target);
            if (distanceToTarget < 0.001) {
                current = target;
                routeIndex++;
                continue;
            }

            double remaining = remainingRouteDistance(current, route, routeIndex);
            double currentSpeed = player.getCurrentSpeed();
            double stoppingDistance = (currentSpeed * currentSpeed) / (2.0 * ACCELERATION_KM_PER_SEC2);
            if (remaining <= stoppingDistance + 5.0) {
                currentSpeed = Math.max(0.0, currentSpeed - (ACCELERATION_KM_PER_SEC2 * dt));
            } else {
                currentSpeed = Math.min(MAX_SPEED_KM_PER_SEC, currentSpeed + (ACCELERATION_KM_PER_SEC2 * dt));
            }

            double step = currentSpeed * dt;
            if (step >= distanceToTarget) {
                current = copyPosition(target);
                dt -= distanceToTarget / Math.max(1.0, currentSpeed);
                routeIndex++;
                player.setCurrentSpeed(Math.max(0.0, currentSpeed * 0.9));
                continue;
            }

            DimensionalPosition next = moveTowards(current, target, step);
            player.setLocation(next);
            player.setVelocity(directionVector(current, target, currentSpeed));
            player.setCurrentSpeed(currentSpeed);
            player.setFlightPlanIndex(routeIndex);
            return;
        }

        player.setLocation(current);
        player.setVelocity(new DimensionalPosition(0.0, 0.0, 0.0));
        player.setCurrentSpeed(0.0);
        player.setFlightPlan(new DimensionalPosition[0]);
        player.setFlightPlanLabel("No filed flight plan");
        player.setFlightPlanIndex(0);
    }

    private void layoutStarSystem(Star system) {
        Planet[] planets = system.getPlanets();
        if (planets == null || planets.length == 0) {
            return;
        }
        double orbitRadius = 280.0;
        for (int i = 0; i < planets.length; i++) {
            Planet planet = planets[i];
            if (planet.getPosition() == null || isOrigin(planet.getPosition())) {
                double angle = (Math.PI * 2.0 * i) / planets.length;
                planet.setPosition(new DimensionalPosition(
                        Math.cos(angle) * orbitRadius,
                        Math.sin(angle) * orbitRadius,
                        0.0
                ));
            }
            Moon[] moons = planet.getMoons();
            if (moons != null && moons.length > 0) {
                double moonRadius = 60.0;
                for (int j = 0; j < moons.length; j++) {
                    Moon moon = moons[j];
                    if (moon.getPosition() == null || isOrigin(moon.getPosition())) {
                        double angle = (Math.PI * 2.0 * j) / moons.length;
                        moon.setPosition(new DimensionalPosition(
                                planet.getPosition().getX() + Math.cos(angle) * moonRadius,
                                planet.getPosition().getY() + Math.sin(angle) * moonRadius,
                                0.0
                        ));
                    }
                }
            }
            orbitRadius += 180.0;
        }
    }

    private boolean isOrigin(DimensionalPosition position) {
        return Math.abs(position.getX()) < 0.0001 && Math.abs(position.getY()) < 0.0001 && Math.abs(position.getZ()) < 0.0001;
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

    private Market loadStationMarket() {
        Market market = new Market();
        try {
            market.fromCSV("testing/celestialsons.market.csv");
        } catch (IOException e) {
            return null;
        }
        return market;
    }

    private void ensureUsersDatabase() {
        try {
            if (USERS_DATABASE.getParent() != null) {
                Files.createDirectories(USERS_DATABASE.getParent());
            }
            if (Files.notExists(USERS_DATABASE)) {
                Files.writeString(USERS_DATABASE, "serverName,username,salt,passwordHash,createdAt,characterName,shipType,systemName,stationName,docked\n", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize users database.", e);
        }
    }

    private List<AccountRecord> readUserRecords() throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        List<String> lines = Files.readAllLines(USERS_DATABASE, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",", -1);
            records.add(AccountRecord.fromCsv(parts));
        }
        return records;
    }

    private void writeUserRecords(List<AccountRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(USERS_DATABASE, StandardCharsets.UTF_8)) {
            writer.write("serverName,username,salt,passwordHash,createdAt,characterName,shipType,systemName,stationName,docked");
            writer.newLine();
            for (AccountRecord record : records) {
                writer.write(record.toCsvLine());
                writer.newLine();
            }
        }
    }

    private List<AccountRecord> replaceRecord(List<AccountRecord> records, AccountRecord updatedRecord) {
        List<AccountRecord> result = new ArrayList<>();
        for (AccountRecord record : records) {
            if (record.serverName.equalsIgnoreCase(updatedRecord.serverName)
                    && record.username.equalsIgnoreCase(updatedRecord.username)) {
                result.add(updatedRecord);
            } else {
                result.add(record);
            }
        }
        return result;
    }

    private AccountRecord createAccountRecord(String username, char[] password) {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String passwordHash = hashPassword(salt, password);
        StationAssignment assignment = pickRandomStationAssignment();
        return new AccountRecord(
                this.serverName,
                username,
                salt,
                passwordHash,
                Instant.now().toString(),
                "",
                "",
                assignment.systemName,
                assignment.stationName,
                true
        );
    }

    private StationAssignment pickRandomStationAssignment() {
        SpaceStation[] stations = this.universe == null ? null : this.universe.getStationList();
        if (stations != null && stations.length > 0) {
            List<SpaceStation> candidates = new ArrayList<>();
            for (SpaceStation station : stations) {
                if (station != null && station.getPosition() != null) {
                    candidates.add(station);
                }
            }
            if (!candidates.isEmpty()) {
                SpaceStation station = candidates.get(SECURE_RANDOM.nextInt(candidates.size()));
                Star star = resolveNearestStar(station.getPosition());
                String systemName = star == null ? "High Security System" : star.getName();
                return new StationAssignment(systemName, station.getName());
            }
        }

        Star[] stars = this.universe == null ? null : this.universe.getStarList();
        if (stars == null || stars.length == 0) {
            return new StationAssignment("High Security System", "Local Station");
        }

        List<Star> candidates = new ArrayList<>();
        for (Star star : stars) {
            if (star != null) {
                candidates.add(star);
            }
        }
        if (candidates.isEmpty()) {
            return new StationAssignment("High Security System", "Local Station");
        }

        Star chosenStar = candidates.get(SECURE_RANDOM.nextInt(candidates.size()));
        String systemName = chosenStar.getName();
        String stationName = chooseStationName(chosenStar);
        return new StationAssignment(systemName, stationName);
    }

    private String chooseStationName(Star star) {
        if (star == null) {
            return "Local Station";
        }
        SpaceStation[] stations = this.universe == null ? null : this.universe.getStationList();
        if (stations != null) {
            List<SpaceStation> matches = new ArrayList<>();
            for (SpaceStation station : stations) {
                if (station == null || station.getPosition() == null) {
                    continue;
                }
                Star stationSystem = resolveNearestStar(station.getPosition());
                if (stationSystem != null && stationSystem.getName().equals(star.getName())) {
                    matches.add(station);
                }
            }
            if (!matches.isEmpty()) {
                return matches.get(SECURE_RANDOM.nextInt(matches.size())).getName();
            }
        }
        Planet[] planets = star.getPlanets();
        if (planets != null && planets.length > 0) {
            Planet planet = planets[SECURE_RANDOM.nextInt(planets.length)];
            return planet.getName() + " Trade Hub";
        }
        return star.getName() + " Station";
    }

    private Star resolveNearestStar(DimensionalPosition position) {
        if (position == null || this.universe == null) {
            return null;
        }
        Star[] stars = this.universe.getStarList();
        if (stars == null || stars.length == 0) {
            return null;
        }
        Star closestStar = null;
        double closestDistance = Double.MAX_VALUE;
        for (Star star : stars) {
            if (star == null || star.getPosition() == null) {
                continue;
            }
            double distance = distanceSquared(star.getPosition(), position);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestStar = star;
            }
        }
        return closestStar;
    }

    private DimensionalPosition resolveDockedLocation(String systemName, String stationName) {
        DimensionalPosition stationPosition = resolveStationPosition(systemName, stationName);
        if (stationPosition == null) {
            return new DimensionalPosition(0.0, 0.0, 0.0);
        }
        return stationPosition;
    }

    private SpaceStation findStation(String systemName, String stationName) {
        if (this.universe == null || stationName == null || stationName.isBlank()) {
            return null;
        }
        SpaceStation[] stations = this.universe.getStationList();
        if (stations == null) {
            return null;
        }
        for (SpaceStation station : stations) {
            if (station == null || station.getPosition() == null) {
                continue;
            }
            if (!stationName.equalsIgnoreCase(station.getName())) {
                continue;
            }
            if (systemName == null || systemName.isBlank()) {
                return station;
            }
            Star nearestStar = resolveNearestStar(station.getPosition());
            if (nearestStar != null && systemName.equalsIgnoreCase(nearestStar.getName())) {
                return station;
            }
        }
        return null;
    }

    private DimensionalPosition resolveSafeDestination(Star system, DimensionalPosition start, DimensionalPosition desired) {
        DimensionalPosition safeTarget = copyPosition(desired);
        List<OrbitalObstacle> obstacles = collectObstacles(system);
        for (OrbitalObstacle obstacle : obstacles) {
            if (obstacle.containsPoint(safeTarget)) {
                DimensionalPosition away = vectorFromCenter(obstacle.position, start);
                if (away == null) {
                    away = new DimensionalPosition(1.0, 0.0, 0.0);
                }
                double length = Math.max(1.0, Math.sqrt((away.getX() * away.getX()) + (away.getY() * away.getY())));
                double offset = obstacle.radius + OBSTACLE_MARGIN_KM;
                safeTarget = new DimensionalPosition(
                        obstacle.position.getX() + (away.getX() / length) * offset,
                        obstacle.position.getY() + (away.getY() / length) * offset,
                        desired.getZ()
                );
            }
        }
        return safeTarget;
    }

    private DimensionalPosition[] buildFlightRoute(Star system, DimensionalPosition start, DimensionalPosition destination) {
        List<OrbitalObstacle> obstacles = collectObstacles(system);
        List<DimensionalPosition> route = new ArrayList<>();
        route.add(copyPosition(start));
        addRouteSegment(route, start, destination, obstacles, 0);
        if (!samePoint(route.get(route.size() - 1), destination)) {
            route.add(copyPosition(destination));
        }
        return route.toArray(new DimensionalPosition[0]);
    }

    private void addRouteSegment(List<DimensionalPosition> route,
                                 DimensionalPosition start,
                                 DimensionalPosition end,
                                 List<OrbitalObstacle> obstacles,
                                 int depth) {
        if (depth > 12 || !segmentIntersectsAnyObstacle(start, end, obstacles)) {
            if (route.isEmpty() || !samePoint(route.get(route.size() - 1), end)) {
                route.add(copyPosition(end));
            }
            return;
        }

        OrbitalObstacle obstacle = firstIntersectedObstacle(start, end, obstacles);
        if (obstacle == null) {
            if (route.isEmpty() || !samePoint(route.get(route.size() - 1), end)) {
                route.add(copyPosition(end));
            }
            return;
        }

        DimensionalPosition detour = chooseDetourPoint(start, end, obstacle, obstacles);
        if (!samePoint(route.get(route.size() - 1), detour)) {
            route.add(detour);
        }
        addRouteSegment(route, detour, end, obstacles, depth + 1);
    }

    private DimensionalPosition chooseDetourPoint(DimensionalPosition start,
                                                  DimensionalPosition end,
                                                  OrbitalObstacle obstacle,
                                                  List<OrbitalObstacle> obstacles) {
        DimensionalPosition segment = vectorFrom(start, end);
        if (segment == null) {
            return copyPosition(end);
        }
        DimensionalPosition perpendicular = new DimensionalPosition(-segment.getY(), segment.getX(), 0.0);
        double length = Math.max(1.0, Math.sqrt((perpendicular.getX() * perpendicular.getX()) + (perpendicular.getY() * perpendicular.getY())));
        double offset = obstacle.radius + OBSTACLE_MARGIN_KM;

        DimensionalPosition candidateA = new DimensionalPosition(
                obstacle.position.getX() + (perpendicular.getX() / length) * offset,
                obstacle.position.getY() + (perpendicular.getY() / length) * offset,
                end.getZ()
        );
        DimensionalPosition candidateB = new DimensionalPosition(
                obstacle.position.getX() - (perpendicular.getX() / length) * offset,
                obstacle.position.getY() - (perpendicular.getY() / length) * offset,
                end.getZ()
        );

        double scoreA = routeScore(start, candidateA, end, obstacles);
        double scoreB = routeScore(start, candidateB, end, obstacles);
        return scoreA <= scoreB ? candidateA : candidateB;
    }

    private double routeScore(DimensionalPosition start, DimensionalPosition candidate, DimensionalPosition end, List<OrbitalObstacle> obstacles) {
        double score = distance(start, candidate) + distance(candidate, end);
        for (OrbitalObstacle obstacle : obstacles) {
            if (segmentIntersectsObstacle(start, candidate, obstacle) || segmentIntersectsObstacle(candidate, end, obstacle)) {
                score += obstacle.radius * 3.0;
            }
        }
        return score;
    }

    private boolean segmentIntersectsAnyObstacle(DimensionalPosition start, DimensionalPosition end, List<OrbitalObstacle> obstacles) {
        for (OrbitalObstacle obstacle : obstacles) {
            if (segmentIntersectsObstacle(start, end, obstacle)) {
                return true;
            }
        }
        return false;
    }

    private OrbitalObstacle firstIntersectedObstacle(DimensionalPosition start, DimensionalPosition end, List<OrbitalObstacle> obstacles) {
        OrbitalObstacle closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (OrbitalObstacle obstacle : obstacles) {
            if (!segmentIntersectsObstacle(start, end, obstacle)) {
                continue;
            }
            double distance = distance(start, obstacle.position);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = obstacle;
            }
        }
        return closest;
    }

    private boolean segmentIntersectsObstacle(DimensionalPosition start, DimensionalPosition end, OrbitalObstacle obstacle) {
        if (obstacle == null) {
            return false;
        }
        double dist = distancePointToSegment(obstacle.position, start, end);
        return dist < obstacle.radius;
    }

    private double distancePointToSegment(DimensionalPosition point, DimensionalPosition start, DimensionalPosition end) {
        double vx = end.getX() - start.getX();
        double vy = end.getY() - start.getY();
        double wx = point.getX() - start.getX();
        double wy = point.getY() - start.getY();
        double segmentLengthSquared = (vx * vx) + (vy * vy);
        if (segmentLengthSquared <= 0.0001) {
            return distance(point, start);
        }
        double t = ((wx * vx) + (wy * vy)) / segmentLengthSquared;
        t = Math.max(0.0, Math.min(1.0, t));
        double closestX = start.getX() + (vx * t);
        double closestY = start.getY() + (vy * t);
        double dx = point.getX() - closestX;
        double dy = point.getY() - closestY;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    private List<OrbitalObstacle> collectObstacles(Star system) {
        if (system == null) {
            return Collections.emptyList();
        }
        List<OrbitalObstacle> obstacles = new ArrayList<>();
        if (system.getPosition() != null) {
            obstacles.add(new OrbitalObstacle(copyPosition(system.getPosition()), 65.0));
        }
        Planet[] planets = system.getPlanets();
        if (planets != null) {
            for (Planet planet : planets) {
                if (planet == null || planet.getPosition() == null) {
                    continue;
                }
                obstacles.add(new OrbitalObstacle(copyPosition(planet.getPosition()), 24.0 + Math.max(0, planet.getPlanetSize() * 2.0)));
                Moon[] moons = planet.getMoons();
                if (moons != null) {
                    for (Moon moon : moons) {
                        if (moon != null && moon.getPosition() != null) {
                            obstacles.add(new OrbitalObstacle(copyPosition(moon.getPosition()), 8.0));
                        }
                    }
                }
                SpaceStation[] stations = planet.getSpaceStations();
                if (stations != null) {
                    for (SpaceStation station : stations) {
                        if (station != null && station.getPosition() != null) {
                            obstacles.add(new OrbitalObstacle(copyPosition(station.getPosition()), 12.0));
                        }
                    }
                }
            }
        }
        SpaceStation[] stations = this.universe == null ? null : this.universe.getStationList();
        if (stations != null) {
            for (SpaceStation station : stations) {
                if (station != null && station.getPosition() != null) {
                    obstacles.add(new OrbitalObstacle(copyPosition(station.getPosition()), 12.0));
                }
            }
        }
        return obstacles;
    }

    private String buildFlightPlanLabel(DimensionalPosition[] route) {
        if (route == null || route.length == 0) {
            return "No filed flight plan";
        }
        StringBuilder text = new StringBuilder("Filed route: ");
        for (int i = 0; i < route.length; i++) {
            if (i > 0) {
                text.append(" -> ");
            }
            text.append(route[i].toLabel());
        }
        return text.toString();
    }

    private DimensionalPosition moveTowards(DimensionalPosition start, DimensionalPosition end, double distance) {
        double total = this.distance(start, end);
        if (total <= 0.0001 || distance >= total) {
            return copyPosition(end);
        }
        double ratio = distance / total;
        return new DimensionalPosition(
                start.getX() + ((end.getX() - start.getX()) * ratio),
                start.getY() + ((end.getY() - start.getY()) * ratio),
                start.getZ() + ((end.getZ() - start.getZ()) * ratio)
        );
    }

    private DimensionalPosition directionVector(DimensionalPosition start, DimensionalPosition end, double speed) {
        double total = distance(start, end);
        if (total <= 0.0001 || speed <= 0.0) {
            return new DimensionalPosition(0.0, 0.0, 0.0);
        }
        double scale = speed / total;
        return new DimensionalPosition((end.getX() - start.getX()) * scale, (end.getY() - start.getY()) * scale, (end.getZ() - start.getZ()) * scale);
    }

    private double remainingRouteDistance(DimensionalPosition start, DimensionalPosition[] route, int index) {
        double total = 0.0;
        DimensionalPosition current = start;
        for (int i = index; i < route.length; i++) {
            total += distance(current, route[i]);
            current = route[i];
        }
        return total;
    }

    private DimensionalPosition copyPosition(DimensionalPosition position) {
        if (position == null) {
            return new DimensionalPosition(0.0, 0.0, 0.0);
        }
        return new DimensionalPosition(position.getX(), position.getY(), position.getZ());
    }

    private boolean samePoint(DimensionalPosition a, DimensionalPosition b) {
        return distance(a, b) < 0.001;
    }

    private double distance(DimensionalPosition a, DimensionalPosition b) {
        return Math.sqrt(distanceSquared(a, b));
    }

    private DimensionalPosition vectorFrom(DimensionalPosition from, DimensionalPosition to) {
        if (from == null || to == null) {
            return null;
        }
        return new DimensionalPosition(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
    }

    private DimensionalPosition vectorFromCenter(DimensionalPosition center, DimensionalPosition toward) {
        if (center == null || toward == null) {
            return null;
        }
        return new DimensionalPosition(toward.getX() - center.getX(), toward.getY() - center.getY(), toward.getZ() - center.getZ());
    }

    private DimensionalPosition resolveStationPosition(String systemName, String stationName) {
        if (this.universe == null || stationName == null || stationName.isBlank()) {
            return null;
        }
        SpaceStation[] stations = this.universe.getStationList();
        if (stations == null) {
            return null;
        }
        for (SpaceStation station : stations) {
            if (station == null || station.getPosition() == null) {
                continue;
            }
            boolean nameMatches = stationName.equalsIgnoreCase(station.getName());
            boolean systemMatches = true;
            if (systemName != null && !systemName.isBlank()) {
                Star nearestStar = resolveNearestStar(station.getPosition());
                systemMatches = nearestStar != null && systemName.equalsIgnoreCase(nearestStar.getName());
            }
            if (nameMatches && systemMatches) {
                return station.getPosition();
            }
        }
        return null;
    }

    private double distanceSquared(DimensionalPosition first, DimensionalPosition second) {
        if (first == null || second == null) {
            return Double.MAX_VALUE;
        }
        double dx = first.getX() - second.getX();
        double dy = first.getY() - second.getY();
        double dz = first.getZ() - second.getZ();
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    private static final class OrbitalObstacle {
        private final DimensionalPosition position;
        private final double radius;

        private OrbitalObstacle(DimensionalPosition position, double radius) {
            this.position = position;
            this.radius = radius;
        }

        private boolean containsPoint(DimensionalPosition point) {
            if (point == null || this.position == null) {
                return false;
            }
            double dx = point.getX() - this.position.getX();
            double dy = point.getY() - this.position.getY();
            double dz = point.getZ() - this.position.getZ();

            double distanceSquared = (dx * dx) + (dy * dy) + (dz * dz);

            return distanceSquared <= (this.radius * this.radius);
        }
    }

    private Star resolveSystem(String systemName) {
        if (this.universe == null || systemName == null || systemName.isBlank()) {
            return null;
        }
        try {
            Star star = this.universe.findSystem(systemName);
            if (star != null) {
                return star;
            }
        } catch (NullPointerException ignored) {
        }
        Star[] stars = this.universe.getStarList();
        if (stars != null && stars.length > 0) {
            return stars[0];
        }
        return null;
    }

    private AccountRecord findUser(List<AccountRecord> records, String username) {
        for (AccountRecord record : records) {
            if (record.serverName.equalsIgnoreCase(this.serverName) && record.username.equalsIgnoreCase(username)) {
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

    private static final class StationAssignment {
        private final String systemName;
        private final String stationName;

        private StationAssignment(String systemName, String stationName) {
            this.systemName = systemName;
            this.stationName = stationName;
        }
    }

    private static final class AccountRecord {
        private final String serverName;
        private final String username;
        private final String salt;
        private final String passwordHash;
        private final String createdAt;
        private String characterName;
        private String shipType;
        private String systemName;
        private String stationName;
        private boolean docked;

        private AccountRecord(String serverName,
                              String username,
                              String salt,
                              String passwordHash,
                              String createdAt,
                              String characterName,
                              String shipType,
                              String systemName,
                              String stationName,
                              boolean docked) {
            this.serverName = serverName;
            this.username = username;
            this.salt = salt;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
            this.characterName = characterName;
            this.shipType = shipType;
            this.systemName = systemName;
            this.stationName = stationName;
            this.docked = docked;
        }

        private static AccountRecord fromCsv(String[] parts) {
            String serverName = part(parts, 0);
            String username = part(parts, 1);
            String salt = part(parts, 2);
            String passwordHash = part(parts, 3);
            String createdAt = part(parts, 4);
            String characterName = part(parts, 5);
            String shipType = part(parts, 6);
            String systemName = part(parts, 7);
            String stationName = part(parts, 8);
            boolean docked = Boolean.parseBoolean(part(parts, 9));
            if (serverName.isEmpty()) {
                serverName = "Local Game Server";
            }
            return new AccountRecord(serverName, username, salt, passwordHash, createdAt, characterName, shipType, systemName, stationName, docked);
        }

        private static String part(String[] parts, int index) {
            return index < parts.length ? parts[index] : "";
        }

        private boolean matchesPassword(char[] password) {
            return this.passwordHash.equals(hashPassword(this.salt, password));
        }

        private boolean hasCharacter() {
            return this.characterName != null && !this.characterName.isBlank();
        }

        private boolean isDocked() {
            return this.docked;
        }

        private String toCsvLine() {
            return String.join(",",
                    this.serverName,
                    this.username,
                    this.salt,
                    this.passwordHash,
                    this.createdAt,
                    safe(this.characterName),
                    safe(this.shipType),
                    safe(this.systemName),
                    safe(this.stationName),
                    Boolean.toString(this.docked));
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
