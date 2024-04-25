package com.syscom.fep.vo.communication;

import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.CompressionUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BaseJsonCommu extends BaseCommu {

    @Override
    public String toString() {
        return this.toJSON();
    }

    protected String toJSON() {
        String json = new Gson().toJson(this);
        if (isCompress()) {
            try {
                // hex字串轉byte
                byte[] decompressedBytes = ConvertUtil.toBytes(json, StandardCharsets.UTF_8);
                // 壓縮
                byte[] compressedBytes = CompressionUtil.compress(decompressedBytes);
                // 轉成hex
                String compressed = ConvertUtil.toHex(compressedBytes);
                // 重新組裝json字串
                json = StringUtils.join("{\"compressed\":", compressed, "}");
            } catch (IOException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
        return json;
    }

    public static <T extends BaseCommu> T fromJson(String json) throws Exception {
        return fromJson(json, null);
    }

    public static <T extends BaseCommu> T fromJson(String json, Class<T> clazz) throws Exception {
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(json, Map.class);
        String compressed = map.get("compressed");
        if (StringUtils.isNotBlank(compressed)) {
            // hex字串轉byte
            byte[] compressedBytes = ConvertUtil.hexToBytes(compressed);
            // 解壓
            byte[] decompressedBytes = CompressionUtil.decompress(compressedBytes);
            // 再轉為解壓後的json字串
            json = ConvertUtil.toString(decompressedBytes, StandardCharsets.UTF_8);
            // 再轉一次
            map = gson.fromJson(json, Map.class);
        }
        if (clazz == null) {
            String classname = map.get("classname");
            if (StringUtils.isNotBlank(classname)) {
                clazz = (Class<T>) Class.forName(classname);
            } else {
                return null;
            }
        }
        return gson.fromJson(json, clazz);
    }
}