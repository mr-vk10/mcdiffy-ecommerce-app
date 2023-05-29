package com.demo.ecommerce.controllers;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecommerce.config.JwtUtil;
import com.demo.ecommerce.exceptions.ProductAlreadyPresentException;
import com.demo.ecommerce.models.Cart;
import com.demo.ecommerce.models.CartProduct;
import com.demo.ecommerce.models.Product;
import com.demo.ecommerce.models.Role;
import com.demo.ecommerce.models.User;
import com.demo.ecommerce.repo.CartProductRepo;
import com.demo.ecommerce.repo.CartRepo;
import com.demo.ecommerce.repo.ProductRepo;

@RestController
@RequestMapping("/api/auth/consumer")
public class ConsumerController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private CartProductRepo cartProductRepo;

	@Autowired
	private ProductRepo productRepo;

	@GetMapping("/cart")
	public ResponseEntity<Object> getCart(HttpServletRequest request) throws Exception {

		User user = null;

		String username = null;

		Optional<Cart> cart = null;

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

		try {

			// username = jwtUtil.extractUsername(jwtToken);

			user = jwtUtil.getUser(jwtToken);
			
			if(!user.getRoles().contains(Role.CONSUMER)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}

			if (user != null) {
				username = user.getUsername();
			}

			cart = cartRepo.findByUserUsername(username);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok(cart.get());
	}

	@PostMapping("/cart")
	public ResponseEntity<Object> postCart(HttpServletRequest request, @RequestBody Product product) throws Exception {

		User user = null;

		String username = null;

		Optional<Cart> cartOptional = null;

		Cart cart = null;

		List<CartProduct> cartProducts = null;

		CartProduct cartProduct = new CartProduct();

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

		try {

			// username = jwtUtil.extractUsername(jwtToken);

			user = jwtUtil.getUser(jwtToken);

			if (user != null) {
				username = user.getUsername();
			}

			cartOptional = cartRepo.findByUserUsername(username);

			if (cartOptional.isPresent()) {
				cart = cartOptional.get();
			}

			cartProducts = (List<CartProduct>) cart.getCartProducts();

			for (CartProduct cartProd : cartProducts) {
				if (cartProd.getProduct().getProductId() == product.getProductId()) {
					throw new ProductAlreadyPresentException();
					// return new ResponseEntity<>(HttpStatus.CONFLICT);
				}
			}

			cartProduct.setProduct(product);
			cartProduct.setCart(cart);
			cartProductRepo.save(cartProduct);

		} catch (ProductAlreadyPresentException e) {
			throw new ProductAlreadyPresentException(e);
		}catch (Exception e) {
			throw new Exception(e);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("/cart")
	public ResponseEntity<Object> putCart(HttpServletRequest request, @RequestBody CartProduct cartProduct)
			throws Exception {

		// fetch user based on token

		// fetch cart from user

		// fetch cart product from cart

		User user = null;

		String username = null;

		Optional<Cart> cartOptional = null;

		Cart cart = null;

		List<CartProduct> cartProducts = null;

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

		try {

			// username = jwtUtil.extractUsername(jwtToken);

			user = jwtUtil.getUser(jwtToken);

			if (user != null) {
				username = user.getUsername();
			}

			cartOptional = cartRepo.findByUserUsername(username);

			if (cartOptional.isPresent()) {
				cart = cartOptional.get();
			}

			cartProducts = (List<CartProduct>) cart.getCartProducts();
			/*
			 * for(CartProduct cartProd: cartProducts) {
			 * // if product is alreadyt present then update the quantity
			 * if(cartProd.getProduct().getProductId() ==
			 * cartProduct.getProduct().getProductId()) {
			 * cartProductToAdd = cartProd;
			 * }
			 * }
			 */

			Optional<CartProduct> cartProductToAddOpt = cartProducts.stream()
					.filter(cartProd -> cartProd.getProduct().getProductId()
							.equals(cartProduct.getProduct().getProductId()))
					.findFirst();

			if (cartProductToAddOpt.isPresent()) {
				// Product already present, update the quantity
				cartProductToAddOpt.get().setQuantity(cartProduct.getQuantity());
			} else if (cartProduct.getQuantity() != 0) {
				// Product not present and quantity is not 0, add it to the cart

				Optional<Product> productOpt = productRepo.findById(cartProduct.getProduct().getProductId());

				if (productOpt.isPresent()) {
					cartProductToAddOpt = Optional.of(new CartProduct());
					cartProductToAddOpt.get().setProduct(productOpt.get());
					cartProductToAddOpt.get().setCart(cart);
					cartProductToAddOpt.get().setQuantity(cartProduct.getQuantity());
				}

			}

			if (!cartProductToAddOpt.isPresent() && cartProduct.getQuantity() == 0) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
			if (cartProductToAddOpt.get().getQuantity() == 0) {
				// if quantity is 0 delete the product from cart
				cartProductRepo.deleteById(cartProductToAddOpt.get().getCpId());
			} else {
				cartProductRepo.save(cartProductToAddOpt.get());
			}
		} catch (Exception e) {
			throw new Exception(e);
		}

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@DeleteMapping("/cart")
	public ResponseEntity<Object> deleteCart(HttpServletRequest request, @RequestBody Product product)
			throws Exception {

		// fetch user based on token

		// fetch cart from user

		// fetch cart product from cart

		User user = null;

		String username = null;

		Optional<Cart> cartOptional = null;

		Cart cart = null;

		List<CartProduct> cartProducts = null;

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

		try {

			// username = jwtUtil.extractUsername(jwtToken);

			user = jwtUtil.getUser(jwtToken);

			if (user != null) {
				username = user.getUsername();
			}

			cartOptional = cartRepo.findByUserUsername(username);

			if (cartOptional.isPresent()) {
				cart = cartOptional.get();
			}

			cartProducts = (List<CartProduct>) cart.getCartProducts();
			/*
			 * for(CartProduct cartProd: cartProducts) {
			 * // if product is already present then update the quantity
			 * if(cartProd.getProduct().getProductId() ==
			 * cartProduct.getProduct().getProductId()) {
			 * cartProductToAdd = cartProd;
			 * }
			 * }
			 */

			Optional<CartProduct> cartProductToDelOpt = cartProducts.stream()
					.filter(cartProd -> cartProd.getProduct().getProductId()
							.equals(product.getProductId()))
					.findFirst();

			if (cartProductToDelOpt.isPresent()) {
				// Product already present, update the quantity
				cartProductRepo.deleteById(cartProductToDelOpt.get().getCpId());
			}

		} catch (Exception e) {
			throw new Exception(e);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

}