
package celestialsons;

import market.Market;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CelestialSons {
    
    public static void main(String[] args) throws IOException {
        /*
        GraphicalInterface mainMenu = new GraphicalInterface();
        System.out.println("Welcome to Celestial Sons");
        Universe newUniverse = FileHandling.loadAlmanacData();
        gameLoop();
        mainMenu.mainMenu();
        CommandLineUtils.utilityCommandLineMenu();
        String[] outputStr = {"\"Bob", "Joe\\", "Jim", "1/", "2", "3"};
        FileHandling.convertToCSV(outputStr, "TEST.csv");

        for(String items : FileHandling.convertFromCSV("star_file.csv")){
            System.out.println(items);
        }*/
        Market market = new Market();
        market.fromCSV("testing/market.csv");
        market.printMarketData();
    }

    
    public static void gameLoop(){
        GraphicalInterface mainMenu = new GraphicalInterface();
        PlayerCharacter[] playerCharacters = new PlayerCharacter[5];
        playerCharacters[0] = new PlayerCharacter("NEDMInsane", 100);
        playerCharacters[1] = new PlayerCharacter("Player1", 100);
        playerCharacters[2] = new PlayerCharacter("Player2", 100);
        playerCharacters[3] = new PlayerCharacter("Player3", 100);
        playerCharacters[4] = new PlayerCharacter("Player4", 100);

        NonPlayerCharacter[] nonPlayerCharacters = new NonPlayerCharacter[1];
        nonPlayerCharacters[0] = new NonPlayerCharacter("Paul");

        Universe universe = new Universe(null, null, null);

        mainMenu.mainMenu(universe, nonPlayerCharacters, playerCharacters);
//        while(CommandLineUtils.utilityCommandLineMenu(universe, player)){
//            System.out.println("Good-bye!");
//        }
    }
}
