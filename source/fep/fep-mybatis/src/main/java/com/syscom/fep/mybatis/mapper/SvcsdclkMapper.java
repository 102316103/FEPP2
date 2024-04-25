package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Svcsdclk;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface SvcsdclkMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCLK
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Svcsdclk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCLK
     *
     * @mbg.generated
     */
    int insert(Svcsdclk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCLK
     *
     * @mbg.generated
     */
    int insertSelective(Svcsdclk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCLK
     *
     * @mbg.generated
     */
    Svcsdclk selectByPrimaryKey(@Param("seqno") Long seqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCLK
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Svcsdclk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCLK
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Svcsdclk record);
}