import Backend.RoutingEngine;
import Database.Queries.StopNameQuery;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * We are testing the query that returns a bus stop name based given the stopId.
 * it returns the name of the stop inside "" so we had to add these in the expected resulsts
 */
public class TestStopNameQuery {
    private StopNameQuery query = new StopNameQuery();
    private RoutingEngine engine = new RoutingEngine();

    public TestStopNameQuery() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStopName() {
        String expected = "Telakkakatu";
        assertEquals(expected, query.getStopName("1050417"));
    }
}