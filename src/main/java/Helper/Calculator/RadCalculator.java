package Helper.Calculator;

import DataStructures.Coordinate;

/**
 * This Class handles calculations for generating radii about nodes
 * Takes an int as a perameter for the size of the radius in terms of time to get to another node from a start point
 */
public class RadCalculator {
    int radius;

    public RadCalculator(int radius) {
        this.radius = radius;
    }

    public boolean checkIfWalkable(Coordinate start, Coordinate end) {
        WalkingCalculator wc = new WalkingCalculator();
        double timeToCheck = wc.calculateTime(start, end);
        if (radius < timeToCheck) {
            return false;
        } else return true;
    }
}
