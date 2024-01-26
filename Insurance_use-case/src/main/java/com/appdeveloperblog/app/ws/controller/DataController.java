package com.appdeveloperblog.app.ws.controller;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import com.appdeveloperblog.app.ws.models.CustomMessage;
import com.appdeveloperblog.app.ws.models.Data;
import com.appdeveloperblog.app.ws.repository.Datarepository;
import com.appdeveloperblog.app.ws.service.Insurance_service;
import com.appdeveloperblog.app.ws.service.ValidationService;
import com.appdeveloperblog.app.ws.utils.JwtUtil;
import com.appdeveloperblog.app.ws.utils.WebSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.kafka.core.KafkaTemplate;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/data")
public class DataController {
	
	@Autowired
	private JwtUtil jwtUtil;

    @Autowired
    private Datarepository dataRepository;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    WebSecurityConfig webSecurityConfig;
    
    @Autowired
    Insurance_service insurance_service;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
   
    
    
    private String generateETag(Object object) {
        return DigestUtils.md5DigestAsHex(object.toString().getBytes());
    }
    
    @GetMapping("/getToken")
	public ResponseEntity<?> generateToken() {
    	System.out.println("Inside getToken ##################");
		String token = jwtUtil.generateToken();
		return ResponseEntity.status(HttpStatus.CREATED).body(token);
	}

    

	@PostMapping("/validate")
	public boolean validateToken(@RequestHeader HttpHeaders requestHeader) throws Exception {
		boolean isValid;
		String authorization = requestHeader.getFirst("Authorization");
		if (authorization == null || authorization.isBlank())
			throw new Exception("Missing token!");
		try {
			String token = authorization.split(" ")[1];
			isValid = jwtUtil.validateToken(token);
		} catch (Exception e) {
			return false;
		}
		return isValid;
	}


