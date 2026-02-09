package LogiTrack;

import jakarta.persistence.Entity;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication

@EnableScheduling
@EnableJpaAuditing
@EnableAsync
public class LogiTrackBackendSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(LogiTrackBackendSystemApplication.class, args);
	}
}
