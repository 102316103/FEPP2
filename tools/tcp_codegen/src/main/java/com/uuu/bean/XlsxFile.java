package com.uuu.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * excel屬性資料
 *
 * @author mickey
 *
 */
public class XlsxFile {
	// Filename
	private String className = "";

	// 電文類型
	private FileType fileType;

	// Request 或 Response
	private String cellMessageType = "";

	// 電文總長度
	private int totalSize = 0;

	// excel所有屬性設定
	private List<Xlsx> xlsxList;

	public XlsxFile() {
		super();
	}

	public XlsxFile(String className, String cellMessageType) {
		super();
		this.className = className;
		FileType.getByValue(className);
		this.cellMessageType = cellMessageType;
		this.xlsxList = new ArrayList<Xlsx>();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getCellMessageType() {
		return cellMessageType;
	}

	public void setCellMessageType(String cellMessageType) {
		this.cellMessageType = cellMessageType;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public List<Xlsx> getXlsxList() {
		return xlsxList;
	}

	public void setXlsxList(List<Xlsx> xlsxList) {
		this.xlsxList = xlsxList;
	}

	@Override
	public String toString() {
		return "XlsxFile [className=" + className + ", fileType=" + fileType + ", cellMessageType=" + cellMessageType
				+ ", totalSize=" + totalSize + ", xlsxList=" + xlsxList + "]";
	}

}
