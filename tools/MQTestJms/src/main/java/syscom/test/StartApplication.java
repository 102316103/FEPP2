package syscom.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class StartApplication {
	public static void main(String[] args) {
        new SpringApplicationBuilder(StartApplication.class) //
        .run(args);
	}
}