package com.syscom.fep.gateway.cmd;

import com.syscom.fep.frmcommon.io.ConsoleIn;

/**
 * 通過執行本程序, 用來給ATM Gateway傳送不同的指令執行不同的動作
 * <p>
 * 1.增加憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f ssladd -d "file=atmgw-certificate.p12&sscode=syscom"
 * <p>
 * 2.列舉當前所有的憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f ssllist
 * <p>
 * 3.當前正在使用的憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslnow
 * <p>
 * 4.切換憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslswitch
 * <p>
 * 5.改變憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslchange -d "index=1"
 * <p>
 * 6.刪除憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslremove -d "index=1"
 * <p>
 * 7.操控憑證alias
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslalias -d "action=set&atmIp=127.0.0.1&alias=1.0"
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslalias -d "action=get&atmIp=127.0.0.1"
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslalias -d "action=remove&atmIp=127.0.0.1"
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslalias -d "action=list"
 * <p>
 * 8.獲取monitor資料
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f monitor -d "action=get"
 * <p>
 * 9.獲取ATM Client的列表
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f clientlist -d "atmStatus=0"
 * <p>
 */
public class ATMGatewayCommand extends GatewayCommand {

    public static void main(String[] args) {
        new ATMGatewayCommand().execute(args);
    }

    @Override
    protected Gateway getGateway() {
        return Gateway.ATM;
    }

    @Override
    protected int getDefaultPort() {
        return 8300;
    }

    @Override
    protected String displayUsage() {
        printWelcome();
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\r\n");
        sb.append("\t1 Show Help:\r\n\t\t-h\r\n");
        sb.append("\t2 SSL Certificate:\r\n");
        sb.append("\t\t2-a) Switch SSL Certificate\r\n\t\t\t -f sslswitch\r\n");
        sb.append("\t\t2-b) Change SSL Certificate to index {number}\r\n\t\t\t -f sslchange -d index={number}\r\n");
        sb.append("\t\t2-c) Get SSL Certificate in use now\r\n\t\t\t -f sslnow\r\n");
        sb.append("\t\t2-d) Get SSL Certificate list\r\n\t\t\t -f ssllist\r\n");
        sb.append("\t\t2-e) Add new SSL Certificate\r\n\t\t\t -f ssladd -d file=atmgw-certificate_3.p12&sscode=syscom&type=PKCS12&clientAuth=true\r\n");
        sb.append("\t\t2-f) Remove SSL Certificate by index {number}\r\n\t\t\t -f sslremove -d index={number}\r\n");
        sb.append("\t\t2-g) Set SSL Alias\r\n\t\t\t -f sslalias -d action=set&atmIp=127.0.0.1&alias=1.0\r\n");
        sb.append("\t\t2-h) Remove SSL Alias\r\n\t\t\t -f sslalias -d action=remove&atmIp=127.0.0.1\r\n");
        sb.append("\t\t2-i) List SSL Alias\r\n\t\t\t -f sslalias -d action=list\r\n");
        sb.append("\t3 Monitor Data:\r\n");
        sb.append("\t\t3-a) Get Monitor Data\r\n\t\t\t -f monitor -d action=get&listClient=true\r\n");
        return GatewayCommandUtil.printOut(sb.toString());
    }

    @Override
    protected String function(String ip, int port, GatewayCommandFunc func, String[] args) {
        String url = null;
        // 目前只有monitor可以查詢遠程GW資料
        if (func == GatewayCommandFunc.monitor) {
            url = getUrl(ip, port, PROP_FILENAME_ATMGW, PROP_NAME_ATMGW_HOST, func);
        } else {
            url = getUrl(PROP_FILENAME_ATMGW, PROP_NAME_ATMGW_HOST, func);
        }
        // 找data參數
        String data = findArg(args, GatewayCommandArgs.Data.getParam());
        switch (func) {
            case ssladd:
            case sslchange:
            case sslremove:
            case sslalias:
                if (GatewayCommandUtil.isBlank(data)) {
                    // ssl add step by step
                    if (func == GatewayCommandFunc.ssladd) {
                        return this.executeSslAdd(ip, port, func);
                    }
                    // ssl remove step by step
                    else if (func == GatewayCommandFunc.sslremove) {
                        return this.executeSslRemove(ip, port, func);
                    } else {
                        // 如果沒找到, 直接顯示usage
                        return displayUsage();
                    }
                } else {
                    return GatewayCommandUtil.printOut(httpPost(url, new String[]{data}));
                }
            case monitor:
            case clientlist:
                if (GatewayCommandUtil.isBlank(data)) {
                    if (func == GatewayCommandFunc.monitor) {
                        data = "action=get&listClient=true";
                    }
                }
                return GatewayCommandUtil.printOut(httpPost(url, new String[]{data}));
            default:
                return GatewayCommandUtil.printOut(httpPost(url, null));
        }
    }

    private String executeSslAdd(String ip, int port, GatewayCommandFunc func) {
        printWelcome();
        ConsoleIn console = new ConsoleIn();
        GatewayCommandUtil.printOutLn("Please enter new certificate file name, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
        String fileName = getInput(console, false);
        String ssCode = null;
        while (true) {
            GatewayCommandUtil.printOutLn("Please enter new certificate password, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
            ssCode = getInput(console, true);
            GatewayCommandUtil.printOutLn("Please confirm new certificate password, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
            String confirm = getInput(console, true);
            if (!ssCode.equals(confirm)) {
                GatewayCommandUtil.printOutLn("The password is entered twice inconsistently!!");
                continue;
            }
            if (console.isConsole()) GatewayCommandUtil.printOutLn("The new certificate password in masking is [", GatewayCommandUtil.repeat('*', ssCode.length()), "]");
            break;
        }
        return this.function(ip, port, func, new String[]{
                GatewayCommandArgs.Data.getParam(),
                GatewayCommandUtil.join("file=", fileName, "&sscode=", ssCode)
        });
    }

    private String executeSslRemove(String ip, int port, GatewayCommandFunc func) {
        printWelcome();
        String response = function(ip, port, GatewayCommandFunc.ssllistshort, null);
        if (!GatewayCommandUtil.isBlank(response) && response.contains("ATM Gateway SSL Certificate list")) {
            ConsoleIn console = new ConsoleIn();
            String index = null;
            while (true) {
                GatewayCommandUtil.printOutLn("Please enter certificate index to remove, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
                index = getInput(console, false);
                if (!GatewayCommandUtil.isNumeric(index)) {
                    GatewayCommandUtil.printOutLn("The certificate index must be numeric!!");
                    continue;
                }
                break;
            }
            response = this.function(ip, port, func, new String[]{
                    GatewayCommandArgs.Data.getParam(),
                    GatewayCommandUtil.join("index=", index)
            });
        }
        return response;
    }
}
