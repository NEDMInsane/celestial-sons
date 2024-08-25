/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package celestialsons;

import java.util.Scanner;
import orbitalbodies.*;


public class CommandLineUtils {

    public static boolean utilityCommandLineMenu(Universe universe, PlayerCharacter player){
        Scanner scnr = new Scanner(System.in);

        int selection = 0;
        while(selection != 9){
            System.out.println("*************************");
            System.out.println("*     Celestial Sons    *");
            System.out.println("*************************");
            System.out.println("* 1.) Character         *");
            System.out.println("* 2.) Skills            *");
            System.out.println("* 3.) List Stars        *");
            System.out.println("* 4.) List Planets      *");
            System.out.println("* 5.) List Moons        *");
            System.out.println("* 6.)                   *");
            System.out.println("* 7.)                   *");
            System.out.println("* 8.)                   *");
            System.out.println("* 9.)                   *");
            System.out.println("* 0.)                   *");
            System.out.println("*************************");
            selection = scnr.nextInt();

            switch(selection){
                case 1:
                    characterSelectionMenu(universe, player);
                    break;
                case 3:
                    universe.listStars();
                    break;
                case 4:
                    universe.listPlanets();
                    break;
                case 5:
                    universe.listMoons();
                    break;
                default:
                    return false;
            }
        }
        return false;
    }
    
    public static void characterSelectionMenu(Universe universe, PlayerCharacter player){
        Scanner scnr = new Scanner(System.in);

        int selection = 0;
        while(selection != 9){
            System.out.println("*************************");
            System.out.println("*     Celestial Sons    *");
            System.out.println("*      Player Menu      *");
            System.out.println("*************************");
            System.out.println("* 1.) Character Name    *");
            System.out.println("* 2.) Skills            *");
            System.out.println("* 3.) Current System    *");
            System.out.println("* 4.) Current Location  *");
            System.out.println("* 5.) Move Systems      *");
            System.out.println("* 6.) Teleport          *");
            System.out.println("* 7.)                   *");
            System.out.println("* 8.)                   *");
            System.out.println("* 9.) Quit              *");
            System.out.println("* 0.) Back              *");
            System.out.println("*************************");
            
            selection = scnr.nextInt();
            
            switch(selection){
                case 1:
                    System.out.println(player.getName());
                    break;
                case 2:
                    player.printSkills();
                    break;
                case 3:
                    System.out.println(player.getCurrentSystem());
                    break;
                case 4:
                    System.out.println(player.getCurrentLocation());
                    break;
                case 5:
                    Star newStar = universe.findSystem("273");
                    if(newStar != null){
                        player.setSystem(newStar);
                        System.out.printf("%s moved to %s\n", player.getName(), newStar.getName());
                    } else {
                        System.out.println("Could not find star system.");
                    }
                    break;
                case 6:
                    player.setLocation(new Vector3d(3.0, 3.0, 3.0));
                    break;
                default:
                    selection = 9;
                    break;
            }
        }
    }
    
}
