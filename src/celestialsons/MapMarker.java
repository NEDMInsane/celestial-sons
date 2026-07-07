package celestialsons;


public class MapMarker {
    private String name;
    private String type;
    private DimensionalPosition position;

    public MapMarker(String name, String type, DimensionalPosition position) {
        this.name = name;
        this.type = type;
        this.position = position;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public DimensionalPosition getPosition() {
        return this.position;
    }
}
