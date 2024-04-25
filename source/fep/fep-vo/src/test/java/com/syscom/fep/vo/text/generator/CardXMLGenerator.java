package com.syscom.fep.vo.text.generator;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Richard
 */
public class CardXMLGenerator {
    private LogHelper logger = LogHelperFactory.getUnitTestLogger();
    private static final String BASE_PATH_SOURCE = "E:\\FEP\\documents\\FEP_Recent\\20221122_Card\\CARD電文\\";
    private static final String BASE_PATH_SOURCE_REQUEST = BASE_PATH_SOURCE + "Request\\";
    private static final String BASE_PATH_SOURCE_RESPONSE = BASE_PATH_SOURCE + "Respone\\";
    private static final String HEADER_PATH_SOURCE_REQUEST = BASE_PATH_SOURCE_REQUEST + "Header.xlsx";
    private static final String HEADER_PATH_SOURCE_RESPONSE = BASE_PATH_SOURCE_RESPONSE + "Header.xlsx";
    private static final String BASE_PATH_TARGE = "E:\\FEP\\documents\\FEP_Recent\\20221122_Card\\CARD電文\\Target\\";
    private static final String BASE_PATH_TARGE_REQUEST = BASE_PATH_TARGE + "request\\";
    private static final String BASE_PATH_TARGE_RESPONSE = BASE_PATH_TARGE + "response\\";
    private static final int HEADER_SHEET_INDEX = 0;
    private static final int HEADER_ROW_START = 2;
    private static final int HEADER_CELL_INDEX_FIELD_NAME = 1;
    private static final int EACH_SHEET_INDEX = 0;
    private static final int EACH_ROW_INFO = 1;
    private static final int EACH_ROW_START = 2;
    private static final int EACH_CELL_INDEX_FIELD_NAME = 1;
    private static final int EACH_CELL_INDEX_CLASS_NAME = 2;
    private static final int EACH_CELL_INDEX_FILE_NAME = 3;
    private static final int EACH_CELL_INDEX_REMARK = 4;

    private static final String ENTER = "\r\n";
    private static final String PACKAGE_RM = "com.syscom.fep.vo.text.card";
    private static final String PACKAGE_RM_REQUEST = PACKAGE_RM + ".request";
    private static final String PACKAGE_RM_RESPONSE = PACKAGE_RM + ".response";
    private static final String ELEMENT_ROOT = "BlueStar";
    private static final String HEADER_REQUEST_CLASS_NAME = "CardHeaderRequest";
    private static final String HEADER_RESPONSE_CLASS_NAME = "CardHeaderResponse";
    private static final String FILE_NAME_PREFIX = "EAI";
    private static final String ELEMENT_ROOT_CONSTANT = "ELEMENT_ROOT";

    @Test
    public void generate() throws Exception {
        // prepare
        // headerRequest
        Header headerRequest = header(HEADER_PATH_SOURCE_REQUEST);
        headerRequest.attributeList.add(new Field("timestamp", "XStreamAsAttribute"));
        headerRequest.attributeList.add(new Field("BWSeqNo", "XStreamAsAttribute"));
        headerRequest.attributeList.add(new Field("RqUid", "XStreamAsAttribute"));
        headerRequest.attributeList.add(new Field("MsgName", "XStreamAsAttribute"));
        headerRequest.attributeList.add(new Field("ClientIP", "XStreamAsAttribute"));
        headerRequest.attributeList.add(new Field("xmlns", "XStreamAsAttribute"));
        // headerResponse
        Header headerResponse = header(HEADER_PATH_SOURCE_RESPONSE);
        headerResponse.attributeList.add(new Field("MsgName", "XStreamAsAttribute"));
        headerResponse.attributeList.add(new Field("RqUid", "XStreamAsAttribute"));
        headerResponse.attributeList.add(new Field("xmlns", "XStreamAsAttribute"));
        headerResponse.attributeList.add(new Field("Status", "XStreamAsAttribute"));
        // field
        List<Each> eachRequestList = eachAll(BASE_PATH_SOURCE_REQUEST);
        List<Each> eachResponseList = eachAll(BASE_PATH_SOURCE_RESPONSE);
        // generate
        String generateHeaderRequest = generateHeader(HEADER_REQUEST_CLASS_NAME, headerRequest);
        String generateHeaderResponse = generateHeader(HEADER_RESPONSE_CLASS_NAME, headerResponse);
        FileUtils.write(new File(BASE_PATH_TARGE + HEADER_REQUEST_CLASS_NAME + ".java"), generateHeaderRequest, StandardCharsets.UTF_8, false);
        FileUtils.write(new File(BASE_PATH_TARGE + HEADER_RESPONSE_CLASS_NAME + ".java"), generateHeaderResponse, StandardCharsets.UTF_8, false);
        for (Each each : eachRequestList) {
            String className = each.classname + "Request";
            String generateEach = generateEach(HEADER_REQUEST_CLASS_NAME, PACKAGE_RM_REQUEST, className, each, StringUtils.join(HEADER_REQUEST_CLASS_NAME, ".", ELEMENT_ROOT_CONSTANT));
            FileUtils.write(new File(BASE_PATH_TARGE_REQUEST + className + ".java"), generateEach, StandardCharsets.UTF_8, false);
        }
        for (Each each : eachResponseList) {
            String className = each.classname + "Response";
            String generateEach = generateEach(HEADER_RESPONSE_CLASS_NAME, PACKAGE_RM_RESPONSE, className, each, StringUtils.join(HEADER_RESPONSE_CLASS_NAME, ".", ELEMENT_ROOT_CONSTANT));
            FileUtils.write(new File(BASE_PATH_TARGE_RESPONSE + className + ".java"), generateEach, StandardCharsets.UTF_8, false);
        }
    }

