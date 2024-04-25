package com.syscom.safeaa.security.impl;

import java.text.DecimalFormat;

import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.syscom.safeaa.mybatis.extmapper.SyscomserialExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomserial;
import com.syscom.safeaa.security.Serial;

/**
 * 
 * @author syscom
 *
 */
@Component
public class SerialImpl implements Serial {

	@Autowired
	private SyscomserialExtMapper serialMapper;
	
	@Override
	public Long getNextId(String serialName) {		
		return getNextId(serialName, "", "", 0);
	}
	
	@Override
	public Long getNextId(String serialName,String resetValue) {		
		return getNextId(serialName, "", resetValue, 0);
	}
	
	@Override
	public Long getNextId(String serialName,Integer interval) {		
		return getNextId(serialName, "", "", interval);
	}
	
	@Override
	public Long getNextId(String serialName,String resetValue,Integer interval) {		
		return getNextId(serialName, "", resetValue, interval);
	}
		
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	private Long getNextId(String serialname,String numberformat,String resetvalue,Integer interval) {
		Syscomserial syscomserial = serialMapper.selectByPrimaryKey(serialname);
		if(syscomserial==null) {
			syscomserial = new Syscomserial();
			syscomserial.setSerialname(serialname);
			syscomserial.setNumberformat(numberformat);
			syscomserial.setResetfield(resetvalue);
			syscomserial.setNextid(1L);
			serialMapper.insert(syscomserial);
			return 1L;
		}else {
			if(syscomserial.getNextid()!=null) {
				syscomserial.setNextid(syscomserial.getNextid() + 1);
				syscomserial.setNumberformat(numberformat);
				syscomserial.setInterval(interval);
				syscomserial.setResetfield(resetvalue);
			}else {
				syscomserial.setNextid(1L);
			}
			
			serialMapper.updateByPrimaryKey(syscomserial);
			return syscomserial.getNextid().longValue();
		}
		
	}
	
	@Override
	public void resetId(String serialName) throws Exception {
		try {
			serialMapper.resetIdBySerialName(serialName);
		}catch(Exception e) {
			throw e;
		}
		
	}
	
	@Override
	public String getNextIdWithFormat(String serialName) {
	    String numberFormat = "";
	    Long number = getNextId(serialName,numberFormat,"",0);
	    DecimalFormat nf = new DecimalFormat(numberFormat);
		return nf.format(number);
	}
	
	
}
