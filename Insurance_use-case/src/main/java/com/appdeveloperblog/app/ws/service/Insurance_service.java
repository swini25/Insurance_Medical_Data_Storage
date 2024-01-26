package com.appdeveloperblog.app.ws.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdeveloperblog.app.ws.models.Data;
import com.appdeveloperblog.app.ws.repository.Datarepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class Insurance_service {

    @Autowired
    Datarepository dataRepository;

    private static Map<String, Object> resultMap = new HashMap<>(); 

    public void parseAndSaveData(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        savePlan(jsonObject);
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Data newdata = new Data(value);
            newdata.setId(key);
            Data savedData = dataRepository.save(newdata);
        }
    }

    private static String savePlan(JSONObject jsonObject) {
        String objectId = jsonObject.getString("objectId");
        String objectType = jsonObject.getString("objectType");
        String key = objectType + ":" + objectId;

        jsonObject.keySet().forEach(jsonKey -> {
            Object jsonValue = jsonObject.get(jsonKey.toString());
            if(jsonValue instanceof Number || jsonValue instanceof String){
                resultMap.put(key, jsonObject.toString()); 
            }else if(jsonValue instanceof JSONObject){
                String childKey = savePlan((JSONObject) jsonValue);
                resultMap.put(key + ":" + jsonKey, resultMap.get(childKey)); 
            }else if(jsonValue instanceof JSONArray){
                JSONArray jsonArray = (JSONArray) jsonValue;
                List<String> childKeys = new ArrayList<>();
                for(int i=0; i<jsonArray.length(); i++) {
                    childKeys.add(savePlan(jsonArray.getJSONObject(i)));
                }
                resultMap.put(key + ":" + jsonKey, childKeys);
            }
        });
        return key;
    }
    
//    public void handleUpdatePlan(String planId, JsonNode payload) {
//        Optional<Data> existingData = dataRepository.findById(planId);
//        if (!existingData.isPresent()) {
//            throw new IllegalArgumentException("Object with id " + planId + " not found");
//        }
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode existingPlan = objectMapper.readTree(existingData.get().getObject().toString());
//            ArrayNode existingLinkedPlanServices = (ArrayNode) existingPlan.get("linkedPlanServices");
//            ArrayNode newLinkedPlanServices = (ArrayNode) payload.get("linkedPlanServices");
//
//            // Create a copy of existingLinkedPlanServices for iteration
//            List<JsonNode> existingServicesList = StreamSupport.stream(existingLinkedPlanServices.spliterator(), false)
//                .collect(Collectors.toList());
//
//            for (JsonNode newService : newLinkedPlanServices) {
//                String newServiceId = newService.get("objectId").asText();
//                boolean objectIdChanged = true;
//
//                for (JsonNode existingService : existingServicesList) {
//                    String existingServiceId = existingService.get("objectId").asText();
//
//                    if (existingServiceId.equals(newServiceId)) {
//                        // If service objectId is found and it's the same, no change required
//                        objectIdChanged = false;
//                        break;
//                    }
//                }
//
//                if (objectIdChanged) {
//                    // If service objectId is changed, append the new service
//                    existingLinkedPlanServices.add(newService);
//                }
//            }
//
//            // Remove the "linkedPlanServices" field from the payload
//            ((ObjectNode) payload).remove("linkedPlanServices");
//
//            // Update other fields in the plan
//            ((ObjectNode) existingPlan).setAll((ObjectNode) payload);
//
//            // Add the updated linkedPlanServices to the plan
//            ((ObjectNode) existingPlan).set("linkedPlanServices", existingLinkedPlanServices);
//
//         // Now save the updated plan
//            parseAndSaveData(existingPlan.toString());
//
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }

    public void handleUpdatePlan(String planId, JsonNode payload) {
        Optional<Data> existingData = dataRepository.findById(planId);
        if (!existingData.isPresent()) {
            throw new IllegalArgumentException("Object with id " + planId + " not found");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode existingPlan = objectMapper.readTree(existingData.get().getObject().toString());
            ArrayNode existingLinkedPlanServices = (ArrayNode) existingPlan.get("linkedPlanServices");
            ArrayNode newLinkedPlanServices = (ArrayNode) payload.get("linkedPlanServices");

            // Create a copy of existingLinkedPlanServices for iteration
            List<JsonNode> existingServicesList = StreamSupport.stream(existingLinkedPlanServices.spliterator(), false)
                .collect(Collectors.toList());

            for (JsonNode newService : newLinkedPlanServices) {
                String newServiceId = newService.get("objectId").asText();
                boolean objectIdChanged = true;
                Iterator<JsonNode> existingServiceIterator = existingServicesList.iterator();

                while (existingServiceIterator.hasNext()) {
                    ObjectNode existingService = (ObjectNode) existingServiceIterator.next();
                    String existingServiceId = existingService.get("objectId").asText();

                    if (existingServiceId.equals(newServiceId)) {
                        // If service objectId is found and it's the same, no change required
                        objectIdChanged = false;

                        // Update the fields in the existing service
                        updateData(existingService, newService);
                    }
                }

                if (objectIdChanged) {
                    // If service objectId is changed, append the new service
                    existingLinkedPlanServices.add(newService);
                }
            }

            // Remove the "linkedPlanServices" field from the payload
            ((ObjectNode) payload).remove("linkedPlanServices");

            // Update other fields in the plan
            ((ObjectNode) existingPlan).setAll((ObjectNode) payload);

            // Add the updated linkedPlanServices to the plan
            ((ObjectNode) existingPlan).set("linkedPlanServices", existingLinkedPlanServices);

            // Now save the updated plan
            parseAndSaveData(existingPlan.toString());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void updateData(ObjectNode existingNode, JsonNode payload) {
        Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();

            if (fieldName.equals("planserviceCostShares") || fieldName.equals("linkedService")) {
                // For "planserviceCostShares" and "linkedService", update the entire object if "objectId" has changed
                String existingObjectId = existingNode.get(fieldName).get("objectId").asText();
                String payloadObjectId = fieldValue.get("objectId").asText();
                if (!existingObjectId.equals(payloadObjectId)) {
                    // "objectId" has changed, so replace the entire object
                    existingNode.replace(fieldName, fieldValue);
                } else {
                    // "objectId" has not changed, so update individual fields
                    updateData((ObjectNode) existingNode.get(fieldName), fieldValue);
                }
            } else {
                // For all other fields, just update the field value
                existingNode.replace(fieldName, fieldValue);
            }
        }
    }



}
