package com.syscom.fep.suipConnect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.ibm.db2.jcc.am.ResultSet;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.enums.ENCMessageFlow;
import com.syscom.fep.enclib.vo.ENCLogData;

@SpringBootApplication
@ComponentScan(basePackages = {"com.syscom.fep"})
@StackTracePointCut(caller = SvrConst.SVR_SUIP_CONNECT)
public class ConnectSuipChangeDateAndMakeMac implements CommandLineRunner{
	
	static String ej;
	static String encRetryCountStr;
	static String encLibRetryIntervalStr;
	static String suipTimeoutStr;
	static String subSys;
	static String programName;
	static String channel;
	static String messageFlowType;
	static String messageId;
	static String hostIp;
	static String hostName;
	static String systemId ;
	static String step;
	static String encFunc;
	static String suipAddress;
	static String url;
	static String user;
	static String password;
	static String selectTerminalSql;
	static String date;
	//外部檔案
    static String propertiesFile = "./suipConnect_Bruce.properties";
//    static String propertiesFile = "C:\\Syscom\\TCBFEP\\source\\fep\\fep-suipConnect\\suipConnect_Bruce.properties";
    static Properties properties;
	//最原始的電文
	static String originals;
	//產生terminal檔案路徑
	static String terminalPath;
	//只拿1跟5的隨機數字
	static int num;
	//失敗幾筆
	static int field = 0;
    //成功幾筆
	static int success = 0;
	
	static List<String> terminalIdList = null;

