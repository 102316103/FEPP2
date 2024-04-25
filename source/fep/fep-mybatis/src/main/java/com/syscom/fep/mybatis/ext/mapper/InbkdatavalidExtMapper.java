package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.InbkdatavalidMapper;
import com.syscom.fep.mybatis.model.Inbkdatavalid;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface InbkdatavalidExtMapper extends InbkdatavalidMapper {
    int deleteINBKDATAVALID(@Param("inbkdatavalidDate") String inbkdatavalidDate,@Param("inbkdatavalidProgram") String inbkdatavalidProgram);
    List<Inbkdatavalid> select(@Param("inbkdatavalidDate") String inbkdatavalidDate,@Param("inbkdatavalidProgram") String inbkdatavalidProgram);

}
