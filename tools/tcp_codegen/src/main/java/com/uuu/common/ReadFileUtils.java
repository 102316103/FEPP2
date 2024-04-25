package com.uuu.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.uuu.bean.FileType;
import com.uuu.bean.Xlsx;
import com.uuu.bean.XlsxFile;
import com.uuu.ebcdic.ReadFile4ATM;
import com.uuu.ebcdic.ReadFile4IMS;
import com.uuu.string.ReadFile4MFT;
import com.uuu.xml.ReadFile4EATM;
import com.uuu.xml.ReadFile4HCE;
import com.uuu.xml.ReadFile4NB;
import com.uuu.xml.ReadFile4VA;
import com.uuu.xml.ReadFile4VO;

public class ReadFileUtils {
	/**
	 * 將電文每個屬性資料轉為物件，單筆excel
	 *
	 * @param excelFile
	 * @return
	 */
	public static XlsxFile getXlsxFile(File excelFile) {
		XlsxFile xlsxFile = new XlsxFile();
		try ( //
				FileInputStream fis = new FileInputStream(excelFile); // 檔案輸入流
				XSSFWorkbook wb = new XSSFWorkbook(fis); // Excel檔
		) {
			// 頁籤，不會有多個頁籤，故只取第一頁籤
			XSSFSheet sheet = wb.getSheetAt(0);
			// 總行數，從0開始計數
			int maxRow = sheet.getLastRowNum();

			// B2，Request/Response
			XSSFCell cellMessageType = sheet.getRow(1).getCell(1);
			xlsxFile.setCellMessageType(cellMessageType.getStringCellValue().toLowerCase().trim());

			// C2，ClassName、FileName
			XSSFCell classNameCell = sheet.getRow(1).getCell(2);
			xlsxFile.setClassName(classNameCell.getStringCellValue().trim());

			// 電文類型
			Object obj = null;
			
			if(com.uuu.Main.notJar) {
				obj = PropertiesUtils.fileTypeProperties.get(xlsxFile.getClassName());
			}
					
			if (obj == null) {
				xlsxFile.setFileType(null);
			} else {
				xlsxFile.setFileType(FileType.getByValue(obj.toString()));
			}
			System.out.println(String.format("excelFile : %s; messageType: %s; FileType: %s; maxRow: %s", //
					xlsxFile.getClassName(), xlsxFile.getCellMessageType(), xlsxFile.getFileType(), maxRow));

			// 處理每筆屬性設定參數
			List<Xlsx> xlsxList = new ArrayList<Xlsx>();
			int totalSize = 0;
			for (int i = 3; i <= maxRow; i++) {
				Xlsx xlsx = new Xlsx();
				XSSFRow row = sheet.getRow(i); // 行數

				if (row == null) {
					continue;
				}

				XSSFCell cell1 = row.getCell(1); // B列，屬性名

				// B列從未使用
				if (cell1 == null || "".equals(cell1)) {
					continue;
				}
				String attributeName = cell1.getStringCellValue().trim();

				// B列為空字串
				if (StringUtils.EMPTY.equals(attributeName)) {
					continue;
				}

				XSSFCell cell0 = row.getCell(0); // 列數0 (電文說明。HEADER)
				XSSFCell cell2 = row.getCell(2); // C列，欄位格式定義，數值型態才會使用
				XSSFCell cell5 = row.getCell(5); // F列，是否不轉ascii，Y
				XSSFCell cell6 = row.getCell(6); // G列，"Y"為數值型態BigDecimal，空白為String
				XSSFCell cell8 = row.getCell(8); // I列，tita字串開始截取位置
				XSSFCell cell9 = row.getCell(9); // J列，tita字串Size

				int start = (int) cell8.getNumericCellValue();
				int lg = (int) cell9.getNumericCellValue();
				totalSize = totalSize + lg;

				xlsx.setHeaderBody(getStringByCell(cell0));
				xlsx.setNotAscii(getStringByCell(cell5));
				xlsx.setType(getStringByCell(cell6));
				xlsx.setName(attributeName);
				xlsx.setGetsetName(toUppercaseHead(attributeName));
				xlsx.setStart(start);
				xlsx.setLg(lg);
				xlsx.setEnd(start + lg);

				// 如果為數字型態，需要取得格式定義，如：S9(11)V99、S9(14).99
				if (StringUtils.equalsIgnoreCase("Y", xlsx.getType())) {
					String numberFormat = getStringByCell(cell2).toUpperCase();
					// 是否有+-符號
					xlsx.setHasSign(Boolean.toString(numberFormat.startsWith("S")));
					// 小數點前長度
					Matcher numberLenMatcher = Pattern.compile("9\\([0-9]{1,2}\\)").matcher(numberFormat);
					if(numberLenMatcher.find()) {
						String numberLenStr = numberLenMatcher.group();
						numberLenStr = numberLenStr.substring(2, numberLenStr.length() - 1);
						xlsx.setNumberLen(Integer.parseInt(numberLenStr));
					}else { // 找不到定義
						xlsx.setNumberLen(0);
					}
					Matcher floatLenMatcher = Pattern.compile("V[9]{1,}").matcher(numberFormat);
					Matcher floatLenPointMatcher = Pattern.compile("\\.[9]{1,}").matcher(numberFormat);
					// 小數點後長度，無小數點
					if (floatLenMatcher.find()) {
						String floatLenStr = floatLenMatcher.group();
						xlsx.setFloatLen(floatLenStr.substring(1).length());
						xlsx.setHasPoint(Boolean.FALSE.toString());
					}
					// 小數點後長度，有小數點
					else if(floatLenPointMatcher.find()){
						String floatLenStr = floatLenPointMatcher.group();
						xlsx.setFloatLen(floatLenStr.substring(1).length());
						xlsx.setHasPoint(Boolean.TRUE.toString());
					}
					// 找不到定義
					else {
						xlsx.setFloatLen(0);
					}
				}

				xlsxList.add(xlsx);
			}
			xlsxFile.setTotalSize(totalSize);
			xlsxFile.setXlsxList(xlsxList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xlsxFile;
	}

	private static String getStringByCell(XSSFCell cell) {
		String returnStr = "";
		if (cell != null && !"".equals(cell)) {
			returnStr = cell.getStringCellValue().trim();
		}
		return returnStr;
	}

	/**
	 * 將字串的第一個英文字轉大寫
	 *
	 * @param property
	 * @return
	 */
	public static String toUppercaseHead(String property) {
		String first = property.substring(0, 1);
		String english = "[a-z]";
		if (first.matches(english)) {
			return first.toUpperCase() + property.substring(1);
		}
		return property;
	}

	/**
	 * 取得資料夾下所有excel檔
	 *
	 * @param folder
	 * @return
	 */
	public static List<File> findAllFilesInFolder(File folder) {
		List<File> fileList = new ArrayList<File>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}
			if (file.getName().endsWith("xlsx")) {
				fileList.add(file);
			}
		}
		return fileList;
	}

