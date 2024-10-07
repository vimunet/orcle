To ensure that integers are properly represented as numbers and not as strings in the JSON output, you need to detect numeric fields and convert them to the appropriate type before writing them to JSON. Here's an updated version of the program that handles this:

### Updated Java Program:

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
        String csvFile = "data.csv";
        String jsonFile = "data.json";

        try {
            List<Map<String, Object>> data = readCsv(csvFile);
            writeJson(data, jsonFile);
            System.out.println("JSON file created successfully.");
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private static List<Map<String, Object>> readCsv(String csvFile) throws IOException, CsvException {
        List<Map<String, Object>> csvData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> rows = reader.readAll();
            String[] headers = rows.get(0);  // First row is the header

            for (int i = 1; i < rows.size(); i++) {
                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String value = rows.get(i)[j];

                    // Try to parse the value as an integer, otherwise keep it as a string
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

    private static void writeJson(List<Map<String, Object>> data, String jsonFile) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(data, writer);
        }
    }

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

### Changes:

1. **Map<String, Object>**: The program now uses `Map<String, Object>` instead of `Map<String, String>`. This allows us to store both strings and integers.

2. **isNumeric Method**: A helper method checks whether a string can be parsed into an integer. If it can, the value is stored as an `Integer`, otherwise it's kept as a `String`.

3. **Integer Parsing**: During CSV reading, the code checks if the value is numeric and converts it to an `Integer` if so. Otherwise, it leaves it as a string.

### CSV Example (`data.csv`):

```csv
id,name,age
1,John Doe,30
2,Jane Smith,25
3,Jim Brown,40
```

### JSON Output (`data.json`):

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

Now, integers will be written to the JSON output as numbers instead of being quoted strings.
