package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.TestdropObjects;
import javax.annotation.Resource;

@Resource
public interface TestdropObjectsMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    int insert(TestdropObjects record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    int insertSelective(TestdropObjects record);
}