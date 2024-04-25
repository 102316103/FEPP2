package com.syscom.fep.enclib.function;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

import org.apache.commons.codec.DecoderException;

public class FN000303 extends ENCFunction {
    /**
     * 這個構建函數一定要增加
     *
     * @param suipData
     */
    public FN000303(SuipData suipData) {
        super(suipData);
    }

    /**
     * 產生MAC資料 For RM
     */
    @Override
    public SuipData process() throws Exception {
        final String fn = "31";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();

        // 1.傳入參數檢核
        rc = this.checkFN000303Data();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        //2.讀取Key
        RefString refKey = new RefString(null);
        ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
        rc = keyData.getKey(refKey);
        String key = refKey.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        // 3.Call suip取得回應資料
        String inputData = this.suipData.getInputData1();
        String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
        RefString refRtn = new RefString(null);
        rc = this.sendReceive(command, log, refRtn);
        String rtn = refRtn.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 4.拆解Suip回傳結果
        rc = this.parseFN000303ReturnData(rtn);
        //if (rc != ENCRC.Normal)
        //{
        //    _suipData.RC = (int)rc;
        //}
        return this.suipData;
    }

    private ENCRC checkFN000303Data() {
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
            return ENCRC.KeyLengthError;
        }
        if (!this.checkInputData(this.suipData.getInputData1().trim())) {
            return ENCRC.InputString1Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000303ReturnData(String suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;

        // 301 Return Data 12byte header + 4byte rc+ 8byte key=24
        if (suipReturn.length() > 47) {
            String suipRc = suipReturn.substring(24, 24 + 2);
            this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
            if (this.suipData.getRc() == 0) {
                this.suipData.setOutputData1("0008" + StringUtil.fromHex(suipReturn.substring(32, 32 + 16)));
                this.suipData.setOutputData2("");
                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
