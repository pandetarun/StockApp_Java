import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class GenerateIndicationfromMovingAverageTest {
	Connection connection = null;
	public static int daysToCheck = 5;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
	SMAIndicatorDetails objSMAIndicatorDetails;
	String stockName;
	String bseCode;
	static String DATES_FILE = "C:\\Tarun\\Personal\\Tool\\StockApp\\DatesForMA.txt";
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateIndicationfromMovingAverageTest obj = new GenerateIndicationfromMovingAverageTest();
		obj.CalculateAndSendIndicationfromSMA();
	}

	public void CalculateAndSendIndicationfromSMA() {
		ArrayList<String> stocklist = null;
		UpdateIndicatedStocksTest tmpUpdateIndicatedStocks = new UpdateIndicatedStocksTest();
		stocklist = getStockListFromDB();
		SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
		int stockcounter = 1;
		ArrayList<Date> DatesToCalculate = readFileAndGetDates();
		for(Date datetoCalculateSMA : DatesToCalculate) {		
			for (String stock : stocklist) {				
				stockName = stock.split("!")[1];
				bseCode = stock.split("!")[0];
				System.out.println("Calculating for Date -> "+datetoCalculateSMA+" and stock -> "+stock+ " and Stock count -> " + stockcounter++);
				if(getFinancialIndication(bseCode)) {	
					objSMAIndicatorDetails = new SMAIndicatorDetails();
					objSMAIndicatorDetails.stockCode = stockName;
					
					CalculateIndicationfromSMA(stockName, datetoCalculateSMA);
					if (objSMAIndicatorDetails.signalPriceToSMA != null || objSMAIndicatorDetails.signalSMAToSMA != null) {
						System.out.println("*****************************Stock Added for indication -> " + stockName);
						SMAIndicatorDetailsList.add(objSMAIndicatorDetails);
					}
				}
				/*if (stockcounter > 100) {
					break;
				}*/
			}
			// Collections.sort(SMAIndicatorDetailsList);
			Collections.sort(SMAIndicatorDetailsList, new SMAIndicatorDetailsComparator());
			tmpUpdateIndicatedStocks.updateSMAIndication(SMAIndicatorDetailsList, datetoCalculateSMA);
			sendTopStockInMail(SMAIndicatorDetailsList, datetoCalculateSMA);
		}
		System.out.println("End");
	}
	
	private ArrayList<Date> readFileAndGetDates () {
		ArrayList<Date> DatesToCalculate = new ArrayList<Date>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(DATES_FILE));
			String line = br.readLine();
			while (line != null) {
				DatesToCalculate.add(new Date(line));
				line = br.readLine();
			}
		} catch (Exception ex) {
			System.out.println("Error in reading date file - > "+ex);
			//logger.error("Error in getStockListFromDB  -> ", ex);
			//return null;
		}
		return DatesToCalculate;
	}

	private ArrayList<String> getStockListFromDB() {

		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToTestDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT BSECODE, stockname FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + "!" + resultSet.getString(2);
				stockList.add(stockBSECode);
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

	private void CalculateIndicationfromSMA(String stockCode, Date dateToCalculate) {
		ArrayList<Integer> prefPeriod = null;
		ArrayList<Float> lowerSMAPeriodValues = null;
		ArrayList<Float> middleSMAPeriodValues = null;
		ArrayList<Float> higherSMAPeriodValues = null;
		ArrayList<Float> stockPriceValues = null;

		
		prefPeriod = GetPreferredSMA(stockCode);
		if (prefPeriod != null && prefPeriod.size() > 0) {
			lowerSMAPeriodValues = GetSMAData(stockCode, prefPeriod.get(0), dateToCalculate);
			middleSMAPeriodValues = GetSMAData(stockCode, prefPeriod.get(1), dateToCalculate);
			higherSMAPeriodValues = GetSMAData(stockCode, prefPeriod.get(2), dateToCalculate);
			stockPriceValues = GetStockPrices(stockCode, dateToCalculate);

			if (middleSMAPeriodValues.size() > 0 && stockPriceValues.size() > 0) {
				calculateIndicationFromMiddleSMAAndPriceV1(middleSMAPeriodValues, stockPriceValues);
			}
			if (lowerSMAPeriodValues.size() > 0 && higherSMAPeriodValues.size() > 0) {
				calculateIndicationFromLowerSMAAndHigherSMAV1(lowerSMAPeriodValues, higherSMAPeriodValues, stockPriceValues);
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
			connection = StockUtils.connectToTestDB();
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

	private ArrayList<Float> GetSMAData(String stockCode, Integer period, Date dateToCalculate) {
		ArrayList<Float> SMAData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String SMAvalue;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		
		try {
			SMAData = new ArrayList<Float>();
			connection = StockUtils.connectToTestDB();
			statement = connection.createStatement();
			tmpSQL = "SELECT first 20 SMA FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = " + period.intValue() + " and tradeddate <='" + dateFormat.format(dateToCalculate) + "' order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
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

	private ArrayList<Float> GetStockPrices(String stockCode, Date dateToCalculate) {
		ArrayList<Float> priceData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String price;
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String tmpSQL;
		
		try {
			priceData = new ArrayList<Float>();
			connection = StockUtils.connectToTestDB();
			statement = connection.createStatement();
			tmpSQL = "SELECT first 20 closeprice, tradeddate FROM DAILYSTOCKDATA where stockname='" + stockCode + "' and tradeddate <='" + dateFormat.format(dateToCalculate) + "' order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
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
		//Assign price to SMA object to get updated in excel
		objSMAIndicatorDetails.stockPrice = stockPriceValues.get(0);
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

	private void calculateIndicationFromMiddleSMAAndPriceV1(ArrayList<Float> middleSMAPeriodValues, ArrayList<Float> stockPriceValues) {

		float percentagePriceChange = 0;
		float priceToSMAPercentageDeviation = 0;
		float lowerLevelDifference = 0;
		boolean continuousGrowth = true;
		
		// Logic to get buy condition
		if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
			return;
		}
		objSMAIndicatorDetails.stockPrice = stockPriceValues.get(0);
		if (stockPriceValues.size() > daysToCheck && middleSMAPeriodValues.size() > daysToCheck) {
			//last day price is less than price daytocheck before
			if (stockPriceValues.get(0) < stockPriceValues.get(daysToCheck-1)) { 
				return;
			}
			//price trending below middle SMA then stock is not good to buy
			if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) < 0) { 
				return;
			}
			//Last day price lower than previous day means down trend
			if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
				return;
			}
			for (int counter = 1 ; counter < daysToCheck ; counter++ ) {
				if (stockPriceValues.get(counter-1) - middleSMAPeriodValues.get(counter-1) < stockPriceValues.get(counter) - middleSMAPeriodValues.get(counter)) {
					continuousGrowth = false;
				}
				if (stockPriceValues.get(counter) - middleSMAPeriodValues.get(counter) < 0) { 
					objSMAIndicatorDetails.PNSMAcrossover = true;
				}
			}
			if(continuousGrowth) {
				objSMAIndicatorDetails.PNSMcontinuousGrowth = true;
			}
			if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) > 0 && (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) > stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))) {
				objSMAIndicatorDetails.signalPriceToSMA = "buy";
				percentagePriceChange = (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck-1)) / stockPriceValues.get(daysToCheck-1);
				objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
				if ((stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))<0) {
					lowerLevelDifference = 1;
				} else {
					lowerLevelDifference = stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1);
				}
				priceToSMAPercentageDeviation = ((stockPriceValues.get(0) - middleSMAPeriodValues.get(0)) - (stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))) / lowerLevelDifference;				
				objSMAIndicatorDetails.priceToSMApercentageDeviation = priceToSMAPercentageDeviation;
			} else {
				objSMAIndicatorDetails.PNSMAcrossover = false;
			}
		}
