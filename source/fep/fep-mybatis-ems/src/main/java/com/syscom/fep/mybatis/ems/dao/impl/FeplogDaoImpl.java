package com.syscom.fep.mybatis.ems.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.syscom.fep.mybatis.ems.configuration.DataSourceEmsConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.mapper.FeplogExtMapper;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ems.mapper.FeplogMapper;
import com.syscom.fep.mybatis.ems.model.Feplog;

@Repository("feplogDao")
@Scope("prototype")
public class FeplogDaoImpl implements FeplogDao {
	@Autowired
	private FeplogExtMapper feplogMapper;

	private String tableNameSuffix;

	private Feplog setTableNameSuffix(Feplog feplog) {
		if (feplog != null) {
			if (feplog instanceof FeplogExt) {
				((FeplogExt) feplog).setTableNameSuffix(this.tableNameSuffix);
			} else {
				return this.setTableNameSuffix(new FeplogExt(feplog));
			}
		}
		return feplog;
	}

	private List<Feplog> setTableNameSuffix(List<Feplog> list) {
		List<Feplog> resultList = null;
		if (list != null) {
			resultList = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				resultList.add(this.setTableNameSuffix(list.get(i)));
			}
		}
		return resultList;
	}

	@Override
	public void setTableNameSuffix(String tableNameSuffix, String invoker) {
		this.tableNameSuffix = tableNameSuffix;
		LogHelperFactory.getTraceLogger().info("Switch to [FEPLOG", tableNameSuffix, "] by [", invoker, "]");
	}

	@Override
	public String getTableNameSuffix() {
		return this.tableNameSuffix;
	}

	@Override
	public int deleteByPrimaryKey(Feplog record) {
		return feplogMapper.deleteByPrimaryKey(this.setTableNameSuffix(record));
	}

	@Override
	public int insert(Feplog record) {
		return feplogMapper.insert(this.setTableNameSuffix(record));
	}

	@Override
	public int insertSelective(Feplog record) {
		return feplogMapper.insertSelective(this.setTableNameSuffix(record));
	}

	@Override
	public Feplog selectByPrimaryKey(Long logno) {
		return this.setTableNameSuffix(feplogMapper.selectByPrimaryKey(this.tableNameSuffix, logno));
	}

	@Override
	public int updateByPrimaryKeySelective(Feplog record) {
		return feplogMapper.updateByPrimaryKeySelective(this.setTableNameSuffix(record));
	}

	@Override
	public int updateByPrimaryKeyWithBLOBs(Feplog record) {
		return feplogMapper.updateByPrimaryKeyWithBLOBs(this.setTableNameSuffix(record));
	}

	@Override
	public int updateByPrimaryKey(Feplog record) {
		return feplogMapper.updateByPrimaryKey(this.setTableNameSuffix(record));
	}

	@Override
	public int insertBatch(List<Feplog> recordList, int flushStatementsTotal) {
		if (CollectionUtils.isEmpty(recordList)) {
			return -1;
		}
		int result = 0;
		SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceEmsConstant.BEAN_NAME_SQL_SESSION_FACTORY);
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
			FeplogMapper mapper = sqlSession.getMapper(FeplogMapper.class);
			int total = 1;
			for (Feplog record : recordList) {
				result += mapper.insert(this.setTableNameSuffix(record));
				if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
					sqlSession.flushStatements();
				}
			}
			sqlSession.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(sqlSession);
		}
		return result;
	}

	@Override
	public int insertSelectiveBatch(List<Feplog> recordList, int flushStatementsTotal) {
		if (CollectionUtils.isEmpty(recordList)) {
			return -1;
		}
		int result = 0;
		SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceEmsConstant.BEAN_NAME_SQL_SESSION_FACTORY);
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
			FeplogMapper mapper = sqlSession.getMapper(FeplogMapper.class);
			int total = 1;
			for (Feplog record : recordList) {
				result += mapper.insertSelective(this.setTableNameSuffix(record));
				if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
					sqlSession.flushStatements();
				}
			}
			sqlSession.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(sqlSession);
		}
		return result;
	}

	/**
	 * 2021-08-29 Richard add for FEPLOG查詢
	 * 
	 * @param feplog
	 * @param logDateBegin
	 * @param logDateEnd
	 * @param ejfnoList
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@Override
	public PageInfo<Feplog> getMultiFepLogByDef(Feplog feplog, Date logDateBegin, Date logDateEnd, List<Long> ejfnoList, Integer pageNum, Integer pageSize) {
		pageNum = pageNum == null ? 0 : pageNum;
		pageSize = pageSize == null ? 0 : pageSize;
		// 分頁查詢
		PageInfo<Feplog> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
			@Override
			public void doSelect() {
				feplogMapper.getMultiFepLogByDef(
						tableNameSuffix,
						feplog,
						logDateBegin,
						logDateEnd,
						ejfnoList);
			}
		});
		pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
		return pageInfo;
	}

	@Override
	public List<Feplog> getFeplogByDef(Feplog feplog) {
		return this.setTableNameSuffix(feplogMapper.getFeplogByDef(tableNameSuffix, feplog));
	}

	@Override
	public PageInfo<Feplog> getMultiFEPLogByDef(Map<String, Object> argsMap) {
		int pageNum = argsMap.get("pageNum") == null ? 0 : (int) argsMap.get("pageNum") ;
		int pageSize = argsMap.get("pageSize") == null ? 0 : (int) argsMap.get("pageSize");
		// 分頁查詢
		PageInfo<Feplog> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
			@Override
			public void doSelect() {
				feplogMapper.getFepLog(argsMap);
			}
		});
		pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
		return pageInfo;
	}
}
