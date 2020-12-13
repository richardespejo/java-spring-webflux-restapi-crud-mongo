package com.springboot.webflux.app.handler;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Autowired
	private ProductoService serviceProd; 
	
	@Value("${C:\\Users\\richa\\Documents\\workspace-spring-tool-suite-4-4.8.1.RELEASE\\uploads\\}")
	private String filePath;
	
	@Autowired
	private Validator validator;
	
	public Mono<ServerResponse> handlerList(ServerRequest request) {
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(serviceProd.findAll() , Producto.class );
	}
	
	
	public Mono<ServerResponse> handlerDetail(ServerRequest request){		
		String id = request.pathVariable("id");		
		return serviceProd.findById(id).flatMap(prod -> ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(prod)))
				.switchIfEmpty(ServerResponse.notFound().build());				
	}
	
	public Mono<ServerResponse> handlerCreate(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		
		return producto.flatMap(prod -> {
			
			Errors errors = new BeanPropertyBindingResult(prod, Producto.class.getName());
			validator.validate(prod, errors);
			
			if(errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo "+ fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromValue(list)));
			}else {
				if(prod.getCreateAt() == null) {
					prod.setCreateAt(new Date());
				}			
				return serviceProd.save(prod).flatMap(pdb -> ServerResponse
						.created(URI.create("/api/routefun/coches/".concat(pdb.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(pdb)));
			}
			
			

		
		});
		
	}
	
	
	public Mono<ServerResponse> handlerEdit(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");		
		
		Mono<Producto> productoDB = serviceProd.findById(id);
				
		return productoDB.zipWith(producto , (db , req) -> {
			db.setNombre(req.getNombre());
			db.setPrecio(req.getPrecio());
			db.setCategoria(req.getCategoria());
			
			return db;
		}).flatMap(prod -> ServerResponse.created(URI.create("/api/routefun/coches/".concat(prod.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(serviceProd.save(prod) , Producto.class))
				.switchIfEmpty(ServerResponse.notFound().build());
		
	}
	
	public Mono<ServerResponse> handlerDalete(ServerRequest request){
		String id = request.pathVariable("id");		
		
		Mono<Producto> producto = serviceProd.findById(id);
		
		return producto.flatMap(prod -> serviceProd.delete(prod).then(ServerResponse.noContent().build() ))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> handlerUpload(ServerRequest request) {
		String id = request.pathVariable("id");		
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
			.cast(FilePart.class)
			.flatMap(file -> serviceProd.findById(id)
					.flatMap(prod -> {
						prod.setImagen(UUID.randomUUID().toString() +"-"+ file.filename()
						.replace(" ","-")
						.replace(":","")
						.replace("\\",""));						
						return file.transferTo( new File(filePath + prod.getImagen())).then( serviceProd.save(prod));								
					
					})).flatMap( prod -> ServerResponse.created(URI.create("/api/routefun/coches/".concat(prod.getId())))
							.contentType(MediaType.APPLICATION_JSON)
							.body(BodyInserters.fromValue(prod)))
						.switchIfEmpty(ServerResponse.notFound().build());

	}
	
	public Mono<ServerResponse> handlerCreateUpload(ServerRequest request) {
		
		Mono<Producto> producto = request.multipartData().map(multipart -> {
			FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart cetegoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");
			
			Categoria categoria = new Categoria(categoriaNombre.value());
			categoria.setId(cetegoriaId.value());
			return new Producto( nombre.value() , Double.parseDouble(precio.value()) , categoria );
			
		});  
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
			.cast(FilePart.class)
			.flatMap(file -> producto 
					.flatMap(prod -> {
						prod.setImagen(UUID.randomUUID().toString() +"-"+ file.filename()
						.replace(" ","-")
						.replace(":","")
						.replace("\\",""));		
						prod.setCreateAt(new Date());
						return file.transferTo( new File(filePath + prod.getImagen())).then( serviceProd.save(prod));								
					
					})).flatMap( prod -> ServerResponse.created(URI.create("/api/routefun/coches/".concat(prod.getId())))
							.contentType(MediaType.APPLICATION_JSON)
							.body(BodyInserters.fromValue(prod)));
						
	}
	
	
}
