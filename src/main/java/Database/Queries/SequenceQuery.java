package Database.Queries;

import DataStructures.Stop;
import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SequenceQuery {
    //get neighbors class/method
    public Stop getNextImmediateNeighbor(String stopId, String tripId) {
        String sql = """
                SELECT stop_id, departure_time
                FROM stop_times
                WHERE trip_id = ? AND stop_sequence = 
                    (SELECT stop_sequence +1 
                    FROM stop_times WHERE stop_id = ? 
                    AND trip_id = ?) 
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tripId);
            pstmt.setString(2, stopId);
            pstmt.setString(3, tripId);

            var rs = pstmt.executeQuery();

            while (rs.next()) {
                String stopIdNeighbor = rs.getString("stop_id");
                String arrivalTime = rs.getString("departure_time");
                // System.err.println("inside sequence query, stopIdNeighbor: " + stopIdNeighbor +  " arrival time: " + arrivalTime);
                return new Stop(stopIdNeighbor, arrivalTime);
            }


        } catch (SQLException e) {
            System.err.println("Error TripIdQuery: " + e.getMessage());
        }

        return null;
    }
}
