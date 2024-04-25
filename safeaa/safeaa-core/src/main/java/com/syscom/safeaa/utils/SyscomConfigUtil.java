package com.syscom.safeaa.utils;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SyscomConfigUtil {
//    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        //1.創建Reader對象
        SAXReader reader = new SAXReader();
        //2.加載xml
        Document document = reader.read(new File("safeaa-core/src/main/resources/SyscomConfig.xml"));
        //3.獲取根節點
        Element rootElement = document.getRootElement();
        Iterator<?> iterator = rootElement.elementIterator();
        while (iterator.hasNext()){
            Element stu = (Element) iterator.next();

//            List<Attribute> attributes = stu.attributes();
//            for (Attribute attribute : attributes) {
//                System.out.println("-----"+ attribute.getValue());
//            }

            Iterator<?> iterator1 = stu.elementIterator();
            while (iterator1.hasNext()){
//                Element stuChild = (Element) iterator1.next();
            }
        }
    }
}