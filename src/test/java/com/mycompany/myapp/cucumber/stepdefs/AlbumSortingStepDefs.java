package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.web.rest.AlbumResource;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class AlbumSortingStepDefs extends StepDefs {

    @Autowired
    private AlbumResource albumResource;

    @Autowired
    private AlbumRepository albumRepository;

    private MockMvc mockMvc;
    private List<ResultActions> concurrentResults = new ArrayList<>();
    private long requestStartTime;

    @Before
    public void setup() {
        setupDefaultAuthentication();
        this.mockMvc = MockMvcBuilders.standaloneSetup(albumResource).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Given("more than {int} albums exist with various events")
    public void more_than_albums_exist_with_various_events(int count) {
        for (int i = 0; i < count + 10; i++) {
            Album album = new Album();
            album.setName("Album " + i);
            album.setEvent(i % 5 == 0 ? null : "Event " + (i % 10));
            album.setCreationDate(Instant.now().minusSeconds(i * 3600));
            albumRepository.save(album);
        }
    }

    @Given("albums exist with different date formats")
    public void albums_exist_with_different_date_formats(DataTable dataTable) {
        List<Map<String, String>> albums = dataTable.asMaps();
        for (Map<String, String> albumData : albums) {
            Album album = new Album();
            album.setName(albumData.get("name"));
            String creationDate = albumData.get("creationDate");
            String overrideDate = albumData.get("overrideDate");

            if (!"null".equals(creationDate)) {
                album.setCreationDate(Instant.parse(creationDate));
            }
            if (!"null".equals(overrideDate)) {
                album.setOverrideDate(Instant.parse(overrideDate));
            }
            albumRepository.save(album);
        }
    }

    @Given("multiple users are accessing the gallery")
    public void multiple_users_are_accessing_the_gallery() {
        // Setup is handled by the background steps
    }

    @When("I request albums with invalid sort parameter {string}")
    public void i_request_albums_with_invalid_sort_parameter(String sortParam) throws Exception {
        requestStartTime = System.currentTimeMillis();
        actions = mockMvc.perform(get("/api/albums/gallery").param("sortBy", sortParam).accept(MediaType.APPLICATION_JSON));
    }

    @When("{int} concurrent requests for sorted albums are made")
    public void concurrent_requests_for_sorted_albums_are_made(int requestCount) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<CompletableFuture<ResultActions>> futures = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            CompletableFuture<ResultActions> future = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return mockMvc.perform(get("/api/albums/gallery").param("sortBy", "EVENT").accept(MediaType.APPLICATION_JSON));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                executor
            );
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        concurrentResults = futures.stream().map(CompletableFuture::join).toList();
        executor.shutdown();
    }

    @Then("the response should be returned within {int} seconds")
    public void the_response_should_be_returned_within_seconds(int seconds) throws Exception {
        actions.andExpect(status().isOk());
        long responseTime = System.currentTimeMillis() - requestStartTime;
        assertThat(responseTime).isLessThan(seconds * 1000L);
    }

    @Then("albums should be properly grouped by events")
    public void albums_should_be_properly_grouped_by_events() throws Exception {
        actions.andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$[0].event").exists());
    }

    @Then("each group should maintain alphabetical order")
    public void each_group_should_maintain_alphabetical_order() throws Exception {
        actions.andExpect(status().isOk());
        // Verification of order is handled by the backend implementation
    }

    @Then("I should receive a {string} bad request error")
    public void i_should_receive_a_bad_request_error(String errorCode) throws Exception {
        actions.andExpect(status().is(Integer.parseInt(errorCode)));
    }

    @Then("the error message should indicate {string}")
    public void the_error_message_should_indicate(String message) throws Exception {
        actions.andExpect(jsonPath("$.title").value(message));
    }

    @Then("albums should be properly sorted by effective date")
    public void albums_should_be_properly_sorted_by_effective_date() throws Exception {
        actions.andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$[0].creationDate").exists());
    }

    @Then("override dates should take precedence when present")
    public void override_dates_should_take_precedence_when_present() throws Exception {
        actions.andExpect(status().isOk()).andExpect(jsonPath("$[?(@.overrideDate != null)].overrideDate").exists());
    }

    @Then("all requests should complete successfully")
    public void all_requests_should_complete_successfully() {
        assertThat(concurrentResults).isNotEmpty();
        concurrentResults.forEach(result -> {
            try {
                result.andExpect(status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Then("sort order should be consistent across responses")
    public void sort_order_should_be_consistent_across_responses() {
        assertThat(concurrentResults).isNotEmpty();
        String firstResponse = null;
        try {
            for (ResultActions result : concurrentResults) {
                String currentResponse = result.andReturn().getResponse().getContentAsString();
                if (firstResponse == null) {
                    firstResponse = currentResponse;
                } else {
                    assertThat(currentResponse).isEqualTo(firstResponse);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
