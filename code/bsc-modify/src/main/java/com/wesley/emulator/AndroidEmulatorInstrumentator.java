package com.wesley.emulator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.bunq.teacher.IInstrumentator;
import com.bunq.util.Property;

import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.android.Connection;

/**
 * Takes care of all actions that can be executed on an Android device. Depends
 * heavily on Appium.
 * 
 * @author tom
 * @implements IInstrumentator
 */
public class AndroidEmulatorInstrumentator implements IInstrumentator {
	private Property properties = Property.getInstance();

	private AndroidDriver driver;

	/**
	 * Sets all required capabilities to connect to the Android device and
	 * starts the application.
	 */
	public void startApp() {

		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability("deviceName", properties.get("emulatorName"));
		capabilities.setCapability("platformVersion", properties.get("emulatorPlatformVersion"));
		capabilities.setCapability("appPackage", properties.get("appPackage"));
		capabilities.setCapability("appActivity", properties.get("appActivity"));
		capabilities.setCapability("noReset", properties.get("noReset"));
		capabilities.setCapability("newCommandTimeout", Integer.parseInt(properties.get("timeout")));
		// capabilities.setCapability("automationName", "UiAutomator2");

		// System.out.println(capabilities.toString());
		try {
			if (driver == null) {
				driver = new AndroidDriver(new URL("http://127.0.0.1:4724/wd/hub"), capabilities);
				 System.out.println("New Driver Driving");
			}
		} catch (Exception m) {
			System.out.println("Exception in AndroidEmulatorInstrumentator:startApp");
			// System.err.println(m.getMessage());
			m.printStackTrace();
			System.out.println("Exiting");
			System.exit(1);
		}
		login(properties.get("login"));
	}

	public String currentActivityIsInPackage() {
		String currentActivity = driver.currentActivity();
		if (!currentActivity.contains("Launcher")) {
			return properties.get("successMsg");
		} else {
			return properties.get("failureMsg");
		}
	}

	public String activity(String activityName) {
		driver.startActivity(new Activity(properties.get("appPackage"), activityName));
		return currentActivityIsInPackage();
	}

	public String back() {
		driver.pressKeyCode(AndroidKeyCode.BACK);
		return currentActivityIsInPackage();
	}

	/**
	 * Closes the driver so that a new session can be started.
	 */
	public void close() {
		driver.closeApp();
		driver.quit();
		driver = null;
	}

	/**
	 * logs the user in.
	 * 
	 * @param pin
	 *            the pincode of the user
	 * @return logged_in after the device has been logged in
	 */
	public String loginOld(String pin) {
		WebElement we = driver.findElementByXPath(properties.get("pinEditTextPath"));
		if (!we.isSelected()) {
			tryToClick(we);
		}
		we.sendKeys(pin);

		WebDriverWait waitForLogin = new WebDriverWait(driver, 100);
		waitForLogin.until((Function<? super WebDriver, ?>) ExpectedConditions
				.elementToBeClickable(By.id("com.myApp.android:id/myBtn")));
		return "logged_in";
	}

	/**
	 * Does not login the user. Functions as a mock-up.
	 */
	public String login(String pin) {
		return "logged_in";
	}

	/**
	 * Taps the given x and y. Can be used to keep the app alive.
	 * 
	 * @param tapX
	 *            the x coordinate where this method taps
	 * @param tapY
	 *            the y coordinate where this method taps
	 * @return tapped when the function terminates.
	 */
	public String tap(String tapX, String tapY) {
		driver.tap(1, Integer.parseInt(tapX), Integer.parseInt(tapY), 20);
		return "tapped";
	}

