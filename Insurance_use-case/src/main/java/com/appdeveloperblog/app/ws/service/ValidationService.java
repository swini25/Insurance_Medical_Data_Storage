package com.appdeveloperblog.app.ws.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Configuration
@Service
public class ValidationService {
	   private final JsonSchema schema;

	    public ValidationService() throws IOException, ProcessingException {
	        String schemaText = new String(Files.readAllBytes(Paths.get(new ClassPathResource("data-schema.json").getURI())));
	        JsonNode schemaNode = new ObjectMapper().readTree(schemaText);
	        this.schema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
	    }

	    public List<String> validate(ObjectNode object) throws ProcessingException {
	        ProcessingReport report = schema.validate(object);
	        List<String> errors = new ArrayList<>();
	        for (ProcessingMessage message : report) {
	            if (message.getLogLevel() == LogLevel.ERROR) {
	                String field = message.asJson().get("instance").get("pointer").asText();
	                String expectedType = message.asJson().get("expected").get(0).asText();
	                String receivedType = message.asJson().get("found").asText();
	                errors.add(String.format("Error -> Expected type %s but received type %s for parameter %s", expectedType, receivedType, field));
	            }
	        }
        return errors;
    }
}
