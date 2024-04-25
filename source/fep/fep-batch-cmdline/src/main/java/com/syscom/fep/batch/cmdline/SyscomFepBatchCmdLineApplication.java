package com.syscom.fep.batch.cmdline;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.cmdline.service.JobHelper;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.esapi.ESAPIConfiguration;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CommandLineUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.syscom.fep"})
public class SyscomFepBatchCmdLineApplication implements CommandLineRunner {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private JobHelper jobHelper;

    static {
        ESAPIConfiguration.init();
        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(SyscomFepBatchCmdLineApplication.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.run(args);
        } catch (Exception e) {
            if ("org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException".equals(e.getClass().getName())) {
                // ignore
            } else {
                logger.exceptionMsg(e, "SyscomFepBatchCmdLineApplication run failed!!!");
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
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_COMMAND_LINE);
        BatchReturnCode rtnCode = BatchReturnCode.Succeed;
        logger.info("Run Batch Task with args = [", StringUtils.join(args, StringUtils.SPACE), "]");
        String programName = CommandLineUtil.findArg(args, "-p");
        if (StringUtils.isNotBlank(programName)) {
            String arguments = CommandLineUtil.findArg(args, "-a");
            RefString errMsg = new RefString(StringUtils.EMPTY);
            rtnCode = jobHelper.runProcess(programName, arguments, errMsg);
            if (rtnCode == BatchReturnCode.Succeed) {
                String info = StringUtils.join(programName, " run successful!!!");
                logger.info(info);
                System.out.println(info);
            } else {
                String info = StringUtils.join(programName, " run failed, error message is ", errMsg.get());
                logger.info(info);
                System.err.println(info);
            }
        } else if (CommandLineUtil.existArg(args, "-h")) {
            displayUsage();
        } else {
            System.err.println("Invalid Arguments!!!\r\n");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            displayUsage();
        }
        if (rtnCode == null) rtnCode = BatchReturnCode.Succeed;
        System.exit(rtnCode.getValue());
    }

    private static void displayUsage() {
        System.out.println("USAGE:");
        System.out.println("  Batch Command Line Options");
        System.out.println();
        System.out.println("  Options:");
        System.out.println("      -h      Display this help message.");
        System.out.println("      -p      Program Name, Required.");
        System.out.println("      -a      Arguments which must be enclosed in double quotes, Optional.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar fep-batch-cmdline.jar -p com.syscom.fep.batch.task.cmn.ArchivingLogFile -a \"/SourceDir:G:\\FEP10\\Log /TargetDir:G:\\FEPLog\\Backup\\ /ArchiveDay:2 /ReserveDay:7\"");
    }
}
