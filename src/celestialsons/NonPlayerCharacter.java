
package celestialsons;

import java.util.Arrays;

public class NonPlayerCharacter {
    private String name;
    private int level;
    private DimensionalPosition currentLocation = new DimensionalPosition(0, 0, 0);
    private boolean transponderActive = false;
    private DimensionalPosition[] flightPlan = new DimensionalPosition[0];
    private String flightPlanLabel = "No filed flight plan";
    
    public NonPlayerCharacter(){
        this.name = "Player1";
        this.level = 0;
    }
    
    public NonPlayerCharacter(String name){
        this.name = name;
        this.level = 0;
    }
    
    public NonPlayerCharacter(String name, int level){
        this.name = name;
        this.level = level;
    }    
    
    public void setLevel(int level){this.level = level;}
    public int getLevel(){return this.level;}
    public void setName(String name){this.name = name;}   
    public String getName(){return this.name;}
    public void setCurrentLocation(DimensionalPosition currentLocation){this.currentLocation = currentLocation;}
    public DimensionalPosition getCurrentLocation(){return this.currentLocation;}
    public void setTransponderActive(boolean active){this.transponderActive = active;}
    public boolean isTransponderActive(){return this.transponderActive;}
    public void setFlightPlan(DimensionalPosition[] flightPlan){
        this.flightPlan = flightPlan == null ? new DimensionalPosition[0] : Arrays.copyOf(flightPlan, flightPlan.length);
    }
    public DimensionalPosition[] getFlightPlan(){return Arrays.copyOf(this.flightPlan, this.flightPlan.length);}
    public void setFlightPlanLabel(String flightPlanLabel){this.flightPlanLabel = flightPlanLabel;}
    public String getFlightPlanLabel(){return this.flightPlanLabel;}
}
