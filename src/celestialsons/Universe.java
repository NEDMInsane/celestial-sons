
package celestialsons;

import celestialsons.orbitalbodies.*;

public class Universe {
    private final Star[] starList;
    private final Planet[] planetList;
    private final Moon[] moonList; // Moons are orbital bodies inside the Planet classes, which are stored in the star classes...
    private final SpaceStation[] stationList;

    // Why load all these when the stars each contain their own planet list, which contain the moons...?
    public Universe(Star[] starList, Planet[] planetList, Moon[] moonList){
        this(starList, planetList, moonList, null);
    }

    public Universe(Star[] starList, Planet[] planetList, Moon[] moonList, SpaceStation[] stationList){
        this.starList = starList;
        this.planetList = planetList;
        this.moonList = moonList;
        this.stationList = stationList;
        System.out.println("Universe Created!");
    }

    public Universe(Star[] starlist){
        this.starList = starlist;
        this.planetList = null;
        this.moonList = null;
        this.stationList = null;
    }

    public Star[] getStarList(){
        return this.starList;
    }
    
    public Planet[] getPlanetList(){
        return this.planetList;
    }
    
    public Moon[] getMoonList(){
        return this.moonList;
    }

    public SpaceStation[] getStationList() {
        return this.stationList;
    }
    
    public Star findSystem(String starName){
        for(Star star : starList){
            if(star.getName().equals(starName)){
                return star;
            } 
        }
        return null;
    }
    
    public void listStars(){
        for(Star star : this.starList){
            System.out.printf("Star : %s - %s\n",star.getName(), star.getStarType());
        }
    }
    
    public void listPlanets(){
        for(Planet planet : this.planetList){
            System.out.printf("Planet : %s\n", planet.getName());
        }
    }
    
    public void listMoons(){
        for(Moon moon : this.moonList){
            System.out.printf("Moon : %s\n", moon.getName());
        }
    }

    public void listStations() {
        if (this.stationList == null) {
            return;
        }
        for (SpaceStation station : this.stationList) {
            System.out.printf("Station : %s\n", station.getName());
        }
    }
}
