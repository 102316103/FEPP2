package com.syscom.fep.common.tools;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.io.ConsoleIn;
import com.syscom.fep.frmcommon.util.IOUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

public class JasyptTool {
    private static final String ITEM_SPACE = StringUtils.repeat(StringUtils.SPACE, 2);
    private static final String MSG_INCORRECT_INPUT = "Incorrect Input!!!";
    private static final String PROP_KEY_DB_USERNAME = "spring.datasource.{0}.username";
    private static final String PROP_KEY_DB_SSCODE = "spring.datasource.{0}.password";
    private static final String PROP_KEY_MAIL_SSCODE = "spring.fep.mail.sscode";
    private static final String PROP_KEY_IBM_MQ_SSCODE = "ibm.mq.password";
    private static final int REPEAT = 70;
    private static final String WELCOME = "Welcome to using Jasypt Tool v1.1.0 ";
    private static final String CONFIGURATION = "fep-jasypt-tool.properties";
    private static final String PKI_FILE_ACCOUNT = "acdu";
    private static final String PKI_FILE_SSCODE = "acdp";
    private static final Pattern patternAlphanumeric = Pattern.compile("^[a-z0-9A-Z]+$");

    public static void main(String[] args) {
        String border = "*";
        printOut(StringUtils.repeat(border, REPEAT));
        for (int i = 0; i < 2; i++)
            printOut(border, StringUtils.repeat(StringUtils.SPACE, REPEAT - 2), border);
        int num = (REPEAT - 2 - WELCOME.length()) / 2;
        printOut(border, StringUtils.repeat(StringUtils.SPACE, num), WELCOME, StringUtils.repeat(StringUtils.SPACE, num), border);
        for (int i = 0; i < 2; i++)
            printOut(border, StringUtils.repeat(StringUtils.SPACE, REPEAT - 2), border);
        printOut(StringUtils.repeat(border, REPEAT));
        JasyptTool tool = new JasyptTool();
        tool.execute();
    }

    private static void printOut(Object... objs) {
        System.out.println(StringUtils.join(objs));
    }

    private static void printErr(Object... objs) {
        System.err.println(StringUtils.join(objs));
    }

    private void execute() {
        JasyptToolConfiguration configuration = null;
        try {
            configuration = new JasyptToolConfiguration(CONFIGURATION);
        } catch (Exception e) {
            printErr("Load configuration [", CONFIGURATION, "] failed with exception occur, ", e.getMessage());
            return;
        }
        ConsoleIn consoleIn = new ConsoleIn();
        execute(configuration, consoleIn);
    }

    private void execute(JasyptToolConfiguration configuration, ConsoleIn consoleIn) {
        InputParam inputParam = new InputParam();
        // choose Category
        // inputParam.category = this.chooseCategory(consoleIn);
        inputParam.category = Category.DB; // 2023-03-17 Richard modified 先暫時只考慮DB
        switch (inputParam.category) {
            case DB:
                // choose DBName
                inputParam.dbName = this.chooseDBName(consoleIn);
                // input Account
                inputParam.acct = this.inputAccount(consoleIn, inputParam.category);
                break;
        }
        // input A/B Part Password
        inputParam.ssCodeA = this.inputPassword(consoleIn, "A");
        inputParam.ssCodeB = this.inputPassword(consoleIn, "B");
        // show confirm
        updateConfiguration(configuration, consoleIn, inputParam, false);
        printOut(StringUtils.repeat("=", REPEAT));
        execute(configuration, consoleIn);
    }

    private void updateConfiguration(JasyptToolConfiguration configuration, ConsoleIn consoleIn, InputParam inputParam, boolean confirm) {
        if (confirm || confirmToEncrypt(consoleIn, inputParam)) {
            try {
                createOrUpdateConfiguration(configuration, inputParam);
            } catch (IOException e) {
                printErr("Update Configuration failed with exception occur, ", e.getMessage());
                e.printStackTrace(System.err);
                printOut("Use Latest Input and Try Again? ", Cmd.YES.getDescription(), "|", Cmd.EXIT.getDescription());
                String input = getInput(consoleIn, false);
                if (Cmd.YES.getInput().equalsIgnoreCase(input)) {
                    this.updateConfiguration(configuration, consoleIn, inputParam, true);
                }
            }
        }
    }

