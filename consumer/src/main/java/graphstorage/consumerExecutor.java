package graphstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ConsumerExecutor {

	@Autowired
	private ConsumerExample consumerExample;
	
	
	
//	@Autowired
//	private ProducerExample producer;

	public void execute() {
		String servers = "localhost:9200";
		String topic = "Demo3";

		consumerExample.consumeMessages(topic);
	
		
		
		//producer.produceMessages("hariharn", 10);
	}
}