package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Mowkpostdtl;
import java.util.Date;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface MowkpostdtlMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTDTL
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Mowkpostdtl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTDTL
     *
     * @mbg.generated
     */
    int insert(Mowkpostdtl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTDTL
     *
     * @mbg.generated
     */
    int insertSelective(Mowkpostdtl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTDTL
     *
     * @mbg.generated
     */
    Mowkpostdtl selectByPrimaryKey(@Param("syscode") String syscode, @Param("txdate") Date txdate, @Param("seqno") Integer seqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTDTL
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Mowkpostdtl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTDTL
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Mowkpostdtl record);
}