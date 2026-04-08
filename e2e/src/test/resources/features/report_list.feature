Feature: Report List

  Scenario: Page loads and shows report dropdown
    Given the report viewer is open
    Then the page title is "报表管理系统"
    And the report dropdown is visible
    And the dropdown contains at least 1 report option
