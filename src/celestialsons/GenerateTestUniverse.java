package celestialsons;


import orbitalbodies.Planet;
import orbitalbodies.Star;
//Using EveOnline names, kill me.

public class GenerateTestUniverse {
    public static Star createSystem(String name, int planetCount){
        Star star = new Star(name);
        Planet[] planetList = new Planet[planetCount];
        for (int i = 0; i < planetList.length ; i++) {
            Planet planet = new Planet(star, String.format("%s-Planet-%d", name, i), "Any", 1, null, null);
            planet.setPosition(new DimensionalPosition(i, 0.0, 0.0)); // just put it somewhere on the grid.
            planetList[i] = planet;
        }
        star.setPlanets(planetList);
        return star;
    }
    public static void main(String[] args) {
/*        Star jita = new Star("Jita");
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
        sobasekai.setPlanets(sobasekiPlanetList);*/
        System.out.println("Generating Test universe...");
        Star[] starList = new Star[3];
        starList[0] = createSystem("Jita", 5);
        starList[1] = createSystem("New Caldari", 5);
        starList[2] = createSystem("Sobasekai", 5);

        PlayerCharacter[] playerCharacterList = new PlayerCharacter[1];
        playerCharacterList[0] = new PlayerCharacter("NEDMInsane", 100);
        playerCharacterList[0].setSystem(starList[0]);
        playerCharacterList[0].setLocation(new DimensionalPosition(0.5, 0.0, 0.0)); // We don't want to be inside the star

        Universe universe = new Universe(starList);
        System.out.println(universe);

        GraphicalInterface graphicalInterface = new GraphicalInterface();
        graphicalInterface.mainMenu(universe, null, playerCharacterList);
    }
}
