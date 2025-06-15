/*
 * Query to retrive the agency name for each transport taken
 */

package Database.Queries;

import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AgencyQuery {

    public String getAgency(String tripId) {
        String sql = """
                SELECT agency_name
                FROM agency
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            var rs = st.executeQuery();

            while (rs.next()) {
                return rs.getString("agency_name");
            }

        } catch (SQLException e) {
            System.err.println("Error Agency name Query: " + e.getMessage());
        }
        return null;
    }

}
