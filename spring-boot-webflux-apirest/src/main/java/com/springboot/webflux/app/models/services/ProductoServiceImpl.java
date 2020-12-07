package com.springboot.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.webflux.app.models.dao.CategoriaDao;
import com.springboot.webflux.app.models.dao.ProductoDao;
import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

	@Autowired
	private CategoriaDao daoCategoria;
	
	@Autowired
	private ProductoDao daoProducto;
	
	@Override
	public Flux<Producto> findAll() {
		return daoProducto.findAll();
	}
	
	@Override
	public Flux<Producto> findAllNameUpperCase() {
		return daoProducto.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});
	}

	@Override
	public Flux<Producto> findAllNameUpperCaseRepeat() {
		return daoProducto.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().repeat(5000));
			return producto;
		});
	}
	
	@Override
	public Mono<Producto> findById(String id) {
		return daoProducto.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return daoProducto.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		return daoProducto.delete(producto);
	}

	@Override
	public Flux<Categoria> findAllCategory() {
		return daoCategoria.findAll();
	}

	@Override
	public Mono<Categoria> findAllCategorybyId(String id) {
		return daoCategoria.findById(id);
	}

	@Override
	public Mono<Categoria> saveCategory(Categoria categoria) {
		return daoCategoria.save(categoria);
	}





}
