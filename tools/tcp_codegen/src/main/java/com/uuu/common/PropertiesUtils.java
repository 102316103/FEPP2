package com.uuu.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {
	public static Properties fileTypeProperties;

	/**
	 * 更新電文名稱對應電文類型配置檔 將打印已codegen的電文類型轉為properties，避免日後重新codegen要猜電文類型
	 *
	 * @param inputPath 存放電文java檔總路徑
	 */
	public static void updateFileTypeProperties(String inputPath) {
		// properties檔案路徑
		String outputPath = CommonValue.propertiesPath + "fileType.properties";
		// properties內容
		StringBuilder content = new StringBuilder();

		listFilesAndDirectories(inputPath, "", content);
		writeToFile(new File(outputPath).getAbsolutePath(), content);
	}

	private static void listFilesAndDirectories(String path, String parentDirectoryName, StringBuilder content) {
		File directory = new File(path);
		// 指定路徑不存在或不是資料夾
		if (!directory.exists() || !directory.isDirectory()) {
			return;
		}

		File[] files = directory.listFiles(); // 取得目錄下的所有檔案和資料夾
		// 目錄為空
		if (files == null) {
			return;
		}

		for (File file : files) {
			if (file.isDirectory()) {
				// 遞迴處理資料夾
				listFilesAndDirectories(file.getAbsolutePath(), file.getName(), content);
			} else {
				String fileName = file.getName();
				int dotIndex = fileName.lastIndexOf(".");
				String nameWithoutExtension = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
				content.append(nameWithoutExtension).append("=").append(parentDirectoryName).append("\n");
			}
		}
	}

	private static void writeToFile(String path, StringBuilder content) {
		try {
			File outputFile = new File(path);
			// 如果目錄不存在，則新建目錄
			outputFile.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(outputFile);
			writer.write(content.toString());
			writer.close();
			System.out.println("內容已成功寫入檔案：" + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 讀取properties檔案並返回map
	 *
	 * @param filePath
	 * @return
	 */
	public static Properties loadProperties(String filePath) {
		Properties properties = new Properties();

		try (FileInputStream fis = new FileInputStream(filePath)) {
			properties.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}
}
