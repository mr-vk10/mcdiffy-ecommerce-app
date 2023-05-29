package com.demo.ecommerce.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.demo.ecommerce.exceptions.IncorrectCredentialsException;
import com.demo.ecommerce.exceptions.ProductAlreadyPresentException;

@ControllerAdvice
public class MyExceptionHandler {

	// add Exception handling code here
    @ExceptionHandler
    public ResponseEntity<Object> handleException(ProductAlreadyPresentException exc){
        
        
        // return ResponseEntity
        return new ResponseEntity<>(HttpStatus.CONFLICT);        
    }
    
    // add Exception handling code here
    @ExceptionHandler
    public ResponseEntity<Object> handleException(IncorrectCredentialsException exc){
        
        
        // return ResponseEntity
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);        
    }
    
    // add another exception handler ... to catch any exception (catch all)
    @ExceptionHandler
    public ResponseEntity<Object> handleException(Exception exc){
                
        // return ResponseEntity
        return new ResponseEntity<>(exc,HttpStatus.BAD_REQUEST);        
    }
}
