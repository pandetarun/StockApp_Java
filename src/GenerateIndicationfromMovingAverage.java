import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class GenerateIndicationfromMovingAverage {
	Connection connection = null;
	public static int daysToCheck = 3;
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateAndSendIndicationfromSMA();
	}
	
	public void CalculateAndSendIndicationfromSMA() {
		ArrayList<String> stocklist = null;
		
		stocklist = getStockListFromDB();
		for (String stock: stocklist) {
			CalculateIndicationfromSMA(stock);
			
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
	
	private void CalculateIndicationfromSMA(String stockCode) {
		ArrayList<Integer> prefPeriod = null;
		ArrayList<Float> lowerSMAPeriodValues = null;
		ArrayList<Float> middleSMAPeriodValues = null;
		ArrayList<Float> higherSMAPeriodValues = null;
		ArrayList<Float> stockPriceValues = null;
		
		prefPeriod = GetPreferredSMA(stockCode);
		
		lowerSMAPeriodValues = GetSMAData(stockCode,prefPeriod.get(0));
		middleSMAPeriodValues = GetSMAData(stockCode,prefPeriod.get(1));
		higherSMAPeriodValues = GetSMAData(stockCode,prefPeriod.get(2));
		stockPriceValues = GetStockPrices(stockCode);
		
		calculateIndicationFromMiddleSMAAndPrice(middleSMAPeriodValues, stockPriceValues);
		calculateIndicationFromLowerSMAAndHigherSMA(lowerSMAPeriodValues, higherSMAPeriodValues);
	}
	
	private ArrayList<Integer> GetPreferredSMA(String stockCode) {
		ArrayList<Integer> prefPeriod = null;		
		ResultSet resultSet = null;
		Statement statement = null;
		String[] prefPeriodsInDB;
		
		try {
			prefPeriod = new ArrayList<Integer>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT PREFDAILYSMAPERIODS FROM STOCKWISEPERIODS;");
			while (resultSet.next()) {
				prefPeriodsInDB = resultSet.getString(1).split(",");
				for(int counter = 0; counter < prefPeriodsInDB.length; counter++) {
					prefPeriod.add(new Integer(prefPeriodsInDB[counter]));
				}
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			connection.close();
			connection = null;
		} catch (Exception ex) {
			System.out.println("Error in getting preferred period from DB" + ex);
			return null;
		}		
		return prefPeriod;		
	}
	
	private ArrayList<Float> GetSMAData(String stockCode, Integer period) {
		ArrayList<Float> SMAData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String SMAvalue;
		
		try {
			SMAData = new ArrayList<Float>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT first 20 SMA FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = " + period.intValue() + " order by tradeddate desc;");
			while (resultSet.next()) {
				SMAvalue = resultSet.getString(1);
				SMAData.add(Float.parseFloat(SMAvalue));
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			connection.close();
			connection = null;			
		} catch (Exception ex) {
			System.out.println("Error in getting SMA values for period = "+period +" error = "+ex);
			return null;
		}
		return SMAData;		
	}
	
	private ArrayList<Float> GetStockPrices(String stockCode) {
		ArrayList<Float> priceData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String price;
		
		try {
			priceData = new ArrayList<Float>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8",
					"SYSDBA", "Jan@2017");
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT first 20 closeprice FROM DAILYSTOCKDATA where stockname='" + stockCode + "' order by tradeddate desc;");
			while (resultSet.next()) {
				price = resultSet.getString(1);
				priceData.add(Float.parseFloat(price));
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			connection.close();
			connection = null;			
		} catch (Exception ex) {
			System.out.println("Error in getting price = "+ex);
			return null;
		}
		return priceData;		
	}

	private void calculateIndicationFromMiddleSMAAndPrice(ArrayList<Float> middleSMAPeriodValues, ArrayList<Float> stockPriceValues) {
		int positiveCounter = 0;
		float nextDayPrice = 0;
		float priceToSMADifference = 0;
		float twoDaysPriceDifference = 0;
		float lastdayPrice = 0;
		float perentagePriceChange = 0;
		
		lastdayPrice = stockPriceValues.get(0);
		for(int counter =0; counter<middleSMAPeriodValues.size(); counter++) {
			
			priceToSMADifference = stockPriceValues.get(counter)-middleSMAPeriodValues.get(counter);
			if(nextDayPrice==0){
				twoDaysPriceDifference = 0;
			} else {
				twoDaysPriceDifference = nextDayPrice - stockPriceValues.get(counter);
			}
			if(priceToSMADifference>0 && twoDaysPriceDifference>=0) {
				positiveCounter++;
			} else {
				break;
			}
			if(positiveCounter >= daysToCheck) {
				//Generate buy indicator
				
				//percentage Price change will help in ranking the selected stock. More the % change more higher the ranking
				perentagePriceChange = (lastdayPrice - stockPriceValues.get(counter)) / lastdayPrice;
				break;
			}
			nextDayPrice = stockPriceValues.get(counter);
		}
		
		positiveCounter = 0;
		nextDayPrice = 0;
		for(int counter =0; counter<middleSMAPeriodValues.size(); counter++) {
			priceToSMADifference = stockPriceValues.get(counter)-middleSMAPeriodValues.get(counter);
			if(nextDayPrice==0){
				twoDaysPriceDifference = 0;
			} else {
				twoDaysPriceDifference = nextDayPrice - stockPriceValues.get(counter);
			}
			if(priceToSMADifference<0 && twoDaysPriceDifference<=0) {
				positiveCounter++;
			} else {
				break;
			}
			if(positiveCounter >= daysToCheck) {
				//Generate put indicator
				
				//percentage Price change will help in ranking the selected stock. More the % change more higher the ranking
				perentagePriceChange = (lastdayPrice - stockPriceValues.get(counter)) / lastdayPrice;
				break;
			}
			nextDayPrice = stockPriceValues.get(counter);
		}		
	}

	private void calculateIndicationFromLowerSMAAndHigherSMA(ArrayList<Float> lowerSMAPeriodValues, ArrayList<Float> higherSMAPeriodValues) {		
		int positiveCounter = 0;
		float differenceInSMA = 0;
		float nextDayLowerSMA = 0;
		float twoDaysLowerSMADIfference = 0;
		
		for(int counter =0; counter<lowerSMAPeriodValues.size(); counter++) {
			differenceInSMA = lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter);
			if(nextDayLowerSMA==0){
				twoDaysLowerSMADIfference = 0;
			} else {
				twoDaysLowerSMADIfference = nextDayLowerSMA - lowerSMAPeriodValues.get(counter);
			}
			if (differenceInSMA > 0 && twoDaysLowerSMADIfference >= 0) {				
				positiveCounter++;
			} else {
				break;
			}
			if(positiveCounter >= daysToCheck) {
				//Generate buy indicator
				break;
			}
			nextDayLowerSMA = lowerSMAPeriodValues.get(counter);
		}
		
		positiveCounter = 0;
		nextDayLowerSMA = 0;
		for(int counter =0; counter<lowerSMAPeriodValues.size(); counter++) {
			differenceInSMA = lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter);
			if(nextDayLowerSMA==0){
				twoDaysLowerSMADIfference = 0;
			} else {
				twoDaysLowerSMADIfference = nextDayLowerSMA - lowerSMAPeriodValues.get(counter);
			}
			if (differenceInSMA < 0 && twoDaysLowerSMADIfference <= 0) {
				positiveCounter++;
			} else {
				break;
			}
			if(positiveCounter >= daysToCheck) {
				//Generate put indicator
				break;
			}
		}
		
	}

}
