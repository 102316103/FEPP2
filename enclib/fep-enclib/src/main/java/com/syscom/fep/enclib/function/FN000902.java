package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000902 extends ENCFunction {
    /**
     * 合庫 FN000902 Translate ATM PIN Block to Host ISO-0 ( ANSI X9.8 ) PIN Block
     * 
     * @param suipData
     */
    public FN000902(SuipData suipData) {
        super(suipData);
    }

    /**
     * 產生MAC資料
     */
    @Override
    public SuipData process() throws Exception {
        final String fn = "92";
        ENCRC rc = ENCRC.ENCLibError;
        ENCLogData log = this.suipData.getTxLog();
        // 1.傳入參數檢核
        rc = this.checkData();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }

        // 2.讀取Key:902有3把Key, 第一把是ATM的PPK
        RefString refKey1 = new RefString(StringUtils.EMPTY);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(0, refKey1);
		String key1 = refKey1.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		// 讀取第2把key, IMS的PPK
		RefString refKey2 = new RefString(StringUtils.EMPTY);
		rc = keyData.getKey(1, refKey2);
		String key2 = refKey2.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

        String key3 = "0000000000000000";
        String mode = this.suipData.getInputData1().substring(4, 6); 
     		
        
        String pinblock = this.suipData.getInputData1().substring(6); // pinblock
        String atmseqno = this.suipData.getInputData2().substring(4); // Sequence Number(4)
        String accno = "";
        if(mode.equals("01")) {
            accno = this.suipData.getInputData2().substring(4); //accno(12)

            // 讀取第3把key, ATM的MAC KEY, 只有mode 01會用到
            RefString refKey3 = new RefString(StringUtils.EMPTY);
            rc = keyData.getKey(2, refKey3);
            key3 = refKey3.get();
            if (rc != ENCRC.Normal) {
                this.suipData.setRc(rc.getValue());
                return this.suipData;
            }
        }        
        // 3.Call suip取得回應資料        
        // CDK.keytype+CDK+tmk.keytype+tmk+inputdata2
        // String command = this.getSuipCommand(fn, keyData.getKeys()[1].KeyType, key2,
        // keyData.getKeys()[0].KeyType, key, "", this.suipData.getInputData2());
        String keyLength1 = this.getKeyLength(keyData.getKeys()[0].KeyType);
        String keyLength2 = this.getKeyLength(keyData.getKeys()[1].KeyType);
        
        String command = "";
        if(mode.equals("01")){
            command = getSuipCommand(fn, mode + keyLength1 + StringUtil.toHex(StringUtils.rightPad(key1, 48, '0')) +
                                                   keyLength2 + StringUtil.toHex(StringUtils.rightPad(key2, 48, '0')) + 
                                                   StringUtil.toHex(pinblock) + 
                                                   StringUtil.toHex(key3) + 
                                                   StringUtil.toHex(StringUtils.rightPad(atmseqno, 16, '0')) +
                                                   StringUtil.toHex(accno));
        } else {
            command = getSuipCommand(fn, mode + keyLength1 + StringUtil.toHex(StringUtils.rightPad(key1, 48, '0')) +
                                                   keyLength2 + StringUtil.toHex(StringUtils.rightPad(key2, 48, '0')) + 
                                                   StringUtil.toHex(pinblock) + 
                                                   StringUtil.toHex(key3) + 
                                                   StringUtil.toHex(StringUtils.rightPad(atmseqno, 16, '0')));
        }
        
        RefString refRtn = new RefString(null);
        rc = this.sendReceive(command, log, refRtn);
        String rtn = refRtn.get();
        if (rc != ENCRC.Normal) {
            this.suipData.setRc(rc.getValue());
            return this.suipData;
        }
        // 4.拆解Suip回傳結果
        rc = this.parseReturnData(keyData.getKeys()[0].KeyType, rtn); 
        return this.suipData;
    }

    private ENCRC checkData() {
        // Key:2T2MAC RMF 950 1 CDK FISC 950 1
        if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
            return ENCRC.KeyLengthError;
        }
        
        if (!this.checkInputData(this.suipData.getInputData1().trim())) {
            return ENCRC.InputString1Error;
        }
        // Input_data_2 ="0016"+"9500000000000000"
        if (!this.checkInputData(this.suipData.getInputData2().trim())) {
            return ENCRC.InputString2Error;
        }
        return ENCRC.Normal;
    }

    private ENCRC parseReturnData(String keytype, String suipReturn) throws DecoderException {
        ENCRC rc = ENCRC.SuipReturnError;
        
        if (suipReturn.length() > 47) {
            String suipRc = suipReturn.substring(24, 24 + 8);
            this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
            if (this.suipData.getRc() == 0) {
                this.suipData.setOutputData1(StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(32, 32 + 32))));
                rc = ENCRC.Normal;
            }
        } else {
            this.suipData.setRc(rc.getValue());
        }
        return rc;
    }
}
