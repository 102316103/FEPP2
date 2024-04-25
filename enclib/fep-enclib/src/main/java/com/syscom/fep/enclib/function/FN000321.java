package com.syscom.fep.enclib.function;

import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

public class FN000321 extends ENCFunction {

    /**
     * 這個構建函數一定要增加
     *
     * @param suipData
     */
    public FN000321(SuipData suipData) {
        super(suipData);
    }

    /**
     * 檢核MAC_DATA，與檢核交易驗證碼(TAC_DATA)
     */
    @Override
    public SuipData process() throws Exception {
        final String fn = "39";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();

        // 1.傳入參數檢核
        RefBase<SuipData[]> refSuip = new RefBase<SuipData[]>(new SuipData[2]); // 應該有2組
        rc = this.checkFN000321Data(refSuip);
        SuipData[] suip = refSuip.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key
        RefString refKey1 = new RefString(null);
        ENCKey keyData1 = new ENCKey(suip[0].getKeyIdentity(), log);
        rc = keyData1.getKey(refKey1);
        String key1 = refKey1.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        RefString refKey2 = new RefString(null);
        ENCKey keyData2;
        // 有可能2個function都讀同一把Key
        if (suip[0].getKeyIdentity().equals(suip[1].getKeyIdentity())) {
            keyData2 = keyData1;
            refKey2.set(key1);
        } else {
            keyData2 = new ENCKey(suip[1].getKeyIdentity(), log);
            rc = keyData2.getKey(refKey2);
            if (rc != ENCRC.Normal) {
                this.suipData.setRc(rc.getValue());
                return this.suipData;
            }
        }
        String key2 = refKey2.get();

        // 3.Call suip取得回應資料
        String inputData = StringUtils.join(suip[0].getInputData1(), suip[1].getInputData1());
        String command = this.getSuipCommand(fn, keyData1.getKeys()[0].KeyType, key1, keyData2.getKeys()[0].KeyType, key2, suip[0].getInputData2(), inputData);
        RefString refRtn = new RefString(null);
        rc = this.sendReceive(command, log, refRtn);
        String rtn = refRtn.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 4.拆解Suip回傳結果
        rc = this.parseFN000321ReturnData(rtn);
        return this.suipData;
    }

    private ENCRC checkFN000321Data(RefBase<SuipData[]> suip) {
        if (!this.checkCompoundInputData(this.suipData.getInputData1(), suip)) {
            return ENCRC.InputString1Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000321ReturnData(String suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        // 301 Return Data 12byte header + 4byte rc=16
        if (suipReturn.length() > 47) {
            String suipRc = suipReturn.substring(24, 24 + 2);
            this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
            if(this.suipData.getRc() == 0){
                String mac = StringUtil.fromHex(suipReturn.substring(32, 32 + 16));
                this.suipData.setOutputData1(StringUtils.join("0008", mac));
                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
