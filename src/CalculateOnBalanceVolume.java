import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
public class CalculateOnBalanceVolume {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateOnBalanceVolume.class);	
	String stockName;
	String bseCode;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateOnBalanceVolume Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateOnBalanceVolume obj = new CalculateOnBalanceVolume();
		obj.OnBalanceVolumeCalculation();
		//dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateOnBalanceVolume End");
	}
	
	private void OnBalanceVolumeCalculation() {
		ArrayList<String> stockList = null;
		stockList = StockUtils.getStockListFromDB();
		ArrayList<OnBalanceVolumeIndicator> onBalanceSelectedStockList = new ArrayList<OnBalanceVolumeIndicator>();
		OnBalanceVolumeIndicator tmpOnBalanceVolumeIndicator;
		for (String stockCode : stockList) {
			//calculate average on bulk
			//calculateOnBalanceVolume(stockCode);
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			if(StockUtils.getFinancialIndication(bseCode)) {
				tmpOnBalanceVolumeIndicator = calculateOnBalanceVolumeDaily(stockCode);
				if (tmpOnBalanceVolumeIndicator!=null) {
					onBalanceSelectedStockList.add(tmpOnBalanceVolumeIndicator);
				}
			}
		}
	}
	
	private void calculateOnBalanceVolume(String stockCode) {
		SMAData stockDetails = null;
		long onBalanceVolume = 0;
		float lastDayClosingPrice = 0;
		stockDetails = getStockDetailsFromDB(stockCode);
		if (stockDetails == null || stockDetails.tradeddate == null) {
			System.out.println("stock details null for - > "+stockCode);
		}
		for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
			if(counter == 0) {
				onBalanceVolume = 0;
				
			} else {
				if (stockDetails.closePrice.get(counter) > lastDayClosingPrice) {
					onBalanceVolume = onBalanceVolume + stockDetails.volume.get(counter);
				} else if (stockDetails.closePrice.get(counter) < lastDayClosingPrice) {
					onBalanceVolume = onBalanceVolume - stockDetails.volume.get(counter);
				}
			}
			lastDayClosingPrice = stockDetails.closePrice.get(counter);
			storeOnBalanceVolumeinDB(stockCode, stockDetails.tradeddate.get(counter), stockDetails.closePrice.get(counter),onBalanceVolume, stockDetails.volume.get(counter));
			
		}
	}
	
	private OnBalanceVolumeIndicator calculateOnBalanceVolumeDaily(String stockCode) {
		OnBalanceVolumeData stockDetails = null;
		long onBalanceVolume = 0;
		float lastDayClosingPrice = 0;
		//long volumechangeinlasday;
		boolean continuousVolumeIncrease = true;
		
		stockDetails = getStockDetailsFromDBDaily(stockCode);
		ArrayList<Long> tmpOnBalanceVol = new ArrayList<Long>();
		if (stockDetails == null || stockDetails.tradeddate == null) {
			System.out.println("stock details null for - > "+stockCode);
		}
		for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {			
			if(counter == 0) {
				onBalanceVolume = 0;				
			} else {
				if (stockDetails.closePrice.get(counter) > lastDayClosingPrice) {
					onBalanceVolume = onBalanceVolume + stockDetails.volume.get(counter);
				} else if (stockDetails.closePrice.get(counter) < lastDayClosingPrice) {
					onBalanceVolume = onBalanceVolume - stockDetails.volume.get(counter);
				}
			}
			tmpOnBalanceVol.add(onBalanceVolume);
			lastDayClosingPrice = stockDetails.closePrice.get(counter);
			//storeOnBalanceVolumeinDB(stockCode, stockDetails.tradeddate.get(counter), stockDetails.closePrice.get(counter),onBalanceVolume, stockDetails.volume.get(counter));			
		}
		//if last day volume is down then do not add stock to list
		if(tmpOnBalanceVol.get(tmpOnBalanceVol.size()-1) < tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2)) {
			return null;
		}
		OnBalanceVolumeIndicator tmpOnBalanceVolumeIndicator = new OnBalanceVolumeIndicator();
		tmpOnBalanceVolumeIndicator.stockName = stockCode;
		tmpOnBalanceVolumeIndicator.tradeddate = stockDetails.tradeddate.get(stockDetails.tradeddate.size()-1);
		tmpOnBalanceVolumeIndicator.volumeChangeInLastDay = tmpOnBalanceVol.get(tmpOnBalanceVol.size()-1) - tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2);
		tmpOnBalanceVolumeIndicator.continuousIncreasedVolume = continuousVolumeIncrease;
		tmpOnBalanceVolumeIndicator.percentageChangeInLastDay = (tmpOnBalanceVol.get(tmpOnBalanceVol.size()-1) - tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2))/tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2);
		
		for (int counter = tmpOnBalanceVol.size()-2; counter > 0 ; counter--) {
			if (tmpOnBalanceVol.get(counter) < tmpOnBalanceVol.get(counter-1)) {
				continuousVolumeIncrease = false;
				break;
			}
		}
		return tmpOnBalanceVolumeIndicator;
	}
	
	private SMAData getStockDetailsFromDB(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice;
		long volume;
		int counter = 0;
		SMAData smaDataObj = null;
		if (stockCode.equalsIgnoreCase("UPL")) {
			System.out.println("TEst");
		}
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
			smaDataObj.volume = new ArrayList<Long>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			resultSet = statement.executeQuery("SELECT tradeddate, closeprice, volume FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' order by tradeddate;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				if (tradedDate.equalsIgnoreCase("2015-06-08")) {
					System.out.println("Tet");
				}
				//BigDecimal.valueOf(resultSet.getString(3));
				closePrice = Float.parseFloat(resultSet.getString(2));
				//volume =  Long.parseLong((resultSet.getString(3).substring(0, resultSet.getString(3).length()-2)));
				volume = BigDecimal.valueOf(resultSet.getDouble(3)).longValue();
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
				smaDataObj.volume.add(volume);
			}
			return smaDataObj;
		} catch (Exception ex) {
			try{
				System.out.println("Error in DB action Date = " + resultSet.getString(1));
			} catch(Exception ex1) { }
			logger.error("Error in getStockDetailsFromDB  -> ", ex);
			return null;
		}
	}

	private OnBalanceVolumeData getStockDetailsFromDBDaily(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice;
		long volume;
		int counter = 0;
		OnBalanceVolumeData onBalanceVolumeDataObj = null;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			onBalanceVolumeDataObj = new OnBalanceVolumeData();
			onBalanceVolumeDataObj.closePrice = new ArrayList<Float>();
			onBalanceVolumeDataObj.tradeddate = new ArrayList<String>();
			onBalanceVolumeDataObj.volume = new ArrayList<Long>();
			onBalanceVolumeDataObj.onBalanceVolume = new ArrayList<Long>();
			statement = connection.createStatement();
			onBalanceVolumeDataObj.stockName = stockCode;
			resultSet = statement.executeQuery("Select * from (SELECT first 10 tradeddate, closeprice, volume FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' order by tradeddate desc ) order by TRADEDDATE;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				/*if (tradedDate.equalsIgnoreCase("2015-06-08")) {
					System.out.println("Tet");
				}*/
				//BigDecimal.valueOf(resultSet.getString(3));
				closePrice = Float.parseFloat(resultSet.getString(2));
				//volume =  Long.parseLong((resultSet.getString(3).substring(0, resultSet.getString(3).length()-2)));
				volume = BigDecimal.valueOf(resultSet.getDouble(3)).longValue();
				onBalanceVolumeDataObj.closePrice.add(closePrice);
				onBalanceVolumeDataObj.tradeddate.add(tradedDate);
				onBalanceVolumeDataObj.volume.add(volume);
			}
			return onBalanceVolumeDataObj;
		} catch (Exception ex) {
			try{
				System.out.println("Error in DB action Date = " + resultSet.getString(1));
			} catch(Exception ex1) { }
			logger.error("Error in getStockDetailsFromDB  -> ", ex);
			return null;
		}
	}
	
	private ArrayList<String> getStockListFromDB() {

		ResultSet resultSet = null;
		Statement statement = null;
		String stockNSECode;
		ArrayList<String> stockList = null;

		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
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
	
	private void storeOnBalanceVolumeinDB(String stockName, String tradedDate, float closingPrice, long onBalanceVolume, long volume) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO ONBALANCEVOLUME (STOCKNAME, TRADEDDATE, CLOSINGPRICE, ONBALANCEVOLUME, VOLUME) VALUES('"
					+ stockName + "','" + tradedDate + "'," + closingPrice + "," + onBalanceVolume + "," + volume + ");";
			statement.executeUpdate(tmpsql);
		} catch (Exception ex) {
			System.out.println("storeOnBalanceVolumeinDB for stock -> " + stockName + " and Date - > " + tradedDate + " Error in DB action" + ex);
			logger.error("Error in storeOnBalanceVolumeinDB for stock -> " + stockName + " and Date - > " + tradedDate, ex);
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
			connection = StockUtils.connectToDB();
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
			logger.error("Error in getStockDetailsFromDBForDaily  -> ", ex);
			return null;
		}
	}
	
}
