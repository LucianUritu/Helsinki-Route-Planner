package Database.Queries;

import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * query to complete our routes part of the output exactly as requested by the client, so we are retrieving:
 * 1. the long name of the bus
 * 2. the short name of the bus
 * 3. the headsign
 */

public class BusDetailsQuery {

    public String getLongName(String tripId) {
        String sql = """
                SELECT r.route_long_name
                FROM trips t
                JOIN routes r ON t.route_id= r.route_id
                WHERE t.trip_id = ?
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, tripId);
            var rs = st.executeQuery();

            while (rs.next()) {
                return rs.getString("route_long_name");
            }

        } catch (SQLException e) {
            System.err.println("Error Bus Details Query: " + e.getMessage());
        }
        return null;
    }

    public String getShortName(String tripId) {
        String sql = """
                SELECT r.route_short_name
                FROM trips t
                JOIN routes r ON t.route_id= r.route_id
                WHERE t.trip_id = ?
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, tripId);
            var rs = st.executeQuery();

            while (rs.next()) {
                return rs.getString("route_short_name");
            }

        } catch (SQLException e) {
            System.err.println("Error Bus Details Query: " + e.getMessage());
        }
        return null;
    }

    public String getHeadsign(String tripId) {
        String sql = """
                SELECT trip_headsign
                FROM trips 
                WHERE trip_id = ?
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, tripId);
            var rs = st.executeQuery();

            while (rs.next()) {

                String raw = rs.getString("trip_headsign");
                if (raw == null) return null;
                return raw.replaceAll("^\"|\"$", "");
            }

        } catch (SQLException e) {
            System.err.println("Error Bus Details Query: " + e.getMessage());
        }
        return null;
    }

}
