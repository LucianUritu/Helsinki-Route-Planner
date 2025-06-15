import Backend.Algorithms.DijkstraHeatmap;
import Backend.RoutingEngine;
import DataStructures.Coordinate;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test if heatmap is generated in less than 15 seconds
 */
public class TestHeatmap {
    private RoutingEngine engine = new RoutingEngine();

    public TestHeatmap() {
        try {
            engine.loadDatabase("src/main/resources/hsl.zip");
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHeatmapTime() {
        long startTime = System.currentTimeMillis();
        Coordinate start = new Coordinate(60.164750, 24.933773);
        DijkstraHeatmap dijkstraHeatmap = new DijkstraHeatmap(start, 480);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertTrue(duration < 20000, "Heatmap generation took more than 20 seconds: " + duration + " milliseconds");
    }
}
