package com.demo.ecommerce.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecommerce.config.JwtUtil;
import com.demo.ecommerce.exceptions.IncorrectCredentialsException;
import com.demo.ecommerce.models.JwtRequest;
import com.demo.ecommerce.models.Product;
import com.demo.ecommerce.models.User;
import com.demo.ecommerce.repo.ProductRepo;
import com.demo.ecommerce.services.UserAuthService;

@RestController
@RequestMapping("/api/public")
public class PublicController {

	@Autowired
	private UserAuthService userAuthService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private ProductRepo productRepo;
	    
    @GetMapping("/product/search")
    public List<Product> getProducts(@RequestParam(name = "keyword") String keyword) throws Exception {
    	System.out.println("keyword: "+keyword);
    	if(keyword==null) {
    		throw new Exception("No Search keyword.");
    	}
        return productRepo.findByProductNameContainingIgnoreCaseOrCategoryCategoryNameContainingIgnoreCase(keyword, keyword);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody JwtRequest jwtRequest) throws Exception {
        
    	System.out.println(jwtRequest);
    	
    	try {
    		// authenticates user
    		authenticationManager
    			.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword()));
    		
    	}catch(UsernameNotFoundException e) {
    		e.printStackTrace();
    		throw new Exception("User Not Found");
    	}catch(BadCredentialsException e) {
    		e.printStackTrace();
    		throw new IncorrectCredentialsException();
    	}
    	
    	// Generate token if user credentials are correct
    	User user = userAuthService.loadUserByUsername(jwtRequest.getUsername());
    	
    	// String token = jwtUtil.generateToken(user);
    	String token = jwtUtil.generateToken(user.getUsername());
    	
    	System.out.println("JWT Token: "+token);
    	
    	// return ResponseEntity.ok(new JwtResponse(token));
    	return ResponseEntity.ok(token);
    }
        
    // added to check added products
    @GetMapping("/product/all")
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }
    

}