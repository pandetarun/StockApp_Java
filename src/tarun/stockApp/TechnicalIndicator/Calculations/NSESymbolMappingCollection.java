package tarun.stockApp.TechnicalIndicator.Calculations;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import tarun.stockApp.TechnicalIndicator.Data.StockDetails;

public class NSESymbolMappingCollection extends SetupBase {
	
	final String URL = "http://www.business-standard.com";
	ArrayList<StockDetails> stockDetailsArray;
	ArrayList<String> storedStockNames;
	ArrayList<String> stocklistFromFile;
	StockDetails stockDetailsObj;
	//ArrayList<String> stocklistFromDB;
	Connection connection = null;
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		NSESymbolMappingCollection obj = new NSESymbolMappingCollection();
		obj.startCollectingNSEMappingData();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}
	
	public void startCollectingNSEMappingData() {
		try {
			if(ReadFileandCreateStockList()) {
				if(getStoredStockName()) {
					connection = StockUtils.connectToDB();
					while (!CollectionUtils.isEqualCollection(stocklistFromFile, storedStockNames)) {
						setupSelenium(URL);
						readFileandGetNSEMapping();			
						stopSelenium();
						getStoredStockName();
					}
				}
			}			
		} catch (Exception e) {
			System.out.println("Error occurred in getting quote -> " + e.getMessage());
		}
	}
	
	boolean ReadFileandCreateStockList() {
		BufferedReader br = null;
		stocklistFromFile = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader("C:/Selenium/AllStocks.txt"));
			String line = br.readLine();
			while (line != null) {
				stocklistFromFile.add(line);
		    	line = br.readLine();
		    }
			br.close();
			return true;
		} catch(Exception ex) {
			System.out.println("Error in reading stock list from file -> " + ex);
			return false;
		}
	}
	
	void readFileandGetNSEMapping() { 
		try {
		    waitForPageLoad(3000);
			
			WebElement ele = driver.findElement(By.id("select_type"));
			Select select= new Select(ele);
			select.selectByVisibleText("Stock Quote");
			stockDetailsArray = new ArrayList<StockDetails>();
			//getStoredStockName();
			for (String line :  stocklistFromFile) {				
				if(!storedStockNames.contains(line)){
	    			getNSEMapings(line);
		    	}
			}			
	        //storeQuotestoDB();
		} catch(Exception ex) {
			System.out.println("Error in reading and storing file -> " + ex);
			//storeQuotestoDB();
		}
	}
	
	void getNSEMapings (String quoteName) {
		WebElement ele = null;
		//Actions actions = new Actions(driver);
		String nseSymbol;
		//String xpathe;
		
		stockDetailsObj = new StockDetails();
		System.out.println("working for  -> "+quoteName);
		//waitForPageLoad(5000);
		
		stockDetailsObj.stockName = quoteName;
		ele = driver.findElement(By.id("head-suggest"));
		ele.sendKeys(quoteName);
		ele = driver.findElement(By.id("search_btn"));
		ele.click();
		waitForPageLoad(3000);
		
		for(int i=2;;i++) {
			try {
				//xpathe = "//*[@id='hpcontentbox']/div[6]/div[1]/div[1]/div[4]/div/table/tbody/tr[" + i + "]/td[1]/a";
				//System.out.println("xpath -> " + xpathe);
				ele = driver.findElement(By.xpath("//*[@id='hpcontentbox']/div[6]/div[1]/div[1]/div[4]/div/table/tbody/tr[" + i + "]/td[1]/a"));
			} catch(Exception ex) {				
				System.out.println("Error in waiting for drop down suggestion");
				break;
			}
			nseSymbol = ele.getText();
			if (nseSymbol.equalsIgnoreCase(quoteName)) {
				ele.click();
				break;
			}				
		}
		waitForPageLoad(3000);
		ele = driver.findElement(By.xpath("//*[@id='hpcontentbox']/div[5]/div/div[1]/table/tbody/tr[2]/td[1]"));
		nseSymbol = ele.getText().substring(5);
		stockDetailsObj.nseSymbol = ele.getText().substring(5);;
		System.out.println("NSE Symbol -> "+ele.getText().substring(4));
		
		ele = driver.findElement(By.xpath("//*[@id='hpcontentbox']/div[5]/div/div[1]/table/tbody/tr[2]/td[2]"));
		//isinCode = ele.getText().substring(11);
		stockDetailsObj.isinCode = ele.getText().substring(11);
		ele = driver.findElement(By.xpath("//*[@id='hpcontentbox']/div[5]/div/div[1]/table/tbody/tr[1]/td[2]"));
		//sector = ele.getText().substring(8);
		stockDetailsObj.sector = ele.getText().substring(8);
	
		ele = driver.findElement(By.xpath("//*[@id='hpcontentbox']/div[5]/div/div[1]/table/tbody/tr[1]/td[1]"));
		stockDetailsObj.BSECode = ele.getText().substring(5);
		storeIndividualQuotestoDB();
		stockDetailsArray.add(stockDetailsObj);
		//return nseSymbol + "\t" + isinCode + "\t" + sector;
	}
	
	private void storeQuotestoDB() {
		Connection connection = null;
        Statement statement = null; 
        String tmpsql;
        try {
        	Date dateObj = new Date();
    		System.out.println("DB entry started -> " + dateObj.toString());
    		connection = StockUtils.connectToDB();
        	statement = connection.createStatement();
        	for (StockDetails stockDetails : stockDetailsArray) {
        		tmpsql = "INSERT INTO STOCKDETAILS (STOCKNAME, NSECODE, BSECODE, ISINCODE, SECTOR) VALUES('" + 
        				stockDetails.stockName + "','" + stockDetails.nseSymbol + "','" + stockDetails.BSECode + "','" + 
        				stockDetails.isinCode + "','" + stockDetails.sector + "');";
        		statement.executeUpdate(tmpsql);
        	}
        	dateObj = new Date();
    		System.out.println("DB entry completed -> " + dateObj.toString());
        } catch(Exception ex){
        	System.out.println("Error in DB action");
        }
	}
	
	private void storeIndividualQuotestoDB() {
		
        //ResultSet resultSet = null;
        Statement statement = null; 
        String tmpsql;
        try {     
        	
        	Date dateObj = new Date();
        	//Date yesterday = new Date(dateObj.getTime() - (1000*60*60*24));
    		System.out.println("DB entry started -> " + dateObj.toString());
        	//DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        	
        	statement = connection.createStatement();
        	//for (StockDetails stockDetails : stockDetailsArray) {
        		tmpsql = "INSERT INTO STOCKDETAILS (STOCKNAME, NSECODE, BSECODE, ISINCODE, SECTOR) VALUES('" + 
        				stockDetailsObj.stockName + "','" + stockDetailsObj.nseSymbol + "','" + stockDetailsObj.BSECode + "','" + 
        				stockDetailsObj.isinCode + "','" + stockDetailsObj.sector + "');";
        		statement.executeUpdate(tmpsql);
        	//}
        	dateObj = new Date();
    		System.out.println("DB entry completed -> " + dateObj.toString());
        } catch(Exception ex){
        	System.out.println("Error in DB action" + ex);
        }
	}
	
	private boolean getStoredStockName() {
		Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null; 
        String stockName;
        try {     
        	storedStockNames = new ArrayList<String>();
        	connection = StockUtils.connectToDB();
        	statement = connection.createStatement();        	
        	resultSet = statement.executeQuery("SELECT STOCKNAME FROM STOCKDETAILS;");
        	while (resultSet.next()) {
        		stockName = resultSet.getString(1);
        		storedStockNames.add(stockName);
        	    //System.out.println("StockNme - " + stockName);
        	}
        	return true;
        } catch(Exception ex){
        	System.out.println("Error in DB action");
        	return false;
        }
	}	
}
