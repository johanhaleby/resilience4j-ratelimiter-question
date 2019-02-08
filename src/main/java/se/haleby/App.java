package se.haleby;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.TEXT_PLAIN;

/**
 * Hello world!
 *
 */
public class App {


    private final WebClient webClient;
    private final InMemoryRateLimiterRegistry rateLimiterRegistry;

    public App(int port, int rps) {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        rateLimiterRegistry = new InMemoryRateLimiterRegistry(RateLimiterConfig.ofDefaults());
        rateLimiterRegistry.rateLimiter("test", RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(rps)
                .timeoutDuration(Duration.ofHours(1))
                .build());
    }

    public Mono<String> makeRequest() {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("test");
        Mono<String> result = webClient.post()
                .uri("/testing")
                .accept(ALL)
                .contentType(TEXT_PLAIN)
                .syncBody("hello world")
                .retrieve().bodyToMono(String.class);

        return result.transform(RateLimiterOperator.of(rateLimiter));
    }
}