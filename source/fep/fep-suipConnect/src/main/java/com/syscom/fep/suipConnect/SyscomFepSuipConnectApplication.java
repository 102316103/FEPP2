package com.syscom.fep.suipConnect;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.enums.ENCMessageFlow;
import com.syscom.fep.enclib.vo.ENCLogData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"com.syscom.fep"})
@StackTracePointCut(caller = SvrConst.SVR_SUIP_CONNECT)
public class SyscomFepSuipConnectApplication  {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    
    Properties properties = null;
    String ej;
    String subSys;
    String programName;
    String channel;
    String messageFlowType;
    String messageId;
    String systemId;
    String hostIp;
    String hostName;
    String encRetryCountStr;
    String encLibRetryIntervalStr;
    String suipAddress;
    String suipTimeoutStr;
    String step;
    String pattern;
    String date;

    static {
//        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(SyscomFepSuipConnectApplication.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.run(args);
        } catch (Exception e) {
            if ("org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException".equals(e.getClass().getName())) {
                // ignore
            } else {
                logger.exceptionMsg(e, "SyscomFepSuipConnectApplication run failed!!!");
            }
        } finally {
            System.exit(0);
        }
    }
    
    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    //@Override
    public void run3(String... args) throws Exception {
    	BufferedWriter fw = null;
    	BufferedWriter fw473X = null;
        try {
            // FileReader fr = new FileReader("./suipData.txt");
        	//拿來壓MAC或改日期的檔案檔
            InputStreamReader isr = new InputStreamReader(new FileInputStream("./473XData.txt"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //最原始的完整473X電文檔案
            InputStreamReader isr3 = new InputStreamReader(new FileInputStream("./originalData.txt"), "UTF-8");
            BufferedReader originalData = new BufferedReader(isr3);
            Properties properties = new Properties();
            //參數設定檔
            InputStreamReader isr2 = new InputStreamReader(new FileInputStream("./suipConnect.properties"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr2);
            properties.load(bufferedReader);
            String ej = properties.getProperty("ej");
            String subSys = properties.getProperty("subSys");
            String programName = properties.getProperty("programName");
            String channel = properties.getProperty("channel");
            String messageFlowType = properties.getProperty("MessageFlowType");
            String messageId = properties.getProperty("MessageId");
            String systemId = properties.getProperty("systemId");
            String hostIp = properties.getProperty("hostIp");
            String hostName = properties.getProperty("hostName");
            String encRetryCountStr = properties.getProperty("encRetryCount");
            String encLibRetryIntervalStr = properties.getProperty("encLibRetryInterval");
            String suipAddress = properties.getProperty("suipAddress");
            String suipTimeoutStr = properties.getProperty("suipTimeout");
            String step = properties.getProperty("step");
            String pattern = properties.getProperty("pattern");
            String date = properties.getProperty("date");
            
        	String selstr = "";
        	//拿來壓MAC的長度
            String length = properties.getProperty("length");
            //從ebcdic格式電文第34格(C6C1C1)擷取到MAC前的值的位置,共抓取多長的值,目前邏輯為抓480長度,C6C1C1轉成ASCII的值是FAA
            int macprefixindex = Integer.valueOf(length);
            //mac的位置在ebcdic第743格
            int lastindex = 742;
        	//mac後面的ebcdic的內容，最終的電文組成為prefix+in1+macprefix+ebcmac+macsuffix
        	String macsuffix = "";
        	//mac前面的ebcdic的內容，最終的電文組成為prefix+in1+macprefix+ebcmac+macsuffix
        	String macprefix = "";
        	//ebcdic格式電文C6C1C1的位置
        	int strint = 34;
        	
        	//原始電文 0~34  +   我們拿來改成當天日期跟MAC的中間480長度電文   +   我們壓出來的 新MAC (第741~749)ECBDIC會乘2的長度(16格) + MAC後面的原始電文內容
        	
//        	mac前的位置，但壓mac統一到480
        	//這些電文因為mac的位置不在743，所以抓取的位置比較不一樣
//        	int p3pp = 488;
//        	int ppp1 = 488;
        	int p5p6 = 326;
        	int p1macprefix = 596;
        	int p5p6macprefix = 360;
        	int ppp3macprefix = 522;
        	int d8i5macprefix = 730;
        	int d8i5macsuffix = 746;
        	//國際卡的mac前的位置
        	int internationlastindex = 750;
        	int internationmacsuffix = 766;
        	
        	
        	
            //壓MAC
        	if("createMac".equals(pattern)) {	
        		//最後會把 原始電文 + 新壓的日期跟MAC + 後半段原始電文 組合出來的結果產出在template.txt
        		File filetemp = new File("./template.txt");
        		FileWriter fileWriter =new FileWriter(filetemp);
        	    fileWriter.write("");
        	    fileWriter.flush();
        	    fileWriter.close();
//        	    File file = new File("./template.txt");
//                fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
//        	    fw.append("ChangeMasterKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2D4");
//                fw.newLine();
//                fw.flush();
//                fw.append("ChangeWorkingKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2C3");
//                fw.newLine();
//                fw.flush();
              //更改測試日期
            }else if("changeDate".equals(pattern)) {
            	File filetemp = new File("./473XData_changeDate.txt");
        		FileWriter fileWriter =new FileWriter(filetemp);
        	    fileWriter.write("");
        	    fileWriter.flush();
        	    fileWriter.close();
            }
//        	String title = "";
        	List<String> originalDataList = new ArrayList<String>();
        	while (originalData.ready()) {
    			String originalDatavalue = originalData.readLine();
                try {
                    if (StringUtils.isNotBlank(originalDatavalue)) {
                    	originalDataList.add(originalDatavalue);
                	}
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception ");
                }
                
            }
        	
        	//拿來壓MAC或改日期的檔案檔
    	    File file = new File("./template.txt");
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            //產生出更改日期後檔案的檔名
    	    File file473X = new File("./473XData_changeDate.txt");
            fw473X = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file473X, true), "UTF-8"));
        	
            while (br.ready()) {
                String value = br.readLine();
                String encFunc = null;
                try {
                    if (StringUtils.isNotBlank(value)) {
                    	//壓MAC
                    	if("createMac".equals(pattern)) {
                            String[] array = value.split(",");
                            char ch = ',';
                            long count = 0;
                            if(array.length>1) {
                            	try {
                            		count = value.chars().filter(c -> c == ch).count();
                            	} catch (Exception e) {
                                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                                }
                            }
                            
                            //壓mac的區域  //確認傳送進來的參數有四個,三個逗號
                            if (count == 3) {
                                int ix = array.length;
                                encFunc = array[0];
                                String keyid = array[1];
                                String inputStr1 = "";
                                String inputStr2 = "";
                                //參數理論上有四個  防止爆掉
                                if (ix >= 3)
                                    inputStr1 = array[2];
                                if (ix >= 4)
                                    inputStr2 = array[3];
                                
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
                                }
                                if (StringUtils.isNotBlank(ej)) {
                                    encLibRetryInterval = Integer.valueOf(encLibRetryIntervalStr);
                                }
                                if (StringUtils.isNotBlank(ej)) {
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
                                //呼叫 suip  塞入相對應的參數
                                ENCLib encLib = new ENCLib(encLog, encRetryCount, encLibRetryInterval, suipAddress, suipTimeout);
                                //取得suip回傳的參數
                                rtn = encLib.callSuip(encFunc, keyid, inputStr1, inputStr2, o1, o2);
                                String ebcmac = "";
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
                                String fwvalue = "";
                                //FN000313, RC:0, Output1:0008FFFFFFFF, Output2:
                                System.out.println(encFunc + ", RC:" + rtn + ", Output1:" + o1.get() + ", Output2:" + o2.get() + ", Output1toEbcdic:" + ebcmac);
                                if(StringUtils.isNotBlank(selstr)) {
                                	String in1 = inputStr1.substring(4);
                                	fwvalue = selstr+in1+ebcmac;
                                    fw.append(fwvalue);
                                    fw.flush(); 
                                    selstr="";
                                }else {
                                	String in1 = inputStr1.substring(4);
                                    String prefix = "C9C2D7C2D4F0F0F040F9F9F8F2F1F0F140";
                                    fwvalue = prefix+in1+macprefix+ebcmac;
                                    fw.append(fwvalue);
                                    fw.flush(); 
                                }
                                fwvalue = fwvalue+macsuffix;
                                //MAC後面的EBDC電文
                                fw.append(macsuffix);
                                fw.newLine();
                                fw.flush(); 
//                                System.out.println("fwvalue:"+fwvalue);
                              //補摺
                            }else if(value.length() >= 18 && "C5C2E3E2D7D4F0F0".equals(value.substring(0, 16))) {
                            	fw.append(value);
                                fw.newLine();
                                fw.flush(); 
//                              //FV指靜脈 FSCODE=FV，FP  ，FP="C6D7".equals(value.substring(146, 150))  C6C3F7 = FC7
                            }else if(value.length() >= 150 && "C6E5".equals(value.substring(146, 150)) && !"C6C3F7".equals(value.substring(34, 40))){
                            	fw.append(value);
                                fw.newLine();
                                fw.flush(); 
                            }else if(value.contains("ChangeMasterKey") || value.contains("ChangeWorkingKey")) {
                            	fw.append(value);
                                fw.newLine();
                                fw.flush();
                             //這個區塊是473XData.txt標題列   
                            } else {
                            	
//                            	System.out.println("exit while:");
                            	
                            	macprefix = "";
                            	macsuffix = "";
                            	
                            	
                            	if(value.contains("I1晶片卡餘額查詢-自行-1")) {
                            		macprefix = originalDataList.get(1).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(1).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-自行-3成功")) {
                            		macprefix = originalDataList.get(3).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(3).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-自行-3失敗")) {
                            		macprefix = originalDataList.get(5).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(5).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-1")) {
                            		macprefix = originalDataList.get(413).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(413).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-3成功")) {
                            		macprefix = originalDataList.get(415).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(415).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-3失敗")) {
                            		macprefix = originalDataList.get(417).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(417).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-1")) {
                            		macprefix = originalDataList.get(7).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(7).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-3成功")) {
                            		macprefix = originalDataList.get(9).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(9).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-3失敗")) {
                            		macprefix = originalDataList.get(11).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(11).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-1")) {
                            		macprefix = originalDataList.get(13).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(13).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-3成功")) {
                            		macprefix = originalDataList.get(15).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(15).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-3失敗")) {
                            		macprefix = originalDataList.get(17).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(17).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-1")) {
                            		macprefix = originalDataList.get(19).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(19).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-3成功")) {
                            		macprefix = originalDataList.get(21).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(21).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-3失敗")) {
                            		macprefix = originalDataList.get(23).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(23).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-1")) {
                            		macprefix = originalDataList.get(25).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(25).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-3成功")) {
                            		macprefix = originalDataList.get(27).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(27).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-3失敗")) {
                            		macprefix = originalDataList.get(29).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(29).substring(758);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-1")) {
                            			macprefix = originalDataList.get(31).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(31).substring(d8i5macsuffix);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-3成功")) {
                            			macprefix = originalDataList.get(33).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(33).substring(d8i5macsuffix);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-3失敗")) {
                            			macprefix = originalDataList.get(35).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(35).substring(d8i5macsuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-1")) {
                            			macprefix = originalDataList.get(37).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(37).substring(d8i5macsuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-3成功")) {
                            			macprefix = originalDataList.get(39).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(39).substring(d8i5macsuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-3失敗")) {
                            			macprefix = originalDataList.get(41).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(41).substring(d8i5macsuffix);
                            	}else if(value.contains("F2晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(43).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(43).substring(758);
                            	}else if(value.contains("F2晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(45).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(45).substring(758);
                            	}else if(value.contains("F2晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(47).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(47).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(49).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(49).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(51).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(51).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(53).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(53).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(55).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(55).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(57).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(57).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(59).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(59).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(61).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(61).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(63).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(63).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(65).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(65).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(395).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(395).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(397).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(397).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(399).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(399).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(67).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(67).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(69).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(69).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(71).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(71).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(419).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(419).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(421).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(421).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(423).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(423).substring(758);
                            	}else if(value.contains("W2無卡提-自行款-1")) {
                            		macprefix = originalDataList.get(73).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(73).substring(758);
                            	}else if(value.contains("W2無卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(75).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(75).substring(758);
                            	}else if(value.contains("W2無卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(77).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(77).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-1")) {
                            		macprefix = originalDataList.get(79).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(79).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-3成功")) {
                            		macprefix = originalDataList.get(81).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(81).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(83).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(83).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-1")) {
                            		macprefix = originalDataList.get(389).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(389).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(391).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(391).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(393).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(393).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-1")) {
                            		macprefix = originalDataList.get(85).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(85).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-3成功")) {
                            		macprefix = originalDataList.get(87).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(87).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(89).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(89).substring(758);
                            	}else if(value.contains("F-C8(WF的前置交易)")) {
                            		macprefix = originalDataList.get(91).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(91).substring(758);
                            	}else if(value.contains("F-C9(WF的前置交易)")) {
                            		macprefix = originalDataList.get(93).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(93).substring(758);
                            	}else if(value.contains("WF指靜脈提款-1")) {
                            		macprefix = originalDataList.get(95).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(95).substring(758);
                            	}else if(value.contains("WF指靜脈提款-3成功")) {
                            		macprefix = originalDataList.get(97).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(97).substring(758);
                            	}else if(value.contains("WF指靜脈提款-3失敗")) {
                            		macprefix = originalDataList.get(99).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(99).substring(758);
                            	}else if(value.contains("FCC(IC的前置交易)-1")) {
                            		macprefix = originalDataList.get(101).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(101).substring(758);
                            	}else if(value.contains("IC的案例只有失敗沒有成功，此為ATM交易裡的垃圾案例，舊卡啟用新卡(前置)查詢新卡卡別-1失敗")) {
                            		macprefix = originalDataList.get(105).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(105).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-1")) {
                            		macprefix = originalDataList.get(107).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(107).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-3成功")) {
                            		macprefix = originalDataList.get(109).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(109).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-3失敗")) {
                            		macprefix = originalDataList.get(111).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(111).substring(758);
                            	}else if(value.contains("FC2-下送一次的T1交易-1")) {
                            		macprefix = originalDataList.get(113).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(113).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-1")) {
                            		macprefix = originalDataList.get(115).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(115).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-3成功")) {
                            		macprefix = originalDataList.get(117).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(117).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-3失敗")) {
                            		macprefix = originalDataList.get(119).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(119).substring(758);
                            	}else if(value.contains("FC2-下送兩次的T1交易-1")) {
                            		macprefix = originalDataList.get(121).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(121).substring(758);
                            	}else if(value.contains("FC2-下送兩次的T1交易-3")) {
                            		macprefix = originalDataList.get(123).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(123).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-1")) {
                            		macprefix = originalDataList.get(125).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(125).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-3-成功")) {
                            		macprefix = originalDataList.get(127).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(127).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-3-失敗")) {
                            		macprefix = originalDataList.get(129).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(129).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-1")) {
                            		macprefix = originalDataList.get(131).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(131).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-3成功")) {
                            		macprefix = originalDataList.get(133).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(133).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-3失敗")) {
                            		macprefix = originalDataList.get(135).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(135).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -1")) {
                            		macprefix = originalDataList.get(137).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(137).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -3成功")) {
                            		macprefix = originalDataList.get(139).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(139).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -3失敗")) {
                            		macprefix = originalDataList.get(141).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(141).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -1")) {
                            		macprefix = originalDataList.get(143).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(143).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -3成功")) {
                            		macprefix = originalDataList.get(145).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(145).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -3失敗")) {
                            		macprefix = originalDataList.get(147).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(147).substring(758);
                            	}else if(value.contains("FCZ-AW的前置交易-1")) {
                            		macprefix = originalDataList.get(149).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(149).substring(758);
                            	}else if(value.contains("AW無卡提款服務-1")) {
                            		macprefix = originalDataList.get(151).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(151).substring(758);
                            	}else if(value.contains("AW無卡提款服務-3成功")) {
                            		macprefix = originalDataList.get(153).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(153).substring(758);
                            	}else if(value.contains("AW無卡提款服務-3失敗")) {
                            		macprefix = originalDataList.get(155).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(155).substring(758);
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-1")) {
                            		macprefix = originalDataList.get(157).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-3成功")) {
                            		macprefix = originalDataList.get(159).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-3失敗")) {
                            		macprefix = originalDataList.get(161).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("F-C1-P1磁條卡密碼變更-1")) {
                            		macprefix = originalDataList.get(163).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-1")) {
                            		macprefix = originalDataList.get(165).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-3成功")) {
                            		macprefix = originalDataList.get(167).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-3失敗")) {
                            		macprefix = originalDataList.get(169).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("FC4-P4的前置交易-1")) {
                            	
                            		macprefix = originalDataList.get(171).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(171).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-1")) {
                            	
                            		macprefix = originalDataList.get(173).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(173).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-3成功")) {
                            	
                            		macprefix = originalDataList.get(175).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(175).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-3失敗")) {
                            	
                            		macprefix = originalDataList.get(177).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(177).substring(758);
                            	}else if(value.contains("FC2-TD的前置交易-1")) {
                            	
                            		macprefix = originalDataList.get(179).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(179).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-1")) {
                            	
                            		macprefix = originalDataList.get(181).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(181).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-3成功")) {
                            	
                            		macprefix = originalDataList.get(183).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(183).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-3失敗")) {
                            		macprefix = originalDataList.get(185).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(185).substring(758);
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-1")) { //length:376
                            	
                            		macprefix = originalDataList.get(187).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-3成功")) {
                            	
                            		macprefix = originalDataList.get(189).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(191).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-1")) {
                            	
                            		macprefix = originalDataList.get(193).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-3成功")) {
                            	
                            		macprefix = originalDataList.get(195).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-3失敗")) {
                            		macprefix = originalDataList.get(197).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-1")) {
                            		
                            		macprefix = originalDataList.get(199).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(199).substring(758);
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-3成功")) {
                            	
                            		macprefix = originalDataList.get(201).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(201).substring(758);
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(203).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(203).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-1")) {
                            	
                            		macprefix = originalDataList.get(205).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(205).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-3成功")) {
                            	
                            		macprefix = originalDataList.get(207).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(207).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(209).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(209).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -1")) {
                            	
                            		macprefix = originalDataList.get(211).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(211).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -3成功")) {
                            	
                            		macprefix = originalDataList.get(213).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(213).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -3失敗")) {
                            	
                            		macprefix = originalDataList.get(215).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(215).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 A銀行 =>A銀行-1")) {
                            	
                            		macprefix = originalDataList.get(217).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(217).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 A銀行 =>A銀行-3成功")) {
                            	
                            		macprefix = originalDataList.get(219).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(219).substring(758);
                            	
                            		macprefix = originalDataList.get(221).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(221).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-1")) {
                            	
                            		macprefix = originalDataList.get(223).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(223).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-3成功")) {
                            	
                            		macprefix = originalDataList.get(225).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(225).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(227).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(227).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-1")) {
                            	
                            		macprefix = originalDataList.get(229).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(229).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-3成功")) {
                            	
                            		macprefix = originalDataList.get(231).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(231).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-3失敗")) {
                            	
                            		macprefix = originalDataList.get(233).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(233).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-1")) {
                            	
                            		macprefix = originalDataList.get(235).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(235).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(237).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(237).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(239).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(239).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-1")) {
                            	
                            		macprefix = originalDataList.get(241).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(241).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(243).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(243).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(245).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(245).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-1")) {
                            	
                            		macprefix = originalDataList.get(247).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(247).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(249).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(249).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(251).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(251).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-1")) {
                            	
                            		macprefix = originalDataList.get(253).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(253).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-3成功")) {
                            	
                            		macprefix = originalDataList.get(255).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(255).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(257).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(257).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -1")) {
                            	
                            		macprefix = originalDataList.get(259).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(259).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -3成功")) {
                            	
                            		macprefix = originalDataList.get(261).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(261).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -3失敗")) {
                            	
                            		macprefix = originalDataList.get(263).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(263).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-1")) {
                            	
                            		macprefix = originalDataList.get(265).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(265).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-3成功")) {
                            	
                            		macprefix = originalDataList.get(267).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(267).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(269).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(269).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-1")) {
                            	
                            		macprefix = originalDataList.get(271).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(271).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(273).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(273).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-3失敗")) {
                            		macprefix = originalDataList.get(275).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(275).substring(758);
                            		
                            	}else if(value.contains("FC7-FV的前置交易-1")) {
                            		macprefix = originalDataList.get(309).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(309).substring(758);
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush(); 
                            		
                            	}else if(value.contains("FV指靜脈建置-1")) {
                            	
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush();
                            	}else if(value.contains("FV指靜脈建置-3成功")) {
                            	
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush();
                            	}else if(value.contains("FV指靜脈建置-3失敗")) {
                            	
                            	}else if(value.contains("FP指靜脈密碼設定-1")) {
                            	
                            		macprefix = originalDataList.get(317).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(317).substring(758);
                            	}else if(value.contains("FP指靜脈密碼設定-3成功")) {
                            	
                            		macprefix = originalDataList.get(319).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(319).substring(758);
                            	}else if(value.contains("FP指靜脈密碼設定-3失敗")) {
                            	
                            		macprefix = originalDataList.get(321).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(321).substring(758);
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-1")) {
                            	
                            		macprefix = originalDataList.get(323).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-3成功")) {
                            	
                            		macprefix = originalDataList.get(325).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-3失敗")) {
                            	
                            		macprefix = originalDataList.get(327).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -1")) {
                            	
                            		macprefix = originalDataList.get(329).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(329).substring(758);
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -3成功")) {
                            	
                            		macprefix = originalDataList.get(331).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(331).substring(758);
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -3失敗")) {
                            		macprefix = originalDataList.get(333).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(333).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-1")) {
                            		macprefix = originalDataList.get(335).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(335).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-3成功")) {
                            		macprefix = originalDataList.get(337).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(337).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-3失敗")) {
                            		macprefix = originalDataList.get(339).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(339).substring(758);
                            	}else if(value.contains("SW銀聯卡提款-1")) {
                            		macprefix = originalDataList.get(341).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(341).substring(internationmacsuffix);
                            	}else if(value.contains("SW銀聯卡提款-3成功")) {
                            		macprefix = originalDataList.get(343).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(343).substring(internationmacsuffix);
                            	}else if(value.contains("SW銀聯卡提款-3失敗")) {
                            		macprefix = originalDataList.get(345).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(345).substring(internationmacsuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-1")) {
                            		macprefix = originalDataList.get(347).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(347).substring(internationmacsuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-3成功")) {
                            		macprefix = originalDataList.get(349).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(349).substring(internationmacsuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-3失敗")) {
                            		macprefix = originalDataList.get(351).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(351).substring(internationmacsuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-1")) {
                            		macprefix = originalDataList.get(353).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(353).substring(internationmacsuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-3成功")) {
                            		macprefix = originalDataList.get(355).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(355).substring(internationmacsuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-3失敗")) {
                            		macprefix = originalDataList.get(357).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(357).substring(internationmacsuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-1")) {
                            		macprefix = originalDataList.get(359).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(359).substring(internationmacsuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-3成功")) {
                            		macprefix = originalDataList.get(361).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(361).substring(internationmacsuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-3失敗")) {
                            		macprefix = originalDataList.get(363).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(363).substring(internationmacsuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-1")) {
                            		macprefix = originalDataList.get(365).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(365).substring(internationmacsuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-3成功")) {
                            		macprefix = originalDataList.get(367).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(367).substring(internationmacsuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-3失敗")) {
                            		macprefix = originalDataList.get(369).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(369).substring(internationmacsuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-1")) {
                            		macprefix = originalDataList.get(371).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(371).substring(internationmacsuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-3成功")) {
                            		macprefix = originalDataList.get(373).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(373).substring(internationmacsuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-3失敗")) {
                            		macprefix = originalDataList.get(375).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(375).substring(internationmacsuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-1")) {
                            		macprefix = originalDataList.get(377).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(377).substring(internationmacsuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-3成功")) {
                            		macprefix = originalDataList.get(379).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(379).substring(internationmacsuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-3失敗")) {
                            		macprefix = originalDataList.get(381).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(381).substring(internationmacsuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-1")) {
                            		macprefix = originalDataList.get(383).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(383).substring(internationmacsuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-3成功")) {
                            		macprefix = originalDataList.get(385).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(385).substring(internationmacsuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-3失敗")) {
                            		macprefix = originalDataList.get(387).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(387).substring(internationmacsuffix);
                            	}else if(value.contains("F5晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(401).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(401).substring(758);
                            	}else if(value.contains("F5晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(403).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(403).substring(758);
                            	}else if(value.contains("F5晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(405).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(405).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(407).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(407).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(409).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(409).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(411).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(411).substring(758);
                            	}
//                            	System.out.println("macprefix"+macprefix);
//                            	System.out.println("title:"+value);
                            	//標題
                            	fw.append(value+";");
                                fw.flush(); 
                            }
                            //更改測試日期
                    	}else if("changeDate".equals(pattern)) {
                            char ch = ',';
                            long count = 0;
                            	try {
                            		count = value.chars().filter(c -> c == ch).count();
                            	} catch (Exception e) {
                                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                                }
                            	//確認傳送進來的參數有四個,三個逗號
                            if (count == 3) {
                            	String lns = value.substring(33, 37);
                            	String orldate = value.substring(43, 55);
                            	if (StringUtils.isNotBlank(date)) {
                            		String newString = "";
                            		String ebcdate = EbcdicConverter.toHex(CCSID.English, date.length(), date);
                            		newString = value.substring(0, 43) + ebcdate+ value.substring(55);
                            		if("0480".equals(lns)) {
                            	        if(value.substring(417, 430).equals(orldate)) {
                            	        	newString = newString.substring(0, 416) + ebcdate+ newString.substring(430);
                            	        }
                            	        // PP
                            		}else if("0488".equals(lns)) {  
                            			//P5 P6
                                	}else if("0326".equals(lns)) {
                                	}
                            		
                            		fw473X.append(newString);
                                    fw473X.newLine();
                                    fw473X.flush(); 
                            	}
                            	//FV指靜脈 FSCODE=FV，FP, 日期位置跟別人不一樣,所以單獨一塊做邏輯處理
                            }else if(value.length() >= 150 && ("C6E5".equals(value.substring(146, 150))||"C6D7".equals(value.substring(146, 150)))){
                            	String orldate = value.substring(40, 52);
                            	if (StringUtils.isNotBlank(date)) {
                            		String newString = "";
                            		String ebcdate = EbcdicConverter.toHex(CCSID.English, date.length(), date);
                            		newString = value.substring(0, 40) + ebcdate+ value.substring(52);
                            		if(value.length() == 1018) {
                            	        if(value.substring(414, 426).equals(orldate)) {
                            	        	newString = newString.substring(0, 414) + ebcdate+ newString.substring(426);
                            	        }
                            		}
                            		fw473X.append(newString);
                                    fw473X.newLine();
                                    fw473X.flush(); 
                            	}
                            }else {
                            	fw473X.append(value);
                            	fw473X.newLine();
                                fw473X.flush(); 
                            }
                    	}
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                }
            }
            
            System.out.println("pattern:"+pattern+" done");
            // fr.close();
            br.close();
            bufferedReader.close();
        } catch (Exception e) {
            logger.exceptionMsg(e, "SyscomFepSuipConnectApplication run failed!!!");
        } finally {
    	  if (fw != null) {
			  try {
			    fw.close();
			  } catch (IOException e) {
			    e.printStackTrace();
			  }
    	  }
    	  if (fw473X != null) {
			  try {
				  fw473X.close();
			  } catch (IOException e) {
			    e.printStackTrace();
			  }
    	  }
        		  
            System.exit(0);
        }
    }
    
    /**
     * 讀取設定檔
     * @throws IOException
     */
    public void readProp() throws IOException {
        properties = new Properties();
        //參數設定檔
        InputStreamReader prop = new InputStreamReader(new FileInputStream("./suipConnect.properties"), "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(prop);
        properties.load(bufferedReader);
        ej = properties.getProperty("ej");
        subSys = properties.getProperty("subSys");
        programName = properties.getProperty("programName");
        channel = properties.getProperty("channel");
        messageFlowType = properties.getProperty("MessageFlowType");
        messageId = properties.getProperty("MessageId");
        systemId = properties.getProperty("systemId");
        hostIp = properties.getProperty("hostIp");
        hostName = properties.getProperty("hostName");
        encRetryCountStr = properties.getProperty("encRetryCount");
        encLibRetryIntervalStr = properties.getProperty("encLibRetryInterval");
        suipAddress = properties.getProperty("suipAddress");
        suipTimeoutStr = properties.getProperty("suipTimeout");
        step = properties.getProperty("step");
        pattern = properties.getProperty("pattern");
        date = properties.getProperty("date");
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    public void run2(String... args) throws Exception {
    	BufferedWriter bufferedTemplateFile = null;
    	BufferedWriter fw473X = null;
        try {
            // FileReader fr = new FileReader("./suipData.txt");
            //讀取設定檔
        	this.readProp();
            
//        	String selstr = "";
//        	//拿來壓MAC的長度
//            String macLength = properties.getProperty("length");
//            //從原始電文originalData.txt ebcdic格式電文第34格(C6C1C1)擷取到MAC前的值的位置,共抓取多長的值,目前邏輯為抓480長度,C6C1C1轉成ASCII的值是FAA
//            int macPrefixIndex = Integer.valueOf(macLength);
//            //mac的位置在ebcdic第743格
//            int macLastIndex = 742;
//        	//mac後面的ebcdic的內容，最終的電文組成為prefix+in1+macPrefix+ebcmac+macSuffix
//        	String macSuffix = "";
//        	//mac前面的ebcdic的內容，最終的電文組成為prefix+in1+macPrefix+ebcmac+macSuffix
//        	String macPrefix = "";
//        	//ebcdic格式電文C6C1C1的位置
//        	int C6C1C1Location = 34;
//        
//        	//原始電文 0~34  +   我們拿來改成當天日期跟MAC的中間480長度電文   +   我們壓出來的 新MAC (第741~749)ECBDIC會乘2的長度(16格) + MAC後面的原始電文內容
//        	
////        	mac前的位置，但壓mac統一到480
//        	//這些電文因為mac的位置不在743，所以抓取的位置比較不一樣
////        	int p3pp = 488;
////        	int ppp1 = 488;
//        	int p5p6 = 326;
//        	int p1macPrefix = 596;
//        	int p5p6macPrefix = 360;
//        	int ppp3macPrefix = 522;
//        	int d8i5macPrefix = 730;
//        	int d8i5macSuffix = 746;
//        	//國際卡的mac前的位置
//        	int internationmacLastIndex = 750;
//        	int internationmacSuffix = 766;
        	
        	
        	
            //壓MAC
        	if("createMac".equals(pattern)) {	
        		//最後會把 原始電文 + 新壓的日期跟MAC + 後半段原始電文 組合出來的結果產出在template.txt
        		File filetemp = new File("./template.txt");
        		FileWriter fileWriter =new FileWriter(filetemp);
        	    fileWriter.write("");
        	    fileWriter.flush();
        	    fileWriter.close();
//        	    File file = new File("./template.txt");
//                fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
//        	    fw.append("ChangeMasterKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2D4");
//                fw.newLine();
//                fw.flush();
//                fw.append("ChangeWorkingKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2C3");
//                fw.newLine();
//                fw.flush();
              //更改測試日期
            }else if("changeDate".equals(pattern)) {
            	File filetemp = new File("./473XData_changeDate.txt");
        		FileWriter fileWriter =new FileWriter(filetemp);
        	    fileWriter.write("");
        	    fileWriter.flush();
        	    fileWriter.close();
            }
//        	String title = "";
            //讀取最原始的完整473X電文檔案originalData.txt
            InputStreamReader originalDataFile = new InputStreamReader(new FileInputStream("./originalData.txt"), "UTF-8");
            BufferedReader originalData = new BufferedReader(originalDataFile);
        	List<String> originalDataList = new ArrayList<String>();
        	try {
	        	while (originalData.ready()) {
	    			String originalDatavalue = originalData.readLine();	               
                    if (StringUtils.isNotBlank(originalDatavalue)) {
                    	originalDataList.add(originalDatavalue);
                	}                
	            }
            } catch (Exception e) {
                logger.exceptionMsg(e, "Exception ");
            }finally {
            	if(originalData != null) {
            		originalData.close();
            	}
            }
        	
        	//壓好的MAC或改日期的檔案檔寫入template.txt
//    	    File file = new File("./template.txt");
        	File templateFile = new File("./template.txt");
        	bufferedTemplateFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(templateFile, true), "UTF-8"));
            //產生出更改日期後檔案的檔名
    	    File file473X = new File("./473XData_changeDate.txt");
            fw473X = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file473X, true), "UTF-8"));
        	//mac後面的ebcdic的內容，最終的電文組成為prefix+in1+macPrefix+ebcmac+macSuffix
        	String macSuffix = "";
        	//mac前面的ebcdic的內容，最終的電文組成為prefix+in1+macPrefix+ebcmac+macSuffix
        	String macPrefix = "";
        	String selstr = "";//Bruce add
            //讀取473XData.txt，拿來壓MAC或改日期的檔案檔
            InputStreamReader tel473XData = new InputStreamReader(new FileInputStream("./473XData.txt"), "UTF-8");
            BufferedReader readTel473XData = new BufferedReader(tel473XData);
            while (readTel473XData.ready()) {
                String value = readTel473XData.readLine();
                String encFunc = null;
                try {
                    if (StringUtils.isNotBlank(value)) {
                    	//壓MAC
                    	if("createMac".equals(pattern)) {
                            String[] array = value.split(",");
                            char ch = ',';
                            long count = 0;
                            if(array.length>1) {
                            	try {
                            		count = value.chars().filter(c -> c == ch).count();
                            	} catch (Exception e) {
                                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                                }
                            }
                            //壓mac的區域  //確認傳送進來的參數有四個,三個逗號
                            if (count == 3) {
                                int ix = array.length;
                                encFunc = array[0];
                                String keyid = array[1];
                                String inputStr1 = "";
                                String inputStr2 = "";
                                //參數理論上有四個  防止爆掉
                                if (ix >= 3)
                                    inputStr1 = array[2];
                                if (ix >= 4)
                                    inputStr2 = array[3];
                                
                                //-------------suip基本參數設定
                                ENCLogData encLog = new ENCLogData();
                                if (StringUtils.isNotBlank(ej)) {
                                    int eji = Integer.valueOf(ej);
                                    encLog.setEj(eji);
                                }
                                int encRetryCount = 0;
                                int encLibRetryInterval = 0;
                                int suipTimeout = 0;
                                //不知道下面三個判斷式用意在哪.....
//                                if (StringUtils.isNotBlank(ej)) {
//                                    encRetryCount = Integer.valueOf(encRetryCountStr);
//                                }
//                                if (StringUtils.isNotBlank(ej)) {
//                                    encLibRetryInterval = Integer.valueOf(encLibRetryIntervalStr);
//                                }
//                                if (StringUtils.isNotBlank(ej)) {
//                                    suipTimeout = Integer.valueOf(suipTimeoutStr);
//                                }
                                if (StringUtils.isNotBlank(ej)) {
                                    encRetryCount = Integer.valueOf(encRetryCountStr);
                                    suipTimeout = Integer.valueOf(suipTimeoutStr);
                                    encLibRetryInterval = Integer.valueOf(encLibRetryIntervalStr);
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
//                                encLog.setHostIp(hostIp);
//                                encLog.setHostName(hostName);
                                encLog.setStep(Integer.valueOf(step));
                                //------------suip基本參數設定 end
                                RefString o1 = new RefString();
                                RefString o2 = new RefString();
                                int rtn = -1;
                                //呼叫 suip  塞入相對應的參數
                                ENCLib encLib = new ENCLib(encLog, encRetryCount, encLibRetryInterval, suipAddress, suipTimeout);
                                //取得suip回傳的參數
                                rtn = encLib.callSuip(encFunc, keyid, inputStr1, inputStr2, o1, o2);
                                String ebcmac = "";
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
//                                String fwvalue = "";
                                //最後組成電文的內容
                                String finalTel = "";
                            	
                                //FN000313, RC:0, Output1:0008FFFFFFFF, Output2:
                                System.out.println(encFunc + ", RC:" + rtn + ", Output1:" + o1.get() + ", Output2:" + o2.get() + ", Output1toEbcdic:" + ebcmac);
                                //上面都宣告selstr為空了，不懂小傑下面這行用意在哪....
                                if(StringUtils.isNotBlank(selstr)) {
                                	String in1 = inputStr1.substring(4);
                                	finalTel = selstr + in1 + ebcmac;
                                	bufferedTemplateFile.append(finalTel);
                                	bufferedTemplateFile.flush(); 
                                    selstr="";
                                }else {
                                	String in1 = inputStr1.substring(4);
                                	//這段是固定不變的前34byte
                                    String prefix = "C9C2D7C2D4F0F0F040F9F9F8F2F1F0F140";
                                    //最終的電文組成為 prefix + in1 + macPrefix + ebcmac + macSuffix
                                    finalTel = prefix + in1 + macPrefix + ebcmac;
                                    bufferedTemplateFile.append(finalTel);
                                    bufferedTemplateFile.flush(); 
                                }
//                                finalTel = finalTel + macSuffix;//
                                //MAC後面的EBDC電文
                                bufferedTemplateFile.append(macSuffix);
                                bufferedTemplateFile.newLine();
                                bufferedTemplateFile.flush(); 
//                                System.out.println("fwvalue:"+fwvalue);
                            //補摺
                            }else if(value.length() >= 18 && "C5C2E3E2D7D4F0F0".equals(value.substring(0, 16))) {
                            	bufferedTemplateFile.append(value);
                            	bufferedTemplateFile.newLine();
                            	bufferedTemplateFile.flush(); 
//                          //FV指靜脈 FSCODE=FV，FP  ，FP="C6D7".equals(value.substring(146, 150))  C6C3F7 = FC7
                            }else if(value.length() >= 150 && "C6E5".equals(value.substring(146, 150)) && !"C6C3F7".equals(value.substring(34, 40))){
                            	bufferedTemplateFile.append(value);
                            	bufferedTemplateFile.newLine();
                            	bufferedTemplateFile.flush(); 
                            }else if(value.contains("ChangeMasterKey") || value.contains("ChangeWorkingKey")) {
                            	bufferedTemplateFile.append(value);
                            	bufferedTemplateFile.newLine();
                            	bufferedTemplateFile.flush();
                            //這個區塊是473XData.txt標題列   
                            } else {
                            	
//                            	System.out.println("exit while:");
                            	//拿來壓MAC的長度
//                                String macLength = properties.getProperty("length");
                            	//ebcdic格式電文C6C1C1的位置
                            	int C6C1C1Location = 34;
                                //從原始電文originalData.txt ebcdic格式電文第34格(C6C1C1的C開始)擷取到MAC前的位置,共抓取多長的值,目前大部份邏輯為抓480長度,C6C1C1轉成ASCII的值是FAA
                            	//下面這段到34不動 prefix
                            	//C9C2D7C2D4F0F0F040F9F9F8F2F1F0F140
                            	//下面這段長度為480  macPrefixIndex
                            	//C6C1C1F2F2F0F6F2F8F1F4F3F9F4F6F1F6F4F5F0F1C9D5F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0E3F9F9F9F8E2F0F2F0F1C9F14040404040404040404040404040404040404040404040404040404040404040404040404040404040404040F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F040F9F0E3F0F0F3F3C6C6C6C6F0F2F4F0F0F6F0F0F0F0F0F9F9F9F8F7F6F5F1F2F3F1F6F3F0F1F0D240F2F0F0F2F5F0F0F0F0F0F0F0F2F3F1F2F0F0F0F0F1F6F4F5F2F0F2F2F0F6F2F8F1F4F3F9F4F5F6F0F1F154313232353430393230393939383736353132333136333031202020203740404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040F0F2F7F8F8F0F2F5000ABC6023BCF084DB77202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020
                                int macPrefixIndex = Integer.valueOf(properties.getProperty("length"));
                                //從原始電文originalData.txt ebcdic格式電文第34格(C6C1C1)擷取到MAC前的值位置,共抓取多長的值,P5、P6邏輯為抓326長度,C6C1C1轉成ASCII的值是FAA
                            	int p5p6MacPrefixIndex = 326;
                            	
                                //I1、I2、I4、DX、D7、F2、F5、F6、F7、W1、W2、W3、W4、F-C8、F-C9、WF mac最後的位置在ebcdic第743格
                                int macLastIndex = 742;
                                //下面這些電文因為mac的位置不在743，所以抓取的位置比較不一樣
                                //D8、I5mac的位置在ebcdic第731格
                                int d8i5macPrefix = 730;
                                //PP、P3mac的位置在ebcdic第523格
                            	int ppp3macPrefix = 522;
                            
                            	//原始電文 0~34  +   我們拿來改成當天日期跟MAC的中間480長度電文   +   我們壓出來的 新MAC (第741~749)ECBDIC會乘2的長度(16格) + MAC後面的原始電文內容
                            	
//                            	mac前的位置，但壓mac統一到480
                            	//這些電文因為mac的位置不在743，所以抓取的位置比較不一樣
//                            	int p3pp = 488;
//                            	int ppp1 = 488;
//                            	int p5p6 = 326;
                            	int p1macPrefix = 596;
                            	int p5p6macPrefix = 360;
//                            	int ppp3macPrefix = 522;
//                            	int d8i5macPrefix = 730;
                            	int d8i5macSuffix = 746;
                            	//國際卡的mac前的位置
                            	int internationmacLastIndex = 750;
                            	int internationmacSuffix = 766;
                            	
                            	macPrefix = "";
                            	macSuffix = "";
                            	
                            	
                            	if(value.contains("I1晶片卡餘額查詢-自行-1")) {
                            		//34+480,742
                            		macPrefix = originalDataList.get(1).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(1).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-自行-3成功")) {
                            		macPrefix = originalDataList.get(3).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(3).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-自行-3失敗")) {
                            		macPrefix = originalDataList.get(5).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(5).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-1")) {
                            		macPrefix = originalDataList.get(413).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(413).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-3成功")) {
                            		macPrefix = originalDataList.get(415).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(415).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(417).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(417).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-1")) {
                            		macPrefix = originalDataList.get(7).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(7).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-3成功")) {
                            		macPrefix = originalDataList.get(9).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(9).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-3失敗")) {
                            		macPrefix = originalDataList.get(11).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(11).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-1")) {
                            		macPrefix = originalDataList.get(13).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(13).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-3成功")) {
                            		macPrefix = originalDataList.get(15).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(15).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-3失敗")) {
                            		macPrefix = originalDataList.get(17).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(17).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-1")) {
                            		macPrefix = originalDataList.get(19).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(19).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-3成功")) {
                            		macPrefix = originalDataList.get(21).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(21).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-3失敗")) {
                            		macPrefix = originalDataList.get(23).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(23).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-1")) {
                            		macPrefix = originalDataList.get(25).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(25).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-3成功")) {
                            		macPrefix = originalDataList.get(27).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(27).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-3失敗")) {
                            		macPrefix = originalDataList.get(29).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(29).substring(758);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-1")) {
                            			macPrefix = originalDataList.get(31).substring(C6C1C1Location + macPrefixIndex, d8i5macPrefix);
                                		macSuffix= originalDataList.get(31).substring(d8i5macSuffix);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-3成功")) {
                            			macPrefix = originalDataList.get(33).substring(C6C1C1Location + macPrefixIndex, d8i5macPrefix);
                                		macSuffix= originalDataList.get(33).substring(d8i5macSuffix);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-3失敗")) {
                            			macPrefix = originalDataList.get(35).substring(C6C1C1Location + macPrefixIndex, d8i5macPrefix);
                                		macSuffix= originalDataList.get(35).substring(d8i5macSuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-1")) {
                            			macPrefix = originalDataList.get(37).substring(C6C1C1Location + macPrefixIndex, d8i5macPrefix);
                                		macSuffix= originalDataList.get(37).substring(d8i5macSuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-3成功")) {
                            			macPrefix = originalDataList.get(39).substring(C6C1C1Location + macPrefixIndex, d8i5macPrefix);
                                		macSuffix= originalDataList.get(39).substring(d8i5macSuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-3失敗")) {
                            			macPrefix = originalDataList.get(41).substring(C6C1C1Location + macPrefixIndex, d8i5macPrefix);
                                		macSuffix= originalDataList.get(41).substring(d8i5macSuffix);
                            	}else if(value.contains("F2晶片卡提款-自行-1")) {
                            		macPrefix = originalDataList.get(43).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(43).substring(758);
                            	}else if(value.contains("F2晶片卡提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(45).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(45).substring(758);
                            	}else if(value.contains("F2晶片卡提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(47).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(47).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-1")) {
                            		macPrefix = originalDataList.get(49).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(49).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(51).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(51).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(53).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(53).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-1")) {
                            		macPrefix = originalDataList.get(55).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(55).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(57).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(57).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(59).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(59).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-1")) {
                            		macPrefix = originalDataList.get(61).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(61).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(63).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(63).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(65).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(65).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-1")) {
                            		macPrefix = originalDataList.get(395).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(395).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-3成功")) {
                            		macPrefix = originalDataList.get(397).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(397).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(399).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(399).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-1")) {
                            		macPrefix = originalDataList.get(67).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(67).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(69).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(69).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(71).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(71).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-1")) {
                            		macPrefix = originalDataList.get(419).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(419).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-3成功")) {
                            		macPrefix = originalDataList.get(421).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(421).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(423).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(423).substring(758);
                            	}else if(value.contains("W2無卡提-自行款-1")) {
                            		macPrefix = originalDataList.get(73).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(73).substring(758);
                            	}else if(value.contains("W2無卡提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(75).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(75).substring(758);
                            	}else if(value.contains("W2無卡提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(77).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(77).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-1")) {
                            		macPrefix = originalDataList.get(79).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(79).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(81).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(81).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(83).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(83).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-1")) {
                            		macPrefix = originalDataList.get(389).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(389).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-3成功")) {
                            		macPrefix = originalDataList.get(391).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(391).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(393).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(393).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-1")) {
                            		macPrefix = originalDataList.get(85).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(85).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-3成功")) {
                            		macPrefix = originalDataList.get(87).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(87).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-3失敗")) {
                            		macPrefix = originalDataList.get(89).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(89).substring(758);
                            	}else if(value.contains("F-C8(WF的前置交易)")) {
                            		macPrefix = originalDataList.get(91).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(91).substring(758);
                            	}else if(value.contains("F-C9(WF的前置交易)")) {
                            		macPrefix = originalDataList.get(93).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(93).substring(758);
                            	}else if(value.contains("WF指靜脈提款-1")) {
                            		macPrefix = originalDataList.get(95).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(95).substring(758);
                            	}else if(value.contains("WF指靜脈提款-3成功")) {
                            		macPrefix = originalDataList.get(97).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(97).substring(758);
                            	}else if(value.contains("WF指靜脈提款-3失敗")) {
                            		macPrefix = originalDataList.get(99).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(99).substring(758);
                            	}else if(value.contains("FCC(IC的前置交易)-1")) {
                            		macPrefix = originalDataList.get(101).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(101).substring(758);
                            	}else if(value.contains("IC的案例只有失敗沒有成功，此為ATM交易裡的垃圾案例，舊卡啟用新卡(前置)查詢新卡卡別-1失敗")) {
                            		macPrefix = originalDataList.get(105).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(105).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-1")) {
                            		macPrefix = originalDataList.get(107).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(107).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-3成功")) {
                            		macPrefix = originalDataList.get(109).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(109).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(111).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(111).substring(758);
                            	}else if(value.contains("FC2-下送一次的T1交易-1")) {
                            		macPrefix = originalDataList.get(113).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(113).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-1")) {
                            		macPrefix = originalDataList.get(115).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(115).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-3成功")) {
                            		macPrefix = originalDataList.get(117).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(117).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-3失敗")) {
                            		macPrefix = originalDataList.get(119).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(119).substring(758);
                            	}else if(value.contains("FC2-下送兩次的T1交易-1")) {
                            		macPrefix = originalDataList.get(121).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(121).substring(758);
                            	}else if(value.contains("FC2-下送兩次的T1交易-3")) {
                            		macPrefix = originalDataList.get(123).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(123).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-1")) {
                            		macPrefix = originalDataList.get(125).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(125).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-3-成功")) {
                            		macPrefix = originalDataList.get(127).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(127).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-3-失敗")) {
                            		macPrefix = originalDataList.get(129).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(129).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-1")) {
                            		macPrefix = originalDataList.get(131).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(131).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-3成功")) {
                            		macPrefix = originalDataList.get(133).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(133).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-3失敗")) {
                            		macPrefix = originalDataList.get(135).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(135).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -1")) {
                            		macPrefix = originalDataList.get(137).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(137).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -3成功")) {
                            		macPrefix = originalDataList.get(139).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(139).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -3失敗")) {
                            		macPrefix = originalDataList.get(141).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(141).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -1")) {
                            		macPrefix = originalDataList.get(143).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(143).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -3成功")) {
                            		macPrefix = originalDataList.get(145).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(145).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -3失敗")) {
                            		macPrefix = originalDataList.get(147).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(147).substring(758);
                            	}else if(value.contains("FCZ-AW的前置交易-1")) {
                            		macPrefix = originalDataList.get(149).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(149).substring(758);
                            	}else if(value.contains("AW無卡提款服務-1")) {
                            		macPrefix = originalDataList.get(151).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(151).substring(758);
                            	}else if(value.contains("AW無卡提款服務-3成功")) {
                            		macPrefix = originalDataList.get(153).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(153).substring(758);
                            	}else if(value.contains("AW無卡提款服務-3失敗")) {
                            		macPrefix = originalDataList.get(155).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(155).substring(758);
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-1")) {
                            		macPrefix = originalDataList.get(157).substring(C6C1C1Location + macPrefixIndex, ppp3macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-3成功")) {
                            		macPrefix = originalDataList.get(159).substring(C6C1C1Location + macPrefixIndex, ppp3macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-3失敗")) {
                            		macPrefix = originalDataList.get(161).substring(C6C1C1Location + macPrefixIndex, ppp3macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("F-C1-P1磁條卡密碼變更-1")) {
                            		macPrefix = originalDataList.get(163).substring(C6C1C1Location + macPrefixIndex, p1macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-1")) {
                            		macPrefix = originalDataList.get(165).substring(C6C1C1Location + macPrefixIndex, p1macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-3成功")) {
                            		macPrefix = originalDataList.get(167).substring(C6C1C1Location + macPrefixIndex, p1macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-3失敗")) {
                            		macPrefix = originalDataList.get(169).substring(C6C1C1Location + macPrefixIndex, p1macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("FC4-P4的前置交易-1")) {
                            	
                            		macPrefix = originalDataList.get(171).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(171).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-1")) {
                            	
                            		macPrefix = originalDataList.get(173).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(173).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-3成功")) {
                            	
                            		macPrefix = originalDataList.get(175).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(175).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(177).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(177).substring(758);
                            	}else if(value.contains("FC2-TD的前置交易-1")) {
                            	
                            		macPrefix = originalDataList.get(179).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(179).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-1")) {
                            	
                            		macPrefix = originalDataList.get(181).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(181).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-3成功")) {
                            	
                            		macPrefix = originalDataList.get(183).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(183).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-3失敗")) {
                            		macPrefix = originalDataList.get(185).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(185).substring(758);
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-1")) { //length:376
                            	
                            		macPrefix = originalDataList.get(187).substring(C6C1C1Location + p5p6MacPrefixIndex , p5p6macPrefix);
                            		macSuffix= "";
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-3成功")) {
                            	
                            		macPrefix = originalDataList.get(189).substring(C6C1C1Location + p5p6MacPrefixIndex , p5p6macPrefix);
                            		macSuffix= "";
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(191).substring(C6C1C1Location + p5p6MacPrefixIndex , p5p6macPrefix);
                            		macSuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-1")) {
                            	
                            		macPrefix = originalDataList.get(193).substring(C6C1C1Location + p5p6MacPrefixIndex , p5p6macPrefix);
                            		macSuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-3成功")) {
                            	
                            		macPrefix = originalDataList.get(195).substring(C6C1C1Location + p5p6MacPrefixIndex , p5p6macPrefix);
                            		macSuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-3失敗")) {
                            		macPrefix = originalDataList.get(197).substring(C6C1C1Location + p5p6MacPrefixIndex , p5p6macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-1")) {
                            		
                            		macPrefix = originalDataList.get(199).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(199).substring(758);
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-3成功")) {
                            	
                            		macPrefix = originalDataList.get(201).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(201).substring(758);
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(203).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(203).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-1")) {
                            	
                            		macPrefix = originalDataList.get(205).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(205).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-3成功")) {
                            	
                            		macPrefix = originalDataList.get(207).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(207).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(209).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(209).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -1")) {
                            	
                            		macPrefix = originalDataList.get(211).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(211).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -3成功")) {
                            	
                            		macPrefix = originalDataList.get(213).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(213).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -3失敗")) {
                            	
                            		macPrefix = originalDataList.get(215).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(215).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 A銀行 =>A銀行-1")) {
                            	
                            		macPrefix = originalDataList.get(217).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(217).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 A銀行 =>A銀行-3成功")) {
                            	
                            		macPrefix = originalDataList.get(219).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(219).substring(758);
                            	
                            		macPrefix = originalDataList.get(221).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(221).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-1")) {
                            	
                            		macPrefix = originalDataList.get(223).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(223).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-3成功")) {
                            	
                            		macPrefix = originalDataList.get(225).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(225).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(227).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(227).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-1")) {
                            	
                            		macPrefix = originalDataList.get(229).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(229).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-3成功")) {
                            	
                            		macPrefix = originalDataList.get(231).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(231).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(233).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(233).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-1")) {
                            	
                            		macPrefix = originalDataList.get(235).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(235).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-3成功")) {
                            	
                            		macPrefix = originalDataList.get(237).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(237).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(239).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(239).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-1")) {
                            	
                            		macPrefix = originalDataList.get(241).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(241).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-3成功")) {
                            	
                            		macPrefix = originalDataList.get(243).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(243).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(245).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(245).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-1")) {
                            	
                            		macPrefix = originalDataList.get(247).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(247).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-3成功")) {
                            	
                            		macPrefix = originalDataList.get(249).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(249).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(251).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(251).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-1")) {
                            	
                            		macPrefix = originalDataList.get(253).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(253).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-3成功")) {
                            	
                            		macPrefix = originalDataList.get(255).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(255).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(257).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(257).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -1")) {
                            	
                            		macPrefix = originalDataList.get(259).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(259).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -3成功")) {
                            	
                            		macPrefix = originalDataList.get(261).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(261).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -3失敗")) {
                            	
                            		macPrefix = originalDataList.get(263).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(263).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-1")) {
                            	
                            		macPrefix = originalDataList.get(265).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(265).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-3成功")) {
                            	
                            		macPrefix = originalDataList.get(267).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(267).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(269).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(269).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-1")) {
                            	
                            		macPrefix = originalDataList.get(271).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(271).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-3成功")) {
                            	
                            		macPrefix = originalDataList.get(273).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(273).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-3失敗")) {
                            		macPrefix = originalDataList.get(275).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(275).substring(758);
                            		
                            	}else if(value.contains("FC7-FV的前置交易-1")) {
                            		macPrefix = originalDataList.get(309).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(309).substring(758);
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush(); 
                            		
                            	}else if(value.contains("FV指靜脈建置-1")) {
                            	
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush();
                            	}else if(value.contains("FV指靜脈建置-3成功")) {
                            	
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush();
                            	}else if(value.contains("FV指靜脈建置-3失敗")) {
                            	
                            	}else if(value.contains("FP指靜脈密碼設定-1")) {
                            	
                            		macPrefix = originalDataList.get(317).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(317).substring(758);
                            	}else if(value.contains("FP指靜脈密碼設定-3成功")) {
                            	
                            		macPrefix = originalDataList.get(319).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(319).substring(758);
                            	}else if(value.contains("FP指靜脈密碼設定-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(321).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(321).substring(758);
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-1")) {
                            	
                            		macPrefix = originalDataList.get(323).substring(C6C1C1Location + macPrefixIndex, ppp3macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-3成功")) {
                            	
                            		macPrefix = originalDataList.get(325).substring(C6C1C1Location + macPrefixIndex, ppp3macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-3失敗")) {
                            	
                            		macPrefix = originalDataList.get(327).substring(C6C1C1Location + macPrefixIndex, ppp3macPrefix);
                            		macSuffix= "";
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -1")) {
                            	
                            		macPrefix = originalDataList.get(329).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(329).substring(758);
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -3成功")) {
                            	
                            		macPrefix = originalDataList.get(331).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(331).substring(758);
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -3失敗")) {
                            		macPrefix = originalDataList.get(333).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(333).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-1")) {
                            		macPrefix = originalDataList.get(335).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(335).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-3成功")) {
                            		macPrefix = originalDataList.get(337).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(337).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-3失敗")) {
                            		macPrefix = originalDataList.get(339).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(339).substring(758);
                            	}else if(value.contains("SW銀聯卡提款-1")) {
                            		macPrefix = originalDataList.get(341).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(341).substring(internationmacSuffix);
                            	}else if(value.contains("SW銀聯卡提款-3成功")) {
                            		macPrefix = originalDataList.get(343).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(343).substring(internationmacSuffix);
                            	}else if(value.contains("SW銀聯卡提款-3失敗")) {
                            		macPrefix = originalDataList.get(345).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(345).substring(internationmacSuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-1")) {
                            		macPrefix = originalDataList.get(347).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(347).substring(internationmacSuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-3成功")) {
                            		macPrefix = originalDataList.get(349).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(349).substring(internationmacSuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-3失敗")) {
                            		macPrefix = originalDataList.get(351).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(351).substring(internationmacSuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-1")) {
                            		macPrefix = originalDataList.get(353).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(353).substring(internationmacSuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-3成功")) {
                            		macPrefix = originalDataList.get(355).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(355).substring(internationmacSuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-3失敗")) {
                            		macPrefix = originalDataList.get(357).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(357).substring(internationmacSuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-1")) {
                            		macPrefix = originalDataList.get(359).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(359).substring(internationmacSuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-3成功")) {
                            		macPrefix = originalDataList.get(361).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(361).substring(internationmacSuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-3失敗")) {
                            		macPrefix = originalDataList.get(363).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(363).substring(internationmacSuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-1")) {
                            		macPrefix = originalDataList.get(365).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(365).substring(internationmacSuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-3成功")) {
                            		macPrefix = originalDataList.get(367).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(367).substring(internationmacSuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-3失敗")) {
                            		macPrefix = originalDataList.get(369).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(369).substring(internationmacSuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-1")) {
                            		macPrefix = originalDataList.get(371).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(371).substring(internationmacSuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-3成功")) {
                            		macPrefix = originalDataList.get(373).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(373).substring(internationmacSuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-3失敗")) {
                            		macPrefix = originalDataList.get(375).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(375).substring(internationmacSuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-1")) {
                            		macPrefix = originalDataList.get(377).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(377).substring(internationmacSuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-3成功")) {
                            		macPrefix = originalDataList.get(379).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(379).substring(internationmacSuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-3失敗")) {
                            		macPrefix = originalDataList.get(381).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(381).substring(internationmacSuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-1")) {
                            		macPrefix = originalDataList.get(383).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(383).substring(internationmacSuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-3成功")) {
                            		macPrefix = originalDataList.get(385).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(385).substring(internationmacSuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-3失敗")) {
                            		macPrefix = originalDataList.get(387).substring(C6C1C1Location + macPrefixIndex, internationmacLastIndex);
                            		macSuffix= originalDataList.get(387).substring(internationmacSuffix);
                            	}else if(value.contains("F5晶片卡提款-跨行-1")) {
                            		macPrefix = originalDataList.get(401).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(401).substring(758);
                            	}else if(value.contains("F5晶片卡提款-跨行-3成功")) {
                            		macPrefix = originalDataList.get(403).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(403).substring(758);
                            	}else if(value.contains("F5晶片卡提款-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(405).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(405).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-1")) {
                            		macPrefix = originalDataList.get(407).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(407).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-3成功")) {
                            		macPrefix = originalDataList.get(409).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(409).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-3失敗")) {
                            		macPrefix = originalDataList.get(411).substring(C6C1C1Location + macPrefixIndex, macLastIndex);
                            		macSuffix= originalDataList.get(411).substring(758);
                            	}
//                            	System.out.println("macPrefix"+macPrefix);
//                            	System.out.println("title:"+value);
                            	//標題
                            	bufferedTemplateFile.append(value+";");
                            	bufferedTemplateFile.flush(); 
                            }
                            //更改測試日期
                    	}else if("changeDate".equals(pattern)) {
                            char ch = ',';
                            long count = 0;
                            	try {
                            		count = value.chars().filter(c -> c == ch).count();
                            	} catch (Exception e) {
                                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                                }
                            	//確認傳送進來的參數有四個,三個逗號
                            if (count == 3) {
                            	String lns = value.substring(33, 37);
                            	String orldate = value.substring(43, 55);
                            	if (StringUtils.isNotBlank(date)) {
                            		String newString = "";
                            		String ebcdate = EbcdicConverter.toHex(CCSID.English, date.length(), date);
                            		newString = value.substring(0, 43) + ebcdate+ value.substring(55);
                            		if("0480".equals(lns)) {
                            	        if(value.substring(417, 430).equals(orldate)) {
                            	        	newString = newString.substring(0, 416) + ebcdate+ newString.substring(430);
                            	        }
                            	        // PP
                            		}else if("0488".equals(lns)) {  
                            			//P5 P6
                                	}else if("0326".equals(lns)) {
                                	}
                            		
                            		fw473X.append(newString);
                                    fw473X.newLine();
                                    fw473X.flush(); 
                            	}
                            	//FV指靜脈 FSCODE=FV，FP, 日期位置跟別人不一樣,所以單獨一塊做邏輯處理
                            }else if(value.length() >= 150 && ("C6E5".equals(value.substring(146, 150))||"C6D7".equals(value.substring(146, 150)))){
                            	String orldate = value.substring(40, 52);
                            	if (StringUtils.isNotBlank(date)) {
                            		String newString = "";
                            		String ebcdate = EbcdicConverter.toHex(CCSID.English, date.length(), date);
                            		newString = value.substring(0, 40) + ebcdate+ value.substring(52);
                            		if(value.length() == 1018) {
                            	        if(value.substring(414, 426).equals(orldate)) {
                            	        	newString = newString.substring(0, 414) + ebcdate+ newString.substring(426);
                            	        }
                            		}
                            		fw473X.append(newString);
                                    fw473X.newLine();
                                    fw473X.flush(); 
                            	}
                            }else {
                            	fw473X.append(value);
                            	fw473X.newLine();
                                fw473X.flush(); 
                            }
                    	}
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                }
            }
            
            System.out.println("pattern:"+pattern+" done");
            // fr.close();
            readTel473XData.close();
            bufferedTemplateFile.close();
        } catch (Exception e) {
            logger.exceptionMsg(e, "SyscomFepSuipConnectApplication run failed!!!");
        } finally {
    	  if (bufferedTemplateFile != null) {
			  try {
				  bufferedTemplateFile.close();
			  } catch (IOException e) {
			    e.printStackTrace();
			  }
    	  }
    	  if (fw473X != null) {
			  try {
				  fw473X.close();
			  } catch (IOException e) {
			    e.printStackTrace();
			  }
    	  }
        		  
            System.exit(0);
        }
    }
    
    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    public void run1(String... args) throws Exception {
    	BufferedWriter fw = null;
    	BufferedWriter fw473X = null;
        try {
            // FileReader fr = new FileReader("./suipData.txt");
        	//拿來壓MAC或改日期的檔案檔
            InputStreamReader isr = new InputStreamReader(new FileInputStream("./473XData.txt"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //最原始的完整473X電文檔案
            InputStreamReader isr3 = new InputStreamReader(new FileInputStream("./originalData.txt"), "UTF-8");
            BufferedReader originalData = new BufferedReader(isr3);
            Properties properties = new Properties();
            //參數設定檔
            InputStreamReader isr2 = new InputStreamReader(new FileInputStream("./suipConnect.properties"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr2);
            properties.load(bufferedReader);
            String ej = properties.getProperty("ej");
            String subSys = properties.getProperty("subSys");
            String programName = properties.getProperty("programName");
            String channel = properties.getProperty("channel");
            String messageFlowType = properties.getProperty("MessageFlowType");
            String messageId = properties.getProperty("MessageId");
            String systemId = properties.getProperty("systemId");
            String hostIp = properties.getProperty("hostIp");
            String hostName = properties.getProperty("hostName");
            String encRetryCountStr = properties.getProperty("encRetryCount");
            String encLibRetryIntervalStr = properties.getProperty("encLibRetryInterval");
            String suipAddress = properties.getProperty("suipAddress");
            String suipTimeoutStr = properties.getProperty("suipTimeout");
            String step = properties.getProperty("step");
            String pattern = properties.getProperty("pattern");
            String date = properties.getProperty("date");
            
        	String selstr = "";
        	//拿來壓MAC的長度
            String length = properties.getProperty("length");
            //從ebcdic格式電文第34格(C6C1C1)擷取到MAC前的值的位置,共抓取多長的值,目前邏輯為抓480長度,C6C1C1轉成ASCII的值是FAA
            int macprefixindex = Integer.valueOf(length);
            //mac的位置在ebcdic第743格
            int lastindex = 742;
        	//mac後面的ebcdic的內容，最終的電文組成為prefix+in1+macprefix+ebcmac+macsuffix
        	String macsuffix = "";
        	//mac前面的ebcdic的內容，最終的電文組成為prefix+in1+macprefix+ebcmac+macsuffix
        	String macprefix = "";
        	//ebcdic格式電文C6C1C1的位置
        	int strint = 34;
        	
        	//原始電文 0~34  +   我們拿來改成當天日期跟MAC的中間480長度電文   +   我們壓出來的 新MAC (第741~749)ECBDIC會乘2的長度(16格) + MAC後面的原始電文內容
        	
//        	mac前的位置，但壓mac統一到480
        	//這些電文因為mac的位置不在743，所以抓取的位置比較不一樣
//        	int p3pp = 488;
//        	int ppp1 = 488;
        	int p5p6 = 326;
        	int p1macprefix = 596;
        	int p5p6macprefix = 360;
        	int ppp3macprefix = 522;
        	int d8i5macprefix = 730;
        	int d8i5macsuffix = 746;
        	//國際卡的mac前的位置
        	int internationlastindex = 750;
        	int internationmacsuffix = 766;
        	
        	
        	
            //壓MAC
        	if("createMac".equals(pattern)) {	
        		//最後會把 原始電文 + 新壓的日期跟MAC + 後半段原始電文 組合出來的結果產出在template.txt
        		File filetemp = new File("./template.txt");
        		FileWriter fileWriter =new FileWriter(filetemp);
        	    fileWriter.write("");
        	    fileWriter.flush();
        	    fileWriter.close();
//        	    File file = new File("./template.txt");
//                fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
//        	    fw.append("ChangeMasterKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2D4");
//                fw.newLine();
//                fw.flush();
//                fw.append("ChangeWorkingKey;E3F9F9F9F8C9F0F140F140C6C1C1F1F2F0F1F0F6F1F4F3F0F0F0F0F0F0F1F0F1C3D2C3");
//                fw.newLine();
//                fw.flush();
              //更改測試日期
            }else if("changeDate".equals(pattern)) {
            	File filetemp = new File("./473XData_changeDate.txt");
        		FileWriter fileWriter =new FileWriter(filetemp);
        	    fileWriter.write("");
        	    fileWriter.flush();
        	    fileWriter.close();
            }
//        	String title = "";
        	List<String> originalDataList = new ArrayList<String>();
        	while (originalData.ready()) {
    			String originalDatavalue = originalData.readLine();
                try {
                    if (StringUtils.isNotBlank(originalDatavalue)) {
                    	originalDataList.add(originalDatavalue);
                	}
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception ");
                }
                
            }
        	
        	//拿來壓MAC或改日期的檔案檔
    	    File file = new File("./template.txt");
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            //產生出更改日期後檔案的檔名
    	    File file473X = new File("./473XData_changeDate.txt");
            fw473X = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file473X, true), "UTF-8"));
        	
            while (br.ready()) {
                String value = br.readLine();
                String encFunc = null;
                try {
                    if (StringUtils.isNotBlank(value)) {
                    	//壓MAC
                    	if("createMac".equals(pattern)) {
                            String[] array = value.split(",");
                            char ch = ',';
                            long count = 0;
                            if(array.length>1) {
                            	try {
                            		count = value.chars().filter(c -> c == ch).count();
                            	} catch (Exception e) {
                                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                                }
                            }
                            
                            //壓mac的區域  //確認傳送進來的參數有四個,三個逗號
                            if (count == 3) {
                                int ix = array.length;
                                encFunc = array[0];
                                String keyid = array[1];
                                String inputStr1 = "";
                                String inputStr2 = "";
                                //參數理論上有四個  防止爆掉
                                if (ix >= 3)
                                    inputStr1 = array[2];
                                if (ix >= 4)
                                    inputStr2 = array[3];
                                
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
                                }
                                if (StringUtils.isNotBlank(ej)) {
                                    encLibRetryInterval = Integer.valueOf(encLibRetryIntervalStr);
                                }
                                if (StringUtils.isNotBlank(ej)) {
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
                                //呼叫 suip  塞入相對應的參數
                                ENCLib encLib = new ENCLib(encLog, encRetryCount, encLibRetryInterval, suipAddress, suipTimeout);
                                //取得suip回傳的參數
                                rtn = encLib.callSuip(encFunc, keyid, inputStr1, inputStr2, o1, o2);
                                String ebcmac = "";
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
                                String fwvalue = "";
                                //FN000313, RC:0, Output1:0008FFFFFFFF, Output2:
                                System.out.println(encFunc + ", RC:" + rtn + ", Output1:" + o1.get() + ", Output2:" + o2.get() + ", Output1toEbcdic:" + ebcmac);
                                if(StringUtils.isNotBlank(selstr)) {
                                	String in1 = inputStr1.substring(4);
                                	fwvalue = selstr+in1+ebcmac;
                                    fw.append(fwvalue);
                                    fw.flush(); 
                                    selstr="";
                                }else {
                                	String in1 = inputStr1.substring(4);
                                    String prefix = "C9C2D7C2D4F0F0F040F9F9F8F2F1F0F140";
                                    fwvalue = prefix+in1+macprefix+ebcmac;
                                    fw.append(fwvalue);
                                    fw.flush(); 
                                }
                                fwvalue = fwvalue+macsuffix;
                                //MAC後面的EBDC電文
                                fw.append(macsuffix);
                                fw.newLine();
                                fw.flush(); 
//                                System.out.println("fwvalue:"+fwvalue);
                              //補摺
                            }else if(value.length() >= 18 && "C5C2E3E2D7D4F0F0".equals(value.substring(0, 16))) {
                            	fw.append(value);
                                fw.newLine();
                                fw.flush(); 
//                              //FV指靜脈 FSCODE=FV，FP  ，FP="C6D7".equals(value.substring(146, 150))  C6C3F7 = FC7
                            }else if(value.length() >= 150 && "C6E5".equals(value.substring(146, 150)) && !"C6C3F7".equals(value.substring(34, 40))){
                            	fw.append(value);
                                fw.newLine();
                                fw.flush(); 
                            }else if(value.contains("ChangeMasterKey") || value.contains("ChangeWorkingKey")) {
                            	fw.append(value);
                                fw.newLine();
                                fw.flush();
                             //這個區塊是473XData.txt標題列   
                            } else {
                            	
//                            	System.out.println("exit while:");
                            	
                            	macprefix = "";
                            	macsuffix = "";
                            	
                            	
                            	if(value.contains("I1晶片卡餘額查詢-自行-1")) {
                            		macprefix = originalDataList.get(1).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(1).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-自行-3成功")) {
                            		macprefix = originalDataList.get(3).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(3).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-自行-3失敗")) {
                            		macprefix = originalDataList.get(5).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(5).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-1")) {
                            		macprefix = originalDataList.get(413).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(413).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-3成功")) {
                            		macprefix = originalDataList.get(415).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(415).substring(758);
                            	}else if(value.contains("I1晶片卡餘額查詢-跨行-3失敗")) {
                            		macprefix = originalDataList.get(417).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(417).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-1")) {
                            		macprefix = originalDataList.get(7).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(7).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-3成功")) {
                            		macprefix = originalDataList.get(9).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(9).substring(758);
                            	}else if(value.contains("I2餘額查詢~同ID其他帳戶-3失敗")) {
                            		macprefix = originalDataList.get(11).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(11).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-1")) {
                            		macprefix = originalDataList.get(13).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(13).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-3成功")) {
                            		macprefix = originalDataList.get(15).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(15).substring(758);
                            	}else if(value.contains("I4餘額查詢~自行存款帳號檢核(卡片及入帳-3失敗")) {
                            		macprefix = originalDataList.get(17).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(17).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-1")) {
                            		macprefix = originalDataList.get(19).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(19).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-3成功")) {
                            		macprefix = originalDataList.get(21).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(21).substring(758);
                            	}else if(value.contains("DX(D7的前置交易)-3失敗")) {
                            		macprefix = originalDataList.get(23).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(23).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-1")) {
                            		macprefix = originalDataList.get(25).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(25).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-3成功")) {
                            		macprefix = originalDataList.get(27).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(27).substring(758);
                            	}else if(value.contains("D7無卡現金存款(存入合庫)  (前置交易DX)-3失敗")) {
                            		macprefix = originalDataList.get(29).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(29).substring(758);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-1")) {
                            			macprefix = originalDataList.get(31).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(31).substring(d8i5macsuffix);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-3成功")) {
                            			macprefix = originalDataList.get(33).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(33).substring(d8i5macsuffix);
                            	}else if(value.contains("I5企業無卡存款授權存入查詢-3失敗")) {
                            			macprefix = originalDataList.get(35).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(35).substring(d8i5macsuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-1")) {
                            			macprefix = originalDataList.get(37).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(37).substring(d8i5macsuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-3成功")) {
                            			macprefix = originalDataList.get(39).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(39).substring(d8i5macsuffix);
                            	}else if(value.contains("D8企業無卡存款     (前置交易I5, F-C6)-3失敗")) {
                            			macprefix = originalDataList.get(41).substring(strint+macprefixindex, d8i5macprefix);
                                		macsuffix= originalDataList.get(41).substring(d8i5macsuffix);
                            	}else if(value.contains("F2晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(43).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(43).substring(758);
                            	}else if(value.contains("F2晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(45).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(45).substring(758);
                            	}else if(value.contains("F2晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(47).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(47).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(49).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(49).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(51).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(51).substring(758);
                            	}else if(value.contains("F5晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(53).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(53).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(55).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(55).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(57).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(57).substring(758);
                            	}else if(value.contains("F6晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(59).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(59).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(61).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(61).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(63).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(63).substring(758);
                            	}else if(value.contains("F7晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(65).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(65).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(395).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(395).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(397).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(397).substring(758);
                            	}else if(value.contains("F7晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(399).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(399).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-1")) {
                            		macprefix = originalDataList.get(67).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(67).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(69).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(69).substring(758);
                            	}else if(value.contains("W1晶片卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(71).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(71).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(419).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(419).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(421).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(421).substring(758);
                            	}else if(value.contains("W1晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(423).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(423).substring(758);
                            	}else if(value.contains("W2無卡提-自行款-1")) {
                            		macprefix = originalDataList.get(73).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(73).substring(758);
                            	}else if(value.contains("W2無卡提款-自行-3成功")) {
                            		macprefix = originalDataList.get(75).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(75).substring(758);
                            	}else if(value.contains("W2無卡提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(77).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(77).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-1")) {
                            		macprefix = originalDataList.get(79).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(79).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-3成功")) {
                            		macprefix = originalDataList.get(81).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(81).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(83).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(83).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-1")) {
                            		macprefix = originalDataList.get(389).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(389).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(391).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(391).substring(758);
                            	}else if(value.contains("W3HCE掃碼提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(393).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(393).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-1")) {
                            		macprefix = originalDataList.get(85).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(85).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-3成功")) {
                            		macprefix = originalDataList.get(87).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(87).substring(758);
                            	}else if(value.contains("W4行動網銀掃碼提款-自行-3失敗")) {
                            		macprefix = originalDataList.get(89).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(89).substring(758);
                            	}else if(value.contains("F-C8(WF的前置交易)")) {
                            		macprefix = originalDataList.get(91).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(91).substring(758);
                            	}else if(value.contains("F-C9(WF的前置交易)")) {
                            		macprefix = originalDataList.get(93).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(93).substring(758);
                            	}else if(value.contains("WF指靜脈提款-1")) {
                            		macprefix = originalDataList.get(95).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(95).substring(758);
                            	}else if(value.contains("WF指靜脈提款-3成功")) {
                            		macprefix = originalDataList.get(97).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(97).substring(758);
                            	}else if(value.contains("WF指靜脈提款-3失敗")) {
                            		macprefix = originalDataList.get(99).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(99).substring(758);
                            	}else if(value.contains("FCC(IC的前置交易)-1")) {
                            		macprefix = originalDataList.get(101).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(101).substring(758);
                            	}else if(value.contains("IC的案例只有失敗沒有成功，此為ATM交易裡的垃圾案例，舊卡啟用新卡(前置)查詢新卡卡別-1失敗")) {
                            		macprefix = originalDataList.get(105).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(105).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-1")) {
                            		macprefix = originalDataList.get(107).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(107).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-3成功")) {
                            		macprefix = originalDataList.get(109).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(109).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 合庫 =>合庫-跨行-3失敗")) {
                            		macprefix = originalDataList.get(111).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(111).substring(758);
                            	}else if(value.contains("FC2-下送一次的T1交易-1")) {
                            		macprefix = originalDataList.get(113).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(113).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-1")) {
                            		macprefix = originalDataList.get(115).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(115).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-3成功")) {
                            		macprefix = originalDataList.get(117).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(117).substring(758);
                            	}else if(value.contains("T1晶片卡轉帳 合庫 =>合庫查約轉主機下送一次的T1交易(成功)-3失敗")) {
                            		macprefix = originalDataList.get(119).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(119).substring(758);
                            	}else if(value.contains("FC2-下送兩次的T1交易-1")) {
                            		macprefix = originalDataList.get(121).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(121).substring(758);
                            	}else if(value.contains("FC2-下送兩次的T1交易-3")) {
                            		macprefix = originalDataList.get(123).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(123).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-1")) {
                            		macprefix = originalDataList.get(125).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(125).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-3-成功")) {
                            		macprefix = originalDataList.get(127).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(127).substring(758);
                            	}else if(value.contains("T1-查約轉主機下送兩次的T1交易(成功)-3-失敗")) {
                            		macprefix = originalDataList.get(129).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(129).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-1")) {
                            		macprefix = originalDataList.get(131).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(131).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-3成功")) {
                            		macprefix = originalDataList.get(133).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(133).substring(758);
                            	}else if(value.equals("T2晶片卡轉帳 合庫 =>合庫 (含手機門號轉帳)-3失敗")) {
                            		macprefix = originalDataList.get(135).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(135).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -1")) {
                            		macprefix = originalDataList.get(137).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(137).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -3成功")) {
                            		macprefix = originalDataList.get(139).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(139).substring(758);
                            	}else if(value.contains("EA晶片卡全國繳費 合庫 =>合庫 -3失敗")) {
                            		macprefix = originalDataList.get(141).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(141).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -1")) {
                            		macprefix = originalDataList.get(143).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(143).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -3成功")) {
                            		macprefix = originalDataList.get(145).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(145).substring(758);
                            	}else if(value.contains("DX無卡存款前置—傳送簡訊驗證碼 -3失敗")) {
                            		macprefix = originalDataList.get(147).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(147).substring(758);
                            	}else if(value.contains("FCZ-AW的前置交易-1")) {
                            		macprefix = originalDataList.get(149).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(149).substring(758);
                            	}else if(value.contains("AW無卡提款服務-1")) {
                            		macprefix = originalDataList.get(151).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(151).substring(758);
                            	}else if(value.contains("AW無卡提款服務-3成功")) {
                            		macprefix = originalDataList.get(153).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(153).substring(758);
                            	}else if(value.contains("AW無卡提款服務-3失敗")) {
                            		macprefix = originalDataList.get(155).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(155).substring(758);
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-1")) {
                            		macprefix = originalDataList.get(157).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-3成功")) {
                            		macprefix = originalDataList.get(159).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("PPＡＴＭ向中心要1-DES PP KEY-3失敗")) {
                            		macprefix = originalDataList.get(161).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("F-C1-P1磁條卡密碼變更-1")) {
                            		macprefix = originalDataList.get(163).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-1")) {
                            		macprefix = originalDataList.get(165).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-3成功")) {
                            		macprefix = originalDataList.get(167).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P1磁條卡密碼變更-3失敗")) {
                            		macprefix = originalDataList.get(169).substring(strint+macprefixindex, p1macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("FC4-P4的前置交易-1")) {
                            	
                            		macprefix = originalDataList.get(171).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(171).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-1")) {
                            	
                            		macprefix = originalDataList.get(173).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(173).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-3成功")) {
                            	
                            		macprefix = originalDataList.get(175).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(175).substring(758);
                            	}else if(value.contains("P4指靜脈密碼變更-3失敗")) {
                            	
                            		macprefix = originalDataList.get(177).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(177).substring(758);
                            	}else if(value.contains("FC2-TD的前置交易-1")) {
                            	
                            		macprefix = originalDataList.get(179).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(179).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-1")) {
                            	
                            		macprefix = originalDataList.get(181).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(181).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-3成功")) {
                            	
                            		macprefix = originalDataList.get(183).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(183).substring(758);
                            	}else if(value.contains("TD約定轉帳帳號查詢-3失敗")) {
                            		macprefix = originalDataList.get(185).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(185).substring(758);
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-1")) { //length:376
                            	
                            		macprefix = originalDataList.get(187).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-3成功")) {
                            	
                            		macprefix = originalDataList.get(189).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P5ATM預借現金舊密碼檢核(信用卡)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(191).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-1")) {
                            	
                            		macprefix = originalDataList.get(193).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-3成功")) {
                            	
                            		macprefix = originalDataList.get(195).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.equals("P6ATM預借現金新密碼變更-3失敗")) {
                            		macprefix = originalDataList.get(197).substring(strint+p5p6, p5p6macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-1")) {
                            		
                            		macprefix = originalDataList.get(199).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(199).substring(758);
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-3成功")) {
                            	
                            		macprefix = originalDataList.get(201).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(201).substring(758);
                            	}else if(value.contains("T4晶片卡轉帳 合庫 =>他行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(203).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(203).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-1")) {
                            	
                            		macprefix = originalDataList.get(205).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(205).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-3成功")) {
                            	
                            		macprefix = originalDataList.get(207).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(207).substring(758);
                            	}else if(value.contains("DA晶片卡跨行存款-存入他行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(209).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(209).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -1")) {
                            	
                            		macprefix = originalDataList.get(211).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(211).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -3成功")) {
                            	
                            		macprefix = originalDataList.get(213).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(213).substring(758);
                            	}else if(value.contains("TW晶片卡轉帳 他行 =>合庫 -3失敗")) {
                            	
                            		macprefix = originalDataList.get(215).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(215).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 A銀行 =>A銀行-1")) {
                            	
                            		macprefix = originalDataList.get(217).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(217).substring(758);
                            	}else if(value.contains("TA晶片卡轉帳 A銀行 =>A銀行-3成功")) {
                            	
                            		macprefix = originalDataList.get(219).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(219).substring(758);
                            	
                            		macprefix = originalDataList.get(221).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(221).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-1")) {
                            	
                            		macprefix = originalDataList.get(223).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(223).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-3成功")) {
                            	
                            		macprefix = originalDataList.get(225).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(225).substring(758);
                            	}else if(value.contains("TR晶片卡轉帳 A銀行 =>B銀行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(227).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(227).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-1")) {
                            	
                            		macprefix = originalDataList.get(229).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(229).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-3成功")) {
                            	
                            		macprefix = originalDataList.get(231).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(231).substring(758);
                            	}else if(value.contains("T7晶片卡跨行繳稅(15類,自繳稅)　-3失敗")) {
                            	
                            		macprefix = originalDataList.get(233).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(233).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-1")) {
                            	
                            		macprefix = originalDataList.get(235).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(235).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(237).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(237).substring(758);
                            	}else if(value.contains("T8晶片卡跨行繳稅(非 15類,核定稅)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(239).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(239).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-1")) {
                            	
                            		macprefix = originalDataList.get(241).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(241).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(243).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(243).substring(758);
                            	}else if(value.contains("T5晶片卡自行繳稅(15類,自繳稅)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(245).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(245).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-1")) {
                            	
                            		macprefix = originalDataList.get(247).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(247).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(249).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(249).substring(758);
                            	}else if(value.contains("T6晶片卡自行繳稅(非 15類,核定稅)-3失敗")) {
                            	
                            		macprefix = originalDataList.get(251).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(251).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-1")) {
                            	
                            		macprefix = originalDataList.get(253).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(253).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-3成功")) {
                            	
                            		macprefix = originalDataList.get(255).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(255).substring(758);
                            	}else if(value.contains("ED晶片卡全國性繳費(繳費) 合庫 =>他行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(257).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(257).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -1")) {
                            	
                            		macprefix = originalDataList.get(259).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(259).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -3成功")) {
                            	
                            		macprefix = originalDataList.get(261).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(261).substring(758);
                            	}else if(value.contains("EW晶片卡全國性繳費(繳費) 他行 =>合庫 -3失敗")) {
                            	
                            		macprefix = originalDataList.get(263).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(263).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-1")) {
                            	
                            		macprefix = originalDataList.get(265).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(265).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-3成功")) {
                            	
                            		macprefix = originalDataList.get(267).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(267).substring(758);
                            	}else if(value.contains("ER晶片卡全國性繳費(繳費) A銀行 =>B銀行-3失敗")) {
                            	
                            		macprefix = originalDataList.get(269).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(269).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-1")) {
                            	
                            		macprefix = originalDataList.get(271).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(271).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-3成功")) {
                            	
                            		macprefix = originalDataList.get(273).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(273).substring(758);
                            	}else if(value.contains("T5(手機門號轉帳)晶片卡自行繳稅(15類,自繳稅)-3失敗")) {
                            		macprefix = originalDataList.get(275).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(275).substring(758);
                            		
                            	}else if(value.contains("FC7-FV的前置交易-1")) {
                            		macprefix = originalDataList.get(309).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(309).substring(758);
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush(); 
                            		
                            	}else if(value.contains("FV指靜脈建置-1")) {
                            	
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush();
                            	}else if(value.contains("FV指靜脈建置-3成功")) {
                            	
//                            		fw.append(value);
//                                    fw.newLine();
//                                    fw.flush();
                            	}else if(value.contains("FV指靜脈建置-3失敗")) {
                            	
                            	}else if(value.contains("FP指靜脈密碼設定-1")) {
                            	
                            		macprefix = originalDataList.get(317).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(317).substring(758);
                            	}else if(value.contains("FP指靜脈密碼設定-3成功")) {
                            	
                            		macprefix = originalDataList.get(319).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(319).substring(758);
                            	}else if(value.contains("FP指靜脈密碼設定-3失敗")) {
                            	
                            		macprefix = originalDataList.get(321).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(321).substring(758);
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-1")) {
                            	
                            		macprefix = originalDataList.get(323).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-3成功")) {
                            	
                            		macprefix = originalDataList.get(325).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("P3ＡＴＭ向中心要3-DES PP KEY-3失敗")) {
                            	
                            		macprefix = originalDataList.get(327).substring(strint+macprefixindex, ppp3macprefix);
                            		macsuffix= "";
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -1")) {
                            	
                            		macprefix = originalDataList.get(329).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(329).substring(758);
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -3成功")) {
                            	
                            		macprefix = originalDataList.get(331).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(331).substring(758);
                            	}else if(value.contains("D5晶片卡現金存款-入合庫    (前置交易I4) -3失敗")) {
                            		macprefix = originalDataList.get(333).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(333).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-1")) {
                            		macprefix = originalDataList.get(335).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(335).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-3成功")) {
                            		macprefix = originalDataList.get(337).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(337).substring(758);
                            	}else if(value.contains("P1晶片卡密碼變更通知-3失敗")) {
                            		macprefix = originalDataList.get(339).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(339).substring(758);
                            	}else if(value.contains("SW銀聯卡提款-1")) {
                            		macprefix = originalDataList.get(341).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(341).substring(internationmacsuffix);
                            	}else if(value.contains("SW銀聯卡提款-3成功")) {
                            		macprefix = originalDataList.get(343).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(343).substring(internationmacsuffix);
                            	}else if(value.contains("SW銀聯卡提款-3失敗")) {
                            		macprefix = originalDataList.get(345).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(345).substring(internationmacsuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-1")) {
                            		macprefix = originalDataList.get(347).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(347).substring(internationmacsuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-3成功")) {
                            		macprefix = originalDataList.get(349).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(349).substring(internationmacsuffix);
                            	}else if(value.contains("VC(2622)ＶＩＳＡ卡預借現金-3失敗")) {
                            		macprefix = originalDataList.get(351).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(351).substring(internationmacsuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-1")) {
                            		macprefix = originalDataList.get(353).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(353).substring(internationmacsuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-3成功")) {
                            		macprefix = originalDataList.get(355).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(355).substring(internationmacsuffix);
                            	}else if(value.contains("CC(2630)ＣＩＲＲＵＳ提款-3失敗")) {
                            		macprefix = originalDataList.get(357).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(357).substring(internationmacsuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-1")) {
                            		macprefix = originalDataList.get(359).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(359).substring(internationmacsuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-3成功")) {
                            		macprefix = originalDataList.get(361).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(361).substring(internationmacsuffix);
                            	}else if(value.contains("MC(2632)ＭＡＳＴＥＲ卡預借現金-3失敗")) {
                            		macprefix = originalDataList.get(363).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(363).substring(internationmacsuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-1")) {
                            		macprefix = originalDataList.get(365).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(365).substring(internationmacsuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-3成功")) {
                            		macprefix = originalDataList.get(367).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(367).substring(internationmacsuffix);
                            	}else if(value.contains("JC(2640)ＪＣＢ卡預借現金-3失敗")) {
                            		macprefix = originalDataList.get(369).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(369).substring(internationmacsuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-1")) {
                            		macprefix = originalDataList.get(371).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(371).substring(internationmacsuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-3成功")) {
                            		macprefix = originalDataList.get(373).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(373).substring(internationmacsuffix);
                            	}else if(value.contains("SI(2601)銀聯卡查詢-3失敗")) {
                            		macprefix = originalDataList.get(375).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(375).substring(internationmacsuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-1")) {
                            		macprefix = originalDataList.get(377).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(377).substring(internationmacsuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-3成功")) {
                            		macprefix = originalDataList.get(379).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(379).substring(internationmacsuffix);
                            	}else if(value.contains("PQ(2621)ＰＬＵＳ餘額查詢-3失敗")) {
                            		macprefix = originalDataList.get(381).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(381).substring(internationmacsuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-1")) {
                            		macprefix = originalDataList.get(383).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(383).substring(internationmacsuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-3成功")) {
                            		macprefix = originalDataList.get(385).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(385).substring(internationmacsuffix);
                            	}else if(value.contains("CQ(2631)ＣＩＲＲＵＳ餘額查詢-3失敗")) {
                            		macprefix = originalDataList.get(387).substring(strint+macprefixindex, internationlastindex);
                            		macsuffix= originalDataList.get(387).substring(internationmacsuffix);
                            	}else if(value.contains("F5晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(401).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(401).substring(758);
                            	}else if(value.contains("F5晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(403).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(403).substring(758);
                            	}else if(value.contains("F5晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(405).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(405).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-1")) {
                            		macprefix = originalDataList.get(407).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(407).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-3成功")) {
                            		macprefix = originalDataList.get(409).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(409).substring(758);
                            	}else if(value.contains("F6晶片卡提款-跨行-3失敗")) {
                            		macprefix = originalDataList.get(411).substring(strint+macprefixindex, lastindex);
                            		macsuffix= originalDataList.get(411).substring(758);
                            	}
//                            	System.out.println("macprefix"+macprefix);
//                            	System.out.println("title:"+value);
                            	//標題
                            	fw.append(value+";");
                                fw.flush(); 
                            }
                            //更改測試日期
                    	}else if("changeDate".equals(pattern)) {
                            char ch = ',';
                            long count = 0;
                            	try {
                            		count = value.chars().filter(c -> c == ch).count();
                            	} catch (Exception e) {
                                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                                }
                            	//確認傳送進來的參數有四個,三個逗號
                            if (count == 3) {
                            	String lns = value.substring(33, 37);
                            	String orldate = value.substring(43, 55);
                            	if (StringUtils.isNotBlank(date)) {
                            		String newString = "";
                            		String ebcdate = EbcdicConverter.toHex(CCSID.English, date.length(), date);
                            		newString = value.substring(0, 43) + ebcdate+ value.substring(55);
                            		if("0480".equals(lns)) {
                            	        if(value.substring(417, 430).equals(orldate)) {
                            	        	newString = newString.substring(0, 416) + ebcdate+ newString.substring(430);
                            	        }
                            	        // PP
                            		}else if("0488".equals(lns)) {  
                            			//P5 P6
                                	}else if("0326".equals(lns)) {
                                	}
                            		
                            		fw473X.append(newString);
                                    fw473X.newLine();
                                    fw473X.flush(); 
                            	}
                            	//FV指靜脈 FSCODE=FV，FP, 日期位置跟別人不一樣,所以單獨一塊做邏輯處理
                            }else if(value.length() >= 150 && ("C6E5".equals(value.substring(146, 150))||"C6D7".equals(value.substring(146, 150)))){
                            	String orldate = value.substring(40, 52);
                            	if (StringUtils.isNotBlank(date)) {
                            		String newString = "";
                            		String ebcdate = EbcdicConverter.toHex(CCSID.English, date.length(), date);
                            		newString = value.substring(0, 40) + ebcdate+ value.substring(52);
                            		if(value.length() == 1018) {
                            	        if(value.substring(414, 426).equals(orldate)) {
                            	        	newString = newString.substring(0, 414) + ebcdate+ newString.substring(426);
                            	        }
                            		}
                            		fw473X.append(newString);
                                    fw473X.newLine();
                                    fw473X.flush(); 
                            	}
                            }else {
                            	fw473X.append(value);
                            	fw473X.newLine();
                                fw473X.flush(); 
                            }
                    	}
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                }
            }
            
            System.out.println("pattern:"+pattern+" done");
            // fr.close();
            br.close();
            bufferedReader.close();
        } catch (Exception e) {
            logger.exceptionMsg(e, "SyscomFepSuipConnectApplication run failed!!!");
        } finally {
    	  if (fw != null) {
			  try {
			    fw.close();
			  } catch (IOException e) {
			    e.printStackTrace();
			  }
    	  }
    	  if (fw473X != null) {
			  try {
				  fw473X.close();
			  } catch (IOException e) {
			    e.printStackTrace();
			  }
    	  }
        		  
            System.exit(0);
        }
    }
}
