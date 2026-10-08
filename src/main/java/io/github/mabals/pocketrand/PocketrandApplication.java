package io.github.mabals.pocketrand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PocketrandApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocketrandApplication.class, args);
	}

}
