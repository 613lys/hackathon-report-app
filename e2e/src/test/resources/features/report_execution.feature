Feature: Report Execution

  Scenario: User selects and runs a predefined report
    Given the report viewer is open
    When I select the first report from the dropdown
    Then the report description section is visible
    When I click the "执行报表" button
    Then the results table is displayed with at least 1 row
