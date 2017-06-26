import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class CalculateSimpleAndExpoMovingAvg {
	Connection connection = null;

	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
		obj.MovingAverageCalculation();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}
	
	public void MovingAverageCalculation(){
		ArrayList<String> stockList = null;
		stockList = getStockListFromDB();

		for (String stockCode : stockList) {
			//calculate average on bulk
			//obj.calculateSimpleMovingAverage(stockCode);
			//calculate average on daily basis
			calculateSimpleMovingAverageDaily(stockCode);
		}
	}

	private ArrayList<String> getStockListFromDB() {

		ResultSet resultSet = null;
		Statement statement = null;
		String stockNSECode;
		ArrayList<String> stockList = null;

		try {
			stockList = new ArrayList<String>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
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
			return null;
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
		stockDetails = getStockDetailsFromDBForDaily(stockCode);

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

			// }
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
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
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
			return null;
		}
	}

	private SMAData getStockDetailsFromDBForDaily(String stockCode) {
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
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
			smaDataObj = new SMAData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			resultSet = statement
					.executeQuery("SELECT first 200 tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
							+ stockCode + "' order by tradeddate desc;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			return smaDataObj;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			return null;
		}
	}

	private void storeMovingAverageinDB(String stockName, String tradedDate, float simpMovingAverage, int period,
			float closingPrice, float expMovingAverage) {
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
			return eMA;
		}
	}
}
