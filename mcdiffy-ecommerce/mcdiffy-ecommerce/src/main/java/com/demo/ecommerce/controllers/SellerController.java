package com.demo.ecommerce.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.experimental.categories.Categories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.demo.ecommerce.config.JwtUtil;
import com.demo.ecommerce.models.Category;
import com.demo.ecommerce.models.Product;
import com.demo.ecommerce.models.Role;
import com.demo.ecommerce.models.User;
import com.demo.ecommerce.repo.CategoryRepo;
import com.demo.ecommerce.repo.ProductRepo;

@RestController
@RequestMapping("/api/auth/seller")
public class SellerController {

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/product")
	public ResponseEntity<Object> postProduct(HttpServletRequest request, @RequestBody Product product)
			throws URISyntaxException, Exception {

		String jwtToken = null;

		// get jwt token
		// String requestTokenHeader = request.getHeader("Authorization");
		String requestTokenHeader = request.getHeader("JWT");

		// if(requestTokenHeader==null || !requestTokenHeader.startsWith("Bearer ")) {
		if (requestTokenHeader == null) {

			throw new Exception("Invalid Token");
		}

		// jwtToken = requestTokenHeader.substring(7);
		jwtToken = requestTokenHeader;

		User seller = jwtUtil.getUser(jwtToken);

		product.setSeller(seller);

		Optional<Category> categoriesOpt = categoryRepo.findByCategoryName(product.getCategory().getCategoryName());

		if (!categoriesOpt.isPresent()) {
			throw new Exception("Category not found.");
		}

		product.setCategory(categoriesOpt.get());
		productRepo.save(product);

		// redirect url to get product that was inserted
		/*
		StringBuilder url = new StringBuilder();
		StringBuilder bearedToken = new StringBuilder();

		url.append("http://localhost:8000/api/auth/seller/product/" + product.getProductId());

		// bearedToken.append("Bearer " + jwtToken);
		bearedToken.append(jwtToken);

		URI sellerGetProd = new URI(url.toString());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(sellerGetProd);
		// httpHeaders.set("Authorization", bearedToken.toString());
		httpHeaders.set("JWT", bearedToken.toString());
		
		HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<Product> responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, requestEntity,
				Product.class);

		return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.CREATED);
		*/
		
		StringBuilder url = new StringBuilder();

		url.append("http://localhost:8000/api/auth/seller/product/" + product.getProductId());
		
		Map<String, String> response = new HashMap<>();
		
	    response.put("redirectedUrl", url.toString());
	    // return new ResponseEntity<>(response, HttpStatus.CREATED);
	    
	    return ResponseEntity.status(HttpStatus.CREATED).location(URI.create(url.toString())).build();

	}

	@GetMapping("/product")
	public ResponseEntity<Object> getAllProducts(HttpServletRequest request) throws Exception {

		String jwtToken = null;
		List<Product> products = null;
		// get jwt token
		// String requestTokenHeader = request.getHeader("Authorization");
		String requestTokenHeader = request.getHeader("JWT");

		// if(requestTokenHeader==null || !requestTokenHeader.startsWith("Bearer ")) {
		if (requestTokenHeader == null) {

			throw new Exception("Invalid Token");
		}

		// jwtToken = requestTokenHeader.substring(7);
		jwtToken = requestTokenHeader;

		User seller = jwtUtil.getUser(jwtToken);
		
		if(seller.getRoles().contains(Role.SELLER)) {
			products = productRepo.findBySellerUserId(seller.getUserId());
		}else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		return ResponseEntity.ok(products);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<Object> getProduct(HttpServletRequest request, @PathVariable int productId) throws Exception {

		String jwtToken = null;

		// get jwt token
		// String requestTokenHeader = request.getHeader("Authorization");
		String requestTokenHeader = request.getHeader("JWT");

		// if(requestTokenHeader==null || !requestTokenHeader.startsWith("Bearer ")) {
		if (requestTokenHeader == null) {

			throw new Exception("Invalid Token");
		}

		// jwtToken = requestTokenHeader.substring(7);
		jwtToken = requestTokenHeader;

		User seller = jwtUtil.getUser(jwtToken);

		Product product = null;

		Optional<Product> productOpt = productRepo.findBySellerUserIdAndProductId(seller.getUserId(), productId);

		if (productOpt.isPresent()) {
			product = productOpt.get();
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			// throw new Exception("Product not found.");
		}

		return ResponseEntity.ok(product);
	}

	@PutMapping("/product")
	public ResponseEntity<Object> putProduct(@RequestBody Product product) throws Exception {

		Product prodToUpdate = null;

		Optional<Product> prodToUpdateOpt = productRepo.findById(product.getProductId());

		if (product.getCategory() != null) {

			Optional<Category> categoriesOpt = categoryRepo.findByCategoryName(product.getCategory().getCategoryName());

			if (!categoriesOpt.isPresent()) {
				throw new Exception("Category not found.");
			}

			product.setCategory(categoriesOpt.get());
		}

		if (prodToUpdateOpt.isPresent()) {
			prodToUpdate = prodToUpdateOpt.get();

			// Update the necessary fields of the product object if they are present in the
			// request body
			Optional.ofNullable(product.getProductName()).ifPresent(prodToUpdate::setProductName);
			Optional.ofNullable(product.getPrice()).ifPresent(prodToUpdate::setPrice);
			Optional.ofNullable(product.getSeller()).ifPresent(prodToUpdate::setSeller);
			Optional.ofNullable(product.getCategory()).ifPresent(prodToUpdate::setCategory);

		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			// throw new Exception("Product not found");
		}

		productRepo.save(product);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/product/{productId}")
	public ResponseEntity<Product> deleteProduct(HttpServletRequest request, @PathVariable int productId)
			throws Exception {

		String jwtToken = null;

		// get jwt token
		// String requestTokenHeader = request.getHeader("Authorization");
		String requestTokenHeader = request.getHeader("JWT");

		// if(requestTokenHeader==null || !requestTokenHeader.startsWith("Bearer ")) {
		if (requestTokenHeader == null) {

			throw new Exception("Invalid Token");
		}

		// jwtToken = requestTokenHeader.substring(7);
		jwtToken = requestTokenHeader;

		User seller = jwtUtil.getUser(jwtToken);

		Optional<Product> productOpt = productRepo.findById(productId);

		Product product = productOpt.get();

		if (product.getSeller().getUserId() == seller.getUserId()) {
			productRepo.delete(product);
		} else {
			return new ResponseEntity<Product>(HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(product);
	}
}