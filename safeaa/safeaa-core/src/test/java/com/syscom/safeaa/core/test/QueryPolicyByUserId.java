package com.syscom.safeaa.core.test;


import com.syscom.safeaa.mybatis.extmapper.SyscompolicyExtMapper;
import com.syscom.safeaa.mybatis.model.SyscomQueryParentRolesByUserId;
import com.syscom.safeaa.mybatis.model.Syscompolicy;
import com.syscom.safeaa.utils.SafeaaSpringBeanFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;

public class QueryPolicyByUserId {
    private final static Logger log= LoggerFactory.getLogger(QueryPolicyByUserId.class);
    public static void main(String[] args) throws Exception {

        Syscompolicy syscompolicy = new Syscompolicy();
        SyscompolicyExtMapper syscompolicyMapper = SafeaaSpringBeanFactoryUtil.getBean(SyscompolicyExtMapper.class);

        List<SyscomQueryParentRolesByUserId> list = new ArrayList<SyscomQueryParentRolesByUserId>();
        list = syscompolicyMapper.queryPolicyByUserId(2);
        log.info("Test is OK");
    }
}

