package com.syscom.fep.server.common.cbsprocess;

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.IB_LE_I002;
import com.syscom.fep.vo.text.ims.IB_LE_O002;

/**
 * 組送CBS 主機原存約定及核驗Confirm交易電文
 *
 * @author Joseph
 */

public class IBLEI002 extends ACBSAction {

    public IBLEI002(MessageBase txData) {
        super(txData, new IB_LE_I002());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
    private NpsunitMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
    Vatxn vatxn = this.getFiscData().getVatxn();

    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        //組CBS 原存交易Request電文, 電文內容格式請參照: D_I7_合庫FEP_主機電文規格-原存行約定及核驗交易V1.0(111xxxx).doc
        IB_LE_I002 cbsTita = new IB_LE_I002();
        // HEADER
        cbsTita.setIMS_TRANS("MFEPFG00"); // 主機業務別 長度8
        cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime()); // FEP交易日期時間 長度14
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("02");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給02 */
        cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
        cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
        cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
        cbsTita.setPROCESS_TYPE("RVS"); /*沖正*/
        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno()); // 設備代理行 長度3
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan()); // 跨行交易序號 長度7
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());// 端末機代號 長度8
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType()); // 端末設備型態 長度4
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
        if (StringUtils.isNotBlank(feptxn.getFeptxnIcmark())) {
            cbsTita.setCARDTYPE("K"); /* 晶片卡 */
        } else {
            cbsTita.setCARDTYPE("X"); /* 未使用卡片 */
        }
        cbsTita.setRESPONSE_CODE(feptxn.getFeptxnConRc()); // 回應代號(RC) 長度4
        cbsTita.setHRVS(StringUtils.repeat(" ", 33)); // 保留欄位 長度33
        /* CBS Request DETAIL */
        cbsTita.setICCHIPSTAN(StringUtils.repeat("0", 8)); // IC卡交易序號
        cbsTita.setTERM_CHECKNO(StringUtils.repeat(" ", 8)); // 端末設備查核碼
        cbsTita.setTERMTXN_DATETIME(StringUtils.repeat("0", 14)); // 交易日期時間
        cbsTita.setICMEMO(StringUtils.repeat(" ", 30)); // IC卡備註欄
        cbsTita.setTXNICCTAC(StringUtils.repeat(" ", 10)); // 交易驗證碼
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號(無卡提款序號)
        cbsTita.setLE_CHARGCUS(feptxn.getFeptxnFeeCustpayAct().toString()); // 跨行手續費
        cbsTita.setLE_2566_TYPE(vatxn.getVatxnType()); // 業務類別
        cbsTita.setLE_AEIPYTP(vatxn.getVatxnType()); // 交易類別
        cbsTita.setDRVS(StringUtils.repeat(" ", 372)); // 保留欄位
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS TOTA電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 電文內容格式請參照TOTA電文格式(IB_LE_O002) */
        /* 拆解主機回應電文 */
        IB_LE_O002 tota = new IB_LE_O002();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新FEPTXN */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新FEPTXN
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(IB_LE_O002 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
        feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
        feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 帳戶手續費 */
        feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE()); /* 提領幣別手續費 */
        feptxn.setFeptxnBalb(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */

        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        if (!cbsTota.getIMSRC_TCB().equals("000")) {
            feptxn.setFeptxnAccType((short) 3); /*更正/轉入失敗*/

            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            /* CBS回覆成功 */
            if ("Y".equals(cbsTota.getIMSRVS_FLAG())) { // 主機記帳狀況
                feptxn.setFeptxnAccType((short) 2);  //已更正
            } else if ("N".equals(cbsTota.getIMSRVS_FLAG())) {
                feptxn.setFeptxnAccType((short) 3); /*更正/轉入失敗*/
            }
            rtnCode = FEPReturnCode.Normal;
        }
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        return rtnCode;
    }
}
