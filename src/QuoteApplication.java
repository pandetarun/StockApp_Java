import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class QuoteApplication extends SetupBase {
	final String URL = "https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm";
	final String timeOut = "2000";

	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("quote")) {
				CollectDailyStockData obj = new CollectDailyStockData();
				obj.startCollectingDailyData();
			} else if (args[0].equalsIgnoreCase("movingaveragecalculation")) {
				CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
				obj.MovingAverageCalculation();
			} else if (args[0].equalsIgnoreCase("movingaverageindicator")) {
				GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
				obj.CalculateAndSendIndicationfromSMA();
			}
		} else {
			System.out.println("No Args specified");
		}

	}
}
