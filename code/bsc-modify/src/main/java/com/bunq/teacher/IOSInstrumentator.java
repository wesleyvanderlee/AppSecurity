package com.bunq.teacher;

import com.bunq.util.Property;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.ios.IOSDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

/**
 * Takes care of all actions that can be executed on an Android device. Depends heavily on Appium.
 * 
 * @author michel
 * @implements IInstrumentator
 */
public class IOSInstrumentator implements IInstrumentator {

  private static Property properties = Property.getInstance();

  private static final String USER = properties.get("userName");
  private IOSDriver driver;

  /**
   * Sets all required capabilities to connect to the iOS device and starts the application.
   */
  public void startApp() {
    DesiredCapabilities capabilities = new DesiredCapabilities();

    capabilities.setCapability("deviceName", properties.get("deviceName"));
    capabilities.setCapability("platformVersion", properties.get("platformVersion"));
    capabilities.setCapability("app", properties.get("app"));
    
    capabilities.setCapability("noReset", properties.get("noReset"));
    capabilities.setCapability("newCommandTimeout", properties.get("timeout"));

    try {
      driver = new IOSDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
    } catch (MalformedURLException m) {
      System.out.println(m.getMessage());
      System.exit(1);
    }
    login(properties.get("login"));
  }

  /**
   * Closes the driver so that a new session can be started.
   */
  public void close() {
    driver.closeApp();
    driver.quit();
  }

  /**
   * logs the user in.
   * 
   * @param pin
   *          the pincode of the user
   */
  public String login(String pin) {
    if (inputPin(pin).equals(properties.get("successMsg"))) {
      WebDriverWait waitForLogin = new WebDriverWait(driver, 100);
      waitForLogin.until((Function<? super WebDriver, ?>) ExpectedConditions.elementToBeClickable(By.name("New Message")));
      return "logged_in";
    } else {
      return "login_failed";
    }
  }

  /**
   * Input the PIN, as it requires the keyboard instead of sendKeys() function.
   * 
   * @param pin
   *          The pin to use
   * @return Returns properties.get(successMsg) if everything went ok, when the element could not be
   *         found properties.get(failureMsg) and in case of an error properties.get(errorMsg).
   */
  public String inputPin(String pin) {
    WebElement we = driver.findElementByName("PIN field for user " + USER);
    if (!we.isSelected()) {
      we.click();
    }
    driver.getKeyboard().sendKeys(pin);

    try {
      Thread.sleep(4000); // Sleep in order to let the pin be validated
    } catch (InterruptedException e) {
      System.err.println("Thread sleep was interrupted: " + e.getMessage());
      return properties.get("errorMsg");
    }
    return properties.get("successMsg");
  }
  
  /**
   * Taps the given x and y. Can be used to keep the app alive.
   * @param tapX
   *          the x coordinate where this method taps
   * @param tapY
   *          the y coordinate where this method taps
   * @return tapped when the function terminates.
   */
  public String tap(String tapX, String tapY) {
    driver.tap(1, Integer.parseInt(tapX), Integer.parseInt(tapY), 20);
    return "tapped";
  }

  /**
   * Clicks on the appropriate UIelement.
   * 
   * @param resourceName
   *          The name of the element to be clicked
   * @return Returns properties.get(successMsg) if the element was clicked, otherwise
   *         properties.get(failureMsg)
   */
  public String push(String resourceName) {
    try {
      return tryToClick(findElement(resourceName));
    } catch (NoSuchElementException nsee) {
      return properties.get("failureMsg");
    }
  }

  /**
   * Clicks on the appropriate UIelement.
   * 
   * @param resourceName
   *          The name of the element to be clicked
   * @param value
   *          The current value of the element to be clicked
   * @return Returns properties.get(successMsg) if the element was clicked, otherwise
   *         properties.get(failureMsg)
   */
  public String push(String resourceName, String value) {
    try {
      return tryToClick(findElement(resourceName, value));
    } catch (NoSuchElementException nsee) {
      return properties.get("failureMsg");
    }
  }

  /**
   * Checks the appropriate UIelement.
   * 
   * @param resourceName
   *          The name of the element to be checked
   * @param value
   *          The current value of the element to be checked
   * @return Returns properties.get(successMsg) if the element was checked, otherwise
   *         properties.get(failureMsg)
   */
  public String check(String resourceName, String value) {
    return push(resourceName, value);
  }

  /**
   * Enters text in the appropriate UIelement.
   * 
   * @param resourceName
   *          The name of the element in which text should be entered
   * @param argument
   *          The text that should be entered
   * @return Returns properties.get(successMsg) if the text was correctly entered, otherwise
   *         properties.get(failureMsg)
   */
  public String enterText(String resourceName, String argument) {
    if (resourceName.contains("PIN")) {
      return inputPin(argument);
    }
    return enterText(resourceName, null, argument);
  }

