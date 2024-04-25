import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SocketClient {
    private static final String MESSAGE_IN = "<<<<<<<<<<";
    private static final String MESSAGE_OUT = ">>>>>>>>>>";
    private static final String INFO = "INFO";
    private static final String ERROR = "ERROR";
    private static final String WARN = "WARM";
//    private static final String DEBUG = "DEBUG";
    private Socket clientSck;

    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            displayUsage();
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];
        int timeout = Integer.parseInt(System.getProperty("timeout", "10000"));
        SocketClient client = new SocketClient();
        try {
            if (client.connect(host, port, timeout)) {
                client.sendReceive(message);
            }
        } catch (Exception e) {
            write(error("Connect and send message failed to ", host, ":", port, " cause ", e.getMessage()));
        } finally {
            client.closeConnection(host, port);
        }
    }

    private static void displayUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Socket Client Console\r\n");
        sb.append("Usage:\r\n");
        sb.append("\t\tjava SocketClient {host} {port} {message} \r\n");
        System.out.print(sb.toString());
    }

    private boolean connect(String host, int port, int timeout) {
        try {
            this.clientSck = new Socket(host, port);
            this.clientSck.setSoTimeout(timeout);
            if (clientSck.isConnected()) {
                write(info("Connected to ", host, ":", port));
                return true;
            } else {
                write(error("Connect failed to ", host, ":", port));
            }
        } catch (Exception e) {
            write(error("Connect failed to ", host, ":", port, " cause ", e.getMessage()));
        }
        return false;
    }

    private String sendReceive(String message) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] bytes = hexToBytes(message);
            OutputStream out = this.clientSck.getOutputStream();
//            long currentTimeMillis = System.currentTimeMillis();
            out.write(bytes);
            out.flush();
            this.clientSck.shutdownOutput();
            write(info("Message sent out in ASCII format is [", new String(bytes, StandardCharsets.UTF_8), "]"));
            write(info(MESSAGE_OUT, message));
            byte[] buffer = new byte[1024];
            InputStream in = this.clientSck.getInputStream();
            int length = -1;
            while ((length = in.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
            }
//            long elapsedMilliseconds = System.currentTimeMillis() - currentTimeMillis;
            if (sb.length() > 0) {
                write(info(MESSAGE_IN, sb.toString()));
                write(info("Message received in HEX format is [", toHex(sb.toString()), "]"));
            } else {
                write(warn("No response from server!!!"));
            }
        } catch (Exception e) {
            write(error("Send message failed cause ", e.getMessage()));
        }
        return sb.toString();
    }

    private void closeConnection(String host, int port) {
        try {
            if (this.clientSck != null) {
                if (this.clientSck.isConnected()) {
                    this.clientSck.close();
                }
                this.clientSck = null;
            }
            write(info("Connection closed on ", host, ":", port));
        } catch (Exception e) {
            write(error("Connection close failed cause ", e.getMessage()));
        }
    }

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

    private static void write(String message) {
        if (message == null || message.trim().length() == 0) return;
        File directory = new File("");
        File file = new File(directory.getAbsoluteFile(), "message_" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()) + ".txt");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            wr.append(message);
            wr.newLine();
        } catch (Exception e) {
            error("Write file failed cause ", e.getMessage());
        }
    }

    public static byte[] hexToBytes(String hexString) {
        String HEX_STRING = "0123456789ABCDEF";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2);
        for (int i = 0; i < hexString.length(); i += 2) {
            baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
        }
        return baos.toByteArray();
    }

    public static String toHex(String ascii) {
        String HEX_STRING = "0123456789ABCDEF";
        byte[] bytes = ascii.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.valueOf(HEX_STRING.charAt(0xf & bytes[i] >> 4)));
            sb.append(String.valueOf(HEX_STRING.charAt(bytes[i] & 0xf)));
        }
        return sb.toString();
    }
}
