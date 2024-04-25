package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MerchantMapper;
import org.apache.ibatis.annotations.Param;
import javax.annotation.Resource;
import com.syscom.fep.mybatis.model.Merchant;

import java.util.List;

@Resource
public interface MerchantExtMapper extends MerchantMapper {

	void truncateMERCHANT();

}