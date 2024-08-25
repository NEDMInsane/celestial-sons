
package orbitalbodies;

import celestialsons.Vector3d;

public class OrbitalBodies {
    // PLANETS MOVE(maybe)!?
    //private Vector3d velocity = new Vector3d(0.0, 0.0, 0.0);
    private Vector3d startingPosition = new Vector3d(0.0, 0.0, 0.0);
    private Vector3d position = new Vector3d(0.0, 0.0, 0.0);
    
    public Vector3d getStartingPosition(){
        return this.startingPosition;
    }
    
    public void setStartingPosition(Vector3d position){
        this.startingPosition = position;
    }
    
    public Vector3d getPosition(){
        return this.position;
    }
    
    public void setPosition(Vector3d position){
        this.position = position;
    }
    
}
