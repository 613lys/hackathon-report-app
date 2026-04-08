Feature: Custom SQL Query

  Scenario: User runs a custom SQL query
    Given the report viewer is open
    When I type "SELECT 1 AS result" into the SQL textarea
    And I click the "执行SQL" button
    Then the results section is displayed

  Scenario: Invalid SQL shows an error
    Given the report viewer is open
    When I type "NOT VALID SQL" into the SQL textarea
    And I click the "执行SQL" button
    Then an error message is displayed
