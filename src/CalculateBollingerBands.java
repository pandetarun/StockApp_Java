import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

public class CalculateBollingerBands {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateBollingerBands.class);
	public final static int ACCEPTED_PERCENTAGE_DEVIATION = 10;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateBollingerBands Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateBollingerBands obj = new CalculateBollingerBands();
		obj.calculateBollingerBandsBulk();		
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateBollingerBands End");
	}
	
	public void calculateBollingerBandsBulk() {
		ArrayList<String> stockList = null;
		String stockName;
		String bseCode;
		String nseCode;
		
		stockList = StockUtils.getStockListFromDB();

		for (String stockCode : stockList) {
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			//BulkBollingerBandCalculateAndStore(nseCode);
			calculateBollingerBandsDaily(nseCode);
		}
	}
	
	private void BulkBollingerBandCalculateAndStore(String stockCode) {
		ArrayList<DailyStockData> objDailyStockDataList;
		int counter = 1;
		float totalPrice = 0;
		String BBDate = null;
		double perioddeviation = 0;
        double BBLower = 0;
        double BBUper = 0;
        double periodBandwidth;
        float simpleMA, tmpvar;
        float closingPrice = 0;
        ArrayList<Float> periodData = null;
        ArrayList<Float> tmpPeriodData;
        
        
		String bbPeriod = getBBPeriod(stockCode);
		if(bbPeriod == null) {
			logger.error("Null Bb Period for stock -> "+stockCode);
			System.out.println("Null Bb Period for stock -> "+stockCode);
			return;
		}
		String[] tmplist = bbPeriod.split(",");
		ArrayList<String> bbPeriodArray = new ArrayList<String> (Arrays.asList(tmplist));
		ArrayList<String> tmpBBPeriodArray;
		System.out.println("Creating BB entry for stock -> " + stockCode);
		objDailyStockDataList = getStockDetailsFromDBForDaily(stockCode, null);
		if(objDailyStockDataList.size()>0) {
			for(int iterationcounter = 0; iterationcounter<200; iterationcounter++) {
				if(objDailyStockDataList.size()<14) {
					break;
				}
				counter = 1;
				totalPrice = 0;
				tmpBBPeriodArray = (ArrayList<String>) bbPeriodArray.clone();
				periodData = new ArrayList<Float>();
				for (DailyStockData objDailyStockData : objDailyStockDataList) {
					totalPrice = totalPrice + objDailyStockData.closePrice;
					if(counter==1) {
						BBDate = objDailyStockData.tradeddate;
						closingPrice = objDailyStockData.closePrice;
					}
					periodData.add(objDailyStockData.closePrice);
					if(tmpBBPeriodArray.size()==0) {
						break;
					}
					if( tmpBBPeriodArray.contains(counter+"") ) {	
						tmpBBPeriodArray.remove(counter+"");
						perioddeviation = 0;
		                BBLower = 0;
		                BBUper = 0;
		                tmpPeriodData = new ArrayList<Float>();
		                simpleMA = totalPrice/counter;
		                for(int counter1 = 0; counter1<counter; counter1++) {
		                	tmpPeriodData.add(periodData.get(counter1)-simpleMA);
		                	tmpvar = tmpPeriodData.get(counter1) * tmpPeriodData.get(counter1); 
		                	tmpPeriodData.set(counter1, tmpvar);
		                	perioddeviation = perioddeviation + tmpPeriodData.get(counter1);
		                }
		                perioddeviation = perioddeviation / counter;
		                perioddeviation = Math.sqrt(perioddeviation);
		                BBLower = simpleMA - 2 * perioddeviation;
		                BBUper = simpleMA + 2 * perioddeviation;
		                periodBandwidth = BBUper - BBLower;
		                insertBBToDB(stockCode, BBDate, counter, closingPrice, simpleMA, BBUper, BBLower, periodBandwidth);
					}			
					counter++;	
				}
				objDailyStockDataList.remove(0);
			}
		} else {
			System.out.println("Quote size is 0 for stock -> "+stockCode);
			
		}
		System.out.println("Test");
	}
	
	private String getBBPeriod(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String bbPeriod = null;
		String tmpSQL;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			tmpSQL = "SELECT DAILYBBPERIOD from STOCKWISEPERIODS where stockname='" + stockCode + "';";
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				bbPeriod = resultSet.getString(1);
			}
			return bbPeriod;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getBBPeriod  -> ", ex);
			return null;
		}
	}
	
	private ArrayList<DailyStockData> getStockDetailsFromDBForDaily(String stockCode, Date bbDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<DailyStockData> objDailyStockDataList = new ArrayList<DailyStockData>();
		DailyStockData objDailyStockData = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();	
			if(bbDate!=null) {
				tmpSQL = "SELECT CLOSEPRICE, HIGHPRICE, LOWPRICE, VOLUME, TRADEDDATE, OPENPRICE from DAILYSTOCKDATA where stockname='" + stockCode + "' and tradeddate <= '" + dateFormat.format(bbDate) +"' order by tradeddate desc;";
			} else {
				tmpSQL = "SELECT CLOSEPRICE, HIGHPRICE, LOWPRICE, VOLUME, TRADEDDATE, OPENPRICE from DAILYSTOCKDATA where stockname='" + stockCode + "' order by tradeddate desc;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				objDailyStockData = new DailyStockData();
				objDailyStockData.closePrice = Float.parseFloat(resultSet.getString(1));
				objDailyStockData.highPrice =  Float.parseFloat(resultSet.getString(2));
				objDailyStockData.lowPrice =  Float.parseFloat(resultSet.getString(3));
				objDailyStockData.volume =  (long) Double.parseDouble(resultSet.getString(4));
				objDailyStockData.tradeddate =  resultSet.getString(5);
				objDailyStockData.openPrice =  Float.parseFloat(resultSet.getString(6));
				objDailyStockDataList.add(objDailyStockData);
			}
			return objDailyStockDataList;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getStockDetailsFromDBForDaily  -> ", ex);
			return null;
		}
	}
	
	private void insertBBToDB(String stockNSECode, String tradedDate, int period, float closingPrice, double SMA, double BBUpper, double BBLOwer, double bandwidth) {		
		Statement statement = null;
		String tmpSQL;
		
		try {
			if (connection == null) {
				connection = StockUtils.connectToDB();
			}			
			statement = connection.createStatement();
			tmpSQL = "INSERT INTO DAILYBOLLINGERBANDS (TRADEDDATE, STOCKNAME, PERIOD, CLOSINGPRICE, SMA, UPPERBAND, LOWERBAND, BANDWIDTH) VALUES('"
					+ tradedDate + "', '" + stockNSECode + "', " + period + ", " + closingPrice + ", " + SMA + ", " + BBUpper + ", " + BBLOwer + ", " + bandwidth + ");";
			statement.executeUpdate(tmpSQL);			
		} catch (Exception ex) {
			System.out.println("insertBBToDB Error in DB action ->"+ex);
			logger.error("Error in insertBBToDB  -> ", ex);
		}
	}	
	
	public void calculateBollingerBandsDaily(String stockCode) {
		ArrayList<DailyStockData> objDailyStockDataList;
		int counter = 1;
		float totalPrice = 0;
		String BBDate = null;
		double perioddeviation = 0;
        double BBLower = 0;
        double BBUper = 0;
        double periodBandwidth;
        float simpleMA, tmpvar;
        float closingPrice = 0;
        ArrayList<Float> periodData = null;
        ArrayList<Float> tmpPeriodData;     
        Date date = null;
         
		//date = new Date(System.currentTimeMillis()-1*24*60*60*1000);
		
		String bbPeriod = getBBPeriod(stockCode);
		if(bbPeriod == null) {
			logger.error("Null Bb Period for stock -> "+stockCode);
			System.out.println("Null Bb Period for stock -> "+stockCode);
			return;
		}
		String[] tmplist = bbPeriod.split(",");
		ArrayList<String> bbPeriodArray = new ArrayList<String> (Arrays.asList(tmplist));
		//ArrayList<String> tmpBBPeriodArray;
		System.out.println("Creating BB entry for stock -> " + stockCode);
		objDailyStockDataList = getStockDetailsFromDBForDaily(stockCode, date);
		if(objDailyStockDataList.size()>0) {			
			counter = 1;
			totalPrice = 0;
			//tmpBBPeriodArray = (ArrayList<String>) bbPeriodArray.clone();
			periodData = new ArrayList<Float>();
			for (DailyStockData objDailyStockData : objDailyStockDataList) {
				totalPrice = totalPrice + objDailyStockData.closePrice;
				if(counter==1) {
					BBDate = objDailyStockData.tradeddate;
					closingPrice = objDailyStockData.closePrice;
				}
				periodData.add(objDailyStockData.closePrice);
				if(bbPeriodArray.size()==0) {
					break;
				}
				if( bbPeriodArray.contains(counter+"") ) {	
					bbPeriodArray.remove(counter+"");
					perioddeviation = 0;
	                BBLower = 0;
	                BBUper = 0;
	                tmpPeriodData = new ArrayList<Float>();
	                simpleMA = totalPrice/counter;
	                for(int counter1 = 0; counter1<counter; counter1++) {
	                	tmpPeriodData.add(periodData.get(counter1)-simpleMA);
	                	tmpvar = tmpPeriodData.get(counter1) * tmpPeriodData.get(counter1); 
	                	tmpPeriodData.set(counter1, tmpvar);
	                	perioddeviation = perioddeviation + tmpPeriodData.get(counter1);
	                }
	                perioddeviation = perioddeviation / counter;
	                perioddeviation = Math.sqrt(perioddeviation);
	                BBLower = simpleMA - 2 * perioddeviation;
	                BBUper = simpleMA + 2 * perioddeviation;
	                periodBandwidth = BBUper - BBLower;
	                insertBBToDB(stockCode, BBDate, counter, closingPrice, simpleMA, BBUper, BBLower, periodBandwidth);
				}			
				counter++;	
			}
		} else {
			System.out.println("Quote size is 0 for stock -> "+stockCode);
			
		}
		System.out.println("Test");
	}
	
	public String getBBIndicationForStock(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<Float> dailyBandwidth = new ArrayList<Float>();
		String bbContracting = "Contracting";
		float BBcontractingPercentage;
		String tmpSQL;
		boolean onedaydeviation = false;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();	
			tmpSQL = "SELECT first 5 BANDWIDTH from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				dailyBandwidth.add(Float.parseFloat(resultSet.getString(1)));
			}
			for(int counter = 0; counter< dailyBandwidth.size()-1; counter++) {
				BBcontractingPercentage = (dailyBandwidth.get(counter) - dailyBandwidth.get(counter+1))/dailyBandwidth.get(counter+1);
				if(dailyBandwidth.get(counter) > dailyBandwidth.get(counter+1)){
					if(BBcontractingPercentage <= ACCEPTED_PERCENTAGE_DEVIATION) {
						if(onedaydeviation) {
							bbContracting = "expanding";
							break;
						}
						onedaydeviation = true;
					} else {
						bbContracting = "expanding";
						break;	
					}
				} else {
				}
			}
			return bbContracting;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			logger.error("Error in getBBIndicationForStock  -> ", ex);
			return "expanding";
		}
	}
}
