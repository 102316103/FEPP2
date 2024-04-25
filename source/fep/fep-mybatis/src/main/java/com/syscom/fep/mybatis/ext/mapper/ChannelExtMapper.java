package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ChannelMapper;
import com.syscom.fep.mybatis.model.Channel;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface ChannelExtMapper extends ChannelMapper {
	/**
	 * ADD BY WJ 20210514
	 * 
	 * @param channelName
	 * @return
	 */
	List<Channel> selectByChannelName(@Param("channelName_S") String channelName_S);
	
	/**
	 * Bruce add 取得通道下拉選單
	 * @return
	 */
	public List<Channel> queryChannelOptions();
}