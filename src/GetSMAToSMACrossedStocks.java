import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class GetSMAToSMACrossedStocks {
	Connection connection = null;
	public static int daysToCheck = 2;
	public static int LOWER_SMA = 50;
	public static int HIGHER_SMA = 200;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
	SMAIndicatorDetails objSMAIndicatorDetails;
	String stockName;
	String bseCode;
	String nseCode;
	static Logger logger = Logger.getLogger(GetSMAToSMACrossedStocks.class);
	
	public static void main(String[] args) {
		logger.debug("GetSMAToSMACrossedStocks main started");
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GetSMAToSMACrossedStocks obj = new GetSMAToSMACrossedStocks();
		obj.CalculateIndicationfromSMA();
		logger.debug("GetSMAToSMACrossedStocks main end");
	}

	public void CalculateIndicationfromSMA() {
		logger.debug("CalculateIndicationfromSMA start");
		ArrayList<String> stocklist = null;
		Date todayDate = new Date();
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		//UpdateIndicatedStocks tmpUpdateIndicatedStocks = new UpdateIndicatedStocks();
		stocklist = StockUtils.getStockListFromDB();
		SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
		SMAIndicatorDetailsBelowHundredList = new ArrayList<SMAIndicatorDetails>();
		int stockcounter = 1;
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		for (String stock : stocklist) {
			stockName = stock.split("!")[1];
			bseCode = stock.split("!")[0];
			nseCode = stock.split("!")[2];
			System.out.println("For Stock -> " + nseCode + " Stock count -> " + stockcounter++);
			if(StockUtils.getFinancialIndication(bseCode)) {	
				objSMAIndicatorDetails = new SMAIndicatorDetails();
				objSMAIndicatorDetails.stockCode = nseCode;
				objSMAIndicatorDetails.signalDate = LocalDate.now();
				
				CalculateIndicationfromSMA(nseCode);
				if (objSMAIndicatorDetails.SMNSMcrossover) {
					System.out.println("*****************************Stock Added for indication -> " + nseCode);
					SMAIndicatorDetailsList.add(objSMAIndicatorDetails);
					if(objSMAIndicatorDetails.stockPrice<100) {
						SMAIndicatorDetailsBelowHundredList.add(objSMAIndicatorDetails);
					}
				}
			}
			/*if (stockcounter > 200) {
				break;
			}*/
		}
		logger.debug("CalculateAndSendIndicationfromSMA calculation completed");
		// Collections.sort(SMAIndicatorDetailsList);
		logger.debug("CalculateAndSendIndicationfromSMA start mail");
		//Collections.sort(SMAIndicatorDetailsList, new SMAIndicatorDetailsComparator());
		//Collections.sort(SMAIndicatorDetailsBelowHundredList, new SMAIndicatorDetailsComparator());
		if(SMAIndicatorDetailsList.size()>0) {
			sendTopStockInMail(SMAIndicatorDetailsList, false);
			sendTopStockInMail(SMAIndicatorDetailsBelowHundredList, true);
		} else {
			logger.error("CalculateAndSendIndicationfromSMA No stock to send in mail");
		}
		//tmpUpdateIndicatedStocks.updateSMAIndication(SMAIndicatorDetailsList);
		logger.debug("CalculateIndicationfromSMA end");
		System.out.println("End");
	}
	
	private void CalculateIndicationfromSMA(String stockCode) {
		ArrayList<Float> lowerSMAPeriodValues = null;
		ArrayList<Float> higherSMAPeriodValues = null;
		ArrayList<Float> stockPriceValues = null;
		lowerSMAPeriodValues = GetSMAData(stockCode, LOWER_SMA);
		higherSMAPeriodValues = GetSMAData(stockCode, HIGHER_SMA);			
		if (lowerSMAPeriodValues.size() > 0 && higherSMAPeriodValues.size() > 0) {
			calculateIndicationFromLowerSMAAndHigherSMAV1(lowerSMAPeriodValues, higherSMAPeriodValues, stockPriceValues);
		}
	}
	
	private ArrayList<Float> GetSMAData(String stockCode, Integer period) {
		ArrayList<Float> SMAData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String SMAvalue;

		try {
			SMAData = new ArrayList<Float>();
			connection = StockUtils.connectToDB();
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
			System.out.println("Error in getting SMA values for period = " + period + " error = " + ex);
			return null;
		}
		return SMAData;
	}
	
	private void calculateIndicationFromLowerSMAAndHigherSMAV1(ArrayList<Float> lowerSMAPeriodValues, ArrayList<Float> higherSMAPeriodValues, ArrayList<Float> stockPriceValues) {

		float lowerLevelDifference = 0;
		float SMAToSMAPercentageDeviation = 0;
		if (lowerSMAPeriodValues.size() > daysToCheck && higherSMAPeriodValues.size() > daysToCheck) {
			//check if lower SA crossed higher SMA in today or in last 2 days
			//last day lower SMA is greater than higher SMA while higher SMQ was more in previous days
			if((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0))>0 && ((lowerSMAPeriodValues.get(1) - higherSMAPeriodValues.get(1)<0) || (lowerSMAPeriodValues.get(2) - higherSMAPeriodValues.get(2))<0) ) {
				objSMAIndicatorDetails.SMNSMcrossover = true;
			}
			if ((lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))<0) {
				lowerLevelDifference = 1;
			} else {
				lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1);
			}
			SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))) / lowerLevelDifference;
			objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
			
		}
	}
	
	private void sendTopStockInMail(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList, Boolean belowHunderd) {
		logger.debug("sendTopStockInMail Started");
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>signalSMAToSMA</th><th>SMNSMcrossover</th><th>SMNSMcontinuousGrowth</th><th>SMAToSMApercentageDeviation</th><th>signalPriceToSMA</th><th>PNSMAcrossover</th>"
				+ "<th>PNSMcontinuousGrowth</th><th>priceToSMApercentageDeviation</th><th>percentagePriceChange</th></tr>");
		
		for (int counter = 0; counter <(SMAIndicatorDetailsList.size()>20?20:SMAIndicatorDetailsList.size()); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalDate + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).stockCode + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalSMAToSMA + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMNSMcrossover + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMNSMcontinuousGrowth + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMAToSMApercentageDeviation + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalPriceToSMA + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).PNSMAcrossover + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).PNSMcontinuousGrowth + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).priceToSMApercentageDeviation + "</td>");
			//mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).percentagePriceChange + "</td></tr>");
		}
		mailBody.append("</table></body></html>");
		SendSuggestedStockInMail mailSender;
        if(belowHunderd && SMAIndicatorDetailsList.size() > 0) {
        	mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","SMA Cross over -> Below 100 Stocklist on "+SMAIndicatorDetailsList.get(0).signalDate.toString(),mailBody.toString());
        } else if( SMAIndicatorDetailsList.size() > 0 ){
        	mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","SMA Cross over -> Stocklist on "+SMAIndicatorDetailsList.get(0).signalDate.toString(),mailBody.toString());
        }
        logger.debug("sendTopStockInMail end");
	}
	
}
