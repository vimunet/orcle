Yes, it is a good practice to move the Kafka consumer properties to a factory to make the code cleaner and reusable. Below is the updated implementation with a `ConsumerFactory` for Kafka consumers.

---

### **1. Kafka Consumer Factory**

This factory centralizes the creation of Kafka consumers with the required properties.

```java
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConsumerFactory {

    @Bean
    public Properties kafkaConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dead-letter-processor");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start at the earliest offset
        return props;
    }
}
```

---

### **2. Updated Dead Letter Processor**

The `DeadLetterProcessor` now uses the consumer properties from the factory.

```java
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
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

@Component
public class DeadLetterProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterProcessor.class);

    @Value("${dead.letter.topics}")
    private List<String> deadLetterTopics;

    @Value("#{${topic.mapping}}") // Map of dead letter topic -> source topic
    private Map<String, String> topicMapping;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Properties consumerProperties;

    public DeadLetterProcessor(KafkaTemplate<String, String> kafkaTemplate, Properties consumerProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.consumerProperties = consumerProperties;
    }

    @PostConstruct
    public void processDeadLetterTopics() {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties)) {
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
                kafkaTemplate.send(sourceTopic, record.key(), record.value()).get();
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

### **3. Benefits of Moving to a Consumer Factory**

1. **Reusability**: 
   - The `kafkaConsumerProperties` bean can be reused for other consumers in the application.

2. **Centralized Configuration**:
   - All Kafka consumer-related properties are in a single location, making maintenance easier.

3. **Clean Code**:
   - The `DeadLetterProcessor` class is now focused only on the business logic, with no clutter from consumer configuration details.

---

### **4. Application Properties**

The application properties remain unchanged:

```properties
kafka.bootstrap-servers=localhost:9092
dead.letter.topics=dead-letter-topic1,dead-letter-topic2
topic.mapping.dead-letter-topic1=source-topic1
topic.mapping.dead-letter-topic2=source-topic2
```

---

This implementation achieves a cleaner design while adhering to good software engineering principles.
