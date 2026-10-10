package ai.elaro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point for the Elaro intelligence broker. */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class ElaroApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElaroApplication.class, args);
    }
}
