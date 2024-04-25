package com.syscom.fep.enclib.vo;

public class SuipData {
	private String functionNo;
	private String keyIdentity;
	private String inputData1;
	private int input1Length;
	private String inputData2;
	private int input2Length;
	private String outputData1;
	private String outputData2;
	private ENCLogData txLog;
	private int rc;

	public String getFunctionNo() {
		return functionNo;
	}

	public void setFunctionNo(String functionNo) {
		this.functionNo = functionNo;
	}

	public String getKeyIdentity() {
		return keyIdentity;
	}

	public void setKeyIdentity(String keyIdentity) {
		this.keyIdentity = keyIdentity;
	}

	public String getInputData1() {
		return inputData1;
	}

	public void setInputData1(String inputData1) {
		this.inputData1 = inputData1;
	}

	public int getInput1Length() {
		return input1Length;
	}

	public void setInput1Length(int input1Length) {
		this.input1Length = input1Length;
	}

	public String getInputData2() {
		return inputData2;
	}

	public void setInputData2(String inputData2) {
		this.inputData2 = inputData2;
	}

	public int getInput2Length() {
		return input2Length;
	}

	public void setInput2Length(int input2Length) {
		this.input2Length = input2Length;
	}

	public String getOutputData1() {
		return outputData1;
	}

	public void setOutputData1(String outputData1) {
		this.outputData1 = outputData1;
	}

	public String getOutputData2() {
		return outputData2;
	}

	public void setOutputData2(String outputData2) {
		this.outputData2 = outputData2;
	}

	public ENCLogData getTxLog() {
		return txLog;
	}

	public void setTxLog(ENCLogData txLog) {
		this.txLog = txLog;
	}

	public int getRc() {
		return rc;
	}

	public void setRc(int rc) {
		this.rc = rc;
	}
}
