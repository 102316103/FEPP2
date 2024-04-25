package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class ObtltxnExtMapperTest extends MybatisBaseTest {

	@Autowired
	private ObtltxnExtMapper mapper;
	
	@Test
	public void test() {
        mapper.getQrptxnByIcSeqno("11", "22", "33", "44", "55");
	}
	
	@Test
	public void queryOBTLXNByStan() {
		mapper.queryOBTLXNByStan("11", "22", "33");
	}

}
