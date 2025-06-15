import Backend.RoutingEngine;
import DataStructures.Coordinate;
import Database.Queries.CoordinateQuery;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * We are testing the query that returns the object Coordinates givne a stopId.
 * we're not using assertEquals because, since the query returns a coordinate object, it would compare the memory addresses.
 */

public class TestCoordinateQuery {

    private CoordinateQuery query = new CoordinateQuery();
    private RoutingEngine engine = new RoutingEngine();

    public TestCoordinateQuery() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCoordinateByStopId() {
        assertNotNull(query.getCoordinateByStopId("1020243"));
        System.out.println(query.getCoordinateByStopId("1020243"));
    }

    @Test
    public void testGetStopIdByCoordinate() {
        assertNotNull(query.getStopIdByCoordinate(new Coordinate(60.17428, 24.96071)));
    }

}