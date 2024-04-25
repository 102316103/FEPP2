package com.syscom.fep.web.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.web.entity.MappingURI;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.interceptor.AuthenticationInterceptor;
import com.syscom.fep.web.interceptor.ViewInterceptor;
import com.syscom.fep.web.interceptor.WebAuditInterceptor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.web")
@RefreshScope
public class WebConfiguration implements WebMvcConfigurer {
    @Value("${spring.fep.web.demo:false}")
    private boolean showDemo;
    @Value("${spring.fep.web.ldap:false}")
    private boolean ldapEnable;
    @Value("${spring.fep.web.ldap.simulator:false}")
    private boolean ldapSimulator;
    @Value("${spring.fep.web.title:FEP監控系統}")
    private String appTitle;
    @Value("${spring.fep.web.web-type:FEP}")
    private String webType;
    @Value("${spring.fep.web.query-only:0}")
    private String queryOnly;
    @Value("${spring.fep.web.time-out:30000}")
    private int reNewTime;
    @Value("#{'${spring.fep.web.subsys:1,2,3,4,5,6,7,8,9,10,11,12,13,14}'.split(',')}")
    private List<String> subsysList;
    @Value("${spring.fep.web.isFEPWebOrRMWeb:FEPWeb}")
    private String isWeb;
    @Value("${spring.fep.web.RemoteMQServiceFlag:ON}")
    private String RemoteMQServiceFlag;
    @Value("${spring.fep.web.RemoteMQServiceIP:serverIP}")
    private String serverIP;
    @Value("${spring.fep.web.mq.service.port:8161}")
    private String port;
    @Value("${spring.fep.web.mq.service.request:/api/CustomCommand}")
    private String mqServiceRequest;
    @Value("${spring.fep.web.system.principal:HWAFANG@tcbt.com}")
    private String principal;
    @Value("${spring.fep.web.system.webaddress:ldaps://10.0.6.2:636}")
    private String webaddress;
    @Value("${spring.fep.web.system.strtemp:dc=tcbt,dc=com}")
    private String strtemp;
    @Value("${spring.fep.web.system.username:HWAFANG}")
    private String username;
    @Value("${spring.fep.web.system.userinname:HWAFANG}")
    private String userinname;
    @Value("${spring.fep.web.system.chkid:G201450209}")
    private String chkid;
    @Value("${spring.fep.web.system.userid:0410}")
    private String unitid;
    @Value("${spring.fep.web.system.fileName:fepd.cer}")
    private String fileName;
    @Value("${spring.fep.web.system.filesscode:changeit}")
    private String filesscode;
    @Value("${spring.fep.web.system.certificateName:X.509}")
    private String certificateName;
    @Value("${spring.fep.web.system.fiddler:fiddler}")
    private String fiddler;
    @Value("${spring.fep.web.system.download.feplogpath:/fep/logs}")
    private String fepLogPath;
    @Value("${spring.fep.web.system.download.feplogarchivespath:/fep/logs/archives}")
    private String fepLogArcivesPath;
    @Value("${spring.fep.web.system.download.fepwaslogpath:/fep/waslogs}")
    private String fepWasLogPath;
    @Value("${spring.fep.web.app-mon.ip:127.0.0.1}")
    private String appMonIp;
    @Value("${spring.fep.web.app-mon.port:8201}")
    private String appMonPort;
    @NestedConfigurationProperty
    private final List<WebApLogConfiguration> aplog = new ArrayList<>();

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @Autowired
    private ViewInterceptor viewInterceptor;

    public static WebConfiguration getInstance() {
        return SpringBeanFactoryUtil.getBean(WebConfiguration.class);
    }

