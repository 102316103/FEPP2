package com.syscom.fep.gateway.cmd;

public class FISCGatewayAgentCommand extends GatewayCommand {

    public static void main(String[] args) {
        new FISCGatewayAgentCommand().execute(args);
    }

    @Override
    protected Gateway getGateway() {
        return Gateway.FISC;
    }

    @Override
    protected int getDefaultPort() {
        return 8304;
    }

    @Override
    protected String displayUsage() {
        printWelcome();
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\r\n");
        sb.append("\t1 Show Help:\r\n\t\t").append(GatewayCommandArgs.Help.getParam()).append("\r\n");
        sb.append("\t2 Operate Primary/Secondary Gateway:\r\n");
        sb.append("\t\t2-a) Start Primary Gateway\r\n\t\t\t -f channel -d mode=primary&action=start\r\n");
        sb.append("\t\t2-b) Stop Primary Gateway\r\n\t\t\t -f channel -d mode=primary&action=stop\r\n");
        sb.append("\t\t2-c) Start Secondary Gateway\r\n\t\t\t -f channel -d mode=secondary&action=start\r\n");
        sb.append("\t\t2-d) Stop Secondary Gateway\r\n\t\t\t -f channel -d mode=secondary&action=stop\r\n");
        sb.append("\t\t2-e) Start All Gateway\r\n\t\t\t -f start\r\n");
        sb.append("\t\t2-f) Stop All Gateway\r\n\t\t\t -f stop\r\n");
        sb.append("\t\t2-g) Check Gateway Status\r\n\t\t\t -f check\r\n");
        return GatewayCommandUtil.printOut(sb.toString());
    }

    @Override
    protected String function(String ip, int port, GatewayCommandFunc func, String[] args) {
        String url = getUrl(ip, port, PROP_FILENAME_FISCGW_AGENT, PROP_NAME_FISCGW_AGENT_HOST, func);
        String data = findArg(args, GatewayCommandArgs.Data.getParam());
        switch (func) {
            case channel:
                if (GatewayCommandUtil.isBlank(data)) {
                    data = "mode=primary&action=start";
                }
                return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
            default:
                return GatewayCommandUtil.printOut(httpPost(url, null));
        }
    }
}
