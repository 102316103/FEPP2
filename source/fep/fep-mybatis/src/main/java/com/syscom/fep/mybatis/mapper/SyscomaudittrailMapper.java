package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Syscomaudittrail;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface SyscomaudittrailMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Syscomaudittrail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    int insert(Syscomaudittrail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    int insertSelective(Syscomaudittrail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    Syscomaudittrail selectByPrimaryKey(@Param("logid") String logid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Syscomaudittrail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    int updateByPrimaryKeyWithBLOBs(Syscomaudittrail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Syscomaudittrail record);
}