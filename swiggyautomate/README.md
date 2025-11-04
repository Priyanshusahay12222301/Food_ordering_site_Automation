# Swiggy End-to-End UI Automation

## Project Overview
This project implements a complete end-to-end UI automation test for the Swiggy food ordering flow using Java, Selenium WebDriver, and TestNG.

## Technical Stack
- **Language**: Java (JDK 11+)
- **Automation Tool**: Selenium WebDriver
- **Build Tool**: Apache Maven
- **Testing Framework**: TestNG
- **Driver Management**: WebDriverManager
- **Target Website**: https://www.swiggy.com/

## Automation Flow
The test script performs the following steps:

1. **Login Process**: Navigates to Swiggy, clicks login, enters phone number
2. **OTP Handling**: Pauses for 30 seconds for manual OTP entry
3. **Post-Login Validation**: Captures page title and URL
4. **Location Setup**: Sets delivery location to "Bengaluru"
5. **Restaurant Search**: Searches for "Domino's Pizza" and selects first result
6. **Add to Cart**: Adds the second available food item to cart
7. **Quantity Modification**: Increases item quantity to 2
8. **Address Entry**: Adds new delivery address (Door: 123, Landmark: Near Main Road, Type: Home)
9. **Payment Page**: Navigates to payment gateway (automation stops here)

## Console Logging
The script logs the following information:
- Page title and URL after login
- Selected restaurant name
- Selected food item name
- Total cart value after quantity increase

## Screenshot Capture
The following screenshots are automatically captured:
- `screenshot_after_adding_item.png`
- `screenshot_after_increasing_quantity.png`
- `screenshot_after_entering_address.png`
- `screenshot_payment_page.png`

## Prerequisites
1. Java JDK 11 or higher installed
2. Maven installed and configured
3. Chrome browser installed
4. Internet connection for WebDriverManager to download ChromeDriver

## How to Run

### Using Maven Command Line
```bash
# Navigate to project directory
cd C:\Users\priya\OneDrive\Desktop\project\swiggytesting\swiggyautomate

# Clean and compile the project
mvn clean compile

# Run the tests
mvn test

# Or run with TestNG XML file
mvn test -DsuiteXmlFile=testng.xml
```

### Using IDE (IntelliJ IDEA / Eclipse)
1. Import the project as a Maven project
2. Ensure all dependencies are downloaded
3. Right-click on `testng.xml` and select "Run"
4. Or right-click on `SwiggyAutomation.java` and run the test method

## Important Notes

### Manual Intervention Required
- **OTP Entry**: When the script pauses for OTP entry, you have 30 seconds to manually enter the OTP received on your phone
- **Phone Number**: The script uses a sample phone number (9876543210). Replace with a valid number if needed

### Expected Artifacts
After successful execution, you should find:
- Four PNG screenshot files in the project root directory
- Console output with detailed step-by-step logging
- TestNG reports in `target/surefire-reports/`

## Troubleshooting

### Common Issues
1. **Element Not Found**: Swiggy's UI changes frequently. The script includes alternative selectors and fallback methods
2. **Timeout Issues**: Increase wait times if your internet connection is slow
3. **ChromeDriver Issues**: WebDriverManager automatically handles driver versions
4. **OTP Timeout**: Ensure you enter OTP within the 30-second window

### Configuration
- Default wait time: 20 seconds
- OTP wait time: 30 seconds
- Chrome browser runs in maximized mode with notifications disabled

## Project Structure
```
swiggyautomate/
├── pom.xml                 # Maven configuration
├── testng.xml             # TestNG suite configuration
├── README.md              # This file
└── src/
    ├── main/java/         # Main source code
    └── test/java/
        └── SwiggyAutomation.java  # Test automation class
```

## Dependencies
- Selenium WebDriver 4.15.0
- TestNG 7.8.0
- WebDriverManager 5.5.3
- Apache Commons IO 2.11.0

## Support
For issues or questions related to this automation suite, please refer to the detailed console logs and screenshot evidence generated during test execution.
