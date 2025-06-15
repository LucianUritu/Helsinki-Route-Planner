package Backend.Algorithms;

import DataStructures.Coordinate;
import DataStructures.DijkstraEdge;
import Database.DijkstraLoader;
import Database.Queries.CoordinateQuery;
import Helper.Calculator.WalkingCalculator;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import java.util.*;

public class DijkstraHeatmap {
    public Map<Waypoint, Integer> coordinatesEarliestArrival;
    DijkstraLoader dijkstraLoader = new DijkstraLoader();
    Map<String, List<DijkstraEdge>> scheduleMap = dijkstraLoader.DKSloader();

    public DijkstraHeatmap(Coordinate start, int startTime) {
        CoordinateQuery coordinateQuery = new CoordinateQuery();
        this.scheduleMap = dijkstraLoader.addStartStop(this.scheduleMap, start);
        Dijkstra alg = new Dijkstra(start, startTime);

        Map<String, Integer> earliestArrival = alg.computeEarliestArrivalTimes("Start", startTime);
        this.coordinatesEarliestArrival = new HashMap<>();
        for (Map.Entry<String, Integer> elem : earliestArrival.entrySet()) {
            if (elem.getKey().equals("Start")) {
                continue;
            }
            double tempLat = coordinateQuery.getCoordinateByStopId(elem.getKey()).getLatitude();
            double tempLon = coordinateQuery.getCoordinateByStopId(elem.getKey()).getLongitude();
            GeoPosition tempGeo = new GeoPosition(tempLat, tempLon);
            Waypoint tempWaypoint = new DefaultWaypoint(tempGeo);
            this.coordinatesEarliestArrival.put(tempWaypoint, elem.getValue() - startTime);
        }
    }

    public static List<String> getReachableStops(Map<String, Integer> earliestArrival, int startTime, int timeLimit) {
        List<String> reachable = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : earliestArrival.entrySet()) {
            if (entry.getValue() != Integer.MAX_VALUE && (entry.getValue() - startTime) <= timeLimit) {
                reachable.add(entry.getKey());
            }
        }
        return reachable;
    }

    public void eliminateClosestStop(Coordinate userCoord) {
        WalkingCalculator query = new WalkingCalculator();
        GeoPosition input = new GeoPosition(userCoord.getLatitude(), userCoord.getLongitude());
        Waypoint closest = null;
        double smallestDistance = Double.MAX_VALUE;
        for (Waypoint id : coordinatesEarliestArrival.keySet()) {
            GeoPosition stop = id.getPosition();
            double dist = query.calculateDistance(new Coordinate(input.getLatitude(), input.getLongitude()), new Coordinate(stop.getLatitude(), stop.getLongitude()));
            if (dist < smallestDistance) {
                smallestDistance = dist;
                closest = id;
            }
        }
        if (closest != null) {
            coordinatesEarliestArrival.remove(closest);
        }
    }

    public Waypoint closestStop(Coordinate userCoord) {
        WalkingCalculator query = new WalkingCalculator();
        GeoPosition input = new GeoPosition(userCoord.getLatitude(), userCoord.getLongitude());
        Waypoint closest = null;
        double smallestDistance = Double.MAX_VALUE;
        for (Waypoint id : coordinatesEarliestArrival.keySet()) {
            GeoPosition stop = id.getPosition();
            double dist = query.calculateDistance(new Coordinate(input.getLatitude(), input.getLongitude()), new Coordinate(stop.getLatitude(), stop.getLongitude()));
            if (dist < smallestDistance) {
                smallestDistance = dist;
                closest = id;
            }
        }
        if (closest != null) {
            coordinatesEarliestArrival.remove(closest);
        }
        return closest;
    }

    public void eliminateExactStop(Waypoint selectedStop) {
        coordinatesEarliestArrival.remove(selectedStop);
    }
}
