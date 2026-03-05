package com.revshop.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class AppConfig {

    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    public static void loadDotenv() {
        logger.info("Loading environment variables from .env file");

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        for (DotenvEntry entry : dotenv.entries()) {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
                logger.debug("Loaded env variable: {}", entry.getKey());
            }
        }

        logger.info(".env variables loaded successfully");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Creating BCryptPasswordEncoder bean");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        logger.info("Creating ModelMapper bean");
        return new ModelMapper();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        logger.info("Creating MultipartResolver bean for file uploads");
        return new StandardServletMultipartResolver();
    }
}