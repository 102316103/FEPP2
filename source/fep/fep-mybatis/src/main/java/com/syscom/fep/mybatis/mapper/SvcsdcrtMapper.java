package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Svcsdcrt;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface SvcsdcrtMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Svcsdcrt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRT
     *
     * @mbg.generated
     */
    int insert(Svcsdcrt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRT
     *
     * @mbg.generated
     */
    int insertSelective(Svcsdcrt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRT
     *
     * @mbg.generated
     */
    Svcsdcrt selectByPrimaryKey(@Param("seqno") Long seqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Svcsdcrt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Svcsdcrt record);
}