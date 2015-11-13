package tests;

import common.enums.markers.SeleniumTest;
import common.enums.markers.SeleniumTestGroup;
import exceptions.LoginFailedException;
import exceptions.base.AbstractSeleniumException;
import org.openqa.selenium.By;
import tests.base.AbstractTest;

/**
 * Created by Ugene Reshetnyak on 12.11.2015.
 *
 * This is example of test
 *
 */
@SeleniumTest(name = "JiraLoginTest")
@SeleniumTestGroup(name = "JiraGroup")
public final class JiraLoginTest extends AbstractTest {
    @Override
    protected void setUp() {
        baseUrl = "https://jira.softarex.com/login.jsp";
    }

    @Override
    protected void runTest() throws LoginFailedException {
        //go to Jira login page
        driver.get(baseUrl);
        //fill form
        driver.findElement(By.id("login-form-username")).sendKeys("login");
        driver.findElement(By.id("login-form-password")).sendKeys("password");
        //do login
        String loginPageUrl = driver.getCurrentUrl();
        driver.findElement(By.id("login-form-submit")).click();
        //check
        if(driver.getCurrentUrl().equals(loginPageUrl)){
            throw new LoginFailedException("Invalid password");
        }

    }

    @Override
    protected void onError(AbstractSeleniumException e) {
        System.out.println(driver.findElement(By.cssSelector("#login-form > div.form-body > div.aui-message.error p")).getText());
    }

}
