package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmmonMapper;
import com.syscom.fep.mybatis.model.Clrtotal;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface RmmonExtMapper extends RmmonMapper {

    List<HashMap<String,Object>> getT24Pending();
}
