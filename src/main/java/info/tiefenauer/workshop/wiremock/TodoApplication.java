package info.tiefenauer.workshop.wiremock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class TodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}


	@Configuration
	static class TodoApplicationConfig {

		@Bean
		public RestTemplateBuilder restTemplateBuilder() {
			return new RestTemplateBuilder()
				.connectTimeout(Duration.ofMillis(2000));

		}

		@Bean
		public RestTemplate restTemplate(RestTemplateBuilder builder) {
			return builder.build();
		}
	}
}
