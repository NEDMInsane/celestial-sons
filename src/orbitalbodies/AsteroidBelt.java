
package orbitalbodies;


public class AsteroidBelt extends OrbitalBodies {
    /**
     * Asteroid belts contain a main type of mineral,
     * but has chances to spawn other types as well.
     */
    
    final String asteroidType;
    private String name;
    
    public AsteroidBelt(){
        super(null);
        this.asteroidType = "";
    }
    
    public AsteroidBelt(String name){
        super(name);
        this.asteroidType = "Titanium";
    }
    
    public AsteroidBelt(String name, String asteroidType){
        super(name);
        this.asteroidType = asteroidType;
    }
    
    public void buildAsteroidField(){
        //TODO: Create an asteroid feild made mostly of the asteroidType
    }
    
    
    
    public String getAsteroidType(){return this.asteroidType;}
}
