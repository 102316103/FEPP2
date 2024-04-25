package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XML操作類
 *
 * @author Richard
 */
public class XmlUtil {
    private static final LogHelper logger = new LogHelper();
    private static final ConcurrentHashMap<String, XStream> classnameToXStreamFromMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, XStream> classnameToXStreamToMap = new ConcurrentHashMap<>();

    private XmlUtil() {}

    private static <T> XStream getXStream(Class<T> cls, boolean isTo) {
        String key = cls.getName();
        XStream xs = isTo ? classnameToXStreamToMap.get(key) : classnameToXStreamFromMap.get(key);
        if (xs == null) {
            xs = isTo ? new XStream(new Xpp3Driver(new NoNameCoder())) : new XStream(); // 解決欄位名出現雙下劃線的問題
            xs.addPermission(AnyTypePermission.ANY);
            xs.allowTypes(new Class[] {cls});
            xs.processAnnotations(cls);
            // XStream.setupDefaultSecurity(xs);
            if (isTo) {
                classnameToXStreamToMap.put(key, xs);
            } else {
                classnameToXStreamFromMap.put(key, xs);
            }
        }
        return xs;
    }

    /**
     * 將一個實體類轉換為XML字串
     *
     * @param <T>
     * @param object
     * @param formatted
     * @return
     */
    public static <T> String toXML(T object, boolean... formatted) {
        XStream xs = getXStream(object.getClass(), true);
        String xml = xs.toXML(object);
        if (ArrayUtils.isEmpty(formatted) || !formatted[0]) {
            xml = RegExUtils.replaceAll(xml, "\\s*\\n\\s*", StringUtils.EMPTY);
            return xml;
        }
        return xml;
    }

    /**
     * 將XML字串轉為實體類
     *
     * @param <T>
     * @param xml
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXML(String xml, Class<T> cls) {
        return fromXML(xml, cls, false);
    }

    /**
     * 將XML字串轉為實體類
     *
     * @param <T>
     * @param xml
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXML(String xml, Class<T> cls, boolean ignoreUnknownElements) {
        XStream xs = getXStream(cls, false);
        if (ignoreUnknownElements)
            xs.ignoreUnknownElements();
        return (T) xs.fromXML(xml);
    }

    /**
     * 判斷一個字串是否是XML字串
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public static boolean isXML(String xml) throws Exception {
        try {
            load(IOUtils.toInputStream(xml, StandardCharsets.UTF_8), false);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 載入一個XML檔案
     *
     * @param file
     * @return
     * @throws Exception
     */
    // public static Element load(File file) throws Exception {
    //     return load(new FileInputStream(file));
    // }

