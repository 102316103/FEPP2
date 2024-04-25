package com.syscom.fep.enclib.function;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

public class FN000108 extends ENCFunction {
    // ATM要求換PPK key, 由DES隨機產生新PPK Key, PPKSync, MAC Key,
    // MACSync並更新Key-File(含1-DES/3-DES Key)
    // 信用卡主機要求換PPK key,由DES隨機產生新PPK Key, PPKSync, MAC Key,
    // MACSync並更新Key-File(含1-DES/3-DES Key)
    public FN000108(SuipData suipData) {
        super(suipData);
    }

    @Override
    public SuipData process() throws Exception {
        final String fn = "08";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();

        // 1.傳入參數檢核
        rc = this.checkFN000108Data();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key:3TMK + ATMID
        RefString key = new RefString(StringUtils.join(suipData.getKeyIdentity().substring(0, 3), TMK,
                suipData.getKeyIdentity().substring(6)));
        // RefString key = new RefString(StringUtils.join("1S1", TMK,
        // suipData.getKeyIdentity().substring(6)));
        ENCKey keyData = new ENCKey(key.get(), log);
        rc = keyData.getKey(key);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 3.Call suip取得回應資料
        String atmid = suipData.getKeyIdentity().substring(13, 13 + 8);
        String inputData = StringUtils.repeat("0", 260);
        String command = this.getSuipCommand(fn, atmid, keyData.getKeys()[0].KeyType, key.get(), inputData);
        RefString refRtn = new RefString(null);
        rc = this.sendReceive(command, log, refRtn);
        String rtn = refRtn.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 4.拆解Suip回傳結果
        RefString refNewKey = new RefString(null);
        RefString refNewKey1 = new RefString(null);
        RefString refNewKey2 = new RefString(null);
        rc = this.parseFN000108ReturnData(rtn, refNewKey, refNewKey1, refNewKey2);
        if (rc != ENCRC.Normal) {
            return this.suipData;
        }

        // 5.有換Key時要更新Key檔
        // PPK(single des)
        ENCKey keyData1 = new ENCKey(StringUtils.join("1S1PPK", suipData.getKeyIdentity().substring(6)), log);
        rc = keyData1.updateOrInsertKey(refNewKey.get());
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
        }
        // PPK(3 des)
        ENCKey keyData2 = new ENCKey(StringUtils.join("1T3PPK", suipData.getKeyIdentity().substring(6)), log);
        rc = keyData2.updateOrInsertKey(refNewKey1.get());
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
        }
        // MAC(3 des)
        // ENCKey keyData3 = new ENCKey(StringUtils.join("1S1MAC",
        // suipData.getKeyIdentity().substring(6)), log);
        // rc = keyData3.updateOrInsertKey(refNewKey2.get());
        // if (rc != ENCRC.Normal) {
        // this.suipData.setRc(rc.getValue());
        // }
        return this.suipData;
    }

    private ENCRC checkFN000108Data() {
        // Key:1T3PPK ATM 80713729 1
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
            return ENCRC.KeyLengthError;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000108ReturnData(String suipReturn, RefString ppk1, RefString ppk3, RefString macK3)
            throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        ppk1.set(StringUtils.EMPTY);
        ppk3.set(StringUtils.EMPTY);
        macK3.set(StringUtils.EMPTY);
        // 108 Return Data: 12 byte header + 4 byte rc + 48 byte PPK + 48 byte E(PPK) +
        // 16 byte PPK_KCV + 48 byte TAK + 48 byte E(TAK) + 16 byte TAK_KCV = 240 byte
        // 合庫版:12 byte header + 4 byte rc + 16 byte PPK + 16 byte E(PPK) + 16 byte
        // PPK_KCV +
        // 48 byte PPK + 48 byte E(PPK) + 16 byte PPK_KCV +
        // 48 byte MAC + 48 byte E(MAC) + 16 byte MAC_KCV
        if (suipReturn.length() > 479) {
            String suipRc = suipReturn.substring(24, 24 + 8);
            this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
            if (this.suipData.getRc() == 0) {
                String data = StringUtil.fromHex(suipReturn.substring(32, 32 + 544));
                ppk1.set(data.substring(0, 0 + 16));
                ppk3.set(data.substring(48, 48 + 48));
                macK3.set(data.substring(160, 160 + 48));
                this.suipData.setOutputData1(StringUtils.join(
                        "0160",
                        data.substring(16, 16 + 32),
                        data.substring(96, 96 + 64),
                        data.substring(208, 208 + 64)));
                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
