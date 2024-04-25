package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Npscust;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface NpscustMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSCUST
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Npscust record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSCUST
     *
     * @mbg.generated
     */
    int insert(Npscust record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSCUST
     *
     * @mbg.generated
     */
    int insertSelective(Npscust record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSCUST
     *
     * @mbg.generated
     */
    Npscust selectByPrimaryKey(@Param("npscustUnitno") String npscustUnitno, @Param("npscustPaytype") String npscustPaytype, @Param("npscustFeeno") String npscustFeeno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSCUST
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Npscust record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSCUST
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Npscust record);
}