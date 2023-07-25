package com.mttk.lowcode.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mttk.lowcode.backend.config.util.DocumentMessageConverter;

@Configuration
@EnableWebMvc

public class WebConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(java.util.List<HttpMessageConverter<?>> converters) {
		converters.add(0, messageConverter());
	}

	@Bean
	public DocumentMessageConverter messageConverter() {
		return new DocumentMessageConverter();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
				.allowCredentials(false).maxAge(3600);
	}

//	@Autowired
//	TestInterceptor testInterceptor;
//
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//		// 没有设置基本登录
//		registry.addInterceptor(testInterceptor).addPathPatterns("/**");
//	}

}
