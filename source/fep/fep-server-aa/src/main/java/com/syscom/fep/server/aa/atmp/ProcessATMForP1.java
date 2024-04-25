package com.syscom.fep.server.aa.atmp;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.server.common.adapter.IMSAdapter;
import com.syscom.fep.server.common.adapter.MobileAdapter;
import com.syscom.fep.vo.communication.ToSendQueryAccountCommu;

/**
 * P1階段負責接收ATMGW來之ATM交易, 檢核及重壓MAC後送至主機,取得主機回應後, 再用ATM KEY重壓mac後回給ATM
 *
 * @author Jaime
 */
public class ProcessATMForP1 extends ATMPAABase {
    // 1.宣告private變數
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private Boolean needCheckMac = false;

    public ProcessATMForP1(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 處理電文
     *
     * @return
     */
    @Override
    public String processRequestData() throws Exception {
        RefString response = new RefString("");
        try {
            // 2.記錄FEPLOG內容
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage(this.getTxData().getTxRequestMessage());
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);

            // 20230213 add判斷是否為補褶交易(電文開頭是EBTSPM00),補褶交易只需送主機
            Boolean isPassbookTx = false;
            if ("40C5C2E3E2D7D4F0".equals(getTxData().getTxRequestMessage().substring(0, 16))) {
                isPassbookTx = true;
            }

            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), " send data ASCII "));
            this.logContext.setMessage(EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage()));
            this.logContext.setRemark(" by PBO :" + isPassbookTx);
            logMessage(this.logContext);
            
            //3. 	Prepare():記錄MessageText
            // 非補摺交易
            if(!isPassbookTx) {
            	 _rtnCode = getATMBusiness().prepareFEPTXN();
                 
                 addTxData();
                 updateFeptxnAaRc();
            }
            
            // 4.檢核交易MAC 驗ATM MAC
            //非補摺交易且指靜脈建置(FV)、企業存款及授權查詢(D8/I5)、企業名稱查詢(C6)不驗MAC

            if (!isPassbookTx && !"FV".equals(getTxData().getFscode()) && !"I5".equals(getTxData().getFscode()) &&
                    !"D8".equals(getTxData().getFscode()) && !"C6".equals(getTxData().getMsgType()) && !"C5".equals(getTxData().getMsgType())) {
                _rtnCode = this.checkMac();
                updateFeptxnAaRc();
                needCheckMac = true;
            } else {
                if ("FC7".equals(getTxData().getMsgCategory() + getTxData().getMsgType())) {
                    _rtnCode = this.checkMac();
                    updateFeptxnAaRc();
                    needCheckMac = true;
                }
            }
            // 5. 判斷是否需轉換PIN, 若是則呼叫DESHelper轉換PIN
            if (!isPassbookTx && _rtnCode == FEPReturnCode.Normal) {
                _rtnCode = CheckPinBlockConvert();
                updateFeptxnAaRc();
                this.logContext.setRemark("CheckPinBlockConvert _rtnCode:" + _rtnCode);
                logMessage(this.logContext);
            }
            // 6. 判斷是否呼叫手機門號轉帳API //20221205 Add
            // StringUtil.fromHex(getTxData().getTxRequestMessage())
            if (!isPassbookTx && "E3D4".contains(getTxData().getTxRequestMessage().substring(194, 198))
                    && "F2F5F2".contains(getTxData().getTxRequestMessage().substring(368, 374))) {
                this.logContext.setRemark(StringUtils.join("Enter ", "into MobileAdapter"));
                logMessage(this.logContext);
                // SYSCONF - 9 要加一個MobileQueryUrl
                try {
                    ToSendQueryAccountCommu request = new ToSendQueryAccountCommu();
                    // 為了打通測試使用假資料，此測試資料可以串通到對方，並回傳此門號不存在
                    String ascText = EbcdicConverter.fromHex(CCSID.English, getTxData().getTxRequestMessage().substring(342, 358));
                    request.setMobilePhone(EbcdicConverter.fromHex(CCSID.English, getTxData().getTxRequestMessage().substring(210,
                            230)));
                    request.setIdNo("AT" + ascText);
                    request.setBankCode("");

                    MobileAdapter mobileAdapter = new MobileAdapter(getTxData());
                    mobileAdapter.setToSendQueryAccountCommu(request);
                    _rtnCode = mobileAdapter.sendReceive();
                    updateFeptxnAaRc();
                    this.logContext.setRemark(StringUtils.join("into MobileAdapter _rtnCode ", _rtnCode));
                    logMessage(this.logContext);
                } catch (Exception e) {
                    logContext.setProgramException(e);
                    logContext.setProgramName(ProgramName);
                    logMessage(this.logContext);
                    // sendEMS(logContext);
                    // _rtnCode = handleException(e, "processRequestData");
                }
                // 本函式請包try catch, 不論呼叫結果成功或失敗, 均回傳return FEPReturnCode.Normal;
                // 若有Exception請SendEMS即可
                _rtnCode = FEPReturnCode.Normal;
            }

            // 7.用主機KEY重壓MAC 壓主機MAC
            RefString cbsmac = new RefString("");
            if (needCheckMac && _rtnCode == FEPReturnCode.Normal) {
                _rtnCode = this.makeCBSMac(cbsmac);
                updateFeptxnAaRc();
                this.logContext.setRemark("make cbsmac ASCII:" + cbsmac.get());
                logMessage(this.logContext);
            } else {
                //for 正式環境暫時修改，fc5不驗atm mac但要壓cbsmac
                if ("C5".equals(getTxData().getMsgType())) {
                    _rtnCode = this.makeCBSMac(cbsmac);
                    this.logContext.setRemark("make cbsmac ASCII:" + cbsmac.get());
                    logMessage(this.logContext);
                }
            }
            // if (!isPassbookTx && _rtnCode == FEPReturnCode.Normal) {
            //     if (!"FV".equals(getTxData().getFscode()))
            //         _rtnCode = this.makeCBSMac(cbsmac);
            //     else {
            //         if ("FC7".equals(getTxData().getMsgCategory() + getTxData().getMsgType()))
            //             _rtnCode = this.makeCBSMac(cbsmac);
            //     }

            // }
            // 記錄FEPLOG內容

            // 8.上送主機並取得主機回應
            if (_rtnCode == FEPReturnCode.Normal || isPassbookTx) {
                _rtnCode = this.sendToCBS(EbcdicConverter.toHex(CCSID.English, cbsmac.get().length(), cbsmac.get()),
                        response, isPassbookTx);
                updateFeptxnAaRc();
                // 記錄FEPLOG內容
                this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".sendToCBS"));
                this.logContext.setMessage(response.get());

                if (StringUtils.isBlank(response.get())) {
                    this.logContext.setRemark("sendToCBS response is null");
                    //logMessage(this.logContext);
                    _rtnCode = FEPReturnCode.HostResponseTimeout;
                    updateFeptxnAaRc();
                } else {
                    this.logContext.setRemark("sendToCBS Get response OK");
                }
                this.logContext.setRemark("sendToCBS _rtnCode:" + _rtnCode);
                logMessage(this.logContext);
            }

            //補驗證cbsmac
