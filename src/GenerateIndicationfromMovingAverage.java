import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class GenerateIndicationfromMovingAverage {
	Connection connection = null;
	public static int daysToCheck = 5;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
	SMAIndicatorDetails objSMAIndicatorDetails;

	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateAndSendIndicationfromSMA();
	}

	public void CalculateAndSendIndicationfromSMA() {
		ArrayList<String> stocklist = null;

		stocklist = getStockListFromDB();
		SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
		int stockcounter = 1;
		for (String stock : stocklist) {
			System.out.println("For Stock -> " + stock + " Stock count -> " + stockcounter++);
			objSMAIndicatorDetails = new SMAIndicatorDetails();
			objSMAIndicatorDetails.stockCode = stock;
			CalculateIndicationfromSMA(stock);
			if (objSMAIndicatorDetails.signalPriceToSMA != null || objSMAIndicatorDetails.signalSMAToSMA != null) {
				System.out.println("*****************************Stock Added for indication -> " + stock);
				SMAIndicatorDetailsList.add(objSMAIndicatorDetails);
			}
			if (stockcounter > 20) {
				break;
			}
		}
		// Collections.sort(SMAIndicatorDetailsList);
		Collections.sort(SMAIndicatorDetailsList, new SMAIndicatorDetailsComparator());
		System.out.println("End");
	}

	private ArrayList<String> getStockListFromDB() {

		ResultSet resultSet = null;
		Statement statement = null;
		String stockNSECode;
		ArrayList<String> stockList = null;

		try {
			stockList = new ArrayList<String>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8", "SYSDBA", "Jan@2017");
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
		if (prefPeriod != null && prefPeriod.size() > 0) {
			lowerSMAPeriodValues = GetSMAData(stockCode, prefPeriod.get(0));
			middleSMAPeriodValues = GetSMAData(stockCode, prefPeriod.get(1));
			higherSMAPeriodValues = GetSMAData(stockCode, prefPeriod.get(2));
			stockPriceValues = GetStockPrices(stockCode);

			if (middleSMAPeriodValues.size() > 0 && stockPriceValues.size() > 0) {
				calculateIndicationFromMiddleSMAAndPrice(middleSMAPeriodValues, stockPriceValues);
			}
			if (lowerSMAPeriodValues.size() > 0 && higherSMAPeriodValues.size() > 0) {
				calculateIndicationFromLowerSMAAndHigherSMA(lowerSMAPeriodValues, higherSMAPeriodValues);
			}
		}
	}

	private ArrayList<Integer> GetPreferredSMA(String stockCode) {
		ArrayList<Integer> prefPeriod = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String[] prefPeriodsInDB;

		try {
			prefPeriod = new ArrayList<Integer>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8", "SYSDBA", "Jan@2017");
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT PREFDAILYSMAPERIODS FROM STOCKWISEPERIODS where stockname = '" + stockCode + "';");
			while (resultSet.next()) {
				prefPeriodsInDB = resultSet.getString(1).split(",");
				for (int counter = 0; counter < prefPeriodsInDB.length; counter++) {
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
			connection = DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8", "SYSDBA", "Jan@2017");
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

	private ArrayList<Float> GetStockPrices(String stockCode) {
		ArrayList<Float> priceData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String price;

		try {
			priceData = new ArrayList<Float>();
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			connection = DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8", "SYSDBA", "Jan@2017");
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT first 20 closeprice, tradeddate FROM DAILYSTOCKDATA where stockname='" + stockCode + "' order by tradeddate desc;");
			objSMAIndicatorDetails.signalDate = null;
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				price = resultSet.getString(1);
				priceData.add(Float.parseFloat(price));
				if (objSMAIndicatorDetails.signalDate == null) {
					objSMAIndicatorDetails.signalDate = LocalDate.parse(resultSet.getString(2)); // new
																									// Date(dateFormat.format(resultSet.getString(2)));
				}
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			connection.close();
			connection = null;
		} catch (Exception ex) {
			System.out.println("Error in getting price = " + ex);
			return null;
		}
		return priceData;
	}

	private void calculateIndicationFromMiddleSMAAndPrice(ArrayList<Float> middleSMAPeriodValues, ArrayList<Float> stockPriceValues) {
//		int positiveCounter = 0;
//		float nextDayPrice = 0;
//		float priceToSMADifference = 0;
//		float twoDaysPriceDifference = 0;
		//float lastdayPrice = 0;
		float percentagePriceChange = 0;
		float priceToSMAPercentageDeviation = 0;
		float lowerLevelDifference = 0;
		//lastdayPrice = stockPriceValues.get(0);
		// Logic to get buy condition
		if (stockPriceValues.size() > daysToCheck && middleSMAPeriodValues.size() > daysToCheck) {
			if (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck) > 0) {
				if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) > stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck)) {
					objSMAIndicatorDetails.signalPriceToSMA = "buy";
					percentagePriceChange = (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck)) / stockPriceValues.get(daysToCheck);
					if ((stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck))<0) {
						lowerLevelDifference = 1;
					} else {
						lowerLevelDifference = stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck);
					}
					priceToSMAPercentageDeviation = ((stockPriceValues.get(0) - middleSMAPeriodValues.get(0)) - (stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck))) / lowerLevelDifference;
					objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
					objSMAIndicatorDetails.priceToSMApercentageDeviation = priceToSMAPercentageDeviation;
				}
			}
		}

		// Logic to get put condition
		if (stockPriceValues.size() > daysToCheck && middleSMAPeriodValues.size() > daysToCheck) {
			if (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck) < 0) {
				if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) < stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck)) {
					if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) < 0) {
						objSMAIndicatorDetails.signalPriceToSMA = "put";
						percentagePriceChange = (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck)) / stockPriceValues.get(daysToCheck);
						if ((stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck))<0) {
							lowerLevelDifference = 1;
						} else {
							lowerLevelDifference = stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck);
						}
						priceToSMAPercentageDeviation = ((stockPriceValues.get(0) - middleSMAPeriodValues.get(0)) - (stockPriceValues.get(daysToCheck) - middleSMAPeriodValues.get(daysToCheck))) / lowerLevelDifference;
						objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
						objSMAIndicatorDetails.priceToSMApercentageDeviation = priceToSMAPercentageDeviation;
					}
				}
			}
		}
		
		

		/*
		 * for(int counter =0; counter<middleSMAPeriodValues.size(); counter++)
		 * {
		 * 
		 * if(middleSMAPeriodValues.size()> counter && stockPriceValues.size() >
		 * counter){ priceToSMADifference =
		 * stockPriceValues.get(counter)-middleSMAPeriodValues.get(counter);
		 * if(nextDayPrice==0){ twoDaysPriceDifference = 0; } else {
		 * twoDaysPriceDifference = nextDayPrice -
		 * stockPriceValues.get(counter); } if(priceToSMADifference>0 &&
		 * twoDaysPriceDifference>=0) { positiveCounter++; } else { break; }
		 * if(positiveCounter >= daysToCheck) { //Generate buy indicator
		 * objSMAIndicatorDetails.signalPriceToSMA = "buy"; //percentage Price
		 * change will help in ranking the selected stock. More the % change
		 * more higher the ranking perentagePriceChange = (lastdayPrice -
		 * stockPriceValues.get(counter)) / lastdayPrice;
		 * objSMAIndicatorDetails.percentagePriceChange = perentagePriceChange;
		 * break; } nextDayPrice = stockPriceValues.get(counter); } else {
		 * break; } }
		 * 
		 * positiveCounter = 0; nextDayPrice = 0; for(int counter =0;
		 * counter<middleSMAPeriodValues.size(); counter++) {
		 * if(middleSMAPeriodValues.size()> counter && stockPriceValues.size() >
		 * counter){ priceToSMADifference =
		 * stockPriceValues.get(counter)-middleSMAPeriodValues.get(counter);
		 * if(nextDayPrice==0){ twoDaysPriceDifference = 0; } else {
		 * twoDaysPriceDifference = nextDayPrice -
		 * stockPriceValues.get(counter); } if(priceToSMADifference<0 &&
		 * twoDaysPriceDifference<=0) { positiveCounter++; } else { break; }
		 * if(positiveCounter >= daysToCheck) { //Generate put indicator
		 * objSMAIndicatorDetails.signalPriceToSMA="put"; //percentage Price
		 * change will help in ranking the selected stock. More the % change
		 * more higher the ranking perentagePriceChange = (lastdayPrice -
		 * stockPriceValues.get(counter)) / lastdayPrice;
		 * objSMAIndicatorDetails.percentagePriceChange = perentagePriceChange;
		 * break; } nextDayPrice = stockPriceValues.get(counter); } else {
		 * break; } }
		 */
	}

	private void calculateIndicationFromLowerSMAAndHigherSMA(ArrayList<Float> lowerSMAPeriodValues, ArrayList<Float> higherSMAPeriodValues) {
//		int positiveCounter = 0;
//		float differenceInSMA = 0;
//		float nextDayLowerSMA = 0;
//		float twoDaysLowerSMADIfference = 0;
		float lowerLevelDifference = 0;
		//float percentagePriceChange = 0;
		float SMAToSMAPercentageDeviation = 0;
		// Logic to get buy condition
		if (lowerSMAPeriodValues.size() > daysToCheck && higherSMAPeriodValues.size() > daysToCheck) {
			if ((lowerSMAPeriodValues.get(0) - lowerSMAPeriodValues.get(daysToCheck) > 0) && (higherSMAPeriodValues.get(0) - higherSMAPeriodValues.get(daysToCheck) > 0)) {
				if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) > lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck)) {
					objSMAIndicatorDetails.signalSMAToSMA = "buy";
					//percentagePriceChange = (lowerSMAPeriodValues.get(0) - stockPriceValues.get(daysToCheck)) / stockPriceValues.get(daysToCheck);
					if ((lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))<0) {
						lowerLevelDifference = 1;
					} else {
						lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck);
					}
					SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))) / lowerLevelDifference;
					//objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
					objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
				}
			}
		}

		// Logic to get put condition
		if (lowerSMAPeriodValues.size() > daysToCheck && higherSMAPeriodValues.size() > daysToCheck) {
			if ((lowerSMAPeriodValues.get(0) - lowerSMAPeriodValues.get(daysToCheck) < 0) && (higherSMAPeriodValues.get(0) - higherSMAPeriodValues.get(daysToCheck) < 0)) {
				if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck)) {
					if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < 0) {
						objSMAIndicatorDetails.signalSMAToSMA = "put";
						//percentagePriceChange = (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck)) / stockPriceValues.get(daysToCheck);
						if ((lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))<0) {
							lowerLevelDifference = 1;
						} else {
							lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck);
						}
						SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))) / lowerLevelDifference;
						//objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
						objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
					}
				}
			}
		}
				
		
		/*for (int counter = 0; counter < lowerSMAPeriodValues.size(); counter++) {
			if (lowerSMAPeriodValues.size() > counter && higherSMAPeriodValues.size() > counter) {
				differenceInSMA = lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter);
				if (nextDayLowerSMA == 0) {
					twoDaysLowerSMADIfference = 0;
				} else {
					twoDaysLowerSMADIfference = nextDayLowerSMA - lowerSMAPeriodValues.get(counter);
				}
				if (differenceInSMA > 0 && twoDaysLowerSMADIfference >= 0) {
					positiveCounter++;
				} else {
					break;
				}
				if (positiveCounter >= daysToCheck) {
					// Generate buy indicator
					objSMAIndicatorDetails.signalSMAToSMA = "buy";
					break;
				}
				nextDayLowerSMA = lowerSMAPeriodValues.get(counter);
			} else {
				break;
			}
		}

		positiveCounter = 0;
		nextDayLowerSMA = 0;
		for (int counter = 0; counter < lowerSMAPeriodValues.size(); counter++) {
			if (lowerSMAPeriodValues.size() > counter && higherSMAPeriodValues.size() > counter) {
				differenceInSMA = lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter);
				if (nextDayLowerSMA == 0) {
					twoDaysLowerSMADIfference = 0;
				} else {
					twoDaysLowerSMADIfference = nextDayLowerSMA - lowerSMAPeriodValues.get(counter);
				}
				if (differenceInSMA < 0 && twoDaysLowerSMADIfference <= 0) {
					positiveCounter++;
				} else {
					break;
				}
				if (positiveCounter >= daysToCheck) {
					// Generate put indicator
					objSMAIndicatorDetails.signalSMAToSMA = "put";
					break;
				}
				nextDayLowerSMA = lowerSMAPeriodValues.get(counter);
			} else {
				break;
			}
		}*/
	}
}
