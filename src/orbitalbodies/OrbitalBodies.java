package orbitalbodies;

import celestialsons.DimensionalPosition;

public class OrbitalBodies {
    // PLANETS MOVE(maybe)!?
    //private DimensionalPosition velocity = new DimensionalPosition(0.0, 0.0, 0.0);
    private DimensionalPosition startingPosition = new DimensionalPosition(0.0, 0.0, 0.0);
    private DimensionalPosition position = new DimensionalPosition(0.0, 0.0, 0.0);



    public DimensionalPosition getStartingPosition(){
        return this.startingPosition;
    }
    public void setStartingPosition(DimensionalPosition position){
        this.startingPosition = position;
    }
    public DimensionalPosition getPosition(){
        return this.position;
    }
    public void setPosition(DimensionalPosition position){
        this.position = position;
    }
    final String name;

    public OrbitalBodies(String name) {
        this.name = name;
    }

    public String getName() { return this.name; }
    public String toString(){ return this.name; }
}
