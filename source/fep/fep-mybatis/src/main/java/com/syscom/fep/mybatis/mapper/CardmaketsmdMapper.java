package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Cardmaketsmd;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface CardmaketsmdMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Cardmaketsmd record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    int insert(Cardmaketsmd record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    int insertSelective(Cardmaketsmd record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    Cardmaketsmd selectByPrimaryKey(@Param("txdate") String txdate, @Param("batchseq") Integer batchseq, @Param("seqno") Integer seqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Cardmaketsmd record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Cardmaketsmd record);
}