    @PostMapping("/save")
    public ResponseEntity<?> createData(@RequestHeader("Authorization") String token, @RequestBody String payload) {
        if (!webSecurityConfig.verifier(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            JSONObject jsonObject = new JSONObject(payload);
            String objectType = jsonObject.getString("objectType");
            String objectId = jsonObject.getString("objectId");
            if (objectType == null || objectId == null) {
                return ResponseEntity.badRequest().body("objectType or objectId missing");
            }
            String currentKey = objectType + ":" + objectId;

          ObjectMapper objectMapper  = new ObjectMapper();
		try {
			  JsonNode jsonNode = objectMapper.readTree(payload);
			  ObjectNode objectNode = (ObjectNode) jsonNode;

	          // Validate incoming data.
	          List<String> errors = validationService.validate(objectNode);
	          if (!errors.isEmpty()) {
	              return ResponseEntity.badRequest().body(errors);
	          }
		} catch (Exception e) {
			
			System.out.println("error validation");
			e.printStackTrace();
		}


            // No validation errors; proceed with processing...
            insurance_service.parseAndSaveData(payload);
            
            // Send payload to Kafka topic
//            CustomMessage payload1 = new CustomMessage(payload, "SAVE");
//            kafkaTemplate.send("Demo3", payload1);

            kafkaTemplate.send("Demo3", payload);
            
            Optional<Data> saved = dataRepository.findById(currentKey);
            JsonNode objectNode2 = objectMapper.readTree(saved.get().getObject().toString());
            String eTag = generateETag(objectNode2);

            return ResponseEntity.status(201).eTag(eTag).body("objectId : " + currentKey);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unhandled errors, contact admin");
        }
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getData(@RequestHeader("Authorization") String token, @PathVariable String id, HttpServletRequest request) throws JsonMappingException, JsonProcessingException {
        if (!webSecurityConfig.verifier(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<Data> retrievedData = dataRepository.findById(id);
        if (!retrievedData.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode objectNode2 = objectMapper.readTree(retrievedData.get().getObject().toString());

        String eTag = generateETag(objectNode2);
        
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch == null || ifNoneMatch.isEmpty()) {
            JsonNode objectNode = null;
            try {
                objectNode = objectMapper.readTree(retrievedData.get().getObject().toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing JSON");
            }
            return ResponseEntity.status(HttpStatus.OK).eTag(eTag).body(objectNode);
        }

        if (eTag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        JsonNode objectNode1 = objectMapper.readTree(retrievedData.get().getObject().toString());
        String eTag1 = generateETag(objectNode1);
        
        return ResponseEntity.status(HttpStatus.OK).eTag(eTag1).body(objectNode1);
    }


    
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateData(@RequestHeader("Authorization") String token, @PathVariable String id, @RequestBody String payload, HttpServletRequest request) {
        if (!webSecurityConfig.verifier(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            Optional<Data> existingData = dataRepository.findById(id);

            if (!existingData.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Object with id " + id + " not found");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode objectNode2 = objectMapper.readTree(existingData.get().getObject().toString());

            String eTag = generateETag(objectNode2);
            String ifMatch = request.getHeader("If-Match");
            if(!eTag.equals(ifMatch) || eTag.isEmpty()) {
            	return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("etag mismatch");
            }
      	  JsonNode jsonNode = objectMapper.readTree(payload);
		  ObjectNode objectNode = (ObjectNode) jsonNode;

          List<String> errors = validationService.validate(objectNode);
          if (!errors.isEmpty()) {
              return ResponseEntity.badRequest().body(errors);
          }

          insurance_service.handleUpdatePlan(id, jsonNode);
          
	       // Send payload to Kafka topic
	       kafkaTemplate.send("Demo3", payload);
          
//       // Send payload to Kafka topic
//          CustomMessage payload2 = new CustomMessage(payload, "SAVE");
//          kafkaTemplate.send("Demo3", payload2);
          
            Optional<Data> existingData1 = dataRepository.findById(id);
            JsonNode objectNode3 = objectMapper.readTree(existingData1.get().getObject().toString());

            String eTag1 = generateETag(objectNode3);

            return ResponseEntity.ok().eTag(eTag1).body("Data updated successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unhandled errors, contact admin");
        }
    }
    
    
    @PutMapping("/{id}")
    public ResponseEntity<?> deleteAndSaveData(@RequestHeader("Authorization") String token, @PathVariable("id") String id, @RequestBody String payload, HttpServletRequest request) {
        if (!webSecurityConfig.verifier(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            JSONObject jsonObject = new JSONObject(payload);
            String objectType = jsonObject.getString("objectType");
            String objectId = jsonObject.getString("objectId");
            if (objectType == null || objectId == null) {
                return ResponseEntity.badRequest().body("objectType or objectId missing");
            }
            String currentKey = objectType + ":" + objectId;

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(payload);
                ObjectNode objectNode = (ObjectNode) jsonNode;

                // Validate incoming data.
                List<String> errors = validationService.validate(objectNode);
                if (!errors.isEmpty()) {
                    return ResponseEntity.badRequest().body(errors);
                }
            } catch (Exception e) {
                System.out.println("Error during validation");
                e.printStackTrace();
            }

            Optional<Data> existingData = dataRepository.findById(id);

            if (!existingData.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Object with id " + id + " not found");
            }

            JsonNode objectNode2 = objectMapper.readTree(existingData.get().getObject().toString());

            String eTag = generateETag(objectNode2);
            String ifMatch = request.getHeader("If-Match");
            if (!eTag.equals(ifMatch) || eTag.isEmpty()) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("ETag mismatch");
            }

            // Delete all data from the database
            dataRepository.deleteAll();

            // Parse and save new data
            insurance_service.parseAndSaveData(payload);

            return ResponseEntity.status(200).body("Data updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unhandled errors occurred, please contact the admin");
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteData(@RequestHeader("Authorization") String token, @PathVariable String id) {
    	if (!webSecurityConfig.verifier(token)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
	    }
        if (!dataRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Optional<Data> existingData = dataRepository.findById(id);
        String payload = (String) existingData.get().getObject();
     // Send payload to Kafka topic
	    kafkaTemplate.send("DEMO3delete", payload);
        
     // Send payload to Kafka topic
//        CustomMessage payload2 = new CustomMessage(payload, "DELETE");
//        kafkaTemplate.send("Demo3", payload2);
        
        System.out.println("-----"+payload+"-----");
        dataRepository.deleteById(id);
        
        return ResponseEntity.ok().build();
    }
}
