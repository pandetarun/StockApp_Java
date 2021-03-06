package tarun.stockApp.TechnicalIndicator.Calculations;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class QuoteApplication {
	final String URL = "https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm";
	final String timeOut = "2000";
	static Logger logger = Logger.getLogger(QuoteApplication.class);
	public static void main(String[] args) {
//		String log4jConfigFile = System.getProperty("user.dir")
//				+ File.separator + "log4j.properties";
		String log4jConfigFile = System.getProperty("log4j.configuration");
		System.out.println("*************Prop -> "+log4jConfigFile);
		PropertyConfigurator.configure(log4jConfigFile);

		logger.debug("QuoteApplication Started");
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("test")) {
				logger.debug("test called");
			}
			if (args[0].equalsIgnoreCase("quote")) {
				logger.debug("Daily Quote Collection Started");
				CollectDailyStockData obj = new CollectDailyStockData();
				obj.startCollectingDailyData();
				logger.debug("Daily Quote Collection End");
			} else if (args[0].equalsIgnoreCase("movingaveragecalculation")) {
				logger.debug("MA Calculation Started");
				CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
				obj.MovingAverageCalculation();
				logger.debug("MA Calculation End");
			} else if (args[0].equalsIgnoreCase("movingaverageindicator")) {
				logger.debug("MA Indication Started");
				GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
				obj.CalculateAndSendIndicationfromSMA();
				logger.debug("MA Indication End");
			} else if (args[0].equalsIgnoreCase("volumeindicator")) {
				logger.debug("Volume Indication Started");
				OnBalanceVolumeUpdated obj = new OnBalanceVolumeUpdated();
				obj.OnBalanceVolumeCalculation();
				logger.debug("Volume Indication End");
			} else if (args[0].equalsIgnoreCase("calculateBB")) {
				logger.debug("calculateBB Started");
				CalculateBollingerBands obj = new CalculateBollingerBands();
				obj.calculateBollingerBands();
				logger.debug("calculateBB End");
			} else if (args[0].equalsIgnoreCase("combined")) {
				logger.debug("Combined Indication Started");
				GenerateCombinedIndication obj = new GenerateCombinedIndication();
				obj.generateCombinedIndicationForStocks();
				logger.debug("Combined Indication End");
			}  else if (args[0].equalsIgnoreCase("calculateRSI")) {
				logger.debug("CalculateRSIIndicator Started");
				CalculateRSIIndicator obj = new CalculateRSIIndicator();
				obj.CalculateRSIForAllStocks();
				logger.debug("CalculateRSIIndicator End");
			} else if (args[0].equalsIgnoreCase("calculateStochastic")) {
				logger.debug("CalculateStochasticIndicator Started");
				CalculateStochasticOscillator obj = new CalculateStochasticOscillator();
				obj.CalculateStochasticOscillatorForAllStocks();
				logger.debug("CalculateStochasticIndicator End");
			} else if (args[0].equalsIgnoreCase("calculateATR")) {
				logger.debug("CalculateATR Started");
				CalculateAverageTrueRange obj = new CalculateAverageTrueRange();
				obj.calculateAverageTrueRangeForAllStocks();
				logger.debug("CalculateATR End");
			}
		} else {
			System.out.println("No Args specified");
		}
		logger.debug("QuoteApplication end");
	}
}
