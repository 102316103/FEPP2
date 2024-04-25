package com.syscom.fep.enclib.function;

import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

public class FN000135 extends ENCFunction {
    /**
     * 這個構建函數一定要增加
     *
     * @param suipData
     */
    public FN000135(SuipData suipData) {
        super(suipData);
    }

    @Override
    public SuipData process() throws Exception {
        final String fn = "1F";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();

        // 1.傳入參數檢核
        rc = this.checkFN000135Data();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key 1T3ICC C6    807     "
        RefString key = new RefString(null);
        ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
        rc = keyData.getKey(key);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        String keyType = getKeyLength(keyData.getKeys()[0].KeyType);
        key.set(StringUtil.toHex(StringUtils.rightPad(key.get(), 48, '0')));
        // 3.Call suip取得回應資料
        String mode = this.suipData.getInputData1().substring(4, 2 + 4).replace('0', '3');
        // String inputData = StringUtils.join(this.suipData.getInputData2(), this.suipData.getInputData1());
        // String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key.get(), inputData);
        String inputdata1 = this.suipData.getInputData1().substring(6);
        String inputData = StringUtils.join(mode, keyType, key, StringUtil.toHex(this.suipData.getInputData2()),
                StringUtil.toHex(StringUtils.join(StringUtils.leftPad(Integer.toString(inputdata1.length()), 4, '0'), inputdata1)));
        String command = this.getSuipCommand(fn, inputData);
        RefString rtn = new RefString(null);
        rc = this.sendReceive(command, log, rtn);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 4.拆解Suip回傳結果
        rc = this.parseFN000135ReturnData(rtn);
        // if (rc != ENCRC.Normal) {
        //     this.suipData.setRc(rc.getValue());
        // }
        return this.suipData;
    }

    private ENCRC checkFN000135Data() {
        // 1T3ICC C6    807
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) return ENCRC.KeyLengthError;
           /*1. Input_data_1="0072"+交易序號(N8)+交易代號(N4)+交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+轉出帳號(N16)
            2.  Input_data_1="0088"+交易序號(N8)+交易代號(N4)+ 交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+轉入帳號(N16)+轉出帳號(N16)
            3.  Input_data_1="0040"+交易序號(N8)+交易代號(N4)+ 端末查核碼(AN8)+轉出帳號(N16)
            4.  Input_data_1="0096"+交易序號(N8)+交易代號(N4)+ 繳費金額(NS14)+端末代號(N8)+端末查核碼(AN8)+ 交易日期時間(N14)+繳款類別(N5)+轉出帳號(N16)+ 銷帳編號(N16)
            Input_data_2="0048"+DivData(N48)*/

        if (!this.checkKeyIdentity(this.suipData.getInputData1().trim())) return ENCRC.InputString1Error;
        // Input_data_2="0048"+DivData(N48)
        if (!this.checkKeyIdentity(this.suipData.getInputData2().trim())) return ENCRC.InputString2Error;
        return ENCRC.Normal;
    }

    private ENCRC parseFN000135ReturnData(RefString suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        // 311 Return Data 12byte header + 4byte rc+ 8byte key=24
        if (suipReturn.get().length() > 63) {
            String suipRc = suipReturn.substring(24, 2 + 24);
            this.suipData.setRc(Integer.parseInt(suipRc, 16));  //先將Suip回應的RC塞至SuipData中
            if (this.suipData.getRc() == 0) {
                // Return_data_1 ="0016"+MAC(16)
                this.suipData.setOutputData1(StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(32, 32 + 32))));
                this.suipData.setOutputData2(StringUtils.EMPTY);
                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
