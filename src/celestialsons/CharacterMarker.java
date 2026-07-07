package celestialsons;

public class CharacterMarker {
    private String name;
    private DimensionalPosition position;
    private boolean transponderActive;
    private String flightPlanLabel;

    public CharacterMarker(String name, DimensionalPosition position, boolean transponderActive,
                              String flightPlanLabel) {
        this.name = name;
        this.position = position;
        this.transponderActive = transponderActive;
        this.flightPlanLabel = flightPlanLabel;
    }

    public String getName() {
        return name;
    }

    public DimensionalPosition getPosition() {
        return this.position;
    }

    public boolean isTransponderActive() {
        return transponderActive;
    }

    public String getFlightPlanLabel() {
        return flightPlanLabel;
    }
}
