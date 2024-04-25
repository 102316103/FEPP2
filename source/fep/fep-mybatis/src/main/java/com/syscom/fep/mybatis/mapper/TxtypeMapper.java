package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Txtype;
import org.apache.ibatis.annotations.Param;

public interface TxtypeMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXTYPE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Txtype record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXTYPE
     *
     * @mbg.generated
     */
    int insert(Txtype record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXTYPE
     *
     * @mbg.generated
     */
    int insertSelective(Txtype record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXTYPE
     *
     * @mbg.generated
     */
    Txtype selectByPrimaryKey(@Param("txtypeType1") String txtypeType1, @Param("txtypeType2") String txtypeType2);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXTYPE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Txtype record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXTYPE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Txtype record);
}