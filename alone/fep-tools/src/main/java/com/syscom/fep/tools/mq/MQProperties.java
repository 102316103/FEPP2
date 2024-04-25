package com.syscom.fep.tools.mq;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.IOUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public class MQProperties extends Properties implements MQConstants {
    private final LogHelper logger = new LogHelper();

    public void load() throws Exception {
        this.load(IOUtil.openInputStream(CONFIGURATION_PROPERTIES));
        for (Object key : this.keySet()) {
            String value = this.getProperty((String) key);
            if (value.startsWith("ENC(") && value.endsWith(")")) {
                this.setProperty((String) key, Jasypt.decrypt(value));
            }
        }
        logger.info(this);
    }

    public boolean containsKey(MQInstance instance, String key) {
        return this.containsKey(StringUtils.join(instance.name(), ".", key));
    }

    public String getProperty(MQInstance instance, String key) {
        return this.getProperty(StringUtils.join(instance.name(), ".", key));
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getProperty(MQInstance instance, String key, T defaultValue) {
        Object value = this.get(StringUtils.join(instance.name(), ".", key));
        if (value != null) {
            return (T) value;
        }
        return defaultValue;
    }
}
