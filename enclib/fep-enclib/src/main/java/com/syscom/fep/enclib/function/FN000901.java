package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000901 extends ENCFunction {
    /**
     * 這個構建函數一定要增加
     * 
     * @param suipData
     */
    public FN000901(SuipData suipData) {
        super(suipData);
    }

    /**
     * 產生MAC資料
     */
    @Override
    public SuipData process() throws Exception {
        final String fn = "91";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();
        // 1.傳入參數檢核
        rc = this.checkFN000901Data();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key:901有2把Key
        RefString refKey2 = new RefString(null);
        ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
        rc = keyData.getKey(1, refKey2); // 讀取第2把CDK
        String key2 = refKey2.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        // 3.Call suip取得回應資料
        String key = this.suipData.getInputData1().substring(4); // 主機給的KEY
        // CDK.keytype+CDK+tmk.keytype+tmk+inputdata2
        // String command = this.getSuipCommand(fn, keyData.getKeys()[1].KeyType, key2,
        // keyData.getKeys()[0].KeyType, key, "", this.suipData.getInputData2());
        String keyLength2 = this.getKeyLength(keyData.getKeys()[1].KeyType);
        String keyLength1 = this.getKeyLength(keyData.getKeys()[0].KeyType);
        String command = getSuipCommand(fn, keyLength2 + StringUtil.toHex(StringUtils.rightPad(key2, 48, '0')) +
                keyLength1 + StringUtil.toHex(StringUtils.rightPad(key, 48, '0')) +
                StringUtil.toHex(this.suipData.getInputData2()));
        RefString refRtn = new RefString(null);
        rc = this.sendReceive(command, log, refRtn);
        String rtn = refRtn.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        // 4.拆解Suip回傳結果
        rc = this.parseFN000901ReturnData(keyData.getKeys()[0].KeyType, rtn);
        return this.suipData;
    }

    private ENCRC checkFN000901Data() {
        // Key:2T2MAC RMF 950 1 CDK FISC 950 1
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
            return ENCRC.KeyLengthError;
        }
        // Input_data_1 ="0032"+Ekcd(key)(H16)+Ekcd(N)(H16)
        // Input_data_1 ="0048"+Ekcd(key)(H32)+Ekcd(N)(H16)
        // Input_data_1 ="0064"+Ekcd(key)(H48)+Ekcd(N)(H16)
        if (!this.checkInputData(this.suipData.getInputData1().trim())) {
            return ENCRC.InputString1Error;
        }
        // Input_data_2 ="0016"+"9500000000000000"
        if (!this.checkInputData(this.suipData.getInputData2().trim())) {
            return ENCRC.InputString2Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000901ReturnData(String keytype, String suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        // System.out.println("FN000901 KEYTYPE =" + keytype);
        // 901 Return Data 12byte header + 4byte rc+ key(based by keytype) + mac
        if (suipReturn.length() > 47) {
            String suipRc = suipReturn.substring(24, 24 + 8);
            this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
            if (this.suipData.getRc() == 0) {
                if (keytype.equals("T1")) {
                    this.suipData.setOutputData1(
                            StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(32, 32 + 64))));
                    this.suipData
                            .setOutputData2("0008" + StringUtil.fromHex(suipReturn.substring(96, 128)));
                } else if (keytype.equals("T2")) {
                    this.suipData.setOutputData1(
                            StringUtils.join("0064", StringUtil.fromHex(suipReturn.substring(32, 32 + 64 + 64))));
                    this.suipData
                            .setOutputData2("0008" + StringUtil.fromHex(suipReturn.substring(160, 176)));
                } else {
                    this.suipData.setOutputData1(
                            StringUtils.join("0096", StringUtil.fromHex(suipReturn.substring(32, 32 + 96 + 96))));
                    this.suipData
                            .setOutputData2("0008" + StringUtil.fromHex(suipReturn.substring(224, 240)));
                }

                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
