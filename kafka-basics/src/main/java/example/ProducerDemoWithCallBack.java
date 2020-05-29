package example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallBack {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallBack.class);

        String bootStrapServers = "127.0.0.1:9092";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for(int i =0; i< 10; i++) {
            String value = "Hellow from " + i;
            String topic = "first-topic";
            String key = "id_" + i;
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record, (recordMetadata, error) -> {
                if (error == null) {
                    logger.info("\nOffset " + recordMetadata.offset() + "\n" +
                            "Partitions " + recordMetadata.partition() + "\n" +
                            "Topic " + recordMetadata.topic() + "\n" +
                            "Timestamp " + recordMetadata.timestamp() + "\n");
                } else {
                    logger.error("Error producing to topic " + recordMetadata.topic(), error);
                }
            });
        }
        producer.flush();
        producer.close();
    }
}
