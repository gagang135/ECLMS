package com.company.eclms.common.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractSearchServiceImpl implements ContractSearchService {

    @Value("${spring.data.elasticsearch.uris:http://localhost:9200}")
    private String esUrl;

    private RestClient restClient;
    private boolean initialized = false;

    private RestClient getClient() {
        if (!initialized) {
            this.restClient = RestClient.builder().baseUrl(esUrl).build();
            this.initialized = true;
        }
        return restClient;
    }

    @Override
    public void indexContract(UUID contractId, String name, String content, String vendorName, String status, String ocrText) {
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", contractId.toString());
            doc.put("name", name);
            doc.put("content", content);
            doc.put("vendorName", vendorName);
            doc.put("status", status);
            doc.put("ocrText", ocrText);

            getClient().put()
                    .uri("/contracts/_doc/" + contractId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(doc)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully indexed contract {} in Elasticsearch", contractId);
        } catch (Exception e) {
            log.warn("Elasticsearch indexing failed for contract {}: {}. Continuing without index.", contractId, e.getMessage());
        }
    }

    @Override
    public void deleteIndex(UUID contractId) {
        try {
            getClient().delete()
                    .uri("/contracts/_doc/" + contractId)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully deleted contract {} from Elasticsearch index", contractId);
        } catch (Exception e) {
            log.warn("Failed to delete Elasticsearch index for contract {}: {}", contractId, e.getMessage());
        }
    }

    @Override
    public List<UUID> searchContractIds(String searchTerm) {
        List<UUID> matchingIds = new ArrayList<>();
        try {
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> multiMatch = new HashMap<>();
            multiMatch.put("query", searchTerm);
            multiMatch.put("fields", List.of("name^3", "content", "vendorName^2", "ocrText"));
            
            Map<String, Object> matchObj = new HashMap<>();
            matchObj.put("multi_match", multiMatch);
            query.put("query", matchObj);

            String response = getClient().post()
                    .uri("/contracts/_search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(query)
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode hits = root.path("hits").path("hits");
            if (hits.isArray()) {
                for (JsonNode hit : hits) {
                    String idStr = hit.path("_id").asText();
                    matchingIds.add(UUID.fromString(idStr));
                }
            }
        } catch (Exception e) {
            log.warn("Elasticsearch search query failed: {}. Returning empty list.", e.getMessage());
        }
        return matchingIds;
    }
}
