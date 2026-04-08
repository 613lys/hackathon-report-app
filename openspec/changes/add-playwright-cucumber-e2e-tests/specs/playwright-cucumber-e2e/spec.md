## ADDED Requirements

### Requirement: Standalone e2e Maven module exists
The project SHALL contain a standalone Maven module at `e2e/` with its own `pom.xml` declaring Playwright Java, Cucumber 7, and JUnit Platform Suite dependencies, with no dependency on `backend/` source code.

#### Scenario: e2e module has correct dependencies
- **WHEN** `e2e/pom.xml` is inspected
- **THEN** it SHALL declare `com.microsoft.playwright:playwright:1.42.0`, `io.cucumber:cucumber-java:7.15.0`, `io.cucumber:cucumber-junit-platform-engine:7.15.0`, and `org.junit.platform:junit-platform-suite:1.10.2`

#### Scenario: e2e module has no backend dependency
- **WHEN** `e2e/pom.xml` is inspected
- **THEN** it SHALL NOT declare any dependency on `com.legacy` group artifacts

### Requirement: Cucumber runner class configured
The e2e module SHALL contain a JUnit Platform Suite runner class that discovers `.feature` files from the classpath and configures the Cucumber HTML report plugin.

#### Scenario: Runner class exists with correct annotations
- **WHEN** the runner class in `e2e/src/test/java/.../runners/CucumberRunner.java` is inspected
- **THEN** it SHALL be annotated with `@Suite`, `@IncludeEngines("cucumber")`, `@SelectClasspathResource("features")`, and `@ConfigurationParameter` setting the HTML report plugin to `html:target/cucumber-reports/report.html`

### Requirement: Page Object Model wraps Angular UI locators
The e2e module SHALL contain a `ReportViewerPage` class that encapsulates all Playwright locators for the report viewer UI, exposing named methods for each user interaction.

#### Scenario: Page object navigates to the report viewer
- **WHEN** `ReportViewerPage.open()` is called
- **THEN** the browser SHALL navigate to the configured `e2e.baseUrl` (default `http://localhost:4200`)

#### Scenario: Page object exposes report selection
- **WHEN** `ReportViewerPage.selectFirstReport()` is called
- **THEN** the `<select>` dropdown SHALL have the second option (index 1) selected

#### Scenario: Page object exposes run report action
- **WHEN** `ReportViewerPage.clickRunReport()` is called
- **THEN** the button with text `执行报表` SHALL be clicked

#### Scenario: Page object exposes custom SQL input and run
- **WHEN** `ReportViewerPage.enterCustomSql(sql)` is called with a SQL string
- **THEN** the `<textarea>` SHALL contain the SQL string
- **WHEN** `ReportViewerPage.clickRunSql()` is called
- **THEN** the button with text `执行SQL` SHALL be clicked

### Requirement: BDD feature files cover three user flows
The e2e module SHALL contain Gherkin feature files covering: (1) page load and report list, (2) report selection and execution, (3) custom SQL execution.

#### Scenario: Report list feature file exists
- **WHEN** `e2e/src/test/resources/features/report_list.feature` is read
- **THEN** it SHALL contain a scenario verifying the page title is `报表管理系统` and the dropdown is visible with at least one report option

#### Scenario: Report execution feature file exists
- **WHEN** `e2e/src/test/resources/features/report_execution.feature` is read
- **THEN** it SHALL contain a scenario verifying that selecting the first report and clicking `执行报表` produces a visible results table

#### Scenario: Custom SQL feature file exists
- **WHEN** `e2e/src/test/resources/features/custom_sql.feature` is read
- **THEN** it SHALL contain a scenario verifying that entering `SELECT 1 AS result` and clicking `执行SQL` displays a results section

#### Scenario: Invalid SQL shows error
- **WHEN** `e2e/src/test/resources/features/custom_sql.feature` is read
- **THEN** it SHALL contain a scenario verifying that entering invalid SQL and clicking `执行SQL` causes an error element to be visible

### Requirement: Cucumber hooks manage browser lifecycle
The e2e module SHALL contain a `CucumberHooks` class that opens a new Playwright `Page` before each scenario and closes it after, capturing a screenshot on failure.

#### Scenario: New page opened before each scenario
- **WHEN** a Cucumber scenario starts
- **THEN** a fresh Playwright `Page` SHALL be available for that scenario's steps

#### Scenario: Screenshot taken on scenario failure
- **WHEN** a Cucumber scenario fails
- **THEN** a PNG screenshot SHALL be saved to `target/screenshots/<scenario-name>.png`

### Requirement: Cucumber HTML report generated after mvn test
After running `mvn test` in the `e2e/` directory (with frontend and backend running), Cucumber SHALL produce an HTML report at `e2e/target/cucumber-reports/report.html`.

#### Scenario: HTML report exists after test run
- **WHEN** `mvn test` completes in the `e2e/` directory
- **THEN** `e2e/target/cucumber-reports/report.html` SHALL exist and be a valid HTML file

#### Scenario: Report shows scenario results
- **WHEN** `e2e/target/cucumber-reports/report.html` is opened
- **THEN** it SHALL display pass/fail status for each BDD scenario
