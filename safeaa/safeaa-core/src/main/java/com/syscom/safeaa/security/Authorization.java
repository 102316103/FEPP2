package com.syscom.safeaa.security;

import com.syscom.safeaa.enums.EnumActionType;

/**
 * Class of processing user menu and all authorization events
 * 
 * <p>功能選單及授權的類別。
 * </p>
 * @author David Tai
 * @author Chenyang
 *
 */

public interface Authorization {
	
	/**
	 * Check user logon.
	 * <p> 根據SAFE定義的功能控制清單檢查對應的Button是否Enable</p>
	 *    
	 * <p>預設值為Disable，需檢查功能控制清單長度是否符合SAFE定義 </p>
	 * <li>example:
	 *  com.syscom.safeaa.security.Authorization auth = new Authorization();
	 *  try{
	 *     String functionList = "11111111111111";
	 *     EnumActionType actionType = "1234";
	 *     int button.Enable = auth.checkButtonEnable(functionList, actionType.Add);
	 *  }catch(Exception e){
	 *      throw e;
	 *  }
	 * 
	 * @param functionList SAFE defined button list
	 * @param actionType action type for each button
	 * @return True: function button enable, False: function button disable
	 */

	public boolean checkButtonEnable(final String functionList, final EnumActionType actionType ) ;
		
	
	
	/**
	 * Generate XML of menu control for user login
	 * 
	 * <p>產生MENU CONTROL所需的XM</p>
	 * @param userId  user sequence no
	 * @param culture culture code
	 * @param rootGroup root node for search tree
	 * @return  XML for menu control
	 */ 
	public String getMenuXml(final long userId, final String culture, final String rootGroup) throws Exception;	

}
