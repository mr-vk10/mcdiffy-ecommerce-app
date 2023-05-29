package com.demo.ecommerce.models;

/*
 * created new class
 * used as Res obj while logging in
 */

public class JwtResponse {

	String token;

	public JwtResponse() {
	}

	public JwtResponse(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "JwtResponse [token=" + token + "]";
	}
	
}