    /**
     * 載入一個XML檔案
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static Element load(URL url) throws Exception {
        return load(url.openStream());
    }

    /**
     * 載入一個XML字串
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public static Element load(String xml) throws Exception {
        return load(IOUtils.toInputStream(xml, StandardCharsets.UTF_8));
    }

    /**
     * 載入XML
     *
     * @param is
     * @param printExceptionStackTrace
     * @return
     * @throws Exception
     */
    public static Element load(InputStream is, boolean... printExceptionStackTrace) throws Exception {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setExpandEntities(false);
            saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document doc = saxBuilder.build(is);
            return doc.getRootElement();
        } catch (Exception e) {
            if (ArrayUtils.isEmpty(printExceptionStackTrace) || printExceptionStackTrace[0]) {
                logger.exceptionMsg(e, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 將XML字串寫入指定的OutputStream中
     *
     * @param root
     * @param rootComment
     * @param os
     * @throws Exception
     */
    public static void save(Element root, String rootComment, OutputStream os) throws Exception {
        Document doc = new Document(root);
        if (StringUtils.isNotBlank(rootComment)) {
            doc.addContent(0, new Comment(rootComment));
        }
        XMLOutputter outputter = new XMLOutputter();
        Format format = Format.getCompactFormat();
        format.setIndent("\t");
        format.setLineSeparator(System.getProperty("line.separator"));
        outputter.setFormat(format);
        try {
            outputter.output(doc, os);
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    /**
     * 將XML字串產出到檔案中
     *
     * @param root
     * @param rootComment
     * @param file
     * @throws Exception
     */
    public static void save(Element root, String rootComment, File file) throws Exception {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            save(root, rootComment, bos);
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    /**
     * 獲取某個節點的boolean類型值
     *
     * @param element
     * @param defaultValue
     * @return
     */
    public static boolean getElementValue(Element element, boolean defaultValue) {
        String value = getElementValue(element);
        try {
            return StringUtils.isNotBlank(value) ? Boolean.parseBoolean(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點的double類型值
     *
     * @param element
     * @param defaultValue
     * @return
     */
    public static double getElementValue(Element element, double defaultValue) {
        String value = getElementValue(element);
        try {
            return StringUtils.isNotBlank(value) ? Double.parseDouble(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點的long類型值
     *
     * @param element
     * @param defaultValue
     * @return
     */
    public static long getElementValue(Element element, long defaultValue) {
        String value = getElementValue(element);
        try {
            return StringUtils.isNotBlank(value) ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點的int類型值
     *
     * @param element
     * @param defaultValue
     * @return
     */
    public static int getElementValue(Element element, int defaultValue) {
        String value = getElementValue(element);
        try {
            return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點的String類型值
     *
     * @param element
     * @param defaultValue
     * @return
     */
    public static String getElementValue(Element element, String defaultValue) {
        String value = getElementValue(element);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    /**
     * 獲取某個節點的文本內容
     *
     * @param element
     * @return
     */
    private static String getElementValue(Element element) {
        if (element != null) {
            return element.getTextTrim();
        }
        return null;
    }

    /**
     * 給某個節點塞入boolean類型的值
     *
     * @param element
     * @param value
     */
    public static void setElementValue(Element element, boolean value) {
        setElementValue(element, String.valueOf(value));
    }

    /**
     * 給某個節點塞入Number類型的值
     *
     * @param element
     * @param value
     */
    public static void setElementValue(Element element, Number value) {
        setElementValue(element, String.valueOf(value));
    }

    /**
     * 給某個節點塞入值
     *
     * @param element
     * @param value
     */
    public static void setElementValue(Element element, String value) {
        if (element != null) {
            element.setText(value);
        }
    }

    /**
     * 獲取某個節點下的某個屬性的boolean類型的值
     *
     * @param element
     * @param propName
     * @param defaultValue
     * @return
     */
    public static boolean getPropValue(Element element, String propName, boolean defaultValue) {
        String value = getPropValue(element, propName);
        try {
            return StringUtils.isNotBlank(value) ? Boolean.parseBoolean(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點下的某個屬性的double類型的值
     *
     * @param element
     * @param propName
     * @param defaultValue
     * @return
     */
    public static double getPropValue(Element element, String propName, double defaultValue) {
        String value = getPropValue(element, propName);
        try {
            return StringUtils.isNotBlank(value) ? Double.parseDouble(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點下的某個屬性的long類型的值
     *
     * @param element
     * @param propName
     * @param defaultValue
     * @return
     */
    public static long getPropValue(Element element, String propName, long defaultValue) {
        String value = getPropValue(element, propName);
        try {
            return StringUtils.isNotBlank(value) ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點下的某個屬性的int類型的值
     *
     * @param element
     * @param propName
     * @param defaultValue
     * @return
     */
    public static int getPropValue(Element element, String propName, int defaultValue) {
        String value = getPropValue(element, propName);
        try {
            return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個節點下的某個屬性的String類型的值
     *
     * @param element
     * @param propName
     * @param defaultValue
     * @return
     */
    public static String getPropValue(Element element, String propName, String defaultValue) {
        String value = getPropValue(element, propName);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    /**
     * 獲取某個節點下的某個屬性的文本
     *
     * @param element
     * @param propName
     * @return
     */
    private static String getPropValue(Element element, String propName) {
        if (element != null) {
            if (element.getAttribute(propName) != null) {
                return element.getAttributeValue(propName);
            }
        }
        return null;
    }

    /**
     * 給某個節點下的某個屬性塞入boolean類型的值
     *
     * @param element
     * @param propName
     * @param value
     */
    public static void setPropValue(Element element, String propName, boolean value) {
        setPropValue(element, propName, String.valueOf(value));
    }

    /**
     * 給某個節點下的某個屬性塞入Number類型的值
     *
     * @param element
     * @param propName
     * @param value
     */
    public static void setPropValue(Element element, String propName, Number value) {
        setPropValue(element, propName, String.valueOf(value));
    }

    /**
     * 給某個節點下的某個屬性塞入String類型的值
     *
     * @param element
     * @param propName
     * @param value
     */
    public static void setPropValue(Element element, String propName, String value) {
        if (element != null) {
            if (element.getAttribute(propName) == null) {
                element.setAttribute(propName, value);
            } else {
                element.getAttribute(propName).setValue(value);
            }
        }
    }

    /**
     * 判斷屬性是否存在
     *
     * @param element
     * @param propName
     * @return
     */
    public static boolean hasProp(Element element, String propName) {
        if (element != null) {
            return element.getAttribute(propName) != null;
        }
        return false;
    }

    /**
     * 判斷節點是否存在
     *
     * @param element
     * @param tagName
     * @return
     */
    public static boolean hasElement(Element element, String tagName) {
        if (element != null) {
            return element.getChild(tagName) != null;
        }
        return false;
    }

    /**
     * 獲取某個父節點下子節點的boolean類型的值
     *
     * @param parent
     * @param tagName
     * @param defaultValue
     * @return
     */
    public static boolean getChildElementValue(Element parent, String tagName, boolean defaultValue) {
        String value = getChildElementValue(parent, tagName);
        try {
            return StringUtils.isNotBlank(value) ? Boolean.parseBoolean(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個父節點下子節點的double類型的值
     *
     * @param parent
     * @param tagName
     * @param defaultValue
     * @return
     */
    public static double getChildElementValue(Element parent, String tagName, double defaultValue) {
        String value = getChildElementValue(parent, tagName);
        try {
            return StringUtils.isNotBlank(value) ? Double.parseDouble(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個父節點下子節點的long類型的值
     *
     * @param parent
     * @param tagName
     * @param defaultValue
     * @return
     */
    public static long getChildElementValue(Element parent, String tagName, long defaultValue) {
        String value = getChildElementValue(parent, tagName);
        try {
            return StringUtils.isNotBlank(value) ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個父節點下子節點的int類型的值
     *
     * @param parent
     * @param tagName
     * @param defaultValue
     * @return
     */
    public static int getChildElementValue(Element parent, String tagName, int defaultValue) {
        String value = getChildElementValue(parent, tagName);
        try {
            return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : defaultValue;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * 獲取某個父節點下子節點的String類型的值
     *
     * @param parent
     * @param tagName
     * @param defaultValue
     * @return
     */
    public static String getChildElementValue(Element parent, String tagName, String defaultValue) {
        String value = getChildElementValue(parent, tagName);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    /**
     * 獲取某個父節點下子節點的文本
     *
     * @param parent
     * @param tagName
     * @return
     */
    private static String getChildElementValue(Element parent, String tagName) {
        if (parent != null) {
            Element child = parent.getChild(tagName);
            if (child != null) {
                return child.getTextTrim();
            }
        }
        return null;
    }

    /**
     * 給某父節點下的子節點塞入boolean類型的值
     *
     * @param element
     * @param tagName
     * @param value
     */
    public static void setChildElementValue(Element element, String tagName, boolean value) {
        setPropValue(element, tagName, String.valueOf(value));
    }

    /**
     * 給某父節點下的子節點塞入Number類型的值
     *
     * @param element
     * @param tagName
     * @param value
     */
    public static void setChildElementValue(Element element, String tagName, Number value) {
        setPropValue(element, tagName, String.valueOf(value));
    }

    /**
     * 給某父節點下的子節點塞入String類型的值
     *
     * @param element
     * @param tagName
     * @param value
     */
    public static void setChildElementValue(Element element, String tagName, String value) {
        if (element != null) {
            if (element.getChild(tagName) == null) {
                Element child = new Element(tagName);
                child.setText(value);
                element.addContent(child);
            } else {
                element.getChild(tagName).setText(value);
            }
        }
    }

    public static <T> String jaxbToXML(T object, boolean... formatted) throws Exception {
        return jaxbToXML(object, StandardCharsets.UTF_8.name(), formatted);
    }

    public static <T> String jaxbToXML(T object, String encoding, boolean... formatted) throws Exception {
        String result = null;
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        // 指定是否使用换行和缩排对已编组 XML 数据进行格式化的属性名称。
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, ArrayUtils.isNotEmpty(formatted) && formatted[0]);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
        try (StringWriter writer = new StringWriter()) {
            marshaller.marshal(object, writer);
            result = writer.toString();
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T jaxbFromXML(String xml, Class<T> clazz) throws Exception {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        try (StringReader reader = new StringReader(xml)) {
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        }
    }
}
