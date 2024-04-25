package com.syscom.fep.web.controller.atmmon;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TxtypeExtMapper;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.mybatis.model.Txtype;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.atmmon.UI_060291_Form;
import com.syscom.fep.web.form.atmmon.UI_060291_Form_TreeData;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.ChannelService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * FEP 服務/通路/線路控制
 *
 * @author Alma
 */
@Controller
public class UI_060291Controller extends BaseController {

	@Autowired
	private TxtypeExtMapper txtypeExtMapper;
	@Autowired
	private MsgctlExtMapper msgctlExtMapper;
	@Autowired
	private ChannelService channelService;

	@Autowired
	private InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_060291_Form form = new UI_060291_Form();
		this.doKeepFormData(mode, form);
		getChannel(form,mode);
		getSysstat(form, mode);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/atmmon/UI_060291/select")
	@ResponseBody
	public UI_060291_Form initData(@RequestBody UI_060291_Form form, ModelMap mode){
		getTxType(form);
		getChannel(form, mode);
		getSysstat(form, mode);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return form;
	}
	@PostMapping(value = "/atmmon/UI_060291/confirm")
	@ResponseBody
	public BaseResp<UI_060291_Form> confirm(@RequestBody UI_060291_Form form, ModelMap mode){
		this.infoMessage("查詢主檔數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		BaseResp<UI_060291_Form> response = new BaseResp<>();
		try {
			UI_060291_Form data = new UI_060291_Form();
			List<Msgctl> updList = new ArrayList<>();
			getTxType(data);
			if (!CollectionUtils.isEmpty(data.getTreeData())){
				for (UI_060291_Form_TreeData treeData : data.getTreeData()){
					if (treeData.getTreeLevel() == 3){
						updList.add(treeData.getMsgctl().get(0));
					}
				}
			}

			for (Msgctl msgctl : updList){
				msgctl.setMsgctlStatus(DbHelper.toShort(false));
				for (UI_060291_Form_TreeData r : form.getTreeData()){
					if (  r.getTreeLevel() ==3 && r.getId().equals(msgctl.getMsgctlMsgid())){
						msgctl.setMsgctlStatus(DbHelper.toShort(r.isChecked()));
					}
				}
				msgctlExtMapper.updateByPrimaryKeySelective(msgctl);
			}


			//更新Channel
			for (Channel channel : form.getChannelList()){
				channel.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
				channel.setUpdateTime(new Date());
				channelService.updateChannel(channel);
			}

			//更新系統狀態
			Sysstat sysstat = inbkService.getStatus();
			sysstat.setSysstatCbs(form.getSysstat().getSysstatCbs());
			sysstat.setSysstatFcs(form.getSysstat().getSysstatFcs());
			sysstat.setSysstatCredit(form.getSysstat().getSysstatCredit());
			sysstat.setSysstatMtp(form.getSysstat().getSysstatMtp());
			sysstat.setSysstatTwmp(form.getSysstat().getSysstatTwmp());
			inbkService.UpdateSYSSTAT(sysstat);

			response.setData(form);
			response.setMessage(MessageType.SUCCESS, UpdateSuccess);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} catch (Exception e) {
			e.printStackTrace();
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
			response.setMessage(MessageType.DANGER, e.getMessage());
			LogData logContext = new LogData();
			logContext.setRemark(e+"");
			logContext.setMessageId("UI_060291");
			logContext.setMessageGroup("1");
			logContext.setProgramName("UI_060291");
			logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			logContext.setChannel(FEPChannel.FEP);
			inbkService.inbkLogMessage(Level.INFO, logContext);
		}
		return response;
	}

	/**
	 * Mapping TxType與Msgctl資料
	 * @param form
	 */
	private void getTxType(UI_060291_Form form){

		List<Msgctl> msgctlList = msgctlExtMapper.selectAllTxTypeMsgctl();
		List<Txtype> txtypeList = txtypeExtMapper.getAllData();

		List<UI_060291_Form_TreeData> level1 = new ArrayList<>();
		List<Map<String,String>> _1List = txtypeExtMapper.getLevel1TxType();
		if (!CollectionUtils.isEmpty(_1List)){
			for (Map<String, String> map : _1List){
				UI_060291_Form_TreeData data = new UI_060291_Form_TreeData();
				data.setTxType1(Short.parseShort(map.get("TXTYPE_TYPE1")));
				data.setTxType1Name(map.get("TXTYPE_TYPE1_NAME"));
				data.setTreeLevel(1);
				level1.add(data);
			}
		}

		List<UI_060291_Form_TreeData> level2 = new ArrayList<>();
		List<Map<String,String>> _2List = txtypeExtMapper.getLevel2();
		if (!CollectionUtils.isEmpty(_2List)){
			for (Map<String, String> map : _2List){
				UI_060291_Form_TreeData data = new UI_060291_Form_TreeData();
				data.setTxType1(Short.parseShort(map.get("TXTYPE_TYPE1")));
				data.setTxType1Name(map.get("TXTYPE_TYPE1_NAME"));
				data.setTxType2(Short.parseShort(map.get("TXTYPE_TYPE2")));
				data.setTxType2Name(map.get("TXTYPE_TYPE2_NAME"));
				Txtype n = txtypeList.stream().filter(new Predicate<Txtype>() {
					@Override
					public boolean test(Txtype txtype) {
						if ( null != txtype.getTxtypeType1() && null != txtype.getTxtypeType2()
							&& txtype.getTxtypeType1().equals(String.valueOf(data.getTxType1())) && txtype.getTxtypeType2().equals(String.valueOf(data.getTxType2()))
						){
							return true;
						}
						return false;
					}
				}).findFirst().get();
				data.setTxtype(n);
				List<Msgctl> m  = msgctlList.stream().filter(new Predicate<Msgctl>() {
					@Override
					public boolean test(Msgctl msgctl) {
						if (data.getTxType1() == msgctl.getMsgctlTxtype1() && data.getTxType2() == msgctl.getMsgctlTxtype2()){
							return true;
						}
						return false;
					}
				}).collect(Collectors.toList());
				data.setMsgctl(m);
				data.setTreeLevel(2);
				level2.add(data);
			}
		}

		List<UI_060291_Form_TreeData> level3 = new ArrayList<>();
		if (!CollectionUtils.isEmpty(level2)){
			for (UI_060291_Form_TreeData treeData : level2){
				if (!CollectionUtils.isEmpty(treeData.getMsgctl())){
					for (Msgctl msgctl : treeData.getMsgctl()){
						UI_060291_Form_TreeData data = new UI_060291_Form_TreeData();
						List<Msgctl> msgctls = new ArrayList<>();
						msgctls.add(msgctl);
						data.setMsgctl(msgctls);
						data.setTxType1(treeData.getTxType1());
						data.setTxType1Name(treeData.getTxType1Name());
						data.setTxType2(treeData.getTxType2());
						data.setTxType2Name(treeData.getTxType2Name());
						data.setTreeLevel(3);
						data.setChecked(DbHelper.toBoolean(msgctl.getMsgctlStatus()));
						if (data.isChecked()){
							level2.stream().filter(r -> {
								if ( r.getTxType1() == data.getTxType1()
										&& r.getTxType2() == data.getTxType2()){
									return true;
								}
								return false;
							}).findFirst().get().setChecked(true);
							level1.stream().filter(r->r.getTxType1().equals(data.getTxType1())).findFirst().get().setChecked(true);

						}
						level3.add(data);
					}
				}
			}
		}
		form.setTreeData(level1);
		form.getTreeData().addAll(level2);
		form.getTreeData().addAll(level3);
	}

	/**
	 * 取得通道
	 * @param form
	 * @param mode
	 */
	private void getChannel(UI_060291_Form form,ModelMap mode) {
		try {
			List<Channel> list = channelService.queryAllDataAsc();
			if (!CollectionUtils.isEmpty(list)){
				form.setChannelList(list);
			}
		}catch (Exception ex){
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	/**
	 * 取得系統狀態
	 * @param form
	 * @param mode
	 */
	private void getSysstat(UI_060291_Form form,ModelMap mode) {
		try {
			form.setSysstat(inbkService.getStatus());
		}catch (Exception ex){
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
}
