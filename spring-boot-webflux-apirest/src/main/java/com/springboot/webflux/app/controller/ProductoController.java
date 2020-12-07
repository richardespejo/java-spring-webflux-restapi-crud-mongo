package com.springboot.webflux.app.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/coches")
public class ProductoController {

	@Autowired
	private ProductoService serviceProd;
	
	@Value("${C:\\Users\\richa\\Documents\\workspace-spring-tool-suite-4-4.8.1.RELEASE\\uploads\\}")
	private String filePath;
	
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Producto>>> list(){
		return Mono.just(
				ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(serviceProd.findAll())
				);
	}
	
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> detail( @PathVariable String id){
		
		return serviceProd.findById(id).map(prod -> ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(prod))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String,Object>>> save(@Valid @RequestBody Mono<Producto> monoProducto){
		
		Map<String , Object> respuesta = new HashMap<String , Object>();
		
		return monoProducto.flatMap( producto -> {
			if(producto.getCreateAt()==null) {
				producto.setCreateAt(new Date());
			}	

			return serviceProd.save(producto).map(prod -> {
				respuesta.put("coche ", prod);
				respuesta.put("message ", "Coche creado con exito");
				return ResponseEntity
					.created(URI.create("/api/coches/".concat(prod.getId())))
					.contentType(MediaType.APPLICATION_JSON)
					.body(respuesta);
			});	
		
		}).onErrorResume( trow -> {
			return Mono.just(trow).cast(WebExchangeBindException.class)
						.flatMap(err -> Mono.just(err.getFieldErrors()))
						.flatMapMany(Flux::fromIterable)
						.map( fieldError -> "El campo "+ fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> {
							respuesta.put("errors" , list);
							respuesta.put("timestamp" , new Date());
							respuesta.put("status" , HttpStatus.BAD_REQUEST.value());
							return Mono.just(ResponseEntity.badRequest().body(respuesta));
						});
		});
		
		
			
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Producto>> edit(@RequestBody Producto producto , @PathVariable String id){
		
		return serviceProd.findById(id).flatMap(prod -> {			
			prod.setNombre(producto.getNombre());
			prod.setPrecio(producto.getPrecio());
			prod.setCategoria(producto.getCategoria());			
			return serviceProd.save(prod);
		}).map( prod -> ResponseEntity.created(URI.create("/api/coches/".concat(prod.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(prod))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
		return serviceProd.findById(id).flatMap( prod -> {
			return serviceProd.delete(prod).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}	

	//UPLOAD FILE OR IMAGE
	@PostMapping("/uploads/{id}")
	public Mono<ResponseEntity<Producto>> upload(@PathVariable String id , @RequestPart FilePart file){
		return serviceProd.findById(id).flatMap( prod -> {
			prod.setImagen(UUID.randomUUID().toString()+ "-" + file.filename()
			.replace(" ", "")
			.replace(":", "")
			.replace("\\", ""));
			
			return file.transferTo(new File(filePath + prod.getImagen())).then(serviceProd.save(prod));
		}).map( prod -> ResponseEntity.ok(prod)).defaultIfEmpty(ResponseEntity.notFound().build());
		
	}	

	//CREATE AND UPLOAD FILE COMPLETE VERSION to test this endpoint set body to form-data and set the var params like image
	@PostMapping("/createandupload")
	public Mono<ResponseEntity<Producto>> saveWithUpload(Producto producto , @RequestPart FilePart image){
		
		if(producto.getCreateAt()==null) {
			producto.setCreateAt(new Date());
		}
		producto.setImagen(UUID.randomUUID().toString()+ "-" + image.filename()
		.replace(" ", "")
		.replace(":", "")
		.replace("\\", ""));
		
		return image.transferTo(new File(filePath + producto.getImagen())).then(serviceProd.save(producto))
				.map(prod -> ResponseEntity
				.created(URI.create("/api/coches/".concat(prod.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(prod)
				);		
	}
	
}
