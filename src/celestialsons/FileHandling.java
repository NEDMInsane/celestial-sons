/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package celestialsons;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

import orbitalbodies.*;

public class FileHandling {
    
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
