package com.springboot.webflux.app.models.services;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

	public Flux<Producto> findAll();
	
	public Flux<Producto> findAllNameUpperCase();
	
	public Flux<Producto> findAllNameUpperCaseRepeat();
	
	public Mono<Producto> findById(String id);
	
	public Mono<Producto> save(Producto prodcuto);
	
	public Mono<Void> delete(Producto producto);
	
	public Flux<Categoria> findAllCategory();
	
	public Mono<Categoria> findAllCategorybyId(String id);
	
	public Mono<Categoria> saveCategory(Categoria categoria);	
	
	
}
