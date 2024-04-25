package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.NpsbatchMapper;
import com.syscom.fep.mybatis.model.Npsbatch;

@Resource
public interface NpsbatchExtMapper extends NpsbatchMapper {

    /**
     * xy 2021/8/26 add
     * @param fileid
     * @param txdate
     * @return
     */
    List<Npsbatch> queryNPSBATCH(@Param("fileid")String fileid, @Param("txdate")String txdate);


}
