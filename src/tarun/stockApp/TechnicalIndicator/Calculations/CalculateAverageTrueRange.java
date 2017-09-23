package tarun.stockApp.TechnicalIndicator.Calculations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import tarun.stockApp.TechnicalIndicator.Data.StochasticOscillatorData;

public class CalculateAverageTrueRange {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateAverageTrueRange.class);
	public static int ATR_PERIOD = 14;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateAverageTrueRange Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateAverageTrueRange obj = new CalculateAverageTrueRange();
		obj.calculateAverageTrueRangeForAllStocks();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateAverageTrueRange End");
	}
	
	public void calculateAverageTrueRangeForAllStocks() {
		ArrayList<String> stockList = null;
		Date todayDate = new Date();
		
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;
		
//		calculateAverageTrueRangeForStockInBulk("UPL");
		
		for (String stockCode : stockList) {
			
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Calculating Average True Range for stock - >"+nseCode);
			//calculate RSI on bulk
			calculateAverageTrueRangeForStockInBulk(nseCode);
//			//calculate average on daily basis
//			//calculateAverageTrueRangeForStock(nseCode, null);
		}
	}
	
	private void calculateAverageTrueRangeForStockInBulk(String stockCode) {
		StochasticOscillatorData stockDetails = null;
		float currentHighLowDifference = 0, currentHighAndPreviousCloseDifference = 0, currentLowAndPreviousCloseDifference = 0, trueRange = 0, sumOfTrueRange = 0, averageTrueRange = 0;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBForBulk(stockCode);
		ArrayList<Float> highestHighArr, lowestLowArr;
		Comparator<Float> comparatorForLow = Collections.reverseOrder();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			} 
			connection = StockUtils.connectToDB();
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				if(counter < ATR_PERIOD) {
					currentHighLowDifference = stockDetails.highPrice.get(counter) - stockDetails.lowPrice.get(counter);
					if(counter > 0) {
						currentHighAndPreviousCloseDifference = Math.abs(stockDetails.highPrice.get(counter) - stockDetails.closePrice.get(counter-1));
						currentLowAndPreviousCloseDifference =  Math.abs(stockDetails.lowPrice.get(counter) - stockDetails.closePrice.get(counter-1));
					}
					trueRange =  Math.max(currentLowAndPreviousCloseDifference, Math.max(currentHighLowDifference, currentHighAndPreviousCloseDifference));
					sumOfTrueRange = sumOfTrueRange + trueRange;
					if(counter == ATR_PERIOD-1) {
						System.out.println("Sum of true range -> "+sumOfTrueRange);
						averageTrueRange = sumOfTrueRange / ATR_PERIOD;
						//Store ATR in DB
						System.out.println("Inserting oschillator value in DB Date-> "+ stockDetails.tradeddate.get(counter) + " ATR -> "+ averageTrueRange );
						//storeStochasticOscillatorinDB(stockCode, stockDetails.tradeddate.get(counter), ATR_PERIOD, stochasticOscillator);
					}
				} else {
					currentHighLowDifference = stockDetails.highPrice.get(counter) - stockDetails.lowPrice.get(counter);
					currentHighAndPreviousCloseDifference = Math.abs(stockDetails.highPrice.get(counter) - stockDetails.closePrice.get(counter-1));
					currentLowAndPreviousCloseDifference =  Math.abs(stockDetails.lowPrice.get(counter) - stockDetails.closePrice.get(counter-1));
					trueRange =  Math.max(currentLowAndPreviousCloseDifference, Math.max(currentHighLowDifference, currentHighAndPreviousCloseDifference));
					averageTrueRange = ((averageTrueRange* (ATR_PERIOD-1)) + trueRange) / ATR_PERIOD;
					System.out.println("Inserting oschillator value in DB Date-> "+ stockDetails.tradeddate.get(counter) + " ATR -> "+ averageTrueRange );
					//storeStochasticOscillatorinDB(stockCode, stockDetails.tradeddate.get(counter), ATR_PERIOD, stochasticOscillator);
				}
				
			
			}
		} catch (Exception ex) {
			System.out.println("calculateStochasticOscillatorForStockInBulk Error in DB action "+ex);
			logger.error("Error in getBBIndicationForStock  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("calculateStochasticOscillatorForStockInBulk Error in DB action ");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	private StochasticOscillatorData getStockDetailsFromDBForBulk(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice, highPrice, lowPrice;
		StochasticOscillatorData soDataObj = null;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			soDataObj = new StochasticOscillatorData();
			soDataObj.closePrice = new ArrayList<Float>();
			soDataObj.tradeddate = new ArrayList<String>();
			soDataObj.highPrice = new ArrayList<Float>();
			soDataObj.lowPrice = new ArrayList<Float>();			
			statement = connection.createStatement();
			soDataObj.stockName = stockCode;
			resultSet = statement.executeQuery("SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate >= '1-Jun-2016' order by tradeddate;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				highPrice = Float.parseFloat(resultSet.getString(3));
				lowPrice = Float.parseFloat(resultSet.getString(4));
				soDataObj.closePrice.add(closePrice);
				soDataObj.tradeddate.add(tradedDate);
				soDataObj.highPrice.add(highPrice);
				soDataObj.lowPrice.add(lowPrice);
			}
			return soDataObj;
		} catch (Exception ex) {
			System.out.println("getStockDetailsFromDBForBulk -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForBulk  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
}
