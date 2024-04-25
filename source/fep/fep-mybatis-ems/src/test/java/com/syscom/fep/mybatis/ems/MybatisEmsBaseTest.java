package com.syscom.fep.mybatis.ems;

import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.Rollback;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(true)
@ImportAutoConfiguration({
		RefreshAutoConfiguration.class,
		EnableEncryptablePropertiesConfiguration.class,
		JacksonAutoConfiguration.class
})
public class MybatisEmsBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();
}
