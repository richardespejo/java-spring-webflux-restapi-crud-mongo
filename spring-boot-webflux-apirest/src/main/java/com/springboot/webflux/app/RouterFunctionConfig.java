package com.springboot.webflux.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.app.handler.ProductoHandler;


import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class RouterFunctionConfig {

	/*
	 * Desacoplamiento por rutas
	 * Referencia a la clase ProductoHandler 
	 * */	
	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler){
		
		//Esto indica que dos o mas rutas pueden acceder al mismo handle o servicio
		return route( GET("/api/routefun/coches").or(GET("/api/v2/coches")) , request -> handler.handlerList(request))
				.andRoute(GET("/api/routefun/coches/{id}"), request -> handler.handlerDetail(request))
				.andRoute(POST("/api/routefun/coches"), request -> handler.handlerCreate(request))
				.andRoute(PUT("/api/routefun/coches/{id}"), request -> handler.handlerEdit(request))
				.andRoute(DELETE("/api/routefun/coches/{id}"), request -> handler.handlerDalete(request))
				.andRoute(POST("/api/routefun/coches/upload/{id}"), request -> handler.handlerUpload(request))
				.andRoute(POST("/api/routefun/coches/createupload"), request -> handler.handlerCreateUpload(request));


	}
}