    private void createOrUpdateConfiguration(JasyptToolConfiguration configuration, InputParam inputParam) throws IOException {
        List<String> propKeyList = new ArrayList<>();
        switch (inputParam.category) {
            case DB:
                // username
                propKeyList.add(MessageFormat.format(PROP_KEY_DB_USERNAME, inputParam.dbName.name().toLowerCase()));
                // if (inputParam.dbName == DBName.FEPDB) {
                //     propKeyList.add(MessageFormat.format(PROP_KEY_DB_USERNAME, DBName.BATCHDB.name().toLowerCase()));
                //     propKeyList.add(MessageFormat.format(PROP_KEY_DB_USERNAME, DBName.SAFEAADB.name().toLowerCase()));
                // }
                createOrUpdateConfiguration(configuration.getPkiAccountFile(inputParam.dbName.name()), propKeyList, inputParam.acct);
                // sscode
                propKeyList.clear();
                propKeyList.add(MessageFormat.format(PROP_KEY_DB_SSCODE, inputParam.dbName.name().toLowerCase()));
                // if (inputParam.dbName == DBName.FEPDB) {
                //     propKeyList.add(MessageFormat.format(PROP_KEY_DB_SSCODE, DBName.BATCHDB.name().toLowerCase()));
                //     propKeyList.add(MessageFormat.format(PROP_KEY_DB_SSCODE, DBName.SAFEAADB.name().toLowerCase()));
                // }
                createOrUpdateConfiguration(configuration.getPkiSscodeFile(inputParam.dbName.name()), propKeyList, inputParam.encrypt());
                break;
            case Mail:
                // sscode
                propKeyList.add(PROP_KEY_MAIL_SSCODE);
                createOrUpdateConfiguration(configuration.getPkiSscodeFile(null), propKeyList, inputParam.encrypt());
                break;
            case Queue:
                // sscode
                propKeyList.add(PROP_KEY_IBM_MQ_SSCODE);
                createOrUpdateConfiguration(configuration.getPkiSscodeFile(null), propKeyList, inputParam.encrypt());
                break;
            default:
                break;
        }
    }

    private static void createOrUpdateConfiguration(File pkiFile, List<String> propKeyList, String propValue) throws IOException {
        Action action;
        List<String> lines = null;
        if (pkiFile.exists()) {
            lines = FileUtils.readLines(pkiFile, StandardCharsets.UTF_8);
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                if (StringUtils.isNotBlank(lines.get(i))) {
                    for (String propKey : propKeyList) {
                        if (lines.get(i).startsWith(propKey)) {
                            lines.set(i, getConfigurationLine(propKey, propValue));
                            found = true;
                            break;
                        }
                    }
                    if (found && propKeyList.size() == 1) {
                        break;
                    }
                }
            }
            if (found) {
                action = Action.UPDATE;
            } else {
                if (lines == null) {
                    lines = new ArrayList<>();
                }
                for (String propKey : propKeyList) {
                    lines.add(getConfigurationLine(propKey, propValue));
                }
                action = Action.UPDATE_ADD;
            }
        } else {
            lines = new ArrayList<>();
            for (String propKey : propKeyList) {
                lines.add(getConfigurationLine(propKey, propValue));
            }
            action = Action.CREATE;
        }
        FileUtils.writeLines(pkiFile, StandardCharsets.UTF_8.name(), lines, null, false);
        printOut(action.getDescription(), " PKI File \"", pkiFile.getAbsolutePath(), "\" successful!!");
    }

    private static String getConfigurationLine(String propKey, String propValue) {
        return StringUtils.join(propKey, "=", propValue);
    }

    private boolean confirmToEncrypt(ConsoleIn consoleIn, InputParam inputParam) {
        printOut(inputParam.showInfo());
        String input = getInput(consoleIn, false);
        return Cmd.YES.getInput().equalsIgnoreCase(input);
    }

