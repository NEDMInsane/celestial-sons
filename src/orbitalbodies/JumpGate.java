package orbitalbodies;

import celestialsons.DimensionalPosition;

public class JumpGate extends OrbitalBodies {
    public JumpGate(String name) {
        super(name);
        System.out.println("JumpGate created, requires position. JumpGate name: " + name);
    }

    public JumpGate(Star star, DimensionalPosition gridPosition) {
        super(String.format("%s, Jump Gate", star));
        super.setPosition(gridPosition);
    }
}
