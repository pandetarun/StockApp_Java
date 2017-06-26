import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import org.apache.commons.io.FileUtils;
import org.jsoup.parser.Parser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class CollectDailyHistory extends SetupBase {
	
	final String URL = "https://www.nseindia.com/products/content/equities/equities/eq_security.htm";
	final String timeOut = "2000";
	ArrayList<String> storedStockNSECode;
	QuotesData quotesDataObj;
	Connection connection = null;
	int i =1;
	String lastDate = "6-Jun-2017";
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		CollectDailyHistory obj = new CollectDailyHistory();
		obj.startCollectingHistoryData();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}

	public void startCollectingHistoryData() {
		WebElement ele = null;
		String enteredStockLastDate;
		
		
		try {
			if(unProcessedStocks()) {
				setupSelenium(URL);				
				waitForPageLoad(3000);
				ele = driver.findElement(By.id("dateRange"));				
				Select select= new Select(ele);				
				select.selectByVisibleText("24 Months");
				FileUtils.cleanDirectory(new File(downloadFilepath)); 
				for (String stockNSECode : storedStockNSECode) {
					enteredStockLastDate = getEneteredStockData(stockNSECode);
					if( enteredStockLastDate == null) {
						getHistoryData(stockNSECode);						
						readFileAndPopulateStockQuote(null);					
						
					} else {
						if(enteredStockLastDate!=null) {
							getHistoryData(stockNSECode);						
							readFileAndPopulateStockQuote(enteredStockLastDate);					
							System.out.println("called for Date");
						}
					}
				}
				
				stopSelenium();
			}
			
		} catch (Exception e) {
			System.out.println("Error occurred in getting quote -> " + e.getMessage());
		}
	}

	private void getHistoryData (String stockNSECode) {
		
		WebElement ele = null;		
		ele = driver.findElement(By.id("symbol"));
		ele.clear();
		ele.sendKeys(stockNSECode);
		ele = driver.findElement(By.id("get"));
		ele.click();		
		waitForPageLoad(3000);
		try {
			Thread.sleep(5000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		
		ele = driver.findElement(By.xpath("//*[@id='historicalData']/div[1]/span[2]/a"));
		ele.click();
		try {
			Thread.sleep(5000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
	}
	
	private void readFileAndPopulateStockQuote(String enteredStockLastDate) {
		BufferedReader br = null;
		String[] stockData = null;
		File inputFileForException = null;
		
		try {
			quotesDataObj = new QuotesData();
			File inputFolder = new File(downloadFilepath);			
			File[] inputFileList = inputFolder.listFiles();			
			Date eneterdDate = null;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			if (enteredStockLastDate!=null)
				eneterdDate = dateFormat.parse(enteredStockLastDate);
			for (File inputFile : inputFileList) {
				if (inputFile.isFile()) {
					inputFileForException= inputFile;
					br = new BufferedReader(new FileReader(inputFile));
					String line = br.readLine();
					line = br.readLine();
					while (line != null) {
						//stocklistFromFile.add(line);												
						stockData = line.split(",");
						
						//System.out.println("Line read at " + i++  + " Date -> "+stockData[2]);
						if((stockData[1].substring(1, stockData[1].length()-1).trim().equalsIgnoreCase("EQ") || stockData[1].substring(1, stockData[1].length()-1).trim().equalsIgnoreCase("BE")) && enteredStockLastDate == null) {
							quotesDataObj.stockName = Parser.unescapeEntities(stockData[0].substring(1, stockData[0].length()-1).trim(), false);						
							quotesDataObj.quoteDate = stockData[2].substring(1, stockData[2].length()-1).trim();
							quotesDataObj.openPrice = Float.parseFloat(stockData[4].substring(1, stockData[4].length()-1).trim());
							quotesDataObj.dailyHigh = Float.parseFloat(stockData[5].substring(1, stockData[5].length()-1).trim());
							quotesDataObj.dailyLow = Float.parseFloat(stockData[6].substring(1, stockData[6].length()-1).trim());
							quotesDataObj.closingPrice = Float.parseFloat(stockData[8].substring(1, stockData[8].length()-1).trim());						
							quotesDataObj.volume = Long.parseLong(stockData[10].substring(1, stockData[10].length()-1).trim());						
							
							storeQuotestoDB();
						} else if (enteredStockLastDate != null && eneterdDate.getTime()<Date.parse(stockData[2].substring(1, stockData[2].length()-1).trim()) && (stockData[1].substring(1, stockData[1].length()-1).trim().equalsIgnoreCase("EQ") || stockData[1].substring(1, stockData[1].length()-1).trim().equalsIgnoreCase("BE"))) {
							quotesDataObj.stockName = stockData[0].substring(1, stockData[0].length()-1).trim();						
							quotesDataObj.quoteDate = stockData[2].substring(1, stockData[2].length()-1).trim();
							quotesDataObj.openPrice = Float.parseFloat(stockData[4].substring(1, stockData[4].length()-1).trim());
							quotesDataObj.dailyHigh = Float.parseFloat(stockData[5].substring(1, stockData[5].length()-1).trim());
							quotesDataObj.dailyLow = Float.parseFloat(stockData[6].substring(1, stockData[6].length()-1).trim());
							quotesDataObj.closingPrice = Float.parseFloat(stockData[8].substring(1, stockData[8].length()-1).trim());						
							quotesDataObj.volume = Long.parseLong(stockData[10].substring(1, stockData[10].length()-1).trim());	
							
							storeQuotestoDB();							
						}
						line = br.readLine();
						if(line==null && quotesDataObj.stockName!=null){
							enterhandledStock();
						}
				    }
					if (enteredStockLastDate != null) {
						System.out.println(i + " - called Date specific for stock -> " + quotesDataObj.stockName);
					} else {
						System.out.println(i + " - called All for stock -> " + quotesDataObj.stockName);
					}
					i++;
					br.close();
					Files.move(Paths.get(inputFile.getAbsolutePath()), Paths.get("C:/Selenium/downlodProcessed/" + inputFile.getName()));
					//inputFile.renameTo(new File("C:/Selenium/downlodProcessed"));
				}
			}			
		} catch (FileAlreadyExistsException exx) {
			System.out.println("File already in destination. Deleting");
			inputFileForException.delete();
		}		
		catch(Exception ex) {
			System.out.println("readFileAndPopulateStockQuote for quote -> " + quotesDataObj.stockName + "  and Date -> " + quotesDataObj.quoteDate + " Error -> " + ex.getMessage());
			enterhandledStock();
			try {
				Files.move(Paths.get(inputFileForException.getAbsolutePath()), Paths.get("C:/Selenium/downlodProcessed/" + inputFileForException.getName()));
			} catch (Exception exx) {
				System.out.println("Error in moving file after exception");
			}
			
			//return false;
		}
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
        }
	}
	
	private void enterhandledStock() {
		Statement statement = null; 
        String tmpsql;
        try {
        	statement = connection.createStatement();
        	tmpsql = "INSERT INTO ENTEREDSTOCKDETAILS (STOCKNAME, LASTDATE) VALUES('" + 
        				quotesDataObj.stockName + "','" + quotesDataObj.quoteDate + "');";
        	statement.executeUpdate(tmpsql);
        } catch(Exception ex){
        	System.out.println("enterhandledStock for quote -> " + quotesDataObj.stockName + " and Date - > " + quotesDataObj.quoteDate + " Error in DB action"+ex);
        }
	}
	
	private boolean unProcessedStocks() {
		ResultSet resultSet = null;
		Statement statement = null; 
        String tmpsql;
        String stockNSECode;
        try {
        	storedStockNSECode = new ArrayList<String>();
        	Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance(); 
        	connection=DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8","SYSDBA","Jan@2017");
        	statement = connection.createStatement();        	
        	//tmpsql = "select LASTDATE from ENTEREDSTOCKDETAILS where STOCKNAME = '"+ stockCode + "';";
        	
        	tmpsql = "SELECT First 70 sd.NSECODE FROM STOCKDETAILS sd LEFT JOIN ENTEREDSTOCKDETAILS esd ON sd.NSECODE = esd.STOCKNAME WHERE esd.STOCKNAME IS NULL Or esd.LASTDATE < '" + lastDate + "';";
        		
        	resultSet = statement.executeQuery(tmpsql);
        	while (resultSet.next()) {
        		stockNSECode = resultSet.getString(1);
        		storedStockNSECode.add(stockNSECode);
        	    //System.out.println("StockNme - " + stockNSECode);
        	}
//        	if ((resultSet.next())) {
//        		return resultSet.getString(1);
//        	} else
//        		return null;
        } catch(Exception ex){
        	System.out.println("unProcessedStocks Error in DB action"+ex);
        	return false;
        }
        return true;
	}
	
	private String getEneteredStockData (String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null; 
        String tmpsql;
        String stockNSECode;
        try {
        	statement = connection.createStatement();        	
        	tmpsql = "select LASTDATE from ENTEREDSTOCKDETAILS where STOCKNAME = '"+ stockCode + "' and LASTDATE < '5-Jun-2017';";
        	
        	//tmpsql = "SELECT First 100 sd.NSECODE FROM STOCKDETAILS sd LEFT JOIN ENTEREDSTOCKDETAILS esd ON sd.NSECODE = esd.STOCKNAME WHERE esd.STOCKNAME IS NULL Or esd.LASTDATE < '5-Jun-2017';";
        			
        	
        	resultSet = statement.executeQuery(tmpsql);
        	
        	if ((resultSet.next())) {
        		return resultSet.getString(1);
        	} else
        		return null;
        } catch(Exception ex){
        	System.out.println(" getEneteredStockData Error in DB action"+ex);
        	return null;
        }
	}

}
