package com.syscom.fep.vo.communication;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.CompressionUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseXmlCommu extends BaseCommu {

    @Override
    public String toString() {
        return this.toXML();
    }

    protected String toXML() {
        String xml = XmlUtil.toXML(this);
        if (isCompress()) {
            String elementRoot = "root";
            XStreamAlias xStreamAlias = this.getClass().getAnnotation(XStreamAlias.class);
            if (xStreamAlias != null && StringUtils.isNotBlank(xStreamAlias.value())) {
                elementRoot = xStreamAlias.value();
            }
            try {
                // hex字串轉byte
                byte[] decompressedBytes = ConvertUtil.toBytes(xml, StandardCharsets.UTF_8);
                // 壓縮
                byte[] compressedBytes = CompressionUtil.compress(decompressedBytes);
                // 轉成hex
                String compressed = ConvertUtil.toHex(compressedBytes);
                // 重新組裝xml字串
                xml = StringUtils.join("<", elementRoot, "><compressed>", compressed, "</compressed></", elementRoot, ">");
            } catch (IOException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
        return xml;
    }

    public static <T extends BaseCommu> T fromXML(String xml) throws Exception {
        return fromXML(xml, null);
    }

    public static <T extends BaseCommu> T fromXML(String xml, Class<T> clazz) throws Exception {
        Element root = XmlUtil.load(xml);
        // 如果有<compressed>, 表示電文是被壓縮的, 所以要先取出來進行解壓
        String compressed = XmlUtil.getChildElementValue(root, "compressed", StringUtils.EMPTY);
        if (StringUtils.isNotBlank(compressed)) {
            // hex字串轉byte
            byte[] compressedBytes = ConvertUtil.hexToBytes(compressed);
            // 解壓
            byte[] decompressedBytes = CompressionUtil.decompress(compressedBytes);
            // 再轉為解壓後的xml字串
            xml = ConvertUtil.toString(decompressedBytes, StandardCharsets.UTF_8);
            // 再load一次
            root = XmlUtil.load(xml);
        }
        if (clazz == null) {
            String classname = XmlUtil.getChildElementValue(root, "classname", StringUtils.EMPTY);
            if (StringUtils.isNotBlank(classname)) {
                clazz = (Class<T>) Class.forName(classname);
            } else {
                return null;
            }
        }
        return (T) XmlUtil.fromXML(xml, clazz);
    }
}