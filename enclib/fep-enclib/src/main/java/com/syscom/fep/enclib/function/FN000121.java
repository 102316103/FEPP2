package com.syscom.fep.enclib.function;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

public class FN000121 extends ENCFunction {

    public FN000121(SuipData suipData) {
        super(suipData);
    }

    @Override
    public SuipData process() throws Exception {
        final String fn = "11";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = suipData.getTxLog();
        //1.傳入參數檢核
        rc = checkFN000121Data();
        if (rc != ENCRC.Normal) {
            suipData.setRc(rc.getValue());
            return suipData;
        }

        //2.讀取Key
        String key = null;
        ENCKey keyData = new ENCKey(suipData.getKeyIdentity(), log);
        RefString refKey2 = new RefString(null);
        rc = keyData.getKey(refKey2);
        key = refKey2.get();
        if (rc != ENCRC.Normal) {
            suipData.setRc(rc.getValue());
            return suipData;
        }

        //3.Call suip取得回應資料
        String inputData = suipData.getInputData1();
        String command = getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
        String rtn = null;
        RefString refRtn = new RefString(null);
        rc = sendReceive(command, log, refRtn);
        rtn = refRtn.get();
        if(rc != ENCRC.Normal) {
            suipData.setRc(rc.getValue());
            return suipData;
        }

        //4.拆解Suip回傳結果
        rc = parseFN000121ReturnData(rtn);
        //if (rc != ENCRC.Normal)
        //{
        //    _suipData.RC = (int)rc;
        //}
        return suipData;

    }

    private ENCRC checkFN000121Data() {
        if (!checkKeyIdentity(suipData.getKeyIdentity().trim())) {
            return ENCRC.KeyLengthError;
        }
        if (!checkInputData(suipData.getInputData1().trim())) {
            return ENCRC.InputString1Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000121ReturnData(String suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        //301 Return Data 12byte header + 4byte rc+ 8byte KeySyncCheckValue=24
        if (suipReturn.length() >= 48) {
            String suipRc = suipReturn.substring(24, 26);
            suipData.setRc(Integer.parseInt(suipRc, 16)); //先將Suip回應的RC塞至_suipData中
            if (suipData.getRc() == 0) {
                suipData.setOutputData1(StringUtils.join("0008" , StringUtil.fromHex(suipReturn.substring(32, 48))));
                suipData.setOutputData2("");
                rc = ENCRC.Normal;
            }
        } else {
            suipData.setRc(rc.getValue());
        }
        return rc;
    }

}
