
package celestialsons;

import orbitalbodies.*;

public class Universe {
    private Star[] starList;
    private Planet[] planetList;
    private Moon[] moonList;
    
    public Universe(Star[] starList, Planet[] planetList, Moon[] moonList){
        this.starList = starList;
        this.planetList = planetList;
        this.moonList = moonList;
        System.out.println("Universe Created!");
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
