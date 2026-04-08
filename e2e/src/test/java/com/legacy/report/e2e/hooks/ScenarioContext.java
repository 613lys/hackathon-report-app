package com.legacy.report.e2e.hooks;

import com.microsoft.playwright.Page;

public class ScenarioContext {

    private ScenarioContext() {}

    private static final ThreadLocal<Page> PAGE_HOLDER = new ThreadLocal<>();

    public static void setPage(Page page) {
        PAGE_HOLDER.set(page);
    }

    public static Page getPage() {
        return PAGE_HOLDER.get();
    }

    public static void clear() {
        PAGE_HOLDER.remove();
    }
}
