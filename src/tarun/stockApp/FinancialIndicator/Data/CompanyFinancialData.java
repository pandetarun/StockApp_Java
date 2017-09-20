package tarun.stockApp.FinancialIndicator.Data;
import java.util.ArrayList;

public class CompanyFinancialData {
	public String stockName;
	public String BSECode;
	public double bookValue;
	public double faceValue;
	public double yearlyHigh;
	public double yearlyLow;
	public double StockPE;
	public double dividendYield;
	public ArrayList<CompanyAnnualFinancialData> companyAnnualFinancialDataList;
	public ArrayList<CompanyQuarterlyFinancialData> companyQuarterlyFinancialDataList;
}
