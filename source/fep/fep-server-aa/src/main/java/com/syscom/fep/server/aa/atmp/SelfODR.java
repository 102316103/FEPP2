package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.mapper.InbkparmExtMapper;
import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Inbkparm;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.server.common.handler.ReserveHandler;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMCTxType;
import com.syscom.fep.vo.enums.ATMCashTxType;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.T24TxType;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.request.ChannelRequest;
import com.syscom.fep.vo.text.atm.response.DDRResponse;
import com.syscom.fep.vo.text.atm.response.ODEResponse;
import com.syscom.fep.vo.text.atm.response.ODRResponse;

/**
 * 負責處理 ATM 跨行存款交易電文
 * ODE: 查詢跨行存款手續費
 * ODR: 跨行存款交易
 * DDR: 現金捐款交易
 * 
 * @author Richard
 */
public class SelfODR extends ATMPAABase {
	private FEPReturnCode rtnCode = CommonReturnCode.Normal;
	private BigDecimal _wFEE_AMT = new BigDecimal(0);
	private ATMTXCD txCode; // ATM交易代號
	private String rtnMessage = StringUtils.EMPTY;
	private boolean needUpdateFEPTXN = false;

	/**
	 * AA的建構式,在這邊初始化及設定好相關變數
	 * 
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * @throws Exception
	 */
	public SelfODR(ATMData txnData) throws Exception {
		super(txnData);
		//--ben-20220922-//this.txCode = ATMTXCD.parse(this.getATMRequest().getTXCD());
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.準備FEP交易記錄檔
			this.rtnCode = this.getATMBusiness().prepareFEPTXN();
			// 2.新增FEP交易記錄檔
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.rtnCode = this.getATMBusiness().addTXData();
			}
			// 3.商業邏輯檢核
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.needUpdateFEPTXN = true;
				this.rtnCode = this.checkBusinessRule();
			}
			// 4. 帳務主機處理
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.rtnCode = this.sendToCBS();
			}
			// 5. 跨行交易處理(ODR)
			if (this.rtnCode == CommonReturnCode.Normal && ATMTXCD.ODR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				this.rtnCode = this.processODR();
			}
			// 6. 更新交易記錄(FEPTXN)
			this.updateFEPTxn();
		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
		} finally {
			// 7. 組回應電文(當海外主機有給TOTA或DES發生例外或主機逾時不組回應)
			if (StringUtils.isBlank(this.getTxData().getTxResponseMessage())) {
				this.rtnMessage = this.prepareATMResponseData();
			} else {
				this.rtnMessage = this.getTxData().getTxResponseMessage();
			}
			this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			this.getTxData().getLogContext().setMessage(this.rtnMessage);
			this.getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return this.rtnMessage;
	}

	/**
	 * 3. 商業邏輯檢核 相關程式
	 * 
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 3.1 CheckHeader
			rtnCode = this.getATMBusiness().checkHeader();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// 3.2 CheckBody
			rtnCode = this.getATMBusiness().checkBody();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			if (ATMTXCD.ODR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark(StringUtils.join("轉入行必須為他行, FEPTXN_TRIN_BKNO=", this.getATMBusiness().getFeptxn().getFeptxnTrinBkno()));
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
				if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnTrinActno())) {
					rtnCode = ATMReturnCode.TranInACTNOError;
					this.logContext.setRemark("轉入帳號不能為NULL或空白");
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
				// 取得跨行轉帳手續費
				Inbkparm inbkparm = new Inbkparm();
				inbkparm.setInbkparmApid("2521");
				inbkparm.setInbkparmPcode(StringUtils.EMPTY);
				inbkparm.setInbkparmAcqFlag("A");
				inbkparm.setInbkparmEffectDate(this.getFeptxn().getFeptxnTxDate());
				inbkparm.setInbkparmCur(this.getFeptxn().getFeptxnTxCur());
				InbkparmExtMapper inbkparmExtMapper = SpringBeanFactoryUtil.getBean(InbkparmExtMapper.class);
				Inbkparm record = inbkparmExtMapper.queryByPK(inbkparm);
				if (record == null) {
					this.logContext.setRemark(StringUtils.join(
							"INBKPARM 找不到跨行轉帳手續費,INBKPARM_APID=", inbkparm.getInbkparmApid(),
							" INBKPARM_PCODE=", inbkparm.getInbkparmPcode(),
							" INBKPARM_ACQ_FLAG=", inbkparm.getInbkparmAcqFlag(),
							" INBKPARM_EFFECT_DATE=", inbkparm.getInbkparmEffectDate(),
							" INBKPARM_CUR=", inbkparm.getInbkparmCur()));
					logMessage(Level.INFO, this.logContext);
					return ATMReturnCode.OtherCheckError;
				}
				inbkparm = record; // 這裡不要忘記賦值
				this.getATMBusiness().getFeptxn().setFeptxnActLoss(inbkparm.getInbkparmFeeCustpay());
				// 手續費收入=跨行存款手續(20)-跨行轉帳手續費(15)
				this.getATMBusiness().getFeptxn().setFeptxnAtmProfit(
						this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct().subtract(this.getATMBusiness().getFeptxn().getFeptxnActLoss()));
				this.logContext.setRemark(StringUtils.join(
						"手續費收入=", this.getATMBusiness().getFeptxn().getFeptxnAtmProfit().intValue(),
						" 跨行存款手續費=", this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct().intValue(),
						" 跨行轉帳手續費=", this.getATMBusiness().getFeptxn().getFeptxnActLoss()));
				logMessage(Level.INFO, this.logContext);
				// 交易金額必須大於手續費金額
				if (this.getATMBusiness().getFeptxn().getFeptxnTxAmt().compareTo(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpay()) < 0) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark(StringUtils.join(
							"交易金額必須大於手續費金額, FEPTXN_TX_AMT=", this.getATMBusiness().getFeptxn().getFeptxnTxAmt(),
							" FEPTXN_FEE_CUSTPAY=", this.getATMBusiness().getFeptxn().getFeptxnFeeCustpay()));
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
			}
			if (ATMTXCD.DDR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark(StringUtils.join("轉入行必須為本行, FEPTXN_TRIN_BKNO=", this.getATMBusiness().getFeptxn().getFeptxnTrinBkno()));
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
				if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnTrinActno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark("轉入帳號不能為NULL或空白");
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
			}
			// 3.3 本行卡片，檢核卡片狀態
			if ((ATMTXCD.ODR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.DDR.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode()))
					&& SysStatus.getPropertyValue().getSysstatHbkno().equals(this.getATMBusiness().getFeptxn().getFeptxnTroutBkno())) {
				rtnCode = this.getATMBusiness().checkCardStatus();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			// 3.4 更新 ATM 狀態
			rtnCode = this.getATMBusiness().updateATMStatus();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// 3.5 檢核單筆限額
			if (getTxData().getMsgCtl().getMsgctlCheckLimit() == (short) 1) {
				rtnCode = this.getATMBusiness().checkODRDLimit();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			// 3.6 檢核ATM電文訊息押碼(MAC)
			if (this.getTxData().getMsgCtl().getMsgctlReqmacType() != null && StringUtils.isNotBlank(this.getTxData().getMsgCtl().getMsgctlReqmacType().toString())) {
				ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn());
				//--ben-20220922-//rtnCode = encHelper.checkAtmMac(this.getATMRequest().getMAC());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			// 3.7 取得跨行存款手續費
			if (ATMTXCD.ODE.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				RefBase<BigDecimal> refFee = new RefBase<BigDecimal>(this._wFEE_AMT);
				rtnCode = this.getATMBusiness().getODRFEE(refFee);
				this._wFEE_AMT = refFee.get();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			return rtnCode;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, "checkBusinessRule"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 4. 帳務主機處理
	 * 
	 * @return
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = new T24(this.getTxData());
		try {
			if (StringUtils.isNotBlank(this.getTxData().getMsgCtl().getMsgctlTwcbstxid())) {
				if (ATMTXCD.ODR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), true);
				} else {
					rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), false);
				}
				if (rtnCode == CommonReturnCode.Normal) {
					// 寫入 ATM 清算資料
					if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
						rtnCode = this.getATMBusiness().insertATMC(ATMCTxType.Accounting.getValue());
					}
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
			}
			return rtnCode;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, "sendToCBS"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 5. 跨行交易處理
	 * 
	 * @return
	 */
	private FEPReturnCode processODR() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = null;
		ATMGeneral tita = new ATMGeneral();
		ATMGeneral tota = new ATMGeneral();
		ReserveHandler handler = new ReserveHandler();
		String sEj = StringUtils.EMPTY;
		Sysconf sysconf = new Sysconf();
		SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
		try {
			// 1. 組ATM(ODT2521)上行電文
			sEj = StringUtils.leftPad(String.valueOf(this.getATMBusiness().getFeptxn().getFeptxnEjfno()), 8, '0');
			ATMGeneralRequest request = tita.getRequest();
			//--ben-20220922-//request.setAtmseq_1(StringUtils.leftPad(CalendarUtil.adStringToROCString(this.getATMBusiness().getFeptxn().getFeptxnTxDate()), 8, '0'));
			//--ben-20220922-//request.setAtmseq_2(sEj.substring(sEj.length() - 8, sEj.length()));
			//--ben-20220922-//request.setBRNO(this.getATMBusiness().getFeptxn().getFeptxnAtmno().substring(0, 3));
			//--ben-20220922-//request.setWSNO(this.getATMBusiness().getFeptxn().getFeptxnAtmno().substring(3, 5));
			//--ben-20220922-//request.setTXCD(ATMTXCD.ODT.name());
			//--ben-20220922-//request.setATMCHK(this.getATMBusiness().getFeptxn().getFeptxnAtmChk());
			//--ben-20220922-//request.setMODE(String.valueOf(this.getATMBusiness().getFeptxn().getFeptxnTxnmode()));
			//--ben-20220922-//request.setBKNO(SysStatus.getPropertyValue().getSysstatHbkno()); // 轉出行
			//--ben-20220922-//request.setTXACT(INBKConfig.getInstance().getODRTroutActNo()); // 轉出帳號(跨行存款虛擬帳號)
			//--ben-20220922-//request.setBknoD(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno()); // 轉入行
			//--ben-20220922-//request.setActD(this.getATMBusiness().getFeptxn().getFeptxnTrinActno()); // 轉入帳號
			// 2017/11/23 Modify by Ruling for 跨行存款優化:判斷跨行存款優化生效日後發卡行/帳號放入IC卡備註欄
			// 晶片卡 REMARK 欄位
			sysconf.setSysconfSubsysno((short) 1);
			sysconf.setSysconfName("IBDEffectDate");
			Sysconf sysconfResult = sysconfMapper.selectByPrimaryKey(sysconf.getSysconfSubsysno(), sysconf.getSysconfName());
			if (sysconfResult != null) {
				if (this.getATMBusiness().getFeptxn().getFeptxnTxDate().compareTo(sysconfResult.getSysconfValue()) >= 0) {
					// 生效日後發卡行/帳號放入IC卡備註欄
					this.logContext.setRemark("晶片金融卡之發卡單位代號及帳號於入IC卡備註欄");
					logMessage(Level.INFO, this.logContext);
					//--ben-20220922-//request.setICMARK(StringUtils.rightPad(StringUtils.join(
					//--ben-20220922-//		"0", this.getATMBusiness().getFeptxn().getFeptxnTroutBkno().trim(), this.getATMBusiness().getFeptxn().getFeptxnMajorActno().trim()), 60, "0"));
				} else {
					// 生效日前
					if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.getATMBusiness().getFeptxn().getFeptxnTroutBkno())) {
						//--ben-20220922-//request.setICMARK(StringUtil.toHex(this.getATMBusiness().getFeptxn().getFeptxnIcmark())); // 轉成HEX
					}
				}
			} else {
				this.logContext.setRemark(StringUtils.join(
						"讀不到跨行存款優化生效日(SYSCONF), SYSCONF_SUBSYSNO=", sysconf.getSysconfSubsysno(), " SYSCONF_NAME=", sysconf.getSysconfName()));
				logMessage(Level.INFO, this.logContext);
				rtnCode = ATMReturnCode.OtherCheckError;
				return rtnCode;
			}
			//--ben-20220922-//request.setICTXSEQ(this.getFeptxn().getFeptxnIcSeqno());
			//--ben-20220922-//request.setICTAC(StringUtils.EMPTY);
			//--ben-20220922-//request.setEXPCD("0000");
			//--ben-20220922-//request.setTXAMT(this.getATMBusiness().getFeptxn().getFeptxnTxAmt().subtract(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct())); // 交易金額 - 手續費
			//--ben-20220922-//request.setCLASS(this.getATMBusiness().getFeptxn().getFeptxnPaytype());
			//--ben-20220922-//request.setUNIT(this.getATMBusiness().getFeptxn().getFeptxnTaxUnit());
			//--ben-20220922-//request.setIDNO(this.getATMBusiness().getFeptxn().getFeptxnIdno());
			//--ben-20220922-//request.setDUEDATE(StringUtils.leftPad(CalendarUtil.adStringToROCString(this.getATMBusiness().getFeptxn().getFeptxnDueDate()), 8, '0'));

			if (MathUtil.compareTo(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct(), 15) >= 0) {
				//--ben-20220922-//request.setCHARGE(this.getATMBusiness().getFeptxn().getFeptxnActLoss());
			} else {
				//--ben-20220922-//request.setCHARGE(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct());
			}

			//--ben-20220922-//request.setPAYCNO(this.getATMBusiness().getFeptxn().getFeptxnReconSeqno());
			//--ben-20220922-//request.setVPID(this.getATMBusiness().getFeptxn().getFeptxnBusinessUnit());
			//--ben-20220922-//request.setPAYID(this.getATMBusiness().getFeptxn().getFeptxnPayno());
			//--ben-20220922-//request.setMENO("6011");
			//--ben-20220922-//request.setChlEJNo(this.getATMBusiness().getFeptxn().getFeptxnEjfno().toString());
			//--ben-20220922-//request.setCHLCODE(this.getATMBusiness().getFeptxn().getFeptxnChannel());
			//--ben-20220922-//request.setPsbmemoD("ATM轉帳"); // 存摺摘要(借方)
			//--ben-20220922-//request.setPsbmemoC(StringUtils.EMPTY); // 存摺摘要(貸方)
			//--ben-20220922-//request.setPsbremSD(StringUtils.join(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno(), this.getATMBusiness().getFeptxn().getFeptxnTrinActno().substring(7, 16))); // 存摺備註(借方)
			//--ben-20220922-//request.setPsbremSC(StringUtils.EMPTY); // 存摺備註(貸方)
			//--ben-20220922-//request.setPsbremFD(StringUtils.join(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno(), this.getATMBusiness().getFeptxn().getFeptxnTrinActno())); // 往來明細(借方)
			//--ben-20220922-//request.setPsbremFC(StringUtils.EMPTY); // 往來明細(貸方)
			//--ben-20220922-//request.setRegTfrType(StringUtils.EMPTY);

			// 2017/05/26 Modify by Ruling for 跨行存款交易(ODT2521)，送財金電文轉出銀行代號(BITMAP 14)放分行代號
			if (this.getATMBusiness().getFeptxn().getFeptxnTroutBkno().equals(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno())
					&& this.getATMBusiness().getFeptxn().getFeptxnMajorActno().equals(this.getATMBusiness().getFeptxn().getFeptxnTrinActno())) {
				//--ben-20220922-//request.setBranchID("9999");
			} else {
				//--ben-20220922-//request.setBranchID("9998");
			}
			//--ben-20220922-//this.logContext.setRemark(StringUtils.join("BranchID=", request.getBranchID()));
			logMessage(Level.INFO, this.logContext);

			//--ben-20220922-//request.setAtmverN("A");

			// 2021-07-20 Richard add start
			// 這裡不加上這段代碼, 下面的tita==null的判斷永遠都進不去
			try {
				ChannelRequest textObj = new ChannelRequest();
				textObj.makeMessageFromGeneral(tita);
			} catch (Exception e) {
				tita = null;
			}
			// 2021-07-20 Richard add end

			if (tita == null) {
				this.logContext.setRemark("ODT2521電文為Nothing");
				logMessage(Level.INFO, this.logContext);
				rtnCode = CommonReturnCode.ProgramException;
				return rtnCode;
			} else {
				// 2. 透過ReserveHandler呼叫AA
				tota = handler.dispatch(FEPChannel.ATM, tita);
				if (tota == null) {
					this.logContext.setRemark("ReserveHandler回應電文為空");
					logMessage(Level.INFO, this.logContext);
					rtnCode = CommonReturnCode.ProgramException;
					return rtnCode;
				}

				// 3. 收到ATM(ODT2521)下行電文，更新FEPTXN
				//ben20221118  this.getATMBusiness().getFeptxn().setFeptxnReplyCode(tota.getResponse().getREJCD()); // 將ODT2521回來的結果再回塞給ODR的Response
				this.getATMBusiness().getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());
				this.getATMBusiness().getFeptxn().setFeptxnRemark(tota.getResponse().getSTAN().trim()); // 跨行轉帳STAN
				//ben20221118  this.getATMBusiness().getFeptxn().setFeptxnChannelEjfno(tota.getResponse().getFepEjno());

				//ben20221118  if (this.getATMBusiness().checkEXPCD(tota.getResponse().getREJCD())) {
				if (this.getATMBusiness().checkEXPCD("")) {
					// 交易成功
					// 更新 ATM 鈔匣資料
					if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
						rtnCode = this.getATMBusiness().updateATMCash(ATMCashTxType.Accounting.getValue());
					}

					// 更新限額資料
					if (this.getTxData().getMsgCtl().getMsgctlCheckLimit() == 1) {
						rtnCode = this.getATMBusiness().insertODRC(1);
					}
				} else {
					// 交易失敗
					// 組T24主機電文沖正 (A2930)
					hostT24 = new T24(this.getTxData());

					rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.EC.getValue(), true);
					if (rtnCode != CommonReturnCode.Normal) {
						this.logContext.setRemark(StringUtils.join("組和送T24主機沖正失敗!!, rtnCode=", rtnCode));
						logMessage(Level.INFO, this.logContext);
					}

					// 寫入 ATM 清算資料
					if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
						rtnCode = this.getATMBusiness().insertATMC(ATMCTxType.EC.getValue());
						if (rtnCode != CommonReturnCode.Normal) {
							this.logContext.setRemark(StringUtils.join("回沖 ATM 清算資料(ATMC)失敗, rtnCode=", rtnCode));
							logMessage(Level.INFO, this.logContext);
						}
					}
				}
			}
			return rtnCode;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 6. 更新交易記錄檔
	 */
	private void updateFEPTxn() {
		this.getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
		this.getATMBusiness().getFeptxn().setFeptxnAaRc(this.rtnCode.getValue());

		if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnReplyCode())) {
			if (this.rtnCode == FEPReturnCode.Normal) {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);
			} else {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode(
						TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()), FEPChannel.FEP, this.getTxData().getTxChannel(), this.getTxData().getLogContext()));
			}
		}

		this.getATMBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		if (this.rtnCode == CommonReturnCode.Normal && this.getATMBusiness().checkEXPCD(this.getATMBusiness().getFeptxn().getFeptxnReplyCode())) {
			if (this.getATMBusiness().getFeptxn().getFeptxnWay() == 3) {
				this.getATMBusiness().getFeptxn().setFeptxnTxrust("B"); // 處理結果=Pending
			} else {
				this.getATMBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
			}
		} else {
			this.getATMBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
		}

		if (this.needUpdateFEPTXN) {
			FEPReturnCode rtncode = CommonReturnCode.Normal;
			rtncode = this.getATMBusiness().updateTxData();
			if (rtncode != CommonReturnCode.Normal) {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode("L013"); // 回寫檔案(FEPTxn)發生錯誤
			}
		}
	}

	/**
	 * 7. 組回應電文 相關程式
	 * 
	 * 組ATM回應電文,Response物件的值已經在AA中MapGeneralResponseFromGeneralRequest搬好Header的值了
	 * 這裏只要處理Response的body的欄位值即可
	 * 
	 * @return
	 */
	private String prepareATMResponseData() {
		String atmResponseString = StringUtils.EMPTY;
		try {
			this.getATMBusiness().mapResponseFromRequest();
			this.loadCommonATMResponse();
			switch (this.txCode) {
				case ODE: {
					//ben20221118  this.getATMResponse().setACFEE(this._wFEE_AMT);
					ODEResponse atmRsp = new ODEResponse();
					atmResponseString = atmRsp.makeMessageFromGeneral(this.getTxData().getTxObject());
					break;
				}
				case ODR: {
					ODRResponse atmRsp = new ODRResponse();
					atmResponseString = atmRsp.makeMessageFromGeneral(this.getTxData().getTxObject());
					break;
				}
				case DDR: {
					DDRResponse atmRsp = new DDRResponse();
					atmResponseString = atmRsp.makeMessageFromGeneral(this.getTxData().getTxObject());
					break;
				}
				default:
					break;
			}
			return atmResponseString;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
	}

	/**
	 * 組ATM回應電文(共同)
	 */
	private void loadCommonATMResponse() {
		ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn(), this.getTxData());
		RefString desMACData = new RefString(StringUtils.EMPTY);
		// 2010-06-15 by kyo 新增一個rtncode變數 來承接MAKEMAC是否成功，避免前面有檢核錯誤，但是被MAKEMAC給蓋回NORMAL
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		//ben20221118  this.getATMResponse().setREJCD(this.getATMBusiness().getFeptxn().getFeptxnReplyCode());
		//ben20221118  this.getATMResponse().setTXSEQ(this.getATMBusiness().getFeptxn().getFeptxnTxseq());
		//--ben-20220922-//this.getATMResponse().setYYMMDD(this.getATMRequest().getYYMMDD()); // 壓碼日期

		// 跨行轉帳STAN
		if (StringUtils.isNotBlank(this.getATMBusiness().getFeptxn().getFeptxnStan())) {
			this.getATMResponse().setSTAN(this.getATMBusiness().getFeptxn().getFeptxnStan());
		}

		// 手續費
		//ben20221118  this.getATMResponse().setHC(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct());

		// 產生ATM電文MAC資料
		if (this.getTxData().getMsgCtl().getMsgctlRepmacType() != null) {
			rtnCode = encHelper.makeAtmMac("", desMACData);
			if (rtnCode != CommonReturnCode.Normal) {
				//ben20221118  this.getATMResponse().setMAC(StringUtils.EMPTY);
			} else {
				//ben20221118  this.getATMResponse().setMAC(desMACData.get()); // 壓碼訊息
			}

		}

		// BugReport(001B0097):加上絕對值避開金額欄位
		//ben20221118  this.getATMResponse().setBal11S(MathUtil.compareTo(this.getATMBusiness().getFeptxn().getFeptxnBala(), 0) >= 0 ? "+" : "-");
		//ben20221118  this.getATMResponse().setBAL11(this.getATMBusiness().getFeptxn().getFeptxnBala().abs());

		//ben20221118  this.getATMResponse().setBal12S(MathUtil.compareTo(this.getATMBusiness().getFeptxn().getFeptxnBalb(), 0) >= 0 ? "+" : "-");
		//ben20221118  this.getATMResponse().setBAL12(this.getATMBusiness().getFeptxn().getFeptxnBalb().abs());

		//--ben-20220922-//this.getATMResponse().setBKNO(this.getATMRequest().getBKNO());
		//--ben-20220922-//this.getATMResponse().setCHACT(this.getATMRequest().getCHACT());
		//--ben-20220922-//this.getATMResponse().setTXACT(this.getATMRequest().getTXACT());
		//--ben-20220922-//this.getATMResponse().setTXAMT(this.getATMRequest().getTXAMT());
		//--ben-20220922-//this.getATMResponse().setTRACK3(this.getATMRequest().getTRACK3());
		//ben20221118  this.getATMResponse().setBalCur(this.getFeptxn().getFeptxnTxCurAct()); // 主帳戶幣別
	}
}
