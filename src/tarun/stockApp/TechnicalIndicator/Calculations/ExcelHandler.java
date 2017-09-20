package tarun.stockApp.TechnicalIndicator.Calculations;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHandler {

	/**
	 * 
	 * @param excelPathAndName
	 * @return
	 * @throws CreateWorkbookFailedException
	 */
	public static XSSFWorkbook createReadOnlyWorkBook(String excelPathAndName) {
		//configBean.getLogger().debug("createWorkBook() Attempting to create read-only workbook");
		XSSFWorkbook outPutWorkBookx = null;
		try {
			/*if (excelToFileMapping == null) {
				excelToFileMapping = new HashMap<XSSFWorkbook, ExcelAndFileObjectMapping>();
			}*/
			File outputFileTmp = new File(excelPathAndName);
			
			if(outputFileTmp.exists()){
			FileInputStream fileOut = new FileInputStream(outputFileTmp);
			outPutWorkBookx = new XSSFWorkbook(excelPathAndName);
			fileOut.close();
			}else{
				//configBean.getLogger().error("Whole environment setup without Object Repository ");
			}
		} catch (Exception e) {
			//configBean.getLogger().error("Exception in opening the file " + excelPathAndName + ". Error is - " + e);
			//throw new CreateWorkbookFailedException(e);
		}
		//configBean.getLogger().debug("createWorkBook() Read-only workbook created successfully");
		return outPutWorkBookx;
	}

	/**
	 * 
	 * @param excelPath
	 * @param excelFileName
	 * @return
	 * @throws CreateWorkbookFailedException
	 */
	public static XSSFWorkbook createReadWriteWorkBook(String excelPath, String excelFileName) {
		//configBean.getLogger().debug("createWorkBook() Attempting to create read-write workbook");
		XSSFWorkbook outPutWorkBookx = null;
		try {
			File outputFileTmp = null;
			outputFileTmp = new File(excelPath + "\\" + excelFileName);
			FileInputStream fileOut = new FileInputStream(outputFileTmp);
			outPutWorkBookx = new XSSFWorkbook(fileOut);
		} catch (Exception e) {
			System.out.println("Error"+e);
		}
		return outPutWorkBookx;
	}

	/**
	 * 
	 * @param outPutWorkBookx
	 */
	/*public static void saveAndCloseWorkBook(XSSFWorkbook outPutWorkBookx) {
		configBean.getLogger().debug("saveAndCloseWorkBook() Attempting save and close workbook");
		try {
			if (excelToFileMapping != null) {
				FileInputStream fileStreamObj = excelToFileMapping.get(outPutWorkBookx).getExcelFileStream();
				String filepath = excelToFileMapping.get(outPutWorkBookx).getFilepath();
				fileStreamObj.close();
				FileOutputStream saveExcel = new FileOutputStream(new File(filepath));
				outPutWorkBookx.write(saveExcel);
				saveExcel.flush();
				saveExcel.close();
				if (excelToFileMapping.get(outPutWorkBookx).getTempFilePath() != null) {
					File tmpFile = new File(excelToFileMapping.get(outPutWorkBookx).getTempFilePath());
					if (tmpFile.delete()) {
						configBean.getLogger().debug("Temp File deleted - " + excelToFileMapping.get(outPutWorkBookx).getTempFilePath());
					} else {
						configBean.getLogger().warn("Temp File not deleted - " + excelToFileMapping.get(outPutWorkBookx).getTempFilePath() + ". Please delete it manually.");
					}
				}
				excelToFileMapping.remove(outPutWorkBookx);
			}
		} catch (Exception e) {
			configBean.getLogger().error("Error occurred in storing the file. Error is : " + e);
		}
		configBean.getLogger().debug("saveAndCloseWorkBook() Workbook saved and closed");
	}*/

	/**
	 * 
	 * @param outPutWorkBookx
	 * @param sheetId
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */

	public static String getCellValueX(XSSFWorkbook outPutWorkBookx, int sheetId, int row, int col) throws Exception {
		Row excleRow;
		Cell cell;
		int cellTypeValue;
		XSSFSheet excelSheet = outPutWorkBookx.getSheetAt(sheetId);

		if (excelSheet == null) {
			throw new Exception("Sheet Not Found" + sheetId);
		}

		excleRow = excelSheet.getRow(row);
		cell = excleRow.getCell(col);
		if (cell != null) {
			cellTypeValue = cell.getCellType();
			if (cellTypeValue == Cell.CELL_TYPE_STRING) {
				return cell.getStringCellValue();
			} else if (cellTypeValue == Cell.CELL_TYPE_NUMERIC) {
				double cellValue = cell.getNumericCellValue();
				return String.valueOf(cellValue);
			} else if (cellTypeValue == Cell.CELL_TYPE_FORMULA) {
				XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(excelSheet.getWorkbook());
				CellValue c = evaluator.evaluate(cell);
				if (c.getCellType() == Cell.CELL_TYPE_STRING) {
					return c.getStringValue();
				} else if (c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					double cellValue = c.getNumberValue();
					return String.valueOf(cellValue);
				}
			}
		}
		return "";
	}

	/**
	 * 
	 * @param inPutWorkBookx
	 * @param sheetName
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
	public static String getCellValueX(XSSFWorkbook inPutWorkBookx, String sheetName, int row, int col) throws Exception {
		Row excleRow;
		Cell cell;
		int cellTypeValue;
		XSSFSheet excelSheet = inPutWorkBookx.getSheet(sheetName);
		String cellStringValue;

		if (excelSheet == null) {
			throw new Exception("Sheet Not Found" + sheetName);
		}

		excleRow = excelSheet.getRow(row);
		cell = excleRow.getCell(col);
		
		
		
		if (cell != null) {
			DataFormatter dataFormatter = new DataFormatter();
			cellStringValue = dataFormatter.formatCellValue(excleRow.getCell(col));
			return cellStringValue;
			/*//System.out.println ("Is shows data as show in Excel file" + cellStringValue);
			cellTypeValue = cell.getCellType();
			if (cellTypeValue == Cell.CELL_TYPE_STRING) {
				return cell.getStringCellValue();
			} else if (cellTypeValue == Cell.CELL_TYPE_NUMERIC) {
				double cellValue = cell.getNumericCellValue();
				return String.valueOf(cellValue);
			} else if (cellTypeValue == Cell.CELL_TYPE_FORMULA) {
				XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(excelSheet.getWorkbook());
				CellValue c = evaluator.evaluate(cell);
				if (c.getCellType() == Cell.CELL_TYPE_STRING) {
					return c.getStringValue();
				} else if (c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					double cellValue = c.getNumberValue();
					return String.valueOf(cellValue);
				}
			}*/
		}
		return "";
	}

	/**
	 * 
	 * @param outPutWorkBookx
	 * @param sheetName
	 * @param row
	 * @param col
	 * @param tmpContent
	 */
	public static void setCellValueX(XSSFWorkbook outPutWorkBookx, String sheetName, int row, int col, String tmpContent) {
		//configBean.getLogger().debug("setCellValueX() Attempting to set cell value at Sheet : " + sheetName + " [Row : " + row + " | column : " + col + "]");
		if (outPutWorkBookx.getSheet(sheetName).getRow(row).getCell(col) != null) {
			outPutWorkBookx.getSheet(sheetName).getRow(row).getCell(col).setCellValue(tmpContent);
		} else {
			outPutWorkBookx.getSheet(sheetName).getRow(row).createCell(col).setCellValue(tmpContent);
		}
		//configBean.getLogger().debug("setCellValueX() Cell value set in Sheet : " + sheetName + "[Row : " + row + " | column : " + col + "]");
	}
	
	
	public static void saveExcel(String excelPath, String excelFileName, XSSFWorkbook excelFile) {
		try {
		FileOutputStream saveExcelFile = new FileOutputStream(new File(excelPath+"\\"+excelFileName));
		excelFile.write(saveExcelFile);
		saveExcelFile.flush();
		saveExcelFile.close();
		} catch (Exception ex) {
			System.out.println("ERROR "+ex);
		}
	}
}