package celestialsons;


import orbitalbodies.Planet;
import orbitalbodies.Star;
//Using EveOnline names, kill me.

public class GenerateUniverse {
    public static void main(String[] args) {
        Star jita = new Star("Jita");
        Star newCaldari = new Star("New Caldari");
        Star sobasekai = new Star("Sobasekai");

        Planet[] jitaPlanetList = new Planet[5];
        for (int i = 0; i < jitaPlanetList.length ; i++) {
            jitaPlanetList[i] = new Planet(jita, "Jita-" + i, "Any", 1, null, null);
        }
        jita.setPlanets(jitaPlanetList);
        Planet[] newCaldariPlanetList = new Planet[5];
        for (int i = 0; i < newCaldariPlanetList.length ; i++) {
            newCaldariPlanetList[i] = new Planet(newCaldari, "Caldari-" + i, "Any", 1, null, null);
        }
        newCaldari.setPlanets(newCaldariPlanetList);
        Planet[] sobasekiPlanetList = new Planet[5];
        for (int i = 0; i < sobasekiPlanetList.length ; i++) {
            sobasekiPlanetList[i] = new Planet(sobasekai, "Sobaseki-" + i, "Any", 1, null, null);
        }
        sobasekai.setPlanets(sobasekiPlanetList);

        Star[] starList = new Star[3];
        starList[0] = jita;
        starList[1] = newCaldari;
        starList[2] = sobasekai;

        PlayerCharacter[] playerCharacterList = new PlayerCharacter[1];
        playerCharacterList[0] = new PlayerCharacter("NEDMInsane", 100);
        playerCharacterList[0].setSystem(starList[0]);

        Universe universe = new Universe(starList);

        GraphicalInterface graphicalInterface = new GraphicalInterface();
        graphicalInterface.mainMenu(universe, null, playerCharacterList);
    }
}
