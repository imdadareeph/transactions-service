package com.ia.vt.transactions_service.router.wf;

import com.ia.vt.transactions_service.handler.wf.WebFluxReportHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class WebFluxReportRoutes {
	@Bean
	public RouterFunction<ServerResponse> reportRoutes(WebFluxReportHandler handler) {
		return RouterFunctions.route(GET("/reports/airline-booking"), handler::handle);
	}
}
