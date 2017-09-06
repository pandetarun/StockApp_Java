import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class GenerateCombinedIndication {
	Connection connection = null;
	public static int daysToCheck = 5;
	
	SMAIndicatorDetails objSMAIndicatorDetails;
	String stockName;
	String bseCode;
	String nseCode;
	static Logger logger = Logger.getLogger(CalculateSimpleAndExpoMovingAvg.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateCombinedIndication obj = new GenerateCombinedIndication();
		obj.generateCombinedIndicationForStocks();
	}

	public void generateCombinedIndicationForStocks() {
		logger.debug("generateCombinedIndicationForStocks Started");
		ArrayList<String> stocklist = null;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
		CalculateOnBalanceVolume objCalculateOnBalanceVolume;
		OnBalanceVolumeIndicator objOnBalanceVolumeIndicator;
		CalculateBollingerBands objCalculateBollingerBands;
		Date todayDate = new Date();
		
		String bbIndicator;
		ArrayList<FinalSelectedStock> objFinalSelectedStockList = new ArrayList<FinalSelectedStock>();
		ArrayList<FinalSelectedStock> objFinalSelectedBelowHundredStockList = new ArrayList<FinalSelectedStock>();
		FinalSelectedStock objFinalSelectedStock = null;
		FinalSelectedStock objFinalSelectedBelowHunderdStock;
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateIndicationfromSMA();
		SMAIndicatorDetailsList = obj.getIndicationStocks();
		SMAIndicatorDetailsBelowHundredList = obj.getBelowHunderdIndicationStocks();
		for(int counter = 0; counter<=20; counter++){
			if(SMAIndicatorDetailsList.size() > counter) {
				//add selected stock				
				objFinalSelectedStock = getAlldetails(SMAIndicatorDetailsList.get(counter));
				objFinalSelectedStockList.add(objFinalSelectedStock);
				//add Selected stock end
			} else {
				break;
			}
		}
		//Send top stock in mail
		sendTopStockInMail(objFinalSelectedStockList, false);
		
		for(int counter = 0; counter<=20; counter++){			
			if(SMAIndicatorDetailsBelowHundredList.size() > counter) {
				objFinalSelectedBelowHunderdStock = new FinalSelectedStock();
				objFinalSelectedBelowHunderdStock.stockCode = SMAIndicatorDetailsBelowHundredList.get(counter).stockCode;
				if(objFinalSelectedStockList.contains(objFinalSelectedBelowHunderdStock)) {
					objFinalSelectedBelowHunderdStock = objFinalSelectedStockList.get(objFinalSelectedStockList.indexOf(objFinalSelectedBelowHunderdStock));
					objFinalSelectedBelowHundredStockList.add(objFinalSelectedBelowHunderdStock);
				} else {
					objFinalSelectedBelowHunderdStock = getAlldetails(SMAIndicatorDetailsBelowHundredList.get(counter));						
					objFinalSelectedBelowHundredStockList.add(objFinalSelectedBelowHunderdStock);
				}
			} else {
				break;
			}
		}
		//Send top below 100 stock in mail
		sendTopStockInMail(objFinalSelectedBelowHundredStockList, true);
		logger.debug("generateCombinedIndicationForStocks End");
	}
	
	private FinalSelectedStock getAlldetails (SMAIndicatorDetails objSMAIndicatorDetails) {
		FinalSelectedStock objFinalSelectedStock = null;
		CalculateOnBalanceVolume objCalculateOnBalanceVolume;
		OnBalanceVolumeIndicator objOnBalanceVolumeIndicator;
		CalculateBollingerBands objCalculateBollingerBands;
		String bbIndicator;
		
		objFinalSelectedStock = new FinalSelectedStock();
		//add selcted stock
		objCalculateOnBalanceVolume = new CalculateOnBalanceVolume();
		objOnBalanceVolumeIndicator = objCalculateOnBalanceVolume.calculateOnBalanceVolumeDaily(objSMAIndicatorDetails.stockCode);
		
		objCalculateBollingerBands = new CalculateBollingerBands();
		bbIndicator = objCalculateBollingerBands.getBBIndicationForStock(objSMAIndicatorDetails.stockCode);
		
		objFinalSelectedStock.stockCode = objSMAIndicatorDetails.stockCode;
		objFinalSelectedStock.stockPrice = objSMAIndicatorDetails.stockPrice;
		objFinalSelectedStock.tradeddate = objSMAIndicatorDetails.signalDate;
		objFinalSelectedStock.percentagePriceChange = objSMAIndicatorDetails.percentagePriceChange;
		objFinalSelectedStock.PNSMAcrossover = objSMAIndicatorDetails.PNSMAcrossover;
		objFinalSelectedStock.SMNSMcrossover = objSMAIndicatorDetails.SMNSMcrossover;
		objFinalSelectedStock.percentageChangeInVolumeInLastDay = objOnBalanceVolumeIndicator.percentageChangeInLastDay;
		objFinalSelectedStock.BBIndicator = bbIndicator;
		
		return objFinalSelectedStock;
	}
	
	private void sendTopStockInMail(ArrayList<FinalSelectedStock> objFinalSelectedStockList, Boolean belowHunderd) {
		logger.debug("sendTopStockInMail Started");
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>Stock Price</th><th>9 to 50 SM Cross</th><th>Price crossed 20 SMA</th><th>% Price Change</th><th>% Volume Increase</th><th>BB Indication</th>"
				+ "<th>RSI Indication</th><th>MACD Indication</th><th>Stochastic Oscillator</th><th>Accumulation/ Distribution Line</th></tr>");
		
		for (int counter = 0; counter <(objFinalSelectedStockList.size()>20?20:objFinalSelectedStockList.size()); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).tradeddate + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).stockCode + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).stockPrice + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).SMNSMcrossover + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).PNSMAcrossover + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).percentagePriceChange + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).percentageChangeInVolumeInLastDay + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).BBIndicator + "</td>");
			mailBody.append("<td>" + "</td>");
			mailBody.append("<td>" +  "</td>");
			mailBody.append("<td>" +  "</td></tr>");
		}
		mailBody.append("</table></body></html>");
		SendSuggestedStockInMail mailSender;
        if(belowHunderd && objFinalSelectedStockList.size() > 0) {
        	mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","Combined -> Below 100 Stocklist on "+objFinalSelectedStockList.get(0).tradeddate.toString(),mailBody.toString());
        } else if( objFinalSelectedStockList.size() > 0 ){
        	mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","Combined -> Stocklist on "+objFinalSelectedStockList.get(0).tradeddate.toString(),mailBody.toString());
        }
        logger.debug("sendTopStockInMail end");
	}
}
