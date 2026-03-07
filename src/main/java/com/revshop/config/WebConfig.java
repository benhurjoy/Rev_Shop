package com.revshop.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LogManager.getLogger(WebConfig.class);

    // uploadDir = "uploads/products"
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // FIX: uploadDir is "uploads/products", so its parent is "uploads/".
        // We map /uploads/** → the "uploads/" folder root so that
        // /uploads/products/filename.jpg correctly resolves to uploads/products/filename.jpg
        // Without this fix, the path was being doubled: uploads/products/products/filename.jpg
        Path uploadsRoot = Paths.get(uploadDir).getParent(); // → "uploads"
        String absolutePath = uploadsRoot.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");

        // Serve static assets (CSS, JS, images from classpath)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        logger.info("WebConfig: Serving uploads from absolute path: {}", absolutePath);
    }
}