//            if (!isPassbookTx && _rtnCode == FEPReturnCode.Normal&&"I1".equals(getTxData().getFscode())) {
//                _rtnCode = this.checkCBSMac(response.get());
//            }

            //FC9-WF 回應時重壓PINBLOCK
            if (_rtnCode == FEPReturnCode.Normal && "WF".equals(getTxData().getFscode()) && "C9".equals(getTxData().getMsgType())) {
                _rtnCode = CheckPinBlockConvertRS(response);
                updateFeptxnAaRc();
                this.logContext.setRemark("CheckPinBlockConvertRS _rtnCode:" + _rtnCode);
                logMessage(this.logContext);
            }

            // 9.用ATM KEY重壓Response電文MAC 壓ATM MAC
            RefString atmmac = new RefString(null);
            if (!isPassbookTx && _rtnCode == FEPReturnCode.Normal) {
                _rtnCode = this.makeATMMac(response.get(), atmmac);
                updateFeptxnAaRc();
                this.logContext.setRemark("makeATMMac _rtnCode:" + _rtnCode);
                logMessage(this.logContext);
            }



            // 10.回傳Response及ReturnCode 回給ATM
            if (!isPassbookTx && _rtnCode == FEPReturnCode.Normal) {
                // 送回給ATM的KEY跟MAC, 都不轉EBCDIC, ascii轉成hex 送給主機
                // response.set(StringUtils.join(response.substring(0, response.get().length() -
                // 16),
                // EbcdicConverter.toHex(CCSID.English, atmmac.get().length(), atmmac.get())));
                // response.set(StringUtils.join(response.substring(0, response.get().length() - 16),
                //跨國EMV 交易的MAC位置調整
                if ("F2F6".contains(getTxData().getTxRequestMessage().substring(368, 372)) && "AA".equals(getTxData().getMsgType())) {
                    if ("D7D5".equals(response.substring(18, 22))) {
                        response.set(StringUtils.join(response.substring(0, 392),
                                atmmac.get(), response.substring(408, response.get().length())));
                    } else {
                        response.set(StringUtils.join(response.substring(0, 410),
                                atmmac.get(), response.substring(426, response.get().length())));
                    }
                } else {
                    response.set(StringUtils.join(response.substring(0, response.get().length() - 16),
                            atmmac.get()));
                }
            }
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            _rtnCode = handleException(e, "processRequestData");
        } finally {
        	updateFeptxnAaRc();
            //若有錯誤 則回應錯誤電文
            if (_rtnCode != FEPReturnCode.Normal) {
                //暫時使用substring切出response所需電文
                String res = "";
                String WSID = getTxData().getTxRequestMessage().substring(18, 28);
                String S1 = "40";
                String RECFMT = "F0";
                String POAPUSE = "40";
                String MSGCAT = "C6";
                String MSGTYP = "D7C3";
                String TRANDATE = getTxData().getTxRequestMessage().substring(40, 52);
                String TRANTIME = getTxData().getTxRequestMessage().substring(52, 64);
                String TRANSEQ = getTxData().getTxRequestMessage().substring(64, 72);
                String TDRSEG = getTxData().getTxRequestMessage().substring(72, 76);
                String PRCRDACT = "F0";
                RefString mac = new RefString(null);
                res = WSID + S1 + RECFMT + POAPUSE + MSGCAT + MSGTYP + TRANDATE + TRANTIME + TRANSEQ + TDRSEG + PRCRDACT;
                if (needCheckMac) {
                    ENCHelper encHelper = new ENCHelper(getTxData());
                    String atmNo = getTxData().getAtmNo();
                    encHelper.makeAtmMac(atmNo, remainderToF0(res), mac);
                    res = res + EbcdicConverter.toHex(CCSID.English, 8, mac.get().substring(0, 8));
                } else {
                    res = res + "F0F0F0F0F0F0F0F0";
                }
                response.set(res);
                this.logContext.setProgramFlowType(ProgramFlow.AAOut);
                this.logContext.setMessageFlowType(MessageFlow.Response);
                this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
                this.logContext.setMessage(response.get());
                this.logContext.setRemark(StringUtils.join("FEP Exception,Exit ", this.getTxData().getAaName()));
                logMessage(this.logContext);
            } else {
                // 記錄FEPLOG內容
                this.logContext.setProgramFlowType(ProgramFlow.AAOut);
                this.logContext.setMessageFlowType(MessageFlow.Response);
                this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
                this.logContext.setMessage(response.get());
                this.logContext.setRemark(StringUtils.join("Exit ", this.getTxData().getAaName()));
                logMessage(this.logContext);
            }
        }
        return response.get();
    }

    /**
     * 檢核ATM電文的MAC資料
     *
     * @return
     */
    private FEPReturnCode checkMac() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(getTxData());
            // 2.取出ATM代號, 壓mac內容及mac值
            String inputData = "";
            String mac = "";
            if (getTxData() != null && StringUtils.isNotBlank(getTxData().getFscode())) {
                // 國際卡
                if ("SW".contains(getTxData().getFscode())
                        || "PC".contains(getTxData().getFscode())
                        || "VC".contains(getTxData().getFscode())
                        || "CC".contains(getTxData().getFscode())
                        || "MC".contains(getTxData().getFscode())
                        || "JC".contains(getTxData().getFscode())
                        || "SI".contains(getTxData().getFscode())
                        || "PQ".contains(getTxData().getFscode())
                        || "CQ".contains(getTxData().getFscode())) { // EMV&JCB
                    inputData = getTxData().getTxRequestMessage().substring(36, 750);
                    mac = getTxData().getTxRequestMessage().substring(750, 766);
                } else if ("P5".equals(getTxData().getFscode())
                        || "P6".equals(getTxData().getFscode())) {
                    // 國際卡變更密碼
                    inputData = getTxData().getTxRequestMessage().substring(36, 360);
                    mac = getTxData().getTxRequestMessage().substring(360, 376);
                    // P1磁條卡密碼變更 , 判斷長度原因 P1晶片卡密碼變更通知不往這走
                } else if ("P1".equals(getTxData().getFscode()) && !(getTxData().getTxRequestMessage().substring(358, 368).equals("D240F2F0F0"))) {
                    // 磁條卡
                    inputData = getTxData().getTxRequestMessage().substring(36, getTxData().getTxRequestMessage().length() - 16);
                    mac = getTxData().getTxRequestMessage().substring(getTxData().getTxRequestMessage().length() - 16, getTxData().getTxRequestMessage().length());
                } else if ("D9".equals(getTxData().getFscode())) {
                    // 企業無卡存款
                    inputData = getTxData().getTxRequestMessage().substring(36, 492);
                    mac = getTxData().getTxRequestMessage().substring(492, 508);
                    // } else if ("I5".equals(getTxData().getFscode()) || "D8".equals(getTxData().getFscode())) {
                    //     inputData = getTxData().getTxRequestMessage().substring(36, 514);
                    //     mac = getTxData().getTxRequestMessage().substring(730, 746);
                } else {
                    inputData = getTxData().getTxRequestMessage().substring(36, 742);
                    // ascii ATM-FEP上送主機前，從MSGCAT(=F)抓取240 bytes作為input來產生MAC
                    // inputData = getTxData().getTxRequestMessage().substring(34, 514);
                    mac = getTxData().getTxRequestMessage().substring(742, 758);
                }
            // } else {
            //     inputData = getTxData().getTxRequestMessage().substring(36, 514);
            //     mac = getTxData().getTxRequestMessage().substring(742, 758);

            }

            String atmNo = getTxData().getAtmNo();
            // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
            //mac = EbcdicConverter.fromHex(CCSID.English, mac);
            this.logContext.setRemark("Begin checkAtmMac mac:" + mac);
            logMessage(this.logContext);
            inputData = TrimSpace(inputData);  //去掉多餘40
            rtnCode = encHelper.checkAtmMac(atmNo, remainderToF0(inputData), mac);

            this.logContext.setRemark("after checkAtmMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        } catch (Exception e) {
            rtnCode = handleException(e, "checkMac");
        }
        return rtnCode;
    }


