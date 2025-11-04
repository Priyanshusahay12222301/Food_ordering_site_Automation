// @ts-check
const { Builder } = require('selenium-webdriver');
const RestaurantSearchUtil = require('./restaurant-search-util');

async function testRestaurantSearch() {
    let driver;
    try {
        // ...existing code to init driver...
        driver = await new Builder().forBrowser('chrome').build();

        const searchUtil = new RestaurantSearchUtil(driver);

        // NEW: Go via Dining -> Online order -> select "Oven Express"
        await searchUtil.openRestaurantFromDining('bangalore', 'Oven Express');

        console.log('Opened "Oven Express" page from Dining > Online order');
        await driver.sleep(3000); // brief visual confirmation
    } catch (error) {
        console.error('Test failed with error:', error);
    } finally {
        if (driver) await driver.quit();
    }
}

testRestaurantSearch();
