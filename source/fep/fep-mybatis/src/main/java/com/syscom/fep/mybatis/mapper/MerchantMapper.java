package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Merchant;
import org.apache.ibatis.annotations.Param;

public interface MerchantMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MERCHANT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Merchant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MERCHANT
     *
     * @mbg.generated
     */
    int insert(Merchant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MERCHANT
     *
     * @mbg.generated
     */
    int insertSelective(Merchant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MERCHANT
     *
     * @mbg.generated
     */
    Merchant selectByPrimaryKey(@Param("merchantId") String merchantId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MERCHANT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Merchant record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MERCHANT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Merchant record);
}