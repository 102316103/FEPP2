package com.uuu.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 外圍電文電文說明，對應至Xlsx.headerBody
 * HEADER/BODY/PAYDATA/REPLYDATA/SENDDATA
 *
 * @author mickey
 *
 */
public class HeaderType {
	// excel定義的電文說明
	private String compareStr;

	// xml顯示名稱
	private String xmlStr;

	// 子類電文說明
	private List<HeaderType> subHeaderList;

	public HeaderType(String compareStr, String xmlStr) {
		super();
		this.compareStr = compareStr;
		this.xmlStr = xmlStr;
		this.subHeaderList = new ArrayList<>();
	}

	public String getCompareStr() {
		return compareStr;
	}

	public String getXmlStr() {
		return xmlStr;
	}

	public List<HeaderType> getSubHeaderList() {
		return subHeaderList;
	}

	public HeaderType addSubHeaderList(HeaderType subHeader) {
		this.subHeaderList.add(subHeader);
		return this;
	}
}
