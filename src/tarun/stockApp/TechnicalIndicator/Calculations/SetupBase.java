package tarun.stockApp.TechnicalIndicator.Calculations;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class SetupBase {
	public  WebDriver driver = null;
	final String timeOut = "2000";
	String downloadFilepath = "c:\\Selenium\\download";
	static Logger logger = Logger.getLogger(SetupBase.class);
	
	public void setupSelenium(String URL) {
		try {
			logger.debug("Setup Selenium Started");
			File file = new File("C:\\Selenium\\chromedriver.exe");
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
	       chromePrefs.put("profile.default_content_settings.popups", 0);
	       chromePrefs.put("download.default_directory", downloadFilepath);
	       ChromeOptions options = new ChromeOptions();
	       HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
	       options.setExperimentalOption("prefs", chromePrefs);
	       options.addArguments("--test-type");
	       options.addArguments("--disable-extensions"); //to disable browser extension popup
	       //System.out.println("Here");
			
			DesiredCapabilities capabilities = null;
			
			capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptionsMap);
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			//capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "accept");
			System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
			//System.out.println("Here");
			driver = new ChromeDriver(capabilities);
			//driver = new HtmlUnitDriver(BrowserVersion.CHROME);
			//((HtmlUnitDriver)driver).setJavascriptEnabled(true);
			driver.manage().timeouts().pageLoadTimeout(Long.parseLong(timeOut), TimeUnit.SECONDS);
			driver.manage().window().maximize();
			driver.get(URL);

		} catch (Exception ex) {
			logger.error("Error occurred in setting up Selenium -> ", ex);
			System.out.println("Error occurred in setting up Selenium -> " + ex.getMessage());
		}
	}
	
	public void waitforElement(String element) throws Exception {

		WebElement ele = null;
		if (element != null) {
			try {
				int second;

				for (second = 0; second <= 40; second++) {

					ele = driver.findElement(By.xpath(element));					
					if (ele != null) {
						try {
							if (ele.isDisplayed()) {
								// if
								// (seleniumBean.getSelenium().isVisible(element))
								// {
								break;
							}
						} catch (Exception ex) {
							System.out.println("Selenium exception occurred during visiblity check. Error is - " + ex);
						}
					}
					boolean breakOuter = false;

					Thread.sleep(1000);
					if (breakOuter)
						break;
				}
			} catch (Exception e) {

				System.out.println("Error in finding element  - > " + e);
			}
		}
	}

	/**
	 * If you provide wait Than Wait for the page to load
	 * 
	 * @param driver
	 * @param totalWaitForSeconds
	 */
	void waitForPageLoad(int totalWaitForSeconds) {
		            
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				//configBean.getLogger().info("In waitForPageLoad 0");
				//System.out.println("In waitForPageLoad0");
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		
		//configBean.getLogger().info("In waitForPageLoad 1 = "+expectation);
		//System.out.println("In waitForPageLoad 1 = "+expectation);
		 //WebDriverWait wait = new WebDriverWait(driver, totalWaitForSeconds);
		//Wait<WebDriver> wait = new WebDriverWait(driver, totalWaitForSeconds);
		try {
			//long initMilliSec = System.currentTimeMillis();
			//configBean.getLogger().info("In waitForPageLoad 2 = "+initMilliSec);
			//System.out.println("In waitForPageLoad2 = "+initMilliSec);
			Thread.sleep(1000);
            //wait.until(expectation);
			
			//configBean.getLogger().info("" + (System.currentTimeMillis() - initMilliSec));
		} catch (Throwable error) {
			//configBean.getLogger().error("Timeout waiting for Page Load Request to complete.");
			System.out.println("Timeout waiting for Page Load Request to complete.");
		}
	}
	
	void stopSelenium() {
		if (driver != null)
			driver.quit();
		driver = null;
		try{
			Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
		}catch(Exception ex) {System.out.println("Error in killing chromedriver.exe"+ex);}		
	}
	
}
