import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class CalculateSimpleAndExpoMovingAvg {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateSimpleAndExpoMovingAvg.class);
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateSimpleAndExpoMovingAvg Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
		obj.MovingAverageCalculation();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateSimpleAndExpoMovingAvg End");
	}
	
	public void MovingAverageCalculation(){
		ArrayList<String> stockList = null;
		Date todayDate = new Date();
		
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;

		for (String stockCode : stockList) {
			//calculate average on bulk
			//calculateSimpleMovingAverage(stockCode);
			//calculate average on daily basis
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			calculateSimpleMovingAverageDaily(nseCode);
		}
	}

	private void calculateSimpleMovingAverage(String stockCode) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		stockDetails = getStockDetailsFromDB(stockCode);
		for (int counter = 0; counter < stockDetails.tradeddate.size() - 3; counter++) {
			period = 1;
			sumOfClosingPrices = 0;
			System.out.println(" Stock -> " + stockCode + " Round -> " + (counter + 1));
			for (int counter1 = counter; counter1 < stockDetails.tradeddate.size(); counter1++) {
				sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter1);

				if (period == 3 || period == 5 || period == 9 || period == 14 || period == 20 || period == 50
						|| period == 200) {
					simpleMovingAverage = sumOfClosingPrices / period;
					expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter1), period);
					if (expMovingAvg == -1) {
						expMovingAvg = simpleMovingAverage;
					}
					storeMovingAverageinDB(stockCode, stockDetails.tradeddate.get(counter1), simpleMovingAverage,
							period, stockDetails.closePrice.get(counter1).floatValue(), expMovingAvg);
				}
				period++;
				if (period > 200) {
					break;
				}

			}
		}
	}

	private void calculateSimpleMovingAverageDaily(String stockCode) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		Date date = null;
		stockDetails = getStockDetailsFromDBForDaily(stockCode, date);
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter);
				if (period == 3 || period == 5 || period == 9 || period == 14 || period == 20 || period == 50
						|| period == 200) {
					simpleMovingAverage = sumOfClosingPrices / period;
					System.out.println(" Stock -> " + stockCode + " Period -> " + (counter));
					expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), period);
					if (expMovingAvg == -1) {
						expMovingAvg = simpleMovingAverage;
					}
					storeMovingAverageinDB(stockCode, stockDetails.tradeddate.get(0), simpleMovingAverage, period,
							stockDetails.closePrice.get(0).floatValue(), expMovingAvg);
				}
				period++;
				if (period > 200) {
					break;
				}
			} 
		}catch (Exception ex) {
				System.out.println("Error in DB action");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
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

	private SMAData getStockDetailsFromDB(String stockCode) {
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
					+ stockCode + "' order by tradeddate;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			return smaDataObj;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockDetailsFromDB  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDB Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDB  -> ", ex);
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
			return smaDataObj;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockDetailsFromDBForDaily  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForDaily Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDBForDaily  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForDaily Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForDaily  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForDaily Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDBForDaily  -> ", ex);
			}
		}
	}

	private void storeMovingAverageinDB(String stockName, String tradedDate, float simpMovingAverage, int period,
			float closingPrice, float expMovingAverage) {
		Statement statement = null;
		String tmpsql;
		try {
			
			connection = StockUtils.connectToDB();
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
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForDaily Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForDaily  -> ", ex);
			}
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

	private float calculateExpMvingAvg(String stockName, float closePrice, int period) {
		float eMA;
		float lastExpMovingAvgStored;

		lastExpMovingAvgStored = getExpMovingAverageFromDB(stockName, period);

		if (lastExpMovingAvgStored != -1) {
			eMA = (2 / ((float) period + 1)) * (closePrice - lastExpMovingAvgStored) + lastExpMovingAvgStored;
		} else {
			eMA = -1;
		}
		return eMA;
	}

	private float getExpMovingAverageFromDB(String stockName, int period) {

		ResultSet resultSet = null;
		Statement statement = null;
		float eMA = -1;
		try {
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT EMA, tradeddate FROM DAILYSNEMOVINGAVERAGES where stockName ='"
					+ stockName + "' and PERIOD = " + period + " order by tradeddate desc;");
			while (resultSet.next()) {
				eMA = Float.parseFloat(resultSet.getString(1));
				break;
				// System.out.println("StockNme - " + stockNSECode);
			}
			return eMA;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getExpMovingAverageFromDB", ex);
			return eMA;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getExpMovingAverageFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getExpMovingAverageFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getExpMovingAverageFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getExpMovingAverageFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getExpMovingAverageFromDB Error in closing connection "+ex);
				logger.error("Error in closing connection getExpMovingAverageFromDB  -> ", ex);
			}
		}
	}
}
