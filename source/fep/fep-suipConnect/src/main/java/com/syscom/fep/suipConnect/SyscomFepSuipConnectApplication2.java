package com.syscom.fep.suipConnect;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.enums.ENCMessageFlow;
import com.syscom.fep.enclib.vo.ENCLogData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {"com.syscom.fep"})
@StackTracePointCut(caller = SvrConst.SVR_SUIP_CONNECT)
public class SyscomFepSuipConnectApplication2 implements CommandLineRunner {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    static {
        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(SyscomFepSuipConnectApplication2.class);
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
    @Override
    public void run(String... args) throws Exception {
        try {
            // FileReader fr = new FileReader("./suipData.txt");
            InputStreamReader isr = new InputStreamReader(new FileInputStream("./suipData.txt"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            Properties properties = new Properties();
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

            while (br.ready()) {
                String value = br.readLine();
                String encFunc = null;
                try {
                    if (StringUtils.isNotBlank(value)) {
                        String[] array = value.split(",");
                        char ch = ',';
                        long count = value.chars().filter(c -> c == ch).count();
                        //確認傳送進來的參數有四個,三個逗號
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
                            RefString o1 = new RefString();
                            RefString o2 = new RefString();
                            int rtn = -1;
                            ENCLib encLib = new ENCLib(encLog, encRetryCount, encLibRetryInterval, suipAddress, suipTimeout);
                            rtn = encLib.callSuip(encFunc, keyid, inputStr1, inputStr2, o1, o2);
                            //FN000301, RC:0, Output1:0008FFFFFFFF, Output2:
                            System.out.println(encFunc + ", RC:" + rtn + ", Output1:" + o1.get() + ", Output2:" + o2.get());
                        }else {
                        	System.out.println(value);
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Exception with encFunc = [", encFunc, "] occur!!");
                }
            }
            // fr.close();
            br.close();
            bufferedReader.close();
        } catch (Exception e) {
            logger.exceptionMsg(e, "SyscomFepSuipConnectApplication run failed!!!");
        } finally {
            System.exit(0);
        }
    }
}
