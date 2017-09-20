package tarun.stockApp.TechnicalIndicator.Calculations;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;

import tarun.stockApp.TechnicalIndicator.Data.QuotesData;

public class CollectData extends SetupBase {

	WebDriver driver = null;
	final String URL = "http://www.business-standard.com/user/login";
	final String timeOut = "2000";
	ArrayList<QuotesData> quoteArray;
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		CollectData obj = new CollectData();
		obj.startCollectingData();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}

	public void startCollectingData() {
		try {
			System.out.println("Program Start -> " + new Date().toString());
			setupSelenium(URL);
			performLoginSteps();
			getStockQuotes();
			storeQuotestoDB();
			stopSelenium();
		} catch (Exception e) {
			System.out.println("Error occurred in getting quote -> " + e.getMessage());
		}
	}
	
	private void performLoginSteps() {
		// Actions actions = new Actions(driver);
		WebElement ele = null;
		// By by = null;
		waitForPageLoad(10000);
		// System.out.println(driver.getPageSource());
		// Pass userName
		ele = driver.findElement(By.xpath("//*[@id='loginEmail']"));
		ele.sendKeys("pandetarun@gmail.com");
		// Pass password
		ele = driver.findElement(By.xpath("//*[@id='loginPassword']"));
		ele.sendKeys("coming@12");

		// Click Login
		ele = driver.findElement(By.xpath("//*[@id='sign_in_a']"));
		ele.click();
	}

	private void getStockQuotes() throws Exception {

		//PrintWriter pw = new PrintWriter(new File("D:/Tarun/StockApp_Latest/Java/Quotes.csv"));
		//StringBuilder sb = new StringBuilder();
		WebElement ele = null;
		// Move to Portfolio
		// System.out.println("Starting with portfolio");
		// waitforElement("//*[@id='nav_menu_227']");
		waitForPageLoad(10000);
		// System.out.println("wait for portfolio over");
		ele = driver.findElement(By.xpath("//*[@id='nav_menu_227']"));
		ele.click();
		// System.out.println("portfolio clicked");
		Thread.sleep(8000);
		// Loop through all companies and store quotes
		Date dte = new Date();
		// System.out.println("Start at -> " + dte.toString());
		//sb.append(dte.toString() + "\n");
		quoteArray = new ArrayList<QuotesData>();
		QuotesData tmpObj;
		System.out.println("Row reading started -> " + dte.toString());
		for (int rows = 3;; rows++) {
			try {
				ele = driver
						.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[1]/a"));
			} catch (Exception ex) {
				ele = null;
			}
			if (ele == null)
				break;
			// System.out.println("StockName = " + ele.getText());
			tmpObj = new QuotesData();
			tmpObj.stockName = ele.getText();
			//sb.append(ele.getText() + "\t");
			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[3]"));
			// System.out.print(" LastPrice = " + ele.getText());
			tmpObj.closingPrice = Float.parseFloat(ele.getText());
			
			//sb.append(ele.getText() + "\t");
			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[4]"));
			// System.out.print(" Change = " + ele.getText());
			tmpObj.changeInPrice = Float.parseFloat(ele.getText().substring(0, ele.getText().indexOf("(")));
			tmpObj.changeInPercentage = Float.parseFloat(ele.getText().substring(ele.getText().indexOf("(")+1, ele.getText().indexOf(")")-1));
			//sb.append(ele.getText() + "\t");
			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[5]"));
			// System.out.print(" Vol = " + ele.getText());
			tmpObj.volume = Long.parseLong(ele.getText());
			//sb.append(ele.getText() + "\t");

			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[6]"));
			// System.out.print(" IntraDayHigh = " + ele.getText());
			tmpObj.dailyHigh = Float.parseFloat(ele.getText());
			//sb.append(ele.getText() + "\t");

			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[7]"));
			// System.out.print(" IntraDayow = " + ele.getText());
			tmpObj.dailyLow = Float.parseFloat(ele.getText());
			//sb.append(ele.getText() + "\t");
			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[8]"));
			// System.out.print(" 52WeekHigh = " + ele.getText());
			tmpObj.yearlyHigh= Float.parseFloat(ele.getText());
			//sb.append(ele.getText() + "\t");
			ele = driver.findElement(By.xpath("//*[@id='loadWatchlist']/div[1]/table/tbody/tr[" + rows + "]/td[9]"));
			// System.out.println(" 52WeekLow = " + ele.getText());
			tmpObj.yearlyLow = Float.parseFloat(ele.getText());
			//sb.append(ele.getText() + "\n");
			quoteArray.add(tmpObj);
		}
		dte = new Date();
		System.out.println("Row reading completed -> " + dte.toString());
//		sb.append(dte.toString() + "\n");
//		pw.write(sb.toString());
//		pw.close();
	}

	
	private void storeQuotestoDB() {
		Connection connection = null;
        //ResultSet resultSet = null;
        Statement statement = null; 
        String tmpsql;
        try {     
        	
        	Date dateObj = new Date();
        	//Date yesterday = new Date(dateObj.getTime() - (1000*60*60*24));
    		System.out.println("DB entry started -> " + dateObj.toString());
        	DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        	Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance(); 
        	connection=DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8","SYSDBA","Jan@2017");
        	statement = connection.createStatement();
        	for (QuotesData tmpQuotesData : quoteArray) {
        		tmpsql = "INSERT INTO DAILYSTOCKDATA (STOCKNAME, CLOSEPRICE, HIGHPRICE, LOWPRICE, CHANGE_PERCENTAGE, VOLUME, YEARLY_HIGH, YEARLY_LOW, TRADEDDATE, CHANGEINPRICE) VALUES('" + 
                		tmpQuotesData.stockName + "'," + tmpQuotesData.closingPrice + "," + tmpQuotesData.dailyHigh + "," + 
                		tmpQuotesData.dailyLow + "," + tmpQuotesData.changeInPercentage + "," + tmpQuotesData.volume + "," + tmpQuotesData.yearlyHigh + "," + 
                		tmpQuotesData.yearlyLow + ",'" + dateFormat.format(dateObj) + "'," + tmpQuotesData.changeInPrice + ");";
        		statement.executeUpdate(tmpsql);
        	}
        	dateObj = new Date();
    		System.out.println("DB entry completed -> " + dateObj.toString());
//        	resultSet = statement
//                    .executeQuery("INSERT INTO DAILYSTOCKDATA (STOCKNAME, CLOSEPRICE, HIGHPRICE, LOWPRICE, CHANGE_PERCENTAGE, VOLUME, YEARLY_HIGH, YEARLY_LOW, TRADEDDATE) VALUES('test',12 ,12 12,12,12,12, 12, 12,'30-May-2017');");
        	
        } catch(Exception ex){
        	System.out.println("Error in DB action");
        }
	}
}
