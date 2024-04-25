package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Ibtid;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface IbtidMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table IBTID
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Ibtid record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table IBTID
     *
     * @mbg.generated
     */
    int insert(Ibtid record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table IBTID
     *
     * @mbg.generated
     */
    int insertSelective(Ibtid record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table IBTID
     *
     * @mbg.generated
     */
    Ibtid selectByPrimaryKey(@Param("ibtidIdno") String ibtidIdno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table IBTID
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Ibtid record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table IBTID
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Ibtid record);
}