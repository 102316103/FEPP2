package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Hkcif;
import javax.annotation.Resource;

@Resource
public interface HkcifMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    int insert(Hkcif record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    int insertSelective(Hkcif record);
}