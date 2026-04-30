package com.wechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class MeetingTranscribeApplication {
  public static void main(String[] args) {
    SpringApplication.run(MeetingTranscribeApplication.class, args);
    System.out.println("会议录音转文字系统启动成功！");
  }

  @Configuration
  public static class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/uploads/**")
          .addResourceLocations("file:/opt/wechat_meeting/uploads/");

      // 提供前端静态文件
      registry.addResourceHandler("/**")
          .addResourceLocations("classpath:/static/")
          .resourceChain(true)
          .addResolver(new PathResourceResolver() {
            @Override
            protected Resource getResource(String resourcePath, Resource location) throws IOException {
              Resource requestedResource = location.createRelative(resourcePath);
              return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
                  : new ClassPathResource("/static/index.html");
            }
          });
    }
  }
}
