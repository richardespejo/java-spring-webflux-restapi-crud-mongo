package com.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	@Autowired
	private ProductoService productoService;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
				
				mongoTemplate.dropCollection("productos").subscribe();
				mongoTemplate.dropCollection("categorias").subscribe();
				
				Categoria gas = new Categoria("Gas");
				Categoria electrico = new Categoria("Electrico");
				Categoria hibrido = new Categoria("Hibrido");
				Categoria diesel = new Categoria("Diesel");
				
				Flux.just(gas , electrico , hibrido , diesel)
				.flatMap( categoria -> productoService.saveCategory(categoria))
				.doOnNext(cat -> {
					log.info("categoria creada: " + cat.getDescripcion() + " id " + cat.getId());
				}).thenMany(
						
						Flux.just( 	new Producto("Mustang Fireback 1965" , 12.000 , gas),
									new Producto("Camaro SS 1975" , 11.500 , gas),
									new Producto("Dodge Charge GT 1970" , 14.000 , gas),
									new Producto("Nissan Leaf" , 16.000 , electrico),
									new Producto("Nissan Qashqai" , 11.600 , electrico),
									new Producto("Tesla Model S" , 20.000 , electrico),
									new Producto("Toyota Prius 2016" , 10.600 , hibrido),
									new Producto("Hyunday Ioniq" , 17.200 , hibrido),
									new Producto("Mino Cooper" , 11.700 , diesel),
									new Producto("Ford Ranger" , 24.000 , diesel)			
								
								)
						.flatMap( producto -> {
							producto.setCreateAt(new Date());
							return productoService.save(producto);
							})
					)
					.subscribe( producto -> log.info("insert " + producto.getId() + " " + producto.getNombre() ));
			
				
				
		
	}

}
