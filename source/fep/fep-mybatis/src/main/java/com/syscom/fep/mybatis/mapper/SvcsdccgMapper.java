package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Svcsdccg;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface SvcsdccgMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCG
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Svcsdccg record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCG
     *
     * @mbg.generated
     */
    int insert(Svcsdccg record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCG
     *
     * @mbg.generated
     */
    int insertSelective(Svcsdccg record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCG
     *
     * @mbg.generated
     */
    Svcsdccg selectByPrimaryKey(@Param("seqno") Long seqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCG
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Svcsdccg record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCG
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Svcsdccg record);
}