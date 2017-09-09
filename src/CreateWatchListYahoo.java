import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

public class CreateWatchListYahoo extends SetupBase {
	
	static final String URL = "https://in.finance.yahoo.com/";
	static final String USER_NAME = "pandey.tarun@yahoo.com";
	static final String PASSWORD = "Jan@2017";
	
	static Logger logger = Logger.getLogger(CollectDailyStockData.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CreateWatchListYahoo Started");
		CreateWatchListYahoo obj = new CreateWatchListYahoo();
		
		
		obj.startCreatingWatchList(true);
		dte = new Date();
		System.out.println("CreateWatchListYahoo End at -> " + dte.toString());
	}
	
	public void startCreatingWatchList(boolean belowHunderd) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String portfolioName;
		
		if(!belowHunderd){
			portfolioName = dateFormat.format(new Date()) + " All";
		} else {
			portfolioName = dateFormat.format(new Date()) + " Below 100";
		}
		creatWatchList(portfolioName, belowHunderd);
		addStocksToWatchList("GNA");
		addStocksToWatchList("LUMAXTECH");
	}
	
	public void creatWatchList(String watchListName, boolean belowHunderd) {
		WebElement ele = null;		
		Actions actions = null;
		String portfolioName;
		try{
			logger.debug("startCollectingDailyData Started");
			setupSelenium(URL);
			logger.debug("Selenium Setup Completed");
			try {
				Thread.sleep(4000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			actions = new Actions(driver);
			//click on sign in
			ele = driver.findElement(By.id("uh-signedin"));			
			ele.click();
			try {
				Thread.sleep(4000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			//Uncheck persitent checkbox
			//ele = driver.findElement(By.id("persistent"));
			ele = driver.findElement(By.xpath("//*[@id='persistent']"));
			
			//ele.click();
			
			//enter user name
			ele = driver.findElement(By.id("login-username"));
			ele.sendKeys(USER_NAME);
			//next button
			ele = driver.findElement(By.id("login-signin"));
			ele.click();
			try {
				Thread.sleep(2000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			//enter password
			ele = driver.findElement(By.id("login-passwd"));
			ele.sendKeys(PASSWORD);
			//Sign in button
			ele = driver.findElement(By.id("login-signin"));
			ele.click();
			try {
				Thread.sleep(5000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			//click on my portfolio
			ele = driver.findElement(By.xpath("//*[@id='Nav-0-DesktopNav']/div/div[3]/div/div[1]/ul/li[9]/a"));
			ele.click();
			//actions.clickAndHold(ele).perform();
			try {
				Thread.sleep(4000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			ele = driver.findElement(By.xpath("//*[@id='main']/section/header/div[1]/div/button"));
			ele.click();
			try {
				Thread.sleep(2000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			//Provide portfolio Name			
			ele = driver.findElement(By.xpath("//*[@id='__dialog']/section/form/div[1]/input"));
			ele.sendKeys(watchListName);
			//Select Currency
			ele = driver.findElement(By.xpath("//*[@id='__dialog']/section/form/div[1]/label/select"));
			Select select= new Select(ele);		
			select.selectByVisibleText("Indian Rupee (INR)");
			
			//Put to fix the error Yahoo is creating at the moment
			//click on create watchlist
			ele = driver.findElement(By.xpath("//*[@id='main']/section/header/div[1]/div/button"));
			ele.click();
			try {
				Thread.sleep(1000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			
			//Put to fix the error Yahoo is creating at the moment
			
			//Click on save button
			ele = driver.findElement(By.xpath("//*[@id='__dialog']/section/form/div[2]/button[1]"));
			ele.click();
			try {
				Thread.sleep(4000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
			//Close help popup
			ele = driver.findElement(By.xpath("//*[@id='feature-tour']/div/div/button"));
			ele.click();			
		} catch (Exception ex) {
			System.out.println("Error in creatWatchList "+ex);
			logger.error("Error in creatWatchList - > "+ex);
		}	
	}
	
	public void addStocksToWatchList(String stockCode) {
		
		WebElement ele = null;	
		//Click on add symbol button
		ele = driver.findElement(By.xpath("//*[@id='main']/section/div/button[1]"));
		ele.click();
		try {
			Thread.sleep(1000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		//Provide stock code
		ele = driver.findElement(By.xpath("//*[@id='__dialog']/section/div/form/div/div[1]/input"));
		ele.sendKeys(stockCode);
		try {
			Thread.sleep(2000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		//Find equity code
		for(int counter = 1; counter <= 40; counter++) {
			ele = driver.findElement(By.xpath("//*[@id='react-autowhatever-1']/ul/li[" + counter + "]/div/p[2]"));
			if(ele.getText().contains("NSE")) {
				//Click on equity code
				ele = driver.findElement(By.xpath("//*[@id='react-autowhatever-1']/ul/li[" + counter + "]"));
				ele.click();
				break;
			}
		}		
		
		//Click main to close quote dialog
//		ele = driver.findElement(By.id("main"));
//		ele.click();
		try {
			Thread.sleep(2000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		//close popuup
		ele = driver.findElement(By.xpath("//*[@id='__dialog']/section/button"));
		ele.click();
		try {
			Thread.sleep(1000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		
	}
}
