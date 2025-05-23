@album-management @integration
Feature: Photo Album Management
  As a gallery system user
  I want to create and manage photo albums
  So that I can organize my photos by events and share them effectively

  Background:
    Given the gallery system is running
    And I am authenticated as a user

  @smoke @critical
  Scenario: Successfully create a new photo album
    Given I have the necessary permissions to create albums
    When I create an album with the following details:
      | name         | Summer Vacation 2024    |
      | event        | Family Summer Trip      |
      | creationDate | 2024-01-15T10:00:00.000Z |
    Then the album should be created successfully
    And the album should have name "Summer Vacation 2024"
    And the album should be associated with event "Family Summer Trip"
    And the album should belong to the current user

  @regression
  Scenario: Create album with minimal required information
    When I create an album with name "Basic Album"
    Then the album should be created successfully
    And the album should have a system-generated creation date
    And the album should have no associated event
    And the album should have no thumbnail

  @validation @error-handling
  Scenario Outline: Validate album creation with invalid data
    When I attempt to create an album with "<field>" as "<value>"
    Then the album creation should fail
    And I should receive a validation error for "<field>"
    And the error message should indicate "<error_type>"

    Examples:
      | field | value | error_type          |
      | name  | ab    | too short           |
      | name  | ""    | required field      |
      | name  | null  | required field      |

  @business-rules
  Scenario: Album creation date handling
    Given the current date is "2024-01-15"
    When I create an album without specifying creation date
    Then the album should use the current date as creation date
    When I create an album with override date "2024-01-10T10:00:00.000Z"
    Then the album should use the override date for display purposes
    And the original creation date should be preserved

  @gallery-view @critical
  Scenario: Retrieve albums for gallery display
    Given multiple albums exist:
      | name           | event          | creationDate           |
      | Wedding Photos | Smith Wedding  | 2024-01-10T10:00:00.000Z |
      | Travel Pics    | Europe Trip    | 2024-01-15T10:00:00.000Z |
      | Random Shots   |                | 2024-01-20T10:00:00.000Z |
    When I request albums for gallery view sorted by "EVENT"
    Then the albums should be grouped by event
    And albums without events should be in "Miscellaneous" group
    And each group should be sorted alphabetically by name

  @gallery-view
  Scenario: Sort albums by date in gallery view
    Given multiple albums exist with different dates
    When I request albums for gallery view sorted by "DATE"
    Then the albums should be grouped by creation month
    And within each group, albums should be sorted chronologically
    And the most recent albums should appear first

  @thumbnail-management
  Scenario: Upload thumbnail for album
    Given an album exists with name "Test Album"
    When I upload a thumbnail image for the album
    Then the album should have the uploaded thumbnail
    And the thumbnail should be accessible for display
    And the thumbnail content type should be properly set

  @thumbnail-management
  Scenario: Album without thumbnail displays default
    Given an album exists without a thumbnail
    When I view the album in gallery
    Then a default thumbnail should be displayed
    And the default thumbnail should indicate missing image

  @security
  Scenario: User can only access their own albums
    Given user "alice" has created an album "Alice's Photos"
    And user "bob" has created an album "Bob's Photos"
    When I authenticate as user "alice"
    And I request my albums
    Then I should see "Alice's Photos"
    And I should not see "Bob's Photos"

  @performance
  Scenario: Gallery loads efficiently with many albums
    Given more than 100 albums exist in the system
    When I request the gallery view
    Then the response should be returned within 2 seconds
    And only necessary album data should be loaded
    And pagination should be applied if needed

  @data-integrity
  Scenario: Album owner relationship is maintained
    Given I am authenticated as user "photographer"
    When I create an album "Portfolio"
    Then the album should be associated with user "photographer"
    And only "photographer" should be able to modify the album
    And the album should appear in "photographer's" album list

  @business-rules
  Scenario: Album event categorization
    Given albums exist with various events:
      | name        | event           |
      | Wedding1    | Wedding         |
      | Wedding2    | Wedding         |
      | Vacation1   | Summer Vacation |
      | Random1     |                 |
      | Random2     |                 |
    When I view albums grouped by event
    Then albums should be categorized as:
      | category           | count |
      | Wedding            | 2     |
      | Summer Vacation    | 1     |
      | Miscellaneous      | 2     |

  @error-handling
  Scenario: Handle album retrieval when user has no albums
    Given I am a new user with no albums
    When I request my album gallery
    Then I should see an empty gallery message
    And I should be prompted to create my first album
    And the create album option should be prominently displayed

  @integration
  Scenario: End-to-end album creation and gallery viewing
    When I create an album "End-to-End Test"
    And I upload a thumbnail for the album
    And I set the event to "Testing Event"
    Then the album should appear in my gallery
    And the album should be properly categorized under "Testing Event"
    And the thumbnail should be displayed correctly 