// 2023-03-17 Richard modified start 先暫時只考慮DB
//    private Category chooseCategory(ConsoleIn consoleIn) {
//        printOut(Category.showList());
//        try {
//            Category category = Category.fromOrdinal(Integer.parseInt(getInput(consoleIn, false)) - 1);
//            while (category == null) {
//                printOut(MSG_INCORRECT_INPUT);
//                return this.chooseCategory(consoleIn);
//            }
//            return category;
//        } catch (NumberFormatException e) {
//            printOut(MSG_INCORRECT_INPUT);
//            return this.chooseCategory(consoleIn);
//        }
//    }
// 2023-03-17 Richard modified end 先暫時只考慮DB

    private DBName chooseDBName(ConsoleIn consoleIn) {
        printOut(DBName.showList());
        try {
            DBName dbName = DBName.fromOrdinal(Integer.parseInt(getInput(consoleIn, false)) - 1);
            while (dbName == null) {
                printOut(MSG_INCORRECT_INPUT);
                return this.chooseDBName(consoleIn);
            }
            return dbName;
        } catch (NumberFormatException e) {
            printOut(MSG_INCORRECT_INPUT);
            return this.chooseDBName(consoleIn);
        }
    }

    private String inputAccount(ConsoleIn consoleIn, Category category) {
        printOut("Please Input ", category.name(), " Account, or Press ", Cmd.EXIT.getDescription());
        String input = getInput(consoleIn, false);
        if (!patternAlphanumeric.matcher(input).matches()) {
            printOut("The ", category.name(), " Account must be Alpha or Numeric!!");
            return inputAccount(consoleIn, category);
        }
        return input;
    }

    private String inputPassword(ConsoleIn consoleIn, String part) {
        printOut("Please Input Password ", part, " Part, or Press ", Cmd.EXIT.getDescription());
        String input = getInput(consoleIn, true);
        printOut("Please Confirm Password ", part, " Part, or Press ", Cmd.EXIT.getDescription());
        String confirm = getInput(consoleIn, true);
        if (!input.equals(confirm)) {
            printOut("The Password ", part, " Part is Entered Twice Inconsistently!!");
            return inputPassword(consoleIn, part);
        }
        if (consoleIn.isConsole()) printOut("The Password ", part, " in masking is [", StringUtils.repeat('*', input.length()), "]");
        return input;
    }

    private String getInput(ConsoleIn consoleIn, boolean sscode) {
        String input = consoleIn.readLine(sscode);
        if (Cmd.EXIT.getInput().equalsIgnoreCase(input)) {
            System.exit(6);
        }
        return input;
    }

    private class InputParam {
        public Category category;
        public DBName dbName;
        public String acct;
        public String ssCodeA;
        public String ssCodeB;

        public String encrypt() {
            return Jasypt.encrypt(StringUtils.join(ssCodeA, ssCodeB), true);
        }

        public String showInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("Confirm to Set or Update Password for ");
            if (category == Category.DB) {
                sb.append(dbName.name()).append(StringUtils.SPACE);
            }
            sb.append(category.name())
                    .append("? ")
                    .append(Cmd.YES.getDescription())
                    .append("|")
                    .append(Cmd.EXIT.getDescription());
            return sb.toString();
        }
    }

    private enum Cmd {
        YES("y", "Y(YES)"),
        EXIT("e", "E(Exit)");

        private String input;
        private String description;

        private Cmd(String input, String description) {
            this.input = input;
            this.description = description;
        }

        public String getInput() {
            return this.input;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private enum Category {
        DB, Mail, Queue;

        public static Category fromOrdinal(int ordinal) {
            for (Category category : values()) {
                if (category.ordinal() == ordinal) {
                    return category;
                }
            }
            return null;
        }

        public static String showList() {
            StringBuilder sb = new StringBuilder("Please Choose Category: ");
            int index = 1;
            for (Category category : values()) {
                sb.append(index++).append(".").append(category.name()).append(ITEM_SPACE);
            }
            sb.append("or Press ").append(Cmd.EXIT.getDescription());
            return sb.toString();
        }
    }

    private enum DBName {
        FEPDB, DESDB, EMSDB, DESLOGDB, FEPHIS;
        // BATCHDB, SAFEAADB;

        public static DBName fromOrdinal(int ordinal) {
            for (DBName name : values()) {
                if (name.ordinal() == ordinal) {
                    return name;
                }
            }
            return null;
        }

        public static String showList() {
            StringBuilder sb = new StringBuilder("Please Choose DB Name: ");
            int index = 1;
            for (DBName name : values()) {
                // if (name == BATCHDB) break;
                sb.append(index++).append(".").append(name.name()).append(ITEM_SPACE);
            }
            sb.append("or Press ").append(Cmd.EXIT.getDescription());
            return sb.toString();
        }
    }

    private enum Action {
        CREATE("Create and Add"),
        UPDATE("Update"),
        UPDATE_ADD("Update and Add");

        private String description;

        private Action(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private class JasyptToolConfiguration {
        private Map<String, String> dbNameToInstanceNameMap = new HashMap<>();
        private Properties properties = new Properties();

        public JasyptToolConfiguration(String configuration) throws Exception {
            try (InputStream in = IOUtil.openInputStream(configuration, false)) {
                properties.load(in);
                put(properties, "InstanceName1");
                put(properties, "InstanceName2");
            }
        }

        private void put(Properties properties, String instanceNameKey) {
            String instanceName = properties.getProperty(instanceNameKey);
            if (StringUtils.isNotBlank(instanceName)) {
                String instanceNameDb = properties.getProperty(StringUtils.join(instanceNameKey, "Db"));
                if (StringUtils.isNotBlank(instanceNameDb)) {
                    for (String dbName : instanceNameDb.split(",")) {
                        if (StringUtils.isBlank(dbName)) continue;
                        dbNameToInstanceNameMap.put(dbName, instanceName);
                    }
                }
            }
        }

        public String getPkiPath() {
            return properties.getProperty("PKI_PATH", "/pki");
        }

        public File getPkiAccountFile(String dbName) {
            return getPkiFile(dbName, PKI_FILE_ACCOUNT);
        }

        public File getPkiSscodeFile(String dbName) {
            return getPkiFile(dbName, PKI_FILE_SSCODE);
        }

        private File getPkiFile(String dbName, String fileName) {
            String dbInstanceName = StringUtils.isBlank(dbName) ? null : dbNameToInstanceNameMap.get(dbName);
            if (StringUtils.isBlank(dbInstanceName)) {
                return new File(getPkiPath(), fileName);
            }
            return new File(getPkiPath(), StringUtils.join(Arrays.asList(dbInstanceName, fileName), "/"));
        }
    }
}
