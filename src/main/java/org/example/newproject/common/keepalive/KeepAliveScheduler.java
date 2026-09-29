package org.example.newproject.common.keepalive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@EnableScheduling
@ConditionalOnExpression("!'${app.keep-alive.url:}'.isEmpty()")
public class KeepAliveScheduler {

    private final RestClient restClient;

    public KeepAliveScheduler(@Value("${app.keep-alive.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "${app.keep-alive.interval:PT10M}")
    public void ping() {
        try {
            restClient.get().uri("/health").retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
