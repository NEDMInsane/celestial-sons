
package orbitalbodies;


public class Moon extends OrbitalBodies {
    private String name;
    private int size;
    private String resources;
    
    public Moon(String name){
        super(name);
        
    }
    
    public Moon(String name, int size, String resources){
        super(name);
        this. size = size;
        this.resources = resources;
        
    }
    
}
