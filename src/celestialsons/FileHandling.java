/**
 * TODO: This whole thing needs gone through again.
 */
package celestialsons;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import orbitalbodies.*;

public class FileHandling {

    public static void convertToCSV(String[] data, String filename) throws IOException {
        try {
            FileOutputStream outputStream = new FileOutputStream(filename);
            for (String dataEntry : data) {
                byte[] entryBytes = dataEntry.getBytes();
                outputStream.write(entryBytes);
                outputStream.write(",".getBytes());
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: ERR:" + e.getMessage());
        }
    }

    public static int getCSVFields(String filename) throws IOException {
        // Returns how many fields there are.
        FileInputStream inputStream = new FileInputStream(filename);
        int streamChar;
        int fields = 0;
        // While the character is not the EOF character, continue.
        while ((streamChar = inputStream.read()) != -1) {
            if (streamChar == ',') {
                fields++;
            }
        }
        inputStream.close();
        return fields;
    }

    public static String[] convertFromCSV(String filename) throws IOException {
        FileInputStream inputStream = new FileInputStream(filename);
        // Using getCSVFields tog et the side of the array needed.
        String[] data = new String[getCSVFields(filename)];
        StringBuilder tempString = new StringBuilder();
        int i = 0, streamChar;
        // Need to purge special Characters.
        while((streamChar = inputStream.read()) != -1){
            if(streamChar != ','){
                tempString.append((char)streamChar);
            } else {
                data[i] = tempString.toString();
                tempString = new StringBuilder();
                i++;
            }
        }
        inputStream.close();
        return data;
    }
    
    public static Star[] loadStarList(FileReader reader, Scanner scanner, File file) throws IOException{
        int universeSize = 250;
        reader = new FileReader(file);
        scanner = new Scanner(reader);
        // Dump the Header data.
        scanner.nextLine();
        scanner.useDelimiter(",");
        Star[] starList = new Star[universeSize];
        int i = 0;
        while(scanner.hasNext()){
            String starName = scanner.next();
            int starSize = scanner.nextInt();
            String planets = scanner.next(); //This is unused.
            String starType = scanner.nextLine();
            starType = starType.substring(1);
            starList[i] = new Star(starName, starType, starSize);
            i++;            
            //System.out.printf("Star Name: %s, Star Size: %d, Planets: %s, Star Type: %s\n", starName, starSize, planets, starType);
        }
        reader.close();
        System.out.println("Starlist created.");
        return starList;
    }
    
    public static Planet[] loadPlanetList(FileReader reader, Scanner scanner, File file) throws IOException{
        reader = new FileReader(file);
        scanner = new Scanner(reader);
        // Dump the header data.
        scanner.useDelimiter(",");
        scanner.nextLine();
        Planet[] planetList = new Planet[fileLength(file) - 1];
        int i = 0;
        while(scanner.hasNext()){
            String starName = scanner.next();
            String planetName = scanner.next();
            int planetSize = scanner.nextInt();
            int moons = scanner.nextInt(); //Throw away value.
            int asteroidBelts = scanner.nextInt();
            scanner.nextLine();
            planetList[i] = new Planet(null, planetName, null, planetSize, asteroidBelts);
            //System.out.printf("Star: %s, Planet: %s, Size: %d, Moons: %d, Belts: %s\n", starName, planetName, planetSize, moons, asteroidBelts);
            i++;
        }
        reader.close();
        System.out.println("Planetlist created.");
        return planetList;
    }
    
    public static Moon[] loadMoonList(FileReader reader, Scanner scanner, File file) throws IOException{
        reader = new FileReader(file);
        scanner = new Scanner(reader);
        
        scanner.useDelimiter(",");
        scanner.nextLine(); // Get rid of the header data.
        Moon[] moonList = new Moon[fileLength(file) - 1];
        int i = 0;
        System.out.println(i);
        while(scanner.hasNext()){
           String planetName = scanner.next();
           String moonName = scanner.next();
           int moonSize = scanner.nextInt();
           String resources = scanner.next();
           scanner.nextLine();
           //System.out.printf("Planet: %s, Moon: %s, Size: %d, Resources: %s\n",planetName, moonName, moonSize, resources);   
           moonList[i] = new Moon(moonName, moonSize, resources);
           i++;
        }
        reader.close();
        System.out.println("Moonlist created.");
        return moonList;
    }
    
    public static int fileLength(File file) throws IOException{
        FileReader reader = new FileReader(file);
        Scanner scanner = new Scanner(reader);
        int length = 0;
        while(scanner.hasNextLine()){
            length++;
            scanner.nextLine();
        }
        return length;
    }

    public static Universe loadAlmanacData(String filePath) throws IOException{
        File starFile = new File(filePath + "/star_file.csv");
        File planetFile = new File(filePath + "/planet_file.csv");
        File moonFile = new File(filePath + "/moon_file.csv");
        File stationFile = new File(filePath + "/station_file.csv");

        List<String[]> starRows = readCsvRows(starFile);
        List<String[]> planetRows = readCsvRows(planetFile);
        List<String[]> moonRows = readCsvRows(moonFile);
        List<String[]> stationRows = stationFile.exists() ? readCsvRows(stationFile) : new ArrayList<>();

        List<Star> stars = new ArrayList<>();
        Map<String, Star> starsByName = new HashMap<>();
        for (int i = 0; i < starRows.size(); i++) {
            String[] row = starRows.get(i);
            if (row.length < 4) {
                continue;
            }
            String starName = cleanCsvValue(row[0]);
            int starSize = parseInt(cleanCsvValue(row[1]), 0);
            String starType = cleanCsvValue(row[3]);
            Star star = new Star(starName, starType, starSize);
            star.setPosition(generateStarMapPosition(i, starRows.size()));
            stars.add(star);
            starsByName.put(starName, star);
        }

        Map<String, List<Planet>> planetsByStar = new HashMap<>();
        Map<String, Planet> planetsByName = new HashMap<>();
        for (int i = 0; i < planetRows.size(); i++) {
            String[] row = planetRows.get(i);
            if (row.length < 5) {
                continue;
            }
            String starName = cleanCsvValue(row[0]);
            String planetName = cleanCsvValue(row[1]);
            int planetSize = parseInt(cleanCsvValue(row[2]), 0);
            int asteroidBelts = parseInt(cleanCsvValue(row[4]), 0);
            Star star = starsByName.get(starName);
            Planet planet = new Planet(star, planetName, "Unknown", planetSize, asteroidBelts);
            planetsByName.put(planetName, planet);
            planetsByStar.computeIfAbsent(starName, key -> new ArrayList<>()).add(planet);
        }

        Map<String, List<Moon>> moonsByPlanet = new HashMap<>();
        for (String[] row : moonRows) {
            if (row.length < 4) {
                continue;
            }
            String planetName = cleanCsvValue(row[0]);
            String moonName = cleanCsvValue(row[1]);
            int size = parseInt(cleanCsvValue(row[2]), 0);
            String resources = cleanCsvValue(row[3]);
            Moon moon = new Moon(moonName, size, resources);
            moonsByPlanet.computeIfAbsent(planetName, key -> new ArrayList<>()).add(moon);
        }

        List<Planet> allPlanets = new ArrayList<>();
        List<Moon> allMoons = new ArrayList<>();
        for (Map.Entry<String, List<Planet>> entry : planetsByStar.entrySet()) {
            Star star = starsByName.get(entry.getKey());
            if (star == null) {
                continue;
            }
            List<Planet> starPlanets = entry.getValue();
            Planet[] attachedPlanets = new Planet[starPlanets.size()];
            for (int i = 0; i < starPlanets.size(); i++) {
                Planet planet = starPlanets.get(i);
                planet.setPosition(generatePlanetPosition(star.getPosition(), i, starPlanets.size()));
                List<Moon> moons = moonsByPlanet.get(planet.getName());
                if (moons != null && !moons.isEmpty()) {
                    Moon[] attachedMoons = new Moon[moons.size()];
                    for (int moonIndex = 0; moonIndex < moons.size(); moonIndex++) {
                        Moon moon = moons.get(moonIndex);
                        moon.setPosition(generateMoonPosition(planet.getPosition(), moonIndex, moons.size()));
                        attachedMoons[moonIndex] = moon;
                        allMoons.add(moon);
                    }
                    planet = new Planet(star, planet.getName(), planet.getPlanetType(), planet.getPlanetSize(), attachedMoons, planet.getAsteroidBelts());
                    planet.setPosition(generatePlanetPosition(star.getPosition(), i, starPlanets.size()));
                }
                attachedPlanets[i] = planet;
                allPlanets.add(planet);
            }
            star.setPlanets(attachedPlanets);
        }

        List<SpaceStation> allStations = new ArrayList<>();
        for (String[] row : stationRows) {
            if (row.length < 6) {
                continue;
            }
            String systemName = cleanCsvValue(row[0]);
            String stationName = cleanCsvValue(row[1]);
            String stationType = cleanCsvValue(row[2]);
            double x = parseDouble(cleanCsvValue(row[3]), 0.0);
            double y = parseDouble(cleanCsvValue(row[4]), 0.0);
            double z = parseDouble(cleanCsvValue(row[5]), 0.0);
            SpaceStation station = new SpaceStation(stationName);
            station.setPosition(new celestialsons.DimensionalPosition(x, y, z));
            allStations.add(station);

            Star star = starsByName.get(systemName);
            if (star != null && star.getPlanets() != null && star.getPlanets().length > 0) {
                Planet[] planets = star.getPlanets();
                Planet closestPlanet = planets[0];
                double closestDistance = distanceSquared(closestPlanet.getPosition(), station.getPosition());
                for (int i = 1; i < planets.length; i++) {
                    double distance = distanceSquared(planets[i].getPosition(), station.getPosition());
                    if (distance < closestDistance) {
                        closestPlanet = planets[i];
                        closestDistance = distance;
                    }
                }
                SpaceStation[] stations = closestPlanet.getSpaceStations();
                if (stations == null) {
                    stations = new SpaceStation[0];
                }
                SpaceStation[] updatedStations = Arrays.copyOf(stations, stations.length + 1);
                updatedStations[updatedStations.length - 1] = station;
                closestPlanet.setSpaceStations(updatedStations);
            }
        }

        Universe universe = new Universe(
                stars.toArray(new Star[0]),
                allPlanets.toArray(new Planet[0]),
                allMoons.toArray(new Moon[0]),
                allStations.toArray(new SpaceStation[0])
        );
        return universe;
    }

    private static List<String[]> readCsvRows(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                rows.add(line.split(",", -1));
            }
        }
        return rows;
    }

    private static String cleanCsvValue(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static celestialsons.DimensionalPosition generateStarMapPosition(int index, int totalStars) {
        if (totalStars == 7) {
            celestialsons.DimensionalPosition[] layout = new celestialsons.DimensionalPosition[] {
                    new celestialsons.DimensionalPosition(-980.0, 240.0, -120.0),
                    new celestialsons.DimensionalPosition(-760.0, -180.0, 110.0),
                    new celestialsons.DimensionalPosition(-520.0, 140.0, 40.0),
                    new celestialsons.DimensionalPosition(0.0, 0.0, 0.0),
                    new celestialsons.DimensionalPosition(520.0, -120.0, -40.0),
                    new celestialsons.DimensionalPosition(760.0, 180.0, 120.0),
                    new celestialsons.DimensionalPosition(980.0, -220.0, -100.0)
            };
            if (index >= 0 && index < layout.length) {
                return layout[index];
            }
        }

        double angle = index * 0.35;
        double radius = 180.0 + (index * 12.5);
        double x = Math.cos(angle) * radius;
        double y = Math.sin(angle) * radius;
        double z = ((index % 7) - 3) * 35.0;
        return new celestialsons.DimensionalPosition(x, y, z);
    }

    private static celestialsons.DimensionalPosition generatePlanetPosition(celestialsons.DimensionalPosition starPosition, int index, int total) {
        double orbitRadius = 90.0 + (index * 35.0);
        double angle = (Math.PI * 2.0 * index) / Math.max(1, total);
        return new celestialsons.DimensionalPosition(
                starPosition.getX() + Math.cos(angle) * orbitRadius,
                starPosition.getY() + Math.sin(angle) * orbitRadius,
                starPosition.getZ() + ((index % 3) - 1) * 12.0
        );
    }

    private static celestialsons.DimensionalPosition generateMoonPosition(celestialsons.DimensionalPosition planetPosition, int index, int total) {
        double orbitRadius = 20.0 + (index * 8.0);
        double angle = (Math.PI * 2.0 * index) / Math.max(1, total);
        return new celestialsons.DimensionalPosition(
                planetPosition.getX() + Math.cos(angle) * orbitRadius,
                planetPosition.getY() + Math.sin(angle) * orbitRadius,
                planetPosition.getZ() + ((index % 2) == 0 ? 5.0 : -5.0)
        );
    }

    private static double distanceSquared(celestialsons.DimensionalPosition first, celestialsons.DimensionalPosition second) {
        if (first == null || second == null) {
            return Double.MAX_VALUE;
        }
        double dx = first.getX() - second.getX();
        double dy = first.getY() - second.getY();
        double dz = first.getZ() - second.getZ();
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
