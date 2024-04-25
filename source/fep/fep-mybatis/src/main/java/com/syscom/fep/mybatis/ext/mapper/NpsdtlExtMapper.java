package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NpsdtlMapper;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.mybatis.model.Odrc;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Resource
public interface NpsdtlExtMapper extends NpsdtlMapper {
    /**
     *返回詳單
     * @param batno
     * @return
     */
    List<HashMap<String,Object>> showDetail(@Param("batno")String batno);
    int deleteByBATNO(@Param("batno")String batno);

    List<Npsdtl> GetNPSDTLByBATNO(@Param("batno")String batno,@Param("threadNo")Integer threadNo);
    List<Npsdtl> GetNPSDTLByBATNOforAll(@Param("batno")String batno);

    Map<String, Object> getNPSDTLTotalCNTAMT(Npsdtl record);

}