//    private FEPReturnCode checkCBSMac(String response) {
//        FEPReturnCode rtnCode = FEPReturnCode.Normal;
//        try {
//
//            // 1.建立ENCHelper物件
//            ENCHelper encHelper = new ENCHelper(getTxData());
//            //FPN/FPC(3)+日期(6)+時間(6)+交易序號(7)+端末機代號(8)+ 端末交易序號(6)+RC(4)
//            String inputData = getTxData().getTxRequestMessage().substring(16,46)+getTxData().getTxRequestMessage().substring(370,384);
//            String cbsmac="";
//
//            // 3.呼叫
//            RefString mac = new RefString(null);
//            rtnCode = encHelper.makeCbsMac(remainderToF0(inputData), mac);
//            if (rtnCode == FEPReturnCode.Normal) {
//                if ("F2F6".contains(getTxData().getTxRequestMessage().substring(368, 372)) && "AA".equals(getTxData().getMsgType())) {
//                    if ("D7D5".equals(response.substring(18, 22))) {
//                        cbsmac = response.substring(392, 408);
//                    } else {
//                        cbsmac = response.substring(410, 426);
//                    }
//                } else {
//                    cbsmac = response.substring(response.length() - 16, response.length());
//                }
//
//                if (cbsmac.equals(mac))
//                    rtnCode = FEPReturnCode.Normal;
//                else
//                    rtnCode = FEPReturnCode.ENCCheckMACError;
//                this.logContext.setRemark("after checkCBSMac oldcbsmac:" + cbsmac + ",newmac:" + mac);
//                logMessage(this.logContext);
//
//            }
//
//
//        } catch (Exception e) {
//            rtnCode = handleException(e, "checkCBSMac");
//        }
//        return rtnCode;
//    }


    /**
     * 把字串補足至 8 的倍數 ，不足以F0補足
     *
     * @param cbsmac
     * @return
     */
    private String remainderToF0(String inputData) {
        String rtnStr = "";
        int instr = inputData.length();
        instr = instr / 2;
        if (instr % 8 != 0) {
            int remainder = instr % 8;
            remainder = 8 - remainder;
            rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
            rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
        }

        return inputData + rtnStr;
    }

    /**
     * 產生主機的MAC資料
     *
     * @param cbsmac
     * @return
     */
    private FEPReturnCode makeCBSMac(RefString cbsmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(getTxData());
            String inputData = null;
//            String ascText = EbcdicConverter.fromHex(CCSID.English, getTxData().getTxRequestMessage().substring(110, 142));
            if ("P5".equals(getTxData().getFscode())
                    || "P6".equals(getTxData().getFscode())) {
                // 國際卡變更密碼
                inputData = getTxData().getTxRequestMessage().substring(34, 354);
            } else {
                inputData = getTxData().getTxRequestMessage().substring(34, 514);
//                inputData = getTxData().getTxRequestMessage().substring(36, 742);
            }

            // if ("SW".contains(getTxData().getFscode())
            // || "PC".contains(getTxData().getFscode())
            // || "VC".contains(getTxData().getFscode())
            // || "CC".contains(getTxData().getFscode())
            // || "MC".contains(getTxData().getFscode())
            // || "JC".contains(getTxData().getFscode())
            // || "SI".contains(getTxData().getFscode())
            // || "PQ".contains(getTxData().getFscode())
            // || "CQ".contains(getTxData().getFscode())) { // EMV&JCB
            // inputData = getTxData().getTxRequestMessage().substring(34, 750);
            // } else if ("F0".equals(getTxData().getTxRequestMessage().substring(282,
            // 284))) {
            // // 磁條卡
            // inputData = getTxData().getTxRequestMessage().substring(34, 596);
            // } else if ("P5".equals(getTxData().getFscode())
            // || "P6".equals(getTxData().getFscode())) {
            // // 國際卡變更密碼
            // inputData = getTxData().getTxRequestMessage().substring(34, 360);
            // } else if ("D9".equals(getTxData().getFscode())) {
            // // 企業無卡存款
            // inputData = getTxData().getTxRequestMessage().substring(34, 492);
            // } else {
            //// inputData = getTxData().getTxRequestMessage().substring(34, 742);
            // inputData = getTxData().getTxRequestMessage().substring(34, 514);
            // }
            // ascii ATM-FEP上送主機前，從MSGCAT(=F)抓取240 bytes作為input來產生MAC

            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMac(inputData, mac);
            // 4.若rtnCode=normal, 則cbsmac = mac
            if (rtnCode == FEPReturnCode.Normal) {
                cbsmac.set(mac.get());
            }
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        } catch (Exception e) {
            rtnCode = handleException(e, "makeCBSMac");
        }
        return rtnCode;
    }

    /**
     * 將交易送至主機並等待回應
     *
     * @param cbsmac
     * @param response
     * @return
     */
    private FEPReturnCode sendToCBS(String cbsmac, RefString response, boolean isPassbookTx) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立IMSAdapter物件
            IMSAdapter adapter = new IMSAdapter(getTxData());
            // 2.設定上傳主機電文
            // ATM電文的mac改用主機key壓過置換掉
            String tita = null;
