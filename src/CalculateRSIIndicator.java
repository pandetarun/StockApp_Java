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
			//calculate RSI on bulk
			calculateRSIForStockInBulk(stockCode);
			//calculate average on daily basis
			//calculateRSIForStock(nseCode);
		}
	}
	
	private void calculateRSIForStockInBulk(String stockCode) {
		SMAData stockDetails = null;
		float sumOfLosses = 0, sumOfGains = 0, priceDifference, avgGain = 0, avgLoss = 0, stockRS = 0, stockRSI = 0;
		
		
		stockDetails = getStockDetailsFromDBForBulk(stockCode);

		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				priceDifference = stockDetails.closePrice.get(counter) - stockDetails.closePrice.get(counter-1); 
				if( counter <= RSI_PERIOD) {			
					if(priceDifference > 0) {
						sumOfGains = sumOfGains + priceDifference;
					} else if(priceDifference < 0) {
						sumOfLosses = sumOfLosses + (priceDifference * -1);
					}
					if( counter == RSI_PERIOD ) {
						avgGain = sumOfGains / counter;
						avgLoss = sumOfLosses / counter;
					}
				} else {
					if(priceDifference > 0) {
						avgGain = ((avgGain * 13) + priceDifference) / 14;
					} else if(priceDifference < 0) {
						avgLoss = ((avgLoss * 13) + (priceDifference * -1)) / 14;
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
					storeRSIinDB(stockCode, stockDetails.tradeddate.get(counter), stockRS, stockRSI, counter);
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
			
			
			sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter);
			if (period == 3 || period == 5 || period == 9 || period == 14 || period == 20 || period == 50
					|| period == 200) {
				simpleMovingAverage = sumOfClosingPrices / period;
				System.out.println(" Stock -> " + stockCode + " Period -> " + (counter));
				expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), period);
				if (expMovingAvg == -1) {
					expMovingAvg = simpleMovingAverage;
				}
				storeRSIinDB(stockCode, stockDetails.tradeddate.get(0), simpleMovingAverage, period,
						stockDetails.closePrice.get(0).floatValue(), expMovingAvg);
			}
			period++;
			if (period > 200) {
				break;
			}

			// }
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
					+ stockCode + "' and tradeddate >= '1-Jan-2017' order by tradeddate;");
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
	
	private void storeRSIinDB(String stockName, String tradedDate, float RS, float RSI, int period) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILYSNEMOVINGAVERAGES (STOCKNAME, TRADEDDATE, SMA, EMA, PERIOD, CLOSINGPRICE) VALUES('"
					+ stockName + "','" + tradedDate + "'," + simpMovingAverage + "," + expMovingAverage + "," + period
					+ "," + closingPrice + ");";
			statement.executeUpdate(tmpsql);
		} catch (Exception ex) {
			System.out.println("storeMovingAverageinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeMovingAverageinDB  ->  storeMovingAverageinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		}
	}
	
}
