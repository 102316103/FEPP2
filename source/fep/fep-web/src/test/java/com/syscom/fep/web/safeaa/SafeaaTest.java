package com.syscom.fep.web.safeaa;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.WebBaseTest;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;
import com.syscom.safeaa.security.Resource;
import com.syscom.safeaa.security.User;

public class SafeaaTest extends WebBaseTest {
	@Autowired
	private User user;
	@Autowired
	private Resource resource;

	@Test
	public void testCreateUser() throws Exception {
		Syscomuser u = new Syscomuser();
		u.setBirthday("19830522");
		u.setEffectdate(Calendar.getInstance().getTime());
		u.setEmailaddress("myfifa2005@qq.com");
		u.setEmpid(161);
		u.setEmployeeid("B00161");
		u.setExpireddate(FormatUtil.parseDataTime("9999-12-31", "yyyy-MM-dd"));
		u.setIdno("00000000000");
		u.setLogAuditTrail(true);
		u.setLogonid("B00161");
		u.setUpdatetime(Calendar.getInstance().getTime());
		u.setUpdateUser(161);
		u.setUpdateuserid(161);
		// u.setUserid(161L);
		u.setUsername("Richard");
		UnitTestLogger.info(user.createUser(u, 0, "Hello", "Hi", "1qaz@WSX"));

		UnitTestLogger.info(user.getAllUsers(null));
	}

	@Test
	public void testGetResourceDataByNo() {
		try {
			List<SyscomresourceAndCulture> list = resource.getResourceDataByNo("019302", "zh-TW");
			if (CollectionUtils.isNotEmpty(list)) {
				list.forEach(t -> {
					{
						UnitTestLogger.info(ReflectionToStringBuilder.toString(t, ToStringStyle.SHORT_PREFIX_STYLE));
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
