package com.unipi.giguniverse;

import com.unipi.giguniverse.config.SwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableAsync
@Import(SwaggerConfiguration.class)
@EnableEurekaClient
public class GiguniverseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiguniverseApplication.class, args);
	}

	@Bean
	public ServerCodecConfigurer serverCodecConfigurer() {
		return ServerCodecConfigurer.create();
	}

	@Bean
	@LoadBalanced //Fetches client names from Discovery Server instead of using ip:port sockets
	public WebClient.Builder getWebClient(){
		return WebClient.builder();
	}

}
