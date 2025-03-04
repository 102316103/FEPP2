package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Fwderr;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface FwderrMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Fwderr record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    int insert(Fwderr record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    int insertSelective(Fwderr record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    Fwderr selectByPrimaryKey(@Param("fwderrSeqno") Integer fwderrSeqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Fwderr record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Fwderr record);
}