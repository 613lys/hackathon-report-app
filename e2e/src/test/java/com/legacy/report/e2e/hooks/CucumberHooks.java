package com.legacy.report.e2e.hooks;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CucumberHooks {

    private static final Playwright playwright;
    private static final Browser browser;

    static {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setChannel("chrome")
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            browser.close();
            playwright.close();
        }));
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        Page page = ScenarioContext.getPage();
        if (page != null) {
            byte[] screenshot = page.screenshot();
            scenario.attach(screenshot, "image/png", scenario.getName());
        }
    }

    @Before
    public void beforeScenario() {
        Page page = browser.newPage();
        ScenarioContext.setPage(page);
    }

    @After
    public void afterScenario(Scenario scenario) {
        Page page = ScenarioContext.getPage();
        if (page != null) {
            if (scenario.isFailed()) {
                try {
                    String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9\\-_]", "_");
                    Path screenshotPath = Paths.get("target/screenshots", safeName + ".png");
                    Files.createDirectories(screenshotPath.getParent());
                    page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath));
                } catch (Exception e) {
                    // Screenshot is best-effort; do not fail the scenario on screenshot errors
                }
            }
            page.close();
        }
        ScenarioContext.clear();
    }
}
