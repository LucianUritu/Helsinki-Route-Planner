import Backend.Algorithms.AStar;
import Backend.RoutingEngine;
import DataStructures.Coordinate;
import DataStructures.Node;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * We are using google maps as the reference for what an optimal (or at least close to optimal) route could look and how long it would take
 */
public class TestAStar {
    private AStar algorithm;
    private Node startingNode;
    private Node destinationNode;
    private RoutingEngine engine = new RoutingEngine();

    public TestAStar() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testWalkJourney() {
        // https://maps.app.goo.gl/gZBwBKPA8rRmiyVf9
        startingNode = new Node(new Coordinate(60.164750, 24.933773), 480);
        destinationNode = new Node(new Coordinate(60.166163, 24.937927), 0);
        algorithm = new AStar(startingNode, destinationNode, 480);
        int fastestTime = algorithm.getTripTime();

        assertTrue(fastestTime >= 1 && fastestTime <= 20, "Walking time should be close to 4 minutes, actual time was: " + fastestTime);
    }

    @Test
    public void testShortJourney() {
        // https://maps.app.goo.gl/JURL1q8rPDP5b1yz7
        startingNode = new Node(new Coordinate(60.168024, 24.941472), 600);
        destinationNode = new Node(new Coordinate(60.158043, 24.940887), 0);
        algorithm = new AStar(startingNode, destinationNode, 600);
        int fastestTime = algorithm.getTripTime();

        assertTrue(fastestTime >= 4 && fastestTime <= 18, "Journey time should be close to 12 minutes, actual time was: " + fastestTime);
    }

    @Test
    public void testMediumJourney() {
        // https://maps.app.goo.gl/VYAPnGZZ3JSG2jCcA
        startingNode = new Node(new Coordinate(60.158541, 24.933045), 485);
        destinationNode = new Node(new Coordinate(60.174084, 24.954763), 0);
        algorithm = new AStar(startingNode, destinationNode, 485);
        int fastestTime = algorithm.getTripTime();

        assertTrue(fastestTime >= 14 && fastestTime <= 32, "Journey time should be close to 22 minutes, actual time was: " + fastestTime);
    }

    @Test
    public void testLongJourney() {
        // https://maps.app.goo.gl/4FAT8YdMTrc1Hw7h8
        startingNode = new Node(new Coordinate(60.186657, 24.962632), 660);
        destinationNode = new Node(new Coordinate(60.151571, 24.926431), 0);
        algorithm = new AStar(startingNode, destinationNode, 660);
        int fastestTime = algorithm.getTripTime();

        assertTrue(fastestTime >= 10 && fastestTime <= 54, "Journey time should be close to 43 minutes, actual time was: " + fastestTime);
    }

}
