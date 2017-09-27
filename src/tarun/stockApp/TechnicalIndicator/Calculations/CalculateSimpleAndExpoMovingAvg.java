package tarun.stockApp.TechnicalIndicator.Calculations;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import tarun.stockApp.TechnicalIndicator.Data.SMAData;

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
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			//calculate average on bulk
			calculateSimpleMovingAverage(nseCode);
			//calculate average on daily basis
			
			//calculateSimpleMovingAverageDaily(nseCode);
		}
	}

	private void calculateSimpleMovingAverage(String stockCode) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		
		ArrayList<Integer> smaPeriods;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			stockDetails = getStockDetailsFromDB(stockCode);
			smaPeriods = GetSMAPeriodsFromDB(stockCode);
			for (int counter = 0; counter < stockDetails.tradeddate.size() - 3; counter++) {
				period = 1;
				sumOfClosingPrices = 0;
				System.out.println(" Stock -> " + stockCode + " Round -> " + (counter + 1));
				for (int counter1 = counter; counter1 < stockDetails.tradeddate.size(); counter1++) {
					sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter1);
					if(smaPeriods.contains(period)) {
					/*if (period != 3 && period != 5 && period != 9 && period != 14 && period != 20 && period != 50
							&& period != 200) {*/
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

	private void calculateSimpleMovingAverageDaily(String stockCode) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		Date date = null;
		ArrayList<Integer> smaPeriods;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			stockDetails = getStockDetailsFromDBForDaily(stockCode, date);
			smaPeriods = GetSMAPeriodsFromDB(stockCode);
			
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter);
				if(smaPeriods.contains(period)) {
				//if (period == 3 || period == 5 || period == 9 || period == 14 || period == 20 || period == 50
				//		|| period == 200) {
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
		}
	}

	private void storeMovingAverageinDB(String stockName, String tradedDate, float simpMovingAverage, int period,
			float closingPrice, float expMovingAverage) {
		Statement statement = null;
		String tmpsql;
		try {
			
			//connection = StockUtils.connectToDB();
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
		}
	}
	
	private ArrayList<Integer> GetSMAPeriodsFromDB(String stockCode) {
		ArrayList<Integer> prefPeriod = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String[] prefPeriodsInDB;

		try {
			prefPeriod = new ArrayList<Integer>();
			
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT DAILYSMAPERIOD FROM STOCKWISEPERIODS where stockname = '" + stockCode + "';");
			while (resultSet.next()) {
				prefPeriodsInDB = resultSet.getString(1).split(",");
				for (int counter = 0; counter < prefPeriodsInDB.length; counter++) {
					if (!prefPeriodsInDB[counter].equals("3") && !prefPeriodsInDB[counter].equals("5") && !prefPeriodsInDB[counter].equals("9") && !prefPeriodsInDB[counter].equals("14") && !prefPeriodsInDB[counter].equals("20") && !prefPeriodsInDB[counter].equals("50")
							&& !prefPeriodsInDB[counter].equals("200")) {
						prefPeriod.add(new Integer(prefPeriodsInDB[counter]));
					}
				}
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
		} catch (Exception ex) {
			System.out.println("Error in getting SMA period from DB" + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("GetSMAPeriodsFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset GetSMAPeriodsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("GetSMAPeriodsFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement GetSMAPeriodsFromDB  -> ", ex);
			}
		}
		return prefPeriod;
	}
}
