package graphstorage;
//
//import org.apache.kafka.clients.producer.*;
//import org.springframework.stereotype.Service;
//
//import java.util.Properties;
//import java.util.Random;
//import java.util.UUID;
//
//@Service
//public class ProducerExample {
//    private final Producer<String, String> producer;
//    private final Random random = new Random();
//
//    public ProducerExample() {
//        Properties properties = new Properties();
//        properties.put("bootstrap.servers", "localhost:9092");
//        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//
//        this.producer = new KafkaProducer<>(properties);
//    }
//
//    public void produceMessages(String topic, int numMessages) {
//        for (int i = 0; i < numMessages; i++) {
//            String key = UUID.randomUUID().toString();
//            String value = "random-value-" + random.nextInt(100);
//
//            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
//
//            producer.send(record, new Callback() {
//                public void onCompletion(RecordMetadata metadata, Exception e) {
//                    if (e != null) {
//                        e.printStackTrace();
//                    } else {
//                        System.out.println("Sent message: (" + key + ", " + value + ") at offset " + metadata.offset());
//                    }
//                }
//            });
//        }
//
//        producer.close();
//    }
//}
