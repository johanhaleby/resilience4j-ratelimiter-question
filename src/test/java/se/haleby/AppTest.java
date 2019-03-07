package se.haleby;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void whyDoesntThisWorkQuestionMarkQuestionMark() {
        // Given
        int numberOfRequests = 30;
        int rps = 10;
        App app = new App(wireMockRule.port(), rps);

        wireMockRule.addStubMapping(stubFor(post(urlPathEqualTo("/testing"))
                .willReturn(aResponse().withStatus(200).withBody("hello hello!"))));

        // When
        ParallelFlux<String> flux = Flux.range(0, numberOfRequests)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(___ -> app.makeRequest());

        long startTime = new Date().getTime();
        StepVerifier.create(flux).expectNextCount(numberOfRequests).verifyComplete();
        long endTime = new Date().getTime();

        assertThat(endTime - startTime)
                .describedAs("I don't understand why this is not taking longer")
                .isGreaterThanOrEqualTo((numberOfRequests / rps) * 1000);
    }
}
