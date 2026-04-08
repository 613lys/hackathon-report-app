package com.legacy.report.e2e.steps;

import com.legacy.report.e2e.hooks.ScenarioContext;
import com.legacy.report.e2e.pages.ReportViewerPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportViewerSteps {

    private ReportViewerPage reportPage;

    private ReportViewerPage getPage() {
        if (reportPage == null) {
            reportPage = new ReportViewerPage(ScenarioContext.getPage());
        }
        return reportPage;
    }

    @Given("the report viewer is open")
    public void theReportViewerIsOpen() {
        getPage().open();
    }

    @Then("the page title is {string}")
    public void thePageTitleIs(String expectedTitle) {
        assertEquals(expectedTitle, getPage().getPageTitle());
    }

    @Then("the report dropdown is visible")
    public void theReportDropdownIsVisible() {
        assertTrue(getPage().isReportDropdownVisible());
    }

    @Then("the dropdown contains at least {int} report option")
    public void theDropdownContainsAtLeastReportOption(int minCount) {
        int actual = getPage().getReportOptionCount();
        assertTrue(actual >= minCount,
            "Expected at least " + minCount + " report option(s), but got " + actual);
    }

    @When("I select the first report from the dropdown")
    public void iSelectTheFirstReportFromTheDropdown() {
        getPage().selectFirstReport();
    }

    @Then("the report description section is visible")
    public void theReportDescriptionSectionIsVisible() {
        assertTrue(getPage().isReportDescriptionVisible());
    }

    @When("I click the {string} button")
    public void iClickTheButton(String buttonText) {
        getPage().clickButton(buttonText);
    }

    @Then("the results table is displayed with at least {int} row")
    public void theResultsTableIsDisplayedWithAtLeastRow(int minRows) {
        assertTrue(getPage().isResultTableVisible(), "Result table should be visible");
        int actual = getPage().getResultRowCount();
        assertTrue(actual >= minRows,
            "Expected at least " + minRows + " row(s), but got " + actual);
    }

    @When("I type {string} into the SQL textarea")
    public void iTypeIntoTheSQLTextarea(String sql) {
        getPage().enterCustomSql(sql);
    }

    @Then("the results section is displayed")
    public void theResultsSectionIsDisplayed() {
        assertTrue(getPage().isResultSectionVisible());
    }

    @Then("an error message is displayed")
    public void anErrorMessageIsDisplayed() {
        assertTrue(getPage().isErrorVisible());
    }
}
