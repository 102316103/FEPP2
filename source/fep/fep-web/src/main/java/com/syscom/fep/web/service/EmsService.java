package com.syscom.fep.web.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ems.mapper.FeplogMapper;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.web.form.rm.UI_028080_Form;

@Service
public class EmsService extends BaseService {
	@Autowired
	FeplogMapper feplogMapper;

	private static final String PROGRAM_NAME = EmsService.class.getSimpleName();

	protected SubSystem getSubSystem() {
		return SubSystem.CMN;
	}
	
	/**
	 * Bruce add feplog查詢功能
	 * @param argsMap
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Feplog> getfepLog(Map<String, Object> argsMap) throws Exception{
		try {
			FeplogDao feplogDao = SpringBeanFactoryUtil.getBean("feplogDao");
			feplogDao.setTableNameSuffix(argsMap.get("tableNameSuffix").toString(), StringUtils.join(PROGRAM_NAME, ".getFeplog_UI060610"));
			return feplogDao.getMultiFEPLogByDef(argsMap);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}
	}

	/**
	 * 2021-08-29 Richard add for FEPLOG查詢
	 * 
	 * @param ejfnoList
	 * @param feptxnTraceEjfno
	 * @param feptxnTxDate
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Feplog> getFeplog_UI060550(List<Long> ejfnoList, Long feptxnTraceEjfno, String feptxnTxDate, Integer pageNum, Integer pageSize) throws Exception {
		try {
			Calendar cal = CalendarUtil.parseDateValue(Integer.parseInt(feptxnTxDate));
			String tableNameSuffix = String.valueOf(CalendarUtil.getDayOfWeek(cal));
			FeplogDao feplogDao = SpringBeanFactoryUtil.getBean("feplogDao");
			feplogDao.setTableNameSuffix(tableNameSuffix, StringUtils.join(PROGRAM_NAME, ".getFeplog_UI060550"));
			Feplog feplog = new FeplogExt();
			if (feptxnTraceEjfno != null && feptxnTraceEjfno != 0) {
				feplog.setEj(feptxnTraceEjfno);
				ejfnoList.add(feptxnTraceEjfno);
			}
			return feplogDao.getMultiFepLogByDef(feplog, null, null, ejfnoList, pageNum, pageSize);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	/**
	 * 2021-08-29 Richard add for FEPLOG查詢明細資料
	 * 
	 * @param logno
	 * @param tableNameSuffix
	 * @return
	 * @throws Exception
	 */
	public Feplog getFeplogDetail(Long logno, String tableNameSuffix) throws Exception {
		try {
			FeplogDao feplogDao = SpringBeanFactoryUtil.getBean("feplogDao");
			feplogDao.setTableNameSuffix(tableNameSuffix, StringUtils.join(PROGRAM_NAME, ".getFeplogDetail"));
			return feplogDao.selectByPrimaryKey(logno);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	public  List<Feplog> getFeplogByDef(Feplog defFEPLog, UI_028080_Form form, String tableNameSuffix,Integer pageNum, Integer pageSize) throws Exception {
		FeplogDao feplogDao = SpringBeanFactoryUtil.getBean("feplogDao");
		feplogDao.setTableNameSuffix(tableNameSuffix, StringUtils.join(PROGRAM_NAME, ".getFeplogByDef"));
		return feplogDao.getFeplogByDef(defFEPLog);
	}
}
