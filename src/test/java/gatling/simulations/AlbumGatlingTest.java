package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Performance test for the Album entity and Gallery functionality.
 *
 * Covers both standard CRUD operations and gallery-specific performance requirements.
 */
public class AlbumGatlingTest extends Simulation {

    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");

    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.9")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (compatible; Gatling Performance Test)")
        .silentResources();

    Map<String, String> headersHttp = Map.of("Accept", "application/json");
    Map<String, String> headersHttpAuthentication = Map.of("Content-Type", "application/json", "Accept", "application/json");
    Map<String, String> headersHttpAuthenticated = Map.of("Accept", "application/json", "Authorization", "${access_token}");

    // Authentication chain
    ChainBuilder authenticate = exec(
        http("Authentication")
            .post("/api/authenticate")
            .headers(headersHttpAuthentication)
            .body(StringBody("{\"username\":\"admin\", \"password\":\"admin\"}"))
            .asJson()
            .check(header("Authorization").saveAs("access_token"))
    )
        .exitHereIfFailed()
        .pause(1);

    // Gallery view performance test - critical for user experience
    ChainBuilder galleryPerformanceChain = exec(
        http("Get Gallery View - Event Sort")
            .get("/api/albums/gallery?sortBy=EVENT")
            .headers(headersHttpAuthenticated)
            .check(status().is(200))
            .check(responseTimeInMillis().lte(2000)) // Must load within 2 seconds per requirements
    )
        .pause(2, 5)
        .exec(
            http("Get Gallery View - Date Sort")
                .get("/api/albums/gallery?sortBy=DATE")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
                .check(responseTimeInMillis().lte(2000))
        )
        .pause(2, 5);

    // Standard CRUD operations
    ChainBuilder albumCrudChain = repeat(3).on(
        exec(http("Get All Albums").get("/api/albums").headers(headersHttpAuthenticated).check(status().is(200)))
            .pause(Duration.ofSeconds(2), Duration.ofSeconds(5))
            .exec(
                http("Create Album")
                    .post("/api/albums")
                    .headers(headersHttpAuthenticated)
                    .body(
                        StringBody(
                            """
                            {
                                "name": "Performance Test Album ${__time()}",
                                "event": "Performance Testing Event",
                                "creationDate": "2025-01-23T10:00:00.000Z",
                                "overrideDate": null,
                                "thumbnail": null
                            }
                            """
                        )
                    )
                    .asJson()
                    .check(status().is(201))
                    .check(headerRegex("Location", "(.*)").saveAs("new_album_url"))
            )
            .exitHereIfFailed()
            .pause(2)
            .repeat(3)
            .on(
                exec(http("Get Created Album").get("${new_album_url}").headers(headersHttpAuthenticated).check(status().is(200))).pause(
                    1,
                    3
                )
            )
            .exec(
                http("Update Album")
                    .put("${new_album_url}")
                    .headers(headersHttpAuthenticated)
                    .body(
                        StringBody(
                            """
                            {
                                "name": "Updated Performance Test Album ${__time()}",
                                "event": "Updated Performance Testing Event",
                                "creationDate": "2025-01-23T10:00:00.000Z",
                                "overrideDate": "2025-01-23T11:00:00.000Z"
                            }
                            """
                        )
                    )
                    .asJson()
                    .check(status().is(200))
            )
            .pause(2)
            .exec(http("Delete Album").delete("${new_album_url}").headers(headersHttpAuthenticated).check(status().is(204)))
            .pause(1)
    );

    // Large dataset performance test
    ChainBuilder largeDatasetChain = exec(
        http("Gallery with Large Dataset")
            .get("/api/albums/gallery?sortBy=EVENT")
            .headers(headersHttpAuthenticated)
            .check(status().is(200))
            .check(responseTimeInMillis().lte(3000)) // Slightly higher tolerance for large datasets
            .check(jsonPath("$").count().lte(1000)) // Should handle up to 1000 albums efficiently
    );

    // Scenarios
    ScenarioBuilder normalUserScenario = scenario("Normal User Gallery Browsing")
        .exec(authenticate)
        .pause(2)
        .repeat(5)
        .on(galleryPerformanceChain)
        .pause(5);

    ScenarioBuilder adminUserScenario = scenario("Admin User CRUD Operations").exec(authenticate).pause(2).exec(albumCrudChain).pause(3);

    ScenarioBuilder highLoadScenario = scenario("High Load Gallery Access")
        .exec(authenticate)
        .pause(1)
        .repeat(10)
        .on(exec(galleryPerformanceChain).pause(1, 3));

    ScenarioBuilder largeDatasetScenario = scenario("Large Dataset Performance")
        .exec(authenticate)
        .pause(2)
        .exec(largeDatasetChain)
        .pause(5);

    // Load test configuration
    {
        setUp(
            // Normal browsing - majority of users
            normalUserScenario.injectOpen(
                rampUsers(Integer.getInteger("normalUsers", 50)).during(Duration.ofMinutes(Integer.getInteger("rampDuration", 2)))
            ),
            // Admin operations - fewer users but more intensive
            adminUserScenario.injectOpen(
                rampUsers(Integer.getInteger("adminUsers", 10)).during(Duration.ofMinutes(Integer.getInteger("rampDuration", 2)))
            ),
            // High load scenario - initial ramp
            highLoadScenario.injectOpen(rampUsers(Integer.getInteger("highLoadUsers", 20)).during(Duration.ofSeconds(30))),
            // High load scenario - constant rate
            highLoadScenario.injectOpen(constantUsersPerSec(5).during(Duration.ofSeconds(60))),
            // Large dataset test
            largeDatasetScenario.injectOpen(rampUsers(5).during(Duration.ofSeconds(10)))
        )
            .protocols(httpConf)
            .assertions(
                global().responseTime().max().lte(5000),
                global().responseTime().mean().lte(1000),
                global().successfulRequests().percent().gte(95.0),
                forAll().responseTime().percentile3().lte(2000)
            );
    }
}
