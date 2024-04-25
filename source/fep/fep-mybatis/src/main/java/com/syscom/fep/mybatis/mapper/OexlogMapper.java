package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Oexlog;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface OexlogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Oexlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    int insert(Oexlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    int insertSelective(Oexlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    Oexlog selectByPrimaryKey(@Param("oexlogTxDate") String oexlogTxDate, @Param("oexlogEjfno") Integer oexlogEjfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Oexlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Oexlog record);
}