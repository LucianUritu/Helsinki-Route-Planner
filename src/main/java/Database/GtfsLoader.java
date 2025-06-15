package Database;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The GtfsLoader class is responsible for loading GTFS data
 * from a folder containing `.txt` files into an SQLite database. Each `.txt` file is converted
 * into a corresponding table in the database, with the table structure and data matching the file's content. It also selects the first
 * day of the database and deletes all information about days that aren't this one
 */
public class GtfsLoader {
    private static final Map<String, String> fieldTypes = new HashMap<>();
    private static final List<String> tableNames = new ArrayList<>();
    private static final Pattern QUOTE_PATTERN = Pattern.compile("^\"|\"$");
    private static String SQLITE_URL;

    static {
        tableNames.add("calendar_dates");
        tableNames.add("calendar");
        tableNames.add("stops");
        tableNames.add("stop_times");
        tableNames.add("routes");
        tableNames.add("trips");
        tableNames.add("shapes");
        tableNames.add("agency");
    }

    static {
        //I've split these by fieldname : key // Type
        fieldTypes.put("route_id", "TEXT");
        fieldTypes.put("service_id", "TEXT");
        fieldTypes.put("agency_id", "TEXT");
        fieldTypes.put("route_short_name", "TEXT");
        fieldTypes.put("route_long_name", "TEXT");
        fieldTypes.put("route_desc", "TEXT");
        fieldTypes.put("route_type", "INTEGER");
        fieldTypes.put("trip_id", "TEXT");
        fieldTypes.put("stop_id", "TEXT");
        fieldTypes.put("stop_sequence", "INTEGER");
        fieldTypes.put("stop_lat", "REAL");
        fieldTypes.put("stop_lon", "REAL");
        fieldTypes.put("stop_name", "TEXT");
        fieldTypes.put("arrival_time", "TEXT");
        fieldTypes.put("departure_time", "TEXT");
        fieldTypes.put("shape_seg_id", "TEXT");
        fieldTypes.put("shape_id", "TEXT");
        fieldTypes.put("shape_lat", "REAL");
        fieldTypes.put("shape_lon", "REAL");
        fieldTypes.put("shape_sequence", "INTEGER");

    }

    private final Path folderPath;

    public GtfsLoader(String folderPath) {
        this.folderPath = Paths.get(folderPath);
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(SQLITE_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the GTFS data from the folder into an SQLite database.
     * If the database already exists, it connects to it; otherwise, it creates a new database.
     *
     * @return A Connection object to the SQLite database, or null if an error occurs.
     * @throws CsvValidationException
     */

    public void loadGtfsFolder() throws CsvValidationException, FileNotFoundException {
        File folder = this.folderPath.toFile();
        if (!folder.exists() || folder.isDirectory()) {
            throw new FileNotFoundException("File not found");
        }

        String dbName = this.folderPath.getFileName().toString().replace(".zip", "");
        String dbPath = dbName + ".db";
        SQLITE_URL = "jdbc:sqlite:" + dbPath;
        File dbFile = new File(dbPath);
        if (!dbFile.exists() && !dbFile.isDirectory()) {
            createDatabase();
        }
    }

    private void createDatabase() throws CsvValidationException {
        try {
            try (Connection connection = getConnection()) {
                createAndLoadTablesFromZip(connection);
                createIndexes(connection);
                filterForFirstCalendarDay(connection);
            }
        } catch (SQLException e) {
            System.err.println("Error while creating database: " + e.getMessage());
        }
    }

    private void createIndexes(Connection connection) {
        String[] indexSQLs = {
                "CREATE INDEX IF NOT EXISTS index_StopTimes_On_StopId_And_DepartureTime ON stop_times (stop_id, departure_time);",
                "CREATE INDEX IF NOT EXISTS index_StopTimes_On_ripId_And_StopSequence ON stop_times (trip_id, stop_sequence);",
                "CREATE INDEX IF NOT EXISTS idx_Stops_On_lat_lon ON stops (stop_lat, stop_lon);",
                "CREATE INDEX IF NOT EXISTS idx_StopTimes_On_tripId ON stop_times (trip_id);",
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : indexSQLs) {
                stmt.execute(sql);
                System.err.println("Index created: " + sql);
            }
        } catch (SQLException e) {
            System.err.println("Error creating indexes: " + e.getMessage());
        }
    }

    private void filterForFirstCalendarDay(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT tbl_name FROM sqlite_master WHERE type = 'table' ");
            Set<String> existingTables = new HashSet<>();
            while (rs.next()) {
                existingTables.add(rs.getString(1));
            }
            List<String> serviceIds = new ArrayList<>();
            String serviceIdQuery;
            if (!existingTables.contains("calendar")) {
                System.err.println("[FilterForFirstCalendarDay] Could not find calendar table!");
                serviceIdQuery = "SELECT DISTINCT service_id FROM calendar_dates WHERE date IN (SELECT date FROM (SELECT date, strftime('%w', substr(date, 1, 4) || '-' || substr(date, 5, 2) || '-' || substr(date, 7, 2)) AS weekday FROM calendar_dates WHERE exception_type <> 2) WHERE weekday = '1' LIMIT 1)";
            } else {
                if (existingTables.contains("calendar_dates")) {
                    serviceIdQuery = "SELECT DISTINCT c.service_id FROM calendar c LEFT JOIN calendar_dates cdates WHERE cdates.exception_type <> 2 AND monday = 1 AND (SELECT start_date FROM calendar WHERE monday = 1 LIMIT 1) BETWEEN start_date AND end_date ORDER BY start_date;";
                } else {
                    System.err.println("[FilterForFirstCalendarDay] Could not find calendar_dates table!");
                    serviceIdQuery = "SELECT DISTINCT c.service_id FROM calendar c WHERE monday = 1 AND (SELECT start_date FROM calendar WHERE monday = 1 LIMIT 1) BETWEEN start_date AND end_date ORDER BY start_date;";
                }
            }
            ResultSet rs2 = stmt.executeQuery(serviceIdQuery);
            while (rs2.next()) {
                serviceIds.add(rs2.getString("service_id"));
            }

            String serviceIdList = "'" + String.join("','", serviceIds) + "'";
            stmt.executeUpdate("DELETE FROM trips WHERE service_id NOT IN (" + serviceIdList + ")");
            stmt.executeUpdate("DELETE FROM stop_times WHERE trip_id NOT IN (SELECT trip_id FROM trips)");
            stmt.executeUpdate("DELETE FROM routes WHERE route_id NOT IN (SELECT DISTINCT route_id FROM trips)");
            stmt.executeUpdate("DELETE FROM stops WHERE stop_id NOT IN (SELECT DISTINCT stop_id FROM stop_times)");
            if (existingTables.contains("shapes")) {
                stmt.executeUpdate("DELETE FROM shapes WHERE shape_id NOT IN (SELECT DISTINCT shape_id FROM trips)");
            }

        } catch (Exception e) {
            System.err.println("Error filtering for first calendar day: " + e.getMessage());
        }
    }


