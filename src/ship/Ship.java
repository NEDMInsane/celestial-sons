
package ship;

import java.util.HashMap;
import celestialsons.Vector3d;

/*
Ships operate in 3 different ways
Impulse - Used for near orbital body travel
Sub-Light(Harmonic Drive) - Used for intrasystem travel
Warp/Jump - Used for interstellar travel
*/
public class Ship {
    private String shipType = "";
    private HashMap<String, Integer> shipSpecialties = new HashMap<>();
    
    private int hardPoints = 0;
    private int launcherSlots = 0;
    private int turretSlots = 0;
    
    private int shieldHitpoints = 0;
    private int hullHitpoints = 0;
    private int structureHitpoints = 0;
    
    private int impulseMaxSpeed = 0;
    private int harmonicMaxSpeed = 0;
    private int jumpDriveDist = 0; //Lightyears
    
    private Vector3d velocity = new Vector3d(0.0, 0.0, 0.0);
    private Vector3d currentPosition = new Vector3d(0.0, 0.0, 0.0);
    private Vector3d radarCrossSection = new Vector3d(10.0, 10.0, 10.0);
    
    public void update(){
        currentPosition.add(velocity);
    }
    
    //private HashMap<String, Modification> shipModifications = new HashMap<>();
    
    public Ship(){
        //This is just a template and shouldnt be used.
        this.shipType = "Escape Pod";
        
        this.shieldHitpoints = 100;
        this.hullHitpoints = 200;
        this.structureHitpoints = 200;
        
        this.impulseMaxSpeed = 700;
        this.harmonicMaxSpeed = 1200;
    }
    
    public Ship(String shipType, int hardPoints, int launcherSlots, int turretSlots){
        this.shipType = shipType;
        this.hardPoints = hardPoints;
        this.launcherSlots = launcherSlots;
        this.turretSlots = turretSlots;
    }
    
    public String getShipType() { return this.shipType; }
    public HashMap<String, Integer> getShipSpecialties() { return this.shipSpecialties; }
    public int getHardpoints() { return this.hardPoints; }
    public int getLauncherSlots() { return this.launcherSlots; }
    public int getTurretSlots() { return this.turretSlots; }
    public int getShieldHitpoints() { return this.shieldHitpoints; }
    public int getHullHitpoints() { return this.hullHitpoints; }
    public int getStructureHitpoints() { return this.structureHitpoints; }
    public int getImpulseMaxSpeed() { return this.impulseMaxSpeed; }
    public int getHarmonicMaxSpeed() { return this.harmonicMaxSpeed; }
    public int getJumpDriveDist() { return this.jumpDriveDist; }
}

