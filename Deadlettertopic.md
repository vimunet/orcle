To achieve the goal of consuming all records from dead letter topics, republishing them to their respective source topics, and shutting down the application when processing is complete, you can use **Spring Boot with Kafka**. Here's a step-by-step guide:

---

### **1. Create a Kafka Listener for Dead Letter Topics**
Configure a Kafka consumer that listens to a list of dead letter topics. Use `@KafkaListener` or `KafkaConsumer`.

---

### **2. Process Messages and Republish to Source Topics**
Create a method to process each message and republish it to the appropriate source topic using a `KafkaTemplate`.

---

### **3. Detect When All Messages Are Consumed**
Use the Kafka `ConsumerRecords` poll method to check when there are no more records in the dead letter topics.

---

### **4. Shut Down the Application**
Once all records are processed and topics are empty, trigger an orderly shutdown using `SpringApplication.exit`.

---

### Implementation Example

```java
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
public class DeadLetterProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeadLetterProcessorApplication.class, args);
    }
}

@Component
class DeadLetterProcessor {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("#{'${dead.letter.topics}'.split(',')}")
    private List<String> deadLetterTopics;

    @Value("#{${topic.mapping}}") // Map of dead letter topic -> source topic
    private Map<String, String> topicMapping;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DeadLetterProcessor(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void processDeadLetterTopics() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "dead-letter-processor");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            deadLetterTopics.forEach(topic -> consumer.assign(Collections.singletonList(new TopicPartition(topic, 0))));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
                if (records.isEmpty()) {
                    // If all topics are empty, shut down the application
                    System.out.println("All dead letter topics are empty. Shutting down.");
                    SpringApplication.exit(SpringApplication.run(DeadLetterProcessorApplication.class), () -> 0);
                    break;
                }

                for (ConsumerRecord<String, String> record : records) {
                    String sourceTopic = topicMapping.get(record.topic());
                    if (sourceTopic != null) {
                        kafkaTemplate.send(new ProducerRecord<>(sourceTopic, record.key(), record.value()));
                        System.out.printf("Republished record from %s to %s%n", record.topic(), sourceTopic);
                    } else {
                        System.err.printf("No mapping found for topic: %s%n", record.topic());
                    }
                }
                consumer.commitSync(); // Commit offsets after processing
            }
        }
    }
}
```

---

### **Explanation of Key Components**
1. **Kafka Consumer**:
   - Reads messages from dead letter topics.
   - Manages offset commits manually to ensure reliability.

2. **KafkaTemplate**:
   - Publishes messages to their respective source topics.

3. **Dynamic Topic Mapping**:
   - Uses a configuration map to determine the source topic for each dead letter topic.

4. **Orderly Shutdown**:
   - When all records are consumed, the application shuts down using `SpringApplication.exit`.

---

### **Configuration**
Add the following properties to `application.properties` or `application.yml`:

```properties
kafka.bootstrap-servers=localhost:9092
dead.letter.topics=dead-letter-topic1,dead-letter-topic2
topic.mapping.dead-letter-topic1=source-topic1
topic.mapping.dead-letter-topic2=source-topic2
```

---

### **Run the Application**
1. Start the Kafka cluster.
2. Deploy this Spring Boot application.
3. The application will consume, republish, and shut down once all records are processed.

This approach ensures the process is efficient, scalable, and handles shutdown gracefully.
