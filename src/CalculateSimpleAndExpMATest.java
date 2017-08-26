import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class CalculateSimpleAndExpMATest {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateSimpleAndExpoMovingAvg.class);
	static String DATES_FILE = "C:\\Tarun\\Personal\\Tool\\StockApp\\DatesForMA.txt";
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateSimpleAndExpoMovingAvg Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateSimpleAndExpMATest obj = new CalculateSimpleAndExpMATest();
		obj.MovingAverageCalculation();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateSimpleAndExpoMovingAvg End");
	}
	
	public void MovingAverageCalculation(){
		ArrayList<String> stockList = null;
		stockList = getStockListFromDB();
		//Get dates for which calculation needs to be done
		ArrayList<Date> DatesToCalculate = readFileAndGetDates();
		for(Date datetoCalculateSMA : DatesToCalculate) {
			for (String stockCode : stockList) {
				
				//calculate average on bulk
				//calculateSimpleMovingAverage(stockCode);
				//calculate average on daily basis
				calculateSimpleMovingAverageDaily(stockCode, datetoCalculateSMA);
			}
		}
	}

	private ArrayList<Date> readFileAndGetDates () {
		ArrayList<Date> DatesToCalculate = new ArrayList<Date>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(DATES_FILE));
			String line = br.readLine();
			while (line != null) {
				DatesToCalculate.add(new Date(line));
				line = br.readLine();
			}
		} catch (Exception ex) {
			System.out.println("Error in reading date file - > "+ex);
			logger.error("Error in getStockListFromDB  -> ", ex);
			//return null;
		}
		return DatesToCalculate;
	}
	
	private ArrayList<String> getStockListFromDB() {

		ResultSet resultSet = null;
		Statement statement = null;
		String stockNSECode;
		ArrayList<String> stockList = null;

		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToTestDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT NSECODE FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockNSECode = resultSet.getString(1);
				stockList.add(stockNSECode);
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

	private void calculateSimpleMovingAverageDaily(String stockCode, Date dateToCalculate) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		stockDetails = getStockDetailsFromDBForDaily(stockCode, dateToCalculate);

		for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
			sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter);
			if (period == 3 || period == 5 || period == 9 || period == 14 || period == 20 || period == 50
					|| period == 200) {
				simpleMovingAverage = sumOfClosingPrices / period;
				System.out.println(" Stock -> " + stockCode + " Period -> " + (counter));
				expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), period, dateToCalculate);
				if (expMovingAvg == -1) {
					expMovingAvg = simpleMovingAverage;
				}
				storeMovingAverageinDB(stockCode, stockDetails.tradeddate.get(0), simpleMovingAverage, period,
						stockDetails.closePrice.get(0).floatValue(), expMovingAvg, dateToCalculate);
			}
			period++;
			if (period > 200) {
				break;
			}

			// }
		}
	}

	private SMAData getStockDetailsFromDBForDaily(String stockCode, Date dateToCalculate) {
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
			connection = StockUtils.connectToTestDB();
			smaDataObj = new SMAData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			tmpSQL = "SELECT first 200 tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"+ stockCode + "' and tradeddate <='" + dateFormat.format(dateToCalculate) + "' order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
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
		}
	}

	private void storeMovingAverageinDB(String stockName, String tradedDate, float simpMovingAverage, int period,
			float closingPrice, float expMovingAverage, Date dateToCalculate) {
		Statement statement = null;
		String tmpsql;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILYSNEMOVINGAVERAGES (STOCKNAME, TRADEDDATE, SMA, EMA, PERIOD, CLOSINGPRICE) VALUES('"
					+ stockName + "','" + dateFormat.format(dateToCalculate) + "'," + simpMovingAverage + "," + expMovingAverage + "," + period
					+ "," + closingPrice + ");";
			statement.executeUpdate(tmpsql);
		} catch (Exception ex) {
			System.out.println("storeMovingAverageinDB for quote -> " + stockName + " and Date - > " + dateFormat.format(dateToCalculate)
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeMovingAverageinDB  ->  storeMovingAverageinDB for quote -> " + stockName + " and Date - > " + dateFormat.format(dateToCalculate)
					+ " and period  - > " + period, ex);
		}
	}

	private float calculateExpMvingAvg(String stockName, float closePrice, int period, Date dateToCalculate) {
		float eMA;
		float lastExpMovingAvgStored;

		lastExpMovingAvgStored = getExpMovingAverageFromDB(stockName, period, dateToCalculate);

		if (lastExpMovingAvgStored != -1) {
			eMA = (2 / ((float) period + 1)) * (closePrice - lastExpMovingAvgStored) + lastExpMovingAvgStored;
		} else {
			eMA = -1;
		}
		return eMA;
	}

	private float getExpMovingAverageFromDB(String stockName, int period, Date dateToCalculate) {

		ResultSet resultSet = null;
		Statement statement = null;
		float eMA = -1;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String tmpSQL;
		
		try {
			statement = connection.createStatement();
			tmpSQL = "SELECT EMA, tradeddate FROM DAILYSNEMOVINGAVERAGES where stockName ='"
					+ stockName + "' and PERIOD = " + period + " and tradeddate <='" + dateFormat.format(dateToCalculate) + "' order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
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
		}
	}
}
