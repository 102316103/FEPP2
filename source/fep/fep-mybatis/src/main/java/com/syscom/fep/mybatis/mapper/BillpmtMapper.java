package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Billpmt;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface BillpmtMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BILLPMT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Billpmt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BILLPMT
     *
     * @mbg.generated
     */
    int insert(Billpmt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BILLPMT
     *
     * @mbg.generated
     */
    int insertSelective(Billpmt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BILLPMT
     *
     * @mbg.generated
     */
    Billpmt selectByPrimaryKey(@Param("billpmtTbsdy") String billpmtTbsdy, @Param("billpmtStan") String billpmtStan);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BILLPMT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Billpmt record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BILLPMT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Billpmt record);
}