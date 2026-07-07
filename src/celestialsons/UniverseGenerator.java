package celestialsons;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class UniverseGenerator {
    private static final Random RANDOM = new Random(20260627L);
    private static final String[] SYSTEM_NAMES = {
            "Aurelia Reach",
            "Caelum Verge",
            "Ilyria Drift",
            "Meridian Axis",
            "Nadir Point",
            "Orion Span",
            "Vesper Gate"
    };
    private static final String[] STAR_TYPES = {
            "Yellow-white",
            "Blue-white",
            "White",
            "Orange",
            "Yellow",
            "Blue",
            "Red"
    };
    private static final String[] STATION_TYPES = {
            "Trade Hub",
            "Security Relay",
            "Industrial Dock",
            "Deep Space Station"
    };
    private static final String[] MOON_RESOURCES = {
            "Water",
            "Ice",
            "Silicates",
            "Metals",
            "Volatiles",
            "Organics"
    };

    private UniverseGenerator() {
    }

    public static void generateSevenSystemUniverse(String outputDirectory) throws IOException {
        Path output = Paths.get(outputDirectory);
        Files.createDirectories(output);

        List<SystemSpec> systems = buildSystems();
        writeStars(output.resolve("star_file.csv"), systems);
        writePlanets(output.resolve("planet_file.csv"), systems);
        writeMoons(output.resolve("moon_file.csv"), systems);
        writeStations(output.resolve("station_file.csv"), systems);
    }

    private static List<SystemSpec> buildSystems() {
        List<SystemSpec> systems = new ArrayList<>();
        for (int i = 0; i < SYSTEM_NAMES.length; i++) {
            SystemSpec system = new SystemSpec();
            system.name = SYSTEM_NAMES[i];
            system.starType = STAR_TYPES[i % STAR_TYPES.length];
            system.starSize = 1 + RANDOM.nextInt(5);
            system.position = layoutPosition(i);
            system.planetCount = 2 + RANDOM.nextInt(4);
            system.stationCount = 1 + RANDOM.nextInt(2);
            for (int planetIndex = 0; planetIndex < system.planetCount; planetIndex++) {
                PlanetSpec planet = new PlanetSpec();
                planet.name = system.name.replace(" ", "") + "-P" + (planetIndex + 1);
                planet.size = 1 + RANDOM.nextInt(5);
                planet.moons = RANDOM.nextInt(5);
                planet.asteroidBelts = RANDOM.nextInt(4);
                system.planets.add(planet);
            }
            systems.add(system);
        }
        return systems;
    }

    private static void writeStars(Path file, List<SystemSpec> systems) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("Star Name,Star Size,Planets,Color,");
            writer.newLine();
            for (SystemSpec system : systems) {
                writer.write(system.name);
                writer.write(',');
                writer.write(Integer.toString(system.starSize));
                writer.write(',');
                writer.write(Integer.toString(system.planetCount));
                writer.write(',');
                writer.write(system.starType);
                writer.write(',');
                writer.newLine();
            }
        }
    }

    private static void writePlanets(Path file, List<SystemSpec> systems) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("Star Name,Planet Name,Planet Size,Moons,Asteroid Belts,");
            writer.newLine();
            for (SystemSpec system : systems) {
                for (PlanetSpec planet : system.planets) {
                    writer.write(system.name);
                    writer.write(',');
                    writer.write(planet.name);
                    writer.write(',');
                    writer.write(Integer.toString(planet.size));
                    writer.write(',');
                    writer.write(Integer.toString(planet.moons));
                    writer.write(',');
                    writer.write(Integer.toString(planet.asteroidBelts));
                    writer.write(',');
                    writer.newLine();
                }
            }
        }
    }

    private static void writeMoons(Path file, List<SystemSpec> systems) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("Planet Name,Moon Name,Size,Resources,");
            writer.newLine();
            for (SystemSpec system : systems) {
                for (PlanetSpec planet : system.planets) {
                    for (int moonIndex = 0; moonIndex < planet.moons; moonIndex++) {
                        writer.write(planet.name);
                        writer.write(',');
                        writer.write(planet.name + "-M" + (moonIndex + 1));
                        writer.write(',');
                        writer.write(Integer.toString(1 + RANDOM.nextInt(5)));
                        writer.write(',');
                        writer.write(MOON_RESOURCES[RANDOM.nextInt(MOON_RESOURCES.length)]);
                        writer.write(',');
                        writer.newLine();
                    }
                }
            }
        }
    }

    private static void writeStations(Path file, List<SystemSpec> systems) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("System Name,Station Name,Station Type,X,Y,Z,");
            writer.newLine();
            for (int systemIndex = 0; systemIndex < systems.size(); systemIndex++) {
                SystemSpec system = systems.get(systemIndex);
                for (int stationIndex = 0; stationIndex < system.stationCount; stationIndex++) {
                    StationSpec station = buildStation(system, systemIndex, stationIndex);
                    writer.write(system.name);
                    writer.write(',');
                    writer.write(station.name);
                    writer.write(',');
                    writer.write(station.type);
                    writer.write(',');
                    writer.write(String.format("%.2f", station.position.getX()));
                    writer.write(',');
                    writer.write(String.format("%.2f", station.position.getY()));
                    writer.write(',');
                    writer.write(String.format("%.2f", station.position.getZ()));
                    writer.write(',');
                    writer.newLine();
                }
            }
        }
    }

    private static StationSpec buildStation(SystemSpec system, int systemIndex, int stationIndex) {
        StationSpec station = new StationSpec();
        station.type = STATION_TYPES[(systemIndex + stationIndex) % STATION_TYPES.length];
        station.name = system.name.replace(" ", "") + "-" + station.type.replace(" ", "") + "-" + (stationIndex + 1);
        double angle = (systemIndex * 0.8) + (stationIndex * 0.45);
        double radius = 520.0 + (stationIndex * 45.0);
        double x = system.position.getX() + Math.cos(angle) * radius;
        double y = system.position.getY() + Math.sin(angle) * radius;
        double z = system.position.getZ() + (((systemIndex + stationIndex) % 3) - 1) * 65.0;
        station.position = new DimensionalPosition(x, y, z);
        return station;
    }

    private static DimensionalPosition layoutPosition(int index) {
        DimensionalPosition[] layout = new DimensionalPosition[] {
                new DimensionalPosition(-980.0, 240.0, -120.0),
                new DimensionalPosition(-760.0, -180.0, 110.0),
                new DimensionalPosition(-520.0, 140.0, 40.0),
                new DimensionalPosition(0.0, 0.0, 0.0),
                new DimensionalPosition(520.0, -120.0, -40.0),
                new DimensionalPosition(760.0, 180.0, 120.0),
                new DimensionalPosition(980.0, -220.0, -100.0)
        };
        return layout[index];
    }

    private static final class SystemSpec {
        private String name;
        private String starType;
        private int starSize;
        private int planetCount;
        private int stationCount;
        private DimensionalPosition position;
        private final List<PlanetSpec> planets = new ArrayList<>();
    }

    private static final class PlanetSpec {
        private String name;
        private int size;
        private int moons;
        private int asteroidBelts;
    }

    private static final class StationSpec {
        private String name;
        private String type;
        private DimensionalPosition position;
    }
}
