
package celestialsons;

import orbitalbodies.*;

public class Universe {
    private Star[] starList;
    private Planet[] planetList;
    private Moon[] moonList; // Moons are orbital bodies inside the Planet classes, which are stored in the star classes...

    // Why load all these when the stars each contain their own planet list, which contain the moons...?
    public Universe(Star[] starList, Planet[] planetList, Moon[] moonList){
        this.starList = starList;
        this.planetList = planetList;
        this.moonList = moonList;
        System.out.println("Universe Created!");
    }

    public Universe(Star[] starlist){
        this.starList = starlist;
        this.planetList = null;
        this.moonList = null;
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
}
