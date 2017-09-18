import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class CalculateRSIIndicator {

	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateRSIIndicator.class);
	public static int RSI_PERIOD = 14;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateSimpleAndExpoMovingAvg Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateRSIIndicator obj = new CalculateRSIIndicator();
		obj.CalculateRSIForAllStocks();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateSimpleAndExpoMovingAvg End");
	}
	
	public void CalculateRSIForAllStocks() {
		ArrayList<String> stockList = null;
		Date todayDate = new Date();
		
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;

		for (String stockCode : stockList) {
			
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Calculating RSI for stock - >"+nseCode);
			//calculate RSI on bulk
			calculateRSIForStockInBulk(nseCode);
			//calculate average on daily basis
			//calculateRSIForStock(nseCode);
		}
	}
	
	private void calculateRSIForStockInBulk(String stockCode) {
		SMAData stockDetails = null;
		float sumOfLosses = 0, sumOfGains = 0, priceDifference, avgGain = 0, avgLoss = 0, stockRS = 0, stockRSI = 0;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBForBulk(stockCode);

		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			for (int counter = 1; counter < stockDetails.tradeddate.size(); counter++) {
				priceDifference = stockDetails.closePrice.get(counter) - stockDetails.closePrice.get(counter-1); 
				if( counter <= RSI_PERIOD+1) {			
					if(priceDifference > 0) {
						sumOfGains = sumOfGains + priceDifference;
					} else if(priceDifference < 0) {
						sumOfLosses = sumOfLosses + (priceDifference * -1);
					}
					if( counter == RSI_PERIOD ) {
						avgGain = sumOfGains / RSI_PERIOD;
						avgLoss = sumOfLosses / RSI_PERIOD;
					}
				} else {
					if(priceDifference > 0) {
						avgGain = ((avgGain * (RSI_PERIOD-1)) + priceDifference) / RSI_PERIOD;
						avgLoss = (avgLoss * (RSI_PERIOD-1)) / RSI_PERIOD;
					} else if(priceDifference < 0) {
						avgLoss = ((avgLoss * (RSI_PERIOD-1)) + (priceDifference * -1)) / RSI_PERIOD;
						avgGain = (avgGain * (RSI_PERIOD-1)) / RSI_PERIOD;
					}
				}
				
				if(counter >= RSI_PERIOD) {
					stockRS = avgGain / avgLoss;
					if( avgLoss == 0 ) {
						stockRSI = 100;
					} else {
						stockRSI = 100 - (100/(1+stockRS));
					}
					//Call method to store RS and RSI with period in DB
					System.out.println("Inserting RSI value in DB");
					storeRSIinDB(stockCode, stockDetails.tradeddate.get(counter), stockRS, stockRSI, counter, avgGain, avgLoss);
				}
			}
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getBBIndicationForStock  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("Error in DB action");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	private void calculateRSIForStock(String stockCode) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfLosses = 0, sumOfGains = 0, priceDifference;
		Date date = null;
		stockDetails = getStockDetailsFromDBForDaily(stockCode, date);

		for (int counter = 1; counter < stockDetails.tradeddate.size(); counter++) {
			
			
			
			
					}
	}
	
	private SMAData getStockDetailsFromDBForBulk(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice;
		SMAData smaDataObj = null;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			smaDataObj = new SMAData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			resultSet = statement.executeQuery("SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate >= '1-Jun-2016' order by tradeddate;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			statement.close();
			statement = null;
			return smaDataObj;
		} catch (Exception ex) {
			System.out.println("getStockDetailsFromDBForBulk -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForBulk  -> ", ex);
			return null;
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("Error in DB action");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	private SMAData getStockDetailsFromDBForDaily(String stockCode, Date SMDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice;
		SMAData smaDataObj = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			smaDataObj = new SMAData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			if(SMDate!=null) {
				tmpSQL = "SELECT first 200 tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' and tradeddate<='" + dateFormat.format(SMDate) +"' order by tradeddate desc;";
			} else {
				tmpSQL = "SELECT first 200 tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
							+ stockCode + "' order by tradeddate desc;";
			}
			resultSet = statement
					.executeQuery(tmpSQL);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			statement.close();
			statement = null;
			return smaDataObj;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockDetailsFromDBForDaily  -> ", ex);
			return null;
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("Error in DB action");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	private void storeRSIinDB(String stockName, String tradedDate, float RS, float RSI, int period, float avgGain, float avgLoss) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILY_RELATIVE_STRENGTH_INDEX (STOCKNAME, TRADEDDATE, PERIOD, STOCKRS, STOCKRSI, AVG_GAIN, AVG_LOSS) VALUES('"
					+ stockName + "','" + tradedDate + "'," + period + "," + RS + "," + RSI + "," + avgGain + "," + avgLoss + ");";
			statement.executeUpdate(tmpsql);
			statement.close();
		} catch (Exception ex) {
			System.out.println("storeRSIinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeRSIinDB  ->  storeRSIinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		}
	}
	
}
