package com.ia.vt.transactions_service.router.vt;

import com.ia.vt.transactions_service.handler.vt.VirtualThreadReportHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.GET;

@Configuration
public class VirtualThreadReportRoutes {
	@Bean
	public RouterFunction<ServerResponse> reportRoutes(VirtualThreadReportHandler handler) {
		return RouterFunctions.route(GET("/reports/airline-booking"), handler::handle);
	}
}
