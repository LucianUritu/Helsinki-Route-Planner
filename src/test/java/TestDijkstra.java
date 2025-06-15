import Backend.Algorithms.Dijkstra;
import Backend.RoutingEngine;
import DataStructures.Coordinate;
import DataStructures.DijkstraEdge;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * We are using google maps as the reference for what an optimal (or at least close to optimal) route could look and how long it would take
 * we take the same trips that we tested Astar with, but we tighten the lower and upper time bounds as Dijkstra shoudl retunr a closer answer to the optimal
 */
public class TestDijkstra {
    private Dijkstra algorithm;
    private Coordinate stratingCoordinate;
    private Coordinate destinationCoordinate;
    private RoutingEngine engine = new RoutingEngine();

    public TestDijkstra() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWalkJourney() {
        // https://maps.app.goo.gl/gZBwBKPA8rRmiyVf9
        stratingCoordinate = new Coordinate(60.164750, 24.933773);
        destinationCoordinate = new Coordinate(60.166163, 24.937927);
        algorithm = new Dijkstra(stratingCoordinate, destinationCoordinate, 480);
        int fastestTime = algorithm.printTime(stratingCoordinate, destinationCoordinate, 480);

        assertTrue(fastestTime >= 2 && fastestTime <= 5, "Walking time should be close to 4 minutes, actual time was: " + fastestTime);

        List<DijkstraEdge> expectedPath = algorithm.retracePathDijkstra();
        for (DijkstraEdge edge : expectedPath) {
            System.out.println("Stop IDs along the path:");
            System.out.println("From stop " + edge.getSourceId() + " to stop " + edge.getDestId() +
                    (edge.isWalk() ? " (walk)" : " (transport)") +
                    ", travel time: " + (edge.isWalk() ? edge.getTravelTime() : edge.getArrivalTime()));
        }
        assertEquals(algorithm.retracePathDijkstra(), expectedPath, "List of edges containing the path are different. For the case when we're walking it should only contain the starting and destination nodes");
    }

    @Test
    public void testShortJourney() {
        // https://maps.app.goo.gl/JURL1q8rPDP5b1yz7
        stratingCoordinate = new Coordinate(60.168024, 24.941472);
        destinationCoordinate = new Coordinate(60.158043, 24.940887);
        algorithm = new Dijkstra(stratingCoordinate, destinationCoordinate, 600);
        int fastestTime = algorithm.printTime(stratingCoordinate, destinationCoordinate, 600);

        assertTrue(fastestTime >= 10 && fastestTime <= 15, "Journey time should be close to 12 minutes, actual time was: " + fastestTime);
    }

    @Test
    public void testMediumJourney() {
        // https://maps.app.goo.gl/VYAPnGZZ3JSG2jCcA
        stratingCoordinate = new Coordinate(60.158541, 24.933045);
        destinationCoordinate = new Coordinate(60.174084, 24.954763);
        algorithm = new Dijkstra(stratingCoordinate, destinationCoordinate, 485);
        int fastestTime = algorithm.printTime(stratingCoordinate, destinationCoordinate, 485);

        assertTrue(fastestTime >= 15 && fastestTime <= 25, "Journey time should be close to 22 minutes, actual time was: " + fastestTime);
    }

    @Test
    public void testLongJourney() {
        // https://maps.app.goo.gl/4FAT8YdMTrc1Hw7h8
        stratingCoordinate = new Coordinate(60.186657, 24.962632);
        destinationCoordinate = new Coordinate(60.151571, 24.926431);
        algorithm = new Dijkstra(stratingCoordinate, destinationCoordinate, 660);
        int fastestTime = algorithm.printTime(stratingCoordinate, destinationCoordinate, 660);

        assertTrue(fastestTime >= 20 && fastestTime <= 45, "Journey time should be close to 43 minutes, actual time was: " + fastestTime);
    }

}
