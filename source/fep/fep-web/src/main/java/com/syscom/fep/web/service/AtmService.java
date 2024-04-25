package com.syscom.fep.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.AlarmExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AlertExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmcExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmmstrExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.CurcdExtMapper;
import com.syscom.fep.mybatis.ext.mapper.EventExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FepgroupExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FepuserExtMapper;
import com.syscom.fep.mybatis.ext.mapper.GuardExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgkbExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SubsysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.ext.mapper.VendorExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.Alarm;
import com.syscom.fep.mybatis.model.Alert;
import com.syscom.fep.mybatis.model.Atmc;
import com.syscom.fep.mybatis.model.Atmmstr;
import com.syscom.fep.mybatis.model.Bctl;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Curcd;
import com.syscom.fep.mybatis.model.Event;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.mybatis.model.Guard;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.mybatis.model.Vendor;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.web.form.dbmaintain.UI_070020_FormDetail;

@Service
public class AtmService extends BaseService {
	@Autowired
	private AlertExtMapper alertExtMapper;

	@Autowired
	private MsgkbExtMapper msgkbExtMapper;

	@Autowired
	private BctlExtMapper bctlExtMapper;

	private static final String ProgramName = AtmService.class.getSimpleName();

	@Autowired
	private AtmmstrExtMapper atmmstrMapper;	//Han add

	@Autowired
	private ZoneExtMapper zoneExtMapper;//Bruce add

	@Autowired
	private CurcdExtMapper curcdExtMapper;//Bruce add

	@Autowired
	private AtmcExtMapper atmcExtMapper; //Bruce add

	@Autowired
	private MsgfileExtMapper msgfileExtMapper;//Bruce add

	@Autowired
	private FepuserExtMapper fepuserExtMapper;//Bruce add

	@Autowired
	private FepgroupExtMapper fepgroupExtMapper;//Bruce add

	@Autowired
	private BsdaysExtMapper bsdaysExtMapper;//Bruce add

	@Autowired
	private AtmmstrExtMapper atmmstrExtMapper;//Bruce add

	@Autowired
	private VendorExtMapper vendorExtMapper;//Bruce add

	@Autowired
	private GuardExtMapper guardExtMapper;//Bruce add

	@Autowired
	private SubsysExtMapper subsysExtMapper;	//Han add

	@Autowired
	SysconfExtMapper sysconfExtMapper;	//Han add

	@Autowired
	private AlarmExtMapper alarmExtMapper;//Ben add

	@Autowired
	private EventExtMapper eventExtMapper;//Ben add

//   20230322 Bruce 先註解掉 TODO
//	public Zone GetDataByZonee(String zone) throws Exception {
//		try {
//			Zone dd = zoneExtMapper.getDataByZonee(zone);
//			return dd;
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			throw ExceptionUtil.createException(this.getInnerMessage(ex));
//		}
//	}
	public List<Map<String, Object>> getBSDAYSByYearAndZoneAndDate(String bsdays_ZONE_CODEDdl, String txtBSDAYS_DATE) {
		return bsdaysExtMapper.getBSDAYSByYearAndZoneAndDate(bsdays_ZONE_CODEDdl, txtBSDAYS_DATE);
	}

	public List<Map<String, Object>> getBSDAYSByYearAndZone(String txtBSDAYS_YEAR, String BSDAYS_TARGET_NEXT_YEAR,
															String lblBSDAYS_ZONE_CODE) {
		return bsdaysExtMapper.getBSDAYSByYearAndZone(txtBSDAYS_YEAR, BSDAYS_TARGET_NEXT_YEAR, lblBSDAYS_ZONE_CODE);
	}
	public void updateBSDAYS(UI_070020_FormDetail form, Long UserId) {


		bsdaysExtMapper.updateByPrimaryKey2(UserId,
				form.getBSDAYS_ZONE_CODEDdl(),form.getTxtBSDAYS_DATE(),
				form.getBSDAYS_WORKDAYDdl(),form.getTxtBSDAYS_JDAY(),form.getTxtBSDAYS_NBSDY(),
				form.getTxtBSDAYS_WEEKNO(),form.getTxtBSDAYS_ST_FLAG(),
				form.getTxtBSDAYS_ST_DATE_ATM(),form.getTxtBSDAYS_ST_DATE_RM());
	}
	public int insertBSDAYS(Bsdays bsdayTable) {
		return bsdaysExtMapper.insert(bsdayTable);
	}
//  20230322 Bruce 先註解掉 TODO
//	public Zone getDataByZonee(String zone) throws Exception {
//		try {
//			Zone defZone = new Zone();
//			defZone.setZoneCode(zone);
//			Zone dt=zoneExtMapper.getDataTableByPrimaryKey(defZone);
//			return dt;
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			sendEMS(ex);
//			throw ExceptionUtil.createException(this.getInnerMessage(ex));
//		}
//	}

