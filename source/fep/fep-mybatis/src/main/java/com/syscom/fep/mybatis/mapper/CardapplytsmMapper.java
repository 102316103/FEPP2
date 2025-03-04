package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Cardapplytsm;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface CardapplytsmMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDAPPLYTSM
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Cardapplytsm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDAPPLYTSM
     *
     * @mbg.generated
     */
    int insert(Cardapplytsm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDAPPLYTSM
     *
     * @mbg.generated
     */
    int insertSelective(Cardapplytsm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDAPPLYTSM
     *
     * @mbg.generated
     */
    Cardapplytsm selectByPrimaryKey(@Param("txDate") String txDate, @Param("ejfno") Integer ejfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDAPPLYTSM
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Cardapplytsm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDAPPLYTSM
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Cardapplytsm record);
}