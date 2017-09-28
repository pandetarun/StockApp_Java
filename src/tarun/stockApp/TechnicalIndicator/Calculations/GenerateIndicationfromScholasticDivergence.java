package tarun.stockApp.TechnicalIndicator.Calculations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import tarun.stockApp.TechnicalIndicator.Data.StochasticIndicatorComparator;
import tarun.stockApp.TechnicalIndicator.Data.StochasticOscillatorData;
import tarun.stockApp.TechnicalIndicator.Data.StochasticeIndicatorCalculationData;

public class GenerateIndicationfromScholasticDivergence {
	Connection connection = null;
	static Logger logger = Logger.getLogger(GenerateIndicationfromScholasticDivergence.class);
	public final static int DIVERGANCE_LONG_PERIOD = 40;
	public final static int DIVERGANCE_SHORT_PERIOD = 10;
	ArrayList<StochasticeIndicatorCalculationData> objStochasticeIndicatorDataList;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("GenerateIndicationfromScholasticDivergence Started");
		System.out.println("Start at -> " + dte.toString());
		GenerateIndicationfromScholasticDivergence obj = new GenerateIndicationfromScholasticDivergence();
		obj.calculateDivergenceForAllStocks();		
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("GenerateIndicationfromScholasticDivergence End");
	}
	
	public void calculateDivergenceForAllStocks() {
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
			calculateDivergenceForStock(nseCode, "20-May-2017");
		}
	}
	
	public void calculateDivergenceForStock(String stockCode, String targetDate) {
		StochasticOscillatorData stockDetails = null;
		float differenceVariance, priceDeclinePercentage;
		ArrayList<Float> highestHighArr, lowestLowArr;
		Comparator<Float> comparatorForLow = Collections.reverseOrder();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			} 
			connection = StockUtils.connectToDB();
			getStockDetailsFromDBDaily(stockCode, targetDate);
			System.out.print(".");
			if(objStochasticeIndicatorDataList!=null) {
				//ArrayList<StochasticeIndicatorCalculationData> tmpStochasticeIndicatorDataList =   (ArrayList<StochasticeIndicatorCalculationData>)objStochasticeIndicatorDataList.clone();
				ArrayList<StochasticeIndicatorCalculationData> tmpStochasticeIndicatorDataList =   new ArrayList<StochasticeIndicatorCalculationData>(objStochasticeIndicatorDataList.subList(0, DIVERGANCE_SHORT_PERIOD));
				Collections.sort(tmpStochasticeIndicatorDataList, new StochasticIndicatorComparator());
				
				priceDeclinePercentage = ((objStochasticeIndicatorDataList.get(objStochasticeIndicatorDataList.size()-1).closePrice - objStochasticeIndicatorDataList.get(0).closePrice)/objStochasticeIndicatorDataList.get(objStochasticeIndicatorDataList.size()-1).closePrice)*100;
				
				//for (int counter = 2; counter > 0; counter--) {
				if(tmpStochasticeIndicatorDataList.get(0).stochasticOscillator - tmpStochasticeIndicatorDataList.get(1).stochasticOscillator > 0) {
					differenceVariance = ((tmpStochasticeIndicatorDataList.get(0).stochasticOscillator - tmpStochasticeIndicatorDataList.get(1).stochasticOscillator)/tmpStochasticeIndicatorDataList.get(1).stochasticOscillator) * 100;
					
					if (differenceVariance>20 && priceDeclinePercentage>20) {
						System.out.println();
						System.out.println("Add stock for divergence -> "+stockCode+" for date -> "+tmpStochasticeIndicatorDataList.get(0).tradedDate+" and date -> "+tmpStochasticeIndicatorDataList.get(1).tradedDate);
					}
					
					if(tmpStochasticeIndicatorDataList.get(1).stochasticOscillator - tmpStochasticeIndicatorDataList.get(2).stochasticOscillator > 0) {
						differenceVariance = ((tmpStochasticeIndicatorDataList.get(1).stochasticOscillator - tmpStochasticeIndicatorDataList.get(2).stochasticOscillator)/tmpStochasticeIndicatorDataList.get(2).stochasticOscillator) * 100;
						if (differenceVariance>5 && priceDeclinePercentage>20) {
							System.out.println("******************** double Add stock for divergence -> "+stockCode+" for date -> "+tmpStochasticeIndicatorDataList.get(2).tradedDate);
						}
					
					}
					
				}
			}
			//}
		} catch (Exception ex) {
			System.out.println("calculateDivergenceForStock Error in DB action "+ex);
			logger.error("Error in calculateDivergenceForStock  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("calculateDivergenceForStock Error in DB action ");
				logger.error("Error in calculateDivergenceForStock  -> ", ex);
			}
		}
	}
	
	private void getStockDetailsFromDBDaily(String stockCode, String calculationDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice, highPrice, lowPrice;
		StochasticeIndicatorCalculationData objStochasticeIndicatorData = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		
		try {
			objStochasticeIndicatorDataList = new ArrayList<StochasticeIndicatorCalculationData>();
			statement = connection.createStatement();
			if(calculationDate!=null) {
				tmpSQL = "SELECT First " + DIVERGANCE_LONG_PERIOD + " DSD.tradeddate, DSD.closeprice, DSO.STOCHASTIC_OSCILLATOR from DAILYSTOCKDATA DSD, DAILY_STOCHASTIC_OSCILLATOR DSO where DSD.stockname='"
						+ stockCode + "' and DSO.stockname='" + stockCode + "' and DSD.tradeddate<='" + dateFormat.format(new Date(calculationDate)) +"' and DSD.tradeddate = DSO.tradeddate order by tradeddate desc;";			
			} else {
				tmpSQL = "SELECT First " + DIVERGANCE_LONG_PERIOD + " DSD.tradeddate, DSD.closeprice, DSO.STOCHASTIC_OSCILLATOR from DAILYSTOCKDATA DSD, DAILY_STOCHASTIC_OSCILLATOR DSO where DSD.stockname='"
						+ stockCode + "' and DSO.stockname='" + stockCode + "' and DSD.tradeddate = DSO.tradeddate order by tradeddate desc;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				objStochasticeIndicatorData = new StochasticeIndicatorCalculationData();
				objStochasticeIndicatorData.stockName = stockCode;
				objStochasticeIndicatorData.tradedDate = resultSet.getString(1);
				objStochasticeIndicatorData.closePrice = Float.parseFloat(resultSet.getString(2));
				objStochasticeIndicatorData.stochasticOscillator = Float.parseFloat(resultSet.getString(3));
				objStochasticeIndicatorDataList.add(objStochasticeIndicatorData);
			}
			//return soDataObj;
		} catch (Exception ex) {
			System.out.println("getStockDetailsFromDBForBulk -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForBulk  -> ", ex);
			//return null;
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
		}
	}
}
