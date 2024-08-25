
package orbitalbodies;


public class Moon extends OrbitalBodies {
    private String name;
    private int size;
    private String resources;
    
    public Moon(String name){
        this.name = name;
        
    }
    
    public Moon(String name, int size, String resources){
        this.name = name;
        this. size = size;
        this.resources = resources;
        
    }
    
    public String getName(){
        return this.name;
    }
}
