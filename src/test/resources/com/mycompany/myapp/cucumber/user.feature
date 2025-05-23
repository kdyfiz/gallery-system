@user-management @integration
Feature: User Management
  As a gallery system administrator
  I want to manage user accounts effectively
  So that I can control access to the photo gallery system and maintain user data integrity

  Background:
    Given the gallery system is running
    And I am authenticated as an administrator

  @smoke @critical
  Scenario: Successfully retrieve an existing administrator user
    Given a user exists with login "admin"
    When I search for user "admin"
    Then the user should be found
    And the user should have first name "Administrator"
    And the user should have last name "Administrator"
    And the user should have administrative privileges
    And the user should be activated

  @regression
  Scenario: Retrieve user with specific details
    Given a user exists with the following details:
      | login     | firstName | lastName | email                | activated |
      | testuser  | John      | Doe      | john.doe@example.com | true      |
    When I search for user "testuser"
    Then the user should be found
    And the user should have first name "John"
    And the user should have last name "Doe"
    And the user should have email "john.doe@example.com"

  @error-handling
  Scenario: Search for non-existent user
    Given no user exists with login "nonexistent"
    When I search for user "nonexistent"
    Then the user should not be found
    And I should receive a "404" error response

  @validation
  Scenario Outline: Search for users with invalid login patterns
    When I search for user "<invalid_login>"
    Then I should receive a validation error
    And the error message should indicate invalid login format

    Examples:
      | invalid_login | reason                    |
      | a             | too short                 |
      | user@domain   | contains invalid character|
      | UPPERCASE     | should be lowercase       |

  @security
  Scenario: Unauthorized user cannot access user management
    Given I am not authenticated
    When I attempt to search for user "admin"
    Then I should receive a "401" unauthorized error
    And access should be denied

  @security  
  Scenario: Regular user cannot access admin user endpoints
    Given I am authenticated as a regular user
    When I attempt to search for user "admin"
    Then I should receive a "403" forbidden error
    And access should be denied

  @data-integrity
  Scenario: Retrieve user with complete profile information
    Given a user exists with complete profile:
      | login        | admin                     |
      | firstName    | Administrator             |
      | lastName     | Administrator             |
      | email        | admin@localhost           |
      | activated    | true                      |
      | langKey      | en                        |
      | imageUrl     | /content/images/admin.png |
    When I search for user "admin"
    Then the user should be found
    And all profile information should be correctly returned
    And the user data should be consistent

  @performance
  Scenario: Search performance for user lookup
    Given multiple users exist in the system
    When I search for user "admin"
    Then the user should be found within acceptable time limits
    And the response should be properly formatted

  @business-rules
  Scenario: Verify user account activation status
    Given users exist with different activation statuses:
      | login          | activated |
      | active_user    | true      |
      | inactive_user  | false     |
    When I search for user "active_user"
    Then the user should be found
    And the user should be activated
    When I search for user "inactive_user"
    Then the user should be found
    And the user should not be activated

  @album-relationship
  Scenario: Retrieve user who owns photo albums
    Given a user "photographer" exists who owns photo albums
    And the user has created albums:
      | name           | event          |
      | Wedding Photos | Smith Wedding  |
      | Travel Pics    | Europe Trip    |
    When I search for user "photographer"
    Then the user should be found
    And the user should be associated with their albums