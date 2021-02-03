package example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {
    public static void main(String[] args) {
        new ConsumerDemoWithThreads().run();
    }

    public ConsumerDemoWithThreads() {
    }

    public void run() {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);
        String bootstrapServer = "127.001:9092";
        String groupId = "first-application";
        String topic = "first-topic";

        // Latch for dealing with multiple threads
        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Creating the consumer thread");

        // create consumer runnable
        Runnable consumer = new ConsumerThreads(topic, groupId, bootstrapServer, latch);

        // start the thread
        Thread thread = new Thread(consumer);
        thread.start();
        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown");
            ((ConsumerThreads) consumer).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited!");
        }));
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    public class ConsumerThreads implements Runnable {

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);

        public ConsumerThreads(String topic, String groupId, String bootstrapServer, CountDownLatch latch) {
            this.latch = latch;
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            consumer = new KafkaConsumer<String, String>(properties);
            //consumer.subscribe(Arrays.asList(topic));

            TopicPartition partition = new TopicPartition(topic, 0);
            consumer.assign(Arrays.asList(partition));

            consumer.seek(partition, 18L);
        }

        @Override
        public void run() {
            int noOfMessage = 5;
            boolean keepOnReading = true;
            int numberOfMessagesReadSoFar = 0;
            // poll for new data
            try {
                do {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        numberOfMessagesReadSoFar += 1;
                        logger.info("Key " + record.key() + "\n" +
                                "Value " + record.value() + "\n" +
                                "Partition " + record.partition() + "\n" +
                                "Offset " + record.offset() + "\n");
                        if(numberOfMessagesReadSoFar >= noOfMessage) {
                            keepOnReading = false;
                            break;
                        }
                    }
                } while (keepOnReading);
                logger.info("Exiting");
            } catch (WakeupException e) {
                logger.info("Received Shutdown Signal!");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutdown() {
            consumer.wakeup();
        }
    }
}
