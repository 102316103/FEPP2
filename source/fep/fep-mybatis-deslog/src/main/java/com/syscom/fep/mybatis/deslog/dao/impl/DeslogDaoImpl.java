package com.syscom.fep.mybatis.deslog.dao.impl;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.deslog.configuration.DataSourceDeslogConstant;
import com.syscom.fep.mybatis.deslog.dao.DeslogDao;
import com.syscom.fep.mybatis.deslog.ext.mapper.DeslogExtMapper;
import com.syscom.fep.mybatis.deslog.mapper.DeslogMapper;
import com.syscom.fep.mybatis.deslog.model.Deslog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DeslogDaoImpl implements DeslogDao, DataSourceDeslogConstant {
    /**
     * @param recordList
     * @return
     */
    @Override
    public int insertBatch(List<Deslog> recordList, int flushStatementsTotal) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        SqlSessionFactory sqlSessionFactory = SpringBeanFactoryUtil.getBean(BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            DeslogExtMapper mapper = sqlSession.getMapper(DeslogExtMapper.class);
            int total = 1;
            for (Deslog record : recordList) {
                result += mapper.insert(record);
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
}
