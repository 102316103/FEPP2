package com.syscom.safeaa.mybatis.mapper;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.extmapper.SyscombasemessagecultureExtMapper;
import com.syscom.safeaa.mybatis.model.Syscombasemessageculture;

public class SyscombasemessagecultureMapperTest extends BaseTest {

	@Autowired
	private SyscombasemessagecultureExtMapper mapper;
	
	@Test
	public void selectByMessageIdAndCultureTest() {
		Syscombasemessageculture syscombasemessageculture = mapper.selectByMessageIdAndCulture("0067", "zh-TW");
		assertNotNull(syscombasemessageculture);
		assertNotNull(syscombasemessageculture.getMessagetext());
	}
}
