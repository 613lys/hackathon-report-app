## Context

The project is a full-stack app: Angular 17 frontend (`frontend/`, port 4200) backed by Spring Boot 2.1 (`backend/`, port 8080). Backend unit/integration tests exist with JaCoCo coverage. There are no browser-level e2e tests. The goal is to add a BDD e2e suite that drives the Angular UI via a real browser and asserts on visible behaviour.

## Goals / Non-Goals

**Goals:**
- Standalone `e2e/` Maven module with no coupling to `backend/` source code
- Playwright Java drives Chromium browser against the running Angular frontend
- Cucumber 7 BDD scenarios expressed in Gherkin, covering 3 user flows
- Page Object Model isolates locator details from step definitions
- Cucumber HTML report generated at `e2e/target/cucumber-reports/report.html`

**Non-Goals:**
- CI/CD pipeline integration (local dev only)
- Visual regression testing
- Mobile or cross-browser testing (Chromium only)
- Backend API testing without the UI layer
- Angular unit tests or component tests

## Decisions

### Decision 1: Separate `e2e/` Maven module (not inside `backend/`)
Keeps e2e dependencies (Playwright, Cucumber) isolated. `backend/` stays a pure Spring Boot module. `e2e/pom.xml` has no parent and declares its own dependencies. Alternative (profile inside `backend/`) rejected because it conflates unit/integration and e2e lifecycles.

### Decision 2: Playwright Java over Selenium
Playwright has built-in auto-wait, reliable locators, no WebDriver setup overhead, and first-class Java support via `com.microsoft.playwright:playwright:1.42.0`. Selenium rejected due to flaky waits and driver management complexity.

### Decision 3: Cucumber 7 + JUnit Platform Suite runner
`io.cucumber:cucumber-java:7.15.0` + `io.cucumber:cucumber-junit-platform-engine:7.15.0` + `org.junit.platform:junit-platform-suite:1.10.2`. Annotated runner class (`@Suite`, `@SelectClasspathResource`, `@ConfigurationParameter`) drives feature file discovery. JUnit 4 runner rejected (deprecated).

### Decision 4: Page Object Model
`ReportViewerPage.java` wraps all Playwright locators. Step definitions in `ReportViewerSteps.java` call page object methods only — no raw Playwright calls in steps. This keeps feature files readable and locator changes contained to one class.

### Decision 5: baseUrl via system property with default
`System.getProperty("e2e.baseUrl", "http://localhost:4200")` in the page object. Override via `mvn test -De2e.baseUrl=http://...` without code changes.

### Decision 6: Browser lifecycle — one Playwright instance per test run, one Browser, one Page per scenario
`@Before` hook opens a new page; `@After` hook closes it and takes a screenshot on failure. Playwright instance and browser created/destroyed in a `CucumberHooks` class using static fields initialized once.

### Decision 7: Cucumber HTML report
Native `io.cucumber.reporting` HTML plugin via `@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "html:target/cucumber-reports/report.html")`. No Allure dependency. Sufficient for local dev review.

## Risks / Trade-offs

- **Selector fragility**: No `data-testid` attributes on DOM elements. Locators use CSS class (`.result table`) and button text (`"执行报表"`). Chinese text in selectors could break if UI language changes. → Mitigation: document selector strategy in page object, add `data-testid` as a future improvement.
- **App must be running**: Tests fail fast if frontend/backend are not up. → Mitigation: README documents startup order.
- **Playwright browser download**: First run requires `mvn exec:java ... install chromium`. → Mitigation: document as a one-time setup step.
- **H2 vs real DB**: Backend in dev mode uses H2 + `data.sql` seed data. Tests depend on seed reports existing. → Mitigation: scenarios use `the first report` to avoid hardcoding report IDs.
