package com.syscom.fep.web.ldap;

import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.reference.DefaultEncoder;
import org.springframework.beans.factory.annotation.Value;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.*;

public class AdConnect {
    //static final Logger logger = LoggerFactory.getLogger(AdConnect.class);

    private String filter;
    private String[] attributes;
    private String[] memberof;
    private String[] allunits;
    private String[] userinfos;
    private String[] chkid;

    private String strldapname;

    private Hashtable<String, String> env = null;
    @Value("${tcb_ldap_url}")
    private String tcb_ldap_url;

    public AdConnect(String ac, String sscode, String url, String strtemp, String principal) {
        // 搜尋根節點
        // baseDN = "dc=tcbd,dc=com";
        this.tcb_ldap_url = url;
        System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
        // 2024-04-25 Richard modified start for 【LDAP Injection】【SSRF】
        // strldapname = strtemp;//dc=tcbd,dc=com
        Encoder esapiEncoder = DefaultEncoder.getInstance();
        strldapname = esapiEncoder.encodeForLDAP(strtemp);
        // 2024-04-25 Richard modified end for 【LDAP Injection】【SSRF】
        String doF = strtemp.split(",")[0].split("=")[1];
        String doS = strtemp.split(",")[1].split("=")[1];

        // 要查詢的屬性列
        this.attributes = new String[] {"description"};

        this.userinfos = new String[] {"description", "mail", "title", "department", "departmentNumber",
                "distinguishedName", "ou", "sAMAccountName", "rOCID", "info"};
        this.memberof = new String[] {"memberOf"};
        this.allunits = new String[] {"st", "street", "1"};
        this.chkid = new String[] {"rOCID", "objectclass"};

        // port 389 ====> LDA
        // port 636 ====> LDAPS
        this.env = new Hashtable<String, String>();
        this.env.put("java.naming.ldap.factory.socket", MySSLSocketFactory.class.getName());
        this.env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        this.env.put(Context.PROVIDER_URL, tcb_ldap_url);//ldaps://10.0.6.2:636

        this.env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        // 2024-04-25 Richard modified start for 【LDAP Injection】【SSRF】
        String sanitizedSscode = esapiEncoder.encodeForLDAP(sscode);
        this.env.put(Context.SECURITY_CREDENTIALS, sanitizedSscode);//Tcb123456
        // 2024-04-25 Richard modified end for 【LDAP Injection】【SSRF】

        String encodeAc = ESAPI.encoder().encodeForLDAP(ac);
        this.env.put(Context.SECURITY_PRINCIPAL, encodeAc + "@" + doF + "." + doS);//HWAFANG@tcbd.com

        this.filter = "cn=" + ac.trim();
    }

