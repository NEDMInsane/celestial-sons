package celestialsons;

public class FlightPlanMarker {
    private String ownerName;
    private String label;
    private DimensionalPosition[] waypoints;

    public FlightPlanMarker(String ownerName, String label, DimensionalPosition[] waypoints) {
        this.ownerName = ownerName;
        this.label = label;
        this.waypoints = waypoints;
    }

    public DimensionalPosition[] getWaypoints() {
        return waypoints;
    }

    public String getLabel() {
        return this.label;
    }

    public char[] getOwnerName() {
        return this.ownerName.toCharArray();
    }
}
