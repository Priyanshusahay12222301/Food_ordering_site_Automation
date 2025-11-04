// @ts-check
const { Builder, By, Key, until } = require('selenium-webdriver');
/** @typedef {import('selenium-webdriver').WebDriver} WebDriver */

class RestaurantSearchUtil {
    /**
     * @param {WebDriver} driver
     */
    constructor(driver) {
        /** @type {WebDriver} */
        this.driver = driver;
    }

    async setLocation(location) {
        // Try to focus/open the location input if it's behind a trigger
        try {
            const triggers = await this.driver.findElements(
                By.css('[data-testid*="location"] button, [aria-label*="location" i], [data-testid*="change-address"]')
            );
            if (triggers.length) await triggers[0].click();
        } catch { /* ignore */ }

        // Type location
        const input = await this.driver.wait(
            until.elementLocated(
                By.css('input[placeholder*="location" i], input[placeholder*="delivery" i], input[placeholder*="area" i]')
            ),
            10000
        );
        await input.clear();
        await input.sendKeys(location);

        // Pick first suggestion
        const firstSuggestion = await this.driver.wait(
            until.elementLocated(
                By.css('[role="listbox"] [role="option"], [data-testid*="suggestion"] li, li[class*="suggestion"]')
            ),
            10000
        );
        await firstSuggestion.click();

        // Allow page to update to selected location
        await this.driver.sleep(1000);
    }

    // NEW: Complete flow until opening the restaurant page from Dining > Online order
    async openRestaurantFromDining(location = 'bangalore', restaurantName = 'Oven Express') {
        await this.driver.get('https://www.swiggy.com/');
        await this.setLocation(location);
        await this.goToDiningPage();
        await this.goToOnlineOrderTab();
        await this.selectRestaurantByName(restaurantName);
        // Stop here as requested (restaurant page opened)
    }

    // NEW: Click the Dining page entry
    async goToDiningPage() {
        const candidates = [
            By.xpath("//a[.//text()[contains(.,'Dining') or contains(.,'Dineout')]]"),
            By.css("a[href*='dineout'], a[href*='dining']"),
            By.xpath("//button[.//text()[contains(.,'Dining') or contains(.,'Dineout')]]")
        ];
        let link;
        for (const by of candidates) {
            try {
                link = await this.driver.wait(until.elementLocated(by), 5000);
                break;
            } catch { /* try next */ }
        }
        if (!link) throw new Error('Dining link not found');
        await link.click();
        await this.driver.wait(
            until.urlMatches(/dine|dining|dineout/i),
            10000
        );
    }

    // NEW: Switch to "Online order" tab inside Dining
    async goToOnlineOrderTab() {
        const tabBy = By.xpath(
            "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'online order') or " +
            "contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'order online') or " +
            "contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delivery') or " +
            "contains(@data-testid,'online-order-tab')]"
        );
        const tab = await this.driver.wait(until.elementLocated(tabBy), 10000);
        await tab.click();
        // wait for restaurant list grid/cards
        await this.driver.wait(
            until.any(
                until.elementLocated(By.css('[data-testid="restaurant-card"]')),
                until.elementLocated(By.css('[class*="restaurant"], [class*="card"]'))
            ),
            10000
        );
    }

    // NEW: Find and click a restaurant card by visible name
    async selectRestaurantByName(name = 'Oven Express') {
        const normalizedName = name.toLowerCase();

        // Try to locate without scrolling first
        let target = await this.findRestaurantCardByNameOnce(normalizedName);
        // If not found, try to scroll a few times (lazy loaded lists)
        let attempts = 0;
        while (!target && attempts < 8) {
            await this.driver.executeScript('window.scrollBy(0, Math.min(800, document.body.scrollHeight));');
            await this.driver.sleep(400);
            target = await this.findRestaurantCardByNameOnce(normalizedName);
            attempts++;
        }
        if (!target) throw new Error(`Restaurant "${name}" not found`);

        await target.click();

        // Wait for restaurant/menu page to load (menu items or header)
        await this.driver.wait(
            until.any(
                until.elementLocated(By.css('[data-testid="menu-item"]')),
                until.elementLocated(By.css('[class*="menu"] [class*="item"]')),
                until.elementLocated(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'recommended')]"))
            ),
            10000
        );
    }

    // Helper: one-shot find of a restaurant card by name
    async findRestaurantCardByNameOnce(lowerName) {
        const cardSelectors = [
            '[data-testid="restaurant-card"]',
            '[class*="restaurant"]:not([role="tab"])',
            '[class*="card"]'
        ].join(',');
        const cards = await this.driver.findElements(By.css(cardSelectors));
        for (const card of cards) {
            try {
                const text = (await card.getText()).toLowerCase();
                if (text.includes(lowerName)) return card;
            } catch { /* ignore */ }
        }
        // Fallback: any link with the name
        try {
            return await this.driver.findElement(
                By.xpath(`//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'${lowerName}')]`)
            );
        } catch {
            return null;
        }
    }

    async close() {
        await this.driver.quit();
    }
}

module.exports = RestaurantSearchUtil;
