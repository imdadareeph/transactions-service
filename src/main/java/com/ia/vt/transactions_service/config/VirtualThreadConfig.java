package com.ia.vt.transactions_service.config;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {
	@Bean(destroyMethod = "close")
	public ExecutorService virtualThreadExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

	@Bean
	public TomcatProtocolHandlerCustomizer<ProtocolHandler> tomcatProtocolHandlerCustomizer(ExecutorService virtualThreadExecutor) {
		return protocolHandler -> protocolHandler.setExecutor(virtualThreadExecutor);
	}
}
