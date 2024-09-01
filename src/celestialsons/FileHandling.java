
package celestialsons;

import java.io.*;
import java.util.Arrays;
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
        FileInputStream inputStream = new FileInputStream(filename);
        int streamChar;
        int fields = 0;
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
        String[] data = new String[getCSVFields(filename)];
        StringBuilder tempString = new StringBuilder();
        int i = 0, streamChar;
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
            planetList[i] = new Planet(planetName, null, planetSize, asteroidBelts);
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
        scanner.nextLine(); // Get ride of the header data.
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

        FileReader dataFileReader = null;
        Scanner dataFileScanner = null;
        
        Moon[] moonList = loadMoonList(dataFileReader, dataFileScanner, moonFile);
        Planet[] planetList = loadPlanetList(dataFileReader, dataFileScanner, planetFile);
        Star[] starList = loadStarList(dataFileReader, dataFileScanner, starFile);
        Universe universe = new Universe(starList, planetList, moonList);

        return universe;
    }
}
