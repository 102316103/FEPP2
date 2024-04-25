package syscom.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MqService implements CommandLineRunner {
    @Value("${ibm.mq.mod}")
    private String mod;

    @Autowired
    private MqMessage mqMessageSender;

	@Override
	public void run(String... args) throws Exception {
		if("send".equals(mod)) {
			this.mqMessageSender.sendMessage();
		} else {
			this.mqMessageSender.receiveMessage();
		}
	}
}
