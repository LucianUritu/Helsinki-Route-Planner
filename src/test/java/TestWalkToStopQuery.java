import Backend.RoutingEngine;
import DataStructures.Coordinate;
import DataStructures.Node;
import Database.Queries.WalkToStopQuery;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * We are testing the query to make sure that a List of Nodes is returned when inputting a qalking cooridnate
 * this list represents all stops we can walk to withing the limit we inoutted and the radius we chose
 */
public class TestWalkToStopQuery {
    private WalkToStopQuery query = new WalkToStopQuery();
    private RoutingEngine engine = new RoutingEngine();

    public TestWalkToStopQuery() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryWalkStops() {
        assertNotNull(query.queryWalkStops(new Node(new Coordinate(60.164750, 24.933773), 480)));
    }
}