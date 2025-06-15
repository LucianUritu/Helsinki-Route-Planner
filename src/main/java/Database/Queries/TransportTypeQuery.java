/*
 * query to retrieve the exact type of public transportation taken during a path. 
 * from gtfs.org we can get the legenda:
0 - Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
1 - Subway, Metro. Any underground rail system within a metropolitan area.
2 - Rail. Used for intercity or long-distance travel.
3 - Bus. Used for short- and long-distance bus routes.
4 - Ferry. Used for short- and long-distance boat service.
5 - Cable tram. Used for street-level rail cars where the cable runs beneath the vehicle (e.g., cable car in San Francisco).
6 - Aerial lift, suspended cable car (e.g., gondola lift, aerial tramway). Cable transport where cabins, cars, gondolas or open chairs are suspended by means of one or more cables.
7 - Funicular. Any rail system designed for steep inclines.
*/
package Database.Queries;

import DataStructures.Node;
import Database.GtfsLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransportTypeQuery {

    public String getTrasnportType(Node node) {

        String stopID = node.getStopId();

        String sql = """
                SELECT vehicle_type
                FROM stops
                WHERE stop_id = ?
                """;

        try (Connection connection = GtfsLoader.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, stopID);
            var rs = st.executeQuery();

            while (rs.next()) {
                return TransportTypeToName(rs.getString("vehicle_type"));
            }

        } catch (SQLException e) {
            System.err.println("Error Transport time Query: " + e.getMessage());
        }
        return "ride";
    }

    public String TransportTypeToName(String vehicle) {
        int convertedVehicle = Integer.valueOf(vehicle);
        if (convertedVehicle == 0) {
            return "Tram";
        } else if (convertedVehicle == 1) {
            return "Metro";
        } else if (convertedVehicle == 2) {
            return "Rail";
        } else if (convertedVehicle == 3) {
            return "Bus";
        } else if (convertedVehicle == 4) {
            return "Ferry";
        } else if (convertedVehicle == 5) {
            return "Cable tram";
        } else if (convertedVehicle == 6) {
            return "Aerial lift";
        } else if (convertedVehicle == 7) {
            return "Funicular";
        } else if (convertedVehicle == 11) {
            return "Trolleybus";
        } else if (convertedVehicle == 12) {
            return "Monorail";
        } else if (convertedVehicle == 15 || convertedVehicle == 109) {
            return "Train";
        } else if (convertedVehicle == 17) {
            return "On-demand bus";
        }
        return "transport type not specified";
    }
}
