Here is the complete solution, including the full program, explanation, and examples.

### 1. Add the necessary dependencies:

If you're using Maven, add these dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- OpenCSV for CSV reading -->
    <dependency>
        <groupId>com.opencsv</groupId>
        <artifactId>opencsv</artifactId>
        <version>5.5.2</version>
    </dependency>

    <!-- Gson for JSON conversion -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.8</version>
    </dependency>
</dependencies>
```

### 2. Java Program to Convert CSV to JSON:

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CsvToJsonConverter {

    public static void main(String[] args) {
        String csvFile = "data.csv"; // Path to the CSV file
        String jsonFile = "data.json"; // Path to the output JSON file

        try {
            // Read CSV and store it as a list of maps
            List<Map<String, Object>> data = readCsv(csvFile);

            // Write the JSON output to the file
            writeJson(data, jsonFile);

            System.out.println("JSON file created successfully.");
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    // Method to read CSV and convert rows to a list of maps
    private static List<Map<String, Object>> readCsv(String csvFile) throws IOException, CsvException {
        List<Map<String, Object>> csvData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> rows = reader.readAll();
            String[] headers = rows.get(0);  // First row is the header

            for (int i = 1; i < rows.size(); i++) {
                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String value = rows.get(i)[j];

                    // Check if value is numeric and convert to Integer if applicable
                    if (isNumeric(value)) {
                        rowData.put(headers[j], Integer.parseInt(value));
                    } else {
                        rowData.put(headers[j], value);
                    }
                }
                csvData.add(rowData);
            }
        }
        return csvData;
    }

    // Method to write the data to JSON format
    private static void writeJson(List<Map<String, Object>> data, String jsonFile) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(data, writer);
        }
    }

    // Helper method to check if a value is numeric (integer)
    private static boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

### 3. Explanation:

- **Dependencies**:
   - `OpenCSV` (`com.opencsv`) is used to read the CSV file.
   - `Gson` (`com.google.code.gson`) is used to convert the data into JSON and write it to a file.

- **`readCsv` Method**:
   - This method reads the CSV file into a list of maps where each map represents a row. The key for each entry in the map is the header, and the value is the corresponding cell value from that row.
   - The method checks if a value can be parsed as an integer and, if so, converts it into an `Integer` object. Otherwise, the value is stored as a string.

- **`writeJson` Method**:
   - Converts the list of maps into JSON format and writes it to a specified output file using `Gson`.

- **`isNumeric` Method**:
   - This method checks whether a string can be parsed as an integer. If it can, the string will be converted into an integer in the final JSON file.

### 4. CSV Example (`data.csv`):

```csv
id,name,age
1,John Doe,30
2,Jane Smith,25
3,Jim Brown,40
```

### 5. JSON Output (`data.json`):

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "age": 30
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "age": 25
  },
  {
    "id": 3,
    "name": "Jim Brown",
    "age": 40
  }
]
```

### Key Points:

- **Numbers in JSON**: In this example, integers such as `id` and `age` are represented as numbers in the JSON file, not as strings.
- **Non-numeric Data**: Names and any other non-numeric data will remain as strings in the JSON output.
- **Scalability**: This solution can handle different types of CSV files and can be easily expanded for more complex data structures.

This program reads a CSV file, processes the rows and columns, and outputs a properly structured JSON file with numbers and strings correctly represented.