//            String ascText = EbcdicConverter.fromHex(CCSID.English, getTxData().getTxRequestMessage().substring(110, 142));
            if (getTxData() != null && StringUtils.isNotBlank(getTxData().getFscode())) {
                if ("SW".contains(getTxData().getFscode())
                        || "PC".contains(getTxData().getFscode())
                        || "VC".contains(getTxData().getFscode())
                        || "CC".contains(getTxData().getFscode())
                        || "MC".contains(getTxData().getFscode())
                        || "JC".contains(getTxData().getFscode())
                        || "SI".contains(getTxData().getFscode())
                        || "PQ".contains(getTxData().getFscode())
                        || "CQ".contains(getTxData().getFscode())) { // EMV&JCB
                    // tita = getTxData().getTxRequestMessage().substring(34, 750) + cbsmac;
                    tita = getTxData().getTxRequestMessage().substring(0, 750) + cbsmac;
                    // MAC之後要加上原始電文TAC到一千
                    if (getTxData().getTxRequestMessage().length() >= 1000) {
                        tita = tita + getTxData().getTxRequestMessage().substring(766, 1000);
                    } else {
                        // 防止爆掉 如果有電文不到一千長度
                        tita = tita + getTxData().getTxRequestMessage().substring(766,
                                getTxData().getTxRequestMessage().length());
                    }
                } else if ("P5".equals(getTxData().getFscode())
                        || "P6".equals(getTxData().getFscode())) {
                    // tita = getTxData().getTxRequestMessage().substring(0, 354) + cbsmac;
                    // 國際卡變更密碼
                    tita = getTxData().getTxRequestMessage().substring(0, 360) + cbsmac;
                    // MAC之後要加上原始電文TAC到一千
                    if (getTxData().getTxRequestMessage().length() >= 1000) {
                        tita = tita + getTxData().getTxRequestMessage().substring(376, 1000);
                    } else {
                        // 防止爆掉 如果有電文不到一千長度
                        tita = tita + getTxData().getTxRequestMessage().substring(376,
                                getTxData().getTxRequestMessage().length());
                    }
                    // P1磁條卡密碼變更 , 判斷長度原因 P1晶片卡密碼變更通知不往這走
                } else if ("P1".equals(getTxData().getFscode()) && !(getTxData().getTxRequestMessage().substring(358, 368).equals("D240F2F0F0"))) {
                    tita = getTxData().getTxRequestMessage().substring(0, 596) + cbsmac;
                    // MAC之後要加上原始電文TAC到一千
                    if (getTxData().getTxRequestMessage().length() >= 1000) {
                        tita = tita + getTxData().getTxRequestMessage().substring(tita.length(), 1000);
                    } else {
                        // 防止爆掉 如果有電文不到一千長度
                        tita = tita + getTxData().getTxRequestMessage().substring(tita.length(), getTxData().getTxRequestMessage().length());
                    }
                } else if ("D9".equals(getTxData().getFscode())) {
                    // 企業無卡存款
                    tita = getTxData().getTxRequestMessage().substring(0, 492) + cbsmac;
                    // MAC之後要加上原始電文TAC到一千
                    if (getTxData().getTxRequestMessage().length() >= 1000) {
                        tita = tita + getTxData().getTxRequestMessage().substring(508, 1000);
                    } else {
                        // 防止爆掉 如果有電文不到一千長度
                        tita = tita + getTxData().getTxRequestMessage().substring(508,
                                getTxData().getTxRequestMessage().length());
                    }
                    // tita = getTxData().getTxRequestMessage().substring(34,492) + cbsmac;
                } else if ("I5".equals(getTxData().getFscode()) || "D8".equals(getTxData().getFscode()) || ("FV".contains(getTxData().getFscode()) && "AA".equals(getTxData().getMsgType()))) {
                    // I5 D8 FAA FV沒有mac 不須壓mac
                    tita = getTxData().getTxRequestMessage();
                    //tita = getTxData().getTxRequestMessage().substring(0, 730) + cbsmac;
                    // MAC之後要加上原始電文TAC到一千
                    //tita = tita + getTxData().getTxRequestMessage().substring(746, 1000);
                } else {
                    if (StringUtils.isNotBlank(cbsmac)) {
                        if (getTxData().getTxRequestMessage().length() >= 742) {
                            tita = getTxData().getTxRequestMessage().substring(0, 742) + cbsmac;
                        } else {
                            // 如果有電文不到742長度可能會出問題，防爆掉
                            int titalength = getTxData().getTxRequestMessage().length() - 16;
                            tita = getTxData().getTxRequestMessage().substring(0, titalength) + cbsmac;
                        }
                        // tita = getTxData().getTxRequestMessage().substring(34, 742) + cbsmac;
                        // MAC之後要加上原始電文TAC到一千
                        if (getTxData().getTxRequestMessage().length() >= 1000) {
                            tita = tita + getTxData().getTxRequestMessage().substring(758, getTxData().getTxRequestMessage().length());
                        } else {
                            // 防止爆掉 如果有電文不到一千長度
                            tita = tita + getTxData().getTxRequestMessage().substring(758,
                                    getTxData().getTxRequestMessage().length());
                        }
                    } else {
                        tita = getTxData().getTxRequestMessage().substring(0, getTxData().getTxRequestMessage().length());
                    }

                }
            } else {
                if (getTxData().getTxRequestMessage().length() >= 742) {
                    tita = getTxData().getTxRequestMessage().substring(0, 742) + cbsmac;
                    // MAC之後要加上原始電文TAC到一千
                    if (getTxData().getTxRequestMessage().length() >= 1000) {
                        tita = tita + getTxData().getTxRequestMessage().substring(758, 1000);
                    } else {
                        // 防止爆掉 如果有電文不到一千長度
                        tita = tita + getTxData().getTxRequestMessage().substring(758,
                                getTxData().getTxRequestMessage().length());
                    }
                } else {
                    // 補摺
                    tita = getTxData().getTxRequestMessage() + cbsmac;
                }
            }


            // FV上送IMS不須提供ATMNO
            String fscode="";
            if(StringUtils.isNotBlank(getTxData().getFscode())){
                fscode=getTxData().getFscode();
            }
            else{
                fscode="pbo";
            }

            // FC7-FV 要加
            if (!("FV".contains(fscode) && !"C7".equals(fscode))) {
                // FC9-WF 不加
                if (!("WF".contains(fscode) && "C9".equals(fscode))) {
                    // TOTAL:1018
                    // 補空白到1000，把ATM代號放到1000，再把長度補滿1018，長度不對會失敗
                    if (!isPassbookTx) {
                        // terminal id固定放在501
                        if (tita.length() > 1000) {
                            tita = tita.substring(0, 1000);
                        }

                        adapter.setMessageToIMS(StringUtils.rightPad(tita, 1000, "40") + StringUtils
                                .join(EbcdicConverter.toHex(CCSID.English, getTxData().getAtmNo().length(),
                                        getTxData().getAtmNo()))
                                + StringUtils.join("40"));
                    } else {
                        // 補摺 不做 「補空白到1000，把ATM代號放到1000，再把長度補滿1018，長度不對會失敗」的邏輯
                        adapter.setMessageToIMS(tita.substring(18)); //補摺去掉前面trancode
                        adapter.setTranCode("EBTSPM00");
                    }
                } else {
                    adapter.setMessageToIMS(tita);
                }
            } else {
                adapter.setMessageToIMS(tita);
            }

            if (!isPassbookTx && StringUtils.isNotEmpty(tita)) {
                adapter.setCBSTxid(this.getTxData().getMsgCategory() + this.getTxData().getMsgType() + "-" + this.getTxData().getFscode());
                adapter.setATMSeq(tita.substring(64, 72));
                adapter.setWSID(tita.substring(18, 28));
            }


            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), " sendToCBS "));
            this.logContext.setMessage("MessageToIMS:" + adapter.getMessageToIMS());
            this.logContext.setRemark("adapter TranCode: " + adapter.getTrancode() + ", sendToCBS tita:" + tita);
            logMessage(this.logContext);
            // 3.呼叫
            // TODO 客戶端可以測
            rtnCode = adapter.sendReceive();
            // 4.若rtnCode=normal
            if (rtnCode == FEPReturnCode.Normal) {
                response.set(adapter.getMessageFromIMS());
            }
        } catch (Exception e) {
            rtnCode = handleException(e, "sendToCBS");
        }
        return rtnCode;
    }

    /**
     * 產生ATM回應電文的MAC資料
     *
     * @param response
     * @param atmmac
     * @return
     */
    private FEPReturnCode makeATMMac(String response, RefString atmmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            String inputData = "";
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(getTxData());
            // 2
            // 從第0個byte抓到倒數第8個byte
            // inputData = response.substring(0, response.length() - 16);
            // 餘額查詢
            if(response.substring(18,22).equals("D7D5")&&response.substring(60,62).equals("C9")){
                inputData = response.substring(18, 286);
            }
            else{
                inputData = response.substring(18, 60);
            }
            // 3.呼叫
            RefString mac = new RefString(null);
            // String atmNo = getTxData().getAtmNo();
            // String atmSno = StringUtils.leftPad(getATMBusiness().getAtmStr().getAtmSno(),
            // 8, ' ');
            String atmNo = getTxData().getAtmNo();
            // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
            rtnCode = encHelper.makeAtmMac(atmNo, remainderToF0(inputData), mac);

            // 4.若rtnCode=normal
            if (rtnCode == FEPReturnCode.Normal) {
                // if(atmNo.equals("T9997X01")) {
                //回atm的mac也是ebcdic
                atmmac.set(EbcdicConverter.toHex(CCSID.English, 8, mac.get().substring(0, 8)));
                // }
                // else{
                //     atmmac.set(mac.get());
                // }

            }
        } catch (Exception e) {
            rtnCode = handleException(e, "makeATMMac");
        }
        return rtnCode;
    }

    private FEPReturnCode CheckPinBlockConvertRS(RefString response) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCKeyType keyType = ENCKeyType.None;
        String mode = ""; //00 for ANSI9.8 , 01 for 3624
        String accno = ""; //帳號
        String pin = "";
        switch (getTxData().getFscode()) {
            case "WF": // 指靜脈提款查詢約定帳號
                if ("C9".equals(getTxData().getMsgType())) {
                    keyType = ENCKeyType.T3;
                    mode = "02";
                    pin = response.substring(152, 168);
                    break;
                } else {
                    return FEPReturnCode.Normal;
                }
            default:
                return FEPReturnCode.Normal;
        }
        RefString newPin = new RefString(null);
        rtnCode = ConvertPin(keyType, mode, accno, pin, newPin);
        if (rtnCode == FEPReturnCode.Normal) {
            // 將原ATM電文的PIN換成主機KEY壓的新PIN
            response.set(StringUtils.join(response.substring(0, 152),
                    newPin.get(), response.substring(168, response.get().length())));
        }

        return rtnCode;
    }

    /**
     * DESHelper轉換PIN
     *
     * @param cbsmac
     * @return
     */
    private FEPReturnCode CheckPinBlockConvert() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCKeyType keyType = ENCKeyType.None;
        String mode = ""; //00 for ANSI9.8 , 01 for 3624
        String accno = ""; //帳號
        String pin = "";
        switch (getTxData().getFscode()) {
            case "W2": // 無卡提款
                if (!getTxData().getTxRequestMessage().substring(310, 316).equals("F0F0F6")) {
                    keyType = ENCKeyType.T3;
                    mode = "03";
                    accno = getTxData().getTxRequestMessage().substring(162, 186);
                    accno = EbcdicConverter.fromHex(CCSID.English, accno);
                } else {
                    keyType = ENCKeyType.T3;
                    mode = "00";
                }
                break;
            case "FP": // 指靜脈密碼設定
                keyType = ENCKeyType.T3;
                mode = "00";
                break;
            case "FV":
                if ("AA".equals(getTxData().getMsgType())) {
                    keyType = ENCKeyType.T3;
                    mode = "00";
                    pin = getTxData().getTxRequestMessage().substring(214, 230);
                    break;
                } else {
                    return FEPReturnCode.Normal;
                }

            case "WF": // 指靜脈提款查詢約定帳號 FAA不驗， FC8 MODE=00
                if("C8".equals(getTxData().getMsgType())) {
                    keyType = ENCKeyType.T3;
                    mode = "00";
                    break;
                }else{
                    return FEPReturnCode.Normal;
                }
            case "P4":
                // 指靜脈舊密碼檢核(F-C4)
                // 指靜脈密碼變更
                keyType = ENCKeyType.T3;
                mode = "00";
                break;
            case "P1":
                // 磁條密碼變更
                // 磁條密碼變更,檢核舊密碼(F-C1)
                //晶片卡密碼變更不需轉換pinblock
//                String ascText = EbcdicConverter.fromHex(CCSID.English, getTxData().getTxRequestMessage().substring(110, 142));
                if ("P1".equals(getTxData().getFscode()) && !(getTxData().getTxRequestMessage().substring(358, 368).equals("D240F2F0F0"))) {
                    keyType = ENCKeyType.S1;
                    mode = "01";
                    accno = getTxData().getTxRequestMessage().substring(402, 426);
                    accno = EbcdicConverter.fromHex(CCSID.English, accno);
                    break;
                } else {
                    return FEPReturnCode.Normal;
                }
            case "SW": // 國際卡提款-銀聯卡
            case "PC":// 國際卡提款-PLUS
            case "VC":// 國際卡預借現金-VISA卡
            case "CC":// 國際卡提款-CIRRUS
            case "MC":// 國際卡預借現金-MASTETR卡
            case "SI":// 國際卡餘額查詢-銀聯卡
            case "PQ":// 國際卡餘額查詢-PLUS
            case "CQ":// 國際卡餘額查詢-CIRRUS
                keyType = ENCKeyType.T3;
                mode = "01";
                accno = getTxData().getTxRequestMessage().substring(290, 314);
                accno = EbcdicConverter.fromHex(CCSID.English, accno);
                break;
            case "JC":// 國際卡預借現金-JCB卡
                mode = "01";
                keyType = ENCKeyType.S1;
                accno = getTxData().getTxRequestMessage().substring(290, 314);
                accno = EbcdicConverter.fromHex(CCSID.English, accno);
                break;
            case "P5": // 國際卡變更密碼-ＡＴＭ預借現金舊密碼檢核
            case "P6":// 國際卡變更密碼-ＡＴＭ預借現金新密碼變更
                mode = "01";
                accno = getTxData().getTxRequestMessage().substring(290, 314);
                accno = EbcdicConverter.fromHex(CCSID.English, accno);
                //APPLUSE: 3-3DES, 空白: single DES
                if ("F3".equals(getTxData().getTxRequestMessage().substring(32, 34))) {
                    keyType = ENCKeyType.T3;
                    break;
                } else {
                    keyType = ENCKeyType.S1;
                    break;
                }
            default:
                return FEPReturnCode.Normal;
        }
        RefString newPin = new RefString(null);
        if (StringUtils.isEmpty(pin))
            pin = EbcdicConverter.fromHex(CCSID.English, getTxData().getTxRequestMessage().substring(110, 142));

        rtnCode = ConvertPin(keyType, mode, accno, pin, newPin);
        if (rtnCode == FEPReturnCode.Normal) {
            // 將原ATM電文的PIN換成主機KEY壓的新PIN
            if (getTxData().getFscode().equals("FV") && "AA".equals(getTxData().getMsgType())) {
                getTxData().setTxRequestMessage(getTxData().getTxRequestMessage().substring(0, 214) +
                        newPin
                        + getTxData().getTxRequestMessage().substring(230));
            } else {
                getTxData().setTxRequestMessage(getTxData().getTxRequestMessage().substring(0, 110) +
                        EbcdicConverter.toHex(CCSID.English, newPin.get().length(), newPin.get())
                        + getTxData().getTxRequestMessage().substring(142));
            }
        }

        return rtnCode;
    }

    private FEPReturnCode ConvertPin(ENCKeyType keyType, String mode, String accno, String pin, RefString newPin) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        // 1.建立ENCHelper物件
        ENCHelper encHelper = new ENCHelper(getTxData());
        String atmSeqNo = "";
        // String atmSno = StringUtils.leftPad(getATMBusiness().getAtmStr().getAtmSno(),
        // 8, ' ');
        //ATM交易序號XOR方式
        //ANSI方式 =>序號為 int格式不轉碼，直接^
        //single DES方式 => 轉EBC再^
        //3DES方式 =>轉EBC 再 ^
        if (mode.equals("01") || mode.equals("03")) {
            atmSeqNo = EbcdicConverter.toHex(CCSID.English, 8, StringUtils.rightPad(this.getTxData().getAtmSeq(), 8, '0'));

        } else {
            if (getTxData().getFscode().equals("FV") && "AA".equals(getTxData().getMsgType()) ||
                getTxData().getFscode().equals("WF") && "C9".equals(getTxData().getMsgType())) {
                atmSeqNo = getTxData().getTxRequestMessage().substring(64, 72) ;
                atmSeqNo += atmSeqNo;
            }
            else {
                atmSeqNo = this.getTxData().getAtmSeq();
            }
            
        }

        String atmNo = getTxData().getAtmNo();

        this.logContext.setRemark("Fscode=" + getTxData().getFscode() + ",keyType:" + keyType + ",pin:" + pin + ",accno=" + accno + ",atmSeqNo=" + atmSeqNo + ",mode=" + mode);
        logMessage(this.logContext);
        // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
        // String atmNo = getTxData().getAtmNo();

        rtnCode = encHelper.ConvertATMPinToIMSforP1(keyType, mode, atmNo, atmSeqNo, accno, pin, newPin);
        return rtnCode;
    }

    private String TrimSpace(String data) {
        String result = "";

        for (int i = 0; i < data.length(); i += 2) {

            String tmp = data.substring(i, i + 2);
            //System.out.println(tmp);
            if (i == 0) {
                result += tmp;
            } else {
                if (tmp.equals("40")) {
                    if (!result.substring(result.length() - 2, result.length()).equals("40")) {
                        result += tmp;
                    }
                } else {
                    result += tmp;
                }
            }
        }
        return result;
    }
    
	private void addTxData() throws Exception {
		try {
			// 新增交易記錄(FEPTxn) Returning FEPReturnCode
			/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".addTxData"));
			int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
			if (insertCount <= 0) { // 新增失敗
				_rtnCode = FEPReturnCode.FEPTXNInsertError;
			}
		} catch (Exception ex) { // 新增失敗
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
			_rtnCode = FEPReturnCode.FEPTXNInsertError;
		}
	}
	
	
	/**
	 * 更新 FEPTXN_AA_RC 值
	 */
	private void updateFeptxnAaRc() {
		feptxn.setFeptxnAaRc(_rtnCode.getValue());
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxnAaRc"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFeptxnAaRc");
			sendEMS(getLogContext());
		}
	}
}