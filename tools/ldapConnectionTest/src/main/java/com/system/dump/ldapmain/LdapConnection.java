package com.system.dump.ldapmain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LdapConnection {
  public static void main(String[] args) {
		try {
			StringBuffer str = new StringBuffer();
			
			Properties properties = new Properties();
			BufferedReader bufferedReader = new BufferedReader(new FileReader("./ldapConnection.properties"));
			properties.load(bufferedReader);
			String userid = properties.getProperty("userid");
			String pwd = properties.getProperty("pwd");
			String webaddress = properties.getProperty("webaddress");
			String username = properties.getProperty("username");
			String userinname = properties.getProperty("userinname");
			String chkid = properties.getProperty("chkid");
			String unitid = properties.getProperty("unitid");
			String strtemp = properties.getProperty("strtemp");
			String principal = properties.getProperty("principal");
			
			System.out.println("LDAP Program Start");
			System.out.println("");
			System.out.println("===================LDAP Init Constructor Start===================================");
//			AdConnect adConnect = new AdConnect(args[0],args[1],args[2]);
			AdConnect adConnect = new AdConnect(userid,pwd,webaddress,strtemp,principal);
			System.out.println("===================LDAP Init Constructor End ANd Success!========================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info Start==============");
			System.out.println("Method Name : getUserName, return String");
			
			//此為UserInfo的使用者值
			String description = adConnect.getUserName(username);
			
			System.out.println("getUserName description :" + description);
			str.append("UserName description:" + "\r\n"+ description + "\r\n");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info End================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info Start==============");
			System.out.println("Method Name : getUserInfo, return Map<String , Object>");
			
			//此為UserInfo的使用者值
			Map<String ,Object> userMap = adConnect.getUserInfo();
			
			System.out.println("getUserInfo userMap :" + userMap);
			str.append("UserInfo userMap: " + "\r\n"+ userMap + "\r\n");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info End================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info Start==============");
			System.out.println("Method Name : getUserinname, return Map<String , Object>");
			
			//此為UserInfo的使用者值
			userMap = adConnect.getUserinname(userinname);
			
			System.out.println("getUserinname userMap :" + userMap);
			str.append("Userinname: " + "\r\n"+ userMap + "\r\n");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info End================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Get Sector Info Start==============");
			System.out.println("Method Name : getallunits, return List<Map<String , Object>>");
			
			//取得所有部門資料
			List<Map<String,Object>> sectorList = adConnect.getallunits();
			
			if(sectorList != null && sectorList.size() > 0) {
				System.out.println("getallunits sectorList :" + sectorList.get(0));
				str.append("allunit: " + "\r\n"+ sectorList.get(0) + "\r\n");
			}
			System.out.println("=============Prepare Create LDAP Connection And Get Sector Info End================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Check if you are a bank clerk Start==============");
			System.out.println("Method Name : getchkid, return boolean");
			
			//檢查身份證是否為銀行行員
			boolean  bankClerk = adConnect.getchkid(chkid);
			
			System.out.println("getchkid bankClerk :" + bankClerk);
			str.append("isbankClerk: " + "\r\n"+ bankClerk + "\r\n");
			System.out.println("=============Prepare Create LDAP Connection And Check if you are a bank clerk End================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Get User Info Start==============");
			System.out.println("Method Name : getUserGroups(), return List<Map<String , Object>>");
			//使用者群組
			List<String> userList= adConnect.getUserGroups();
			if(userList != null && userList.size() > 0) {
				System.out.println("getUserGroups userList :" + userList.get(0));
				str.append("UserGroups: " + "\r\n"+ userList.get(0) + "\r\n");
			}
			System.out.println("=============Prepare Create LDAP Connection And Get User Info End================");
			System.out.println("");
			System.out.println("=============Prepare Create LDAP Connection And Get Sector Mail Start==============");
			System.out.println("Method Name : getUnitUserMail(), return List<Map<String , Object>>");
			
			//獲取部門Mail資料
			List<String> sectorMailList= adConnect.getUnitUserMail(unitid);
			
			if(sectorMailList != null && sectorMailList.size() > 0) {
				System.out.println("getUserGroups sectorMailList :" + sectorMailList.get(0));
				str.append("UserGroups sectorMailList: " + "\r\n"+ sectorMailList.get(0) + "\r\n");
			}
			System.out.println("=============Prepare Create LDAP Connection And Get Sector Mail End================");
		    FileOutputStream fos = new FileOutputStream("./" + File.separator + "ldapConnection.txt");
		    fos.write(str.toString().getBytes("UTF-8"));
		    fos.close();
		} catch (Exception e) {
			System.out.println("=============LDAP Faild Exception Start==============");
			e.printStackTrace();
			System.out.println(e);
			System.out.println("=============LDAP Faild Exception End==============");
		}
  }
}
