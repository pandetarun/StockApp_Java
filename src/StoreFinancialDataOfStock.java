import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class StoreFinancialDataOfStock extends SetupBase {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateOnBalanceVolume.class);	
	final String URL = "https://www.screener.in";
	final String timeOut = "2000";
	boolean standaloneclick = false;
	public final static String CONNECTION_STRING = "jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8";
	public final static String USER = "SYSDBA";
	public final static String PASS = "Jan@2017";
	String stockName;
	String bseCode;
	
	public static void main(String[] args) {
		StoreFinancialDataOfStock tmpStoreFinancialDataOfStock = new StoreFinancialDataOfStock();
		//tmpStoreFinancialDataOfStock.setupSelenium(tmpStoreFinancialDataOfStock.URL);
		//tmpStoreFinancialDataOfStock.getFiancialDataForStock("500355");
		tmpStoreFinancialDataOfStock.getFinancialData();
	}
	
	public void getFinancialData() {
		ArrayList<String> stockList = null;
		ArrayList<CompanyFinancialData> companyFinancialDataList = new ArrayList<CompanyFinancialData>();
		CompanyFinancialData companyFinancialDatatmp = null;
		stockList = getStockListFromDB();
		setupSelenium(URL);
		
		for (String stockCode : stockList) {
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			companyFinancialDatatmp = getFiancialDataForStock(stockCode.split("!")[0]);
			companyFinancialDataList.add(companyFinancialDatatmp);
		}
	}
	
	private CompanyFinancialData getFiancialDataForStock(String BSECode) {		
		logger.debug("getFiancialDataForStock Started");
		CompanyFinancialData companyFinancialDatatmp = new CompanyFinancialData();		
		WebElement ele = null;
		String screenData;
		
		ele = driver.findElement(By.xpath("//*[@id='content']/div/nav/div/div[1]/div/div/div/div/input"));
		ele.clear();
		ele.sendKeys(BSECode);
		//wait for drop down value to appear
		try {
			Thread.sleep(1000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		
		ele = driver.findElement(By.xpath("//*[@id='content']/div/nav/div/div[1]/div/div/div/div/ul/li/a"));
		ele.click();
		try {
			Thread.sleep(3000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		//Get Standalone financials
		if(!standaloneclick) {
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/h2/small/span/a"));
			ele.click();
			standaloneclick = true;
			try {
				Thread.sleep(1000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
		}		
		//Book Value
		ele = driver.findElement(By.xpath("//*[@id='content']/div/div/div/section[1]/div[1]/h4[3]/b"));
		screenData = ele.getText();
		companyFinancialDatatmp.bookValue = Double.parseDouble(screenData.substring(screenData.indexOf(" "), screenData.length()));
		
		//Face Value
		ele = driver.findElement(By.xpath("//*[@id='content']/div/div/div/section[1]/div[1]/h4[6]/b"));
		screenData = ele.getText();
		companyFinancialDatatmp.faceValue = Double.parseDouble(screenData.substring(screenData.indexOf(" "), screenData.length()));
		
		//Stock PE
		ele = driver.findElement(By.xpath("//*[@id='content']/div/div/div/section[1]/div[1]/h4[4]/b"));
		screenData = ele.getText();
		companyFinancialDatatmp.StockPE = Double.parseDouble(screenData);
		
		//Dividend Yield
		ele = driver.findElement(By.xpath("//*[@id='content']/div/div/div/section[1]/div[1]/h4[5]/b"));
		screenData = ele.getText();
		companyFinancialDatatmp.dividendYield = Double.parseDouble(screenData.substring(0, screenData.length()-1));
		
		//52 weeks high
		ele = driver.findElement(By.xpath("//*[@id='content']/div/div/div/section[1]/div[1]/h4[9]/b"));
		screenData = ele.getText();
		companyFinancialDatatmp.yearlyHigh = Double.parseDouble(screenData.substring(2, screenData.indexOf("/")-1));
		
		//52 weeks low		
		companyFinancialDatatmp.yearlyLow = Double.parseDouble(screenData.substring(screenData.indexOf("/")+3, screenData.length()));
		StoreCompanyFinancialDataToDB(companyFinancialDatatmp);
		//Quarter data
		companyFinancialDatatmp.companyQuarterlyFinancialDataList = CollectQuarterlyData();
		StoreQuarterDataToDB(companyFinancialDatatmp.companyQuarterlyFinancialDataList);
		//Annual data
		companyFinancialDatatmp.companyAnnualFinancialDataList = CollectAnnualData();	
		StoreAnnualDataToDB(companyFinancialDatatmp.companyAnnualFinancialDataList);
		logger.debug("getFiancialDataForStock End");
		return companyFinancialDatatmp;
	}
	
	private ArrayList<CompanyQuarterlyFinancialData> CollectQuarterlyData() {
		String screenData;
		CompanyQuarterlyFinancialData companyQuarterlyFinancialDatatmp = null;
		ArrayList<CompanyQuarterlyFinancialData> companyQuarterlyFinancialDatatmpList = new ArrayList<CompanyQuarterlyFinancialData>();
		WebElement ele = null;
		
		for (int quartercolumn = 2; quartercolumn<=13;quartercolumn++) {
			companyQuarterlyFinancialDatatmp = new CompanyQuarterlyFinancialData();
			//month and Year
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/thead/tr/th[" + quartercolumn + "]"));
			screenData = ele.getText();
			companyQuarterlyFinancialDatatmp.month = screenData.substring(0, screenData.indexOf(" "));
			companyQuarterlyFinancialDatatmp.year = Integer.parseInt(screenData.substring(screenData.indexOf(" ")+1));
			//quarterly sales
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[1]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.sales = Double.parseDouble(screenData);
			//quarterly expenses
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[2]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.expenses = Double.parseDouble(screenData);
			//quarterly operating profit
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[3]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.operatingProfit = Double.parseDouble(screenData);
			//quarterly operating profit margin
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[4]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.OPMargin = Double.parseDouble(screenData);
			//quarterly other income
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[5]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.otherIncome = Double.parseDouble(screenData);		
			//quarterly depreciation
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[6]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.depreciation = Double.parseDouble(screenData);				
			//quarterly interest
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[7]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.interest = Double.parseDouble(screenData);		
			//quarterly profit before tax
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[8]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.profitBeforeTax = Double.parseDouble(screenData);		
			//quarterly tax
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[9]/td[" + quartercolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.tax = Double.parseDouble(screenData);		
			//quarterly net profit
			ele = driver.findElement(By.xpath("//*[@id='quarters']/div/div/table/tbody/tr[10]/td[" + quartercolumn + "]"));										   
			screenData = ele.getText().replace(",", "");
			companyQuarterlyFinancialDatatmp.netProfit = Double.parseDouble(screenData);
			companyQuarterlyFinancialDatatmpList.add(companyQuarterlyFinancialDatatmp);
		}
		return companyQuarterlyFinancialDatatmpList;
	}
	
	private ArrayList<CompanyAnnualFinancialData> CollectAnnualData() {
		ArrayList<CompanyAnnualFinancialData> companyAnnualFinancialDatatmpList = new ArrayList<CompanyAnnualFinancialData>();
		String screenData;
		CompanyAnnualFinancialData companyAnnualFinancialDatatmp = null;		
		WebElement ele = null;
		
		for (int annualcolumn = 2; annualcolumn<=13;annualcolumn++) {
			
			companyAnnualFinancialDatatmp = new CompanyAnnualFinancialData();					
			//month and Year
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/thead/tr/th[" + annualcolumn + "]"));
			screenData = ele.getText();
			companyAnnualFinancialDatatmp.month = screenData.substring(0, screenData.indexOf(" "));
			companyAnnualFinancialDatatmp.year = Integer.parseInt(screenData.substring(screenData.indexOf(" ")+1));
			//Annual sales
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[1]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.sales = Double.parseDouble(screenData);					
			//Annual expenses
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[2]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.expenses = Double.parseDouble(screenData);
			//Annual operating profit
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[3]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.operatingProfit = Double.parseDouble(screenData);
			//Annual operating profit margin
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[4]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.OPMargin = Double.parseDouble(screenData);
			//Annual other income
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[5]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.otherIncome = Double.parseDouble(screenData);		
			//Annual interest
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[6]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.interest = Double.parseDouble(screenData);	
			//Annual depreciation
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[7]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.depreciation = Double.parseDouble(screenData);				
				
			//Annual profit before tax
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[8]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.profitBeforeTax = Double.parseDouble(screenData);		
			//Annual tax
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[9]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.tax = Double.parseDouble(screenData);		
			//Annual net profit
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[10]/td[" + annualcolumn + "]"));										   
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.netProfit = Double.parseDouble(screenData);
			
			//Annual EPS
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[11]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.EPS = Double.parseDouble(screenData);
			
			//Annual Dividend Pay out
			ele = driver.findElement(By.xpath("//*[@id='annuals']/div[1]/div/table/tbody/tr[12]/td[" + annualcolumn + "]"));										   
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.dividendPayOut = Double.parseDouble(screenData);
			
			//Annual cashflow
			ele = driver.findElement(By.xpath("//*[@id='cashflow']/div/div/table/tbody/tr[4]/td[" + annualcolumn + "]"));
			screenData = ele.getText().replace(",", "");
			companyAnnualFinancialDatatmp.netCashFLow = Double.parseDouble(screenData);			
			
			companyAnnualFinancialDatatmpList.add(companyAnnualFinancialDatatmp);
		}
		
		return companyAnnualFinancialDatatmpList;
	}
	
	private void StoreCompanyFinancialDataToDB(CompanyFinancialData companyFinancialDataObj) {		
		Statement statement = null;
		String tmpsql;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASS);
							
			statement = connection.createStatement();
			tmpsql = "INSERT INTO STOCK_FINANCIAL_DATA (STOCKNAME, BSECODE, BOOKVALUE, FACEVALUE, YEARLYHIGH, YEARLYLOW, STOCKPE, DIVIDENDYIELD) VALUES('" + 
    				stockName + "','" + bseCode + "'," + companyFinancialDataObj.bookValue + "," + companyFinancialDataObj.faceValue + "," + companyFinancialDataObj.yearlyHigh + 
    				"," + companyFinancialDataObj.yearlyLow + "," + companyFinancialDataObj.StockPE + "," + companyFinancialDataObj.dividendYield + ");";
        	statement.executeUpdate(tmpsql);
			
			connection.close();
			connection = null;			
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
	}
	
	private void StoreQuarterDataToDB(ArrayList<CompanyQuarterlyFinancialData> companyQuarterlyFinancialDataList) {		
		Statement statement = null;
		String tmpsql;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASS);
			for (CompanyQuarterlyFinancialData stockquarterlyDataCode : companyQuarterlyFinancialDataList) {				
				statement = connection.createStatement();
				tmpsql = "INSERT INTO STOCK_QUARTERLY_FINANCIAL_DATA (STOCKNAME, BSECODE, MONTH, YEAR, SALES, EXPENSES, OPERATINGPROFIT, OPMARGIN, OTHERINCOME, INTEREST, DEPRICIATION, PROFITBEFORETAX, TAX, NETPROFIT) VALUES('" + 
        				stockName + "','" + bseCode + "','" + stockquarterlyDataCode.month + "'," + stockquarterlyDataCode.year + "," + stockquarterlyDataCode.sales + "," + stockquarterlyDataCode.expenses + 
        				"," + stockquarterlyDataCode.operatingProfit + "," + stockquarterlyDataCode.OPMargin + "," + stockquarterlyDataCode.otherIncome + "," + stockquarterlyDataCode.interest +
        				stockquarterlyDataCode.depreciation + "," + stockquarterlyDataCode.profitBeforeTax + "," + stockquarterlyDataCode.tax + "," + stockquarterlyDataCode.netProfit + ");";
				statement.executeUpdate(tmpsql);
			}
			connection.close();
			connection = null;			
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
	}

	
	private void StoreAnnualDataToDB(ArrayList<CompanyAnnualFinancialData> companyAnnualFinancialDataList) {

		Statement statement = null;
		String tmpsql;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASS);
			for (CompanyAnnualFinancialData stockAnnualData : companyAnnualFinancialDataList) {				
				statement = connection.createStatement();
				tmpsql = "INSERT INTO STOCK_ANNUAL_FINANCIAL_DATA (STOCKNAME, BSECODE, MONTH, YEAR, SALES, EXPENSES, OPERATINGPROFIT, OPMARGIN, OTHERINCOME, INTEREST, DEPRICIATION, PROFITBEFORETAX, TAX, NETPROFIT, EPS, DIVIDENDPAYOUT, NETCASHFLOW) VALUES('" + 
        				stockName + "','" + bseCode + "','" + stockAnnualData.month + "'," + stockAnnualData.year + "," + stockAnnualData.sales + "," + stockAnnualData.expenses + 
        				"," + stockAnnualData.operatingProfit + "," + stockAnnualData.OPMargin + "," + stockAnnualData.otherIncome + "," + stockAnnualData.interest +
        				stockAnnualData.depreciation + "," + stockAnnualData.profitBeforeTax + "," + stockAnnualData.tax + "," + stockAnnualData.netProfit +
						stockAnnualData.EPS + "," + stockAnnualData.dividendPayOut + "," + stockAnnualData.netCashFLow + ");";
				statement.executeUpdate(tmpsql);
			}
			connection.close();
			connection = null;			
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
	}
	
	private ArrayList<String> getStockListFromDB() {

		ResultSet resultSet = null;
		Statement statement = null;
		String stockBSECode;
		ArrayList<String> stockList = null;

		try {
			stockList = new ArrayList<String>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT BSECODE, stockname FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + resultSet.getString(2);
				stockList.add(stockBSECode);
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			connection.close();
			connection = null;
			return stockList;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockListFromDB  -> ", ex);
			return null;
		}
	}
}
