To add proper error handling, we can introduce logging, retry mechanisms, and exception handling. This ensures that failures during processing, such as connectivity issues or invalid messages, are logged and optionally retried.

Here's the updated implementation with error handling:

---

### Updated Code with Error Handling

```java
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterProcessor.class);

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
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
                    if (records.isEmpty()) {
                        logger.info("All dead letter topics are empty. Shutting down.");
                        SpringApplication.exit(SpringApplication.run(DeadLetterProcessorApplication.class), () -> 0);
                        break;
                    }

                    for (ConsumerRecord<String, String> record : records) {
                        processRecord(record);
                    }

                    consumer.commitSync(); // Commit offsets after processing
                } catch (Exception e) {
                    logger.error("Error during processing or committing offsets", e);
                }
            }
        } catch (Exception e) {
            logger.error("Error initializing Kafka consumer", e);
        }
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            String sourceTopic = topicMapping.get(record.topic());
            if (sourceTopic != null) {
                kafkaTemplate.send(new ProducerRecord<>(sourceTopic, record.key(), record.value())).get();
                logger.info("Republished record from {} to {}", record.topic(), sourceTopic);
            } else {
                logger.warn("No mapping found for topic: {}", record.topic());
            }
        } catch (Exception e) {
            logger.error("Failed to process record from topic: {} with key: {}", record.topic(), record.key(), e);
            throw e; // Trigger retry if enabled
        }
    }
}
```

---

### Key Enhancements

1. **Retry Mechanism**:
   - Added Spring Retry to handle transient errors while republishing records.
   - Retries up to 3 times with exponential backoff (`@Retryable`).

2. **Logging**:
   - Comprehensive logging for all stages of processing (e.g., initialization, processing, shutdown).
   - Logs errors during message processing and offset commits.

3. **Graceful Fallbacks**:
   - Handles cases where no mapping exists for a dead letter topic (`logger.warn`).
   - Ensures the application logs the cause of shutdown properly.

4. **Exception Handling**:
   - Catches and logs errors at each stage (consumer initialization, polling, processing).
   - Ensures errors do not crash the application unless explicitly intended (e.g., retries exhausted).

---

### Dependency Updates

Add the Spring Retry dependency to your `pom.xml` for the retry mechanism:

```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
</dependency>
```

---

### **What Happens Now**
1. The application retries up to 3 times for transient errors (e.g., network issues) when republishing records.
2. If all retries fail, it logs the failure and continues processing the remaining records.
3. Graceful shutdown occurs when no more messages are left on all dead letter topics.

This approach ensures reliability and provides visibility into any issues that arise during processing.
