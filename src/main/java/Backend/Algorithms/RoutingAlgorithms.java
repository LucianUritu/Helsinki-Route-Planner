package Backend.Algorithms;

import DataStructures.Coordinate;
import DataStructures.Route;

public interface RoutingAlgorithms {
    Route FastestPath(Coordinate start, Coordinate end, int startTime);
} 