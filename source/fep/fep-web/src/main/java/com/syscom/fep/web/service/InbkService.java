package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InbkService extends BaseService {
    private static final String ProgramName = InbkService.class.getSimpleName();

    @Autowired
    private AllbankExtMapper allbankExtMapper;
    @Autowired
    private BsdaysMapper bsdaysMapper;
    @Autowired
    private FwdtxnExtMapper fwdtxnExtMapper;
    @Autowired
    private SeqnoMapper seqnoMapper;
    @Autowired
    private FundlogExtMapper fundlogExtMapper;
    @Autowired
    private ClrdtlExtMapper clrdtlExtMapper;
    @Autowired
    private BatchExtMapper batchExtMapper;
    @Autowired
    private AtmmstrMapper atmmstrMapper;
    @Autowired
    private FwdrstMapper fwdrstMapper;
    @Autowired
    private ClrtotalMapper clrtotalMapper;
    @Autowired
    private FeptxnExtMapper feptxnExtMapper;
    @Autowired
    private AptotExtMapper aptotExtMapper;
    @Autowired
    private IctltxnExtMapper ictltxnExtMapper;
    @Autowired
    private BrapExtMapper brapExtMapper;
    @Autowired
    private HkbrapExtMapper hkbrapExtMapper;
    @Autowired
    private MobrapExtMapper mobrapExtMapper;
    @Autowired
    private ZoneExtMapper zoneExtMapper;
    @Autowired
    private FcrmstatExtMapper fcrmstatExtMapper;

    @Autowired
    private NpsbatchExtMapper npsbatchExtMapper;
    @Autowired
    private NpsdtlExtMapper npsdtlExtMapper;
    @Autowired
    private ApibatchExtMapper apibatchExtMapper;
    @Autowired
    private ApidtlExtMapper apidtlExtMapper;
    @Autowired
    private FeptxnDao feptxnDao;
    @Autowired
    private SysstatExtMapper sysstatExtMapper;
    @Autowired
    private InbkparmExtMapper inbkparmExtMapper;
    @Autowired
    private BinExtMapper binExtMapper;
    @Autowired
    private BinMapper binMapper;
    @Autowired
    private InbkparmMapper inbkparmMapper;
    @Autowired
    private CbspendExtMapper cbspendExtMapper;

    @Autowired
    private ObtltxnExtMapper obtltxnExtMapper;

    @Override
    protected SubSystem getSubSystem() {
        return SubSystem.INBK;
    }

    public BigDecimal getCbspendSummary(String cbspendTxDate, Short cbspendSuccessFlag, Short cbspendSubsys, String cbspendZone,
                                        String cbspendCbsTxCode) throws Exception {
        try {
            return cbspendExtMapper.getsumOfTxAMT(cbspendTxDate, cbspendSuccessFlag, cbspendSubsys, cbspendZone, cbspendCbsTxCode);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int UpdateCBSPEND(Cbspend cbspend) throws Exception {
        int iRes = 0;
        try {
            iRes = cbspendExtMapper.updateByPrimaryKey(cbspend);
            return iRes;
        } catch (Exception e) {
            sendEMS(e);
            return iRes;
        }
    }

    public int UpdateResendCNT(String txdate, BigDecimal ejfno, String zone, String tbsdy, String subsys) throws Exception {
        try {
            int iRes = 0;
            iRes = cbspendExtMapper.UpdateResendCNT(txdate, ejfno, zone, tbsdy, subsys);
            return iRes;
        } catch (Exception e) {
            sendEMS(e);
            return 0;
        }
    }

    public List<Map<String, Object>> SelectResendCNT(String txdate, String zone, String tbsdy, String subsys)
            throws Exception {
        try {
            return cbspendExtMapper.QueryResendCNT(txdate, zone, tbsdy, subsys);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Map<String, Object>> GetCBSPENDByTXDATE(String cbspendTxDate, Short cbspendSuccessFlag, Short cbspendSubsys, String cbspendZone,
                                                        String cbspendCbsTxCode) {
        if (StringUtils.isBlank(cbspendTxDate)) {
            return cbspendExtMapper.queryAllData();
        } else {
            cbspendTxDate = StringUtils.replace(cbspendTxDate, "-", StringUtils.EMPTY);
            return cbspendExtMapper.getCBSPENDByTXDATEAndZone(cbspendTxDate, cbspendSuccessFlag, cbspendSubsys, cbspendZone, cbspendCbsTxCode);
        }
    }

    public List<Inbkparm> getINBKPARMByPK(String APID, String a, String INBKPARM_CUR, String INBKPARM_EFFECT_DATE,
                                          BigDecimal INBKPARM_RANGE_FROM) {

        Inbkparm definbkparm = new Inbkparm();
        definbkparm.setInbkparmApid(APID);
        definbkparm.setInbkparmAcqFlag(a);
        definbkparm.setInbkparmCur(INBKPARM_CUR);
        if (StringUtils.isNotBlank(INBKPARM_EFFECT_DATE)) {
            INBKPARM_EFFECT_DATE = StringUtils.replace(INBKPARM_EFFECT_DATE, "-", StringUtils.EMPTY);
            definbkparm.setInbkparmEffectDate(INBKPARM_EFFECT_DATE);
        }
        definbkparm.setInbkparmRangeFrom(INBKPARM_RANGE_FROM);

        return inbkparmExtMapper.getINBKPARMByPK(definbkparm);
    }

    public List<Bin> getBinByPK(String BINNO, String BINBKNO) {

        return binExtMapper.getBinByPrimaryKey(BINNO, BINBKNO);
    }

    public List<Inbkparm> getInbkparmAll() {
        return inbkparmExtMapper.queryInbkparmAll();
    }

    public List<Bin> getBinAll() {
        return binExtMapper.queryBinAll();
    }

    public Integer updateINBKPARM(Inbkparm inbkparm) {
        try {
            // 回傳的為TaskId
            return inbkparmExtMapper.updateByPrimaryKey(inbkparm);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer updateBIN(Bin bin) {
        try {
            // 回傳的為TaskId
            return binExtMapper.updateByPrimaryKey(bin);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer insertINBKPARM(Inbkparm inbkparm) {
        try {
            return inbkparmMapper.insertSelective(inbkparm);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer insertBIN(Bin bin) {
        try {
            return binMapper.insertSelective(bin);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Inbkparm getInbkparmByPK(String APID, String a, String INBKPARM_CUR, String INBKPARM_EFFECT_DATE,
                                    BigDecimal INBKPARM_RANGE_FROM, String pcode) {
        return inbkparmMapper.selectByPrimaryKey(APID, pcode, a, INBKPARM_EFFECT_DATE, INBKPARM_CUR, INBKPARM_RANGE_FROM);
    }

    public Bin getBinDataByPK(String binno, String binbkno) {
        return binMapper.selectByPrimaryKey(binno, binbkno);
    }

    public Integer deleteINBKPARM(Inbkparm inbkparm) {
        try {
            // 回傳的為TaskId
            return inbkparmExtMapper.deleteByPrimaryKey(inbkparm);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer deleteBin(String binno, String binbkno, int userid) {
        try {
            BinExt bin = new BinExt();
            bin.setBinNo(binno);
            bin.setBinBkno(binbkno);
            bin.setUpdateUserid(userid);
            // 回傳的為TaskId
            return binExtMapper.deleteByPrimaryKey(bin);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public int UpdateFCRMSTAT(Fcrmstat fcrmstat) throws Exception {
        int iRes = 0;
        try {
            // 參照.NET要求, 以下三行要加入, 因為要記錄audit trail log
            fcrmstat.setLogAuditTrail(true);
            fcrmstat.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            fcrmstat.setUpdateUser(fcrmstat.getUpdateUserid());
            iRes = fcrmstatExtMapper.updateByPrimaryKeySelective(fcrmstat);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    public int UpdateSYSSTAT(Sysstat sysstat) throws Exception {
        int iRes = 0;
        try {
            // 參照.NET要求, 以下三行要加入, 因為要記錄audit trail log
            sysstat.setLogAuditTrail(true);
            sysstat.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            sysstat.setUpdateUser(sysstat.getUpdateUserid());
            iRes = sysstatExtMapper.updateByHbkno(
                    sysstat.getSysstatHbkno(),
// 2024-03-06 Richard modified for SYSSTATE 調整
//                    sysstat.getSysstatIntra(),
//                    sysstat.getSysstatAgent(),
//                    sysstat.getSysstatIssue(),
//                    sysstat.getSysstatIwdI(),
//                    sysstat.getSysstatIwdA(),
//                    sysstat.getSysstatIwdF(),
//                    sysstat.getSysstatIftI(),
//                    sysstat.getSysstatIftA(),
//                    sysstat.getSysstatIftF(),
//                    sysstat.getSysstatIpyI(),
//                    sysstat.getSysstatIpyA(),
//                    sysstat.getSysstatIpyF(),
//                    sysstat.getSysstatCdpF(),
//                    sysstat.getSysstatIccdpI(),
//                    sysstat.getSysstatIccdpA(),
//                    sysstat.getSysstatIccdpF(),
//                    sysstat.getSysstatEtxI(),
//                    sysstat.getSysstatEtxA(),
//                    sysstat.getSysstatEtxF(),
//                    sysstat.getSysstat2525A(),
//                    sysstat.getSysstat2525F(),
//                    sysstat.getSysstatCpuA(),
//                    sysstat.getSysstatCpuF(),
//                    sysstat.getSysstatCauA(),
//                    sysstat.getSysstatCauF(),
//                    sysstat.getSysstatCwvA(),
//                    sysstat.getSysstatCwmA(),
//                    sysstat.getSysstatGpcwdF(),
//                    sysstat.getSysstatCaI(),
//                    sysstat.getSysstatCafA(),
//                    sysstat.getSysstatCavA(),
//                    sysstat.getSysstatCamA(),
//                    sysstat.getSysstatGpcadF(),
//                    sysstat.getSysstatCaaI(),
//                    sysstat.getSysstatFwdI(),
//                    sysstat.getSysstatAdmI(),
//                    sysstat.getSysstatAig(),
//                    sysstat.getSysstatHkIssue(),
//                    sysstat.getSysstatHkFiscmb(),
//                    sysstat.getSysstatHkPlus(),
//                    sysstat.getSysstatMoIssue(),
//                    sysstat.getSysstatMoFiscmb(),
//                    sysstat.getSysstatMoPlus(),
                    sysstat.getSysstatT24Twn()
// 2024-03-06 Richard modified for SYSSTATE 調整
//                    sysstat.getSysstatT24Hkg(),
//                    sysstat.getSysstatT24Mac(),
//                    sysstat.getSysstatHkFiscmq(),
//                    sysstat.getSysstatMoFiscmq(),
//                    sysstat.getSysstatFawA(),
//                    sysstat.getSysstatEafA(),
//                    sysstat.getSysstatEavA(),
//                    sysstat.getSysstatEamA(),
//                    sysstat.getSysstatEwvA(),
//                    sysstat.getSysstatEwmA(),
//                    sysstat.getSysstatGpemvF(),
//                    sysstat.getSysstatPure(),
//                    sysstat.getSysstatIiqI(),
//                    sysstat.getSysstatIiqF(),
//                    sysstat.getSysstatIiqP(),
//                    sysstat.getSysstatIftP(),
//                    sysstat.getSysstatIccdpP(),
//                    sysstat.getSysstatIpyP(),
//                    sysstat.getSysstatCdpA(),
//                    sysstat.getSysstatCdpP(),
//                    sysstat.getSysstatIqvP(),
//                    sysstat.getSysstatIqmP(),
//                    sysstat.getSysstatIqcP(),
//                    sysstat.getSysstatEqpP(),
//                    sysstat.getSysstatEqcP(),
//                    sysstat.getSysstatEquP(),
//                    sysstat.getSysstatAdmA(),
//                    sysstat.getSysstatNwdI(),
//                    sysstat.getSysstatNwdA(),
//                    sysstat.getSysstatGpiwdF(),
//                    sysstat.getSysstatGpobF(),
//                    sysstat.getSysstatNfwI(),
//                    sysstat.getSysstatVaaF(),
//                    sysstat.getSysstatVaaA()
            );
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    public int lockFwdtxn(Fwdtxn def) throws Exception {
        try {
            return fwdtxnExtMapper.lockFWDTXN(def);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getFwdtxn(Fwdtxn def, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    fwdtxnExtMapper.getFwdtxn(def);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public HashMap<String, Object> getFailTimes(Fwdtxn def) throws Exception {
        try {
            return fwdtxnExtMapper.getFailTimes(def);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Fundlog getFundlogByFgSeqno(String fundlogFgSeqno) throws Exception {
        try {
            return fundlogExtMapper.getFUNDLOGByFGSeqno(fundlogFgSeqno);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int insertFundlog(Fundlog fundlog) throws Exception {
        try {
            return fundlogExtMapper.insertSelective(fundlog);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateFundlog(Fundlog fundlog) throws Exception {
        try {
            return fundlogExtMapper.updateByPrimaryKeySelective(fundlog);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int deleteFundlog(Fundlog fundlog) throws Exception {
        try {
            return fundlogExtMapper.deleteByPrimaryKey(fundlog);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateClrdtlByPK(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.updateByPrimaryKeySelective(clrdtl);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int insertClrdtl(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.insertSelective(clrdtl);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Clrdtl getClrdtlByPrimaryKey(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.selectByPrimaryKey(clrdtl.getClrdtlTxdate(), clrdtl.getClrdtlApId(), clrdtl.getClrdtlPaytype());
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public String getSEQNOByPK() throws Exception {
        try {
            String seqnoName = "FGTWD";
            Seqno seqno = seqnoMapper.selectByPrimaryKey(seqnoName);
            if (seqno != null) {
                seqno.setSeqnoName("FGTWD");
                seqno.setSeqnoNextid(seqno.getSeqnoNextid() + 1);
                seqnoMapper.updateByPrimaryKeySelective(seqno);
                return String.valueOf(seqno.getSeqnoNextid() - 1);
            } else {
                seqno = new Seqno();
                seqno.setSeqnoName("FGTWD");
                seqno.setSeqnoNextid(2);
                seqnoMapper.insertSelective(seqno);
                return "1";
            }
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Bsdays getBsdaysByPk(String bsdaysZoneCode, String bsdaysDate) throws Exception {
        try {
            return bsdaysMapper.selectByPrimaryKey(bsdaysZoneCode, bsdaysDate);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Batch getSingleBATCHByDef(String atchName) throws Exception {
        try {
            return batchExtMapper.getSingleBATCHByDef(atchName);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Fwdtxn getFwdtxn(String fwdtxnTxDate, String fwdtxnTxId) throws Exception {
        try {
            return fwdtxnExtMapper.selectByPrimaryKey(fwdtxnTxDate, fwdtxnTxId);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Atmmstr getAtmmstr(String atmNo) throws Exception {
        try {
            return atmmstrMapper.selectByPrimaryKey(atmNo);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateFwdtxnByPrimaryKey(Fwdtxn fwdtxn) throws Exception {
        try {
            return fwdtxnExtMapper.updateByPrimaryKeySelective(fwdtxn);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int insertFwdrst(Fwdrst fwdrst) throws Exception {
        try {
            return fwdrstMapper.insertSelective(fwdrst);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Fwdrst getFwdrst(String fwdrstTxDate, String fwdrstTxId, Short fwdrstRunNo) throws Exception {
        try {
            return fwdrstMapper.selectByPrimaryKey(fwdrstTxDate, fwdrstTxId, fwdrstRunNo);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Clrtotal getClrtotal(String clrtotalStDate, String clrtotalCur, Short clrtotalSource) throws Exception {
        try {
            return clrtotalMapper.selectByPrimaryKey(clrtotalStDate, clrtotalCur, clrtotalSource);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * FEP Web 查詢OPC交易記錄
     *
     * @param feptxn
     * @param nbsday
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageInfo<Feptxn> getFeptxnByTxDate(FeptxnExt feptxn, String nbsday, Integer pageNum, Integer pageSize) throws Exception {
        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            feptxnDao.setTableNameSuffix(feptxn.getTableNameSuffix(), StringUtils.join(ProgramName, ".getFeptxnByTxDate"));
            return feptxnDao.selectByDatetimeAndPcodesAndBknosAndStansAndEjnos(
                    feptxn.getFeptxnTxDate(),
                    feptxn.getFeptxnPcode(),
                    feptxn.getFeptxnBkno(),
                    feptxn.getFeptxnStan(),
                    feptxn.getFeptxnEjfno(),
                    nbsday, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public void inbkLogMessage(Level level, LogData log) {
        logMessage(level, log);
    }

    /**
     * 查詢請求傳送滯留信息
     *
     * @param way
     * @param sysstatHbkno
     * @param datetime
     * @param stime
     * @param etime
     * @param datetimeo
     * @param bkno
     * @param stan
     * @param trad
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageInfo<Feptxn> selectByRetention(String way, String sysstatHbkno, String datetime, String stime, String etime,
                                              String datetimeo, String bkno, String stan, String trad, Integer pageNum, Integer pageSize) throws Exception {
        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            return feptxnDao.selectByRetention(way, sysstatHbkno, datetime, stime, etime, datetimeo, bkno, stan, trad, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Inbkpend> getINBKPend2270(String ORI_TBSDY, String TX_DATE, String BKNO, String STAN, String OPCODE,
                                          String OSTAN, String SYSSTATHBKNO, String sqlSortExpression) {
        InbkpendExtMapper inbkpend = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
        return inbkpend.getINBKPend2270(ORI_TBSDY, TX_DATE, BKNO, STAN, OPCODE, OSTAN, SYSSTATHBKNO, sqlSortExpression);
    }

    public List<Map<String, Object>> getINBKPend2270csv(String ORI_TBSDY, String TX_DATE, String BKNO, String STAN, String OPCODE,
                                                        String OSTAN, String SYSSTATHBKNO, String sqlSortExpression) throws Exception {
        try {
            List<Map<String, Object>> dt = new ArrayList<>();
            InbkpendExtMapper inbkpend = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
            dt = inbkpend.getINBKPend2270csv(ORI_TBSDY, TX_DATE, BKNO, STAN, OPCODE, OSTAN, SYSSTATHBKNO, sqlSortExpression);
            return dt;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Feptxn getFeptxnByPk(Feptxn feptxn, String tbsdy) throws Exception {
        try {
            if (StringUtils.isNotBlank(feptxn.getFeptxnTxDate()) && StringUtils.isNotBlank(String.valueOf(feptxn.getFeptxnEjfno()))) {
                FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
                feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".getFeptxnByPk"));
                return feptxnDao.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            } else {
                return null;
            }

        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Feptxn getFEPTXNFor2280(Feptxn feptxn, String sysDatetime, String sysStatHbkno, String tableNameSuffix) throws Exception {
        try {
            if (StringUtils.isNotBlank(feptxn.getFeptxnTxDate()) && StringUtils.isNotBlank(feptxn.getFeptxnBkno()) && StringUtils.isNotBlank(feptxn.getFeptxnStan())) {
                return feptxnExtMapper.get01FEPTXNFor2280(tableNameSuffix, sysDatetime, feptxn.getFeptxnTxDate(), feptxn.getFeptxnBkno(), sysStatHbkno, feptxn.getFeptxnStan(),
                        feptxn.getFeptxnTbsdyFisc());
            } else {
                return null;
            }

        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Zone> selectAll() throws Exception {
        try {
            return zoneExtMapper.selectAll();
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * xy add
     * UI019201 引用
     *
     * @param aptotStDate
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getAPTOTByStDate(String aptotStDate) throws Exception {
        try {
            return aptotExtMapper.getAPTOTByStDate(aptotStDate);
        } catch (Exception e) {
            LogData logContext = new LogData();
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * xy add
     * UI019201 引用
     *
     * @param aptotStDate
     * @return COUNT SUM(FUNDLOG_FG_AMT)
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFUNDLOGByTxDate(String aptotStDate) throws Exception {
        LogData logContext = new LogData();
        try {
            return fundlogExtMapper.getFUNDLOGSumAmtBytxDate(aptotStDate);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Ictltxn searchIctltxn(Ictltxn tempIctl) throws Exception {
        try {
            return ictltxnExtMapper.getIctltxn(tempIctl);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
        }
    }

    /**
     * xy add
     * UI019202 引用
     *
     * @param aptotStDate
     * @param apId
     * @param ascFlag
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getAPTOTSumAmtByStDateAPIDKind(String aptotStDate,
                                                                        String apId, String ascFlag) throws Exception {
        LogData logContext = new LogData();
        try {
            if (!"*".equals(apId.substring(3, 4))) {} else {
                apId = apId.substring(0, 3);
            }
            return aptotExtMapper.getAPTOTSumAmtByStDateAPIDKind(aptotStDate, apId, ascFlag);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getBrap(String zone, String stDate, String pcode, String apId, String txType,
                                                     String brno, String deptCode, Integer pageNum, Integer pageSize, String brapCur) {
        PageInfo<HashMap<String, Object>> dt;
        switch (zone) {
            case "TWN":
                dt = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        brapExtMapper.getBRAPBySTDateForUI(stDate, pcode, apId, txType, brno, deptCode, brapCur);
                    }
                });
                break;
            case "HKG":
                dt = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        hkbrapExtMapper.getHKBRAPBySTDateForUI(stDate, pcode, apId, txType, brno, deptCode, brapCur);
                    }
                });
                break;
            case "MAC":
                dt = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        mobrapExtMapper.getMOBRAPBySTDateForUI(stDate, pcode, apId, txType, brno, deptCode, brapCur);
                    }
                });
                break;
            default:
                dt = null;
                break;
        }
        return dt;
    }

    /**
     * 查詢請求傳送交易結果
     *
     * @param datetime
     * @param inbkpendPcode
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageInfo<Inbkpend> getINBKPendList(String datetime, String inbkpendPcode, Integer pageNum, Integer pageSize) throws Exception {
        try {
            return feptxnDao.getINBKPendList(datetime, inbkpendPcode, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getFWDTXNByTSBDYFISC(String fwdrstTxDate, String selectValue, String fwdtxnTxId,
                                                                  String channel, String fwdtxnTroutActno, String fwdtxnTrinBkno,
                                                                  String fwdtxnTrinActno, String fwdtxnTxAmt, Short sysFail,
                                                                  Integer pageNum, Integer pageSize) throws Exception {
        try {
            return feptxnDao.getFWDTXNByTSBDYFISC(fwdrstTxDate, selectValue, fwdtxnTxId, channel, fwdtxnTroutActno, fwdtxnTrinBkno, fwdtxnTrinActno, fwdtxnTxAmt, sysFail, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Inbkpend getInbkpendByBknoStan(Inbkpend inbkpend) throws Exception {
        try {
            InbkpendExtMapper inbkpendExtMapper = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
            return inbkpendExtMapper.getpendingDateStanBkno(inbkpend);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Clrtotal getCLRTOTALByPrimaryKey(String clrtotalStDate, String clrtotalCur, Short clrtotalSource) throws Exception {
        LogData logContext = new LogData();
        try {
            return clrtotalMapper.selectByPrimaryKey(clrtotalStDate, clrtotalCur, clrtotalSource);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<Npsbatch> queryNPSBATCH(String fileid, String txdate, int pageNum, int pageSize) throws Exception {
        try {
            // 分頁查詢
            PageInfo<Npsbatch> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    npsbatchExtMapper.queryNPSBATCH(fileid, txdate);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> showDetail(String batno, int pageNum, int pageSize) throws Exception {
        try {
            // 分頁查詢
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    npsdtlExtMapper.showDetail(batno);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<Apibatch> queryApibatch(String beginDate, String endDate, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<Apibatch> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    apibatchExtMapper.queryApibatch(beginDate, endDate);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> queryApidtl(String archivesDate, String webType, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    apidtlExtMapper.queryApidtl(archivesDate.replace("/", ""), webType);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public HashMap<String, Object> getApibatchTotFee(String archivesDate) throws Exception {
        try {
            return apidtlExtMapper.getApibatchTotFee(archivesDate.replace("/", ""));
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * 查詢FCRMSTAT
     *
     * @return Fcrmstat
     */
    public Fcrmstat getFCRMSTAT() throws Exception {
        Fcrmstat fcrmstat = new Fcrmstat();
        try {
            fcrmstat.setFcrmstatCurrency("001");
            List<Fcrmstat> list = fcrmstatExtMapper.queryByPrimaryKey(fcrmstat);
            if (list.size() > 0) {
                return list.get(0);
            } else {
                return null;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    /**
     * ALLBANK 相關 資料庫函式
     * 該銀行代號是否存在ALLBANK中
     * 透過SAFE QueryByPrimaryKey
     *
     * @param bankNo
     * @return
     */
    public Boolean checkBankExist(String bankNo) {
        Allbank allbank = new Allbank();
        try {
            allbank.setAllbankBkno(bankNo);
            if (allbankExtMapper.queryAllBankByBkno(allbank).size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public Sysstat getStatus() throws Exception {
        try {
            return sysstatExtMapper.selectAll().get(0);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    /**
     * 2022-06-02 Han add
     *
     * @param obtltxnTxDate
     * @param obtltxnEjfno
     * @return Obtltxn
     */
    public Obtltxn getOBTLTXNbyPK(String obtltxnTxDate, Long obtltxnEjfno) {

        return obtltxnExtMapper.getOBTLTXNbyPK(obtltxnTxDate, obtltxnEjfno);
    }

    /**
     * 2022-05-31 Han add
     * HashMap<String, Object>
     *
     * @param txtTroutBkno
     * @param txtTroutActno
     * @param txtTxAMT
     * @param txtOrderNO
     * @param txtMerchantId
     * @param txTransactDate
     * @param txTransactDateE
     * @param txtBkno
     * @param txtStan
     * @return
     */
    public PageInfo<Obtltxn> getObtlTxn(String txtTroutBkno, String txtTroutActno, String txtTxAMT,
                                        String txtOrderNO, String txtMerchantId, String txTransactDate, String txTransactDateE, String txtBkno,
                                        String txtStan) {

        return obtltxnExtMapper.getObtlTxn(txtTroutBkno, txtTroutActno, txtTxAMT, txtOrderNO, txtMerchantId,
                txTransactDate, txTransactDateE, txtBkno, txtStan);
    }


}