/*//Put condition later
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
		}*/
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
	
	private void calculateIndicationFromLowerSMAAndHigherSMAV1(ArrayList<Float> lowerSMAPeriodValues, ArrayList<Float> higherSMAPeriodValues, ArrayList<Float> stockPriceValues) {

		float lowerLevelDifference = 0;
		float SMAToSMAPercentageDeviation = 0;
		boolean continuousGrowth = true;
		
		// Logic to get buy condition
		if (lowerSMAPeriodValues.size() > daysToCheck && higherSMAPeriodValues.size() > daysToCheck && stockPriceValues.size() > daysToCheck) {
			//price trending below middle SMA then stock is not good to buy
			if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < 0) { 
				return;
			}
			//last day price is less than price daytocheck before
			if (stockPriceValues.get(0) < stockPriceValues.get(daysToCheck-1)) { 
				return;
			}
			//Last day price lower than previous day means down trend
			if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
				return;
			}
			for (int counter = 1 ; counter < daysToCheck ; counter++ ) {
				if (lowerSMAPeriodValues.get(counter-1) - higherSMAPeriodValues.get(counter-1) < lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter)) {
					continuousGrowth = false;
				}
				if (lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter) < 0) { 
					objSMAIndicatorDetails.SMNSMcrossover = true;
				}
			}
			if(continuousGrowth) {
				objSMAIndicatorDetails.SMNSMcontinuousGrowth = true;
			}
			if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) > 0 && (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) > lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))) {
				objSMAIndicatorDetails.signalSMAToSMA = "buy";
				if ((lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))<0) {
					lowerLevelDifference = 1;
				} else {
					lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1);
				}
				SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))) / lowerLevelDifference;
				objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
			} else {
				objSMAIndicatorDetails.SMNSMcrossover = false;
			}
		}	
