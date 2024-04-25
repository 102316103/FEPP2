package com.syscom.safeaa.security;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MenuTree extends ApplicationBase {

    private Document mXmlDocument; //root document
    private long groupId;

    public Document getmXmlDocument() {
        return mXmlDocument;
    }

    public long getGroupId() {
        return groupId;
    }

    @SuppressWarnings("unused")
    public MenuTree() {
        mXmlDocument = DocumentHelper.createDocument();
        Element rootElement = mXmlDocument.addElement("root");
        groupId = 0;
    }


    public MenuTree(SyscomgroupmembersAndGroupLevel rootGroup) {
        mXmlDocument = DocumentHelper.createDocument();
        Element rootElement = mXmlDocument.addElement("root");
        //TODO , need to read common menu from Cache
        // set root group Enable
        addGroupNode(rootElement, rootGroup, "TRUE");
        this.groupId = rootGroup.getGroupid();
    }


    public void generateMenuTree(List<SyscomgroupmembersAndGroupLevel> dtGroup) {
        _log.debug("load menu from db");

        for (SyscomgroupmembersAndGroupLevel group : dtGroup) {
            addNode(group);
        }
//		System.out.println(toString());
    }

    public void addRoleGroupResource(List<Syscomrolegroup> dtRoleGroup) {
        _log.debug("add RoleGroup resource");

        if (dtRoleGroup == null || dtRoleGroup.size() == 0) {
            return;
        }
        for (Syscomrolegroup item : dtRoleGroup) {
            switch (item.getChildtype()) {
                case CommonConstants.ChildTypeGroup:
                    enableTreeGroup(item, "TRUE");
                    break;
                case CommonConstants.ChildTypeResource:
                    enableTreeResource(item, "TRUE");
                    break;
            }
        }
    }

    public void denyRoleGroupResource(List<Syscomrolemembers> dtRoleDeny) {
        _log.debug("add RoleGroup resource");

        if (dtRoleDeny == null || dtRoleDeny.size() == 0) {
            return;
        }
        for (Syscomrolemembers item : dtRoleDeny) {
            switch (item.getChildtype()) {
                case CommonConstants.ChildTypeGroup:
                    disableTreeGroup(item, "FALSE");
                    break;
                case CommonConstants.ChildTypeResource:
                    disableTreeResource(item, "FALSE");
                    break;
            }
        }
    }

    private void enableTreeResource(final Syscomrolegroup groupItem, final String flag) {

        String resourceStr = "//Program[@ProgramId='" + groupItem.getGroupid().toString() + "']";
        Node resourceNode = mXmlDocument.selectSingleNode(resourceStr);
        if (null == resourceNode) {
            return;
        }

        Element resource = (Element) resourceNode;
        //resource.addAttribute("Enable", "TRUE");
        resource.addAttribute("Enable", flag);
        if ("TRUE".equalsIgnoreCase(flag)) {
            if (!StringUtils.isBlank(groupItem.getSafedefinefunctionlist())) {
                resource.addAttribute("SafeDefineFunctionList", groupItem.getSafedefinefunctionlist());
                resource.addAttribute("UserDefineFunctionList", groupItem.getUserdefinefunctionlist());

            }
        }

        // update by ashiang:若Resource Enable,則設定上層Group Enable
        while (true) {
            Element parent = resource.getParent();
            if (parent == null) {
                break;
            }
            if (parent.getName().equalsIgnoreCase("root")) {
                break;
            }
            parent.addAttribute("Enable", flag);
            resource = parent;
        }
    }

    private void disableTreeResource(final Syscomrolemembers groupItem, final String flag) {

        String resourceStr = "//Program[@ProgramId='" + groupItem.getChildid().toString() + "']";
        Node resourceNode = mXmlDocument.selectSingleNode(resourceStr);
        if (null == resourceNode) {
            return;
        }

        Element resource = (Element) resourceNode;
        if (resource != null) resource.addAttribute("Enable", flag);

    }

    private void disableTreeGroup(final Syscomrolemembers groupItem, final String flag) {

        String resourceStr = "//Group[@GroupId='" + groupItem.getChildid().toString() + "']";
        Node resourceNode = mXmlDocument.selectSingleNode(resourceStr);
        if (null == resourceNode) {
            return;
        }

        Element resource = (Element) resourceNode;
        resource.addAttribute("Enable", flag);

        // 往下層設定每個Group節點的Enable屬性
        setChildNodesIfEnable(resource, "TRUE");
    }

    private void enableTreeGroup(final Syscomrolegroup groupItem, final String flag) {

        String resourceStr = "//Group[@GroupId='" + groupItem.getGroupid().toString() + "']";
        Node resourceNode = mXmlDocument.selectSingleNode(resourceStr);
        if (null == resourceNode) {
            return;
        }

        Element resource = (Element) resourceNode;
        resource.addAttribute("Enable", flag);

        // 往下層設定每個Group節點的Enable屬性
        setChildNodesIfEnable(resource, "TRUE");

        // 往上找設定每個父節點的Enable屬性
        while (true) {
            Element parent = resource.getParent();
            if (!"Group".equalsIgnoreCase(parent.getName())) {
                break;
            }
            parent.addAttribute("Enable", flag);
            resource = parent;
        }
    }


    @SuppressWarnings("unchecked")
    private void setChildNodesIfEnable(Element parent, String flag) {
        for (Iterator<Element> it = parent.elementIterator(); it.hasNext(); ) {
            Element e = it.next();
            e.addAttribute("Enable", flag);
        }
    }

    @SuppressWarnings("unchecked")
    public void addNode(final SyscomgroupmembersAndGroupLevel item) {
        String groupStr = "//Group[@GroupId='" + item.getGroupid().toString() + "']";
        List<Node> nodes = mXmlDocument.selectNodes(groupStr);
        for (Iterator<Node> iter = nodes.iterator(); iter.hasNext(); ) {
            Element groupElement = (Element) iter.next();
            addSingleNode(groupElement, item, "TRUE");
        }
    }

    public Element addSingleNode(Element parentElement, final SyscomgroupmembersAndGroupLevel item, final String flag) {
        Objects.requireNonNull(parentElement);
        Objects.requireNonNull(item);

        switch (item.getChildtype()) {
            case CommonConstants.ChildTypeGroup:
                addGroupNode(parentElement, item, flag);
            case CommonConstants.ChildTypeResource:
                addProgramNode(parentElement, item, flag);
        }
        return null;
    }

    private void addGroupNode(Element parentElement, final SyscomgroupmembersAndGroupLevel item, final String flag) {
        parentElement.addElement("Group")
                .addAttribute("GroupId", item.getChildid().toString())
                .addAttribute("GroupNo", item.getChildno())
                .addAttribute("GroupName", item.getChildname())
                .addAttribute("GroupLevel", item.getGrouplevel().toString())
                .addAttribute("LocationNo", item.getLocationno().toString())
                .addAttribute("Enable", flag)
                .addAttribute("Url", item.getUrl() == null ? "" : item.getUrl())
        ;
    }

    private void addProgramNode(Element parentElement, final SyscomgroupmembersAndGroupLevel item, final String flag) {
        parentElement.addElement("Program")
                .addAttribute("ProgramId", item.getChildid().toString())
                .addAttribute("ProgramNo", item.getChildno())
                .addAttribute("ProgramName", item.getChildname() == null ? "" : item.getChildname())
                .addAttribute("GroupLevel", item.getGrouplevel().toString())
                .addAttribute("LocationNo", item.getLocationno().toString())
                .addAttribute("Enable", flag)
                .addAttribute("Url", item.getUrl() == null ? "" : item.getUrl())
                .addAttribute("SafeDefineFunctionList", item.getSafedefinefunctionlist())
                .addAttribute("UserDefineFunctionList", item.getUserdefinefunctionlist())
                .addAttribute("Remark", item.getRemark())
        ;
    }

    @Override
    public String toString() {
        return mXmlDocument.asXML();
    }

    public static void main(String[] args) {
        SyscomgroupmembersAndGroupLevel group = new SyscomgroupmembersAndGroupLevel();
        group.setGroupid(1);
        group.setChildid(1);
        group.setChildno("SYSCOM");
        group.setChildname("SYSCOM");

        group.setGrouplevel(0);
        group.setLocationno(15);
        group.setUrl("");

        MenuTree tree = new MenuTree(group);
//		System.out.println(tree.toString());

        List<SyscomgroupmembersAndGroupLevel> dtGroup = new ArrayList<SyscomgroupmembersAndGroupLevel>();

        SyscomgroupmembersAndGroupLevel g1 = new SyscomgroupmembersAndGroupLevel();
        g1.setGroupid(1);
        g1.setChildid(32);
        g1.setChildno("1");
        g1.setChildname("跨行OPC作業");
        g1.setChildtype("G");
        g1.setLocationno(0);
        g1.setGrouplevel(2);
        g1.setUrl("");

        dtGroup.add(g1);
        SyscomgroupmembersAndGroupLevel g2 = new SyscomgroupmembersAndGroupLevel();
        g2.setGroupid(1);
        g2.setChildid(42);
        g2.setChildno("10");
        g2.setChildname("FEP交易查詢");
        g2.setChildtype("G");
        g2.setLocationno(1);
        g2.setGrouplevel(2);
        g2.setUrl("");
        dtGroup.add(g2);
        SyscomgroupmembersAndGroupLevel g32 = new SyscomgroupmembersAndGroupLevel();
        g32.setGroupid(32);
        g32.setChildid(44);
        g32.setChildno("010102");
        g32.setChildname("變更押碼基碼-0102");
        g32.setChildtype("R");
        g32.setLocationno(0);
        g32.setGrouplevel(99);
        g32.setUrl("aaa");
        dtGroup.add(g32);

        tree.generateMenuTree(dtGroup);
//		System.out.println(tree.toString());

    }
}
