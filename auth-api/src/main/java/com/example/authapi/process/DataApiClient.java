package com.example.authapi.process;

import com.example.authapi.config.DataApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Component
public class DataApiClient {

    static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private static final Logger log = LoggerFactory.getLogger(DataApiClient.class);

    private final RestClient restClient;
    private final String internalToken;

    public DataApiClient(RestClient.Builder builder, DataApiProperties properties) {
        if (properties.url() == null || properties.url().isBlank()) {
            throw new IllegalStateException("DATA_API_URL must be set");
        }
        if (properties.internalToken() == null || properties.internalToken().isBlank()) {
            throw new IllegalStateException("INTERNAL_TOKEN must be set");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.baseUrl(properties.url()).requestFactory(factory).build();
        this.internalToken = properties.internalToken();
    }

    public String transform(String text) {
        try {
            DataApiResponse response = restClient.post()
                    .uri("/api/transform")
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DataApiRequest(text))
                    .retrieve()
                    .body(DataApiResponse.class);
            if (response == null || response.result() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "transform failed");
            }
            return response.result();
        } catch (RestClientResponseException ex) {
            log.warn("data-api transform failed with status {}", ex.getStatusCode().value());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "transform failed");
        } catch (RestClientException ex) {
            log.warn("data-api transform request failed: {}", ex.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "transform failed");
        }
    }
}