	/**
	 * Clicks on the appropriate element
	 * 
	 * @param xpath
	 *            The xPath location of the element to be clicked
	 * @param width
	 *            The width of the element, used for further matching (matching
	 *            using xpath can be inconclusive
	 * @param height
	 *            The height of the element.
	 * @return Returns 0-OK if the element was clicked, otherwise 1-NOTFOUND The
	 *         1-NOTFOUND will be returned by the Action.dispatch method. For
	 *         some reason, the NoSuchElementException that
	 *         AndroidDriver.findElementByXPath sometimes generates cannot be
	 *         caught. Action.dispatch has a way to recover this exception, and
	 *         when that is the case, it returns 1-NOTFOUND. When this Exception
	 *         is thrown, this method does not reach the return the reply of
	 *         tryToClick() which thus will not be returned.
	 */
	public String push(String xpath, String width, String height) {
		/*
		 * try { loop(); } catch (Exception e) { e.printStackTrace();
		 * push(xpath,width,height); }
		 */
		WebElement we = null;
		try {
			we = findElement(xpath, Integer.parseInt(width), Integer.parseInt(height));
			// System.out.println(we);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return tryToClick(we);
		// tryToClick(we);
		// return driver.currentActivity().toString();
	}

	public void loop() {
		// System.out.println("\n\n\n\n\nlopPERDELOPERLDEOPAJFILSJKDFILUSD");

		while (true) {
			System.out.println("---------------------------------------------------");
			// System.out.print("lop");
			List<WebElement> we = driver.findElements(By.xpath("//*"));

			// WebElement el=(WebElement)
			// driver.findElementByAndroidUIAutomator("new
			// UiSelector().text(\"netwerkfout\") ");
			// List<WebElement> we =
			// driver.findElements(By.className("android.widget.Toast"));
			// WebElement wel = driver.findElementByAndroidUIAutomator("new
			// UiSelector().text(\"netwerkfout\") ");

			// List<WebElement> we = driver.findElement(By.name("Er was een
			// netwerkfout"));
			// for(WebElement w : we){
			// System.out.print(w.getTagName() + " : ");
			// System.out.println(w.getText());
			// }
			// System.out.println(we);
			for (WebElement w : we) {
				// System.out.print(w.getTagName() + " : ");
				String r = w.getText();
				if (!"".equals(r) && r.contains("netwerkfout")) {
					System.out.println("WWWWW");
					System.out.println(w.getTagName());
					System.out.println(w.getText());
				}
			}
			// // System.out.println("text: " + s);
			System.out.println("---------------------------------------------------\n");
		}
	}

	public List<String> getTextOnScreen() {
		List<WebElement> webels = null;
		try {
			webels = driver.findElements(By.xpath("//*"));
		} catch (Exception e) {
			System.out.println("Could not find elements");
		}
		List<String> strings = new ArrayList<String>();
		if (webels != null) {
			for (WebElement w : webels) {
					if (!"".equals(w.getText())) {
						strings.add(w.getText());
					}
			}
		}
		return strings;

	}

	public String isTextVisible(String[] terms) {
		List<WebElement> webels = null;
		try {
			webels = driver.findElements(By.xpath("//*"));
		} catch (Exception e) {
			System.out.println("Could not find elements");
		}
		if (webels != null) {
			for (WebElement w : webels) {
				for (String term : terms) {
					if (w.getText().contains(term)) {
						return w.getText();
					}
				}
			}
		}
		return null;
	}

	public String getCurrentActivity() {

		if (driver == null) {
			System.out.println("THEDRIVERISNULLLLLLL");
			this.startApp();
		}

		String res = driver.currentActivity();
		if (res == null) {
			System.out.println("res = null");
			this.close();
			this.startApp();
			return getCurrentActivity();
		}

		return res;
	}

	/**
	 * Checks the appropriate element
	 * 
	 * @param xpath
	 *            The xPath location of the element to be checked
	 * @param width
	 *            The width of the element, used for further matching (matching
	 *            using xpath can be inconclusive
	 * @param height
	 *            The height of the element.
	 * @return Returns 0-OK if the element was checked, otherwise 1-NOTFOUND
	 */
	public String check(String xpath, String width, String height) {
		return push(xpath, width, height);
	}

	/**
	 * Enters text in the appropriate element
	 * 
	 * @param xpath
	 *            The xPath location of the element in which text should be
	 *            entered
	 * @param width
	 *            The width of the element, used for further matching (matching
	 *            using xpath can be inconclusive
	 * @param height
	 *            The height of the element.
	 * @param argument
	 *            The text that should be entered
	 * @return Returns 0-OK if the text was entered, otherwise 1-NOTFOUND. SEE
	 *         for 1-NOTFOUND the docs accompanying the push method
	 */
	public String enterText(String xpath, String width, String height, String argument) {
		if (driver.isKeyboardShown()) {
			driver.getKeyboard().sendKeys(Keys.RETURN);
		}

		WebElement we = findElement(xpath, Integer.parseInt(width), Integer.parseInt(height));
		if (!we.isSelected()) {
			tryToClick(we);
		}
		try {
			Integer.parseInt(argument);
			we.sendKeys(argument);
			we.click();
		} catch (NumberFormatException nfe) {
			we.sendKeys(argument);
		}
		driver.getKeyboard().sendKeys(Keys.RETURN);

		return properties.get("successMsg");
	}

	/**
	 * Enters text in the appropriate element
	 * 
	 * @param xpath
	 *            The xPath location of the element in which text should be
	 *            entered
	 * @param width
	 *            The width of the element, used for further matching (matching
	 *            using xpath can be inconclusive
	 * @param height
	 *            The height of the element.
	 * @param argument
	 *            The text that should be entered
	 * @return Returns 0-OK if the text was entered, otherwise 1-NOTFOUND. SEE
	 *         for 1-NOTFOUND the docs accompanying the push method
	 */
	public String enterTextOld(String xpath, String width, String height, String argument) {
		WebElement we = findElement(xpath, Integer.parseInt(width), Integer.parseInt(height));
		if (!we.isSelected()) {
			tryToClick(we);
		}
		try {
			Integer.parseInt(argument);
			we.sendKeys(argument);
			// driver.navigate().back();
			driver.getKeyboard().sendKeys(Keys.RETURN);

		} catch (NumberFormatException nfe) {
			we.sendKeys(argument);
			driver.getKeyboard().sendKeys(Keys.RETURN);
		}

		return properties.get("successMsg");
	}

	
	public void finger(){
		try{
//			driver.
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Tries to click the given element. It checks if the element is enabled en
	 * displayed and then clicks the element.
	 * 
	 * @param we
	 *            the element that you want to click
	 * @return the standard successmessage when the element got clicked, else
	 *         the failuremsg
	 */
	public String tryToClick(WebElement we) {
		// System.out.println("in trytoclick");
		try {
			if (we.isDisplayed() && we.isEnabled()) {
				we.click();
				return properties.get("successMsg");
			}
			if (driver.getConnection() != Connection.ALL) {
				System.out.println("\n\n TrytoClick failed due to lack of connection. WARNING: Recursive call\n\n");
				return tryToClick(we);
			} else {
				System.out.println("\n In trytoclick negation");
				System.out.println("we.isDisplayed: " + we.isDisplayed() + " | we.isEnabled(): " + we.isEnabled()
						+ "  | connection : " + driver.getConnection());
				System.out.println("we dump:");
				System.out.println(we.toString());
				return properties.get("failureMsg");
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return properties.get("failureMsg");
	}

	/**
	 * Finds the element that is described by the given xpath and the given
	 * width and height.
	 * 
	 * @param xpath
	 *            the xpath that leads to the element
	 * @param width
	 *            the width of the element
	 * @param height
	 *            the height of the element
	 * @return an element if it can be matched to the given arguments
	 * @throws NoSuchElementException
	 *             when no match could be found
	 */
	private WebElement findElement(String xpath, int width, int height) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			// never mind, does not really matter.
		}
		List<WebElement> weList = driver.findElementsByXPath(xpath);
		if (weList.isEmpty()) {
			throw new NoSuchElementException("Given xpath gave no results");
			// True for notfound
		} else if (weList.size() == 1) { // this is good
			return weList.get(0);
		} else {
			// The following doesn't work anymore nullpointer at we.getsize,
			// although we is a valid webelement.
			// whaaattheeeefuuuckk.
			System.out.println("PROBLEMOSPROBLEMOSPROBS");
			// int i =0;
			for (WebElement we : weList) {
				// System.out.println("III: " +i++);
				// System.out.println("WEEE " + we.toString());
				//
				Dimension currentDim = we.getSize();
				if (currentDim.getHeight() == height && currentDim.getWidth() == width) {
					return we;
				}
			}
		}
		throw new NoSuchElementException("xPath gave results, yet width and height did not match.");
	}

	/**
	 * Invokes either the softReset or hardReset method.
	 * 
	 * @param method
	 *            should be hard_reset or soft_reset or semi_soft_reset.
	 * @return the output of the invoked reset method, or reset_failed when none
	 *         was invoked
	 */
	public String reset(String method) {
		String resetReturn;
		if (method.equals("hard_reset")) {
			resetReturn = hardReset();
		} else if (method.equals("finish_hard_reset")) {
			resetReturn = finishHardReset();
		} else if (method.equals("soft_reset")) {
			resetReturn = softReset();
		} else if (method.equals("semi_soft_reset")) {
			resetReturn = semiSoftReset();
		} else if (method.equals("pl_hard_reset")) {
			resetReturn = hardReset();
			try {
				Thread.sleep(500);
				finishHardReset();
				Thread.sleep(500);
			} catch (Exception e) {

			}

		} else {
			resetReturn = "reset_failed";
		}
		return resetReturn;
	}

	/**
	 * Closes the app.
	 * 
	 * @return hard_reset_succeeded when the function is done.
	 */
	private String hardReset() {
		try{
			driver.closeApp();
		}catch(Exception e){
			
		}
		return "hard_reset_succeeded";
	}

	/**
	 * boots the application again; also logs in on standard login.
	 * 
	 * @return finish_hard_reset_succeeded when the function is done.
	 */
	private String finishHardReset() {
		driver.launchApp();
		return "finish_hard_reset_succeeded";
	}

	/**
	 * Returns the app to the overview. Does not restart the app! If this method
	 * fails, it falls back on the semiSoftReset method.
	 * 
	 * @return soft_reset_succeeded when the function is done, or the output of
	 *         semiSoftReset when the softreset failed.
	 */
	private String softReset() {
		int width = properties.getInt("overviewBtnWidth");
		int height = properties.getInt("overviewBtnHeight");
		int softResetCount = properties.getInt("softResetCount");
		String xpath = properties.get("overviewBtnXPath");

		for (int i = 0; i < softResetCount; i++) {
			try {
				WebDriverWait waitFor = new WebDriverWait(driver, 3);

				waitFor.until(
						(Function<? super WebDriver, ?>) ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
				findElement(xpath, width, height).click();
				return "soft_reset_succeeded";
			} catch (TimeoutException | NoSuchElementException e) {
				System.out.println("Not found, press back.");
				driver.navigate().back();
			}
		}
		System.out.println("Soft reset did not succeed, doing a semi-soft reset.");
		return semiSoftReset();
	}

	/**
	 * Only restarts the main activity, followed by a login operation.
	 * 
	 * @return 'semi_soft_reset_succeeded' when the function is done.
	 */
	private String semiSoftReset() {
		String appPackage = properties.get("appPackage");
		String appActivity = properties.get("appActivity").replace(appPackage, "");
		driver.startActivity(appPackage, appActivity);
		login(properties.get("login"));
		return "semi_soft_reset_succeeded";
	}

}
