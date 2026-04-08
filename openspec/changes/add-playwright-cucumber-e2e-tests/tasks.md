## 1. Maven Module Setup

- [x] 1.1 Create `e2e/` directory at repo root
- [x] 1.2 Create `e2e/pom.xml` with `playwright:1.42.0`, `cucumber-java:7.15.0`, `cucumber-junit-platform-engine:7.15.0`, and `junit-platform-suite:1.10.2` dependencies
- [x] 1.3 Add `maven-surefire-plugin 3.x` configuration to `e2e/pom.xml` to pick up JUnit Platform tests
- [x] 1.4 Add `exec-maven-plugin` configuration to `e2e/pom.xml` for `mvn exec:java ... install chromium` browser download

## 2. Page Object Model

- [x] 2.1 Create package `com.legacy.report.e2e.pages` under `e2e/src/test/java/`
- [x] 2.2 Create `ReportViewerPage.java` with `open()` navigating to `System.getProperty("e2e.baseUrl", "http://localhost:4200")`
- [x] 2.3 Add `getPageTitle()` returning the `<h1>` text
- [x] 2.4 Add `isReportDropdownVisible()` checking the `<select>` is visible
- [x] 2.5 Add `getReportOptionCount()` returning number of `<option>` elements excluding the placeholder
- [x] 2.6 Add `selectFirstReport()` selecting option at index 1 in the `<select>`
- [x] 2.7 Add `isReportDescriptionVisible()` checking `.report-desc` is visible
- [x] 2.8 Add `clickRunReport()` clicking button with text `执行报表`
- [x] 2.9 Add `isResultTableVisible()` checking `table` inside `.result` is visible
- [x] 2.10 Add `getResultRowCount()` returning number of `tbody tr` rows
- [x] 2.11 Add `enterCustomSql(String sql)` filling the `<textarea>`
- [x] 2.12 Add `clickRunSql()` clicking button with text `执行SQL`
- [x] 2.13 Add `isResultSectionVisible()` checking `.result` div is visible
- [x] 2.14 Add `isErrorVisible()` checking `.error` div is visible

## 3. Cucumber Hooks

- [x] 3.1 Create package `com.legacy.report.e2e.hooks`
- [x] 3.2 Create `CucumberHooks.java` with static `Playwright` and `Browser` fields initialized once in a `@BeforeAll`-equivalent static block
- [x] 3.3 Add `@Before` hook that creates a new `Page` and stores it in a thread-local or shared context object
- [x] 3.4 Add `@After` hook that takes a screenshot to `target/screenshots/<scenario>.png` on failure, then closes the `Page`
- [x] 3.5 Create `ScenarioContext.java` to hold the current `Page` instance and share it between hooks and steps

## 4. Step Definitions

- [x] 4.1 Create package `com.legacy.report.e2e.steps`
- [x] 4.2 Create `ReportViewerSteps.java` with `@Autowired`-free constructor injecting `ScenarioContext`
- [x] 4.3 Implement step: `Given the report viewer is open` — calls `ReportViewerPage.open()`
- [x] 4.4 Implement step: `Then the page title is {string}` — asserts `getPageTitle()` equals expected
- [x] 4.5 Implement step: `Then the report dropdown is visible` — asserts `isReportDropdownVisible()`
- [x] 4.6 Implement step: `Then the dropdown contains at least {int} report option` — asserts `getReportOptionCount() >= n`
- [x] 4.7 Implement step: `When I select the first report from the dropdown` — calls `selectFirstReport()`
- [x] 4.8 Implement step: `Then the report description section is visible` — asserts `isReportDescriptionVisible()`
- [x] 4.9 Implement step: `When I click the {string} button` — clicks button by text via page object
- [x] 4.10 Implement step: `Then the results table is displayed with at least {int} row` — asserts `getResultRowCount() >= n`
- [x] 4.11 Implement step: `When I type {string} into the SQL textarea` — calls `enterCustomSql()`
- [x] 4.12 Implement step: `Then the results section is displayed` — asserts `isResultSectionVisible()`
- [x] 4.13 Implement step: `Then an error message is displayed` — asserts `isErrorVisible()`

## 5. Feature Files

- [x] 5.1 Create `e2e/src/test/resources/features/report_list.feature` with scenario: page title and dropdown with options visible on load
- [x] 5.2 Create `e2e/src/test/resources/features/report_execution.feature` with scenario: select first report → description visible → click run → table with rows visible
- [x] 5.3 Create `e2e/src/test/resources/features/custom_sql.feature` with scenario: enter `SELECT 1 AS result` → click run → results section visible
- [x] 5.4 Add second scenario to `custom_sql.feature`: enter `NOT VALID SQL` → click run → error message visible

## 6. Cucumber Runner

- [x] 6.1 Create package `com.legacy.report.e2e.runners`
- [x] 6.2 Create `CucumberRunner.java` annotated with `@Suite`, `@IncludeEngines("cucumber")`, `@SelectClasspathResource("features")`
- [x] 6.3 Add `@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/report.html")`
- [x] 6.4 Add `@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.legacy.report.e2e")`

## 7. Verification

- [ ] 7.1 Start Spring Boot backend (`mvn spring-boot:run` in `backend/`) and Angular frontend (`ng serve` in `frontend/`)
- [ ] 7.2 Run `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"` in `e2e/` to download browser
- [ ] 7.3 Run `mvn test` in `e2e/` and confirm all 4 scenarios pass
- [ ] 7.4 Verify `e2e/target/cucumber-reports/report.html` exists and opens in a browser
- [ ] 7.5 Confirm report shows feature names, scenario names, and green pass status for all steps
- [ ] 7.6 Intentionally break one step assertion to confirm screenshot is saved to `target/screenshots/`