	/**
	 * Bruce add 取得保全中文
	 * @param guardCode
	 * @return
	 * @throws Exception
	 */
//	public Guard getGuardName(String guardCode) throws Exception {
//		try {
//			return guardExtMapper.selectByPrimaryKey(guardCode);
//		}catch(Exception ex) {
//			sendEMS(ex);
//			throw ExceptionUtil.createException(this.getInnerMessage(ex));
//		}		
//	}
	
	/**
	 * Bruce add 廠牌代號轉中文
	 * @return
	 * @throws Exception
	 */
//	public String queryVendorNameByPK(String atmVendor) throws Exception {
//		if(StringUtils.isBlank(atmVendor)) {
//			return "";
//		}
//		Vendor vendor = null;
//		try {
//			vendor = vendorExtMapper.selectByPrimaryKey(atmVendor);
//			if(vendor == null) {
//				return "VENDOR_NO" + atmVendor + "未建立";
//			}else {
//				if(StringUtils.isBlank(vendor.getVendorNameS())) {
//					return vendor.getVendorName();
//				}else {
//					return vendor.getVendorNameS();
//				}
//			}
//		} catch (Exception e) {
//			sendEMS(e);
//			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
//		}
//	}

	/**
	 * 廠牌下拉選單
	 * @return
	 * @throws Exception
	 */
	public List<Vendor> qetAllVendor() throws Exception{
		try {
//			return vendorExtMapper.queryAllData();
			return null;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}


	/**
	 * Atm基本資料查詢
	 * @param argsMap
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Map<String,Object>> getAtmBasicList(Map<String,Object> argsMap) throws Exception{
		try {
			int pageNum = (Integer) argsMap.get("pageNum") == null ? 0 : (int) argsMap.get("pageNum");
			int pageSize = (Integer) argsMap.get("pageSize") == null ? 0 : (int) argsMap.get("pageSize");
			// 分頁查詢
			PageInfo<Map<String,Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						    @Override
							public void doSelect() {
						    	atmmstrExtMapper.getAtmBasicList(argsMap);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * Atm基本資料查詢
	 * @param argsMap
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getAtmBasicCSV(Map<String,Object> argsMap) throws Exception{
		try {
			List<Map<String, Object>> dt = new ArrayList<>();

			dt = atmmstrExtMapper.getAtmBasicList(argsMap);
			return dt;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	
	/**
	 * Atm更新憑證版本 20230322 Bruce add
	 * 20231227 改成更新 是否連線至FEP(ATMMSTR.ATM_FEP_CONNECTION)、ATMIP(ATMMSTR.ATM_IP)
	 * @param argsMap
	 * @return
	 * @throws Exception
	 */
	public int updateAtmmstr(Map<String, Object> argsMap) throws Exception{
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        int find = 0;
        try {
			find = atmmstrExtMapper.updateAtmmstrByAtmatmNo(argsMap);
			transactionManager.commit(txStatus);
		} catch (Exception e) {
			sendEMS(e);
			transactionManager.rollback(txStatus);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
        return find;
	}

    /**
     * Bruce add
     * @param argsMap
     * @return
     * @throws Exception
     */
	public PageInfo<Feptxn> getFeptxnByEjfno(Map<String,Object> argsMap) throws Exception{
		try {
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			feptxnDao.setTableNameSuffix((String) argsMap.get("tableNameSuffix"), StringUtils.join(ProgramName, ".getFeptxn"));
			return feptxnDao.getFeptxnByEj(argsMap);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * Bruce add
	 * @param argsMap
	 * @throws Exception
	 */
	public void getNextBusinessDate(Map<String,Object> argsMap) throws Exception {
		try {
			bsdaysExtMapper.getNextBusinessDate(argsMap);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
	}

	/**
	 * bruce add 取得BsdaysDate
	 * @param transactDate
	 * @return
	 * @throws Exception
	 */
	public String getNbsDays(String transactDate) throws Exception {
		try {
			return bsdaysExtMapper.getBsdaysDate(transactDate);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
	}

	/**
	 * 取得上次修改人員 logonid 及 name
	 * @param updateUserId
	 * @return
	 * @throws Exception
	 */
	public String getFepUserId(int updateUserId,String paramType) throws Exception {
		try {
			Map<String, Object> argsMap = new HashMap<String, Object>();
			if("userId".equals(paramType)) {
				argsMap.put("updateUserId", updateUserId);
			}else if("bossId".equals(paramType)) {
				argsMap.put("userTelId",  String.valueOf(updateUserId));
			}
			List<Fepuser> fepuser = fepuserExtMapper.getFepUser(argsMap);
			if(fepuser.size() == 0) {
				return null;
			}else {
				return fepuser.get(0).getFepuserLogonid() + "-" + fepuser.get(0).getFepuserName();
			}
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
	}

	/**
	 * Bruce add 取得權限群組中文名稱
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public String getGroupIdNameByID(String groupId) throws Exception {
		try {
			return fepgroupExtMapper.getGroupIDName(groupId);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
	}

	/**
	 * Bruce add 取得分行代號 中文
	 * @param brNo
	 * @return
	 * @throws Exception
	 */
	public String getBctlNameByBrno(String brNo) throws Exception {
		try {
			return fepuserExtMapper.getBrNoName(brNo);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
	}

	/**
	 * Bruce add 使用者資料查詢 查詢
	 * @param argsMap
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Fepuser> queryFepUser(Map<String, Object> argsMap) throws Exception{
		try {
			Integer pageNum = (Integer) argsMap.get("pageNum") == null ? 0 : (Integer) argsMap.get("pageNum");
			Integer pageSize = (Integer) argsMap.get("pageSize") == null ? 0 : (Integer) argsMap.get("pageSize");
			// 分頁查詢
			PageInfo<Fepuser> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						    @Override
							public void doSelect() {
						    	fepuserExtMapper.getFepUser(argsMap);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * Bruce add 取得分行代號下拉選單
	 * @return
	 * @throws Exception
	 */
	public List<Bctl> getAllBCTLBrno() throws Exception{
		try{
			return bctlExtMapper.getBCTLAlias("BCTL_BRNO");
		}catch(Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * Bruce add 錯誤訊息維護MSFFILE  儲存
	 * @param msgfile
	 * @throws Exception
	 */
	public boolean insertMsgFile(Msgfile msgfile) throws Exception {
		int channel = msgfile.getMsgfileChannel();
		String errorCode = msgfile.getMsgfileErrorcode();
		Msgfile haveMsgfile = msgfileExtMapper.selectByPrimaryKey(channel,errorCode);
		if(haveMsgfile != null) {
			return false;
		}else {
			try {
				msgfileExtMapper.insertSelective(msgfile);
			}catch(Exception e) {
				sendEMS(e);
				throw ExceptionUtil.createException(e, this.getInnerMessage(e));
			}
			return true;
		}
	}

	/**
	 * Bruce add 錯誤訊息維護MSFFILE  變更
	 * @param msgfile
	 */
	public void updateMsgFile(Msgfile msgfile) {
//		msgfileExtMapper.updateMsgfile(msgfile);updateByPrimaryKeySelective
		msgfileExtMapper.updateByPrimaryKeySelective(msgfile);
	}

	/**
	 * Bruce add 錯誤訊息維護MSFFILE
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Msgfile> queryMsgFileByDef(Map<String, Object> args) throws Exception{
		try {
			Integer pageNum = (Integer) args.get("pageNum") == null ? 0 : (Integer) args.get("pageNum");
			Integer pageSize = (Integer) args.get("pageSize") == null ? 0 : (Integer) args.get("pageSize");
			// 分頁查詢
			PageInfo<Msgfile> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						    @Override
							public void doSelect() {
								msgfileExtMapper.queryMsgFileByDef(args);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * Bruce add 錯誤訊息查詢
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Msgfile> queryMsgFileByDefLike(Map<String, Object> args) throws Exception{
		try {
			Integer pageNum = (Integer) args.get("pageNum") == null ? 0 : (Integer) args.get("pageNum");
			Integer pageSize = (Integer) args.get("pageSize") == null ? 0 : (Integer) args.get("pageSize");
			// 分頁查詢
			PageInfo<Msgfile> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						    @Override
							public void doSelect() {
								msgfileExtMapper.queryMsgFileByDefLike(args);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			this.errorMessage(e,e.getMessage());
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * Bruce add 取得幣別下拉選單
	 * @return
	 * @throws Exception
	 */
	public List<Curcd> getAllCurcd() throws Exception {
		try {
			return curcdExtMapper.selectAll();
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(ex);
			throw ExceptionUtil.createException(this.getInnerMessage(ex));
		}
	}

	/**
	 * Bruce add 為了取得自行營業日 //   20230322 Bruce 先註解掉 TODO
	 * @param zoneCode
	 * @return
	 * @throws Exception
	 */
//	public Zone getDataByZone(String zoneCode) throws Exception {
//		try {
//			List<Zone> zone = zoneExtMapper.getDataByZone(zoneCode);
//			if(zone.size() == 0) {
//				return null;
//			}else {
//				return zoneExtMapper.getDataByZone(zoneCode).get(0);
//			}
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			sendEMS(ex);
//			throw ExceptionUtil.createException(this.getInnerMessage(ex));
//		}
//	}

	/**
	 * Bruce add ATM(ATMC)收付累計查詢
	 * @param form
	 * @return
	 */
	public PageInfo<Atmc> queryATMCByDef(Map<String, Object> args) throws Exception{
		try {
			Integer pageNum = (Integer) args.get("pageNum") == null ? 0 : (Integer) args.get("pageNum");
			Integer pageSize = (Integer) args.get("pageSize") == null ? 0 : (Integer) args.get("pageSize");
			// 分頁查詢
			PageInfo<Atmc> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
								atmcExtMapper.getATMCbyDef(args);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}


	public Sysstat getStatus() throws Exception {
		try {
			return SysStatus.getPropertyValue();
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * FEP Web 交易日誌(FEPTXN)查詢
	 *
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Feptxn> getFeptxn(Map<String, Object> args) throws Exception {
		try {
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			feptxnDao.setTableNameSuffix((String) args.get("tableNameSuffix"), StringUtils.join(ProgramName, ".getFeptxn"));
			return feptxnDao.getFeptxn(args);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * FEP Web 交易日誌(FEPTXN)查詢
	 *
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getFeptxnSummary(Map<String, Object> args) throws Exception {
		try {
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			feptxnDao.setTableNameSuffix((String) args.get("tableNameSuffix"), StringUtils.join(ProgramName, ".getFeptxnSummary"));
			return feptxnDao.getFeptxnSummary(args);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * FEP Web 交易日誌(FEPTXN)查詢明細資料
	 *
	 * @param tableNameSuffix
	 * @param feptxnEjfno
	 * @param feptxnTxDate
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getFeptxnIntltxn(String tableNameSuffix, Integer feptxnEjfno, String feptxnTxDate) throws Exception {
		try {
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			feptxnDao.setTableNameSuffix(tableNameSuffix, StringUtils.join(ProgramName, ".getFeptxnIntltxn"));
			return feptxnDao.getFeptxnIntltxn(feptxnTxDate, feptxnEjfno);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	/**
	 * FEP Web 事件管理系統(EMS)日誌查詢
	 */
	public PageInfo<HashMap<String,Object>> queryAlertData(String AR_SUBSYS, String beginDateTime, String endDateTime, String sLevel, String sATMNo, String sIP,
														   String sApplication, int pageNum, int pageSize) throws Exception {
		Alert defAlert = new Alert();

		if (!"全部".equals(AR_SUBSYS)) {
			defAlert.setArSubsys(AR_SUBSYS);
		}

		if (!"".equals(sLevel)) {
			defAlert.setArLevel(sLevel);
		}

		if (!"".equals(sATMNo)) {
			defAlert.setAtmno(sATMNo);
		}

		if (!"".equals(sIP)) {
			defAlert.setArHostip(sIP);
		}

		if (StringUtils.isNoneEmpty(sApplication) && !"0".equals(sApplication)) {
			defAlert.setArApplication(sApplication);
		}
		// 分頁查詢
		PageInfo<HashMap<String,Object>> pageInfo = null;
		try{
			pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
				@Override
				public void doSelect() {
					alertExtMapper.queryAlert(defAlert, beginDateTime, endDateTime);
				}
			});
		}catch (Exception ex){
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
		pageInfo.setList(pageInfo.getList());
		return pageInfo;
	}

	public List<Msgkb> get_AlertDetailForUI060620B(String ErrCode, String TxExternalCode, String TxExSubCode) {
		Msgkb defMSGKB = new Msgkb();

		try {
			defMSGKB.setErrorcode(ErrCode);
			defMSGKB.setExternalcode(TxExternalCode);
			defMSGKB.setExsubcode(TxExSubCode);
			List<Msgkb> dt = msgkbExtMapper.getDataTableByPrimaryKey(defMSGKB);
			return dt;
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
//			FEPBase.sendEMS(getLogContext());
		}finally {

		}
		return null;
	}

	public Boolean chkExistInMSGKB(String ErrCode, String TxExternalCode, String TxExSubCode) {
		Msgkb defMSGKB = new Msgkb();
		try {
			defMSGKB.setErrorcode(ErrCode);
			defMSGKB.setExternalcode(TxExternalCode);
			defMSGKB.setExsubcode(TxExSubCode);
			List<Msgkb> dt = msgkbExtMapper.getDataTableByPrimaryKey(defMSGKB);
			if (dt.size() > 0) {
				return true;
			}else {
				return false;
			}
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
//			FEPBase.sendEMS(getLogContext());
		}
		return null;
	}

	public int updateMSGKB(Msgkb msgkb) {
		try {
			return msgkbExtMapper.updateByPrimaryKeySelective(msgkb);
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
		}
		return 0;
	}

	public int insertMSGKB(Msgkb msgkb) {
		try {
			return msgkbExtMapper.insertSelective(msgkb);
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
		}
		return 0;
	}

	public String queryAR_Message(String AR_NO) {
		Alert alert = new Alert();
		if (AR_NO != null) {
			alert.setArNo(Integer.parseInt(AR_NO));
		}
		try {
			List<Alert> dt = alertExtMapper.getDataTableByPrimaryKey(alert);
			if (dt.size() > 0) {
				return dt.get(0).getArMessage();
			}else {
				return "";
			}
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
		}
		return "";
	}
	public List<Bctl> getAllBctlBrno() throws Exception {
		try {
			return bctlExtMapper.getBCTLAlias("BCTL_BRNO");
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(ex);
			throw new Exception(getInnerMessage(ex));
		}
	}

	/**
	 * @param String ATMNoTxt
	 * @edit Han Add
	 * @since 2022/5/5
	 */
	public List<Map<String,Object>> getSingleATM(String ATMNoTxt){
		return atmmstrMapper.getSingleATM(ATMNoTxt);
	}

	/**
	 *  xy add 2022/5/23
	 * @throws Exception
	 */
	public String getBRAlias(String brno) throws Exception{
		try {
			return bctlExtMapper.getBRAlias(brno);
		}catch(Exception ex) {
			sendEMS(ex);
			throw new Exception(getInnerMessage(ex));
		}
	}
	/**
	 * Han add 2022-06-07
	 *
	 * @param orderBy
	 * @return List<Subsys>
	 */
	public List<Subsys> queryAllData(String orderBy) {
		return subsysExtMapper.queryAllData(orderBy);
	}

	/**
	 * Han add 2022-06-13
	 *
	 * @param sysconfSubsysno
	 * @param sysconfName
	 * @return List<Map<String,String>>
	 */
	public Map<String, Object> querySysConfByPK2(String sysconfSubsysno, String sysconfName) {
		sysconfName = "%" + sysconfName + "%";
		return sysconfExtMapper.getAllDataByPk2(Integer.parseInt(sysconfSubsysno), sysconfName);
	}
	/**
	 * Han add 2022-06-07
	 *
	 * @param sysconfSubsysno
	 * @param sysconfName
	 * @return List<Map<String,String>>
	 */
	public List<Map<String, String>> querySysConfByPK(String sysconfSubsysno, String sysconfName) {

		if (StringUtils.isEmpty(sysconfName) && StringUtils.isNotEmpty(sysconfSubsysno)) {
			return sysconfExtMapper.getDataBySubSystem(Integer.parseInt(sysconfSubsysno));
		} else if (StringUtils.isNotEmpty(sysconfName) && StringUtils.isEmpty(sysconfSubsysno)) {
			sysconfName = "%" + sysconfName + "%";
			return sysconfExtMapper.getAllBySubSystemName(sysconfName);
		} else if (StringUtils.isEmpty(sysconfName) && StringUtils.isEmpty(sysconfSubsysno)) {
			sysconfName = "%" + sysconfName + "%";
			return sysconfExtMapper.getAllData();
		} else {
			sysconfName = "%" + sysconfName + "%";
			return sysconfExtMapper.getAllDataByPk(Integer.parseInt(sysconfSubsysno), sysconfName);
		}
	}

	/**
	 * Han add 2022-06-16
	 *
	 * @param parseInt
	 * @param sysconfName
	 * @param sysconfValue
	 * @param sysconfRemark
	 * @param sysconfType
	 * @param sysconfSubsysnoC
	 * @param sysconfReadonly
	 * @param sysconfDatatype
	 * @param sysconfReadonly2
	 */
	public void updateSysConf(int sysconfSubsysno, String sysconfName, String sysconfValue, String sysconfRemark,
			String sysconfType, int sysconfReadonly, String sysconfDatatype) {

		sysconfExtMapper.updateByPrimaryKey(sysconfSubsysno, "%"+sysconfName+"%", sysconfValue, sysconfRemark, sysconfType,
				sysconfReadonly, sysconfDatatype);
	}

	/**
	 * Ben add 刪除警示類別Key時需判斷是否EVENT主檔有使用到，若沒有則可以刪除
	 * @param alarmNo
	 * @return
	 */
	public List<Event> CheckEVENTForAlarmDelete(String alarmNo) throws Exception {
		try {
			return eventExtMapper.CheckEVENTForAlarmDelete(alarmNo);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(ex);
			throw new Exception(getInnerMessage(ex));
		}
	}

	/**
	 * Ben add 更新一筆 ALARM
	 * @param alarm
	 * @return
	 */
	public Integer updateAlarm(Alarm alarm){
		try {
			return alarmExtMapper.updateByPrimaryKeySelective(alarm);
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
			return 0;
		}
	}

	/**
	 * Ben add 新增一筆 ALARM
	 * @param alarm
	 * @return
	 */
	public Integer insertAlarm(Alarm alarm) {
		try {
			return alarmExtMapper.insertSelective(alarm);
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
			return 0;
		}
	}
	/**
	 * Ben add 取得 ALARM List資訊
	 * @param alarmNo
	 * @return
	 */
	public List<Alarm> getAlarmByPKLike(String alarmNo){
		return alarmExtMapper.getAlarmByPKLike(alarmNo);
	}

	/**
	 * Ben add 取得一筆 ALARM
	 * @param alarmNo
	 * @return
	 * @throws Exception
	 */
	public Alarm getAlarmByNo(String alarmNo) throws Exception {
		try {
			return alarmExtMapper.selectByPrimaryKey(alarmNo);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(ex);
			throw ExceptionUtil.createException(this.getInnerMessage(ex));
		}
	}

	/**
	 * Ben add 刪除一筆 ALARM
	 * @param alarm
	 * @return
	 */
	public Integer deleteAlarm(Alarm alarm){
		try {
			return alarmExtMapper.deleteByPrimaryKey(alarm);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			return 0;
		}
	}

	public List<Subsys> getSubsysAll() {
		return subsysExtMapper.queryAll();
	}
	
	/**
	 * 2023-09-18 Bruce add 取得所有gateway ip
	 * @return
	 * @throws Exception 
	 */
	public List<Map<String,String>> getAtmAtmpIp() throws Exception{
		try {
			return atmmstrExtMapper.getAtmpIpList();
		} catch (Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(this.getInnerMessage(ex));
		}
	}
	
	/**
	 * 20230918 Bruce add 更新是否連線fep及atmip
	 * @param atmmstr
	 * @return
	 * @throws Exception
	 */
	public int updateAtmmstrByFepConnect(Atmmstr atmmstr) throws Exception {
		try {
			return atmmstrExtMapper.updateAtmmstrByAtmNoSelective(atmmstr);
		} catch (Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(this.getInnerMessage(ex));
		}
	}

}
