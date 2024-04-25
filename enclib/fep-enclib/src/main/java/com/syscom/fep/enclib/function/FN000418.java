package com.syscom.fep.enclib.function;

import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

public class FN000418 extends ENCFunction {
    /**
     * 這個構建函數一定要增加
     *
     * @param suipData
     */
    public FN000418(SuipData suipData) {
        super(suipData);
    }

    @Override
    public SuipData process() throws Exception {
        final String fn = "48";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();

        // 1.傳入參數檢核
        rc = this.checkFN000418Data();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        //2.讀取Key
        RefString key = new RefString(null);
        ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
        rc = keyData.getKey(key);
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        String keyType = StringUtils.EMPTY;
        if ("S1".equals(keyData.getKeys()[0].KeyType)) {
            keyType = "01";
        } else if ("T2".equals(keyData.getKeys()[0].KeyType)) {
            keyType = "02";
        } else {
            keyType = "03";
        }
        key.set(StringUtil.toHex(StringUtils.rightPad(key.get(), 48, '0')));

        // 3.Call suip取得回應資料

        // String inputData = StringUtils.join("0064", this.suipData.getInputData2().substring(4, 64 + 4), this.suipData.getInputData1());
        // String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key.get(), inputData);
        String mode = this.suipData.getInputData1().substring(4, 2 + 4).replace('0', '3');
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

        //4.拆解Suip回傳結果
        rc = this.parseFN000418ReturnData(rtn);
        // if (rc != ENCRC.Normal) {
        //     this.suipData.setRc(rc.getValue());
        //     return this.suipData;
        // }
        return this.suipData;
    }

    private ENCRC checkFN000418Data() {
        //1T3ICC C6    807
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) return ENCRC.KeyLengthError;
        //Input_data_1="0072"+交易序號(N8)+交易代號(N4)+交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+轉出帳號(N16)
        //Input_data_1="0088"+交易序號(N8)+交易代號(N4)+交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+轉入帳號(N16)+轉出帳號(N16)
        //Input_data_1="0040"+交易序號(N8)+交易代號(N4)+端末查核碼(AN8)+交易帳號/轉出帳號(N16)
        //Input_data_1="0096"+交易序號(N8)+交易代號(N4)+繳費金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+繳款類別(N5)+轉出帳號(N16)+銷帳編號(N16)
        //Input_data_1=“0024”+交易序號(N8)+DivData-1(N16)
        //Input_data_2=“0064”+ICVData-1(H16)+DivData-1(N16)+ DivData-2(N16)+ DivData-3(N16)
        if (!this.checkInputData(this.suipData.getInputData1())) return ENCRC.InputString1Error;
        if (!this.checkInputData(this.suipData.getInputData2())) return ENCRC.InputString2Error;
        return ENCRC.Normal;
    }

    private ENCRC parseFN000418ReturnData(RefString suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        //Return Data 12byte header + 4byte rc+ 16byte tac
        if (suipReturn.get().length() > 63) {
            String suipRc = suipReturn.substring(24, 2 + 24);
            this.suipData.setRc(Integer.parseInt(suipRc, 16));  //先將Suip回應的RC塞至_suipData中
            if (this.suipData.getRc() == 0) {
                // Compare TAC
                String tac = StringUtil.fromHex(suipReturn.substring(32, 32 + 32));
                this.suipData.setOutputData1(StringUtils.join("0016", tac));
                // if (!this.suipData.getInputData2().substring(68, 16 + 68).equals(tac)) {
                //     this.suipData.setRc(ENCRC.CheckReturn1Error.getValue());
                // }
                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
