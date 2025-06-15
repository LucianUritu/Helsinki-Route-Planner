package DataStructures;

import Database.Queries.*;
import Helper.Calculator.RadCalculator;
import Helper.Calculator.WalkingCalculator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Node {
    private String stopId;
    private String tripId;
    private int time;
    private String dateTime;
    private List<Edge> edges;
    private Node parent;
    private int gCost;
    private int hCost;
    private int fCost;
    private List<Node> neighbors;
    private Coordinate coordinate;
    private CoordinateQuery coordinateQuery = new CoordinateQuery();
    private WalkingCalculator wCal = new WalkingCalculator();
    private RadCalculator rCal = new RadCalculator(30);
    private SequenceQuery sequenceQuery = new SequenceQuery();
    private TripIdQuery tripIdQuery = new TripIdQuery();
    private WalkToStopQuery walkToStopQuery = new WalkToStopQuery();
    private TransportTypeQuery transportTypeQuery = new TransportTypeQuery();
    private String transportType;

    public Node(String stopId, String tripId, int time) {
        this.stopId = stopId;
        this.tripId = tripId;
        this.time = time;
        this.edges = new ArrayList<>();
        this.coordinate = coordinateQuery.getCoordinateByStopId(this.stopId);
        this.gCost = Integer.MAX_VALUE;
        this.transportType = transportTypeQuery.getTrasnportType(this);
    }

    /**
     * Special constructor in case we have to create a node for the starting coordinate or destination coordinate
     *
     * @param coordinate
     * @param time
     */
    public Node(Coordinate coordinate, int time) {
        this.stopId = Stop.VIRTUAL_STOP;
        this.tripId = "0";
        this.time = time;
        this.coordinate = coordinate;
        this.edges = new ArrayList<>();
        this.transportType = "walk";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node other = (Node) o;
        return stopId == other.stopId
                && time == other.time
                && Objects.equals(tripId, other.tripId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId, tripId, time);
    }

    public List<Node> generateNeighbors(int maxArrival) {
        Set<Node> neighborSet = new LinkedHashSet<>();

        if (!this.stopId.equals(Stop.VIRTUAL_STOP)) {
            List<String> tripIds = tripIdQuery.getTripId(this.stopId, this.time, 30);
            for (String tripId : tripIds) {
                Stop nextStop = sequenceQuery.getNextImmediateNeighbor(this.stopId, tripId);
                if (nextStop == null)
                    continue;

                int arrival = timeToMinutes(nextStop.getArrivalTime());
                if (arrival <= this.time || arrival > this.time + maxArrival) continue;

                Node neighbor = new Node(nextStop.getStopId(), tripId, arrival);
                neighbor.setCoordinate(coordinateQuery.getCoordinateByStopId(nextStop.getStopId()));
                neighborSet.add(neighbor);  // duplicates removed by set
            }
        }

        List<Node> walkables = walkToStopQuery.queryWalkStops(this);
        for (Node walkNode : walkables) {
            String wStop = walkNode.getStopId();
            int wTime = walkNode.getTime();
            if (wStop.equals(this.stopId) || wTime <= this.time || wTime > this.time + maxArrival) {
                continue;
            }
            walkNode.setTransportType("walk");
            neighborSet.add(walkNode);
        }

        List<Node> neighbors = new ArrayList<>(neighborSet);
        // System.err.println("Total neighbors: " + neighbors.size());
        return neighbors;
    }

    public void setGCost(int gCost) {
        this.gCost = gCost;
    }

    public int getHCost() {
        return this.hCost;
    }

    public void setHCost(Node destination) {
        this.hCost = calculateHCost(this.coordinate, destination.getCoordinate());
    }

    public void addNeighbor(Node node) {
        if (this.neighbors == null) {
            neighbors = new ArrayList<>();
        }
        this.neighbors.add(node);
    }

    //CALCULATORS
    //Currently I've set our heuristic is set to an as the crow flies walking distance, needs some fine-tuning keep in mind
    public int calculateHCost(Coordinate currentNodeCoord, Coordinate destinationNodeCoord) {
        return (wCal.calculateTime(currentNodeCoord, destinationNodeCoord));
    }

    public int calculateFCost() {
        return gCost + hCost;
    }

    public int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    private String minuteToString(int minutes) {
        String s = "";
        String hours = String.valueOf(minutes / 60);
        String newMinutes = String.valueOf(minutes % 60);
        if (Integer.valueOf(hours) < 10) hours = "0" + hours;
        if (Integer.valueOf(newMinutes) < 10) newMinutes = "0" + newMinutes;

        s = hours + ":" + newMinutes + ":" + "00";
        return s;
    }

    private String minuteToClient(int minutes) {
        String s = "";
        String hours = String.valueOf(minutes / 60);
        String newMinutes = String.valueOf(minutes % 60);
        if (Integer.valueOf(hours) < 10) hours = "0" + hours;
        if (Integer.valueOf(newMinutes) < 10) newMinutes = "0" + newMinutes;

        s = hours + ":" + newMinutes;
        return s;
    }

    public String getDateTime() {
        return minuteToClient(this.time);
    }

    public void setDateTime(int time) {
        this.dateTime = minuteToClient(time);
    }

    public String getRouteNumber() {
        if (tripId != null && !tripId.equals("0")) {
            return tripId.split("_")[0];
        }
        return null;
    }
}
