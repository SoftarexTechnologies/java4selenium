package tests.base;

import exceptions.base.AbstractSeleniumException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 */
public abstract class AbstractTest {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractTest.class);

    protected WebDriver driver;

    protected String baseUrl;

    /**
     * @return true, if test failed
     */
    public boolean run(WebDriver driver) {
        this.driver = driver;
        return run();
    }

    /**
    * @return true, if test failed
    */
    public boolean run() {
        boolean testFailed = false;
        setUp();
        if (driver == null) {
            driver = new FirefoxDriver();
        }
        try {
            runTest();
        } catch (AbstractSeleniumException e) {
            testFailed = true;
            logger.error(e.getMessage(), e);
            onError(e);
        } finally {
            tearDown();
        }
        return testFailed;
    }

    protected abstract void setUp();

    protected abstract void runTest() throws AbstractSeleniumException;

    protected void onError(AbstractSeleniumException e){
    }

    protected void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
