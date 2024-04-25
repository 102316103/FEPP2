package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.MFTData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TaskExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.text.mft.MFTGeneralRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 處理主機通知FEP電文
 */
public class MFTNotice extends INBKAABase {
	private String MSGID;
	private MFTGeneralRequest mftGeneralRequest = getFiscBusiness().getMftData().getTxMFTObject().getRequest();

	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
	private BatchExtMapper batchExtMapper = SpringBeanFactoryUtil.getBean(BatchExtMapper.class);
	private TaskExtMapper taskExtMapper = SpringBeanFactoryUtil.getBean(TaskExtMapper.class);

	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	private boolean isEC = false;

	public MFTNotice(MFTData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * <summary>
	 * ''' 程式進入點
	 * ''' </summary>
	 * ''' <returns>Response電文</returns>
	 * ''' <remarks></remarks>
	 */
	@Override
	public String processRequestData() {
		Zone zone =zoneExtMapper.selectByPrimaryKey("TWN");
		BatchJobLibrary batchLib = new BatchJobLibrary();
		String batchName="";
		List<Batch> dt;
		List<Task> list;
		Task task = new Task();
		String fileName= "";
		Map<String, String> arguments = new HashMap<>();
		try {
			MSGID = mftGeneralRequest.getTRAN_CODE()+mftGeneralRequest.getTD_CODE();
			switch (MSGID){
				case "IBAF5102":  /* 財金結帳通知(5102) */
					if(zone.getZoneTbsdy().compareTo(SysStatus.getPropertyValue().getSysstatTbsdyFisc()) <0
							&& DbHelper.toBoolean(zone.getZoneChgday())){
						_rtnCode = getFiscBusiness().ChangeCBSDate(zone);
					}
					break;
				case "IPUFRCV1":  /* 全國繳費整批轉即時檔案 */
					batchName="NPSBatchInOnlineOut";
					dt = batchExtMapper.queryBatchByName(batchName);
					list = taskExtMapper.getTaskByName("NPSBatchInOnlineOut","ASC");
					task.setTaskId(list.get(0).getTaskId());
					fileName= mftGeneralRequest.getFILENAME();

					task.setTaskCommandargs("/FILEID:"+fileName);
					task.setTaskName(null);
					task.setTaskCommand(null);
					taskExtMapper.updateByPrimaryKeySelective(task);
					arguments.put("FILEID",fileName);
					batchLib.setArguments(arguments);
					batchLib.startBatch(
							dt.get(0).getBatchExecuteHostName(),
							dt.get(0).getBatchBatchid().toString(),
							dt.get(0).getBatchStartjobid().toString());
					break;
				case "IBAFRCV1":
					switch (mftGeneralRequest.getTYPE()){
						case "2":
							batchName="ImportNPSUNIT";
							dt = batchExtMapper.queryBatchByName(batchName);
							list = taskExtMapper.getTaskByName("ImportNPSUNIT","ASC");
							task.setTaskId(list.get(0).getTaskId());
							fileName= mftGeneralRequest.getFILENAME().trim();

							task.setTaskCommandargs("/FILEID:"+fileName);
							task.setTaskName(null);
							task.setTaskCommand(null);
							taskExtMapper.updateByPrimaryKeySelective(task);
							arguments.put("FILEID",fileName);
							batchLib.setArguments(arguments);
							batchLib.startBatch(
									dt.get(0).getBatchExecuteHostName(),
									dt.get(0).getBatchBatchid().toString(),
									dt.get(0).getBatchStartjobid().toString());
							break;
						case "3":
							batchName="ImportMERCHANT";
							dt = batchExtMapper.queryBatchByName(batchName);
							list = taskExtMapper.getTaskByName("ImportMERCHANT","ASC");
							task.setTaskId(list.get(0).getTaskId());
							fileName= mftGeneralRequest.getFILENAME().trim();

							task.setTaskCommandargs("/FILEID:"+fileName);
							task.setTaskName(null);
							task.setTaskCommand(null);
							taskExtMapper.updateByPrimaryKeySelective(task);
							arguments.put("FILEID",fileName);
							batchLib.setArguments(arguments);
							batchLib.startBatch(
									dt.get(0).getBatchExecuteHostName(),
									dt.get(0).getBatchBatchid().toString(),
									dt.get(0).getBatchStartjobid().toString());
							break;
						case "4":
							batchName="ImportUPBIN";
							dt = batchExtMapper.queryBatchByName(batchName);
							list = taskExtMapper.getTaskByName("ImportUPBIN","ASC");
							task.setTaskId(list.get(0).getTaskId());
							fileName= mftGeneralRequest.getFILENAME().trim();

							task.setTaskCommandargs("/FILEID:"+fileName);
							task.setTaskName(null);
							task.setTaskCommand(null);
							taskExtMapper.updateByPrimaryKeySelective(task);
							arguments.put("FILEID",fileName);
							batchLib.setArguments(arguments);
							batchLib.startBatch(
									dt.get(0).getBatchExecuteHostName(),
									dt.get(0).getBatchBatchid().toString(),
									dt.get(0).getBatchStartjobid().toString());
							break;
					}
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}
}
