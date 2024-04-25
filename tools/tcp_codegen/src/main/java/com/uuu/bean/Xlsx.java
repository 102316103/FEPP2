package com.uuu.bean;

import org.apache.commons.lang3.StringUtils;

/**
 * 電文屬性資料
 *
 * @author mickey
 *
 */
public class Xlsx {
	// 電文說明 HEADER/BODY/PAYDATA/REPLYDATA/SENDDATA
	private String headerBody = StringUtils.EMPTY;

	// excel客製電文屬性名
	private String name = StringUtils.EMPTY;

	// getter、setter所使用的名稱
	private String getsetName = StringUtils.EMPTY;

	// 是否為數值型態
	// "Y"為數值型態BigDecimal，""為String
	private String type = StringUtils.EMPTY;
	// 數字型態，是否有+-符號, true/false
	private String hasSign = Boolean.FALSE.toString();
	// 數字型態，小數點前長度
	private int numberLen;
	// 數字型態，是否有小數點, true/false
	private String hasPoint = Boolean.FALSE.toString();
	// 數字型態，小數點後長度
	private int floatLen;

	// 是否不轉ascii
	// "N"為不轉，""為預設(轉ascii)
	private String notAscii = StringUtils.EMPTY;

	// 字串開始截取位置
	private int start;
	// 字串最後截取位置，start + lg
	private int end;
	// 字串Size
	private int lg;

	public String getHeaderBody() {
		return headerBody;
	}

	public void setHeaderBody(String headerBody) {
		this.headerBody = headerBody;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGetsetName() {
		return getsetName;
	}

	public void setGetsetName(String getsetName) {
		this.getsetName = getsetName;
	}

	public String getHasSign() {
		return hasSign;
	}

	public void setHasSign(String hasSign) {
		this.hasSign = hasSign;
	}

	public int getNumberLen() {
		return numberLen;
	}

	public void setNumberLen(int numberLen) {
		this.numberLen = numberLen;
	}

	public String getHasPoint() {
		return hasPoint;
	}

	public void setHasPoint(String hasPoint) {
		this.hasPoint = hasPoint;
	}

	public int getFloatLen() {
		return floatLen;
	}

	public void setFloatLen(int floatLen) {
		this.floatLen = floatLen;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getLg() {
		return lg;
	}

	public void setLg(int lg) {
		this.lg = lg;
	}

	public String getNotAscii() {
		return notAscii;
	}

	public void setNotAscii(String notAscii) {
		this.notAscii = notAscii;
	}

	@Override
	public String toString() {
		return "Xlsx [headerBody=" + headerBody + ", name=" + name + ", getsetName=" + getsetName + ", type=" + type
				+ ", hasSign=" + hasSign + ", numberLen=" + numberLen + ", hasPoint=" + hasPoint + ", floatLen="
				+ floatLen + ", notAscii=" + notAscii + ", start=" + start + ", end=" + end + ", lg=" + lg + "]";
	}
}