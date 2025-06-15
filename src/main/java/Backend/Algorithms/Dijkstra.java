package Backend.Algorithms;

import DataStructures.*;
import Database.DijkstraLoader;
import Database.Queries.CoordinateQuery;
import Helper.Calculator.WalkingCalculator;
import org.jxmapviewer.viewer.Waypoint;

import java.util.*;

public class Dijkstra {

    private static Map<String, DijkstraEdge> stopArrivalEdgeMap;
    public Map<Waypoint, Integer> coordinatesEarliestArrival;
    DijkstraLoader dijkstraLoader = new DijkstraLoader();
    Map<String, List<DijkstraEdge>> scheduleMap = dijkstraLoader.DKSloader();

    public Dijkstra(Coordinate start, Coordinate dest, int startTime) {
        this.scheduleMap = dijkstraLoader.addStartStop(this.scheduleMap, start);
        this.scheduleMap = dijkstraLoader.addDestinationStop(this.scheduleMap, dest);
        this.scheduleMap = directWalking(this.scheduleMap, start, dest);
        computeEarliestArrivalTimes("Start", startTime);
    }

    public Dijkstra(Coordinate start, int startTime) {
        this.scheduleMap = dijkstraLoader.addStartStop(this.scheduleMap, start);
        computeEarliestArrivalTimes("Start", startTime);
    }

    public Map<String, Integer> computeEarliestArrivalTimes(String startStop, int startTime) {
        // all stops mentioned in the schedule
        Set<String> allStops = new HashSet<>();
        for (String stop : this.scheduleMap.keySet()) {
            allStops.add(stop);
            for (DijkstraEdge conn : this.scheduleMap.get(stop)) {
                allStops.add(conn.getDestId());
            }
        }

        Map<String, Integer> earliestArrival = new HashMap<>();
        for (String stop : allStops) {
            earliestArrival.put(stop, Integer.MAX_VALUE);
        }
        earliestArrival.put(startStop, startTime);

        // Priority queue to pick the stop with the smallest current arrival time first
        PriorityQueue<Stop> queue = new PriorityQueue<>();
        Map<String, DijkstraEdge> stopArrivalEdgeMap_temp = new HashMap<>();
        queue.offer(new Stop(startStop, String.valueOf(startTime)));

        while (!queue.isEmpty()) {
            Stop current = queue.poll();
            String currentStop = current.getStopId();
            int currentArrivalTime = Integer.valueOf(current.getArrivalTime());

            if (currentArrivalTime > earliestArrival.get(currentStop)) {
                continue;
            }

            List<DijkstraEdge> connections = this.scheduleMap.get(currentStop);
            if (connections != null) {
                for (DijkstraEdge conn : connections) {
                    if (!conn.isWalk()) {
                        if (conn.getDepartureTime() >= currentArrivalTime) {
                            int candidateArrival = conn.getArrivalTime(); // arrival time if you take this bus
                            //if (conn.getDestId() == 1040117){
                            //  System.out.println("I am a bus " + conn.getArrivalTime() + "  " + conn.getSourceId() + "  " + conn.getDepartureTime());
                            //}
                            if (candidateArrival < earliestArrival.get(conn.getDestId())) {
                                earliestArrival.put(conn.getDestId(), candidateArrival);
                                queue.offer(new Stop(conn.getDestId(), String.valueOf(candidateArrival)));

                                stopArrivalEdgeMap_temp.put(conn.getDestId(), conn);
                            }
                        }
                    } else {
                        //    System.out.println("walking edge curent stop="+ conn.getSourceId()+ "target="+ conn.getDestId()+ "travelTime" + conn.getTravelTime());
                        int candidateArrival = currentArrivalTime + conn.getTravelTime();
                        //dubugging, ignore
                    /*   if (conn.getDestId() == 1040117){
                            System.out.println(candidateArrival + "  " + conn.getSourceId() + "  " + currentArrivalTime);
                        }*/
                        if (candidateArrival < earliestArrival.get(conn.getDestId())) {
                            earliestArrival.put(conn.getDestId(), candidateArrival);
                            queue.offer(new Stop(conn.getDestId(), String.valueOf(candidateArrival)));

                            stopArrivalEdgeMap_temp.put(conn.getDestId(), conn);
                        }
                    }
                }
            }
        }
        stopArrivalEdgeMap = stopArrivalEdgeMap_temp;
        return earliestArrival;
    }

    public List<DijkstraEdge> retracePathDijkstra() {
        List<DijkstraEdge> path = new ArrayList<>();
        String currentId = "Destination";

        while (true) {
            if (currentId.equals("Start")) {
                break;
            }
            DijkstraEdge tempPath = stopArrivalEdgeMap.get(currentId);
            path.add(tempPath);

            currentId = tempPath.getSourceId();
        }

        return path;
    }

    public int printTime(Coordinate start, Coordinate end, int startTime) {
        this.scheduleMap = dijkstraLoader.addStartStop(this.scheduleMap, start);
        this.scheduleMap = dijkstraLoader.addDestinationStop(this.scheduleMap, end);

        Map<String, Integer> earliestArrival = computeEarliestArrivalTimes("Start", startTime);
        int arrivalTime = earliestArrival.getOrDefault("Destination", Integer.MAX_VALUE);
        if (arrivalTime == Integer.MAX_VALUE) {
            // no route was found considerin the walking limit between stops of 60 min
            return -1;
        }
        return arrivalTime - startTime;
    }

    public Route getRoute(List<DijkstraEdge> dijkstraRoute, int arrival_time, Coordinate startCoord, Coordinate destCoord, int startTime) {
        CoordinateQuery coordinateQuery = new CoordinateQuery();

        if (dijkstraRoute == null || dijkstraRoute.isEmpty()) {
            return new Route(Collections.emptyList(), 0);
        }
        // I chekced on stack and it says we can use Collections.reverse() without clone()
        Collections.reverse(dijkstraRoute);

        int currentTime = startTime;
        List<Node> nodePath = new LinkedList<>();

        Node startNode = new Node(startCoord, startTime);
        nodePath.add(startNode);

        for (DijkstraEdge edge : dijkstraRoute) {
            String destId = edge.getDestId();

            if (edge.isWalk()) {
                int travel = edge.getTravelTime();
                currentTime += travel;
                Coordinate nextCoord;

                if (destId.equals("Destination")) {
                    // for the final walk to dest we use the destCoord that has as id "Destination" and we always make it a walking edge!
                    nextCoord = destCoord;
                } else {
                    nextCoord = coordinateQuery.getCoordinateByStopId(destId);
                }

                Node walkNode = new Node(nextCoord, currentTime);
                nodePath.add(walkNode);
            } else {
                int depart = edge.getDepartureTime();
                int arrive = edge.getArrivalTime();
                int travel = arrive - depart;
                currentTime += travel;

                Node rideNode = new Node(destId, edge.getRouteId(), currentTime);
                nodePath.add(rideNode);
            }
        }
        return new Route(nodePath, arrival_time);
    }

    public Map<String, List<DijkstraEdge>> directWalking(Map<String, List<DijkstraEdge>> scheduleMap, Coordinate start, Coordinate end) {

        WalkingCalculator calc = new WalkingCalculator();
        int totwalkTime = calc.calculateTime(start, end);
        DijkstraEdge directWalk = new DijkstraEdge("Start", "Destination", totwalkTime, 0, 1);
        List<DijkstraEdge> completeEdge = this.scheduleMap.getOrDefault("Start", new ArrayList<>());
        completeEdge.add(directWalk);
        this.scheduleMap.put("Start", completeEdge);

        return this.scheduleMap;
    }

}