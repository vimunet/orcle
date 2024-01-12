import com.opencsv.CSVWriter;

import java.io.*;
import java.sql.*;

public class ExportToCSVWithBlobToFile {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:oracle:thin:@your_database_host:1521:your_sid";
        String username = "your_username";
        String password = "your_password";
        String sqlQuery = "SELECT * FROM your_table";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery();
             CSVWriter csvWriter = new CSVWriter(new FileWriter("output.csv"))) {

            // Get column names
            String[] columnNames = getColumnNames(resultSet);

            // Write column names to CSV
            csvWriter.writeNext(columnNames);

            while (resultSet.next()) {
                // Get data for each row
                String[] rowData = getRowData(resultSet);

                // Write row data to CSV
                csvWriter.writeNext(rowData);
            }

            System.out.println("Data exported to CSV successfully.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] getColumnNames(ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        String[] columnNames = new String[columnCount];

        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = resultSet.getMetaData().getColumnName(i);
        }

        return columnNames;
    }

    private static String[] getRowData(ResultSet resultSet) throws SQLException, IOException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        String[] rowData = new String[columnCount];

        for (int i = 1; i <= columnCount; i++) {
            // Handle BLOB data separately
            if (resultSet.getMetaData().getColumnType(i) == java.sql.Types.BLOB) {
                InputStream inputStream = resultSet.getBinaryStream(i);
                rowData[i - 1] = handleBlobData(inputStream);
            } else {
                rowData[i - 1] = resultSet.getString(i);
            }
        }

        return rowData;
    }

    private static String handleBlobData(InputStream inputStream) throws IOException {
        // Create a unique filename for each BLOB
        String filename = "blob_data_" + System.currentTimeMillis() + ".bin";

        try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        return filename;
    }
}
