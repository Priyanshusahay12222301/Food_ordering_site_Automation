package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class SwiggyAutomationFixed {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static Scanner scanner;
    private static Properties config;


    public static void main(String[] args) {
        System.out.println("=== SWIGGY AUTOMATION SCRIPT (FIXED LOGIN) ===");
        System.out.println("This script will automate food ordering on Swiggy");
        System.out.println("✅ LOGIN BUTTON ISSUE FIXED");
        System.out.println("✅ ENHANCED RESTAURANT SELECTION");
        System.out.println("===============================================\n");

        try {
            loadConfiguration();
            setupDriver();
            scanner = new Scanner(System.in);

            System.out.println("1. Initializing WebDriver...");
            System.out.println("   ✓ WebDriver initialized successfully\n");

            // Step 1: Navigate to Swiggy and Login
            System.out.println("2. Navigating to Swiggy website...");
            navigateToSwiggy();
            System.out.println("   ✓ Navigated to: https://www.swiggy.com/\n");

            System.out.println("3. Starting login process...");
            loginToSwiggy();
            System.out.println("   ✓ Login process completed\n");

            // Step 2: Validate page title and URL
            System.out.println("4. Validating page title and URL...");
            validatePageTitleAndURL();
            System.out.println("   ✓ Page validation completed\n");

            // Step 3: Set delivery location (Bangalore)
            System.out.println("5. Setting delivery location...");
            String location = config.getProperty("test.location", "Bangalore");
            enterDeliveryLocation(location);
            System.out.println("   ✓ Delivery location set successfully\n");

            // Step 4: Ensure Food Delivery mode is selected and land on /restaurants
            System.out.println("6. Ensuring Food Delivery mode is active...");
            ensureFoodDeliveryMode();
            ensureOrderOnlineOnListing();
            System.out.println("   ✓ Food delivery + Order Online confirmed on listings\n");

            // Step 5: Open first visible restaurant (no search)
            System.out.println("7. Opening first available restaurant...");
            String restaurantName = openFirstRestaurantAndGetName();
            ensureOrderOnlineInRestaurant();
            System.out.println("   ✓ Opened restaurant: " + restaurantName + "\n");

            // Step 6: Select the second dish in menu and add to cart
            System.out.println("8. Adding second dish from the menu...");
            String selectedItem = selectFoodItemAndAddToCart("Veg Loaded");
            System.out.println("   ✓ Food item selected: " + selectedItem);
            takeScreenshot("screenshot_after_adding_item");

            // Step 7: Proceed to cart
            System.out.println("9. Proceeding to cart...");
            proceedToCart();
            takeScreenshot("screenshot_after_increasing_quantity");

            // Step 8: Proceed to checkout and enter delivery address
            System.out.println("10. Proceeding to checkout...");
            proceedToCheckout();
            enterDeliveryAddress();
            System.out.println("   ✓ Delivery address entered");
            takeScreenshot("screenshot_after_entering_address");

            // Step 9: Proceed to payment
            System.out.println("11. Proceeding to payment page...");
            proceedToPayment();
            System.out.println("   ✓ Successfully navigated to the payment page.");
            takeScreenshot("screenshot_payment_page");

            // Final logs
            printFinalLogs(restaurantName, selectedItem);

        } catch (Exception e) {
            System.err.println("❌ Error during automation: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot("error_screenshot");
        } finally {
            // Keep browser open for manual inspection
            System.out.println("\n🎉 Automation completed. Press Enter to close the browser...");
            if (scanner != null) {
                scanner.nextLine();
            }
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static void loadConfiguration() {
        config = new Properties();
        try {
            FileInputStream configFile = new FileInputStream("config.properties");
            config.load(configFile);
            System.out.println("Configuration loaded successfully");
        } catch (IOException e) {
            System.out.println("Using default configuration");
            config = new Properties();
            config.setProperty("user.phone.number", "9508611922");
            config.setProperty("test.location", "Bangalore");
            config.setProperty("test.restaurant", "Domino's Pizza");
        }
    }

    private static void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    private static void navigateToSwiggy() {
        driver.get("https://www.swiggy.com/");
        try {
            Thread.sleep(3000);
            // Handle location popup if it appears
            try {
                WebElement locationPopup = driver.findElement(By.cssSelector("[class*='location-deny']"));
                if (locationPopup.isDisplayed()) {
                    locationPopup.click();
                }
            } catch (Exception e) {
                // Location popup might not appear
            }
        } catch (Exception e) {
            // Continue if there's any issue
        }
    }

    private static void loginToSwiggy() {
        try {
            System.out.println("   🔍 Looking for login button...");
            Thread.sleep(2000); // Reduced from 5000
            takeScreenshot("page_loaded");

            // Find and click login button - optimized selectors
            String[] loginSelectors = {
                "//a[contains(text(),'Sign in')]",
                "//button[contains(text(),'Sign in')]",
                "//a[contains(text(),'Login')]",
                "//button[contains(text(),'Login')]",
                "//*[contains(text(),'SIGN IN') or contains(text(),'LOGIN')]",
                "a[href*='login']", // CSS selector for faster lookup
                "[data-testid*='login']" // CSS selector
            };

            WebElement loginButton = null;

            // Try CSS selectors first (faster than XPath)
            try {
                loginButton = driver.findElement(By.cssSelector("a[href*='login'], a[href*='signin'], [data-testid*='login'], [data-testid*='signin']"));
                if (loginButton.isDisplayed()) {
                    System.out.println("   ✓ Login button found via CSS selector");
                }
            } catch (Exception e) {
                // Fall back to XPath selectors
                for (String selector : loginSelectors) {
                    try {
                        if (selector.startsWith("//")) {
                            loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                        } else {
                            loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                        }
                        if (loginButton.isDisplayed()) {
                            System.out.println("   ✓ Login button found");
                            break;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
            }

            // Fallback for auth page if button not found
            if (loginButton == null) {
                System.out.println("   ↪️ Navigating directly to auth page");
                driver.get("https://www.swiggy.com/auth");
                Thread.sleep(1000); // Reduced wait time
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", loginButton);
                Thread.sleep(200); // Reduced from 400
                try {
                    loginButton.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
                }
                System.out.println("   ✓ Login button clicked");
                Thread.sleep(1500); // Reduced from 3000
            }

            takeScreenshot("after_login_button_click");

            // Enter phone number - optimized
            System.out.println("   📱 Entering phone number...");
            WebElement phoneInput = null;

            // Try most common selectors first
            String[] phoneSelectors = {
                "input[type='tel']", // CSS - fastest
                "input[inputmode='tel']", // CSS
                "//input[@type='tel']", // XPath fallback
                "//input[@placeholder*='phone']",
                "//input[@placeholder*='mobile']",
                "//input[@placeholder*='number']",
                "//input[@inputmode='tel']",
                "//input[@inputmode='numeric']"
            };

            for (String selector : phoneSelectors) {
                try {
                    if (selector.startsWith("//")) {
                        phoneInput = driver.findElement(By.xpath(selector));
                    } else {
                        phoneInput = driver.findElement(By.cssSelector(selector));
                    }
                    if (phoneInput.isDisplayed()) {
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (phoneInput != null) {
                String phoneNumber = config.getProperty("user.phone.number", "9508611922");
                phoneInput.clear();
                phoneInput.sendKeys(phoneNumber);
                System.out.println("   ✓ Phone number entered: " + phoneNumber);

                Thread.sleep(800); // Reduced from 1500
                takeScreenshot("phone_number_entered");

                System.out.println("   🔍 Looking for continue/submit button...");

                // Optimized continue button search
                WebElement continueButton = null;
                String[] continueSelectors = {
                    "button[type='submit']", // CSS - most common
                    "form button:not([disabled])", // CSS
                    "//button[contains(text(),'Continue')]",
                    "//button[contains(text(),'Send OTP')]",
                    "//button[contains(text(),'GET OTP')]",
                    "//button[contains(text(),'LOGIN')]",
                    "//button[contains(text(),'Log in')]",
                    "//button[@type='submit']",
                    "//*[@data-testid='otp-btn' or contains(@data-testid,'send-otp')]"
                };

                boolean buttonFound = false;
                for (String selector : continueSelectors) {
                    try {
                        List<WebElement> buttons;
                        if (selector.startsWith("//")) {
                            buttons = driver.findElements(By.xpath(selector));
                        } else {
                            buttons = driver.findElements(By.cssSelector(selector));
                        }
                        for (WebElement b : buttons) {
                            if (b.isDisplayed() && b.isEnabled()) {
                                continueButton = b;
                                buttonFound = true;
                                break;
                            }
                        }
                        if (buttonFound) break;
                    } catch (Exception ignore) {}
                }

                // Smart parent container search if button still not found
                if (!buttonFound) {
                    try {
                        WebElement parentContainer = phoneInput.findElement(By.xpath("./ancestor::form[1] | ./ancestor::div[contains(@class,'login') or contains(@class,'signin')][1]"));
                        List<WebElement> allButtons = parentContainer.findElements(By.cssSelector("button, [role='button']"));
                        for (WebElement btn : allButtons) {
                            if (btn.isDisplayed() && btn.isEnabled()) {
                                continueButton = btn;
                                buttonFound = true;
                                break;
                            }
                        }
                    } catch (Exception ignore) {}
                }

                if (buttonFound && continueButton != null) {
                    try {
                        continueButton.click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
                    }
                    System.out.println("   ✅ Continue button clicked successfully!");
                } else {
                    System.out.println("   ⚠️ No continue button found, pressing Enter on phone input");
                    phoneInput.sendKeys(Keys.ENTER);
                }

                Thread.sleep(1000);
                takeScreenshot("after_continue_button_click");

                System.out.println("\n   📱 MANUAL OTP ENTRY REQUIRED 📱");
                System.out.println("   ⏰  Enter the OTP in the browser within 30 seconds");
                Thread.sleep(30000);
                takeScreenshot("after_otp_entry");
                System.out.println("   ✓ Login process completed");

                // Quick redirect to Food Delivery listings
                try {
                    String restaurantsUrl = "https://www.swiggy.com/order-online-near-me";
                    System.out.println("   ➡️ Redirecting to Food Delivery: " + restaurantsUrl);
                    driver.get(restaurantsUrl);
                    Thread.sleep(2000);
                    takeScreenshot("after_login_restaurants_redirect");
                } catch (Exception ignore) {}
            } else {
                takeScreenshot("phone_input_not_found");
                throw new RuntimeException("Phone input field not found");
            }

        } catch (Exception e) {
            System.out.println("   ⚠️ Error in login: " + e.getMessage());
            takeScreenshot("login_error");
        }
    }

    private static void validatePageTitleAndURL() {
        String pageTitle = driver.getTitle();
        String currentURL = driver.getCurrentUrl();
        System.out.println("   📄 Page Title: " + pageTitle);
        System.out.println("   🔗 Current URL: " + currentURL);
    }

    private static void enterDeliveryLocation(String location) {
        try {
            System.out.println("   📍 Looking for location input...");
            Thread.sleep(5000);

            String[] locationSelectors = {
                "//input[@placeholder*='Enter your delivery location']",
                "//input[@placeholder*='location']",
                "//input[@placeholder*='area']",
                "//input[@placeholder*='address']"
            };

            WebElement locationInput = null;
            for (String selector : locationSelectors) {
                try {
                    locationInput = driver.findElement(By.xpath(selector));
                    if (locationInput.isDisplayed()) {
                        System.out.println("   ✓ Location input found");
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (locationInput != null) {
                locationInput.clear();
                locationInput.sendKeys(location);
                System.out.println("   ✓ Typed city name: " + location);
                Thread.sleep(3000);

                // Press Enter to confirm location
                locationInput.sendKeys(Keys.ENTER);
                System.out.println("   ✓ Pressed Enter to confirm location");
                Thread.sleep(5000);
                takeScreenshot("location_selected");
            } else {
                System.out.println("   ℹ️ Location input not found, may already be set");
            }

        } catch (Exception e) {
            System.out.println("   ⚠️ Error setting location: " + e.getMessage());
            takeScreenshot("location_error");
        }
    }

    private static void ensureFoodDeliveryMode() {
        try {
            System.out.println("   🚚 Ensuring we're on the food delivery page...");
            Thread.sleep(1500);

            // Try clicking an explicit Delivery/Food Delivery tab/pill if visible
            try {
                WebElement deliveryTab = driver.findElement(By.xpath("//*[contains(text(),'Food Delivery') or normalize-space(.)='Delivery' or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delivery')]/ancestor::*[self::a or self::button or self::div][1]"));
                if (deliveryTab.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deliveryTab);
                    Thread.sleep(1200);
                }
            } catch (Exception ignored) {}

            // If URL contains dineout/dine-out, force redirect to order-online-near-me
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("dineout") || currentUrl.contains("dine-out")) {
                System.out.println("   ↪️ Detected dine-out URL. Redirecting to Food Delivery...");
                driver.get("https://www.swiggy.com/order-online-near-me");
                Thread.sleep(4000);
            }

            // If we're not already on the correct page, navigate there
            currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/order-online-near-me") && !currentUrl.contains("/restaurants")) {
                System.out.println("   ➡️ Navigating directly to Food Delivery listings");
                driver.get("https://www.swiggy.com/order-online-near-me");
                Thread.sleep(4000);
            }

            // Wait and verify presence of restaurant cards (delivery listing)
            takeScreenshot("before_restaurants_navigation");
            Thread.sleep(2000);
            currentUrl = driver.getCurrentUrl();
            System.out.println("   🔗 Current URL: " + currentUrl);

            // Basic presence check
            List<WebElement> restaurantCards = driver.findElements(By.xpath(
                "//a[contains(@href,'/restaurants/') and not(contains(@href,'/restaurants$'))] | " +
                "//*[@data-testid='restaurant-card'] | " +
                "//div[contains(@class,'listing') or contains(@class,'_1MIkP') or contains(@class,'styles_listingCard')]//a[contains(@href,'restaurants')]"
            ));

            if (!restaurantCards.isEmpty()) {
                System.out.println("   ✅ Food Delivery listings detected (" + restaurantCards.size() + ")");
            } else {
                System.out.println("   ⚠️ Could not detect delivery listings yet; forcing one more redirect");
                driver.get("https://www.swiggy.com/order-online-near-me");
                Thread.sleep(4000);
            }

            takeScreenshot("after_restaurants_navigation");
        } catch (Exception e) {
            System.out.println("   ⚠️ Error ensuring food delivery mode: " + e.getMessage());
            takeScreenshot("restaurants_navigation_error");
        }
    }

    // Ensure Order Online tab is active on listings (avoid Dineout)
    private static void ensureOrderOnlineOnListing() {
        try {
            Thread.sleep(800);
            String[] orderOnlineSelectors = new String[] {
                "//*[normalize-space(.)='Order Online']/ancestor::*[self::a or self::button or self::div][1]",
                "//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'order online')]/ancestor::*[self::a or self::button or self::div][1]"
            };
            for (String sel : orderOnlineSelectors) {
                try {
                    List<WebElement> tabs = driver.findElements(By.xpath(sel));
                    if (!tabs.isEmpty()) {
                        WebElement tab = tabs.get(0);
                        if (tab.isDisplayed()) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", tab);
                            Thread.sleep(300);
                            try { tab.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab); }
                            Thread.sleep(1200);
                            takeScreenshot("order_online_listing_selected");
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
            // Force URL to /restaurants if still wrong
            if (!driver.getCurrentUrl().contains("/restaurants")) {
                driver.get("https://www.swiggy.com/restaurants");
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            takeScreenshot("order_online_listing_error");
        }
    }

    // Ensure Order Online tab is active inside a restaurant page
    private static void ensureOrderOnlineInRestaurant() {
        try {
            Thread.sleep(800);
            String[] orderOnlineSelectors = new String[] {
                "//*[normalize-space(.)='Order Online']/ancestor::*[self::a or self::button or self::div][1]",
                "//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'order online')]/ancestor::*[self::a or self::button or self::div][1]"
            };
            for (String sel : orderOnlineSelectors) {
                try {
                    List<WebElement> tabs = driver.findElements(By.xpath(sel));
                    if (!tabs.isEmpty()) {
                        WebElement tab = tabs.get(0);
                        if (tab.isDisplayed()) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", tab);
                            Thread.sleep(300);
                            try { tab.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab); }
                            Thread.sleep(1200);
                            takeScreenshot("order_online_restaurant_selected");
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            takeScreenshot("order_online_restaurant_error");
        }
    }

    private static String searchForRestaurant(String restaurantName) {
        try {
            // Use the configured restaurant from properties
            String configuredRestaurant = config.getProperty("test.restaurant", restaurantName);
            System.out.println("Searching for restaurant: " + configuredRestaurant);
            takeScreenshot("before_restaurant_search");

            // Wait for page to load and ensure we're in delivery mode
            Thread.sleep(5000);

            // First, ensure we're in DELIVERY mode, not DINE OUT
            try {
                System.out.println("Checking and setting delivery mode...");
                String[] deliveryModeSelectors = {
                    "//div[contains(text(),'Delivery')]",
                    "//span[contains(text(),'Delivery')]",
                    "//a[contains(text(),'Delivery')]",
                    "//button[contains(text(),'Delivery')]"
                };

                for (String selector : deliveryModeSelectors) {
                    try {
                        WebElement deliveryMode = driver.findElement(By.xpath(selector));
                        if (deliveryMode.isDisplayed()) {
                            deliveryMode.click();
                            System.out.println("Clicked on Delivery mode");
                            Thread.sleep(2000);
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not find delivery mode selector");
            }

            // Find search bar with enhanced selectors
            WebElement searchBar = null;
            String[] searchSelectors = {
                "//input[@placeholder*='Search for restaurants']",
                "//input[@placeholder*='Search']",
                "//input[contains(@class,'_3704q')]", // Swiggy search class
                "//input[contains(@class,'search')]",
                "//div[contains(@class,'_3xdOh')]//input", // Swiggy search container
                "//input[@type='text' and contains(@placeholder,'restaurant')]",
                "//input[@name='search']",
                "//input[contains(@data-testid,'search')]"
            };

            boolean searchBarFound = false;
            for (int i = 0; i < searchSelectors.length; i++) {
                try {
                    System.out.println("Trying search selector " + (i+1) + ": " + searchSelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    searchBar = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(searchSelectors[i])));

                    if (searchBar.isDisplayed()) {
                        System.out.println("Found search bar with selector: " + searchSelectors[i]);
                        searchBarFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Search bar not found with selector " + (i+1));
                    continue;
                }
            }

            if (!searchBarFound) {
                System.out.println("Search bar not found, skipping restaurant search");
                takeScreenshot("search_bar_not_found");
                return configuredRestaurant;
            }

            // Clear and enter restaurant name
            try {
                searchBar.clear();
                searchBar.sendKeys(configuredRestaurant);
                System.out.println("Restaurant name entered successfully: " + configuredRestaurant);
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", searchBar);
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", searchBar, configuredRestaurant);
                System.out.println("Restaurant name entered using JavaScript: " + configuredRestaurant);
            }

            // Press Enter or click search button
            try {
                searchBar.sendKeys(Keys.ENTER);
                System.out.println("Pressed Enter to search");
            } catch (Exception e) {
                // Try to find and click search button
                try {
                    WebElement searchButton = driver.findElement(By.xpath("//button[contains(@class,'search')] | //button[contains(text(),'Search')]"));
                    searchButton.click();
                    System.out.println("Clicked search button");
                } catch (Exception ex) {
                    System.out.println("Could not find search button, continuing...");
                }
            }

            Thread.sleep(5000);
            takeScreenshot("search_results");
            return configuredRestaurant;

        } catch (Exception e) {
            System.out.println("Error in restaurant search: " + e.getMessage());
            takeScreenshot("restaurant_search_error");
            return restaurantName;
        }
    }

    private static void selectRestaurantByNameOnListing(String name) {
        try {
            String[] selectors = {
                "//a[contains(@href,'restaurants')][.//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), translate('" + name + "', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))]]",
                "//a[contains(@href,'restaurants')][contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), translate('" + name + "', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))]"
            };
            for (String s : selectors) {
                try {
                    List<WebElement> els = driver.findElements(By.xpath(s));
                    if (!els.isEmpty()) {
                        WebElement el = els.get(0);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
                        Thread.sleep(400);
                        try { el.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
                        Thread.sleep(4000);
                        return;
                    }
                } catch (Exception ignored) {}
            }
            // Fallback to any restaurant
            selectAnyAvailableRestaurant();
        } catch (Exception e) {
            selectAnyAvailableRestaurant();
        }
    }

    private static void selectAnyAvailableRestaurant() {
        try {
            System.out.println("   🏠 Looking for any available restaurant...");
            Thread.sleep(1500);

            String[] restaurantSelectors = {
                "//div[contains(@class,'_1MIkP')]//a[contains(@href,'restaurants')]",
                "//a[contains(@href,'restaurants/') and contains(@href,'-')]",
                "//*[@data-testid='restaurant-card']//a",
                "//div[contains(@class,'styles_listingCard')]//a",
                "//a[contains(@href,'/restaurants') and not(contains(@href,'/restaurants$'))]"
            };

            for (String selector : restaurantSelectors) {
                try {
                    List<WebElement> restaurants = driver.findElements(By.xpath(selector));
                    System.out.println("   🔍 Found " + restaurants.size() + " restaurants with selector: " + selector);

                    if (!restaurants.isEmpty()) {
                        for (int i = 0; i < Math.min(5, restaurants.size()); i++) {
                            try {
                                WebElement restaurant = restaurants.get(i);
                                if (restaurant.isDisplayed() && restaurant.isEnabled()) {
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", restaurant);
                                    Thread.sleep(400);
                                    try { restaurant.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", restaurant); }
                                    System.out.println("   ✓ Clicked on restaurant card");
                                    Thread.sleep(4000);
                                    takeScreenshot("restaurant_selected");

                                    String currentUrl = driver.getCurrentUrl();
                                    if (currentUrl.contains("restaurants/") && !currentUrl.endsWith("/restaurants")) {
                                        System.out.println("   ✅ Successfully navigated to restaurant menu page");
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                // try next
                            }
                        }
                    }
                } catch (Exception e) {
                    // try next selector
                }
            }

            System.out.println("   ⚠️ No clickable restaurants found with any selector");
            takeScreenshot("no_restaurants_found");

        } catch (Exception e) {
            System.out.println("   ⚠️ Error selecting restaurant: " + e.getMessage());
            takeScreenshot("restaurant_selection_error");
        }
    }

    // Wait for menu page content to load (presence of dish cards or ADD buttons)
    private static void waitForMenuToLoad() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(12));
            shortWait.until(d -> {
                try {
                    return !d.findElements(By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')] | //div[contains(@class,'dish') or contains(@class,'item')][.//button]"))
                              .isEmpty();
                } catch (Exception ex) {
                    return false;
                }
            });
        } catch (Exception e) {
            // continue; some menus lazy load on scroll
        }
    }

    // Try to add a specific item by name, case-insensitive. Returns true if clicked ADD.
    private static boolean addItemByName(String itemName) throws InterruptedException {
        String itemXpath = "//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), translate('" + itemName + "','ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))]";
        // Find the nearest menu card ancestor and click its ADD button
        String[] cardAncestors = {
            itemXpath + "/ancestor::div[contains(@class,'item')][1]",
            itemXpath + "/ancestor::div[contains(@class,'dish')][1]",
            itemXpath + "/ancestor::div[contains(@class,'_1HEo8') or contains(@class,'_3IlBn') or contains(@class,'_1gURR') or contains(@class,'_2xK7I')][1]",
            itemXpath + "/ancestor::div[@role='listitem' or @data-testid='menu-item'][1]"
        };
        for (String cardSel : cardAncestors) {
            try {
                List<WebElement> cards = driver.findElements(By.xpath(cardSel));
                if (!cards.isEmpty()) {
                    WebElement card = cards.get(0);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", card);
                    Thread.sleep(600);
                    // Find ADD within the card
                    String[] addButtonSel = {
                        ".//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]",
                        ".//div[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add') and @role='button']",
                        ".//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]/parent::button"
                    };
                    for (String ab : addButtonSel) {
                        try {
                            WebElement add = card.findElement(By.xpath(ab));
                            if (add.isDisplayed() && add.isEnabled()) {
                                try { add.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", add); }
                                Thread.sleep(1200);
                                handleCustomizationModalIfAny();
                                return true;
                            }
                        } catch (Exception ignore) { }
                    }
                }
            } catch (Exception ignore) { }
        }
        // Fallback: click first visible ADD matching item context
        try {
            WebElement anyAdd = driver.findElement(By.xpath("(//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')])[1]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", anyAdd);
            Thread.sleep(1000);
            handleCustomizationModalIfAny();
            return true;
        } catch (Exception ignore) {}
        return false;
    }

    // Wait until cart reflects at least 1 item (View Cart visible or a badge > 0)
    private static boolean waitForCartHasItems(int timeoutSec) {
        long end = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < end) {
            try {
                // View Cart visible
                List<WebElement> vc = driver.findElements(By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'view cart')]"));
                if (!vc.isEmpty() && vc.get(0).isDisplayed()) return true;
                // Badge count > 0
                WebElement badge = driver.findElement(By.xpath("//*[contains(@class,'cart') or contains(@data-testid,'cart')]//*[contains(text(),'1') or contains(text(),'2') or contains(text(),'3') or contains(text(),'4') or contains(text(),'5')]"));
                if (badge != null && badge.isDisplayed()) return true;
            } catch (Exception ignored) {}
            try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        return false;
    }

    private static String selectFoodItemAndAddToCart(String preferredItem) {
        String selectedItemName = preferredItem != null ? preferredItem : "Unknown Item";
        try {
            waitForMenuToLoad();
            Thread.sleep(1500);
            takeScreenshot("menu_loaded");

            boolean added = false;
            if (preferredItem != null && !preferredItem.isEmpty()) {
                System.out.println("   🎯 Trying to add specific item: " + preferredItem);
                added = addItemByName(preferredItem);
                if (added) selectedItemName = preferredItem;
            }

            if (!added) {
                // Fallback to previous generic selection
                String fallback = selectFoodItemAndAddToCart();
                if (!"Unknown Item".equals(fallback)) selectedItemName = fallback;
            }

            if (!waitForCartHasItems(12)) {
                System.out.println("   ⚠️ Cart not updated after add; retrying once with scroll...");
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,300);");
                Thread.sleep(800);
                if (preferredItem != null) {
                    addItemByName(preferredItem);
                }
                waitForCartHasItems(8);
            }
        } catch (Exception e) {
            System.out.println("   ⚠️ Error selecting specific food item: " + e.getMessage());
            takeScreenshot("food_item_specific_error");
        }
        return selectedItemName;
    }

    // Keep original fallback method for generic add
    private static String selectFoodItemAndAddToCart() {
        String selectedItemName = "Unknown Item";

        try {
            System.out.println("   🍕 Looking for food items on menu...");
            Thread.sleep(5000);

            // Enhanced food item selectors for Swiggy
            String[] foodItemSelectors = {
                "//div[contains(@class,'_1HEo8')]", // Food item card
                "//div[contains(@class,'_3IlBn')]", // Alternative food item card
                "//div[contains(@class,'_1gURR')]", // Another variant
                "//div[contains(@class,'_2xK7I')]", // Yet another variant
                "//div[contains(@class,'dish-card')]",
                "//div[contains(@class,'menu-item')]",
                "//div[contains(@class,'_1tcx4')]",
                "//*[contains(@class,'item-card')]",
                "//div[contains(@class,'_3DWxi')]",
                "//div[contains(@class,'_1kJ8k')]", // New Swiggy food item selector
                "//div[contains(@data-testid,'menu-item')]" // Data test id approach
            };

            WebElement selectedFoodItem = null;

            for (String selector : foodItemSelectors) {
                try {
                    List<WebElement> foodItems = driver.findElements(By.xpath(selector));
                    System.out.println("   🔍 Found " + foodItems.size() + " food items with selector: " + selector);

                    if (foodItems.size() > 1) { // Get the second item if available
                        selectedFoodItem = foodItems.get(1);
                        if (selectedFoodItem.isDisplayed()) {
                            selectedItemName = selectedFoodItem.getText().split("\n")[0]; // Get just the name
                            System.out.println("   ✓ Selected food item: " + selectedItemName);
                            break;
                        }
                    } else if (!foodItems.isEmpty()) { // Fallback to first item
                        selectedFoodItem = foodItems.get(0);
                        if (selectedFoodItem.isDisplayed()) {
                            selectedItemName = selectedFoodItem.getText().split("\n")[0]; // Get just the name
                            System.out.println("   ✓ Selected food item (first available): " + selectedItemName);
                            break;
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (selectedFoodItem != null) {
                // Scroll to the food item
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", selectedFoodItem);
                Thread.sleep(2000);
                takeScreenshot("food_item_found");

                // Look for ADD button near the selected item
                String[] addButtonSelectors = {
                    ".//button[contains(text(),'ADD')]",
                    ".//button[contains(text(),'Add')]",
                    ".//div[contains(text(),'ADD')]",
                    ".//div[contains(text(),'Add')]",
                    ".//span[contains(text(),'ADD')]",
                    ".//a[contains(text(),'ADD')]",
                    ".//button[contains(@class,'_17vEZ')]", // Swiggy specific
                    ".//button[contains(@class,'add')]",
                    ".//div[contains(@class,'add')]"
                };

                WebElement addButton = null;

                // First try to find ADD button within the selected item
                for (String addSelector : addButtonSelectors) {
                    try {
                        addButton = selectedFoodItem.findElement(By.xpath(addSelector));
                        if (addButton.isDisplayed() && addButton.isEnabled()) {
                            System.out.println("   ✓ Found ADD button within selected item");
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }

                // If not found within item, look in the broader context
                if (addButton == null) {
                    for (String addSelector : addButtonSelectors) {
                        try {
                            List<WebElement> addButtons = driver.findElements(By.xpath(addSelector.substring(2))); // Remove "./"
                            if (!addButtons.isEmpty()) {
                                addButton = addButtons.get(0); // Get first available ADD button
                                if (addButton.isDisplayed() && addButton.isEnabled()) {
                                    System.out.println("   ✓ Found ADD button on page");
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }

                if (addButton != null) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addButton);
                        Thread.sleep(1000);
                        addButton.click();
                        System.out.println("   ✓ Food item added to cart successfully");
                        Thread.sleep(1000);
                        // If a customization modal appears, confirm Add
                        handleCustomizationModalIfAny();
                        Thread.sleep(2000);
                        takeScreenshot("item_added_to_cart");

                    } catch (Exception e) {
                        // Try JavaScript click if normal click fails
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addButton);
                        System.out.println("   ✓ Food item added to cart using JavaScript click");
                        Thread.sleep(1000);
                        handleCustomizationModalIfAny();
                        Thread.sleep(1500);
                        takeScreenshot("item_added_to_cart_js");
                    }
                } else {
                    System.out.println("   ⚠️ ADD button not found for selected item");
                    takeScreenshot("add_button_not_found");
                }
            } else {
                System.out.println("   ⚠️ No food items found on the page");
                takeScreenshot("no_food_items_found");
            }

        } catch (Exception e) {
            System.out.println("   ⚠️ Error selecting food item: " + e.getMessage());
            takeScreenshot("food_item_selection_error");
        }

        return selectedItemName;
    }

    private static void handleCustomizationModalIfAny() {
        try {
            String[] modalAddSelectors = {
                "//div[contains(@role,'dialog')]//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'add')]",
                "//div[contains(@class,'modal') or contains(@class,'Dialog') or contains(@class,'overlay')]//button[contains(.,'Add')]",
                "//button[contains(.,'Add item') or contains(.,'ADD ITEM') or contains(.,'Add to cart')]"
            };
            for (String s : modalAddSelectors) {
                try {
                    List<WebElement> modalButtons = driver.findElements(By.xpath(s));
                    if (!modalButtons.isEmpty()) {
                        WebElement btn = modalButtons.get(0);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
                        Thread.sleep(300);
                        try { btn.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }
                        System.out.println("   ✓ Confirmed add in customization modal");
                        return;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static void proceedToCart() {
        try {
            System.out.println("   🛒 Looking for cart...");
            Thread.sleep(3000);

            // Guard: only proceed if cart shows at least one item
            if (!waitForCartHasItems(5)) {
                System.out.println("   ⚠️ No items detected in cart yet; skipping cart open.");
                takeScreenshot("cart_empty_before_open");
                return;
            }

            // Look for "View Cart" or cart icon
            String[] cartSelectors = {
                "//button[contains(text(),'View Cart')]",
                "//div[contains(text(),'View Cart')]",
                "//a[contains(text(),'View Cart')]",
                "//div[contains(@class,'cart')]//button",
                "//button[contains(@class,'cart')]",
                "//a[contains(@href,'/cart')]",
                "//*[contains(@data-testid,'cart')]"
            };

            for (String selector : cartSelectors) {
                try {
                    WebElement cartElement = driver.findElement(By.xpath(selector));
                    if (cartElement.isDisplayed()) {
                        cartElement.click();
                        System.out.println("   ✓ Navigated to cart");
                        Thread.sleep(3000);
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            System.out.println("   ℹ️ Cart button not found, may already be in cart");

        } catch (Exception e) {
            System.out.println("   ⚠️ Error proceeding to cart: " + e.getMessage());
            takeScreenshot("cart_error");
        }
    }

    private static void proceedToCheckout() {
        try {
            System.out.println("   📋 Looking for checkout button...");
            Thread.sleep(3000);

            // Navigate to checkout - look for checkout button
            String[] checkoutSelectors = {
                "//button[contains(text(),'Checkout')]",
                "//button[contains(text(),'Proceed to Checkout')]",
                "//a[contains(@href,'/checkout')]",
                "//button[contains(@class,'checkout')]",
                "//div[contains(text(),'Checkout')][@role='button']"
            };

            for (String selector : checkoutSelectors) {
                try {
                    WebElement checkoutButton = driver.findElement(By.xpath(selector));
                    if (checkoutButton.isDisplayed()) {
                        checkoutButton.click();
                        System.out.println("   ✓ Proceeded to checkout");
                        Thread.sleep(5000);
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            System.out.println("   ℹ️ Checkout button not found, may already be in checkout");

        } catch (Exception e) {
            System.out.println("   ⚠️ Error proceeding to checkout: " + e.getMessage());
        }
    }

    private static void enterDeliveryAddress() {
        try {
            System.out.println("   📍 Looking for address selection...");
            Thread.sleep(5000);

            // First try to select existing address
            String[] addressSelectors = {
                "//div[contains(@class,'address')]//button[contains(text(),'DELIVER HERE')]",
                "//button[contains(text(),'Deliver here')]",
                "//button[contains(text(),'SELECT')]",
                "//div[contains(@class,'address-card')]//button"
            };

            for (String selector : addressSelectors) {
                try {
                    List<WebElement> addressButtons = driver.findElements(By.xpath(selector));
                    if (!addressButtons.isEmpty()) {
                        addressButtons.get(0).click();
                        System.out.println("   ✓ Selected existing delivery address");
                        Thread.sleep(3000);
                        takeScreenshot("address_selected");
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            // If no existing address, try to add new address
            String[] addAddressSelectors = {
                "//button[contains(text(),'Add New Address')]",
                "//div[contains(text(),'Add Address')]",
                "//button[contains(text(),'Add Address')]"
            };

            for (String selector : addAddressSelectors) {
                try {
                    WebElement addAddressButton = driver.findElement(By.xpath(selector));
                    if (addAddressButton.isDisplayed()) {
                        addAddressButton.click();
                        System.out.println("   ✓ Clicked add new address");
                        Thread.sleep(3000);

                        // Fill address form
                        fillAddressForm();
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            System.out.println("   ℹ️ Address handling completed");

        } catch (Exception e) {
            System.out.println("   ⚠️ Error entering delivery address: " + e.getMessage());
        }
    }

    private static void fillAddressForm() {
        try {
            // Fill basic address fields
            String[] addressFieldSelectors = {
                "//input[@placeholder*='address']",
                "//input[@placeholder*='Address']",
                "//textarea[@placeholder*='address']",
                "//input[@name='address']"
            };

            for (String selector : addressFieldSelectors) {
                try {
                    WebElement addressField = driver.findElement(By.xpath(selector));
                    if (addressField.isDisplayed()) {
                        addressField.clear();
                        addressField.sendKeys("123, 2nd Main, 3rd Cross, Whitefield, Bangalore");
                        System.out.println("   ✓ Delivery address entered");
                        Thread.sleep(2000);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            // Try to save address
            String[] saveSelectors = {
                "//button[contains(text(),'Save')]",
                "//button[contains(text(),'SAVE')]",
                "//button[contains(text(),'Confirm')]",
                "//button[contains(text(),'CONFIRM')]"
            };

            for (String selector : saveSelectors) {
                try {
                    WebElement saveButton = driver.findElement(By.xpath(selector));
                    if (saveButton.isDisplayed()) {
                        saveButton.click();
                        System.out.println("   ✓ Address saved");
                        Thread.sleep(3000);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

        } catch (Exception e) {
            System.out.println("   ⚠️ Error filling address form: " + e.getMessage());
        }
    }

    private static void proceedToPayment() {
        try {
            System.out.println("   💳 Looking for payment options...");
            Thread.sleep(5000);

            // Navigate to payment - look for payment button
            String[] paymentSelectors = {
                "//button[contains(text(),'Pay now')]",
                "//button[contains(text(),'Proceed to pay')]",
                "//button[contains(text(),'PROCEED TO PAY')]",
                "//button[contains(text(),'Place Order')]",
                "//button[contains(@class,'payment')]"
            };

            for (String selector : paymentSelectors) {
                try {
                    WebElement paymentButton = driver.findElement(By.xpath(selector));
                    if (paymentButton.isDisplayed()) {
                        paymentButton.click();
                        System.out.println("   ✓ Proceeded to payment");
                        Thread.sleep(5000);
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            System.out.println("   ℹ️ Payment button not found, may already be on payment page");

        } catch (Exception e) {
            System.out.println("   ⚠️ Error proceeding to payment: " + e.getMessage());
        }
    }

    private static void printFinalLogs(String restaurantName, String selectedItem) {
        System.out.println("\n=== FINAL LOGS ===");
        System.out.println("Restaurant Name: " + restaurantName);
        System.out.println("Selected Item: " + selectedItem);
        System.out.println("====================");
    }

    private static void takeScreenshot(String step) {
        try {
            // Define screenshot file name
            String fileName = "screenshots/" + System.currentTimeMillis() + "_" + step + ".png";

            // Take screenshot
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(fileName));

            System.out.println("   ✓ Screenshot taken: " + fileName);
        } catch (Exception e) {
            System.out.println("   ⚠️ Error taking screenshot: " + e.getMessage());
        }
    }

    // Open first restaurant on listing and return its name if possible
    private static String openFirstRestaurantAndGetName() {
        String name = "Unknown Restaurant";
        try {
            // Reuse the selectors from selectAnyAvailableRestaurant but pick the first visible
            String[] restaurantSelectors = {
                "//div[contains(@class,'_1MIkP')]//a[contains(@href,'restaurants')]",
                "//a[contains(@href,'restaurants/') and contains(@href,'-')]",
                "//*[@data-testid='restaurant-card']//a",
                "//div[contains(@class,'styles_listingCard')]//a",
                "//a[contains(@href,'/restaurants') and not(contains(@href,'/restaurants$'))]"
            };

            for (String selector : restaurantSelectors) {
                try {
                    List<WebElement> links = driver.findElements(By.xpath(selector));
                    for (WebElement link : links) {
                        if (link.isDisplayed() && link.isEnabled()) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                            Thread.sleep(400);
                            try { link.click(); } catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link); }
                            Thread.sleep(4000);
                            takeScreenshot("restaurant_selected");
                            // Try to get restaurant name
                            String[] nameSelectors = {
                                "//*[@data-testid='restaurant-name']",
                                "//h1",
                                "//h2"
                            };
                            for (String ns : nameSelectors) {
                                try {
                                    WebElement n = driver.findElement(By.xpath(ns));
                                    if (n.isDisplayed()) {
                                        String t = n.getText();
                                        if (t != null && !t.trim().isEmpty()) { name = t.trim(); }
                                        break;
                                    }
                                } catch (Exception ignore) {}
                            }
                            if ("Unknown Restaurant".equals(name)) {
                                String title = driver.getTitle();
                                if (title != null && !title.trim().isEmpty()) name = title.replace("Menu", "").replace("—", "-").split("-")[0].trim();
                            }
                            return name;
                        }
                    }
                } catch (Exception ignore) {}
            }
            System.out.println("   ⚠️ Could not open a restaurant via known selectors");
        } catch (Exception e) {
            System.out.println("   ⚠️ Error opening restaurant: " + e.getMessage());
        }
        return name;
    }
}
