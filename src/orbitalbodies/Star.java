
package orbitalbodies;


public class Star extends OrbitalBodies {
    final String name;
    final String starType;
    final int starSize;
    
    private Planet[] planets;
    
    public Star(String name){
        this.name = name;
        this.starType = null;
        this.starSize = 0;
        //Basiclly a blackhole lol
    }
    
    public Star(String name, String starType, int starSize){
        this.name = name;
        this.starType = starType;
        this.starSize = starSize;
        //if no planets are passed, we will generate them.
    }
    
    public Star(String name, String starType, int starSize, Planet[] planetList){
        this.name = name;
        this.starType = starType;
        this.starSize = starSize;
        this.planets = planetList;
    }
    
    public void generateGravityWell(){
        this.planets = new Planet[3];
        
        for(int i = 0; i < this.planets.length; i++){
            Planet newPlanet = new Planet(this.name + i, "Water", 2);
            this.planets[i] = newPlanet;
        }
    }
    
    
    
    public String getName(){
        return this.name;
    }
    public String getStarType(){
        return this.starType;
    }
    public int getStarSize(){
        return this.starSize;
    }
}
