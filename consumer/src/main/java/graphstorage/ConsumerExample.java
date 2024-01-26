package graphstorage;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
public class ConsumerExample {
	public String json = "{\r\n"
			+ "\"planCostShares\": {\r\n"
			+ "\"deductible\": 1000,\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"copay\": 23,\r\n"
			+ "\"objectId\": \"1234vxc2324sdf-501\",\r\n"
			+ "\"objectType\": \"membercostshare\"\r\n"
			+ "},\r\n"
			+ "\"linkedPlanServices\": [{\r\n"
			+ "\"linkedService\": {\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"objectId\": \"1234520xvc30asdf-502\",\r\n"
			+ "\"objectType\": \"service\",\r\n"
			+ "\"name\": \"Yearly physical\"\r\n"
			+ "},\r\n"
			+ "\"planserviceCostShares\": {\r\n"
			+ "\"deductible\": 10,\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"copay\": 0,\r\n"
			+ "\"objectId\": \"1234512xvc1314asdfs-503\",\r\n"
			+ "\"objectType\": \"membercostshare\"\r\n"
			+ "},\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"objectId\": \"27283xvx9asdff-504\",\r\n"
			+ "\"objectType\": \"planservice\"\r\n"
			+ "}, {\r\n"
			+ "\"linkedService\": {\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"objectId\": \"1234520xvc30sfs-505\",\r\n"
			+ "\"objectType\": \"service\",\r\n"
			+ "\"name\": \"well baby\"\r\n"
			+ "},\r\n"
			+ "\"planserviceCostShares\": {\r\n"
			+ "\"deductible\": 10,\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"copay\": 175,\r\n"
			+ "\"objectId\": \"1234512xvc1314sdfsd-506\",\r\n"
			+ "\"objectType\": \"membercostshare\"\r\n"
			+ "},\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"objectId\": \"27283xvx9sdf-507\",\r\n"
			+ "\"objectType\": \"planservice\"\r\n"
			+ "}],\r\n"
			+ "\"_org\": \"example.com\",\r\n"
			+ "\"objectId\": \"12xvxc345ssdsds-508\",\r\n"
			+ "\"objectType\": \"plan\",\r\n"
			+ "\"planType\": \"inNetwork\",\r\n"
			+ "\"creationDate\": \"12-12-2017\"\r\n"
			+ "}";

    private KafkaConsumer<String, String> consumer;
    
    @Autowired
    elastimaster elasti;

    public ConsumerExample() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group1");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(props);
    }

    public void consumeMessages(String topic) {
        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            records.forEach(record -> {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                try {
					elasti.receiveMessage(record.value());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            });
        }
    }
}
