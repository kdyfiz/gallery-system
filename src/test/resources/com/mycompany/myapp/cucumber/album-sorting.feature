@album-sorting @integration
Feature: Album Sorting and Categorization
  As a gallery system user
  I want to sort and view albums by different criteria
  So that I can easily find and organize my photos

  Background:
    Given the gallery system is running
    And I am authenticated as a user

  @performance @critical
  Scenario: Sort large number of albums by event
    Given more than 1000 albums exist with various events
    When I request albums for gallery view sorted by "EVENT"
    Then the response should be returned within 2 seconds
    And albums should be properly grouped by events
    And each group should maintain alphabetical order

  @error-handling
  Scenario: Handle invalid sort parameter
    When I request albums with invalid sort parameter "INVALID_SORT"
    Then I should receive a "400" bad request error
    And the error message should indicate "Invalid sort parameter"

  @regression
  Scenario: Sort albums with mixed date formats
    Given albums exist with different date formats:
      | name          | creationDate           | overrideDate           |
      | Album 1      | 2024-01-15T10:00:00Z   | null                   |
      | Album 2      | null                    | 2024-02-01T15:30:00Z   |
      | Album 3      | 2024-01-20T08:00:00Z   | 2024-01-10T09:00:00Z   |
    When I request albums for gallery view sorted by "DATE"
    Then albums should be properly sorted by effective date
    And override dates should take precedence when present

  @concurrency
  Scenario: Handle concurrent sort operations
    Given multiple users are accessing the gallery
    When 10 concurrent requests for sorted albums are made
    Then all requests should complete successfully
    And sort order should be consistent across responses