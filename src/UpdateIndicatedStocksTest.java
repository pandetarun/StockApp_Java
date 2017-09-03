import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class UpdateIndicatedStocksTest {
	private final static String PATH_TO_FILE = "C:\\Tarun\\Personal\\Tool\\StockApp";
	private final static String NAME_OF_FILE = "SuggestedStockListTest.xlsx";
	StockExcelData tmpStockExcelData = null;
	int excelLastRow;
	
	
	public static void main(String[] args) {
		
	}
	
	public void updateSMAIndication(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList, Date dateToCalculate) {
		XSSFWorkbook outputWorkbook = null;		
		outputWorkbook = ExcelHandler.createReadWriteWorkBook(PATH_TO_FILE, NAME_OF_FILE);
		int counter = 1;
		int existinigStockRow;
		int todaysColumn;
		
		try {
			todaysColumn = identifyTodayColumninExcel(outputWorkbook, "SMA", dateToCalculate);
			readExcelData(outputWorkbook, "SMA");
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String todayDate = dateFormat.format(dateToCalculate);
			for (SMAIndicatorDetails tmpSMAIndicatorDetails : SMAIndicatorDetailsList) {
				if(counter<=20) {
					//stock already there in excel
					if(tmpStockExcelData.stockName.contains(tmpSMAIndicatorDetails.stockCode)) {
						existinigStockRow = tmpStockExcelData.stockName.indexOf(tmpSMAIndicatorDetails.stockCode);
						existinigStockRow = existinigStockRow + 2;
						//tmpStockExcelData.excelRow.get(existinigStockRow);
						//tmpStockExcelData.repeatTime.add(existinigStockRow, tmpStockExcelData.repeatTime.get(existinigStockRow) + 1);
						//put repeat counter in excel
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", existinigStockRow, 4, tmpStockExcelData.repeatTime.get(existinigStockRow-2) + 1+"");
						//Enter price in todays date column
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", existinigStockRow, todaysColumn,tmpSMAIndicatorDetails.stockPrice+"");
						//Enter Date
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", existinigStockRow, 2,todayDate);
						//Enter rank
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", existinigStockRow, 3,counter+"");
						
					} else {
						//Stock not there in excel. Create entry at lst row
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", excelLastRow, 1, tmpSMAIndicatorDetails.stockCode);
						//DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", excelLastRow, 2, todayDate);
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", excelLastRow, 3, counter+"");
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", excelLastRow, 4, 0+"");
						//Enter price in todays date column
						ExcelHandler.setCellValueX(outputWorkbook, "SMA", excelLastRow, todaysColumn,tmpSMAIndicatorDetails.stockPrice+"");
						excelLastRow++;
					}
					counter++;
				} else {
					break;
				}
			}
			ExcelHandler.saveExcel(PATH_TO_FILE, NAME_OF_FILE, outputWorkbook);
		} catch (Exception ex) {
			System.out.println("Error -"+ex);
		}
		
	}
	
	public void updateOBVIndication(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList) {
		
	}

	public void updateBBIndication(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList) {
		
	}
	
	public void updateATRIndication(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList) {
		
	}
	
	private void readExcelData(XSSFWorkbook outputWorkbook, String sheetName) {
		tmpStockExcelData = new StockExcelData();
		ArrayList<String> stockName = new ArrayList<String>();
		ArrayList<Integer> repeatTime = new ArrayList<Integer>();
		ArrayList<Integer> excelRow = new ArrayList<Integer>();
		String cellValue;
		
		try {
			for ( int row = 2;; row++) {
				//stockName
				cellValue = ExcelHandler.getCellValueX(outputWorkbook, sheetName, row, 1);
				if (cellValue!=null && !cellValue.isEmpty() && !cellValue.equalsIgnoreCase("")) {
					stockName.add(cellValue);
					//repeattime
					cellValue = ExcelHandler.getCellValueX(outputWorkbook, sheetName, row, 4);
					repeatTime.add(Integer.parseInt(cellValue));
					//excelRow
					excelRow.add(row);
				} else {
					excelLastRow = row;
					break;
				}
			}
			tmpStockExcelData.excelRow = excelRow;
			tmpStockExcelData.repeatTime = repeatTime;
			tmpStockExcelData.stockName = stockName;
		} catch (Exception ex) {
			
		}
	}
	
	private int identifyTodayColumninExcel(XSSFWorkbook outputWorkbook, String sheetName, Date dateToCalculate) throws Exception{
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
		String todayDate = dateFormat.format(dateToCalculate);
		String cellValue;
		
		for (int colCounter = 230;;colCounter++ ) {
			cellValue = ExcelHandler.getCellValueX(outputWorkbook, sheetName, 1, colCounter);
			if(todayDate.equalsIgnoreCase(cellValue)) {
				return colCounter;
			} else if(colCounter>400) {
				return 0;
			}
		}
	}
}
