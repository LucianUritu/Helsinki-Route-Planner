import Backend.RoutingEngine;
import Database.Queries.BusDetailsQuery;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/*
 * we are making sure the headsign of a given list of trips is not null by testing our BusDetailsQuery query
 */

public class TestHeadsignQuery {
    private RoutingEngine engine = new RoutingEngine();

    public TestHeadsignQuery() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testGetHeadsign() {
        BusDetailsQuery query = new BusDetailsQuery();

        List<String> tripIds = new ArrayList<>();
        tripIds.add("1001_20240103_Ke_1_0628");
        tripIds.add("1001_20240103_Ke_1_0640");

        List<String> headsigns = new ArrayList<>();

        for (String tripID : tripIds) {
            headsigns.add(query.getHeadsign(tripID));
        }

        assertNotNull(headsigns, "Headsigns list should not be null");
        assertFalse(headsigns.isEmpty(), "Headsigns list should not be empty");

        for (String headsign : headsigns) {
            System.out.println("Headsign: " + headsign);
        }
    }
}
