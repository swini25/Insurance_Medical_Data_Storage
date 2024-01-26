package graphstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;


@SpringBootApplication
public class ConsumerApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ConsumerApplication.class, args);
		

		ConsumerExecutor consumerExecutor = context.getBean(ConsumerExecutor.class);
		consumerExecutor.execute();
	}

}
