package com.appdeveloperblog.app.ws.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebSecurityConfig {

	
	
	public boolean verifier(String token) {
	    try {
	        String[] strings = token.split(" ");
	        return verify(strings[1]);
	    } catch (Exception e) {
	        System.out.println("Validation failed: " + e);
	        return false;
	    }
	}

	private ResponseEntity<String> getCall(String url) throws RestClientException {
	    RestTemplate restTemplate = new RestTemplate();
	    return restTemplate.getForEntity(url, String.class);
	}

	private boolean verify(String token) {
	    if (token == null || token.isEmpty()) {
	        System.out.println("Token is null or empty");
	        return false;
	    }
	    
	    try {
	        String url = "https://oauth2.googleapis.com/tokeninfo?access_token=" + token;
	        ResponseEntity<String> response = getCall(url);
	        
	        if(response != null) {
	            return response.getStatusCode() == HttpStatus.OK;
	        } else {
	            System.out.println("Response is null");
	            return false;
	        }
	        
	    } catch (RestClientException e) {
	        System.out.println("Error while verifying token: " + e.getMessage());
	        return false;
	    }
	}




}
