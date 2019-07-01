package cn.bupt.kafka;

import org.apache.kafka.clients.producer.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class KafkaProducers {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        //bootstrap.servers
        props.setProperty("bootstrap.servers","master:9092,slaver1:9092,slaver2:9092");
        props.put("request.required.acks","1");
        props.put("zookeeper.connect","master:2181,slaver1:2181,slaver2:2181");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("F:\\sparkData\\CallLog\\flumeLoggerapp4.log.20170412")));
            String line = null;
            while(null != (line = reader.readLine())){
                Thread.sleep(100);
                producer.send(new ProducerRecord<String, String>("JsonData",line));
                System.out.println("line = " + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
