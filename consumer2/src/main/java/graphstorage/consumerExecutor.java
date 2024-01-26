package graphstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ConsumerExecutor {


	
	@Autowired
	private ConsumerDelete consumerdelete;
	
//	@Autowired
//	private ProducerExample producer;

	public void execute() {

		consumerdelete.consumeMessages();
		
		
		//producer.produceMessages("hariharn", 10);
	}
}