/*//Put condition later
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
*/				
	}
	
	
	private void sendTopStockInMail(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList, Date dateToCalculate) {
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>signalSMAToSMA</th><th>SMNSMcrossover</th><th>SMNSMcontinuousGrowth</th><th>SMAToSMApercentageDeviation</th><th>signalPriceToSMA</th><th>PNSMAcrossover</th>"
				+ "<th>PNSMcontinuousGrowth</th><th>priceToSMApercentageDeviation</th><th>percentagePriceChange</th></tr>");
		
		for (int counter = 0; counter <(SMAIndicatorDetailsList.size()>20?20:SMAIndicatorDetailsList.size()); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalDate + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).stockCode + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalSMAToSMA + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMNSMcrossover + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMNSMcontinuousGrowth + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMAToSMApercentageDeviation + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalPriceToSMA + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).PNSMAcrossover + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).PNSMcontinuousGrowth + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).priceToSMApercentageDeviation + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).percentagePriceChange + "</td></tr>");
		}
		mailBody.append("</table></body></html>");
		SendSuggestedStockInMail mailSender;
        mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","Stocklist on "+(new Date()).toString(),mailBody.toString());
	}
	
	private boolean getFinancialIndication(String stockname) {
		//ArrayList<Float> priceData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String indication;

		try {
			//priceData = new ArrayList<Float>();
			connection = StockUtils.connectToTestDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT ANNUALSALESINDICATOR FROM STOCK_FINANCIAL_TRACKING where bsecode='" + stockname + "';");
			
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				indication = resultSet.getString(1);
				if(indication.equalsIgnoreCase("good")){
					return true;
				} else {
					return false;
				}
			}
			resultSet.close();
			connection.close();
			connection = null;
		} catch (Exception ex) {
			System.out.println("getFinancialIndication Error in getting indication = " + ex);
			return true;
		}
		//Returning true in case of no data to avoid loosing good stock
		return true;
	}
}