	/**
	 * 依電文類型返回相應物件
	 *
	 * @param xlsxFile
	 * @return
	 */
	public static ReadSingleFile getReadFileObject(XlsxFile xlsxFile) {
		ReadSingleFile readFile = null;
		// 産出java檔，ATM
		if (FileType.ATM == xlsxFile.getFileType()) {
			readFile = new ReadFile4ATM();
		}
		// 産出java檔，MFT
		else if (FileType.MFT == xlsxFile.getFileType()) {
			readFile = new ReadFile4MFT();
		}
		// 産出java檔，IMS
		else if (FileType.IMS == xlsxFile.getFileType()) {
			readFile = new ReadFile4IMS();
		}
		// 産出java檔，EATM
		else if (FileType.EATM == xlsxFile.getFileType()) {
			readFile = new ReadFile4EATM();
		}
		// 産出java檔，HCE
		else if (FileType.HCE == xlsxFile.getFileType()) {
			readFile = new ReadFile4HCE();
		}
		// 産出java檔，NB
		else if (FileType.NB == xlsxFile.getFileType()) {
			readFile = new ReadFile4NB();
		}
		// 産出java檔，VA
		else if (FileType.VA == xlsxFile.getFileType()) {
			readFile = new ReadFile4VA();
		}
		// 産出java檔，VO
		else if (FileType.VO == xlsxFile.getFileType()) {
			readFile = new ReadFile4VO();
		}
		return readFile;
	}

	public static FileWriter getFileWriterByFileType(FileType fileType, String className) throws IOException {
		File javaFile = new File(String.format(CommonValue.outputPath, fileType.getValue(), className));
		javaFile.getParentFile().mkdirs();
		FileWriter writer = new FileWriter( //
				javaFile, // java檔路徑
				false // 覆蓋檔案內容
		);
		return writer;
	}

	public static File getFileByFileType(FileType fileType, String className) throws IOException {
		File javaFile = new File(String.format(CommonValue.outputPath, fileType.getValue(), className));
		javaFile.getParentFile().mkdirs();
		if (javaFile.createNewFile()) {
			System.out.println("Create file successed " + javaFile.getName());
		}
		return javaFile;
	}
}
