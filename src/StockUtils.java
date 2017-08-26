import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class StockUtils {
	public final static String CONNECTION_STRING = "jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8";
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
	
	
}
