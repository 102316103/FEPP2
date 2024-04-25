package com.syscom.fep.gateway;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.syscom.fep.frmcommon.socket.SckClient;

public class ReconnectSocketClient {
    private static final String MESSAGE_IN = "<<<<<<<<<<";
    private static final String MESSAGE_OUT = ">>>>>>>>>>";
    private static final String INFO = "INFO";
    private static final String ERROR = "ERROR";
    private static final String WARN = "WARM";
//    private static final String DEBUG = "DEBUG";
    private SckClient clientSck;

    public static void main(String[] args) throws InterruptedException {
        if (args == null || args.length < 2) {
            displayUsage();
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int timeout = Integer.parseInt(System.getProperty("timeout", "10000"));
        ReconnectSocketClient client = new ReconnectSocketClient();
        try {
            client.connect(host, port, timeout);
            while (true) {
                client.sendReceive("Hello");
//                Thread.sleep(5000L);
            }
        } catch (Exception e) {
            error("Connect and send message failed to ", host, ":", port, " cause ", e.getMessage());
        } finally {
            // client.closeConnection(host, port);
        }
    }

    private static void displayUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reconnect Socket Client Console\r\n");
        sb.append("Usage:\r\n");
        sb.append("\t\tjava SocketClient {host} {port} \r\n");
        System.out.print(sb.toString());
    }

    private boolean connect(String host, int port, int timeout) {
        try {
            this.clientSck = new SckClient();
            this.clientSck.connect(host, port);
            this.clientSck.setSoTimeout(timeout);
            this.clientSck.setKeepAlive(true);
            this.clientSck.setSoLinger(true, 0);
            if (this.clientSck.isConnected()) {
                info("Connected to ", host, ":", port);
                return true;
            } else {
                error("Connect failed to ", host, ":", port);
            }
        } catch (Exception e) {
            error("Connect failed to ", host, ":", port, " cause ", e.getMessage());
        }
        return false;
    }

    private String sendReceive(String message) {
        StringBuilder sb = new StringBuilder();
        OutputStream out = null;
        InputStream in = null;
        try {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            out = this.clientSck.getOutputStream();
//            long currentTimeMillis = System.currentTimeMillis();
            out.write(bytes);
            out.flush();
            info(MESSAGE_OUT, message);
            byte[] buffer = new byte[1024];
            in = this.clientSck.getInputStream();
            int length = -1;
            if ((length = in.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
            }
//            long elapsedMilliseconds = System.currentTimeMillis() - currentTimeMillis;
            if (sb.length() > 0) {
                info(MESSAGE_IN, sb.toString());
            } else {
                warn("No response from server!!!");
            }
        } catch (Exception e) {
            error("Send message failed cause ", e.getMessage());
        } finally {
        	if (in != null) {
        		try {
        			in.close();
    			} catch (IOException e) {
    				error("Send message failed cause ", e.getMessage());
    			}
        	}
        	if (out != null) {
        		try {
        			out.close();
    			} catch (IOException e) {
    				error("Send message failed cause ", e.getMessage());
    			}
        	}
        }
        return sb.toString();
    }

//    private void closeConnection(String host, int port) {
//        try {
//            if (this.clientSck != null) {
//                if (this.clientSck.isConnected()) {
//                    this.clientSck.disconnect();
//                }
//                this.clientSck = null;
//            }
//            info("Connection closed on ", host, ":", port);
//        } catch (Exception e) {
//            error("Connection close failed cause ", e.getMessage());
//        }
//    }

    private static String info(Object... msgs) {
        return print(INFO, msgs);
    }

    private static String warn(Object... msgs) {
        return print(WARN, msgs);
    }

    private static String error(Object... msgs) {
        return print(ERROR, msgs);
    }

    private static String print(String level, Object... msgs) {
        if (msgs != null && msgs.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime())).append("][").append(level).append("]");
            for (Object msg : msgs) {
                sb.append(msg);
            }
            System.out.println(sb.toString());
            return sb.toString();
        }
        return "";
    }
}
