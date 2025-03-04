package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.T24atmcob;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface T24atmcobMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T24ATMCOB
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(T24atmcob record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T24ATMCOB
     *
     * @mbg.generated
     */
    int insert(T24atmcob record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T24ATMCOB
     *
     * @mbg.generated
     */
    int insertSelective(T24atmcob record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T24ATMCOB
     *
     * @mbg.generated
     */
    T24atmcob selectByPrimaryKey(@Param("id") String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T24ATMCOB
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(T24atmcob record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T24ATMCOB
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(T24atmcob record);
}