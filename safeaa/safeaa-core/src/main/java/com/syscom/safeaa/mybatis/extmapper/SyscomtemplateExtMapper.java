package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomtemplateMapper;
import com.syscom.safeaa.mybatis.model.Syscomtemplate;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomtemplateExtMapper extends SyscomtemplateMapper {

    List<Syscomtemplate> queryTemplateIdByNo(@Param("templateNo") String templateNo);
}