To stop a Spring Boot Kafka consumer when there are no messages for 1 minute, you can achieve this by implementing a **customized poll timeout and a monitoring mechanism**. Here's how you can approach it:

### Implementation Steps

1. **Customize Kafka Consumer Configuration**  
   Set the `max.poll.interval.ms` and `session.timeout.ms` properties in the consumer configuration to control the polling behavior. These values should align with the desired timeout for inactivity.

2. **Monitor Message Processing**  
   Use a timer or scheduler to track the last time a message was consumed.

3. **Stop the Consumer**  
   Use the `KafkaListenerEndpointRegistry` to stop the consumer when no messages have been received for the specified duration.

---

### Example Implementation

#### 1. Kafka Configuration
```java
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 60000); // 1 minute
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

---

#### 2. Kafka Listener with Monitoring
```java
@Component
public class KafkaConsumer {

    private final KafkaListenerEndpointRegistry registry;
    private volatile long lastMessageTimestamp;

    public KafkaConsumer(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
        this.lastMessageTimestamp = System.currentTimeMillis();
    }

    @KafkaListener(id = "myConsumer", topics = "my-topic", groupId = "group_id")
    public void consume(String message) {
        lastMessageTimestamp = System.currentTimeMillis();
        System.out.println("Consumed message: " + message);
    }

    @Scheduled(fixedRate = 10000) // Check every 10 seconds
    public void checkInactivity() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTimestamp > 60000) { // 1 minute
            System.out.println("No messages for 1 minute. Stopping consumer...");
            registry.getListenerContainer("myConsumer").stop();
        }
    }
}
```

---

#### 3. Add Scheduler Support
Enable scheduling in your Spring Boot application by adding the `@EnableScheduling` annotation.

```java
@SpringBootApplication
@EnableScheduling
public class KafkaConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerApplication.class, args);
    }
}
```

---

### Key Notes
- The `@Scheduled` method periodically checks for inactivity and stops the consumer if no messages are received within the specified duration.
- Adjust the timeout (`60000 ms`) and scheduler interval (`10000 ms`) as needed.
- If you need to restart the consumer dynamically, you can call `registry.getListenerContainer("myConsumer").start()`.

This approach ensures that your consumer stops gracefully after a period of inactivity, conserving resources.
