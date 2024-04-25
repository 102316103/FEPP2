package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class CompanyExtMapperTest extends MybatisBaseTest {

	@Autowired
	private CompanyExtMapper mapper;
	
	@Test
	public void testGetCompanyByBranch() {
		mapper.getCompanyByBranch("186");
	}

}