    /**
     * 頁面跳轉
     *
     * @param registry
     */
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Router route : Router.values()) {
            registry.addViewController(route.getUrl()).setViewName(route.getView());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> baseExcludePathPatternList =
                Arrays.asList(
                        Router.DEFAULT.getUrl(),
                        Router.LOGIN.getUrl(),
                        "/error/**",
                        MappingURI.Logon,
                        MappingURI.Logout,
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/plugins/**",
                        "/ping/**",
                        "/actuator/**",
                        "/UI060610/GetAPLog");
        // authenticationInterceptor
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(baseExcludePathPatternList);
        // viewInterceptor
        List<String> viewExcludePathPatternList = new ArrayList<>(baseExcludePathPatternList);
        viewExcludePathPatternList.add(Router.HOME.getUrl());
        registry.addInterceptor(viewInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(viewExcludePathPatternList);
        // webAuditInterceptor
        WebAuditInterceptor webAuditInterceptor = SpringBeanFactoryUtil.getBean(WebAuditInterceptor.class, false);
        if (webAuditInterceptor != null) {
            List<String> webAuditExcludePathPatternList = new ArrayList<>(baseExcludePathPatternList);
            webAuditExcludePathPatternList.add(Router.HOME.getUrl());
            // 以下動作也要攔截, 所以要移除掉
            webAuditExcludePathPatternList.remove(MappingURI.Logon);
            webAuditExcludePathPatternList.remove(MappingURI.Logout);
            registry.addInterceptor(webAuditInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(webAuditExcludePathPatternList);
        } else {
            LogHelperFactory.getTraceLogger().warn("##########WebAuditInterceptor has been not register as Spring Object, so that no need to record Web Audit!!!");
        }
    }

    public boolean isShowDemo() {
        return showDemo;
    }

    public boolean isLdapEnable() {
        return ldapEnable;
    }

    public boolean isLdapSimulator() {
        return ldapSimulator;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public String getWebType() {
        return webType;
    }

    public String getQueryOnly() {
        return queryOnly;
    }

    public int getReNewTime() {
        return reNewTime;
    }

    public List<String> getSubsysList() {
        return subsysList;
    }

    public String getIsFEPWebOrRMWeb() {
        return isWeb;
    }

    public String getRemoteMQServiceFlag() {
        return RemoteMQServiceFlag;
    }

    public String getRemoteMQServiceIP() {
        return serverIP;
    }

    public String getMQServicePort() {
        return port;
    }

    public String getMqServiceRequest() {
        return mqServiceRequest;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getWebaddress() {
        return webaddress;
    }

    public String getStrtemp() {
        return strtemp;
    }

    public String getUsername() {
        return username;
    }

    public String getUserinname() {
        return userinname;
    }

    public String getChkid() {
        return chkid;
    }

    public String getUnitid() {
        return unitid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilesscode() {
        return filesscode;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public String getFiddler() {
        return fiddler;
    }

    public String getFepLogPath() {
        return fepLogPath;
    }

    public String getFepWasLogPath() {
        return fepWasLogPath;
    }

    // public String getAppMonIp() {
    //     return appMonIp;
    // }

    public String getFepLogArcivesPath() {
        return fepLogArcivesPath;
    }

    public String getAppMonPort() {
        return appMonPort;
    }

    public List<WebApLogConfiguration> getAplog() {
        return aplog;
    }

    @PostConstruct
    public void print() {
        Field[] fields = WebConfiguration.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("Web Configuration:\r\n");
            for (Field field : fields) {
                if (field.getName().equals("authenticationInterceptor")
                        || field.getName().equals("viewInterceptor")) {
                    continue;
                }
                if (field.getName().equals("aplog")) {
                    for (int i = 0; i < aplog.size(); i++) {
                        this.toString(sb, StringUtils.join("spring.fep.web.aplog[", i, "]"), aplog.get(i));
                    }
                    continue;
                }
                ReflectionUtils.makeAccessible(field);
                Value annotation = field.getAnnotation(Value.class);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
                if (annotation != null) {
                    sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().length() - 1));
                } else {
                    ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
                    if (annotation2 != null) {
                        String prefix = annotation2.prefix();
                        if (StringUtils.isNotBlank(prefix)) {
                            sb.append(prefix).append(".");
                        }
                    }
                    sb.append(field.getName());
                }
                sb.append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }

    private void toString(StringBuilder sb, String configurationPropertiesPrefix, Object value) {
        if (value == null)
            return;
        Field[] fields = value.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append(configurationPropertiesPrefix).append(".")
                        .append(field.getName())
                        .append(" = ")
                        .append(ReflectionUtils.getField(field, value))
                        .append("\r\n");
            }
        }
    }
}