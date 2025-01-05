
package orbitalbodies;


public class Planet extends OrbitalBodies {
    /**
     * Planets are a gravity well, so they will contain the asteroid belts,
     * moons, space stations.
     * 
     * The size of a planet will determine the amount of things in the gravity well
     */

    final Star star; // Need to know what gravity well this planet belongs to.


    final String name;
    final String planetType;
    final int planetSize;
    
    private AsteroidBelt[] asteroidBelts;
    private Moon[] moons;
    private SpaceStation[] spaceStations;
    
    public Planet(Star star, String name, String planetType, int planetSize, Moon[] moonList, AsteroidBelt[] beltList){
        this.star = star;
        this.name = name;
        this.planetType = planetType;
        this.planetSize = planetSize;
        this.moons = moonList;
        this.asteroidBelts = beltList;
    }
    
    public Planet(Star star, String name, String planetType, int planetSize, Moon[] moonList, int asteroidBelts){
        this.name = name;
        this.planetType = planetType;
        this.planetSize = planetSize;
        this.moons = moonList;
        this.star = star;
        System.out.printf("Generate %d belts for %s.\n", asteroidBelts, name);
    }
    
    public Planet(Star star, String name, String planetType, int planetSize, int asteroidBelts){
        this.name = name;
        this.planetType = planetType;
        this.planetSize = planetSize;
        this.star = star;
        System.out.printf("Generate %d belts for %s.\n", asteroidBelts, name);
    }
    
    public Planet(Star star, String name, String planetType, int planetSize, Moon[] moonList){
        this.star = star;
        this.name = name;
        this.planetType = planetType;
        this.planetSize = planetSize;
        this.moons = moonList;
    }
    
    public Planet(Star star, String name, String planetType, int planetSize){
        this.star = star;
        this.name = name;
        this.planetType = planetType;
        this.planetSize = planetSize;
        //If no other arguments are passed we will generate the belts, moon, stations.
    }
    
    public void generateGravityWell(){
        //TODO: Fix up this method. should be generated with planet size in mind
        this.moons = new Moon[2];
        this.asteroidBelts = new AsteroidBelt[3];
        this.spaceStations = new SpaceStation[1];
        
        for(int i = 0; i < this.moons.length; i++){
            Moon newMoon = new Moon(this.name + i);
            this.moons[i] = newMoon;
        }
        
        for(int i = 0; i < this.asteroidBelts.length; i++){
            AsteroidBelt newAsteroidBelt = new AsteroidBelt(this.name + i);
            this.asteroidBelts[i] = newAsteroidBelt;
        }
        
        for(int i = 0; i < this.spaceStations.length; i++){
            SpaceStation newSpaceStation = new SpaceStation(this.name + i);
            this.spaceStations[i] = newSpaceStation;
        }
    }
    
    public String getName(){return this.name;}
    public String getPlanetType(){return this.planetType;}
    public int getPlanetSize(){return this.planetSize;}
    public AsteroidBelt[] getAsteroidBelts(){return this.asteroidBelts;}
    public Moon[] getMoons(){return this.moons;}
    public SpaceStation[] getSpaceStation(){return this.spaceStations;}
    
}
