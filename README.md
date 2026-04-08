# Windsurf Hackathon — Project Topics

---

## Overview

This document outlines the five project topics available for this hackathon. Each topic focuses on real-world engineering challenges, with an emphasis on practical impact, code quality, and demonstrable results.

## Topic 1(Dev workflow): Frontend Beautification & Workflow Showcase + Migrate Report SQL Business Logic to Java

**Objective:** Refactor and modernize an existing project on two complementary fronts — improve frontend visual design while also migrating complex report logic out of SQL and into a more maintainable backend architecture.

**Requirements:**
- **Frontend:** Use open-source Skills and frontend design tools to enhance UI aesthetics; document and present the end-to-end workflow for frontend fine-tuning
- **Backend:** Identify business logic currently residing in SQL (e.g., nested queries, conditional logic), migrate that logic to Java (or another suitable backend language) with proper abstraction
- Demonstrate how both refactors improve overall system maintainability and showcase a streamlined, AI-augmented development workflow
- Integrate AI-powered tools across the full stack development lifecycle

**Focus Areas:** UI/UX improvement, workflow automation, toolchain integration, architecture refactoring, code maintainability, separation of concerns

## Topic 2(Dev workflow): Requirements-to-Delivery E2E Workflow

**Objective:** Build a complete, automated workflow that takes a requirement from initial collection through to a ready-for-release change package (PRD/FRD).

**Workflow Stages:**

1. **Requirement Gathering** — Collect and capture requirements from stakeholders
2. **Requirement Clarification** — Refine ambiguous or incomplete requirements into clear, actionable statements
3. **ADR Generation** — Produce Architecture Decision Records documenting key technical choices
4. **Test Generation** — Auto-generate test cases and test plans from the clarified requirements
5. **Code Completion** — Implement the requirement, with AI-assisted code generation
6. **Implementation Testing** — Run and validate tests against the delivered code
7. **Change Preparation** — Package the change with all supporting artifacts
8. **PRD/FRD Output** — Deliver a finalized Product or Functional Requirements Document

**Requirements:**
- Demonstrate the full end-to-end flow with a realistic sample requirement
- Each stage should produce tangible artifacts (e.g., clarified spec, ADR, tests, code, test results, PRD)
- Show how AI tools assist at each stage of the workflow
- Document the workflow itself so it can be replicated for future use
- **Validation Strategy:** Throughout the development process, continuously verify that new changes do not break existing functionality — leverage integration tests and/or end-to-end tests to ensure regression safety after each stage
- **Server Security Hooks:** Implement control hooks that enforce security policies on the codebase during development — e.g., pre-commit or CI-gated checks for hardcoded secrets, SQL injection risks, insecure dependencies, or anomalous code patterns — to ensure every change passes a security baseline before reaching the delivery stage

**Focus Areas:** End-to-end process automation, AI-augmented development lifecycle, requirements-to-delivery traceability, regression safety, server security control hooks

---

## Topic 3(Documentation): Architecture Visualization & Business Logic Documentation

**Objective:** Produce clear architectural artifacts and documentation for an existing project.

**This topic has two complementary directions:**

### Direction A — API & Report SQL & UI module Business Logic Documentation
- Take complex API & Report SQL & UI module business logic currently in production and reorganize it into clear, readable documentation
- Goal is to make previously opaque logic accessible to new team members

### Direction B — Architecture Diagram Generation
- Generate architecture diagrams covering:
  - Upstream and downstream system relationships
  - Data flow paths
  - Data models and their interactions

**Focus Areas:** System comprehension, knowledge transfer, documentation quality

---

## Topic 4(Test automation): Frontend End-to-End (E2E) Automation Testing

**Objective:** Establish a robust E2E automated testing framework for an existing frontend application.

**Requirements:**
- Set up E2E test infrastructure (frameworks such as Playwright, Cypress, or similar)
- Write automated tests covering critical user journeys
- Demonstrate how these tests protect against regressions during future development

**Background:** An end-to-end automation testing platform is already available as a foundation to build upon.

**Focus Areas:** Test coverage, automation reliability, CI integration

---

## Topic 5(Test automation): Backend Functional & Non-Functional Testing

**Objective:** Strengthen backend quality assurance through comprehensive testing across multiple dimensions.

**Requirements:**
- **Stress Testing:** Validate system stability and performance under high concurrency
- **Unit Testing:** Achieve meaningful code-level coverage for backend logic
- **API Testing:** Ensure all Backend Service APIs function correctly and return expected results

**Focus Areas:** System reliability, code correctness, API contract validation

---

## Backend 测试执行指引

本仓库新增了覆盖单测、压测、API 测试的统一方案：

- `./backend/gradlew test`：运行所有 JUnit 单元测试，依赖 `src/test/java` 与 `src/test/resources/fixtures` 中的夹具。
- `./backend/gradlew stressTest -Dstress.virtualUsers=50 -Dstress.iterations=3`：在随机端口启动 Spring Boot 应用，模拟 Maker/Checker 高并发流程，并在阈值超出时失败构建。
- `./backend/gradlew apiTest -DapiBaseUrl=http://localhost:8080`：通过 RestAssured 执行认证、报表、报表运行及审计接口的正负向用例。
- `./backend/gradlew jacocoTestReport jacocoTestCoverageVerification`：生成 Jacoco 报告并校验 80% 行 / 70% 分支覆盖率。

测试资产位于：

- `backend/src/test/java`：单测、仓储测试及夹具工厂。
- `backend/src/stressTest/java`：压测场景（使用 `ReportWorkflowStressTest`）。
- `backend/src/apiTest/java`：API 回归用例（`ReportApiTest`）。

详情请参考 OpenSpec 变更 `backend-testing-suite` 下的 proposal/design/specs/tasks 文件。