package com.uuu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import com.uuu.bean.FileType;
import com.uuu.bean.XlsxFile;
import com.uuu.common.CommonValue;
import com.uuu.common.PropertiesUtils;
import com.uuu.common.ReadFileUtils;
import com.uuu.common.ReadSingleFile;
import com.uuu.ebcdic.ReadFile4ATM_bigClass;
import com.uuu.string.ReadFile4MFT_bigClass;

// CodeGen統一入口
public class Main {
	// codegen根路徑
	public static String rootPath = "";
	// 是否有執行過ATM CodeGen
	private static boolean hasAtmFile = false;
	// 是否有執行過MFT CodeGen
	private static boolean hasMftFile = false;
	//判斷是否為使用可執行jar。false 為可執行jar, true 為程式執行
	public static boolean notJar = false;
	
	public static void main(String[] args) throws Exception {
		// 設定檔資料讀取
		Properties mp ;
		if(notJar) {
			mp = PropertiesUtils.loadProperties(CommonValue.propertiesPath + "main.properties");
		}else {
			mp = PropertiesUtils.loadProperties("main.properties");
		}
		rootPath = String.valueOf(mp.get("file.rootPath"));
		String fileType = String.valueOf(mp.get("file.type")).toUpperCase();
		
		// 存放電文java檔總路徑
		String outputJavaFolderPath = Main.rootPath + "java";
		// codegen excel配置檔路徑
		String inputExcelFolderPath = Main.rootPath + fileType;//excel
		// 默認電文類型，處理新電文會需要用
		FileType defaultFileType = FileType.getByValue(fileType);
		
		if(notJar) {
			inputExcelFolderPath =  Main.rootPath + "excel\\" + fileType;
			// 更新電文名稱對應電文類型配置檔
			PropertiesUtils.updateFileTypeProperties(outputJavaFolderPath);
			// 取得配置檔內容
			PropertiesUtils.fileTypeProperties = PropertiesUtils.loadProperties(CommonValue.propertiesPath + "fileType.properties");
		}
		
		Main.doWriteSingle( //
				inputExcelFolderPath, //
				defaultFileType //
		);

		// ATM_bigClass codegen
		if (hasAtmFile) {
			String atmBigClassExcelFolderPath = rootPath + "excel\\ATM_bigClass";
			Main.doWriteAtmBigClass(inputExcelFolderPath, atmBigClassExcelFolderPath);
		}

		// MFT_bigClass codegen
		if (hasMftFile) {
//			String atmBigClassExcelFolderPath = rootPath + "excel\\MFT_bigClass";
//			Main.doWriteMftBigClass(inputExcelFolderPath, atmBigClassExcelFolderPath);
		}
	}

	/**
	 * 針對單個
	 *
	 * @param outputJavaFolderPath
	 * @param inputExcelFolderPath
	 * @param defaultFileType
	 * @throws IOException
	 */
	public static void doWriteSingle( //
									  String inputExcelFolderPath, //
									  FileType defaultFileType //
	) throws IOException {
		// codegen excel配置檔列表
		List<File> excelFileList = ReadFileUtils.findAllFilesInFolder(new File(inputExcelFolderPath));

		if (excelFileList.size() == 0) {
			throw new RuntimeException(inputExcelFolderPath + "無可codegen檔案");
		}

		for (File excelFile : excelFileList) {
			// 電文屬性資料
			XlsxFile xlsxFile = ReadFileUtils.getXlsxFile(excelFile);

			// 若codegen新電文 或 電文類型配置檔錯誤，使用默認電文
			if (xlsxFile.getFileType() == null) {
				xlsxFile.setFileType(defaultFileType);
			}

			// 若有更新ATM電文，還需要更新ATM_bigClass電文
			if (xlsxFile.getFileType() == FileType.ATM) {
				hasAtmFile = true;
			}

			// 若有更新MFT電文，還需要更新MFT_bigClass電文
			if (xlsxFile.getFileType() == FileType.MFT) {
				hasMftFile = true;
			}

			// 取得不同電文産出物件
			ReadSingleFile readFile = ReadFileUtils.getReadFileObject(xlsxFile);

			// 執行codegen
			if(readFile != null) {
				readFile.doWrite(xlsxFile);
			}
		}
	}

	/**
	 * ATM_bigClass codegen
	 *
	 * @param classExcelFolderPath ATM小Class Excel資料夾路徑
	 * @param bigClassExcelFolderPath ATM大Class Excel資料夾路徑
	 * @throws IOException
	 */
	public static void doWriteAtmBigClass(String classExcelFolderPath, String bigClassExcelFolderPath) throws IOException {
		// 將ATM的excel檔搬至ATM_bigClass目錄
		Main.moveFile(classExcelFolderPath, bigClassExcelFolderPath);

		// 執行codegen
		ReadFile4ATM_bigClass readFile = new ReadFile4ATM_bigClass();
		readFile.doWrite(bigClassExcelFolderPath);
	}

	/**
	 * MFT codegen
	 *
	 * @param classExcelFolderPath MFT小Class Excel資料夾路徑
	 * @param bigClassExcelFolderPath MFT大Class Excel資料夾路徑
	 * @throws IOException
	 */
	public static void doWriteMftBigClass(String classExcelFolderPath, String bigClassExcelFolderPath) throws IOException {
		// 將MFT的excel檔搬至MFT_bigClass目錄
		Main.moveFile(classExcelFolderPath, bigClassExcelFolderPath);

		// 執行codegen
		ReadFile4MFT_bigClass readFile = new ReadFile4MFT_bigClass();
		readFile.doWrite(bigClassExcelFolderPath);
	}

	public static void moveFile(String source, String target) throws IOException {
		// 指定來源資料夾和目標資料夾的路徑
		Path fromFolder = Path.of(source);
		Path toFolder = Path.of(target);
		// 遍歷來源資料夾底下的所有檔案
		Files.walk(fromFolder) //
				.filter(Files::isRegularFile) // 篩選只有檔案，不包含資料夾
				.forEach(file -> {
					try {
						// 建立目標檔案的路徑
						Path destinationFile = toFolder.resolve(fromFolder.relativize(file));
						// 複製檔案至目標資料夾
						Files.createDirectories(destinationFile.getParent()); // 建立目標資料夾（如果不存在）
						Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Copied: " + file.getFileName());
					} catch (IOException e) {
						System.out.println("Failed to copy file: " + file.getFileName());
						e.printStackTrace();
					}
				});
	}
}
