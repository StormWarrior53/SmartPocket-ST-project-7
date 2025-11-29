package org.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		// Load variables from .env (if present) into System properties so Spring can resolve ${VAR}
		try {
			Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();
			String[] keys = new String[]{"DB_HOST","DB_PORT","DB_NAME","DB_USERNAME","DB_PASSWORD","SERVER_PORT","CORS_ALLOWED_ORIGINS","DB_URL"};
			for (String key : keys) {
				String val = dotenv.get(key);
				if (val != null && System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, val);
				}
			}
		} catch (Throwable t) {
			System.err.println("Warning: failed to load .env - " + t.getMessage());
		}
		SpringApplication.run(ServerApplication.class, args);
	}

}
