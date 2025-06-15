package Backend.Algorithms;

import DataStructures.Coordinate;
import DataStructures.Node;

import java.util.*;

public class AStarHeatmap {
    private AStar aStar;
    private Map<Coordinate, Integer> tripTimeMap = new HashMap<>();
    ;
    private List<Coordinate> destinations;
    private Node origin;

    public AStarHeatmap(List<Coordinate> destinations, Node origin) {
        this.destinations = destinations;
        this.origin = origin;
    }

    public Map<Coordinate, Integer> calculateHeatmap() {
        for (Coordinate coord : destinations) {
            Node destination = new Node(coord, 0);
            aStar = new AStar(origin, destination, origin.getTime());
            tripTimeMap.put(coord, aStar.getTripTime());
        }
        return tripTimeMap;
    }
}