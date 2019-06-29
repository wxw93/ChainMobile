package cn.bupt.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class KafkaProducer {
    public static void main(String[] args) {
        Properties properties = new Properties();
        ProducerConfig config = new ProducerConfig(properties);
        Producer<String, String> producer = new kafkaPro;


    }
}
