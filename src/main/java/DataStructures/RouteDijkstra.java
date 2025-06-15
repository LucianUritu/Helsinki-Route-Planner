package DataStructures;

import Database.Queries.CoordinateQuery;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RouteDijkstra {
    private List<DijkstraEdge> path;
    private int tripTime;

    public RouteDijkstra(List<DijkstraEdge> path, int tripTime) {
        this.path = path;
        this.tripTime = tripTime;
    }

    public List<String> printRouteGUI() {

        List<String> routeInstructions = new ArrayList<>();
        routeInstructions.add("Journey time: " + tripTime + " minutes");
        CoordinateQuery cq = new CoordinateQuery();

        DijkstraEdge startEdge = path.get(0);
        String startStopId = startEdge.getSourceId();
        Coordinate startingCoordinate = cq.getCoordinateByStopId(startStopId);
        int arrivalTime;
        String transport;
        if (startEdge.isWalk()) {
            arrivalTime = startEdge.getTravelTime();
            transport = "Walking";
        } else {
            arrivalTime = startEdge.getArrivalTime();
            transport = "taking public transport";
        }
        routeInstructions.add(String.format("\nOrigin: %.6f, %.6f Starting time: %s stop number %d Transport type: %s",
                startingCoordinate.getLatitude(),
                startingCoordinate.getLongitude(),
                arrivalTime,
                0, transport
                /*startStopId.getTransportType()*/));

        for (int i = 1; i < path.size() - 1; i++) {
            DijkstraEdge edge = path.get(i);
            String fromStopId = edge.getSourceId();
            Coordinate coordinate = cq.getCoordinateByStopId(fromStopId);

            if (edge.isWalk()) {
                arrivalTime = startEdge.getTravelTime();
                transport = "Walking";
            } else {
                arrivalTime = edge.getArrivalTime();
                transport = "taking public transport";
            }

            routeInstructions.add(String.format("\nIntermediary node: %.6f, %.6f Arrival Time: %s stop number: %d Transport type: %s",
                    coordinate.getLatitude(),
                    coordinate.getLongitude(),
                    arrivalTime,
                    fromStopId,
                    transport));
        }

        DijkstraEdge destinationEdge = path.getLast();
        String destinationStopId = destinationEdge.getDestId();
        Coordinate destinationCoordinate = cq.getCoordinateByStopId(destinationStopId);

        if (destinationEdge.isWalk()) {
            arrivalTime = destinationEdge.getTravelTime();
            transport = "Walking";
        } else {
            arrivalTime = destinationEdge.getArrivalTime();
            transport = "taking public transport";
        }

        routeInstructions.add(String.format("\nDestination: %.6f, %.6f Arrival time: %s stop number %d Transport type: %s",
                destinationCoordinate.getLatitude(),
                destinationCoordinate.getLongitude(),
                arrivalTime,
                destinationStopId,
                transport
        ));

        return routeInstructions;
    }
}