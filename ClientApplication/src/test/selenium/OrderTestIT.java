package selenium;// Generated by Selenium IDE
import com.client.application.enduser.EndUserApplication;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = EndUserApplication.class)
public class OrderTestIT {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;

  @Before
  public void setUp() throws InterruptedException {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless", "--disable-gpu");
    driver = new ChromeDriver(options);
    js = (JavascriptExecutor) driver;
    vars = new HashMap<>();
    driver.get("http://localhost:8080/login.html");
    logIN();
  }

  @After
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void seeOrderAndItsDetails() {
    driver.findElement(By.id("name")).click();
    driver.findElement(By.id("menuOrders")).click();
    driver.findElement(By.cssSelector("h1")).click();
    assertThat(driver.findElement(By.cssSelector("h1")).getText(), is("Your Orders"));
    driver.findElement(By.id("order_1")).click();
    assertThat(driver.findElement(By.id("order_1")).getText(), is("Order 1"));
    driver.findElement(By.linkText("Details/Invoice")).click();
    WebDriverWait wait = new WebDriverWait(driver, 30);
    wait.until(ExpectedConditions.elementToBeClickable(By.id("order_id")));
   assertThat(driver.findElement(By.id("order_id")).getText(), containsString("Order #1"));
  }

  public void logIN() throws InterruptedException {
    driver.findElement(By.id("inputUsername")).click();
    driver.findElement(By.id("inputUsername")).sendKeys("buyer1");
    driver.findElement(By.id("inputPassword")).sendKeys("pw");
    driver.findElement(By.id("signIn")).click();
    try {
      WebDriverWait wait = new WebDriverWait(driver, 30);
      wait.until(ExpectedConditions.alertIsPresent());
      Alert alert = driver.switchTo().alert();
      alert.accept();
    } catch (Exception e) {
      System.err.println(e);
    }
    Thread.sleep(1000);
    driver.findElement(By.id("signIn")).click();
  }



}