    /**
     * 建立LDAP連線，獲取使用者資訊
     *
     * @return String
     * @throws NamingException
     */
    @SuppressWarnings("rawtypes")
    public String getUserName(String ac) throws Exception {

        //try {
        // Openning the connection
//		DirContext ctx = new InitialDirContext(env);
        LdapContext ctx = new InitialLdapContext(env, null);
        List<Map> list = new ArrayList<Map>();
        String strtemp = "";
        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(attributes);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // NamingEnumeration<?> en = ctx.search(GlobalValues.baseDn, filter,
        // constraints);
//        this.filter = "cn=" + ac.trim();
//        EqualsFilter EqualsFilter = new EqualsFilter("cn", ac.trim());
//        this.filter = EqualsFilter.toString();
        NamingEnumeration<?> en = ctx.search("ou=TCBUsers," + strldapname, filter, constraints);

        while (en != null && en.hasMoreElements()) {

            Object obj = en.nextElement();
            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;
                Attributes attrs = si.getAttributes();

                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < attributes.length; i++) {
                    String attributeName = attributes[i];
                    if (attrs.get(attributeName) == null) {
                        map.put(attributeName, attrs.get(attributeName));
                    } else {
                        map.put(attributeName, attrs.get(attributeName).get());
                    }
                }
                list.add(map);
            }
        }
        if (list.isEmpty())
            return "";
        else
            return (String) (list.get(0) != null ? list.get(0).get("description") : null) + strtemp;

        // Use your context here...
        //} catch (NamingException e) {
        //System.out.println("Problem occurs during context initialization !");
        //e.printStackTrace();
        //}


    }

    /**
     * 建立LDAP連線，獲取使用者資訊
     *
     * @return String
     * @throws NamingException
     */
    public Map<String, Object> getUserInfo() throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        Map<String, Object> infomap = new HashMap<String, Object>();

        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(userinfos);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // NamingEnumeration<?> en = ctx.search(GlobalValues.baseDn, filter,
        // constraints);

        // 2024-04-25 Richard modified start for 【LDAP Injection】【SSRF】
        // String encodeName = ESAPI.encoder().encodeForLDAP("ou=TCBUsers," + strldapname);
        // NamingEnumeration<?> en = ctx.search(encodeName, filter, constraints);
        // NamingEnumeration<?> en = ctx.search("ou=LOCALHIRE," + strldapname, filter, constraints);
        Encoder esapiEncoder = DefaultEncoder.getInstance();
        String sanitizedUsers = esapiEncoder.encodeForLDAP("ou=TCBUsers," + strldapname);
        NamingEnumeration<?> en = ctx.search(sanitizedUsers, filter, constraints);
        // 2024-04-25 Richard modified end for 【LDAP Injection】【SSRF】

        while (en != null && en.hasMoreElements()) {

            Object obj = en.nextElement();

            if (obj instanceof SearchResult) {

                SearchResult si = (SearchResult) obj;

                Attributes attrs = si.getAttributes();

                for (int i = 0; i < userinfos.length; i++) {

                    String attributeName = userinfos[i];

                    if (attrs.get("mail") != null) {

                        if (attrs.get("mail").toString().contains("tcb-bank")) {
                            if (attrs.get(attributeName) == null) {
                                infomap.put(attributeName, attrs.get(attributeName));
                            } else {
                                infomap.put(attributeName, attrs.get(attributeName).get());
                            }
                        }
                    } else {
                        if (attrs.get(attributeName) == null) {
                            infomap.put(attributeName, attrs.get(attributeName));
                        } else {
                            infomap.put(attributeName, attrs.get(attributeName).get());
                        }
                    }
                }

            }
        }

        if (infomap.isEmpty()) {
            // 2024-04-25 Richard modified start for 【LDAP Injection】【SSRF】
            // en = ctx.search("ou=LOCALHIRE," + strldapname, filter, constraints);
            String sanitizedLocalHire = esapiEncoder.encodeForLDAP("ou=LOCALHIRE," + strldapname);
            en = ctx.search(sanitizedLocalHire, filter, constraints);
            // 2024-04-25 Richard modified end for 【LDAP Injection】【SSRF】

            while (en != null && en.hasMoreElements()) {

                Object obj = en.nextElement();

                if (obj instanceof SearchResult) {

                    SearchResult si = (SearchResult) obj;

                    Attributes attrs = si.getAttributes();

                    for (int i = 0; i < userinfos.length; i++) {

                        String attributeName = userinfos[i];

                        if (attrs.get("mail") != null) {

                            if (attrs.get("mail").toString().contains("tcb-bank")) {
                                if (attrs.get(attributeName) == null) {
                                    infomap.put(attributeName, attrs.get(attributeName));
                                } else {
                                    infomap.put(attributeName, attrs.get(attributeName).get());
                                }
                            }
                        } else {
                            if (attrs.get(attributeName) == null) {
                                infomap.put(attributeName, attrs.get(attributeName));
                            } else {
                                infomap.put(attributeName, attrs.get(attributeName).get());
                            }
                        }
                    }

                }
            }
        }
        if (infomap.isEmpty()) {
            // "description","mail","title","department","departmentNumber","distinguishedName","ou","sAMAccountName","rOCID","info"
            infomap.put("description", "");
            infomap.put("mail", "");
            infomap.put("title", "");
            infomap.put("department", "");
            infomap.put("departmentNumber", "");
            infomap.put("distinguishedName", "");
            infomap.put("ou", "");
            infomap.put("sAMAccountName", "");
            infomap.put("rOCID", "");
            infomap.put("info", "");
            return infomap;
        } else {
            return infomap;
        }

    }

    /**
     * 建立LDAP連線，獲取使用者資訊
     *
     * @return String
     * @throws NamingException
     */
    public Map<String, Object> getUserinname(String strname) throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        Map<String, Object> infomap = new HashMap<String, Object>();

        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(userinfos);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // NamingEnumeration<?> en = ctx.search(GlobalValues.baseDn, filter,
        // constraints);

        NamingEnumeration<?> en = ctx.search("ou=TCBUsers," + strldapname, "cn=" + strname.trim(), constraints);

        while (en != null && en.hasMoreElements()) {

            Object obj = en.nextElement();

            if (obj instanceof SearchResult) {

                SearchResult si = (SearchResult) obj;

                Attributes attrs = si.getAttributes();

                for (int i = 0; i < userinfos.length; i++) {

                    String attributeName = userinfos[i];

                    if (attrs.get("mail") != null) {

                        if (attrs.get("mail").toString().contains("tcb-bank")) {
                            // System.out.println("userinfo1: #"+attrs.size()+"# " + attributeName);
                            if (attrs.get(attributeName) == null) {
                                infomap.put(attributeName, attrs.get(attributeName));
                            } else {
                                infomap.put(attributeName, attrs.get(attributeName).get());
                            }
                        }
                    } else {
                        // System.out.println("userinfo2: #"+attrs.size()+"# " + attributeName);
                        if (attrs.get(attributeName) == null) {
                            infomap.put(attributeName, attrs.get(attributeName));
                        } else {
                            infomap.put(attributeName, attrs.get(attributeName).get());
                        }
                    }
                }

                // System.out.println("userinfo4: " +infomap);
            }
        }

        if (infomap.isEmpty()) {
            // "description","mail","title","department","departmentNumber","distinguishedName","ou","sAMAccountName","rOCID","info"
            infomap.put("description", "");
            infomap.put("mail", "");
            infomap.put("title", "");
            infomap.put("department", "");
            infomap.put("departmentNumber", "");
            infomap.put("distinguishedName", "");
            infomap.put("ou", "");
            infomap.put("sAMAccountName", "");
            infomap.put("rOCID", "");
            infomap.put("info", "");
            return infomap;
        } else {
            return infomap;
        }

    }

    /**
     * 建立LDAP連線，獲取所有部門資料
     *
     * @return List
     * @throws NamingException
     */
    public List<Map<String, Object>> getallunits() throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(allunits);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // NamingEnumeration<?> en = ctx.search(GlobalValues.baseDn, filter,
        // constraints);

        NamingEnumeration<?> en = ctx.search("ou=TCBUsers," + strldapname, "(&(objectClass=organizationalUnit))",
                constraints);


        while (en != null && en.hasMoreElements()) {

            Object obj = en.nextElement();

            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;

                Attributes attrs = si.getAttributes();
                Map<String, Object> unitsmap = new HashMap<String, Object>();
                for (int i = 0; i < allunits.length; i++) {
                    String attributeName = allunits[i];

                    if (attrs.get(attributeName) == null) {
                        // unitsmap.put(attributeName, attrs.get(attributeName));
                    } else {
                        // unitsmap.put(attributeName, attrs.get(attributeName).get());

                        if (StringUtils.equals("st", attributeName)) {
                            String strst = (String) attrs.get(attributeName).get();

                            String[] strsts = strst.split(",");

                            if (strsts.length == 5) {
                                unitsmap.put("unitname", strsts[0]);
                                unitsmap.put("ouid", strsts[1]);
                                unitsmap.put("accid", strsts[2]);
                                unitsmap.put("connid", strsts[3]);
                                unitsmap.put("unitid", strsts[4]);
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            } else if (strsts.length == 4) {
                                unitsmap.put("unitname", strsts[0]);
                                unitsmap.put("ouid", strsts[1]);
                                unitsmap.put("accid", strsts[2]);
                                unitsmap.put("connid", strsts[3]);
                                unitsmap.put("unitid", "");
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            } else if (strsts.length == 3) {
                                unitsmap.put("unitname", strsts[0]);
                                unitsmap.put("ouid", strsts[1]);
                                unitsmap.put("accid", strsts[2]);
                                unitsmap.put("connid", "");
                                unitsmap.put("unitid", "");
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            } else if (strsts.length == 2) {
                                unitsmap.put("unitname", strsts[0]);
                                unitsmap.put("ouid", strsts[1]);
                                unitsmap.put("accid", "");
                                unitsmap.put("connid", "");
                                unitsmap.put("unitid", "");
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            } else if (strsts.length == 1) {
                                unitsmap.put("unitname", strsts[0]);
                                unitsmap.put("ouid", "");
                                unitsmap.put("accid", "");
                                unitsmap.put("connid", "");
                                unitsmap.put("unitid", "");
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            } else {
                                unitsmap.put("unitname", "");
                                unitsmap.put("ouid", "");
                                unitsmap.put("accid", "");
                                unitsmap.put("connid", "");
                                unitsmap.put("unitid", "");
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            }

                        } else if (StringUtils.equals("street", attributeName)) {
                            String strstreet = (String) attrs.get(attributeName).get();
                            String[] strstreets = strstreet.split("\\*");
                            if (strstreets.length == 3) {
                                unitsmap.put("telephoNo", strstreets[0]);
                                unitsmap.put("faxNo", strstreets[1]);
                                unitsmap.put("address", strstreets[2]);
                            } else if (strstreets.length == 2) {
                                unitsmap.put("telephoNo", strstreets[0]);
                                unitsmap.put("faxNo", strstreets[1]);
                                unitsmap.put("address", "");
                            } else if (strstreets.length == 1) {
                                unitsmap.put("telephoNo", strstreets[0]);
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            } else {
                                unitsmap.put("telephoNo", "");
                                unitsmap.put("faxNo", "");
                                unitsmap.put("address", "");
                            }
                        }
                    }

                }

                if (!unitsmap.isEmpty())
                    list.add(unitsmap);

            }
        }
        return list;
    }

    /**
     * 建立LDAP連線，檢查身份證是否為銀行行員
     *
     * @return List
     * @throws NamingException
     */
    public boolean getchkid(String strrocid) throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);

        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(chkid);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        boolean bchk = false;
        // NamingEnumeration<?> en = ctx.search(GlobalValues.baseDn, filter,
        // constraints);

        NamingEnumeration<?> en = ctx.search("ou=TCBUsers," + strldapname,
                "(&(objectClass=user)(objectCategory=person))", constraints);


        while (en != null && en.hasMoreElements()) {

            if (bchk)
                break;

            Object obj = en.nextElement();

            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;
                Attributes attrs = si.getAttributes();

                for (int i = 0; i < chkid.length; i++) {
                    String attributeName = chkid[i];

                    if (attrs.get(attributeName) == null) {

                    } else {

                        // if(attributeName=="rOCID")
                        {

                            String strtemp = (String) attrs.get(attributeName).get();
                            // System.out.println("chkid441 !!" +strtemp );
                            if (strtemp.equalsIgnoreCase(strrocid)) {
                                bchk = true;
                                break;
                            }

                        }
                    }

                }

                // if(!unitsmap.isEmpty())
                // list.add(unitsmap);

            }
        }
        // System.out.println("allunits list: " +list);
        return bchk;
    }

    /**
     * 建立LDAP連線，獲取使用者群組
     *
     * @return String
     * @throws NamingException
     */
    public List<String> getUserGroups() throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        List<String> list = new ArrayList<String>();

        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(memberof);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // 2024-04-25 Richard modified start for 【LDAP Injection】【SSRF】
        // String encodeName = ESAPI.encoder().encodeForLDAP("ou=TCBUsers," + strldapname);
        // NamingEnumeration<?> en = ctx.search(encodeName, filter, constraints);
        Encoder esapiEncoder = DefaultEncoder.getInstance();
        String sanitizedUsers = esapiEncoder.encodeForLDAP("ou=TCBUsers," + strldapname);
        NamingEnumeration<?> en = ctx.search(sanitizedUsers, filter, constraints);
        // 2024-04-25 Richard modified end for 【LDAP Injection】【SSRF】

        while (en != null && en.hasMoreElements()) {

            Object obj = en.nextElement();

            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;
                Attributes attrs = si.getAttributes();

                Attribute attr = attrs.get(memberof[0]);

                NamingEnumeration e = attr.getAll();
                while (e.hasMore()) {
                    String value = (String) e.next();

                    if ((value.indexOf("=") != -1) && (value.indexOf(",") != -1)) {
                        value = value.substring(value.indexOf("=") + 1, value.indexOf(","));
                    }
                    list.add(value);
                }

            }
        }

        if (list.size() == 0) {
            // 2024-04-25 Richard modified start for 【LDAP Injection】【SSRF】
            // en = ctx.search("ou=LOCALHIRE," + strldapname, filter, constraints);
            String sanitizedlocalHire = esapiEncoder.encodeForLDAP("ou=LOCALHIRE," + strldapname);
            en = ctx.search(sanitizedlocalHire, filter, constraints);
            // 2024-04-25 Richard modified end for 【LDAP Injection】【SSRF】


            while (en != null && en.hasMoreElements()) {

                Object obj = en.nextElement();

                if (obj instanceof SearchResult) {
                    SearchResult si = (SearchResult) obj;
                    Attributes attrs = si.getAttributes();

                    Attribute attr = attrs.get(memberof[0]);

                    NamingEnumeration e = attr.getAll();
                    while (e.hasMore()) {
                        String value = (String) e.next();

                        if ((value.indexOf("=") != -1) && (value.indexOf(",") != -1)) {
                            value = value.substring(value.indexOf("=") + 1, value.indexOf(","));
                        }
                        list.add(value);
                    }

                }
            }
        }

        return list;

    }

    /**
     * 建立LDAP連線，獲取部門Mail資料
     *
     * @return List
     * @throws NamingException
     */
    public List<String> getUnitUserMail(String unit) throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        List<String> list = new ArrayList<>();

        SearchControls constraints = new SearchControls();
        constraints.setReturningAttributes(userinfos);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<?> en = ctx.search("ou=" + unit + ",ou=TCBUsers," + strldapname,
                "(&(objectClass=user)(objectCategory=person))", constraints);

        while (en != null && en.hasMoreElements()) {
            Object obj = en.nextElement();
            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;
                Attributes attrs = si.getAttributes();
                Attribute userAttr = attrs.get("mail");
                if (userAttr != null) {
                    String mail = (String) userAttr.get();
                    if (StringUtils.isNotBlank(mail)) {
                        list.add(mail);
                    }
                }
            }
        }

        return list;
    }

}
