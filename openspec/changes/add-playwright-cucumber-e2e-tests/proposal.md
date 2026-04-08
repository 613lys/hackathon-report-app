## Why

The project has unit and integration tests for the backend but no browser-level end-to-end tests. A dedicated e2e suite using Playwright Java + Cucumber allows verifying the full user journey through the Angular frontend against the live Spring Boot backend, expressed in human-readable BDD scenarios.

## What Changes

- Add a new standalone Maven module `e2e/` with no dependency on `backend/` source code
- Add Playwright Java for browser automation (Chromium)
- Add Cucumber for BDD scenario definition and execution
- Write `.feature` files covering report list loading, report execution, and custom SQL
- Configure Cucumber HTML report output under `e2e/target/cucumber-reports/`
- Document browser installation and local run instructions in a README

## Capabilities

### New Capabilities
- `playwright-cucumber-e2e`: Browser-level e2e test suite using Playwright Java + Cucumber 7, with Page Object Model for the Angular report viewer UI and Cucumber HTML reporting

### Modified Capabilities

## Impact

- New `e2e/` directory at repo root (standalone Maven module, not part of any existing parent)
- No changes to `backend/` or `frontend/` source code
- Requires `ng serve` (port 4200) and Spring Boot (port 8080) running before executing tests
- Playwright requires one-time browser download (`chromium`) via Maven exec plugin
