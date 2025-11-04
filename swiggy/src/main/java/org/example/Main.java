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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class
Main {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static Scanner scanner;
    private static Properties config;

    public static void main(String[] args) {
        try {
            loadConfiguration();
            setupDriver();
            scanner = new Scanner(System.in);

            // Step 1: Navigate to Swiggy and Login
            navigateToSwiggy();
            loginToSwiggy();

            // Step 2: Validate page title and URL
            validatePageTitleAndURL();

            // Step 3: Enter delivery location
            enterDeliveryLocation("Bengaluru");

            // Step 4: Search for restaurant
            String restaurantName = searchForRestaurant("Domino's Pizza");

            // Step 5: Select food item and add to cart
            String selectedItem = selectFoodItem();
            takeScreenshot("after_adding_item_to_cart");

            // Step 6: Increase quantity
            increaseQuantity();
            takeScreenshot("after_increasing_quantity");

            // Step 7: Enter delivery address
            enterDeliveryAddress();
            takeScreenshot("after_entering_delivery_address");

            // Step 8: Proceed to payment
            proceedToPayment();
            takeScreenshot("after_proceeding_to_payment");

            // Final logs
            printFinalLogs(restaurantName, selectedItem);

        } catch (Exception e) {
            System.err.println("Error during automation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Keep browser open for manual inspection
            System.out.println("\nAutomation completed. Press Enter to close the browser...");
            scanner.nextLine();
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
            System.out.println("Error loading configuration: " + e.getMessage());
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Reduced timeout
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30)); // Add page load timeout
    }

    private static void navigateToSwiggy() {
        System.out.println("Navigating to Swiggy website...");
        driver.get("https://www.swiggy.com/");

        // Handle location popup if it appears
        try {
            Thread.sleep(3000);
            WebElement locationPopup = driver.findElement(By.cssSelector("[class*='location-deny']"));
            if (locationPopup.isDisplayed()) {
                locationPopup.click();
            }
        } catch (Exception e) {
            // Location popup might not appear
        }
    }

    private static void loginToSwiggy() {
        try {
            System.out.println("Attempting to login...");

            // Wait for page to load completely
            Thread.sleep(5000);
            takeScreenshot("page_loaded");

            // Enhanced login button detection with current Swiggy selectors
            WebElement loginButton = null;
            String[] loginSelectors = {
                "//a[contains(text(),'Sign in')]",
                "//div[contains(text(),'Sign in')]",
                "//span[contains(text(),'Sign in')]",
                "//button[contains(text(),'Sign in')]",
                "//a[contains(text(),'Login')]",
                "//button[contains(text(),'Login')]",
                "//div[contains(text(),'Login')]",
                "//a[contains(@class,'_1T-E4')]", // Updated Swiggy class
                "//div[contains(@class,'_1T-E4')]",
                "//*[@data-testid*='login']",
                "//*[@data-testid*='signin']",
                "//a[contains(@href,'signin')]",
                "//div[contains(@class,'sign-in')]",
                "//a[contains(@class,'_2HD6t')]", // New Swiggy login class
                "//div[contains(@class,'_2HD6t')]",
                "//*[contains(text(),'Sign in') or contains(text(),'Login') or contains(text(),'LOGIN')]"
            };

            boolean loginButtonFound = false;
            for (int attempt = 0; attempt < loginSelectors.length; attempt++) {
                try {
                    System.out.println("Trying login selector " + (attempt + 1) + ": " + loginSelectors[attempt]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    loginButton = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(loginSelectors[attempt])));

                    if (loginButton != null && loginButton.isDisplayed() && loginButton.isEnabled()) {
                        System.out.println("Found clickable login button with selector: " + loginSelectors[attempt]);
                        loginButtonFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Login button not found with selector " + (attempt + 1) + ": " + e.getMessage());
                    continue;
                }
            }

            if (!loginButtonFound) {
                System.out.println("Could not find login button with any selector. Taking screenshot for debugging...");
                takeScreenshot("login_button_not_found");
                throw new Exception("Login button not found on the page after trying all selectors");
            }

            // Click login button
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
            Thread.sleep(1000);

            try {
                loginButton.click();
                System.out.println("Clicked login button successfully using normal click");
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
                System.out.println("Clicked login button successfully using JavaScript click");
            }

            Thread.sleep(5000);
            takeScreenshot("after_login_button_click");

            // Enhanced phone input detection
            WebElement phoneInput = null;
            String[] phoneSelectors = {
                "//input[@type='tel']",
                "//input[@placeholder*='phone']",
                "//input[@placeholder*='mobile']",
                "//input[@placeholder*='number']",
                "//input[@placeholder*='Phone']",
                "//input[@placeholder*='Mobile']",
                "//input[contains(@class,'_2zrpA')]", // Updated Swiggy phone input class
                "//input[contains(@class,'phone')]",
                "//input[contains(@name,'phone')]",
                "//input[contains(@id,'phone')]",
                "//input[@inputmode='tel']",
                "//input[@inputmode='numeric']"
            };

            boolean phoneInputFound = false;
            for (int attempt = 0; attempt < phoneSelectors.length; attempt++) {
                try {
                    System.out.println("Trying phone input selector " + (attempt + 1) + ": " + phoneSelectors[attempt]);
                    phoneInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(phoneSelectors[attempt])));

                    if (phoneInput != null && phoneInput.isDisplayed()) {
                        System.out.println("Found phone input with selector: " + phoneSelectors[attempt]);
                        phoneInputFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Phone input not found with selector " + (attempt + 1) + ": " + e.getMessage());
                    continue;
                }
            }

            if (!phoneInputFound) {
                takeScreenshot("phone_input_not_found");
                throw new Exception("Phone input field not found");
            }

            // Enter phone number
            String phoneNumber = config.getProperty("user.phone.number", "");
            System.out.println("Using phone number from configuration: " + phoneNumber);

            // Clear and enter phone number
            phoneInput.clear();
            phoneInput.sendKeys(phoneNumber);
            System.out.println("Phone number entered successfully");

            Thread.sleep(3000);
            takeScreenshot("phone_number_entered");

            // Enhanced continue button detection with modern Swiggy selectors
            WebElement continueButton = null;
            String[] continueSelectors = {
                "//button[contains(text(),'Continue')]",
                "//button[contains(text(),'Send OTP')]",
                "//button[contains(text(),'CONTINUE')]",
                "//button[contains(text(),'SEND OTP')]",
                "//button[contains(text(),'GET OTP')]",
                "//button[contains(text(),'Send')]",
                "//div[contains(text(),'Continue')]",
                "//div[contains(text(),'Send OTP')]",
                "//button[@type='submit']",
                "//button[contains(@class,'_2AkbM')]", // Updated Swiggy button class
                "//button[contains(@class,'_1tcx4')]", // Alternative Swiggy button class
                "//button[contains(@class,'continue')]",
                "//button[contains(@class,'submit')]",
                "//button[contains(@class,'primary')]",
                "//button[contains(@class,'btn-primary')]",
                "//span[contains(text(),'Continue')]",
                "//span[contains(text(),'Send OTP')]",
                "//a[contains(@class,'_2AkbM')]",
                "//div[contains(@class,'_2AkbM')]",
                "//form//button[not(@disabled)]",
                "//button[contains(@data-testid,'continue')]",
                "//button[contains(@data-testid,'submit')]",
                "//button[contains(@data-testid,'send-otp')]"
            };

            boolean continueButtonFound = false;
            for (int attempt = 0; attempt < continueSelectors.length; attempt++) {
                try {
                    System.out.println("Trying continue button selector " + (attempt + 1) + ": " + continueSelectors[attempt]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                    continueButton = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(continueSelectors[attempt])));

                    if (continueButton != null && continueButton.isDisplayed() && continueButton.isEnabled()) {
                        System.out.println("Found clickable continue button with selector: " + continueSelectors[attempt]);
                        continueButtonFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Continue button not found with selector " + (attempt + 1) + ": " + e.getMessage());
                    continue;
                }
            }

            // Alternative: Look for any enabled button in the login form
            if (!continueButtonFound) {
                System.out.println("Trying to find any button in the login form...");
                try {
                    WebElement parentForm = phoneInput.findElement(By.xpath("./ancestor::form | ./ancestor::div[contains(@class,'_3w_9r')] | ./ancestor::div[contains(@class,'login')] | ./ancestor::div[contains(@class,'signin')]"));
                    List<WebElement> buttonsInForm = parentForm.findElements(By.xpath(".//button[not(@disabled)] | .//div[@role='button'] | .//a[contains(@class,'btn')]"));

                    System.out.println("Found " + buttonsInForm.size() + " potential buttons in the form");

                    for (int i = 0; i < buttonsInForm.size(); i++) {
                        WebElement btn = buttonsInForm.get(i);
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            String buttonText = btn.getText().toLowerCase();
                            String buttonClass = btn.getAttribute("class");
                            String buttonRole = btn.getAttribute("role");

                            System.out.println("Button " + (i+1) + ": text='" + buttonText + "', class='" + buttonClass + "', role='" + buttonRole + "'");

                            // Use this button if it looks like a continue/submit button or has no harmful text
                            if (buttonText.contains("continue") || buttonText.contains("send") || buttonText.contains("otp") ||
                                buttonText.contains("submit") || "button".equals(buttonRole) ||
                                (buttonText.trim().isEmpty() && btn.isEnabled())) {
                                continueButton = btn;
                                continueButtonFound = true;
                                System.out.println("Using button " + (i+1) + " as continue button");
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error finding buttons in form: " + e.getMessage());
                }
            }

            // If still no button found, try pressing Enter on phone input
            if (!continueButtonFound) {
                System.out.println("No continue button found. Trying to press Enter on phone input...");
                try {
                    phoneInput.sendKeys(Keys.ENTER);
                    System.out.println("Pressed Enter on phone input field");
                    Thread.sleep(3000);
                    takeScreenshot("after_enter_key_press");

                    // Check if OTP screen appeared
                    try {
                        WebElement otpCheck = driver.findElement(By.xpath("//input[@maxlength='6'] | //input[@placeholder*='OTP'] | //input[@placeholder*='otp'] | //input[contains(@class,'otp')]"));
                        if (otpCheck.isDisplayed()) {
                            System.out.println("OTP screen appeared after pressing Enter!");
                            handleOTPVerification(phoneNumber);
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("OTP screen did not appear after pressing Enter");
                    }
                } catch (Exception e) {
                    System.out.println("Could not press Enter on phone input: " + e.getMessage());
                }

                takeScreenshot("continue_button_not_found");
                throw new Exception("Continue button not found");
            }

            // Click the continue button
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", continueButton);
            Thread.sleep(1000);

            try {
                continueButton.click();
                System.out.println("Clicked continue button successfully");
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
                System.out.println("Clicked continue button using JavaScript");
            }

            Thread.sleep(3000);
            takeScreenshot("after_continue_click");

            // Handle OTP verification
            handleOTPVerification(phoneNumber);

        } catch (Exception e) {
            System.err.println("ERROR in login process: " + e.getMessage());
            takeScreenshot("login_error");
            e.printStackTrace();

            System.out.println("\nLogin process failed. Do you want to continue with the automation anyway?");
            System.out.println("Press Enter to continue or type 'exit' to stop:");
            String userChoice = scanner.nextLine();

            if (userChoice.toLowerCase().contains("exit")) {
                System.out.println("Automation stopped by user.");
                System.exit(1);
            } else {
                System.out.println("Continuing with automation...");
            }
        }
    }

    private static void handleOTPVerification(String phoneNumber) {
        System.out.println("\n=== OTP VERIFICATION REQUIRED ===");
        System.out.println("An OTP has been sent to your phone number: " + phoneNumber);
        System.out.println("Please check your phone and enter the OTP in the browser.");
        System.out.println("After entering the OTP and completing login, press Enter here to continue...");
        takeScreenshot("otp_screen");

        // Wait for user to complete OTP
        scanner.nextLine();

        // Give some time for login to complete
        try {
            Thread.sleep(5000);
            String pageSource = driver.getPageSource().toLowerCase();

            if (pageSource.contains("sign in") || pageSource.contains("login")) {
                System.out.println("Login may not have completed successfully. Please verify manually.");
                takeScreenshot("login_verification");
            } else {
                System.out.println("Login appears to be successful!");
                takeScreenshot("login_successful");
            }
        } catch (Exception e) {
            System.out.println("Error during post-login verification: " + e.getMessage());
        }
    }

    private static void validatePageTitleAndURL() {
        String pageTitle = driver.getTitle();
        String currentURL = driver.getCurrentUrl();

        System.out.println("\n=== PAGE VALIDATION ===");
        System.out.println("Page Title: " + pageTitle);
        System.out.println("Current URL: " + currentURL);
        System.out.println("========================\n");
    }

    private static void enterDeliveryLocation(String cityName) {
        try {
            System.out.println("Entering delivery location: " + cityName);
            takeScreenshot("before_location_entry");

            // Wait for page to stabilize after login
            Thread.sleep(5000);

            // Use the configured location from properties
            String configuredLocation = config.getProperty("test.location", cityName);
            System.out.println("Using configured location: " + configuredLocation);

            // Enhanced location input detection with more current Swiggy selectors
            WebElement locationInput = null;
            String[] locationSelectors = {
                "//input[@placeholder*='Enter your delivery location']",
                "//input[@placeholder*='location']",
                "//input[@placeholder*='area']",
                "//input[@placeholder*='address']",
                "//input[@placeholder*='Enter area']",
                "//input[@placeholder*='Search for area']",
                "//input[contains(@class,'_2McTE')]", // Swiggy specific class
                "//input[contains(@class,'location')]",
                "//input[contains(@class,'search')]",
                "//input[contains(@id,'location')]",
                "//input[@type='text' and contains(@placeholder,'Enter')]",
                "//input[contains(@data-testid,'location')]"
            };

            boolean locationInputFound = false;
            for (int i = 0; i < locationSelectors.length; i++) {
                try {
                    System.out.println("Trying location selector " + (i+1) + ": " + locationSelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    locationInput = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locationSelectors[i])));

                    if (locationInput.isDisplayed()) {
                        System.out.println("Found location input with selector: " + locationSelectors[i]);
                        locationInputFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Location input not found with selector " + (i+1));
                    continue;
                }
            }

            if (!locationInputFound) {
                System.out.println("Location input not found. Checking if location is already set...");
                takeScreenshot("location_input_not_found");

                // Check if we're already on main page with location set
                try {
                    WebElement currentLocation = driver.findElement(By.xpath("//*[contains(text(),'Whitefield') or contains(text(),'Bangalore') or contains(text(),'Bengaluru')]"));
                    if (currentLocation.isDisplayed()) {
                        System.out.println("Location appears to already be set to Bangalore/Whitefield. Continuing...");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Could not verify current location");
                }

                // Try to find location selector button or link
                try {
                    WebElement locationSelector = driver.findElement(By.xpath("//div[contains(@class,'address')] | //span[contains(text(),'Other')] | //div[contains(@class,'location')]"));
                    if (locationSelector.isDisplayed()) {
                        locationSelector.click();
                        Thread.sleep(2000);
                        // Try to find input again after clicking location selector
                        for (String selector : locationSelectors) {
                            try {
                                locationInput = driver.findElement(By.xpath(selector));
                                if (locationInput.isDisplayed()) {
                                    locationInputFound = true;
                                    break;
                                }
                            } catch (Exception ex) {
                                continue;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not find location selector");
                }

                if (!locationInputFound) {
                    System.out.println("Skipping location entry as input field not found");
                    return;
                }
            }

            // Clear and enter location with multiple methods
            try {
                locationInput.clear();
                locationInput.sendKeys(configuredLocation);
                System.out.println("Location entered successfully using sendKeys");
            } catch (Exception e) {
                // Try JavaScript method
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", locationInput);
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", locationInput, configuredLocation);
                System.out.println("Location entered using JavaScript");
            }

            Thread.sleep(3000);
            takeScreenshot("location_entered");

            // Enhanced suggestion selection with Whitefield specific selectors
            WebElement firstSuggestion = null;
            String[] suggestionSelectors = {
                "//div[contains(@class,'_1tcx6')]", // Swiggy suggestion class
                "//div[contains(@class,'suggestion')]",
                "//li[contains(@class,'suggestion')]",
                "//div[contains(@class,'location-item')]",
                "//div[contains(@class,'search-result')]",
                "//*[contains(text(),'Whitefield')]",
                "//*[contains(text(),'Bangalore')]",
                "//ul//li[1]",
                "//div[contains(@class,'dropdown')]//div[1]",
                "//div[contains(@class,'_1fmVk')]" // Another Swiggy class
            };

            boolean suggestionFound = false;
            for (int i = 0; i < suggestionSelectors.length; i++) {
                try {
                    System.out.println("Trying suggestion selector " + (i+1) + ": " + suggestionSelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                    firstSuggestion = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(suggestionSelectors[i])));

                    if (firstSuggestion.isDisplayed()) {
                        System.out.println("Found suggestion with selector: " + suggestionSelectors[i]);
                        suggestionFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Suggestion not found with selector " + (i+1));
                    continue;
                }
            }

            if (suggestionFound) {
                // Try multiple click methods for suggestion
                try {
                    firstSuggestion.click();
                    System.out.println("Clicked suggestion successfully");
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstSuggestion);
                    System.out.println("Clicked suggestion using JavaScript");
                }
                Thread.sleep(3000);
                takeScreenshot("location_selected");
            } else {
                // Try pressing Enter on location input
                locationInput.sendKeys(Keys.ENTER);
                System.out.println("Pressed Enter on location input");
                Thread.sleep(3000);
            }

            System.out.println("Location entry completed successfully");

        } catch (Exception e) {
            System.out.println("Error in location entry: " + e.getMessage());
            takeScreenshot("location_entry_error");
            // Continue anyway as location might already be set
            System.out.println("Continuing with automation...");
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
                    "//button[contains(text(),'Delivery')]",
                    "//*[@data-testid='delivery']",
                    "//div[contains(@class,'delivery')]",
                    "//*[contains(@class,'_1fmVk') and contains(text(),'Delivery')]"
                };

                boolean deliveryModeSet = false;
                for (String selector : deliveryModeSelectors) {
                    try {
                        WebElement deliveryMode = driver.findElement(By.xpath(selector));
                        if (deliveryMode.isDisplayed()) {
                            // Check if it's already selected/active
                            String className = deliveryMode.getAttribute("class");
                            if (!className.contains("active") && !className.contains("selected")) {
                                deliveryMode.click();
                                System.out.println("Switched to Delivery mode");
                                Thread.sleep(2000);
                            } else {
                                System.out.println("Already in Delivery mode");
                            }
                            deliveryModeSet = true;
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }

                if (!deliveryModeSet) {
                    System.out.println("Could not explicitly set delivery mode, continuing...");
                }

                takeScreenshot("delivery_mode_set");
            } catch (Exception e) {
                System.out.println("Error setting delivery mode: " + e.getMessage());
            }

            // Enhanced search bar detection with current Swiggy classes
            WebElement searchBar = null;
            String[] searchSelectors = {
                "//input[@placeholder*='Search for restaurant and food']",
                "//input[@placeholder*='Search']",
                "//input[@placeholder*='restaurant']",
                "//input[@placeholder*='food']",
                "//input[@placeholder*='Search for restaurant']",
                "//input[@placeholder*='Search for dishes']",
                "//input[contains(@class,'_2McTE')]", // Swiggy search class
                "//input[contains(@class,'search')]",
                "//input[contains(@id,'search')]",
                "//input[@type='text' and contains(@class,'input')]",
                "//input[contains(@class,'_1aIHI')]", // Another Swiggy class
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
                // Try to find search icon or button to click first
                try {
                    WebElement searchIcon = driver.findElement(By.xpath("//div[contains(@class,'search')] | //button[contains(@class,'search')] | //*[contains(@data-testid,'search')]"));
                    if (searchIcon.isDisplayed()) {
                        searchIcon.click();
                        Thread.sleep(2000);
                        // Try to find search bar again
                        for (String selector : searchSelectors) {
                            try {
                                searchBar = driver.findElement(By.xpath(selector));
                                if (searchBar.isDisplayed()) {
                                    searchBarFound = true;
                                    break;
                                }
                            } catch (Exception ex) {
                                continue;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not find search icon");
                }
            }

            if (!searchBarFound) {
                System.out.println("Search bar not found. Taking screenshot...");
                takeScreenshot("search_bar_not_found");
                return configuredRestaurant; // Return default instead of throwing exception
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
                    WebElement searchButton = driver.findElement(By.xpath("//button[contains(@class,'search')] | //button[contains(text(),'Search')] | //*[@class='_1GTCc'] | //*[contains(@class,'_3yZyp')]"));
                    searchButton.click();
                    System.out.println("Clicked search button");
                } catch (Exception ex) {
                    System.out.println("Could not find search button, continuing...");
                }
            }

            Thread.sleep(5000);
            takeScreenshot("search_results");

            // Enhanced restaurant selection - specifically look for target restaurant
            WebElement targetRestaurant = null;
            String[] restaurantSelectors = {
                "//div[contains(@class,'_1MIkP')]", // Swiggy restaurant card class
                "//div[contains(@class,'restaurant')]",
                "//a[contains(@class,'restaurant')]",
                "//div[contains(@class,'rest-card')]",
                "//div[contains(@class,'RestaurantList')]",
                "//div[contains(@class,'_2ew_h')]", // Another Swiggy class
                "//*[contains(@class,'restaurant-name')]",
                "//div[contains(@data-testid,'restaurant')]",
                "//a[contains(@href,'restaurant')]",
                "//div[contains(@class,'_3Kib0')]" // Swiggy card container
            };

            boolean targetRestaurantFound = false;
            List<WebElement> allRestaurants = new ArrayList<>();

            // First, collect all restaurant elements
            for (String selector : restaurantSelectors) {
                try {
                    List<WebElement> restaurants = driver.findElements(By.xpath(selector));
                    if (restaurants.size() > 0) {
                        allRestaurants.addAll(restaurants);
                        System.out.println("Found " + restaurants.size() + " restaurants with selector: " + selector);
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            System.out.println("Total restaurant elements found: " + allRestaurants.size());

            // Look for the configured restaurant
            for (int i = 0; i < allRestaurants.size(); i++) {
                WebElement restaurant = allRestaurants.get(i);
                try {
                    if (restaurant.isDisplayed()) {
                        String restaurantText = restaurant.getText().toLowerCase();
                        String targetName = configuredRestaurant.toLowerCase();

                        // Check if this restaurant matches our target
                        if (restaurantText.contains(targetName.split(" ")[0])) { // Match first word (e.g., "domino")
                            // Additional check to ensure it's available for delivery
                            if (!restaurantText.contains("dine out only") &&
                                !restaurantText.contains("pickup only") &&
                                !restaurantText.contains("temporarily closed")) {
                                targetRestaurant = restaurant;
                                targetRestaurantFound = true;
                                System.out.println("Found target restaurant at position " + (i+1) + ": " + restaurantText);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            // If specific restaurant not found, use first available restaurant
            if (!targetRestaurantFound && allRestaurants.size() > 0) {
                for (WebElement restaurant : allRestaurants) {
                    try {
                        if (restaurant.isDisplayed()) {
                            String restaurantText = restaurant.getText().toLowerCase();
                            if (!restaurantText.contains("dine out only") &&
                                !restaurantText.contains("pickup only") &&
                                !restaurantText.contains("temporarily closed")) {
                                targetRestaurant = restaurant;
                                targetRestaurantFound = true;
                                System.out.println("Using first available restaurant: " + restaurantText);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            if (!targetRestaurantFound) {
                takeScreenshot("restaurant_search_error");
                System.out.println("No suitable restaurant found for delivery");
                return configuredRestaurant; // Return default instead of throwing exception
            }

            // Click on the restaurant
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", targetRestaurant);
            Thread.sleep(1000);

            try {
                targetRestaurant.click();
                System.out.println("Clicked on restaurant successfully");
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", targetRestaurant);
                System.out.println("Clicked on restaurant using JavaScript");
            }

            Thread.sleep(5000);
            takeScreenshot("restaurant_page_loaded");

            return configuredRestaurant;

        } catch (Exception e) {
            System.out.println("Error in restaurant search: " + e.getMessage());
            takeScreenshot("restaurant_search_error");
            return restaurantName; // Return default value instead of rethrowing
        }
    }

    private static String selectRestaurantFromHomepage() throws InterruptedException {
        System.out.println("   🔍 Searching for restaurants on homepage...");

        // Scroll down to load more restaurants
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight/2);");
        Thread.sleep(3000);

        // Look for restaurant cards on the homepage with more specific selectors
        String[] restaurantSelectors = {
            "//div[contains(@class,'_1MIkP')]//a",  // Swiggy restaurant card with link
            "//a[contains(@href,'restaurants')]",   // Direct restaurant links
            "//div[contains(@class,'restaurant')]//a",
            "//div[contains(@class,'rest-card')]//a",
            "//a[contains(@href,'menu')]",
            "//div[contains(@data-testid,'restaurant')]//a"
        };

        for (String selector : restaurantSelectors) {
            try {
                List<WebElement> restaurants = driver.findElements(By.xpath(selector));
                System.out.println("   ✓ Found " + restaurants.size() + " restaurants with selector: " + selector);

                if (restaurants.size() > 0) {
                    // Try first few restaurants to find one that works for delivery
                    for (int i = 0; i < Math.min(restaurants.size(), 5); i++) {
                        WebElement restaurant = restaurants.get(i);
                        if (restaurant.isDisplayed()) {
                            try {
                                String restaurantName = restaurant.getText();
                                if (restaurantName.trim().length() > 0) {
                                    System.out.println("   🍕 Trying restaurant: " + restaurantName);

                                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", restaurant);
                                    Thread.sleep(1000);
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", restaurant);
                                    System.out.println("   ✓ Restaurant clicked - navigating to menu");

                                    Thread.sleep(5000);

                                    // Verify we're on a menu page (not dine-out)
                                    String newUrl = driver.getCurrentUrl();
                                    if (newUrl.contains("restaurants") && !newUrl.contains("dineout")) {
                                        takeScreenshot("restaurant_menu_loaded");
                                        return restaurantName;
                                    } else {
                                        System.out.println("   ⚠️  Not a delivery page, trying next restaurant...");
                                        driver.navigate().back();
                                        Thread.sleep(3000);
                                        continue;
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("   ⚠️  Error with restaurant " + i + ": " + e.getMessage());
                                continue;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        throw new RuntimeException("No delivery restaurants found on homepage");
    }

    private static String selectFoodItem() {
        try {
            System.out.println("Selecting food item from restaurant menu...");
            takeScreenshot("before_food_item_selection");

            // Enhanced food item selection with current Swiggy classes
            WebElement firstFoodItem = null;
            String[] foodItemSelectors = {
                "//div[contains(@class,'_1HEoG')]", // Swiggy food item class
                "//div[contains(@class,'menu-item')]",
                "//div[contains(@class,'food-item')]",
                "//div[contains(@class,'dish')]",
                "//div[contains(@class,'_3O0U0')]", // Another Swiggy class
                "//*[contains(@class,'item-name')]",
                "//*[contains(@class,'dish-name')]",
                "//div[contains(@data-testid,'item')]",
                "//a[contains(@href,'/item/')]",
                "//div[contains(@class,'_3Kib0')]" // Swiggy card container
            };

            boolean foodItemFound = false;
            for (int i = 0; i < foodItemSelectors.length; i++) {
                try {
                    System.out.println("Trying food item selector " + (i+1) + ": " + foodItemSelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    firstFoodItem = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(foodItemSelectors[i])));

                    if (firstFoodItem.isDisplayed()) {
                        System.out.println("Found food item with selector: " + foodItemSelectors[i]);
                        foodItemFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Food item not found with selector " + (i+1));
                    continue;
                }
            }

            if (foodItemFound) {
                // Click on the first food item
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstFoodItem);
                    Thread.sleep(1000);
                    firstFoodItem.click();
                    System.out.println("Clicked on food item");
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstFoodItem);
                    System.out.println("Clicked on food item using JavaScript");
                }

                Thread.sleep(5000);
                takeScreenshot("food_item_selected");

                // Return the name of the selected food item
                return firstFoodItem.getText();
            } else {
                System.out.println("No food items found with the specified selectors");
                takeScreenshot("food_item_not_found");
                return "";
            }

        } catch (Exception e) {
            System.out.println("Error selecting food item: " + e.getMessage());
            takeScreenshot("food_item_selection_error");
            return "";
        }
    }

    private static void increaseQuantity() {
        try {
            System.out.println("Increasing quantity of the food item...");
            takeScreenshot("before_quantity_increase");

            // Enhanced quantity selector detection
            WebElement quantitySelector = null;
            String[] quantitySelectors = {
                "//div[contains(@class,'_1gPB8')]", // Swiggy quantity selector class
                "//button[contains(@class,'increment')]",
                "//button[contains(@class,'decrement')]",
                "//input[@type='number']",
                "//input[contains(@class,'quantity')]",
                "//div[contains(@class,'_3Kib0')]" // Swiggy card container
            };

            boolean quantitySelectorFound = false;
            for (int i = 0; i < quantitySelectors.length; i++) {
                try {
                    System.out.println("Trying quantity selector " + (i+1) + ": " + quantitySelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    quantitySelector = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(quantitySelectors[i])));

                    if (quantitySelector.isDisplayed()) {
                        System.out.println("Found quantity selector with selector: " + quantitySelectors[i]);
                        quantitySelectorFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Quantity selector not found with selector " + (i+1));
                    continue;
                }
            }

            if (quantitySelectorFound) {
                // Click on the quantity selector to increase
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", quantitySelector);
                    Thread.sleep(1000);
                    quantitySelector.click();
                    System.out.println("Clicked on quantity selector");
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", quantitySelector);
                    System.out.println("Clicked on quantity selector using JavaScript");
                }

                Thread.sleep(3000);
                takeScreenshot("quantity_increased");
            } else {
                System.out.println("No quantity selector found with the specified selectors");
                takeScreenshot("quantity_selector_not_found");
            }

        } catch (Exception e) {
            System.out.println("Error increasing quantity: " + e.getMessage());
            takeScreenshot("quantity_increase_error");
        }
    }

    private static void enterDeliveryAddress() {
        try {
            System.out.println("Entering delivery address...");
            takeScreenshot("before_address_entry");

            // Enhanced address input detection with current Swiggy selectors
            WebElement addressInput = null;
            String[] addressSelectors = {
                "//textarea[@placeholder*='Address']",
                "//input[@placeholder*='Address']",
                "//input[@placeholder*='Flat, House no., Building, Road']",
                "//input[@placeholder*='Enter your address']",
                "//div[contains(@class,'_1tcx6')]", // Swiggy address class
                "//div[contains(@class,'address')]",
                "//div[contains(@class,'_3Kib0')]" // Swiggy card container
            };

            boolean addressInputFound = false;
            for (int i = 0; i < addressSelectors.length; i++) {
                try {
                    System.out.println("Trying address selector " + (i+1) + ": " + addressSelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    addressInput = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(addressSelectors[i])));

                    if (addressInput.isDisplayed()) {
                        System.out.println("Found address input with selector: " + addressSelectors[i]);
                        addressInputFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Address input not found with selector " + (i+1));
                    continue;
                }
            }

            if (addressInputFound) {
                // Clear and enter address
                try {
                    addressInput.clear();
                    String deliveryAddress = "123, 2nd Main, 3rd Cross, Whitefield, Bengaluru";
                    addressInput.sendKeys(deliveryAddress);
                    System.out.println("Delivery address entered successfully");
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", addressInput);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", addressInput, "123, 2nd Main, 3rd Cross, Whitefield, Bengaluru");
                    System.out.println("Delivery address entered using JavaScript");
                }

                Thread.sleep(3000);
                takeScreenshot("address_entered");

                // Try to find and click save or continue button
                try {
                    WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(),'Save')] | //button[contains(text(),'Continue')] | //button[contains(@class,'submit')]"));
                    if (saveButton.isDisplayed() && saveButton.isEnabled()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
                        Thread.sleep(1000);
                        saveButton.click();
                        System.out.println("Clicked save button");
                    } else {
                        System.out.println("Save button not clickable, trying JavaScript click");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);
                    }

                    Thread.sleep(5000);
                    takeScreenshot("address_saved");
                } catch (Exception e) {
                    System.out.println("Save button not found, trying Enter key");
                    addressInput.sendKeys(Keys.ENTER);
                    Thread.sleep(5000);
                }

            } else {
                System.out.println("Address input not found. Cannot enter delivery address");
                takeScreenshot("address_input_not_found");
            }

        } catch (Exception e) {
            System.out.println("Error entering delivery address: " + e.getMessage());
            takeScreenshot("address_entry_error");
        }
    }

    private static void proceedToPayment() {
        try {
            System.out.println("Proceeding to payment...");
            takeScreenshot("before_payment_proceed");

            // Enhanced payment button detection with current Swiggy selectors
            WebElement paymentButton = null;
            String[] paymentSelectors = {
                "//button[contains(text(),'Proceed to Pay')]",
                "//button[contains(text(),'Place Order')]",
                "//button[contains(@class,'checkout')]",
                "//button[contains(@class,'pay')]",
                "//div[contains(@class,'_3Kib0')]" // Swiggy card container
            };

            boolean paymentButtonFound = false;
            for (int i = 0; i < paymentSelectors.length; i++) {
                try {
                    System.out.println("Trying payment selector " + (i+1) + ": " + paymentSelectors[i]);
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                    paymentButton = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(paymentSelectors[i])));

                    if (paymentButton.isDisplayed() && paymentButton.isEnabled()) {
                        System.out.println("Found clickable payment button with selector: " + paymentSelectors[i]);
                        paymentButtonFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Payment button not found with selector " + (i+1) + ": " + e.getMessage());
                    continue;
                }
            }

            if (paymentButtonFound) {
                // Click on the payment button
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", paymentButton);
                    Thread.sleep(1000);
                    paymentButton.click();
                    System.out.println("Clicked on payment button");
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", paymentButton);
                    System.out.println("Clicked on payment button using JavaScript");
                }

                Thread.sleep(5000);
                takeScreenshot("payment_page_loaded");
            } else {
                System.out.println("Payment button not found, cannot proceed to payment");
                takeScreenshot("payment_button_not_found");
            }

        } catch (Exception e) {
            System.out.println("Error proceeding to payment: " + e.getMessage());
            takeScreenshot("payment_proceed_error");
        }
    }

    private static void printFinalLogs(String restaurantName, String selectedItem) {
        System.out.println("\n=== FINAL LOGS ===");
        System.out.println("Restaurant Name: " + restaurantName);
        System.out.println("Selected Item: " + selectedItem);
        System.out.println("Automation completed successfully.");
        System.out.println("====================");
    }

    private static void takeScreenshot(String stepName) {
        try {
            // Define screenshot file name with step name and timestamp
            String fileName = "screenshot_" + stepName + "_" + System.currentTimeMillis() + ".png";
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile, new File(fileName));
            System.out.println("Screenshot taken: " + fileName);
        } catch (Exception e) {
            System.out.println("Error taking screenshot: " + e.getMessage());
        }
    }
}

