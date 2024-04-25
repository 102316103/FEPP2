package com.syscom.fep.enclib.function;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.SocketHelper;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCConfig;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import com.syscom.fep.enclib.vo.SuipInfo;

public abstract class ENCFunction {
    protected final String ProgramName = this.getClass().getSimpleName();
    // 2013-10-03 Modify by Ruling for ENCLib整合FEP和MRM
    private static final String suipIp = ENCConfig.SuipAddress;
    private static final int RetryInterval = ENCConfig.EncRetryInterval;
    private static final int RetryCount = ENCConfig.EncRetryCount;
    public static final String TMK = "TMK";
    private static final String MessageHeader = "303030303030303030303031"; // Message header: 12 byte

    private static boolean longConnection = false;
    private static int poolCount = 10;
    private static boolean suipFlag;
    private static SuipInfo[] suip;
    protected SuipData suipData;
    private int retryCount = 0;

    private final Object lock = new Object();

    private static final ConcurrentLinkedQueue<SuipInfo> suipQueue = new ConcurrentLinkedQueue<SuipInfo>();

    public abstract SuipData process() throws Exception;

    static {
        String[] tmp = suipIp.split(";");
        if (!longConnection) {
            suip = new SuipInfo[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                String[] hsm = tmp[i].split(":");
                SuipInfo obj = new SuipInfo();
                obj.setSuipIP(hsm[0]);
                obj.setSuipPort(Integer.parseInt(hsm[1]));
                suip[i] = obj;
            }
        } else {
            for (int j = 0; j < poolCount; j++) {
                for (int i = 0; i < tmp.length; i++) {
                    String[] hsm = tmp[i].split(":");
                    SuipInfo obj = new SuipInfo();
                    obj.setSuipIP(hsm[0]);
                    obj.setSuipPort(Integer.parseInt(hsm[1]));
                    suipQueue.add(obj);
                }
                if (suipQueue.size() >= poolCount) {
                    break;
                }
            }
        }
    }

    public ENCFunction(SuipData suipData) {
        this.suipData = suipData;
    }

