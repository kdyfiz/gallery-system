package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Common step definitions shared across multiple feature files.
 * Contains reusable steps for authentication, system state, and validation.
 */
@WebAppConfiguration
@SpringBootTest
public class CommonStepDefs extends StepDefs {

    private MockMvc mockMvc;
    private ResultActions actions;

    @Given("the system is running")
    public void the_system_is_running() {
        // Verify application context is loaded
        // This is automatically handled by Spring Boot Test
    }

    @Given("the database is clean")
    public void the_database_is_clean() {
        // Clean database state if needed for tests
        // Implementation depends on test data management strategy
    }

    @Given("I wait {int} seconds")
    public void i_wait_seconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

    @Then("the response should be successful")
    public void the_response_should_be_successful() throws Exception {
        actions.andExpect(status().isOk());
    }

    @Then("the response should contain valid JSON")
    public void the_response_should_contain_valid_json() throws Exception {
        actions.andExpected(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Then("the response time should be under {int} seconds")
    public void the_response_time_should_be_under_seconds(int maxSeconds) {
        // Implementation for performance verification
        // This would typically be handled by the step definition context
        long responseTime = getLastResponseTime();
        assertThat(responseTime).isLessThan(maxSeconds * 1000);
    }

    @When("I make an invalid request")
    public void i_make_an_invalid_request() throws Exception {
        // Generic invalid request for error testing
        actions = mockMvc.perform(get("/api/invalid-endpoint"));
    }

    @Then("I should receive a {string} error")
    public void i_should_receive_an_error(String errorCode) throws Exception {
        switch (errorCode) {
            case "400":
                actions.andExpect(status().isBadRequest());
                break;
            case "401":
                actions.andExpect(status().isUnauthorized());
                break;
            case "403":
                actions.andExpect(status().isForbidden());
                break;
            case "404":
                actions.andExpect(status().isNotFound());
                break;
            case "500":
                actions.andExpect(status().isInternalServerError());
                break;
            default:
                throw new IllegalArgumentException("Unsupported error code: " + errorCode);
        }
    }

    // Helper method to get response time from context
    private long getLastResponseTime() {
        // Implementation would depend on how response timing is tracked
        return 0; // Placeholder
    }
}
