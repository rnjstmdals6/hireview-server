package store.hireview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HireviewServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HireviewServerApplication.class, args);
    }

}
