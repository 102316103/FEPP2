package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000122 extends ENCFunction {
    /**
     * 這個構建函數一定要增加
     *
     * @param suipData
     */
    public FN000122(SuipData suipData) {
        super(suipData);
    }

    /**
     * 檢核MAC_DATA，與檢核交易驗證碼(TAC_DATA)
     */
    @Override
    public SuipData process() throws Exception {
        final String fn = "12";

        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();
        // 1.傳入參數檢核
        rc = this.checkFN000122Data();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key
        RefString refKey = new RefString(null);
        ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
        rc = keyData.getKey(refKey);
        String key = refKey.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }


        // 3.Call suip取得回應資料
        String inputData = StringUtils.join(this.suipData.getInputData1(), this.suipData.getInputData2());
        String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
        RefString refRtn = new RefString(null);
        rc = this.sendReceive(command, log, refRtn);
        String rtn = refRtn.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 4.拆解Suip回傳結果
        rc = this.parseFN000122ReturnData(rtn);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
        }
        return this.suipData;
    }


    private ENCRC checkFN000122Data() {
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
            return ENCRC.KeyLengthError;
        }
        //Input_data_1 ="0008"++KeySyncCheckValue(H8)
        if (!this.checkInputData(this.suipData.getInputData1().trim())) {
            return ENCRC.InputString1Error;
        }
        //Input_data_2 ="0006"+發信單位代號(N3)+收信單位代號(N3)
        if (!this.checkInputData(this.suipData.getInputData2().trim())) {
            return ENCRC.InputString2Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000122ReturnData(String suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        //301 Return Data 12byte header + 4byte rc=16
        if (suipReturn.length() >= 32) {
            rc = ENCRC.Normal;
            String suipRc = suipReturn.substring(24, 24 + 2);
            this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
