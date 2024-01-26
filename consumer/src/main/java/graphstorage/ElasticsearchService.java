package graphstorage;


import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.*;


@Service
public class ElasticsearchService {

    private final RestHighLevelClient elasticsearchClient;
    private final ObjectMapper objectMapper;
    private static final String INDEX_NAME = "insurance";
    
    @Autowired
    public ElasticsearchService(RestHighLevelClient Client) {
        this.elasticsearchClient = Client;
        this.objectMapper = new ObjectMapper();
    }

//    public void indexDocument(String jsonInput) {
//    	
//    	System.out.println("_______________________"+jsonInput);
//        try {
//            if (!isIndexExist()) {
//                createIndexWithMapping();
//                System.out.println("_______________________"+"hiojnlk");
//            }
//            JSONObject jsonObject = new JSONObject(jsonInput);
//            JSONObject planCostShares = jsonObject.getJSONObject("planCostShares");
//            JSONArray linkedPlanServices = jsonObject.getJSONArray("linkedPlanServices");
//
//            IndexRequest indexRequest;
//
//            // Index planCostShares
//            indexRequest = new IndexRequest(INDEX_NAME).id(planCostShares.getString("objectId"))
//                    .source(planCostShares.toString(), XContentType.JSON);
//            elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
//
//            for (int i = 0; i < linkedPlanServices.length(); i++) {
//                JSONObject linkedPlanService = linkedPlanServices.getJSONObject(i);
//
//                // Index linkedPlanService
//                indexRequest = new IndexRequest(INDEX_NAME).id(linkedPlanService.getString("objectId"))
//                        .source(linkedPlanService.toString(), XContentType.JSON);
//                elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
//
//                JSONObject linkedService = linkedPlanService.getJSONObject("linkedService");
//
//                // Index linkedService
//                indexRequest = new IndexRequest(INDEX_NAME).id(linkedService.getString("objectId"))
//                        .source(linkedService.toString(), XContentType.JSON);
//                elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
//
//                JSONObject planServiceCostShares = linkedPlanService.getJSONObject("planserviceCostShares");
//
//                // Index planServiceCostShares
//                indexRequest = new IndexRequest(INDEX_NAME).id(planServiceCostShares.getString("objectId"))
//                        .source(planServiceCostShares.toString(), XContentType.JSON);
//                elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
//            }
//
//            // Index the root object (plan)
//            indexRequest = new IndexRequest(INDEX_NAME).id(jsonObject.getString("objectId"))
//                    .source(jsonObject.toString(), XContentType.JSON);
//            elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            // Handle the exception
//            e.printStackTrace();
//        }
//    }
    
    public void saveData(String jsonData) throws IOException {
        Map<String, Object> dataMap = objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>(){});

        String objectId = dataMap.get("objectId").toString();
        String objectType = dataMap.get("objectType").toString();
        putData(objectId, objectType, dataMap);

        processSubObject(dataMap, "planCostShares", objectId);

        List<Map<String, Object>> linkedPlanServices = (List<Map<String, Object>>) dataMap.get("linkedPlanServices");
        for (Map<String, Object> linkedPlanService : linkedPlanServices) {
            objectId = linkedPlanService.get("objectId").toString();
            putData(objectId, "linkedPlanServices", linkedPlanService);

            processSubObject(linkedPlanService, "linkedService", objectId);
            processSubObject(linkedPlanService, "planserviceCostShares", objectId);
        }
    }

    private void processSubObject(Map<String, Object> dataMap, String key, String parentId) throws IOException {
        Map<String, Object> subObject = (Map<String, Object>) dataMap.get(key);
        String objectId = subObject.get("objectId").toString();
        putData(objectId, key, subObject, parentId);
    }

    private void putData(String id, String type, Map<String, Object> data, String parentId) throws IOException {
        Map<String, Object> joinField = new HashMap<>();
        joinField.put("name", type);
        if (parentId != null) {
            joinField.put("parent", parentId);
        }
        data.put("plan_join", joinField);

        IndexRequest indexRequest = new IndexRequest("insurance").id(id).source(data, XContentType.JSON);
        IndexResponse response = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

    private void putData(String id, String type, Map<String, Object> data) throws IOException {
        putData(id, type, data, null);
    }

    private boolean isIndexExist() {
        try {
            GetIndexRequest request = new GetIndexRequest(INDEX_NAME);
            return elasticsearchClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createIndexWithMapping() {
        try {
            CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
            String mapping = "{\n" +
                    "  \"settings\": {\n" +
                    "    \"index\": {\n" +
                    "      \"number_of_shards\": 1,\n" +
                    "      \"number_of_replicas\": 1\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"mappings\": {\n" +
                    "    \"properties\": {\n" +
                    "      \"plan\": {\n" +
                    "        \"properties\": {\n" +
                    "          \"_org\": {\"type\": \"text\"},\n" +
                    "          \"objectId\": {\"type\": \"keyword\"},\n" +
                    "          \"objectType\": {\"type\": \"text\"},\n" +
                    "          \"planType\": {\"type\": \"text\"},\n" +
                    "          \"creationDate\": {\n" +
                    "            \"type\": \"date\",\n" +
                    "            \"format\": \"MM-dd-yyyy\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"planCostShares\": {\n" +
                    "        \"properties\": {\n" +
                    "          \"copay\": {\"type\": \"long\"},\n" +
                    "          \"deductible\": {\"type\": \"long\"},\n" +
                    "          \"_org\": {\"type\": \"text\"},\n" +
                    "          \"objectId\": {\"type\": \"keyword\"},\n" +
                    "          \"objectType\": {\"type\": \"text\"}\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"linkedPlanServices\": {\n" +
                    "        \"properties\": {\n" +
                    "          \"_org\": {\"type\": \"text\"},\n" +
                    "          \"objectId\": {\"type\": \"keyword\"},\n" +
                    "          \"objectType\": {\"type\": \"text\"}\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"linkedService\": {\n" +
                    "        \"properties\": {\n" +
                    "          \"_org\": {\"type\": \"text\"},\n" +
                    "          \"name\": {\"type\": \"text\"},\n" +
                    "          \"objectId\": {\"type\": \"keyword\"},\n" +
                    "          \"objectType\": {\"type\": \"text\"}\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"planserviceCostShares\": {\n" +
                    "        \"properties\": {\n" +
                    "          \"copay\": {\"type\": \"long\"},\n" +
                    "          \"deductible\": {\"type\": \"long\"},\n" +
                    "          \"_org\": {\"type\": \"text\"},\n" +
                    "          \"objectId\": {\"type\": \"keyword\"},\n" +
                    "          \"objectType\": {\"type\": \"text\"}\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"plan_join\": {\n" +
                    "        \"type\": \"join\",\n" +
                    "        \"eager_global_ordinals\": true,\n" +
                    "        \"relations\": {\n" +
                    "          \"plan\": [\"planCostShares\", \"linkedPlanServices\"],\n" +
                    "          \"linkedPlanServices\": [\"linkedService\", \"planserviceCostShares\"]\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            request.source(mapping, XContentType.JSON);
            elasticsearchClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

