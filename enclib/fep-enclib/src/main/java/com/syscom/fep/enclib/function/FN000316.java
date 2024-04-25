package com.syscom.fep.enclib.function;

import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

public class FN000316 extends ENCFunction {
    /**
     * 這個構建函數一定要增加
     *
     * @param suipData
     */
    public FN000316(SuipData suipData) {
        super(suipData);
    }

    @Override
    public SuipData process() throws Exception {
        final String fn = "32";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();

        // 1.傳入參數檢核
        RefBase<SuipData[]> suip = new RefBase<>(new SuipData[2]); //應該有2組
        rc = checkFN000316Data(suip);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key
        RefString key1 = new RefString(null);
        ENCKey keyData1 = new ENCKey(suip.get()[0].getKeyIdentity(), log);
        rc = keyData1.getKey(key1);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        RefString key2 = new RefString(null);
        ENCKey keyData2;
        //有可能2個function都讀同一把Key
        if (suip.get()[0].getKeyIdentity().equals(suip.get()[1].getKeyIdentity())) {
            keyData2 = keyData1;
            key2 = key1;
        } else {
            keyData2 = new ENCKey(suip.get()[1].getKeyIdentity(), log);
            rc = keyData2.getKey(key2);
            if (rc != ENCRC.Normal) {
                this.suipData.setRc(rc.getValue());
                return this.suipData;
            }
        }
        key1.set(StringUtil.toHex(StringUtils.rightPad(key1.get(), 48, '0')));
        key2.set(StringUtil.toHex(StringUtils.rightPad(key2.get(), 48, '0')));
        String keyType1 = this.getKeyLength(keyData1.getKeys()[0].KeyType);
        String keyType2 = this.getKeyLength(keyData2.getKeys()[0].KeyType);

        // 3.Call suip取得回應資料
        String inputData1_1 = suip.get()[0].getInputData1();
        String mac = suip.get()[0].getInputData2();
        String inputData2_1 = suip.get()[1].getInputData1(); //tac的inputdata1
        String inputData2_2 = suip.get()[1].getInputData2();
        String mode = inputData2_1.substring(4, 2 + 4).replace('0', '3');
        inputData2_1 = inputData2_1.substring(6);
        inputData2_1 = StringUtils.join(StringUtils.leftPad(Integer.toString(inputData2_1.length()), 4, '0'), inputData2_1);

        // String inputData = StringUtils.join(suip.get()[0].getInputData1(), suip.get()[1].getInputData1(), suip.get()[1].getInputData2());
        // String command = this.getSuipCommand(fn, keyData1.getKeys()[0].KeyType, key1, keyData2.getKeys()[0].KeyType, key2, suip.get()[0].getInputData2(), inputData);
        String inputData = StringUtils.join(mode, keyType1, key1, keyType2, key2,
                StringUtil.toHex(mac),
                StringUtil.toHex(StringUtils.join(inputData1_1, inputData2_1, inputData2_2)));
        String command = this.getSuipCommand(fn, inputData);
        RefString rtn = new RefString(null);
        rc = this.sendReceive(command, log, rtn);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        //4.拆解Suip回傳結果
        rc = this.parseFN000316ReturnData(rtn);
        // if (rc != ENCRC.Normal) {
        //     this.suipData.setRc(rc.getValue());
        // }
        return this.suipData;
    }

    private ENCRC checkFN000316Data(RefBase<SuipData[]> suip) {
        if (!this.checkCompoundInputData(this.suipData.getInputData1(), suip)) {
            return ENCRC.InputString1Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseFN000316ReturnData(RefString suipReturn) {
        ENCRC rc = ENCRC.SuipReturnError;
        // 301 Return Data 12byte header + 4byte rc=16
        if (suipReturn.get().length() > 31) {
            String suipRc = suipReturn.substring(24, 2 + 24);
            this.suipData.setRc(Integer.parseInt(suipRc, 16));  // 先將Suip回應的RC塞至SuipData中
            rc = ENCRC.Normal;
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
