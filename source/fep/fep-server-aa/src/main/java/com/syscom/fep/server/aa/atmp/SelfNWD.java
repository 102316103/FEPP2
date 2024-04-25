package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;

/**
 * @author Richard
 */
public class SelfNWD extends ATMPAABase {

    public SelfNWD(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() {
        throw ExceptionUtil.createNotImplementedException(ProgramName, "尚未實作!!!");
    }
}
