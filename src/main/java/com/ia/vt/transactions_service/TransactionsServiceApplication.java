package com.ia.vt.transactions_service;

import com.ia.vt.transactions_service.config.ParentContextConfig;
import com.ia.vt.transactions_service.config.VirtualThreadServerConfig;
import com.ia.vt.transactions_service.config.WebFluxServerConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class TransactionsServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext parentContext = new SpringApplicationBuilder(ParentContextConfig.class)
				.web(WebApplicationType.NONE)
				.run(args);

		new SpringApplicationBuilder(VirtualThreadServerConfig.class)
				.parent(parentContext)
				.web(WebApplicationType.SERVLET)
				.properties("server.port=9191")
				.run(args);

		new SpringApplicationBuilder(WebFluxServerConfig.class)
				.parent(parentContext)
				.web(WebApplicationType.REACTIVE)
				.properties("server.port=9292")
				.run(args);
	}

}
