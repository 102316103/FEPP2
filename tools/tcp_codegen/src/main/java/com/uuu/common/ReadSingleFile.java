package com.uuu.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.uuu.bean.XlsxFile;

import freemarker.template.Configuration;
import freemarker.template.Version;

public abstract class ReadSingleFile {
	private static Configuration freeMarkerConfiguration = new Configuration(new Version("2.3.31"));

	/**
	 * 實際執行CodeGen
	 *
	 * @param folderName
	 * @throws IOException
	 */
	public abstract void doWrite(XlsxFile xlsxFile) throws IOException;

	public static Configuration getFreeMarkerConfig() {
		return freeMarkerConfiguration;
	}

	/**
	 * 新增多行資料
	 *
	 * @param fileName 檔案名稱
	 * @param content  資料內容
	 * @throws IOException
	 */
	protected void writeLine(FileWriter writer, List<String> contents) throws IOException {
		for (String content : contents) {
			writer.write(content);
			writer.write("\r\n");
		}
	}
}
