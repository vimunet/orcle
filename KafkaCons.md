import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaOffsetConsumer {
    public static void main(String[] args) {
        String topic = "your-topic";
        int partition = 0; // Specify the partition
        long offset = 100L; // Specify the offset to start consuming from

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace with your Kafka broker(s)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "custom-offset-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Ensures we don't miss messages

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // Assign the specific partition
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        consumer.assign(Collections.singletonList(topicPartition));

        // Seek to the specific offset
        consumer.seek(topicPartition, offset);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Consumed record with key=%s, value=%s, offset=%d%n",
                            record.key(), record.value(), record.offset());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
