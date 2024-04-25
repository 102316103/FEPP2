package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Clrdtltxn;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface ClrdtltxnMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Clrdtltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    int insert(Clrdtltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    int insertSelective(Clrdtltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    Clrdtltxn selectByPrimaryKey(@Param("clrdtltxnTxdate") String clrdtltxnTxdate, @Param("clrdtltxnEjfno") Integer clrdtltxnEjfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Clrdtltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Clrdtltxn record);
}