package com.syscom.fep.suipConnect;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"com.syscom.fep"})
@StackTracePointCut(caller = SvrConst.SVR_SUIP_CONNECT)
//public class SyscomFepSuipConnectApplicationchangelength implements CommandLineRunner {
public class SyscomFepSuipConnectApplicationchangelength {
	private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    static {
        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(SyscomFepSuipConnectApplicationchangelength.class);
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
    public void run6(String... args) throws Exception {
    	BufferedWriter fw = null;
    	BufferedWriter fw473X = null;
        try {
            // FileReader fr = new FileReader("./suipData.txt");
        	//拿來壓MAC或改日期的檔案檔
            InputStreamReader isr = new InputStreamReader(new FileInputStream("./473XData.txt"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            InputStreamReader isr2 = new InputStreamReader(new FileInputStream("./suipConnect.properties"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr2);
            Properties properties = new Properties();
            properties.load(bufferedReader);
            String length = properties.getProperty("length");
            
          //拿來壓MAC或改日期的檔案檔
    	    File file = new File("./changeinput1.txt");
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        	
            while (br.ready()) {
                String value = br.readLine();
                String encFunc = null;
                String replacestr = "";
                try {
                    if (StringUtils.isNotBlank(value)) {
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
                            
                            if (count == 3) {
                                int ix = array.length;
                                encFunc = array[0];
                                String keyid = array[1];
                                String inputStr1 = "";
                                String inputStr2 = "";
                                if (ix >= 3)
                                    inputStr1 = array[2];
                                if (ix >= 4)
                                    inputStr2 = array[3];
                                
                                
                               String prefix =  inputStr1.substring(0, 4);
                               if("0708".equals(prefix)) {
                            	   replacestr = inputStr1.replace("0708", "");
                            	   replacestr = replacestr.substring(0,Integer.valueOf(length));
                            	   replacestr = "0"+length+replacestr;
                               }else {
                            	   replacestr = inputStr1;
                               }
                               
                               fw.append(encFunc+","+keyid+","+replacestr+","+inputStr2);
                               fw.newLine();
                               fw.flush(); 
                            }else {
                            	fw.append(value);
                                fw.newLine();
                                fw.flush(); 
                            }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                }
            }
            
            System.out.println("change input1 : done");
            // fr.close();
            br.close();
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
