To load all the properties from the `application.properties` file into a static `Map`, you can use `Environment` to fetch all properties and then store them in a `static` map during the initialization of your Spring application. Here’s how to do it:

### Steps:

1. **Create a component to store all properties in a static map**:
   - Use Spring’s `Environment` to access all the properties.
   - In the `@PostConstruct` method, iterate over the properties and store them in a static `Map`.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ApplicationProperties {

    @Autowired
    private Environment environment;

    private static Map<String, String> propertiesMap = new HashMap<>();

    // This method is called after all dependencies are injected
    @PostConstruct
    public void init() {
        // Load all properties from the environment and store them in the static map
        for (Properties propertySource : ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources()) {
            if (propertySource instanceof Map) {
                propertiesMap.putAll((Map<String, String>) propertySource);
            }
        }
    }

    // Static method to get a specific property by key
    public static String getStaticProperty(String key) {
        return propertiesMap.get(key);
    }

    // Static method to get all properties
    public static Map<String, String> getAllStaticProperties() {
        return propertiesMap;
    }
}
```

### Explanation:

- **`@Autowired Environment`**: The Spring `Environment` interface is used to access properties.
- **`@PostConstruct init()`**: After Spring initializes the component, we use this method to populate a static `Map` with all properties from the `application.properties` file.
- **Static methods**: Provide access to individual properties (`getStaticProperty()`) or the entire `Map` of properties (`getAllStaticProperties()`).

### Accessing the Properties:

You can now access individual properties or the entire `Map` from any part of your application using the static methods:

```java
public class SomeService {

    public void doSomething() {
        // Access all properties
        Map<String, String> properties = ApplicationProperties.getAllStaticProperties();
        System.out.println(properties);

        // Access a specific property by key
        String specificProperty = ApplicationProperties.getStaticProperty("my.property");
        System.out.println(specificProperty);
    }
}
```

### Notes:
- You can filter properties based on prefixes or other criteria before adding them to the map.
- This approach works well for properties defined in `application.properties` or any other property sources defined in your Spring configuration.

Let me know if you need any adjustments!
