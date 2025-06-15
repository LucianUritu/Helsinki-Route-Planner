import Backend.RoutingEngine;
import DataStructures.Node;
import Database.Queries.TransportTypeQuery;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * We are testing the query that returns a transportation type based on the stopId.
 * inside the query we are already turning the numbers found in the table to strings with the exact name of trarnsportation type
 */
public class TestTransprtTypeQuery {
    private TransportTypeQuery query = new TransportTypeQuery();
    private RoutingEngine engine = new RoutingEngine();

    public TestTransprtTypeQuery() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTransportType() {
        String expected = "Tram";
        assertEquals(expected, query.getTrasnportType(new Node("1050417", "1001_20240103_Ke_1_0540", 340)));
    }
}