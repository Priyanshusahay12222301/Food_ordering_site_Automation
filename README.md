Swiggy Automation Script
This project automates the food ordering process on Swiggy using Selenium WebDriver in Java.

Features
✅ Login to Swiggy (Manual OTP entry required)
✅ Page title and URL validation
✅ Location entry (Bengaluru)
✅ Restaurant search (Domino's Pizza)
✅ Food item selection (Second item from menu)
✅ Cart operations (Add item, increase quantity)
✅ Delivery address entry
✅ Payment page navigation (without completing payment)
✅ Screenshot capture at key steps
✅ Console logging for all important actions

Prerequisites
Java 22 (or compatible version)
Maven 3.6+
Chrome browser installed
Internet connection
Setup Instructions
Clone/Download the project

Navigate to project directory:

cd C:\Users\priya\OneDrive\Desktop\project\swiggytesting\swiggy
Install dependencies:

mvn clean install
Run the automation:

mvn exec:java -Dexec.mainClass="org.example.Main"
How to Use
Start the script - It will open Chrome browser and navigate to Swiggy
Login Process:
Enter your phone number when prompted in the console
Manually enter the OTP in the browser when it appears
Press Enter in the console after successful login
Automation continues automatically:
Location entry
Restaurant search
Food selection
Cart operations
Address entry
Payment page navigation
Screenshots
Screenshots are automatically saved in the screenshots/ folder:

after_adding_item_to_cart.png
after_increasing_quantity.png
after_entering_delivery_address.png
after_proceeding_to_payment.png
Console Output
The script provides detailed console logs including:

Page titles and URLs
Selected restaurant name
Selected food item
Cart total
Progress messages for each step
Important Notes
⚠️ Manual OTP Entry Required: The script pauses for manual OTP entry as Swiggy requires phone verification.

⚠️ No Payment Completion: The script stops at the payment page without entering payment details.

⚠️ Browser Remains Open: After completion, the browser stays open for manual inspection until you press Enter.

Troubleshooting
If login fails: Ensure you enter a valid phone number and complete OTP verification manually
If elements not found: Swiggy's UI may have changed; the script includes fallback selectors
If screenshots fail: Ensure the project has write permissions in its directory
Recording the Execution
Use any screen recording software (OBS, Bandicam, etc.) to record the script execution and upload to Google Drive as per assignment requirements.

Project Structure
swiggy/
├── pom.xml                 # Maven dependencies
├── README.md              # This file
├── src/main/java/org/example/
│   └── Main.java          # Main automation script
└── screenshots/           # Auto-generated screenshots
