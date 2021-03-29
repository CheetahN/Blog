package main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Value("${upload.path}")
    private String uploadPath;
    @Value("${upload.url.label}")
    private String uploadUrlLabel;

//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login");
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadUrlLabel + "/**")
                .addResourceLocations("file:///" + uploadPath + "/");
    }
}