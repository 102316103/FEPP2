package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Fcrmsfp;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface FcrmsfpMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSFP
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Fcrmsfp record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSFP
     *
     * @mbg.generated
     */
    int insert(Fcrmsfp record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSFP
     *
     * @mbg.generated
     */
    int insertSelective(Fcrmsfp record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSFP
     *
     * @mbg.generated
     */
    Fcrmsfp selectByPrimaryKey(@Param("fcrmsfpId") String fcrmsfpId, @Param("fcrmsfpChargetype") String fcrmsfpChargetype, @Param("fcrmsfpBrno") String fcrmsfpBrno, @Param("fcrmsfpCurrency") String fcrmsfpCurrency, @Param("fcrmsfpEffectdate") String fcrmsfpEffectdate);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSFP
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Fcrmsfp record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSFP
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Fcrmsfp record);
}