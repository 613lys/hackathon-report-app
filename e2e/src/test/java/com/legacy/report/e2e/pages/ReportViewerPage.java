package com.legacy.report.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

public class ReportViewerPage {

    private final Page page;
    private static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:4200");

    public ReportViewerPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate(BASE_URL);
    }

    public String getPageTitle() {
        return page.locator("h1").textContent().trim();
    }

    public boolean isReportDropdownVisible() {
        return page.locator("select").isVisible();
    }

    public int getReportOptionCount() {
        return (int) page.locator("select option").count() - 1;
    }

    public void selectFirstReport() {
        page.locator("select").selectOption(new SelectOption().setIndex(1));
    }

    public boolean isReportDescriptionVisible() {
        return page.locator(".report-desc").isVisible();
    }

    public void clickButton(String text) {
        page.locator("button:has-text(\"" + text + "\")").click();
        waitForResultOrError();
    }

    public void clickRunReport() {
        page.locator("button:has-text(\"\u6267\u884c\u62a5\u8868\")").click();
        waitForResultOrError();
    }

    public boolean isResultTableVisible() {
        return page.locator(".result table").isVisible();
    }

    public int getResultRowCount() {
        return (int) page.locator(".result tbody tr").count();
    }

    public void enterCustomSql(String sql) {
        page.locator("textarea").fill(sql);
    }

    public void clickRunSql() {
        page.locator("button:has-text(\"\u6267\u884cSQL\")").click();
        waitForResultOrError();
    }

    public boolean isResultSectionVisible() {
        return page.locator(".result").isVisible();
    }

    public boolean isErrorVisible() {
        return page.locator(".error").isVisible();
    }

    private void waitForResultOrError() {
        page.locator(".result, .error").first().waitFor(
            new Locator.WaitForOptions().setTimeout(15000)
        );
    }
}
