package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Keys;
import java.util.List;
import java.util.Arrays;
import java.time.Duration;

public class SwiggyAutomation {

    public static void main(String[] args) {

        // 1. Setup WebDriver
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Step 1: Login to Swiggy
            System.out.println("LOG: Navigating to Swiggy...");
            driver.get("https://www.swiggy.com/");

            WebElement initialLoginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Login']")));
            System.out.println("LOG: Clicking on the main 'Login' button.");
            initialLoginButton.click();

            WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mobile")));
            phoneInput.sendKeys("YOUR_PHONE_NUMBER"); // <-- IMPORTANT: Enter your phone number here
            System.out.println("LOG: Phone number entered.");

            WebElement modalLoginButton = driver.findElement(By.xpath("//a[text()='Login']"));
            modalLoginButton.click();

            System.out.println("LOG: WAITING FOR 30 SECONDS... Please enter the OTP manually in the browser.");
            Thread.sleep(30000);

            // Step 2: Validate Page Title and URL
            System.out.println("LOG: Resuming script. Validating page details post-login...");
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("https://www.swiggy.com/")));
            System.out.println("CONSOLE LOG - Page Title: " + driver.getTitle());
            System.out.println("CONSOLE LOG - Current URL: " + driver.getCurrentUrl());

            // Step 3: Enter Location for Delivery (Post-Login) - REWORKED
            setLocation(driver, wait, "Bengaluru");

            // Step 4: Search for a Restaurant
            WebElement searchLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/search')]")));
            System.out.println("LOG: Found new search link. Clicking on it.");
            searchLink.click();

            // Robust search input selector
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@placeholder,'Search') or contains(@placeholder,'restaurant') or contains(@placeholder,'item')]")
            ));
            String restaurant = "Domino's Pizza";
            searchInput.sendKeys(restaurant);
            System.out.println("LOG: Searched for restaurant: '" + restaurant + "'");

            // First restaurant from results (use union XPath instead of ExpectedConditions.or)
            WebElement firstRestaurant = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//div[contains(@class,'styles_restaurantName')] | //a[contains(@href,'/restaurants') or contains(@href,'/restaurant')])[1]")
            ));
            System.out.println("LOG: Clicking on the first restaurant: " + firstRestaurant.getText());
            firstRestaurant.click();

            // Step 5: Select the second food item and Add (use union XPath)
            WebElement secondAdd = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//button[normalize-space()='ADD' or contains(.,'Add')] | //div[contains(@class,'Add') or contains(@data-testid,'add-button')]//button)[2]")
            ));
            System.out.println("LOG: Clicking 'Add' on the second food item.");
            secondAdd.click();

            // Optional: handle customization modal if it appears
            try {
                WebElement addConfirm = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Add Item') or normalize-space()='Add']")));
                addConfirm.click();
                System.out.println("LOG: Confirmed item add in customization modal.");
            } catch (Exception ignore) { /* no customization needed */ }

            // Step 6: View Cart
            WebElement viewCart = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[translate(normalize-space(),'view cart','VIEW CART')='VIEW CART']/ancestor::button[1] | //a[contains(@href,'/checkout') and (contains(.,'Cart') or contains(.,'Checkout') or contains(.,'CHECKOUT'))]")
            ));
            System.out.println("LOG: Clicking 'View Cart'.");
            viewCart.click();

            // Step 7: Increase Quantity to 2
            WebElement increment = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@aria-label,'increase') or contains(@data-testid,'increment') or normalize-space()='+']")
            ));
            increment.click();
            System.out.println("LOG: Increased quantity to 2.");

            // Step 8: Enter Delivery Address (if prompted)
            try {
                // Address form may auto-open on first checkout
                WebElement door = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[@name='door' or contains(@placeholder,'Door') or contains(@placeholder,'Flat')]")
                ));
                door.clear(); door.sendKeys("12A");

                WebElement landmark = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[contains(@placeholder,'Landmark') or @name='landmark']")
                ));
                landmark.clear(); landmark.sendKeys("Near city park");

                // Select 'Home' address type
                try {
                    WebElement homeType = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[self::button or self::div or self::span][contains(.,'Home')][@role='radio' or contains(@class,'address') or not(@role)]")
                    ));
                    homeType.click();
                } catch (Exception ignore) { /* default type */ }

                // Save Address & Proceed
                WebElement saveAddr = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(.,'Save Address') or contains(.,'Save & Proceed') or contains(.,'Save Address & Proceed')]")
                ));
                saveAddr.click();
                System.out.println("LOG: Address saved and proceeding.");
            } catch (Exception ignore) {
                System.out.println("LOG: Address form not shown; using existing address.");
            }

            // Step 9: Proceed to Payment Page
            WebElement proceedToPay = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Proceed to Pay') or contains(.,'PROCEED TO PAY') or contains(.,'Proceed to payment')]")
            ));
            proceedToPay.click();
            System.out.println("LOG: Proceeding to payment page...");

            // Replace mixed-type or(...) with boolean lambda
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains("payment")
                    || !d.findElements(By.xpath("//*[contains(.,'Payment') and contains(.,'Options')] | //div[contains(@class,'payment')]")).isEmpty());
            System.out.println("\nLOG: Reached payment page (no payment details entered).");

        } catch (Exception e) {
            System.err.println("❌ An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the browser
            System.out.println("LOG: Closing the browser.");
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // --- Helpers ---

    // Robust location setter
    private static void setLocation(WebDriver driver, WebDriverWait wait, String locationText) {
        System.out.println("LOG: Setting location -> " + locationText);

        // Open location UI (try multiple triggers, retry if input not visible)
        List<By> triggers = Arrays.asList(
                By.xpath("//span[contains(@class,'location-name')]"),
                By.xpath("//span[contains(text(),'Enter your delivery location')]"),
                By.xpath("//div[contains(@class,'location') and (contains(.,'Enter your delivery location') or .//input)]"),
                By.xpath("//input[contains(@placeholder,'delivery location') or contains(@placeholder,'Search for area')]"),
                By.xpath("//span[contains(@class,'icon-downArrow') or contains(@class,'icon-upArrow')]"),
                By.xpath("//header//*[contains(translate(.,'LOCATION','location'),'location')]/ancestor::*[self::div or self::button][1]")
        );
        WebElement input = null;
        for (By by : triggers) {
            try {
                WebElement t = new WebDriverWait(driver, Duration.ofSeconds(6))
                        .until(ExpectedConditions.elementToBeClickable(by));
                t.click();
                // try to find input right after clicking
                input = tryFindLocationInput(driver);
                if (input != null) break;
            } catch (Exception ignore) { /* try next */ }
        }
        if (input == null) {
            // one more attempt to find the input directly
            input = tryFindLocationInput(driver);
        }
        if (input == null) throw new RuntimeException("Location input not found");

        // Clear and type
        try {
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);
        } catch (Exception ignore) { try { input.clear(); } catch (Exception ig) {} }
        input.sendKeys(locationText);
        System.out.println("LOG: Typed location: '" + locationText + "'");

        // Wait for a suggestion and click it; else keyboard fallback
        WebElement suggestion = new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            List<WebElement> items = d.findElements(By.xpath(
                    "//div[@role='listbox']//div[@role='option']" +
                    " | //li[@role='option']" +
                    " | //button[contains(@class,'_2W-T9')]" +
                    " | //ul//li//*[self::button or self::div]"
            ));
            for (WebElement el : items) {
                if (el.isDisplayed()) return el;
            }
            return null;
        });

        if (suggestion != null) {
            try {
                suggestion.click();
                System.out.println("LOG: Clicked location suggestion.");
            } catch (Exception e) {
                // fallback to keyboard if click fails
                input.sendKeys(Keys.ARROW_DOWN);
                input.sendKeys(Keys.ENTER);
                System.out.println("LOG: Selected suggestion via keyboard (fallback).");
            }
        } else {
            input.sendKeys(Keys.ARROW_DOWN);
            input.sendKeys(Keys.ENTER);
            System.out.println("LOG: Selected suggestion via keyboard.");
        }

        // Verify location context (boolean lambda to avoid mixed-type or)
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                d.getCurrentUrl().toLowerCase().contains("restaurants")
                        || !d.findElements(By.xpath("//a[contains(@href, '/search')]")).isEmpty()
        );
        System.out.println("LOG: Location set successfully.");
    }

    // Try to locate the location input using multiple robust selectors
    private static WebElement tryFindLocationInput(WebDriver driver) {
        List<By> inputBys = Arrays.asList(
                By.cssSelector("input#location"),
                By.cssSelector("input[placeholder*='delivery'][type='text']"),
                By.cssSelector("input[placeholder*='Search for area'][type='text']"),
                By.xpath("//input[contains(@placeholder,'Enter your delivery location')]"),
                By.xpath("//input[contains(@placeholder,'Search for area') or contains(@placeholder,'street') or contains(@placeholder,'area')]"),
                By.xpath("//div[contains(@role,'dialog') or contains(@class,'modal')]//input")
        );
        for (By by : inputBys) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) {
                if (el.isDisplayed() && el.isEnabled()) return el;
            }
        }
        return null;
    }
}
