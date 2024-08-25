/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package orbitalbodies;


public class AsteroidBelt extends OrbitalBodies {
    /**
     * Asteroid belts contain a main type of mineral,
     * but has chances to spawn other types as well.
     */
    
    final String asteroidType;
    private String name;
    
    public AsteroidBelt(){
        this.asteroidType = "";
    }
    
    public AsteroidBelt(String name){
        this.name = name;
        this.asteroidType = "Titanium";
    }
    
    public AsteroidBelt(String name, String asteroidType){
        this.name = name;
        this.asteroidType = asteroidType;
    }
    
    public void buildAsteroidField(){
        //TODO: Create an asteroid feild made mostly of the asteroidType
    }
    
    
    
    public String getAsteroidType(){return this.asteroidType;}
}
