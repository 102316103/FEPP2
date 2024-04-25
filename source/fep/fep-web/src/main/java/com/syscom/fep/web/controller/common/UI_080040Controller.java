package com.syscom.fep.web.controller.common;

import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.common.UI_080040_Form;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.model.Syscomrole;
import com.syscom.safeaa.mybatis.model.Syscomroleculture;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUsers;
import com.syscom.safeaa.mybatis.vo.SyscomroleInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * For Safeaa
 *
 * @author ChenYang
 */
@Controller
public class UI_080040Controller extends BaseController {

    @Autowired
    private CommonService commonService;

    @PostMapping(value = "/common/UI_080040/select")
    @ResponseBody
    public UI_080040_Form selectData() {
        this.infoMessage("開始執行, 條件 = []");
        UI_080040_Form form = new UI_080040_Form();
        try {
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if(list==null){
                form.setMessage(MessageType.INFO,QueryNoData);
            }else{
                form.setDataList(list);
            }
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, programError);
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,QueryFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/insert")
    @ResponseBody
    public UI_080040_Form insertData(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            @SuppressWarnings("unused")

            Syscomrole role = new Syscomrole();
            role.setRoleno(form.getNo());
            role.setRoletype("1");
            role.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
            role.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
            role.setUpdatetime(Calendar.getInstance().getTime());
            role.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));

            Syscomroleculture roleculture  = new Syscomroleculture();
            roleculture.setRoleid(form.getId());
            roleculture.setRolename(form.getName());

            Integer roleId = commonService.getRoleIdByNo(form.getNo());
            if(roleId!=null){
                form.setMessage(MessageType.DANGER,"該角色編號已存在");
                return form;
            }

            boolean flag = commonService.insertRole(role,roleculture);
            if(!flag){
                form.setMessage(MessageType.DANGER,"角色新增失敗");
                return form;
            }

            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if(list==null){
                form.setMessage(MessageType.INFO,QueryNoData);
            }else{
                form.setDataList(list);
            }

            form.setMessage(MessageType.SUCCESS,InsertSuccess);
        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,InsertFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/updateRole")
    @ResponseBody
    public UI_080040_Form updateRole(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Syscomrole role = new Syscomrole();
            role.setRoleid(form.getId());
            role.setRoleno(form.getNo());
            role.setRoletype("1");
            role.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
            role.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
            role.setUpdatetime(Calendar.getInstance().getTime());
            role.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));

            Syscomroleculture roleculture  = new Syscomroleculture();
            roleculture.setRoleid(form.getId());
            roleculture.setRolename(form.getName());

            Integer roleId = commonService.getRoleIdByNo(form.getNo());
            if(roleId!=null && roleId.intValue()!=form.getId().intValue()){
                form.setMessage(MessageType.DANGER,"該角色編號已存在");
                return form;
            }

            boolean flag = commonService.updateRole(role,roleculture);
            if(!flag){
                form.setMessage(MessageType.DANGER,"角色修改失敗");
                return form;
            }

            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if(list==null){
                form.setMessage(MessageType.INFO,QueryNoData);
            }else{
                form.setDataList(list);
            }
            form.setMessage(MessageType.SUCCESS,UpdateSuccess);
        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,UpdateFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/updateUser")
    @ResponseBody
    public UI_080040_Form updateUser(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Syscomuser syscomuser = new Syscomuser();
            syscomuser.setUserid(Integer.valueOf(form.getId()));
            syscomuser.setLogonid(form.getNo());
            syscomuser.setUsername(form.getName());
            syscomuser.setEmailaddress(form.getUsermail());
            syscomuser.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
            syscomuser.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
            syscomuser.setUpdatetime(Calendar.getInstance().getTime());
            syscomuser.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));

            Fepuser fepuser = new Fepuser();
            fepuser.setFepuserUserid(form.getEmpid());
            fepuser.setFepuserLogonid(form.getNo());
            fepuser.setFepuserName(form.getName());
            fepuser.setUpdateTime(Calendar.getInstance().getTime());
            fepuser.setFepuserUserid(Integer.parseInt(WebUtil.getUser().getUserId()));

            boolean rst = commonService.updatetUser(syscomuser,fepuser);
            if(!rst){
                form.setMessage(MessageType.SUCCESS, UpdateFail);
                return form;
            }

            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if(list==null){
                form.setMessage(MessageType.INFO,QueryNoData);
            }else{
                form.setDataList(list);
                form.setMessage(MessageType.SUCCESS,UpdateSuccess);
            }

        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,UpdateFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/deleteRole")
    @ResponseBody
    public UI_080040_Form deleteData(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            boolean flag = commonService.deleteRole(form.getId());
            if(!flag){
                form.setMessage(MessageType.DANGER,DeleteFail);
                return form;
            }
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if(list==null){
                form.setMessage(MessageType.INFO,QueryNoData);
            }else{
                form.setDataList(list);
                form.setMessage(MessageType.SUCCESS,DeleteSuccess);
            }
        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,DeleteFail);
        }

        return form;
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/common/UI_080040/checkTreeNode")
    @ResponseBody
    public UI_080040_Form checkTreeNode(@RequestBody UI_080040_Form form) {
        List<Map<String,String>> allList = new ArrayList<>();

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<SyscomQueryAllUsers> infoList = commonService.getAllUsers();
            if(infoList!=null){
                for(SyscomQueryAllUsers user:infoList){
                    Map<String,String> hashMap = new HashMap<>();
                    hashMap.put("id",user.getUserId().toString());
                    hashMap.put("pNo",user.getLogonId());
                    hashMap.put("name",user.getUserName());
                    hashMap.put("startDate",simpleDateFormat.format(user.getEffectDate()));
                    hashMap.put("endDate",simpleDateFormat.format(user.getExpiredDate()));
                    allList.add(hashMap);
                }
                form.setAllList(allList);
            }

            List<SyscomrolemembersAndCulture> selecteList = commonService.getSelectedUserMembersById(form.getId());
            form.setSelectList(selecteList);

            return form;
        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,QueryFail);
        }
        return form;
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/common/UI_080040/queryClick")
    @ResponseBody
    public UI_080040_Form queryClick(@RequestBody UI_080040_Form form) {
        try{
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            boolean flg = commonService.deleteAllRoleMembers(form.getPid());
//            if(flg){
                if(form.getIds()!=null){
                    for(int i=0;i<form.getIds().size();i++){
                        Syscomrolemembers member = new Syscomrolemembers();
                        member.setRoleid(form.getPid());
                        member.setChildid(form.getIds().get(i));
                        member.setChildtype("U");
                        member.setEffectdate(sdf1.parse(sdf2.format(new Date()) + " 00:00:00"));
                        member.setExpireddate(sdf1.parse("2039-12-31 00:00:00"));
                        member.setUpdatetime(Calendar.getInstance().getTime());
                        member.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                        commonService.insertRoleMember(member);
                    }
                }
//            }

            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if(list==null){
                form.setMessage(MessageType.INFO,QueryNoData);
            }else{
                form.setDataList(list);
                form.setMessage(MessageType.SUCCESS,UpdateSuccess);
            }

        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,UpdateFail);
        }
        return form;

    }

}
