package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;

public class SwiggyAutomationUtils {

    public static WebElement findElementWithMultipleSelectors(WebDriver driver, String... xpaths) {
        for (String xpath : xpaths) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        throw new NoSuchElementException("Could not find element with any of the provided selectors");
    }

    public static WebElement waitAndFindElement(WebDriverWait wait, String... xpaths) {
        for (String xpath : xpaths) {
            try {
                return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        throw new NoSuchElementException("Could not find element with any of the provided selectors within timeout");
    }

    public static void safeClick(WebDriver driver, WebElement element) {
        try {
            // Try regular click first
            element.click();
        } catch (ElementClickInterceptedException e) {
            // If regular click fails, try JavaScript click
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
        }
    }

    public static void scrollToElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean isElementPresent(WebDriver driver, String xpath) {
        try {
            driver.findElement(By.xpath(xpath));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void handlePopups(WebDriver driver) {
        // Common popup selectors for Swiggy
        String[] popupSelectors = {
            "//button[contains(@class,'close')] | //div[contains(@class,'close')]",
            "//button[contains(text(),'✕')] | //span[contains(text(),'✕')]",
            "//button[contains(@aria-label,'close')] | //div[contains(@aria-label,'close')]",
            "//button[contains(text(),'Later')] | //div[contains(text(),'Later')]",
            "//button[contains(text(),'Skip')] | //div[contains(text(),'Skip')]"
        };

        for (String selector : popupSelectors) {
            try {
                List<WebElement> popups = driver.findElements(By.xpath(selector));
                for (WebElement popup : popups) {
                    if (popup.isDisplayed()) {
                        safeClick(driver, popup);
                        Thread.sleep(1000);
                        break;
                    }
                }
            } catch (Exception e) {
                // Continue to next popup selector
            }
        }
    }

    public static void waitForPageLoad(WebDriver driver) {
        try {
            Thread.sleep(3000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // Wait for page to be ready
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                webDriver -> js.executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            System.out.println("Page load wait completed with exception: " + e.getMessage());
        }
    }
}
