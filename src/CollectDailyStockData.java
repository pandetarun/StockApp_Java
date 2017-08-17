import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.jsoup.parser.Parser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class CollectDailyStockData extends SetupBase {
	final String URL = "https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm";
	final String timeOut = "2000";
	ArrayList<String> storedStockNSECode;
	QuotesData quotesDataObj;
	Connection connection = null;
	static Logger logger = Logger.getLogger(CollectDailyStockData.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CollectDailyStockData Started");
		CollectDailyStockData obj = new CollectDailyStockData();
		
		obj.startCollectingDailyData();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}
	
	public void startCollectingDailyData() {
		BufferedReader br = null;
		String[] stockData = null;
		File inputFileForDeletion = null;
		try{
			logger.debug("startCollectingDailyData Started");
			setupSelenium(URL);
			logger.debug("Selenium Setup Completed");
			getDailyDataFile();
			
			stopSelenium();
			getStoredStockNSECode();
			
			File inputFolder = new File(downloadFilepath);			
			File[] inputFileList = inputFolder.listFiles();	
			for (File inputFile : inputFileList) {
				if (inputFile.getName().contains(".zip")) {
					ZipFile zipFile = new ZipFile(inputFile);
					inputFileForDeletion = inputFile;
				    Enumeration<? extends ZipEntry> entries = zipFile.entries();

				    while(entries.hasMoreElements()){
				        ZipEntry entry = entries.nextElement();
				        InputStream stream = zipFile.getInputStream(entry);
				        br = new BufferedReader(new InputStreamReader(stream));
				        String line = br.readLine();
						line = br.readLine();
						while (line != null) {
							stockData = line.split(",");
							quotesDataObj = new QuotesData();
							//quotesDataObj.stockName = Parser.unescapeEntities(stockData[0].substring(1, stockData[0].length()-1).trim(), false);						
							quotesDataObj.stockName = Parser.unescapeEntities(stockData[0].trim(), false);
							//quotesDataObj.openPrice = Float.parseFloat(stockData[2].substring(1, stockData[2].length()-1).trim());
							quotesDataObj.openPrice = Float.parseFloat(stockData[2].trim());
							//quotesDataObj.dailyHigh = Float.parseFloat(stockData[3].substring(1, stockData[3].length()-1).trim());
							quotesDataObj.dailyHigh = Float.parseFloat(stockData[3].trim());
							//quotesDataObj.dailyLow = Float.parseFloat(stockData[4].substring(1, stockData[4].length()-1).trim());
							quotesDataObj.dailyLow = Float.parseFloat(stockData[4].trim());							
							//quotesDataObj.closingPrice = Float.parseFloat(stockData[5].substring(1, stockData[5].length()-1).trim());						
							quotesDataObj.closingPrice = Float.parseFloat(stockData[5].trim());
							//quotesDataObj.volume = Long.parseLong(stockData[8].substring(1, stockData[8].length()-1).trim());
							quotesDataObj.volume = Long.parseLong(stockData[8].trim());
							//quotesDataObj.quoteDate = stockData[10].substring(1, stockData[10].length()-1).trim();
							quotesDataObj.quoteDate = stockData[10].trim();
							storeQuotestoDB();
							line = br.readLine();
						}
				        br.close();
				        stream.close();
				    }
				    zipFile.close();
				    Files.move(Paths.get(inputFile.getAbsolutePath()), Paths.get("C:/Selenium/downlodProcessed/" + inputFile.getName()));
				    logger.debug("startCollectingDailyData End");			    
				}
			}			
		} catch (Exception ex) {
			System.out.println("Error in reading zip fil "+ex);
			logger.error("Error in startCollectingDailyData - > "+ex);
			inputFileForDeletion.delete();	
		}	
	}
	
	private void getDailyDataFile () {		
		logger.debug("getDailyDataFile Started");
		WebElement ele = null;		
		ele = driver.findElement(By.id("h_filetype"));
		Select select= new Select(ele);		
		select.selectByVisibleText("Bhavcopy");
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); 
		//Date date = new Date(System.currentTimeMillis()-1*24*60*60*1000);
		Date date = new Date(); //Date(System.currentTimeMillis()-24*60*60*1000);
		ele = driver.findElement(By.id("date"));
		ele.clear();
		ele.sendKeys(dateFormat.format(date));
		ele = driver.findElement(By.xpath("//*[@id='wrapper_btm']/div[1]/div[4]/div/div[1]/div/div[3]"));
		ele.click();
		try {
			Thread.sleep(3000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		ele = driver.findElement(By.xpath("//*[@id='wrapper_btm']/div[1]/div[4]/div/div[1]/div/div[4]/input[3]"));
		ele.click();
		waitForPageLoad(5000);
		ele = driver.findElement(By.xpath("//*[@id='spanDisplayBox']/table/tbody/tr/td/a"));
		ele.click();			
		try {
			Thread.sleep(7000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		logger.debug("getDailyDataFile End");
	}
	
	private boolean getStoredStockNSECode() {
		//Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null; 
        String stockNSECode;
        try {     
        	storedStockNSECode = new ArrayList<String>();
        	Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance(); 
        	connection=DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8","SYSDBA","Jan@2017");
        	statement = connection.createStatement();
        	
        	resultSet = statement.executeQuery("SELECT NSECODE FROM STOCKDETAILS;");
        	while (resultSet.next()) {
        		stockNSECode = resultSet.getString(1);
        		storedStockNSECode.add(stockNSECode);
        	    //System.out.println("StockNme - " + stockNSECode);
        	}
        	return true;
        } catch(Exception ex){
        	System.out.println("Error in DB action"+ex);
        	logger.error("Error in getStoredStockNSECode -> ", ex);
        	return false;
        }
	}
	
	private void storeQuotestoDB() {
        Statement statement = null; 
        String tmpsql;
        try {
        	statement = connection.createStatement();
        	tmpsql = "INSERT INTO DAILYSTOCKDATA (STOCKNAME, CLOSEPRICE, HIGHPRICE, LOWPRICE, OpenPrice, VOLUME, TRADEDDATE) VALUES('" + 
        				quotesDataObj.stockName + "'," + quotesDataObj.closingPrice + "," + quotesDataObj.dailyHigh + "," + 
        				quotesDataObj.dailyLow + "," + quotesDataObj.openPrice + "," + quotesDataObj.volume + ",'" + quotesDataObj.quoteDate + "');";
        	statement.executeUpdate(tmpsql);
        } catch(Exception ex){
        	System.out.println("storeQuotestoDB for quote -> " + quotesDataObj.stockName + " and Date - > " + quotesDataObj.quoteDate + " Error in DB action"+ex);
        	logger.error("Error in storeQuotestoDB -> ", ex);
        }
	}
}
