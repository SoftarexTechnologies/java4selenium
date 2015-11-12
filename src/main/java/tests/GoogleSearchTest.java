package tests;

import common.enums.markers.SeleniumTest;
import common.enums.markers.SeleniumTestGroup;
import exceptions.base.AbstractSeleniumException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import tests.base.AbstractTest;


/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 *
 * This is example of test
 *
 */
@SeleniumTest(name = "GoogleSearchTest")
@SeleniumTestGroup(name = "GoogleGroup")
public final class GoogleSearchTest extends AbstractTest {

    @Override
    protected void setUp() {
        baseUrl = "https://www.google.by/";
    }

    @Override
    protected void runTest() throws AbstractSeleniumException {
        //go to page
        driver.get(baseUrl);
        //find search element
        WebElement searchField = driver.findElement(By.id("lst-ib"));
        //set text to search
        searchField.sendKeys("google is...");
        //do search
        searchField.sendKeys(Keys.ENTER);
        //check
        try{
            new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id("resultStats")));
            driver.findElement(By.id("resultStats"));
        }catch (NoSuchElementException e){
            throw new AbstractSeleniumException(e.getMessage());
        }
    }

    @Override
    protected void onError(AbstractSeleniumException e) {
        e.printStackTrace();
    }

}
