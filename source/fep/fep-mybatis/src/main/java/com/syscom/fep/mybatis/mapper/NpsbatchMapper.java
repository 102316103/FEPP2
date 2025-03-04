package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Npsbatch;
import org.apache.ibatis.annotations.Param;

public interface NpsbatchMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSBATCH
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Npsbatch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSBATCH
     *
     * @mbg.generated
     */
    int insert(Npsbatch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSBATCH
     *
     * @mbg.generated
     */
    int insertSelective(Npsbatch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSBATCH
     *
     * @mbg.generated
     */
    Npsbatch selectByPrimaryKey(@Param("npsbatchFileId") String npsbatchFileId, @Param("npsbatchTxDate") String npsbatchTxDate, @Param("npsbatchBatchNo") String npsbatchBatchNo);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSBATCH
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Npsbatch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NPSBATCH
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Npsbatch record);
}