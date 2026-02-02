package com.ia.vt.transactions_service.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
		"com.ia.vt.transactions_service.service",
		"com.ia.vt.transactions_service.benchmark"
})
@ConfigurationPropertiesScan(basePackages = "com.ia.vt.transactions_service.benchmark")
public class ParentContextConfig {
}
