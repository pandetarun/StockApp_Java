import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class QuoteApplication {
	final String URL = "https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm";
	final String timeOut = "2000";
	static Logger logger = Logger.getLogger(QuoteApplication.class);
	public static void main(String[] args) {
		String log4jConfigFile = System.getProperty("user.dir")
				+ File.separator + "log4j.properties";
		PropertyConfigurator.configure(log4jConfigFile);

		logger.debug("Java Program Called");
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("test")) {
				logger.debug("test called");
			}
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
		logger.debug("Java Program Called");
	}
}
