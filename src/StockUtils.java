import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class StockUtils {
	public final static String CONNECTION_STRING = "jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8";
	public final static String TEST_CONNECTION_STRING = "jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEWTest.FDB?lc_ctype=utf8";	
	public final static String USER = "SYSDBA";
	public final static String PASS = "Jan@2017";
	static Logger logger = Logger.getLogger(CalculateOnBalanceVolume.class);	
	
	public static Connection connectToDB () {
		Connection connection = null;		
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
		
			connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASS);
		} catch (Exception ex) {
			System.out.println("connectToDB Error in DB action ->"+ex);
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
		return connection;
	}
	
	public static Connection connectToTestDB () {
		Connection connection = null;
		try {
			Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
		
			connection = DriverManager.getConnection(TEST_CONNECTION_STRING, USER, PASS);
		} catch (Exception ex) {
			System.out.println("connectToDB Error in DB action ->"+ex);
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
		return connection;
	}
	
	public static boolean getFinancialIndication(String bseCode) {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String indication;

		try {
			//priceData = new ArrayList<Float>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT ANNUALSALESINDICATOR FROM STOCK_FINANCIAL_TRACKING where bsecode='" + bseCode + "';");
			
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
	
	public static ArrayList<String> getStockListFromDB() {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT BSECODE, stockname, NSECODE FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + "!" + resultSet.getString(2);
				stockBSECode = stockBSECode + "!" + resultSet.getString(3);
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
}
