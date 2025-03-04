package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Msgout;
import org.apache.ibatis.annotations.Param;

public interface MsgoutMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MSGOUT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Msgout record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MSGOUT
     *
     * @mbg.generated
     */
    int insert(Msgout record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MSGOUT
     *
     * @mbg.generated
     */
    int insertSelective(Msgout record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MSGOUT
     *
     * @mbg.generated
     */
    Msgout selectByPrimaryKey(@Param("msgoutTxdate") String msgoutTxdate, @Param("msgoutBrno") String msgoutBrno, @Param("msgoutFepno") String msgoutFepno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MSGOUT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Msgout record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MSGOUT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Msgout record);
}