    private void createAndLoadTablesFromZip(Connection connection) throws CsvValidationException, SQLException {
        //now pulling out only the tables we use
        try (ZipFile zipFile = new ZipFile(this.folderPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (!entry.getName().endsWith(".txt")) {
                    continue;
                }
                if (!tableNames.contains(entry.getName().replace(".txt", "").toLowerCase())) {
                    continue;
                }
                createTable(connection, entry.getName(), zipFile.getInputStream(entry));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable(Connection connection, String fileName, InputStream inputStream) throws IOException, SQLException, CsvValidationException {
        String tableName = fileName.replace(".txt", "");

        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
            String[] columns = getColumnsFromHeader(br, fileName);
            if (columns == null) return;

            createTableInDatabase(connection, tableName, columns);
            insertDataIntoTable(connection, tableName, columns, br, fileName);
        } catch (IOException | SQLException e) {
            System.err.println("Error processing file '" + fileName + "': " + e.getMessage());
        }
    }

    private String[] getColumnsFromHeader(BufferedReader br, String fileName) throws IOException {
        String headerLine = br.readLine();
        if (headerLine == null) {
            System.err.println("File '" + fileName + "' is empty. Skipping.");
            return null;
        }
        return headerLine.split(",");
    }

    // now will write the db with a specific type and default to strings if we have not specified in the map :)
    private void createTableInDatabase(Connection connection, String tableName, String[] columns) throws SQLException {
        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (int i = 0; i < columns.length; i++) {

            String clean = QUOTE_PATTERN.matcher(columns[i]).replaceAll("");

            if (fieldTypes.containsKey(clean)) {
                String type = fieldTypes.get(clean);
                createTableSQL.append(clean).append(" " + type);
            } else {
                createTableSQL.append(clean).append(" TEXT");
            }
            if (i < columns.length - 1) {
                createTableSQL.append(", ");
            }
        }
        createTableSQL.append(");");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL.toString());
            System.err.println("Table '" + tableName + "' has been created.");
        }
    }

    private void insertDataIntoTable(Connection connection, String tableName, String[] columns, BufferedReader br, String fileName) throws SQLException, IOException, CsvValidationException {
        String insertSQL = buildInsertSQL(tableName, columns);

        connection.setAutoCommit(false);
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            batchInsertRows(br, pstmt, fileName, columns);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            System.err.println("Error inserting data from file '" + fileName + "': " + e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private String buildInsertSQL(String tableName, String[] columns) {
        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
        for (int i = 0; i < columns.length; i++) {
            insertSQL.append("?");
            if (i < columns.length - 1) {
                insertSQL.append(", ");
            }
        }
        insertSQL.append(");");
        return insertSQL.toString();
    }

    private void batchInsertRows(BufferedReader br, PreparedStatement pstmt, String fileName, String[] columns) throws IOException, SQLException, CsvValidationException {

        try (CSVReader csvReader = new CSVReader(br)) {
            String[] values;
            int batchSize = 100000;
            int count = 0;

            List<Integer> timeColumns = IntStream.range(0, columns.length)
                    .filter(idx -> {
                        String cleaned = QUOTE_PATTERN.matcher(columns[idx]).replaceAll("");
                        return cleaned.equals("arrival_time") || cleaned.equals("departure_time");
                    }).boxed().toList();

            while ((values = csvReader.readNext()) != null) {
                if (values.length != columns.length) {
                    throw new SQLException("Columns do not match " + fileName + ": expected " + columns.length + " but got " + values.length + " line " + Arrays.toString(values));
                }

                for (int i = 0; i < values.length; i++) {
                    if (timeColumns.contains(i) && (values[i].length() == 7)) {
                        pstmt.setString(i + 1, "0" + values[i]);
                    } else {
                        pstmt.setString(i + 1, values[i]);
                    }
                }
                pstmt.addBatch();
                count++;

                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch();
            System.err.println("Data from file '" + fileName + "' has been inserted.");
        }
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                System.err.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}