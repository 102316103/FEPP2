package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class QrptxnExtMapperTest extends MybatisBaseTest {
	
	@Autowired
	private QrptxnExtMapper mapper;
	
	@Test
	public void testgetQrptxnByIcSeqno() {
		mapper.getQrptxnByIcSeqno("11", "22");
	}

}
