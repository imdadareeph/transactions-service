package com.ia.vt.transactions_service.config;

import com.ia.vt.transactions_service.benchmark.BenchmarkMetrics;
import com.ia.vt.transactions_service.benchmark.PerformanceLogger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
		"com.ia.vt.transactions_service.handler.vt",
		"com.ia.vt.transactions_service.router.vt"
})
@Import(VirtualThreadConfig.class)
public class VirtualThreadServerConfig {
	@Bean
	public PerformanceLogger virtualThreadPerformanceLogger(BenchmarkMetrics metrics) {
		return new PerformanceLogger(metrics, "Virtual Threads", "VT");
	}
}
