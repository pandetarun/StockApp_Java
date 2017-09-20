package tarun.stockApp.TechnicalIndicator.Calculations;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import tarun.stockApp.TechnicalIndicator.Data.OnBalanceIndicatorComparator;
import tarun.stockApp.TechnicalIndicator.Data.OnBalanceVolumeData;

public class OnBalanceVolumeUpdated {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateOnBalanceVolume.class);	
	String stockName;
	String bseCode;
	String nseCode;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateOnBalanceVolume Started");
		System.out.println("Start at -> " + dte.toString());
		OnBalanceVolumeUpdated obj = new OnBalanceVolumeUpdated();
		obj.OnBalanceVolumeCalculation();
		//dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateOnBalanceVolume End");
	}
	
	public void OnBalanceVolumeCalculation() {
		ArrayList<String> stockList = null;
		Date todayDate = new Date();
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		stockList = StockUtils.getStockListFromDB();
		ArrayList<OnBalanceVolumeIndicator> onBalanceSelectedStockList = new ArrayList<OnBalanceVolumeIndicator>();
		ArrayList<OnBalanceVolumeIndicator> onBalanceSelectedBelowHundredStockList = new ArrayList<OnBalanceVolumeIndicator>();
		OnBalanceVolumeIndicator tmpOnBalanceVolumeIndicator;
		int counter = 1;
		
		for (String stockCode : stockList) {
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Processing Stock -> "+ stockName + " count -> "+counter);
			if(StockUtils.getFinancialIndication(bseCode)) {
				tmpOnBalanceVolumeIndicator = calculateOnBalanceVolumeDaily(stockCode);
				if (tmpOnBalanceVolumeIndicator!=null) {
					System.out.println("******************Stock Added -> "+ stockName);
					if(tmpOnBalanceVolumeIndicator.stockPrice<100) {
						onBalanceSelectedBelowHundredStockList.add(tmpOnBalanceVolumeIndicator);
					} else {
						onBalanceSelectedStockList.add(tmpOnBalanceVolumeIndicator);
					}
				}
			}
			//if(counter>100) break;
			counter++;
		}		

		Collections.sort(onBalanceSelectedStockList, new OnBalanceIndicatorComparator());
		Collections.sort(onBalanceSelectedBelowHundredStockList, new OnBalanceIndicatorComparator());
		if(onBalanceSelectedStockList.size()>0) sendTopStockInMail(onBalanceSelectedStockList, false);
		if(onBalanceSelectedBelowHundredStockList.size()>0) sendTopStockInMail(onBalanceSelectedBelowHundredStockList, true);
	}
	
	private OnBalanceVolumeIndicator calculateOnBalanceVolumeDaily(String stockCode) {
		OnBalanceVolumeData stockDetails = null;
		boolean continuousVolumeIncrease = true;
		
		stockDetails = getStockDetailsFromDBDaily(stockCode.split("!")[2]);
		if (stockDetails == null || stockDetails.tradeddate == null) {
			System.out.println("stock details null for - > "+stockCode.split("!")[1]);
		}		
		//if last day volume is down then do not add stock to list		
		if(stockDetails.volume.get(stockDetails.volume.size()-1) < stockDetails.volume.get(stockDetails.volume.size()-2)) {
			return null;
		}
		for (int counter = stockDetails.volume.size()-2; counter > 0 ; counter--) {
			if (stockDetails.volume.get(counter) < stockDetails.volume.get(counter-1)) {
				continuousVolumeIncrease = false;
				break;
			}
		}
		OnBalanceVolumeIndicator tmpOnBalanceVolumeIndicator = new OnBalanceVolumeIndicator();
		tmpOnBalanceVolumeIndicator.stockName = stockCode.split("!")[1];
		tmpOnBalanceVolumeIndicator.tradeddate = stockDetails.tradeddate.get(stockDetails.tradeddate.size()-1);
		tmpOnBalanceVolumeIndicator.volumeChangeInLastDay = stockDetails.volume.get(stockDetails.volume.size()-1) - stockDetails.volume.get(stockDetails.volume.size()-2);
		tmpOnBalanceVolumeIndicator.continuousIncreasedVolume = continuousVolumeIncrease;
		tmpOnBalanceVolumeIndicator.percentageChangeInLastDay = ((stockDetails.volume.get(stockDetails.volume.size()-1) - stockDetails.volume.get(stockDetails.volume.size()-2))/stockDetails.volume.get(stockDetails.volume.size()-2)) * 100;		
		tmpOnBalanceVolumeIndicator.percentageChangeInLastFewDay = ((stockDetails.volume.get(stockDetails.volume.size()-1) - stockDetails.volume.get(0))/stockDetails.volume.get(0))*100;
		tmpOnBalanceVolumeIndicator.stockPrice = stockDetails.closePrice.get(stockDetails.closePrice.size()-1);
		
		return tmpOnBalanceVolumeIndicator;
	}

	private OnBalanceVolumeData getStockDetailsFromDBDaily(String stockCode) {
		
		String tradedDate;
		Float closePrice;
		long volume;
		String tmpSQL;
		OnBalanceVolumeData onBalanceVolumeDataObj = null;
		ResultSet resultSet = null;
		Statement statement = null;	
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}			
			onBalanceVolumeDataObj = new OnBalanceVolumeData();
			onBalanceVolumeDataObj.closePrice = new ArrayList<Float>();
			onBalanceVolumeDataObj.tradeddate = new ArrayList<String>();
			onBalanceVolumeDataObj.volume = new ArrayList<Long>();
			onBalanceVolumeDataObj.onBalanceVolume = new ArrayList<Long>();			
			onBalanceVolumeDataObj.stockName = stockCode;
			tmpSQL = "Select * from (SELECT first 5 tradeddate, closeprice, volume FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' order by tradeddate desc ) order by TRADEDDATE;";
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(tmpSQL);			
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				volume = BigDecimal.valueOf(resultSet.getDouble(3)).longValue();
				onBalanceVolumeDataObj.closePrice.add(closePrice);
				onBalanceVolumeDataObj.tradeddate.add(tradedDate);
				onBalanceVolumeDataObj.volume.add(volume);
			}
			resultSet.close();
			connection.close();
			connection = null;
			return onBalanceVolumeDataObj;
		} catch (Exception ex) {
			try{
				System.out.println("Error in DB action Date = " + resultSet.getString(1));
			} catch(Exception ex1) { }
			logger.error("Error in getStockDetailsFromDB  -> ", ex);
			return null;
		}
	}
	
	private void sendTopStockInMail(ArrayList<OnBalanceVolumeIndicator> onBalanceVolumeIndicatorList, Boolean belowHunderd) {
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>% Change In Last Day</th><th>% Change In Last Few Days</th><th>Volume Continuous Growth</th></tr>");
		
		for (int counter = 0; counter <(onBalanceVolumeIndicatorList.size()>20?20:onBalanceVolumeIndicatorList.size()); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + onBalanceVolumeIndicatorList.get(counter).tradeddate + "</td>");
			mailBody.append("<td>" + onBalanceVolumeIndicatorList.get(counter).stockName + "</td>");
			mailBody.append("<td>" + onBalanceVolumeIndicatorList.get(counter).percentageChangeInLastDay + "</td>");
			mailBody.append("<td>" + onBalanceVolumeIndicatorList.get(counter).percentageChangeInLastFewDay + "</td>");
			mailBody.append("<td>" + onBalanceVolumeIndicatorList.get(counter).continuousIncreasedVolume + "</td></tr>");
		}
		mailBody.append("</table></body></html>");
        if(belowHunderd) {
        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com","OBV -> Below 100 Stocklist on "+onBalanceVolumeIndicatorList.get(0).tradeddate.toString(),mailBody.toString());
        } else {
        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com","OBV -> Stocklist on "+onBalanceVolumeIndicatorList.get(0).tradeddate.toString(),mailBody.toString());
        }
        
	}
}
