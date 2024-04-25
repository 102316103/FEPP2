package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.form.BaseForm;

import java.util.List;

public class UI_060291_Form extends BaseForm {

	private static final long serialVersionUID = 1L;

	private List<UI_060291_Form_TreeData> treeData;

	private String[] checkList;

	private List<Channel> channelList;

	private Sysstat sysstat;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<UI_060291_Form_TreeData> getTreeData() {
		return treeData;
	}

	public void setTreeData(List<UI_060291_Form_TreeData> treeData) {
		this.treeData = treeData;
	}

	public String[] getCheckList() {
		return checkList;
	}

	public void setCheckList(String[] checkList) {
		this.checkList = checkList;
	}

	public List<Channel> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<Channel> channelList) {
		this.channelList = channelList;
	}

	public Sysstat getSysstat() {
		return sysstat;
	}

	public void setSysstat(Sysstat sysstat) {
		this.sysstat = sysstat;
	}
}