    /**
     * 組送Suip的指令(Hex),複合Function用
     *
     * @param function
     * @param keytype1
     * @param key1
     * @param keytype2
     * @param key2
     * @param mac
     * @param inputData
     * @return
     */
    protected String getSuipCommand(String function, String keytype1, String key1, String keytype2, String key2, String mac, String inputData) {
        String keyLength1 = this.getKeyLength(keytype1);
        String keyLength2 = this.getKeyLength(keytype2);
        String data = StringUtils.join(
                MessageHeader,
                function,
                keyLength1,
                StringUtil.toHex(StringUtils.rightPad(key1, 48, '0')),
                keyLength2,
                StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key2, 48, '0'), mac, inputData)));
        return data;
    }

    protected String getSuipCommandFor801(String function, String atmno, String keytype, String key) {
        String data = StringUtils.join(
                MessageHeader,
                function,
                StringUtil.toHex(atmno),
                keytype,
                StringUtil.toHex(StringUtils.rightPad(key, 128, '0')));
        return data;
    }

    protected String getSuipCommand(String function, String inputData) {
        String data = StringUtils.join(
                MessageHeader,
                function,
                inputData);
        return data;
    }

    /**
     * 組送Suip的指令(Hex),9902用
     *
     * @param function
     * @param keytype1
     * @param key1
     * @param keytype2
     * @param key2
     * @param inputData1
     * @param keytype3
     * @param key3
     * @param inputData2
     * @return
     */
    protected String getSuipCommand(String function, String keytype1, String key1, String keytype2, String key2, String inputData1, String keytype3, String key3, String inputData2) {
        String keyLength1 = this.getKeyLength(keytype1);
        String keyLength2 = this.getKeyLength(keytype2);
        String keyLength3 = this.getKeyLength(keytype3);
        String data = StringUtils.join(
                MessageHeader,
                function,
                keyLength1, StringUtil.toHex(StringUtils.rightPad(key1, 48, '0')),
                keyLength2, StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key2, 48, '0'), inputData1)),
                keyLength3, StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key3, 48, '0'), inputData2)));
        return data;
    }

    /**
     * 組送Suip的指令(Hex)
     *
     * @param function
     * @param keytype
     * @param key
     * @param inputData
     * @return
     */
    protected String getSuipCommand(String function, String keytype, String key, String inputData) {
        String keyLength = this.getKeyLength(keytype);
        String data = StringUtils.join(
                MessageHeader,
                function,
                keyLength,
                StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key, 48, '0'), inputData)));
        // StringUtils.join(StringUtil.toHex(StringUtils.rightPad(key, 48, '0')), inputData));
        return data;
    }

    /**
     * 組送Suip的指令(Hex)
     *
     * @param function
     * @param pvk
     * @param keytype
     * @param key
     * @param inputData
     * @return
     */
    protected String getSuipCommand(String function, String pvk, String keytype, String key, String inputData) {
        String keyLength = this.getKeyLength(keytype);
        String data = StringUtils.join(
                MessageHeader,
                function,
                StringUtil.toHex(pvk),
                keyLength,
                StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key, 48, '0'), inputData)));
        return data;
    }

    /**
     * 組送Suip的指令(Hex)
     *
     * @param function
     * @param pvk
     * @param keytype1
     * @param key1
     * @param pinblock
     * @param keytype2
     * @param key2
     * @param keytype3
     * @param key3
     * @param mac
     * @param inputData
     * @return
     */
    protected String getSuipCommand(String function, String pvk, String keytype1, String key1, String pinblock, String keytype2, String key2, String keytype3, String key3, String mac,
                                    String inputData) {
        String keyLength1 = this.getKeyLength(keytype1);
        String keyLength2 = this.getKeyLength(keytype2);
        String keyLength3 = this.getKeyLength(keytype3);
        String data = StringUtils.join(
                MessageHeader,
                function,
                StringUtil.toHex(pvk),
                keyLength1, StringUtil.toHex(StringUtils.rightPad(key1, 48, '0')),
                StringUtil.toHex(pinblock),
                keyLength2, StringUtil.toHex(StringUtils.rightPad(key2, 48, '0')),
                keyLength3, StringUtil.toHex(StringUtils.rightPad(key3, 48, '0')),
                StringUtil.toHex(StringUtils.join(mac, inputData)));
        return data;
    }

    protected boolean checkInputData(String inputData) {
        if (StringUtils.isBlank(inputData)) {
            return false;
        } else {
            try {
                int len = Integer.parseInt(inputData.substring(0, 4));
                return inputData.length() == len + 4;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    protected boolean checkKeyIdentity(String keyIdentity) {
        if (StringUtils.isBlank(keyIdentity)) {
            return false;
        } else {
            try {
                int keyQty = Integer.parseInt(keyIdentity.substring(0, 1));
                switch (keyQty) {
                    case 1:
                        if (keyIdentity.length() < ENCKey.SingleKeyLength) {
                            return false;
                        }
                        break;
                    case 2:
                        if (keyIdentity.length() < ENCKey.DoubleKeyLength) {
                            return false;
                        }
                        break;
                    case 3:
                        if (keyIdentity.length() < ENCKey.TribleKeyLength) {
                            return false;
                        }
                        break;
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    protected boolean checkCompoundInputData(String inputData, RefBase<SuipData[]> refSuipData) {
        if (StringUtils.isBlank(inputData)) {
            return false;
        }
        // 複合function
        // 00021T3MAC ATM 80703029 1 003801012203029219100100000016600400015694 00086AFB4366 1T3ICC C6 807 1 0072000000782510000000030000000302900021911106201201220811060016600400015694
        // 0064166004000156940216600400015694000000000000000002C6064D16B90424D1
        // 0002 +
        // 第一組資料KeyId1(67) + Input1Length(4) + Input1(128) + Input2Length(4) + Input2(128) =331
        // 第二組資料KeyId1(67) + Input1Length(4) + Input1(128) + Input2Length(4) + Input2(128) =331
        // 第三組資料KeyId1(67) + Input1Length(4) + Input1(128) + Input2Length(4) + Input2(128) =331
        int recLen = 331;
        SuipData[] suipDatas = refSuipData.get();
        try {
            int len = Integer.parseInt(inputData.substring(0, 4));
            if (ArrayUtils.isEmpty(suipDatas) || suipDatas.length != len) {
                return false;
            }
            if (recLen * len + 4 != inputData.length()) {
                return false;
            }
            int offset = 0;
            for (int i = 0; i < len; i++) {
                suipDatas[i] = new SuipData();
                suipDatas[i].setKeyIdentity(inputData.substring(offset + 4, offset + 4 + 67).trim());
                if (!this.checkKeyIdentity(suipDatas[i].getKeyIdentity().trim())) {
                    return false;
                }
                try {
                    int len1 = Integer.parseInt(inputData.substring(offset + 71, offset + 71 + 4));
                    suipDatas[i].setInputData1(inputData.substring(offset + 71, offset + 71 + len1 + 4));
                } catch (NumberFormatException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
                try {
                    int len2 = Integer.parseInt(inputData.substring(offset + 203, offset + 203 + 4));
                    suipDatas[i].setInputData2(inputData.substring(offset + 203, offset + 203 + len2 + 4));
                } catch (NumberFormatException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
                offset += recLen;
            }
            return true;
        } catch (NumberFormatException e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            return false;
        }
    }

    protected String getKeyLength(String keytype) {
        String keyLength = StringUtils.EMPTY;
        switch (keytype) {
            case "S1":
                keyLength = "01";
                break;
            case "T2":
                keyLength = "02";
                break;
            case "T3":
                keyLength = "03";
                break;
        }
        return keyLength;
    }

    /**
     * 傳送至Suip並取得回應
     *
     * @param command
     * @param log
     * @param rtn
     * @return
     */
    protected ENCRC sendReceive(String command, ENCLogData log, RefString rtn) {
        SuipInfo obj;
        SocketHelper sck = null;
        rtn.set(StringUtils.EMPTY);
        ENCRC rc = ENCRC.GetSuipSocketError;
        Calendar now = Calendar.getInstance();
        if (longConnection) {
            while (CalendarUtil.getDiffTimeInMilliseconds(Calendar.SECOND, Calendar.getInstance().getTimeInMillis() - now.getTimeInMillis()) < 10) {
                obj = suipQueue.poll();
                if (obj != null) {
                    if (obj.getSuipSocket() == null) {
                        sck = new SocketHelper(obj.getSuipIP(), obj.getSuipPort(), log, this.suipData);
                        sck.setKeepConnectionOpen(longConnection);
                        rc = sck.connect();
                        obj.setSuipSocket(sck);
                    } else {
                        sck = obj.getSuipSocket();
                        rc = ENCRC.Normal;
                    }
                    // 成功取得已連線之Socket物件
                    if (rc == ENCRC.Normal) {
                        rc = sck.sendReceive(command, rtn);
                    }
                    suipQueue.add(obj); // 長連接等送完Suip物件再歸還Pool
                    break;
                }
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        } else {
            int currentSuip;
            if (suip.length > 1) {
                synchronized (lock) {
                    currentSuip = this.suipFlag ? 0 : 1;
                    this.suipFlag = !this.suipFlag;
                }
            } else {
                currentSuip = 0;
            }
            obj = suip[currentSuip];
            sck = new SocketHelper(obj.getSuipIP(), obj.getSuipPort(), log, this.suipData);
            sck.setKeepConnectionOpen(longConnection);
            rc = sck.connect();
            // 成功取得已連線之Socket物件
            if (rc == ENCRC.Normal) {
                rc = sck.sendReceive(command, rtn);
                // 2013-3-13 modify by Ashiang: RC95時Retry
                if (rc == ENCRC.ReceiveError) {
                    if (this.retryCount < RetryCount) {
                        this.retryCount += 1;
                        try {
                            Thread.sleep(RetryInterval);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                        log.setRemark("ENCLib Retry Send Suip");
                        log.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                        ENCLib.writeLog(Level.INFO, log, this.suipData.getFunctionNo(), this.suipData.getKeyIdentity(), this.suipData.getInputData1(), this.suipData.getInputData2(), StringUtils.EMPTY,
                                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(rc.getValue()));
                        rc = this.sendReceive(command, log, rtn);
                    }
                }
            } else {
                // 如果有2個suip改用另一個suip重送
                if (suip.length > 1) {
                    currentSuip = currentSuip == 0 ? 1 : 0;
                    obj = suip[currentSuip];
                    sck = new SocketHelper(obj.getSuipIP(), obj.getSuipPort(), log, this.suipData);
                    sck.setKeepConnectionOpen(longConnection);
                    rc = sck.connect();
                    // 成功取得已連線之Socket物件
                    if (rc == ENCRC.Normal) {
                        rc = sck.sendReceive(command, rtn);
                        // 2013-3-13 modify by Ashiang: RC95時Retry
                        if (rc == ENCRC.ReceiveError) {
                            if (this.retryCount < RetryCount) {
                                this.retryCount += 1;
                                try {
                                    Thread.sleep(RetryInterval);
                                } catch (InterruptedException e) {
                                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                                }
                                log.setRemark("ENCLib Retry Send Suip");
                                log.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                                ENCLib.writeLog(Level.INFO, log, this.suipData.getFunctionNo(), this.suipData.getKeyIdentity(), this.suipData.getInputData1(), this.suipData.getInputData2(),
                                        StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(rc.getValue()));
                                rc = this.sendReceive(command, log, rtn);
                            }
                        }
                    }
                }
            }
        }
        return rc;
    }

    protected String getSuipCommandFor502(String function, String keyFunc, String zmk, String kek, String mk, String pan) {
        String keytype = StringUtils.EMPTY;
        switch (keyFunc) {
            case "KAC":
                keytype = "01";
                break;
            case "KENC":
                keytype = "03";
                break;
            case "KMC":
                keytype = "02";
                break;
        }
        String data = StringUtils.join(MessageHeader, function, keytype, StringUtil.toHex(zmk), StringUtil.toHex(kek), StringUtil.toHex(mk), StringUtil.toHex(pan));
        return data;
    }

    protected String getSuipCommandFor504(String function, String mode, String meth, String keyType, String key, String inputData) {
        String keyLength = this.getKeyLength(keyType);
        String data = StringUtils.join(MessageHeader, function, mode, meth, keyLength, StringUtil.toHex(StringUtils.join(key, inputData)));
        return data;
    }

    /**
     * 組送Suip的指令(Hex),FN000414用
     *
     * @param function
     * @param keytype1
     * @param key1
     * @param keytype2
     * @param key2
     * @param inputData1
     * @param inputData12
     * @param inputData2
     * @return
     */
    protected String getSuipCommandFor414(String function, String keytype1, String key1, String keytype2, String key2, String inputData1, String inputData12, String inputData2) {
        String keyLength1 = this.getKeyLength(keytype1);
        String keyLength2 = this.getKeyLength(keytype2);
        String data = StringUtils.join(
                MessageHeader,
                function,
                keyLength1, StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key1, 48, '0'), inputData1)),
                keyLength2, StringUtil.toHex(StringUtils.join(StringUtils.rightPad(key2, 48, '0'), inputData12)),
                StringUtil.toHex(inputData2));
        return data;
    }
}