	//transSeq 時間後面抓4 兩種：提款、轉帳，共100筆，各50
	public static void main(String args[]) {
        try {
            SpringApplication application = new SpringApplication(ConnectSuipChangeDateAndMakeMac.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.run(args);
        } catch (Exception e) {
            if ("org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException".equals(e.getClass().getName())) {
                // ignore
            } else {
//                logger.exceptionMsg(e, "SyscomFepSuipConnectApplication run failed!!!");
            }
        } finally {
            System.exit(0);
        }	
	}
	
	@Override
    public void run(String... args) throws Exception {
		//程式啟動就要先吃properties
		initProperties();                                                          
    	BufferedReader terminalRead = null;
    	
    	List<String> originalList = null;
    	InputStreamReader originalInput = null;
    	BufferedReader originalRead = null;
    	
		File tempFile = null;
		FileWriter tempFileWriter = null;
		try {
        	System.out.println("terminalIdList.size:" + terminalIdList.size());
        	//讀取original.txt：目的，壓MAC
        	originalInput = new InputStreamReader(new FileInputStream(originals), "UTF-8");
        	originalRead = new BufferedReader(originalInput);
        	originalList = new ArrayList<String>();
        	while (originalRead.ready()) {
    			String original = originalRead.readLine();
                if (StringUtils.isNotBlank(original)) {
                	originalList.add(original);
            	}
            }
        	System.out.println("originalList.size:" + originalList.size());
        	//產生terminal檔
//        	String changeMasterKey = "ChangeMasterKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2D4";
//        	String ChangeWorkingKey = "ChangeWorkingKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2C3";
        	String newTel = "";//最新電文
        	for(String terminal : terminalIdList) {
        	    //計算TA轉帳有幾筆
        	    int ta = 0;
        	    //計算F2提款有幾筆
        	    int f2 = 0;
        		tempFile = new File(terminalPath + terminal+".txt");
//        		tempFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile, true), "UTF-8"));
        		tempFileWriter = new FileWriter(tempFile);
        		//要跑50圈，提款、轉帳各50
        		for (int j = 0 ; j < 100 ; j++) {
        			//取1及5的奇數隨機數
        			getRandom();
        			if(num == 1 && f2 < 25) {//f2提款電文只跑25次
        				f2++;
            			newTel = getNewTel(originalList.get(num),terminal,String.valueOf(j + 1));
                		tempFileWriter.append(originalList.get(num - 1) + ";" + newTel);
                		tempFileWriter.write("\r\n");
            			newTel = getNewTel(originalList.get(num + 2),terminal,String.valueOf(j + 1));
            			tempFileWriter.append(originalList.get(num + 1) + ";" + newTel);
                		tempFileWriter.write("\r\n");
        			}else if(num == 5 && ta < 25) {//ta轉帳電文只跑25次
        				ta++;
            			newTel = getNewTel(originalList.get(num),terminal,String.valueOf(j + 1));
                		tempFileWriter.append(originalList.get(num - 1) + ";" + newTel);
                		tempFileWriter.write("\r\n");
            			newTel = getNewTel(originalList.get(num + 2),terminal,String.valueOf(j + 1));
            			tempFileWriter.append(originalList.get(num + 1) + ";" + newTel);
                		tempFileWriter.write("\r\n");
        			}
        			//重組電文：換日期、換terminal、換transSeq
        			if(f2 == 25 && ta == 25) {
        				break;
        			}
        		}
        		tempFileWriter.flush(); 
        	}
        	System.out.println("成功："+ success +"筆,失敗："+ field +"筆" );
        	System.out.println("電文產生完畢");
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (terminalRead != null) {					
					terminalRead.close();
				}
				if (originalRead != null) {					
					originalRead.close();
				}
				if (tempFileWriter != null) {
					tempFileWriter.close();
				}     		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}	
    }
	
	//取得奇數亂數
	private static void getRandom() {
		Random r = new Random();
		num = r.nextInt(5) + 1;
		if (num != 1 && num != 5) {
			getRandom();
		}
	}
	
	//取得換好日期、transSeq、terminal
	private static String getNewTel(String tel,String newTerminal,String j) {
		String changeDate = EbcdicConverter.toHex(CCSID.English,12, date);//date;//tel.substring(40,52);//換日期
		String changeTransSeq = EbcdicConverter.toHex(CCSID.English,4, StringUtils.leftPad(j, 4, '0'));//tel.substring(52,60);//換transSeq
		String changeTerminal = EbcdicConverter.toHex(CCSID.English,8, newTerminal);//tel.substring(126,142);//換terminal
		//String defectMessage = frontmost + changeDate + changeSeq + inFrontOfTerminal + changeTerminal + rearmost;//尚未壓MAC的完整電文
//		String defectMessage = tel.substring(0,40) + changeDate + changeTransSeq + tel.substring(60,126) + changeTerminal + tel.substring(142);//尚未壓MAC的完整電文
		String defectMessage = tel.substring(0,40) + changeDate + changeTransSeq + tel.substring(72,126) + changeTerminal + tel.substring(142);//尚未壓MAC的完整電文
		String keyId = "1S1MAC ATM   " + newTerminal + " 1";
//		String keyId = "1S1MAC ATM   T9997S01 1";
		//開始壓mac，長度是480	
		return getCompleteTel(defectMessage ,keyId);
	}
	
	//送suip壓MAC
	private static String getCompleteTel(String defectMessage ,String keyId) {	
		String inputStr1 = defectMessage.substring(36,514);//要送suip的段落
		String inputStr2 = "";
		String ebcmac = "";
        //-------------suip基本參數設定
        ENCLogData encLog = new ENCLogData();
        if (StringUtils.isNotBlank(ej)) {
            int eji = Integer.valueOf(ej);
            encLog.setEj(eji);
        }
        int encRetryCount = 0;
        int encLibRetryInterval = 0;
        int suipTimeout = 0;
        if (StringUtils.isNotBlank(ej)) {
            encRetryCount = Integer.valueOf(encRetryCountStr);
            encLibRetryInterval = Integer.valueOf(encLibRetryIntervalStr);
            suipTimeout = Integer.valueOf(suipTimeoutStr);
        }

        encLog.setSubSys(subSys);
        encLog.setProgramName(programName);
        encLog.setChannel(channel);
        if ("Request".equals(messageFlowType)) {
            encLog.setMessageFlowType(ENCMessageFlow.Request);
        }
        if ("Response".equals(messageFlowType)) {
            encLog.setMessageFlowType(ENCMessageFlow.Response);
        }
        encLog.setMessageId(messageId);
        encLog.setHostIp(hostIp);
        encLog.setHostName(hostName);
        encLog.setSystemId(systemId); // 塞入systemId
        encLog.setHostIp(hostIp);
        encLog.setHostName(hostName);
        encLog.setStep(Integer.valueOf(step));
        //------------suip基本參數設定 end
        RefString o1 = new RefString();
        RefString o2 = new RefString();
        int rtn = -1;
        inputStr1 = TrimSpace(inputStr1);  //去掉多餘40
        inputStr1 = remainderToF0(inputStr1);
        inputStr1 = StringUtils.join(StringUtils.leftPad(Integer.toString(inputStr1.length()), 4, '0'), inputStr1);
        //呼叫 suip  塞入相對應的參數
        ENCLib encLib = new ENCLib(encLog, encRetryCount, encLibRetryInterval, suipAddress, suipTimeout);
        //取得suip回傳的參數
        rtn = encLib.callSuip(encFunc, keyId, inputStr1, inputStr2, o1, o2);
        //壓MAC成功、失敗筆數
        if(rtn == 0) {
        	success++;
        }else {
        	field++;
        }
        //把mac的前面計算長度替換掉
        if(StringUtils.isNotBlank(o1.get())) {
        	//suip回傳回來的output1 因為前面有0008是不用的所以把它刪掉
        	String o1toEbc = o1.get().replace("0008", "");
        	//suip回傳回來的output1 因為前面有0016是不用的所以把它刪掉
            o1toEbc = o1toEbc.replace("0016", "");
            //應SA的需求，只抓前八碼ASCII 轉成EBCDIC格式加回電文裡面
            if(o1toEbc.length() > 8) {
            	o1toEbc = o1toEbc.substring(0,8);
            	ebcmac = EbcdicConverter.toHex(CCSID.English, o1toEbc.length(), o1toEbc);
            }
        }
        //FN000313, RC:0, Output1:0008FFFFFFFF, Output2:
        System.out.println(encFunc + ", RC:" + rtn + ", Output1:" + o1.get() + ", Output2:" + o2.get() + ", Output1toEbcdic:" + ebcmac);
        return defectMessage.substring(0,742) + ebcmac + defectMessage.substring(758);
	}
	
	//初始化所有資訊
	private static void initProperties() {
		properties = new Properties();
	    Connection con;
	    Statement stmt;
	    ResultSet rs;
		try {
	        
	        InputStreamReader isr2 = new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8");
	        BufferedReader bufferedReader = new BufferedReader(isr2);
	        properties.load(bufferedReader);
	    	//判斷吃哪裡的檔案
	        boolean localhostFile = Boolean.valueOf(properties.getProperty("localhostFile"));
	        if(localhostFile) {
	        	originals = properties.getProperty("localOriginals");
	        	terminalPath = properties.getProperty("localTerminalPath");
	        }else {
	        	originals = properties.getProperty("originals");
	        	terminalPath = properties.getProperty("terminalPath");
	        }
	    	ej = properties.getProperty("ej");//"31770600";
	    	encRetryCountStr = properties.getProperty("encRetryCount");//"0";
	    	encLibRetryIntervalStr = properties.getProperty("encLibRetryInterval");//"0";
	    	suipTimeoutStr = properties.getProperty("suipTimeout");//"15";
	    	subSys = properties.getProperty("subSys");//"";
	    	programName = properties.getProperty("programName");//"ENCHelper.checkAtmMac";
	    	channel = properties.getProperty("channel");//"";
	    	messageFlowType = properties.getProperty("MessageFlowType");//"";
	    	messageId = properties.getProperty("MessageId");//"";
	    	hostIp = properties.getProperty("hostIp");//"127.0.0.1";
	    	hostName = properties.getProperty("hostName");//"centos8.host";
	    	systemId = properties.getProperty("systemId");//"FEP10";
	    	step = properties.getProperty("step");//"3";
	    	encFunc = "FN000313";
	    	date = properties.getProperty("date");
	    	//String suipAddress = "10.3.101.3:13931;10.3.101.3:8002";
			boolean isConnectTcbSuip = Boolean.valueOf(properties.getProperty("isConnect_Tcb_Suip"));
			if(isConnectTcbSuip) {
				suipAddress = properties.getProperty("suipAddress_Tcb");//"127.0.0.1:13931;127.0.0.1:8002";
			}else {
				suipAddress = properties.getProperty("suipAddress_Syscom");//"127.0.0.1:13931;127.0.0.1:8002";
			}
			boolean isConnectToDb2 = Boolean.valueOf(properties.getProperty("isConnectToDb2"));
			boolean isConnectTCBDb = Boolean.valueOf(properties.getProperty("isConnectTCBDb"));
	        terminalIdList = new ArrayList<String>();
//	        從資料庫拿terminalID
	        if (isConnectToDb2) {
	        	if(isConnectTCBDb) {
	        	    url = properties.getProperty("url_Tcb");
	        	    user= properties.getProperty("user_Tcb");
	        	    password= properties.getProperty("password_Tcb");
	        	    selectTerminalSql = properties.getProperty("select_Sql_Tcb");
	        	}else {
	        	    url = properties.getProperty("url_Syscom");
	        	    user= properties.getProperty("user_Syscom");
	        	    password= properties.getProperty("password_Syscom");
	        	    selectTerminalSql = properties.getProperty("select_Sql_Syscom"); 
	        	}
//				terminalsInput = new InputStreamReader(new FileInputStream(terminalIds), "UTF-8");
//		        terminalRead = new BufferedReader(terminalsInput);
		        Class.forName("com.ibm.db2.jcc.DB2Driver");    
		        System.out.println("**** Loaded the JDBC driver");
		        con = (Connection) DriverManager.getConnection (url, user, password); 
		        stmt = con.createStatement();                                          
		        System.out.println("**** Created JDBC Statement object");
		        rs = (ResultSet) stmt.executeQuery(selectTerminalSql);                   
		        System.out.println("**** Created JDBC ResultSet object");
		        while (rs.next()) {
		        	terminalIdList.add(rs.getString(1));
//		            empNo = rs.getString(1);
//		            System.out.println("Employee number = " + empNo);
		        }
//	        	while (terminalRead.ready()) {
//	    			String terminal = terminalRead.readLine();
//	                if (StringUtils.isNotBlank(terminal) && terminal.startsWith("T")) {
//	                	terminalIdList.add(terminal);
//	            	}
//	            }
	        }else {
		        String[] terminals = properties.getProperty("terminalId").split(",");
		        for(String terminal : terminals) {
		        	terminalIdList.add(terminal);
		        }
	        }
		}catch(Exception e) {
			
		}
	}
	
    private static String TrimSpace(String data) {
		String result = "";

		for(int i = 0;i< data.length(); i += 2 ) {
			
			String tmp = data.substring(i, i+2);
			//System.out.println(tmp);
			if(i == 0) {
				result += tmp;				
			}
			else {
				if(tmp.equals("40")) {
					if(!result.substring(result.length() - 2, result.length()).equals("40")){
						result += tmp;
					}
				}
				else {
					result += tmp;
				}
			}			
		}
		return result;
	}
    
    /**
     * 把字串補足至 8 的倍數 ，不足以F0補足
     *
     * @param cbsmac
     * @return
     */
    private static String remainderToF0(String inputData) {
        String rtnStr = "";
        int instr = inputData.length();
        instr = instr / 2;
        if (instr % 8 != 0) {
            int remainder = instr % 8;
            remainder = 8 - remainder;
            rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
            rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
        }

        return inputData + rtnStr;
    }
}
