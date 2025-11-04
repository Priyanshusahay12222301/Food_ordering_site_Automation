@echo off
echo.
echo ===============================================
echo    SWIGGY AUTOMATION SCRIPT - ENHANCED VERSION
echo ===============================================
echo.
echo This script automates complete food ordering on Swiggy
echo from login to payment page with screenshots and logging.
echo.
echo REQUIREMENTS CHECKLIST:
echo [✓] Chrome browser installed
echo [✓] Valid phone number for Swiggy login (9508611922)
echo [✓] Good internet connection
echo [✓] Java 11+ installed
echo [✓] Maven installed
echo.
echo WHAT THIS SCRIPT WILL DO:
echo 1. Navigate to Swiggy website
echo 2. Login with phone number (requires manual OTP entry)
echo 3. Set delivery location to Bangalore
echo 4. Navigate to Food Delivery at /restaurants
echo 5. Open the first visible restaurant from the listing
echo 6. Add the second dish on the menu to the cart, then open cart
echo 7. Proceed to checkout and address
echo 8. Proceed to payment page
echo 9. Capture screenshots throughout
echo.
echo IMPORTANT: You will have 30 seconds to manually enter
echo the OTP when prompted during the login process.
echo.
pause
echo.
echo Starting automation...
echo.
mvn exec:java -Dexec.mainClass="org.example.SwiggyAutomationFixed"
echo.
echo ===============================================
echo            AUTOMATION COMPLETED
echo ===============================================
echo.
echo Check the screenshots folder for captured images.
echo.
pause