  /**
   * Enters text in the appropriate UIelement.
   * 
   * @param resourceName
   *          The name of the element in which text should be entered
   * @param value
   *          The current value of the element in which text should be entered
   * @param argument
   *          The text that should be entered
   * @return Returns properties.get(successMsg) if the text was correctly entered, otherwise
   *         properties.get(failureMsg)
   */
  public String enterText(String resourceName, String value, String argument) {
    if (resourceName.contains("PIN")) {
      return inputPin(argument);
    }

    try {
      WebElement textEle = findElement(resourceName, value);
      textEle.sendKeys(argument);
      try {
    	  Integer.parseInt(argument);  	  
      } catch (NumberFormatException e){
    	  textEle.sendKeys(Keys.ENTER);
      }      
      return properties.get("successMsg");
    } catch (NoSuchElementException nsee) {
      return properties.get("failureMsg");
    }
  }

  /**
   * Resets the application to a useable state for the learner. Depending on the used commands, a
   * hard- or softreset will be carried out
   * 
   * @param method
   *          The String containing whether it should be a hard- or soft-reset
   * @return Returns type_reset_succeeded (type = hard / soft) in case of success, otherwise returns
   *         reset_failed
   */
  public String reset(String method) {
    if (method.equals("hard_reset")) {
      return hardReset();
    } else if (method.equals("soft_reset")) {
      return softReset();
    } else if (method.equals("finish_hard_reset")){
    	return finishHardReset();
    } else {
      return "reset_failed";
    }
  }

  /**
   * This functions tries to reset the application by going back to the overview screen
   * 
   * @param index
   *          The current number of tries to get to the overview screen
   * @return Returns soft_reset_succeeded if the soft reset was possible, otherwise it will return a
   *         hardReset message.
   */
  private String softReset() {
    for (int i = 0; i < properties.getInt("softReset"); i++) {
      if (push("Overview").equals(properties.get("successMsg"))) {
        return "soft_reset_succeeded";
      } else if (!push("Back").equals(properties.get("successMsg"))) {
        push("Cancel");
      }
    }
    System.out.println("Soft reset failed: now trying a hard reset");
    hardReset();
    return finishHardReset();
  }

  /**
   * Tries to click on a specified web element.
   * 
   * @param we
   *          The element to be clicked on
   * @return Returns properties.get(sucessMsg) if click was successful or properties.get(failureMsg)
   *         if element was not clickable
   */
  private String tryToClick(WebElement we) {
    if (we.isDisplayed() && we.isEnabled() &&
    		(!we.getTagName().equals("UIANavigationBar") || we.getAttribute("name").equals("Overview"))){
      we.click();
      return properties.get("successMsg");
    } else {
      return properties.get("failureMsg");
    }
  }

  /**
   * Performs a hard reset. This means that the application is closed and re-launched, followed by a
   * tap on the 'Overview' tab as iOS remembers the last used tab while the program expects to start
   * on the Overview tab.
   * 
   * @return hard_reset_succeeded when the reset is finished
   */
  private String hardReset() {
    driver.closeApp();
    return "hard_reset_succeeded";
  }
  
  private String finishHardReset(){
	driver.launchApp();
    login(properties.get("login"));

    push("Overview"); // always click overview, in case another tab was selected
    return "finish_hard_reset_succeeded";
  }

  /**
   * Finds the WebElement with the given resourceName.
   * 
   * @param resourceName
   *          the name of element we want to retrieve.
   * @return the WebWlement with the given name. In case of multiple instances with this name, the
   *         first one is returned.
   * @throws NoSuchElementException
   *           when the search gave no results
   */
  public WebElement findElement(String resourceName) throws NoSuchElementException {
    List<WebElement> weList = driver.findElementsByName(resourceName);
    if (weList.size() >= 1) {
      return weList.get(0);
    } else {
      throw new NoSuchElementException(resourceName + " could not be found!");
    }
  }

  /**
   * Finds the WebElement with the given resourceName and value.
   * 
   * @param resourceName
   *          the name of element we want to retrieve.
   * @param value
   *          the contents of the value attribute we want to retrieve
   * @return the WebWlement with the given name and value; in case of multiple possibilities, this
   *         function returns the first one.
   * @throws NoSuchElementException
   *           when the search gave no results
   */
  public WebElement findElement(String resourceName, String value) throws NoSuchElementException {
    if (value == null) {
      return findElement(resourceName);
    }
    
    List<WebElement> weList = driver.findElementsByName(resourceName);
    for (WebElement we : weList) {
      if (we.getAttribute("value").equals(value)) {
        return we;
      }
    }
    throw new NoSuchElementException(resourceName + " with value " + value 
        + " could not be found.");
  }
}
