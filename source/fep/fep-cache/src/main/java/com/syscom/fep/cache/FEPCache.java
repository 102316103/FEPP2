package com.syscom.fep.cache;

import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class FEPCache {
    private static final String CLASS_NAME = FEPCache.class.getSimpleName();
    private static final LogHelper TRACELogger = LogHelperFactory.getTraceLogger();

    private static List<Curcd> curcdList = Collections.synchronizedList(new ArrayList<>());
    private static List<Msgctl> msgctlList = Collections.synchronizedList(new ArrayList<>());
    private static List<Zone> zoneList = Collections.synchronizedList(new ArrayList<>());
    private static Map<Integer, List<Sysconf>> subsysnoToSysconfListMap = Collections.synchronizedMap(new HashMap<Integer, List<Sysconf>>());
    private static AtomicReference<Sysstat> sysstat = new AtomicReference<Sysstat>(new Sysstat());

    private static MsgctlExtMapper msgctlExtMapper = SpringBeanFactoryUtil.getBean(MsgctlExtMapper.class);
    private static ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
    private static SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
    private static SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    private static CurcdExtMapper curcdExtMapper = SpringBeanFactoryUtil.getBean(CurcdExtMapper.class);

    private FEPCache() {
    }

    public static void reloadCache(CacheItem reloadcache) throws Exception {
        switch (reloadcache) {
            case ALL:
                for (CacheItem item : CacheItem.values()) {
                    if (item == CacheItem.ALL) {
                        continue;
                    }
                    reloadCache(item);
                }
                break;
            case SYSSTAT:
                reloadSysstat();
                break;
            case MSGCTL:
                reloadMsgctlList();
                break;
            case ZONE:
//                reloadZones(); TODO
                break;
            case CURCD:
                reloadCurcdList();
                break;
            case SYSCONF:
                reloadSysconfMap();
                break;
        }
    }

    public static List<Msgctl> getMsgctlList() {
        if (msgctlList.size() == 0) {
            reloadMsgctlList();
        } else {
            TRACELogger.trace(CLASS_NAME, " Get MSGCTL From Cache");
        }
        return msgctlList;
    }

    public static List<Curcd> getCurcdList() {
        if (curcdList.size() == 0) {
            reloadCurcdList();
        } else {
            TRACELogger.trace(CLASS_NAME, " Get CURCD From Cache");
        }
        return curcdList;
    }

    public static Sysstat getSysstat() throws Exception {
        if (StringUtils.isBlank(sysstat.get().getSysstatHbkno())) {
            reloadSysstat();
        } else {
            TRACELogger.trace(CLASS_NAME, " Get SYSSTAT From Cache");
        }
        return sysstat.get();
    }

    public static Msgctl getMsgctrl(String msgId) {
        TRACELogger.trace(CLASS_NAME, " Get MSGCTL by msgId = [", msgId, "] From Cache");
        return msgctlExtMapper.selectByPrimaryKey(msgId);
    }

    public static List<Zone> getZoneList() {
        if (zoneList.size() == 0) {
            reloadZones();
        } else {
            TRACELogger.trace(CLASS_NAME, " Get ZONE From Cache");
        }
        return zoneList;
    }

    public static List<Sysconf> getSysconfList(int subsys) {
        if (subsysnoToSysconfListMap.size() == 0) {
            reloadSysconfMap();
        } else {
            TRACELogger.trace(CLASS_NAME, " Get SYSCONF by subsys = [", subsys, "] From Cache");
        }
        return subsysnoToSysconfListMap.get(subsys);
    }

    // ============================================== Reload Cache Start ==============================================

    private static void reloadSysstat() throws Exception {
        TRACELogger.trace(CLASS_NAME, " Get SYSSTAT From DB");
        List<Sysstat> sysstatList = sysstatExtMapper.selectAll();
        if (CollectionUtils.isEmpty(sysstatList)) {
            throw ExceptionUtil.createException("無法取得SYSSTAT資料");
        }
        sysstat.set(sysstatList.get(0));
    }

    private static void reloadMsgctlList() {
        TRACELogger.trace(CLASS_NAME, " Get MSGCTL From DB");
        List<Msgctl> list = msgctlExtMapper.selectAll();
        msgctlList.clear();
        msgctlList.addAll(list);
    }
//    20230322 Bruce 先註解掉
    private static void reloadZones() {
        TRACELogger.trace(CLASS_NAME, " Get ZONE From DB");
        List<Zone> list = zoneExtMapper.selectAll();
        zoneList.clear();
        zoneList.addAll(list);
    }

    private static void reloadSysconfMap() {
        TRACELogger.trace(CLASS_NAME, " Get SYSCONF From DB");
        List<Sysconf> sysconfList = sysconfExtMapper.queryAllData(StringUtils.EMPTY);
        List<Short> subsysnoList = sysconfList.stream().map(t -> t.getSysconfSubsysno()).distinct().collect(Collectors.toList());
        subsysnoToSysconfListMap.clear();
        for (short subsysno : subsysnoList) {
            List<Sysconf> sysconfForSubsysnoList = Collections.synchronizedList(new ArrayList<>());
            for (Sysconf sysconf : sysconfList) {
                if (subsysno == sysconf.getSysconfSubsysno()) {
                    if (DbHelper.toBoolean(sysconf.getSysconfEncrypt()) && sysconf.getSysconfValue() != null) {
                        byte[] base64 = Base64Utils.decodeFromString(sysconf.getSysconfValue());
                        sysconf.setSysconfValue(ConvertUtil.toString(base64, PolyfillUtil.toCharsetName("950")));
                    }
                    sysconfForSubsysnoList.add(sysconf);
                }
            }
            subsysnoToSysconfListMap.put((int) subsysno, sysconfForSubsysnoList);
        }
    }

    private static void reloadCurcdList() {
        TRACELogger.trace(CLASS_NAME, " Get CURCD From DB");
        List<Curcd> list = curcdExtMapper.selectAll();
        curcdList.clear();
        curcdList.addAll(list);
    }

    // ============================================== Reload Cache End ==============================================
}
