Below is the complete project hierarchy for implementing a Spring Boot Kafka consumer that processes messages of different types (`String` and `byte[]`) and republishes the processed message to another topic using generic processing logic.

### Project Structure

```
kafka-generic-processor/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── kafka/
│   │   │               ├── config/
│   │   │               │   └── KafkaConfig.java
│   │   │               ├── processor/
│   │   │               │   ├── MessageProcessor.java
│   │   │               │   ├── StringMessageProcessor.java
│   │   │               │   └── ByteArrayMessageProcessor.java
│   │   │               ├── service/
│   │   │               │   ├── KafkaGenericConsumerService.java
│   │   │               │   └── KafkaProcessorService.java
│   │   │               └── KafkaGenericProcessorApplication.java
│   │   └── resources/
│   │       └── application.properties
└── pom.xml
```

### 1. `KafkaGenericProcessorApplication.java`

This is the main Spring Boot application class.

```java
package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaGenericProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaGenericProcessorApplication.class, args);
    }
}
```

### 2. `KafkaConfig.java`

This class configures Kafka consumers and producers. You can set different deserializers for each consumer, depending on the type of data consumed.

```java
package com.example.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "your-msk-bootstrap-server-endpoint:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // Consumer factory for String messages
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "your-msk-bootstrap-server-endpoint:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "your-group-id");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    // Consumer factory for byte[] messages
    @Bean
    public ConsumerFactory<String, byte[]> byteArrayConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "your-msk-bootstrap-server-endpoint:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "your-group-id");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(config);
    }
}
```

### 3. `MessageProcessor.java` (Generic Interface)

Defines a generic interface for message processors.

```java
package com.example.kafka.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MessageProcessor<K, V> {
    void process(ConsumerRecord<K, V> record);
}
```

### 4. `StringMessageProcessor.java`

Implements the processor for `String` messages.

```java
package com.example.kafka.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class StringMessageProcessor implements MessageProcessor<String, String> {

    @Override
    public void process(ConsumerRecord<String, String> record) {
        String message = record.value();
        System.out.println("Processing String message: " + message);
        // Implement specific logic for processing String messages
    }
}
```

### 5. `ByteArrayMessageProcessor.java`

Implements the processor for `byte[]` messages.

```java
package com.example.kafka.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class ByteArrayMessageProcessor implements MessageProcessor<String, byte[]> {

    @Override
    public void process(ConsumerRecord<String, byte[]> record) {
        byte[] message = record.value();
        System.out.println("Processing byte[] message: " + new String(message));
        // Implement specific logic for processing byte[] messages
    }
}
```

### 6. `KafkaProcessorService.java`

A service that republishes processed messages to Kafka.

```java
package com.example.kafka.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProcessorService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProcessorService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishProcessedMessage(String topic, String processedMessage) {
        kafkaTemplate.send(topic, processedMessage);
        System.out.println("Published processed message to topic: " + topic);
    }
}
```

### 7. `KafkaGenericConsumerService.java`

The Kafka listener service that consumes messages of different types and delegates processing to the appropriate processor.

```java
package com.example.kafka.service;

import com.example.kafka.processor.MessageProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaGenericConsumerService {

    private final MessageProcessor<String, String> stringMessageProcessor;
    private final MessageProcessor<String, byte[]> byteArrayMessageProcessor;
    private final KafkaProcessorService kafkaProcessorService;

    public KafkaGenericConsumerService(
            MessageProcessor<String, String> stringMessageProcessor,
            MessageProcessor<String, byte[]> byteArrayMessageProcessor,
            KafkaProcessorService kafkaProcessorService) {
        this.stringMessageProcessor = stringMessageProcessor;
        this.byteArrayMessageProcessor = byteArrayMessageProcessor;
        this.kafkaProcessorService = kafkaProcessorService;
    }

    @KafkaListener(topics = "string-topic", groupId = "your-group-id", containerFactory = "stringKafkaListenerContainerFactory")
    public void consumeStringMessages(ConsumerRecord<String, String> record) {
        processMessage(record, stringMessageProcessor);
    }

    @KafkaListener(topics = "bytes-topic", groupId = "your-group-id", containerFactory = "byteArrayKafkaListenerContainerFactory")
    public void consumeByteMessages(ConsumerRecord<String, byte[]> record) {
        processMessage(record, byteArrayMessageProcessor);
    }

    private <K, V> void processMessage(ConsumerRecord<K, V> record, MessageProcessor<K, V> processor) {
        processor.process(record);
        // Example of publishing a processed message to another topic
        if (record.value() instanceof String) {
            kafkaProcessorService.publishProcessedMessage("output-topic", (String) record.value());
        }
    }
}
```

### 8. `application.properties`

Configure Kafka properties for both consumers and producers.

```properties
# Kafka Consumer Configurations
spring.kafka.bootstrap-servers=your-msk-bootstrap-server-endpoint:9092
spring.kafka.consumer.group-id=your-group-id
spring.kafka.consumer.auto-offset-reset=earliest

# String Deserializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# ByteArray Deserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer

# Kafka Producer Configurations
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

### 9. `pom.xml`

Ensure that your `pom.xml` includes the necessary dependencies for Kafka and Spring Boot.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
</dependencies>
```

### Conclusion

This setup enables you to process Kafka messages of different types (`String` and `byte[]`) using generic processing logic. You can easily extend this system by adding new processors for different types or introducing more advanced processing logic.