    private Header header(String path) throws Exception {
        Header header = new Header();
        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(HEADER_SHEET_INDEX);
            for (int rowIndex = HEADER_ROW_START; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                Cell cell = row.getCell(HEADER_CELL_INDEX_FIELD_NAME);
                if (cell == null) continue;
                String fieldName = cell.getStringCellValue().trim();
                if (StringUtils.isBlank(fieldName)) continue;
                if (header.fieldList.stream().filter(t -> t.name.equals(fieldName)).findFirst().orElse(null) != null) {
                    logger.warn("Field Name already exist, fieldName = [", fieldName, "], path = [", path, "] ignore!!!");
                    continue;
                }
                header.fieldList.add(new Field(fieldName));
            }
        }
        return header;
    }

    private List<Each> eachAll(String basePath) throws Exception {
        File dir = new File(basePath);
        File[] files = dir.listFiles(f -> {
            return "xlsx".equalsIgnoreCase(FilenameUtils.getExtension(f.getName()))
                    && !"Header.xlsx".equalsIgnoreCase(f.getName());
        });
        List<Each> eachList = new ArrayList<>();
        for (File file : files) {
            eachList.add(each(file.getAbsolutePath()));
        }
        return eachList;
    }

    private Each each(String path) throws Exception {
        Each each = new Each();
        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(EACH_SHEET_INDEX);
            each.classname = getCellValue(sheet, EACH_ROW_INFO, EACH_CELL_INDEX_CLASS_NAME);
            if (StringUtils.isBlank(each.classname)) {
                logger.warn("Class Name is empty, path = [", path, "] ignore!!!");
                each.classname = FILE_NAME_PREFIX + file.getName().substring(0, 4);
            }
            // each.filename = getCellValue(sheet, EACH_ROW_INFO, EACH_CELL_INDEX_FILE_NAME);
            for (int rowIndex = EACH_ROW_START; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                Cell cell = row.getCell(EACH_CELL_INDEX_FIELD_NAME);
                if (cell == null) continue;
                String fieldName = cell.getStringCellValue().trim();
                if (StringUtils.isBlank(fieldName)) continue;
                each.fieldList.add(new Field(
                        fieldName,
                        getCellValue(row, EACH_CELL_INDEX_FILE_NAME),
                        getCellValue(row, EACH_CELL_INDEX_REMARK)));
                List<Field> duplicateFieldList = each.fieldList.stream().filter(t -> t.xmlName.equals(fieldName)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(duplicateFieldList) && duplicateFieldList.size() > 1) {
                    for (int i = 0; i < duplicateFieldList.size(); i++) {
                        Field duplicateField = duplicateFieldList.get(i);
                        duplicateField.annotation = StringUtils.join("XStreamAlias(\"", fieldName, "\")");
                        duplicateField.name = StringUtils.join(fieldName, StringUtils.leftPad(String.valueOf(i + 1), 2, '0'));
                    }
                    each.hasDuplicateField = true;
                }
            }
        }
        return each;
    }

    private String getCellValue(Sheet sheet, int rowIndex, int cellIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            return getCellValue(row, cellIndex);
        }
        return StringUtils.EMPTY;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell != null) {
            String value = cell.getStringCellValue();
            if (value != null) {
                return value.trim();
            }
        }
        return StringUtils.EMPTY;
    }

    private String generateHeader(String classname, Header header) {
        StringBuilder sb = new StringBuilder();
        // package
        sb.append("package ").append(PACKAGE_RM).append(";").append(ENTER);
        sb.append(ENTER);
        // import
        sb.append("import com.thoughtworks.xstream.annotations.XStreamAsAttribute;").append(ENTER);
        sb.append(ENTER);
        // mark
        sb.append("/**").append(ENTER);
        sb.append(" * This file was generate by Program ").append(this.getClass().getSimpleName()).append(ENTER);
        sb.append(" *").append(ENTER);
        sb.append(" * @author Richard").append(ENTER);
        sb.append(" */").append(ENTER);
        // class
        sb.append("public class ").append(classname).append(" {").append(ENTER);
        // constant
        sb.append("\tpublic static final String ").append(ELEMENT_ROOT_CONSTANT).append(" = \"").append(ELEMENT_ROOT).append("\";").append(ENTER);
        // attribute
        for (Field field : header.attributeList) {
            sb.append(field.generateFieldDeclare());
        }
        // field
        for (Field field : header.fieldList) {
            sb.append(field.generateFieldDeclare());
        }
        sb.append(ENTER);
        // attribute getter & setter
        for (Field field : header.attributeList) {
            sb.append(field.generateMethod());
            sb.append(ENTER);
        }
        // getter & setter
        for (Field field : header.fieldList) {
            sb.append(field.generateMethod());
            sb.append(ENTER);
        }
        sb.append("}");
        return sb.toString();
    }

    private String generateEach(String extendClassname, String packagename, String classname, Each each, String elementRootConstant) {
        StringBuilder sb = new StringBuilder();
        // package
        sb.append("package ").append(packagename).append(";").append(ENTER);
        sb.append(ENTER);
        // import
        if (each.hasDuplicateField) {
            sb.append("import com.syscom.fep.frmcommon.xs.converter.XsDuplicateFieldReflectionConverter;").append(ENTER);
        }
        sb.append("import ").append(PACKAGE_RM).append(".").append(extendClassname).append(";").append(ENTER);
        sb.append("import com.thoughtworks.xstream.annotations.XStreamAlias;").append(ENTER);
        if (each.hasDuplicateField) {
            sb.append("import com.thoughtworks.xstream.annotations.XStreamConverter;").append(ENTER);
        }
        sb.append(ENTER);
        // mark
        sb.append("/**").append(ENTER);
        sb.append(" * This file was generate by Program ").append(this.getClass().getSimpleName()).append(ENTER);
        sb.append(" *").append(ENTER);
        sb.append(" * @author Richard").append(ENTER);
        sb.append(" */").append(ENTER);
        // class
        sb.append("@XStreamAlias(").append(elementRootConstant).append(")").append(ENTER);
        if (each.hasDuplicateField) {
            sb.append("@XStreamConverter(XsDuplicateFieldReflectionConverter.class)").append(ENTER);
        }
        sb.append("public class ").append(classname).append(" extends ").append(extendClassname).append(" {").append(ENTER);
        // field
        for (Field field : each.fieldList) {
            sb.append(field.generateFieldDeclare());
        }
        sb.append(ENTER);
        // getter & setter
        for (Field field : each.fieldList) {
            sb.append(field.generateMethod());
            sb.append(ENTER);
        }
        sb.append("}");
        return sb.toString();
    }

    private class Header {
        public String classname;
        public String filename;
        public List<Field> attributeList = new ArrayList<>();
        public List<Field> fieldList = new ArrayList<>();
    }

    private class Each {
        public String classname;
        // public String filename;
        public List<Field> fieldList = new ArrayList<>();
        public boolean hasDuplicateField;
    }

    private class Field {
        public String name;
        public String desc;
        public String remark;
        public String annotation;
        public String xmlName;

        public Field(String name) {
            this.name = name;
        }

        public Field(String name, String annotation) {
            this.name = name;
            this.annotation = annotation;
            this.xmlName = name;
        }

        public Field(String name, String desc, String remark) {
            this.name = name;
            this.desc = desc;
            this.remark = remark;
            this.xmlName = name;
        }

        public String generateFieldDeclare() {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(desc) || StringUtils.isNotBlank(remark)) {
                sb.append("\t/**").append(ENTER);
                if (StringUtils.isNotBlank(desc))
                    sb.append("\t * ").append(desc).append(ENTER);
                if (StringUtils.isNotBlank(remark))
                    sb.append("\t * ").append(remark).append(ENTER);
                sb.append("\t */").append(ENTER);
            }
            if (StringUtils.isNotBlank(annotation))
                sb.append("\t@").append(annotation).append(ENTER);
            sb.append("\tprivate String ").append(this.name).append(";").append(ENTER);
            return sb.toString();
        }

        public String generateMethod() {
            StringBuilder sb = new StringBuilder();
            // getter
            if (StringUtils.isNotBlank(desc) || StringUtils.isNotBlank(remark)) {
                sb.append("\t/**").append(ENTER);
                if (StringUtils.isNotBlank(desc))
                    sb.append("\t * ").append(desc).append(ENTER);
                if (StringUtils.isNotBlank(remark))
                    sb.append("\t * ").append(remark).append(ENTER);
                sb.append("\t */").append(ENTER);
            }
            sb.append("\tpublic String get").append(this.name).append("() {").append(ENTER);
            sb.append("\t\treturn this.").append(this.name).append(";").append(ENTER);
            sb.append("\t}").append(ENTER);
            // enter
            sb.append(ENTER);
            // setter
            if (StringUtils.isNotBlank(desc) || StringUtils.isNotBlank(remark)) {
                sb.append("\t/**").append(ENTER);
                if (StringUtils.isNotBlank(desc))
                    sb.append("\t * ").append(desc).append(ENTER);
                if (StringUtils.isNotBlank(remark))
                    sb.append("\t * ").append(remark).append(ENTER);
                sb.append("\t */").append(ENTER);
            }
            sb.append("\tpublic void set").append(this.name).append("(String ").append(this.name).append(") {").append(ENTER);
            sb.append("\t\tthis.").append(this.name).append(" = ").append(this.name).append(";").append(ENTER);
            sb.append("\t}").append(ENTER);
            return sb.toString();
        }
    }
}
