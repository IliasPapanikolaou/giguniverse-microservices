package com.unipi.giguniverse;

import com.unipi.giguniverse.config.SwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Import(SwaggerConfiguration.class)
//@EnableEurekaClient
@EnableDiscoveryClient
public class GiguniverseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiguniverseApplication.class, args);
	}

}
