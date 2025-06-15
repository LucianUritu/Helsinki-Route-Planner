package Database.Queries;

import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StopNameQuery {
    public String getStopName(String stopId) {
        String sql = """
                SELECT stop_name
                FROM stops
                WHERE stop_id = ?
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, stopId);
            var rs = st.executeQuery();


            while (rs.next()) {
                return rs.getString("stop_name");
            }

        } catch (SQLException e) {
            System.err.println("Error finding Stop name Query: " + e.getMessage());
        }
        return null;
    }


}
