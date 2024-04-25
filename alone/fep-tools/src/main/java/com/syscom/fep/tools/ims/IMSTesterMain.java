package com.syscom.fep.tools.ims;

import com.ibm.ims.connect.*;

import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.util.Properties;

public class IMSTesterMain {
    private static String _hostName;
    private static int _hostPort;
    private static int _timeout;
    private static String _dsName;
    private static String _inputData;
    private static String _tranCode;
    private static String _senderClientId;
    private static String _receiverClientId;

    public static void main(String[] args) {
        Properties properties = loadProperties();
        if (properties != null) {
            // 初始化時, 讀入以下config的的值, 做為IMS連線參數
            _hostName = properties.getProperty("HostName");
            _hostPort = Integer.parseInt(properties.getProperty("Port"));
            _timeout = Integer.parseInt(properties.getProperty("Timeout", "10")) * 1000;
            _dsName = properties.getProperty("DatastoreName");
            _inputData = properties.getProperty("TestData");
            _tranCode = properties.getProperty("TranCode");
            if (_tranCode == null || _tranCode.trim().isEmpty()) _tranCode = "";
            _senderClientId = properties.getProperty("Sender.ClientId");
            _receiverClientId = properties.getProperty("Receiver.ClientId");
            new IMSSender(); // 啟動Sender, 完了會自動close IMS Connection
            final IMSReceiver receiver = new IMSReceiver(); // 啟動Receiver
            // 加入hook, 確保關閉程式時Receiver能夠close IMS Connection, 注意一定要Ctrl+C, 或者kill -6終止程序, 不要直接關閉或者kill -9強制退出
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                receiver.setRunning(false);
            }));
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(IMSTesterMain.class.getClassLoader().getResourceAsStream("ims.properties"));
            return properties;
        } catch (Exception e) {
            System.err.println("File \"ims.properties\" not exist in JAVA CLASSPATH!!!");
        }
        return null;
    }

    private static class IMSSender extends Thread {
        public IMSSender() {
            super("IMS Sender");
            start();
        }

        @Override
        public void run() {
            Connection connection = null;
            try {
                // 建立IMS ConnectionFactory物件
                ConnectionFactory connectionFactory = buildConnectionFactory(_hostName, _hostPort, _senderClientId);
                // 建立IMS Connection物件
                connection = buildConnection(connectionFactory, _timeout);
                System.out.println(this.getName() + " connect ok, clientId:" + _senderClientId);
                // 建立IMS Interaction物件
                TmInteraction interaction = buildInteraction(connection, _senderClientId, _dsName, _tranCode, ApiProperties.INTERACTION_TYPE_DESC_SENDONLY);
                // 送出測試資料
                InputMessage inMsg = interaction.getInputMessage();
                inMsg.setInputMessageData(hexToBytes(_inputData));
                interaction.execute();
                System.out.println(this.getName() + " send Test Data ok, clientId:" + _senderClientId + ", data:" + _inputData);
                // 取得回應資料
                OutputMessage outMsg = interaction.getOutputMessage();
                String outData = outMsg.getDataAsString();
                System.out.println(this.getName() + " receive ok, clientId:" + _senderClientId + ", data:" + outData);
            } catch (Exception e) {
                System.err.println(this.getName() + " send/receive Message failed, " + e.getMessage());
            } finally {
                // 關閉連線
                if (connection != null && connection.isConnected()) {
                    try {
                        connection.disconnect();
                        System.out.println(this.getName() + " close connection succeed, clientId:" + _senderClientId);
                    } catch (ImsConnectApiException e) {
                        System.err.println(this.getName() + " close connection failed, clientId:" + _senderClientId);
                    }
                }
            }
        }
    }

    private static class IMSReceiver extends Thread {
        private boolean running = true;
        private Connection connection = null;

        public IMSReceiver() {
            super("IMS Receiver");
            start();
        }

        public void setRunning(boolean running) {
            // 設置線程不再執行
            this.running = running;
            // 關閉連線
            if (connection != null && connection.isConnected()) {
                try {
                    connection.disconnect();
                    System.out.println(this.getName() + " close connection succeed, clientId:" + _receiverClientId);
                } catch (ImsConnectApiException e) {
                    System.err.println(this.getName() + " close connection failed, clientId:" + _receiverClientId);
                }
            }
        }

        @Override
        public void run() {
            TmInteraction interaction = null;
            String req = null;
            try {
                // 建立IMS ConnectionFactory物件
                ConnectionFactory connectionFactory = buildConnectionFactory(_hostName, _hostPort, _receiverClientId);
                // 建立IMS Connection物件
                connection = buildConnection(connectionFactory, _timeout);
                System.out.println(this.getName() + " connect ok, clientId:" + _receiverClientId);
                while (this.running) {
                    // 建立IMS Interaction物件
                    interaction = buildInteraction(connection, _receiverClientId, _dsName, _tranCode, ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
                    interaction.execute();
                    OutputMessage outMsg = interaction.getOutputMessage();
                    req = outMsg.getDataAsString();
                    System.out.println(this.getName() + " receive ok, clientId:" + _receiverClientId + ", data:" + req);
                }
            } catch (Exception e) {
                System.err.println(this.getName() + " receive Message failed, " + e.getMessage());
            } finally {
                this.setRunning(false);
            }
        }
    }

    private static ConnectionFactory buildConnectionFactory(String hostName, int portNumber, String clientId) throws ImsConnectApiException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHostName(hostName);
        connectionFactory.setPortNumber(portNumber);
        connectionFactory.setClientId(clientId);
        return connectionFactory;
    }

    private static Connection buildConnection(ConnectionFactory connectionFactory, int timeout) throws ImsConnectApiException, SocketException {
        Connection connection = connectionFactory.getConnection();
        connection.setSocketType(ApiProperties.SOCKET_TYPE_PERSISTENT); //長連接
        connection.setSocketConnectTimeout(timeout);  //連線逾時,單位毫秒
        connection.connect();
        return connection;
    }

    private static TmInteraction buildInteraction(Connection connection, String ltermOverrideName, String imsDatastoreName, String tranCode, String interactionTypeDescription) throws ImsConnectApiException {
        TmInteraction interaction = connection.createInteraction();
        interaction.setLtermOverrideName(ltermOverrideName);
        interaction.setImsDatastoreName(imsDatastoreName);
        interaction.setTrancode(tranCode);
        interaction.setInteractionTypeDescription(interactionTypeDescription);
        interaction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_SINGLE_WAIT);
        interaction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY);
        return interaction;
    }

    public static byte[] hexToBytes(String hexString) {
        final String HEX_STRING = "0123456789ABCDEF";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2);
        for (int i = 0; i < hexString.length(); i += 2) {
            baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
        }
        return baos.toByteArray();
    }
}
