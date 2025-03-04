package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Tsmreference;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface TsmreferenceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TSMREFERENCE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Tsmreference record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TSMREFERENCE
     *
     * @mbg.generated
     */
    int insert(Tsmreference record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TSMREFERENCE
     *
     * @mbg.generated
     */
    int insertSelective(Tsmreference record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TSMREFERENCE
     *
     * @mbg.generated
     */
    Tsmreference selectByPrimaryKey(@Param("reference") String reference, @Param("referenceowner") String referenceowner);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TSMREFERENCE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Tsmreference record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TSMREFERENCE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Tsmreference record);
}