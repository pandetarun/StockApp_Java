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
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateAndSendIndicationfromSMA();
	}

	public void generateCombinedIndicationForStocks() {
		ArrayList<String> stocklist = null;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
		Date todayDate = new Date();
		int counter = 0;
		if(todayDate.getDay() == 0 || todayDate.getDay() == 6)
			return;
		
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateIndicationfromSMA();
		SMAIndicatorDetailsList = obj.getIndicationStocks();
		SMAIndicatorDetailsBelowHundredList = obj.getBelowHunderdIndicationStocks();
		for(SMAIndicatorDetails objSMAIndicatorDetails : SMAIndicatorDetailsList) {
			if(counter >10) break;
		}
		
	}
}
