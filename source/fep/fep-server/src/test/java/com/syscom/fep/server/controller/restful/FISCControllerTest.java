package com.syscom.fep.server.controller.restful;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.ext.mapper.CbstestdataExtMapper;
import com.syscom.fep.mybatis.model.Cbstestdata;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.mybatis.util.StanGenerator;
import com.syscom.fep.server.ServerBaseTest;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.communication.*;
import com.syscom.fep.vo.text.ims.AB_ACC_I01;
import com.syscom.fep.vo.text.ims.AB_ACC_O01;
import com.syscom.fep.vo.text.ims.CB_IQTX_O001;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

//@ActiveProfiles({"mybatis", "jms-localhost", "integration", "taipei"})
public class FISCControllerTest extends ServerBaseTest {
    @Autowired
    private StanGenerator generator;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() throws Exception {
        SpringBeanFactoryUtil.registerController(FISCController.class);
    }

    @AfterAll
    public void tearDown() throws Exception {
        SpringBeanFactoryUtil.unregisterBean(FISCController.class);
    }

    /**
     * 餘額查詢交易-台灣卡
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2500() throws Exception {
        String message =
                "000000303230303235303035353130363136303036303030303935303030303031333031333130393239353230303030984B4A6F04A00010000013013033393431303030303030303435363430363931343832303630313130303831323031303030363434343436383132303130303036343434343630313041313233343536373839313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);
    }
    @Test
    public void teststan() throws Exception {
        String a = "aaa;fff;ggg;eee";
        String[] to = ArrayUtils.toArray(a);
        List<String> tts = StringUtil.split(to[0], ',', ';');
        System.out.println(tts);
//        int message = 15666623;
//        StanGenerator stanGenerator = SpringBeanFactoryUtil.getBean(StanGenerator.class);
//        String generateStan = stanGenerator.generate(message);
//
//        System.out.println(generateStan);
//        this.testProcessRequestData(message);
    }
    @Test
    public void testCBS() {
        CB_IQTX_O001 tota = new CB_IQTX_O001();
        tota.setOUTMSGID("202312271033000001843800");
        tota.setOUTMSGF("Y");
        tota.setOUTDATE("20231227");
        tota.setOUTTIME("103500");
        tota.setOUTSERV("FEP1");
        tota.setOUTTD("910A0011");
        tota.setOUTAP("ATFEP");
        tota.setOUTFF("C");
        tota.setOUTPGNO("001");
        tota.setOUTRTC("4001");
        tota.setTXDATE("1121227");
        tota.setFG_TXDATE("1121227");
        tota.setTX_ACCT_FLAG("Y");
        tota.setTX_RVS_FLAG("Y");

        String output = tota.makeMessage();
        System.out.print(output);
    }

    public String makeMessage(Object tita, String deductCode) throws Exception {
        Class<?> cls = tita.getClass();
        Class<?>[] params = {};
        // 取出bean裡的所有方法
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        String rtnTita = "";// 電文字串
        String fieldType = "";
        String fieldSetName = "";
        String fieldGetName = "";
        Method fieldSetMet = null;
        Method fieldGetMet = null;
        int size = 0;
        String value = null;
        for (java.lang.reflect.Field field : fields) {
            if ("_TotalLength".equals(field.getName())) {
                continue;
            }
            if (field.getAnnotation(Field.class) != null) {
                size = field.getAnnotation(Field.class).length();
            }
            fieldSetName = "set" + field.getName();
            fieldGetName = "get" + field.getName();
            fieldSetMet = cls.getDeclaredMethod(fieldSetName, field.getType());
            fieldGetMet = cls.getDeclaredMethod(fieldGetName, params);
            fieldType = field.getType().getSimpleName();
            value = Objects.toString(fieldGetMet.invoke(tita), "");
            if (StringUtils.isNotBlank(value)) {
                if ("Integer".equals(fieldType) || "int".equals(fieldType)) {
                    value = Objects.toString(Integer.parseInt(value), "");
                } else if ("Long".equalsIgnoreCase(fieldType)) {
                    value = Objects.toString(Long.parseLong(value), "");
                } else if ("Double".equalsIgnoreCase(fieldType)) {
                    value = Objects.toString(Double.parseDouble(value), "");
                } else if ("Boolean".equalsIgnoreCase(fieldType)) {
                    value = Objects.toString(Boolean.parseBoolean(value), "");
                }
            }
            if ("BigDecimal".equalsIgnoreCase(fieldType)) {
                fieldSetMet.invoke(tita, new BigDecimal(Objects.toString(StringUtils.trimToNull(value), "0")));
                rtnTita += StringUtils.leftPad(Objects.toString(fieldGetMet.invoke(tita)), size, "0");
            } else {
                fieldSetMet.invoke(tita, StringUtils.rightPad(Objects.toString(value, ""), size, " "));
                rtnTita += fieldGetMet.invoke(tita).toString();
            }
        }
        // 依照不同下送的方式扣除電碼
        int num = Integer.parseInt(deductCode);
        if (num > 0) {
            if (rtnTita.length() > num) {
                rtnTita = rtnTita.substring(0, rtnTita.length() - num);
            }
        }
        return rtnTita;
    }

    @Test
    public void testJos() throws Exception {
        String atmno= EbcdicConverter.toHex(CCSID.English, "T9998I01".length(), "T9998I01");

        System.out.println(atmno);
//        BinaryTree binaryTree =new BinaryTree(10);
//        binaryTree.insert(5);
//        binaryTree.insert(3);
//        binaryTree.insert(8);
//        binaryTree.insert(2);
//        binaryTree.insert(4);
//        binaryTree.insert(7);
//        binaryTree.insert(9);
//        binaryTree.printInOrder(3);

//        rs16("",10);
//        String b= "20230613";
//
//        String d= "";
//        LocalDate c= LocalDate.parse(a, DateTimeFormatter.ofPattern("yyyyMMdd"));
//        LocalDate e= LocalDate.parse(b, DateTimeFormatter.ofPattern("yyyyMMdd"));
//
//        LocalTime currentTime = LocalTime.now();
//        LocalTime targetTime = LocalTime.of(18, 0);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
//        Integer s = Integer.parseInt(currentTime.format(formatter));
//        if(c.isBefore(e) && targetTime.isAfter(currentTime)){
//            d = "222";
//        }
//        BigDecimal b = new BigDecimal("800.00");
//        String ab = b.toString();
//
//        String aa =  Integer.toString((int)(Double.valueOf(String.valueOf(b))*100));
//
//        String aac = "+" +aa;




//        String CardNo = "601039123456789012";
//
//        String BASE_VISA = "";
//        if (CardNo.length() < 18) {
//
//        }
//        int SUM_VISA = 0;
//        String GWK_MUL_VISA = "212121212121212121";
//        for (int i = 0; i < 18; i++) {
//            int WK_PRODUCT = Integer.parseInt(String.valueOf(CardNo.charAt(i))) * Integer.parseInt(String.valueOf(GWK_MUL_VISA.charAt(i)));
//            String WK = StringUtils.leftPad(String.valueOf(WK_PRODUCT),2,"0");
//            int TWK = 0;
//            for (int j = 0; j < 2; j++) {
//                TWK = TWK + Integer.parseInt(String.valueOf(WK.charAt(j)));
//            }
//            SUM_VISA = SUM_VISA + TWK;
//        }
//        SUM_VISA = SUM_VISA %= 100;
//        String SUM = StringUtils.leftPad(String.valueOf(SUM_VISA), 2, "0");
//        BASE_VISA = SUM.substring(0, 1) + "0";
//
//        Integer CHK_BYTE = Math.abs(Integer.valueOf(BASE_VISA) - SUM_VISA);


    }
    class BinaryTree {
        private int[] treeArray;
        private int size;

        public BinaryTree(int maxSize) {
            treeArray = new int[maxSize];
            size = 0;
        }

        public void insert(int value) {
            if (size < treeArray.length) {
                treeArray[size] = value;
                size++;
            } else {
                System.out.println("Binary tree is full. Cannot insert more elements.");
            }
        }

        public void printInOrder(int index) {
            if (index < size) {
                printInOrder(2 * index + 1); // Left child
                System.out.print(treeArray[index] + " ");
                printInOrder(2 * index + 2); // Right child
            }
        }
    }
    public String rs16(String name,Integer len){
        String message ="";
        if(StringUtils.isNotBlank(name)){
            BigDecimal amt = new BigDecimal(name);
            name = String.format("%.0f",(Double.valueOf(name) * 100));
            if(amt.compareTo(BigDecimal.ZERO) >= 0 ) {
                message = "+"+StringUtils.leftPad(name , len-1, '0').replace(".", "");
            }else {
                message = "-"+StringUtils.leftPad(name , len-1, '0').replace(".", "").replace("-","");
            }
        }
        return message;
    }
    protected String bigDecimalToEbcdic( //
                                         BigDecimal number, //
                                         int numberLen, //
                                         int floatLen, //
                                         boolean hasSign //
    ) {
        // 數字為null，返回空字串
        if (number == null) {
            int signLen = hasSign ? 1 : 0;
            int totalLen = signLen + numberLen + floatLen;
            return EbcdicConverter.toHex(CCSID.English, totalLen, StringUtils.EMPTY);
        }
        // 小數點後長度
        String floatLenFormatStr = StringUtils.EMPTY;
        for (int i = 0; i < floatLen; i++) {
            floatLenFormatStr = floatLenFormatStr + "0";
            number = number.multiply(new BigDecimal(10));
        }
        // 小數點前長度
        String numberLenFormatStr = StringUtils.EMPTY;
        for (int i = 0; i < numberLen; i++) {
            numberLenFormatStr = numberLenFormatStr + "0";
        }
        // +0000000;-0000000
        String format = "%s%s%s;%s%s%s";
        String decimalFormatStr = String.format( //
                format, //
                hasSign ? "+" : StringUtils.EMPTY, // 正數符號
                numberLenFormatStr, //
                floatLenFormatStr, //
                hasSign ? "-" : StringUtils.EMPTY, //
                numberLenFormatStr, //
                floatLenFormatStr //
        );
        // 當不顯示符號，取絕對值，否則因為位數不對導致Exception
        if (!hasSign) {
            number = number.abs();
        }
        // BigDecimal to String
        DecimalFormat decimalFormat = new DecimalFormat(decimalFormatStr);
        String numberStr = decimalFormat.format(number);
        // 總長度
        int signLen = hasSign ? 1 : 0;
        int totalLen = signLen + numberLen + floatLen;
        return EbcdicConverter.toHex(CCSID.English, totalLen, numberStr);
    }
    protected BigDecimal asciiToBigDecimal( //
                                            String numberStr, //
                                            int numberLen, //
                                            int floatLen, //
                                            boolean hasSign //
    ) throws ParseException {
//		String numberStr = EbcdicConverter.fromHex(CCSID.English, numberStr);
        // 字串為空，返回null
        if(numberStr == null || StringUtils.equals(numberStr.trim(), StringUtils.EMPTY)) {
            return null;
        }
        // 小數點後長度，組format
        String floatLenFormatStr = StringUtils.EMPTY;
        for (int i = 0; i < floatLen; i++) {
            floatLenFormatStr = floatLenFormatStr + "0";
        }
        // 小數點前長度
        String numberLenFormatStr = StringUtils.EMPTY;
        for (int i = 0; i < numberLen; i++) {
            numberLenFormatStr = numberLenFormatStr + "0";
        }
        // +0000000;-0000000
        String format = "%s%s%s;%s%s%s";
        String decimalFormatStr = String.format( //
                format, //
                hasSign ? "+" : StringUtils.EMPTY, // 正數符號
                numberLenFormatStr, //
                floatLenFormatStr, //
                hasSign ? "-" : StringUtils.EMPTY, //
                numberLenFormatStr, //
                floatLenFormatStr //
        );
        // String to BigDecimal
        DecimalFormat decimalFormat = new DecimalFormat(decimalFormatStr);
        BigDecimal number = new BigDecimal(decimalFormat.parse(numberStr).toString());
        // 小數點後長度，數值轉為小數
        for (int i = 0; i < floatLen; i++) {
            number = number.divide(new BigDecimal(10));
        }
        return number;
    }
    /**
     * 提款交易-台灣卡
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2510() throws Exception {
        String message =
                "000000303230303235313035353130363130303036303030303831353030303031333031333031343135303330303030984B4A6F24A02001000013012B30303030303030313030303030393130333241202030303031343433303034333032414144323032343031333031343135303331313031313430303831323031303030363434343436383236303031303030303031363830303038333333343232373331303030000A5927F173D074AFAEA3FFB78D";

        this.testProcessRequestData(message);
//
//        message =
//                "000000303230323235313035353038313737303036303030303935303030303031323033323431363139353734303031984B4A6F24000000000000012B303030303030303130303030303931303332412020343C56E1";
//        this.testProcessRequestData(message);
    }

    /**
     * 提款交易確認-台灣卡
     *
     * @throws Exception
     */
    @Test
    public void testCommonConfirmI2510() throws Exception {
        String message =
                "000000303230323235313035343934303232383037303030303935303030303031303036313631303339343334303031984B4A6F24000000000000012B303030303030303130303030303931303332412020C70B59E1";

        // 以下來自Candy測試的電文
        message =
                "000000303230323235313035353039373530303036303030303831353030303031323036313331353036343534303031984B4A6F24000000000000012B303030303030303130303030303931303332412020E29A513F";
        this.testProcessRequestData(message);
    }

    /**
     * 轉入交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2521() throws Exception {
//        String message =
//                "000000303230303235323135353038353230303036303030303031323030303031323035303231353038353130303030984B4A6F240C0011000030012B3030303030303038303030303031323334303131203030363030303030313230303030363533383131303131343030313539303034303030303435303530303030363836313638313839393036C3962483";
//        this.testProcessRequestData(message);

        String message =
                "000000303230303235323135353130363138303036303030303031323030303031333031333131303231323930303030984B4A6F240C0011000030012B3030303030303038303030303036383634303131203030363030303030313230303030363533383131303131343030313539303034303030303435303530303030363836313638313839393036A3FFB78D";
        this.testProcessRequestData(message);
    }

    /**
     * 轉入交易確認
     *
     * @throws Exception
     */
    @Test
    public void testCommonConfirmI2521() throws Exception {
        String message =
                "00000030323032323532313031393931313238303730303030303132303030303038303930353036333131303430303171AB9FEA24000000000000012B3030303030303038303030303036383634303131202636B343";
        message = "000000303230323235323135353130343038303036303030303031323030303031323131313531313331343534303031984B4A6F24000000000000012B303030303030303830303030303638363430313120243D01C4";
        this.testProcessRequestData(message);
    }

    /**
     * 轉出交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2522() throws Exception {
        String message =
                "000000303230303235323235353038313633303036303030303830363030303031323033323330393534333630303030984B4A6F24AC2011000033012B3030303030303031303030303039313033324120203030303030353435303433323231413338303630303030383037303030303230323330323231303935343336363031313131303131343030323031303230303030313136373930303033393030343030303033333933303339303034303030303333393330313030353038383036323739393230000A7CC0D616041B9C8FC02BB775";
        this.testProcessRequestData(message);
        message =
                "000000303230323235323235353038313633303036303030303830363030303031323033323330393534333634303031984B4A6F24000000000000012B303030303030303130303030303931303332412020242C7B9F";
        this.testProcessRequestData(message);
    }

    /**
     * 轉出交易確認
     *
     * @throws Exception
     */
    @Test
    public void testCommonConfirmI2522() throws Exception {
        String message =
                "0000003032303232353232323837353837393830373030303038303630303030303830373239313635373431343030317D37B74624000000000000012B303030303030303130303030303931303332412020E9A7B6B8";
        this.testProcessRequestData(message);
    }

    /**
     * 自行轉帳交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2523() throws Exception {
        String message =
                "000000303230303235323335353130353836303036303030303830363030303031333031323431353537343430303030984B4A6F24AC2011000033012B3030303030303031303030303039313033324120203030303030303033303433323231414330303638393939303036303030303230323430313234313535373434363031313131303131343030383132303130303036343434343630303831323031303030323434343438383132303130303032343434343830313041313233343536373839313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);
        message =
                "000000303230323235323335353130353232303036303030303436313030303031333031303431303131333334303031984B4A6F24000000000000012B303030303030303130303030303931303332412020A3FFB78D";
        this.testProcessRequestData(message);

    }

    /**
     * 合庫自行轉帳交易
     *
     * @throws Exception
     */
    @Test
    public void testRequestI2523() throws Exception {
        String message =
                "000000303230303235323335353130343833303036303030303830363030303031323132323531313530323530303030984B4A6F24AC2011000033012B30303030303030313030303030393130333241202030303030343630383034333232314143303036303030303030363030303032303233313232353131353032353630313131313031313430303039393936393836303030323235393939373736353333383832313031304E3132313736393736363939393737363533333838323130312020202037000A9608E19EAA40C37DF2353DFB";
        this.testProcessRequestData(message);
    }

    /**
     * 自行轉帳交易確認
     *
     * @throws Exception
     */
    @Test
    public void testCommonConfirmI2523() throws Exception {
        String message =
                "000000303230323235323335343934323437383037303030303830363030303031303036323331363334323130353031984B4A6F24000000000000012B303030303030303130303030303931303332412020B91F0C63";
        this.testProcessRequestData(message);
    }

    /**
     * 跨行轉帳交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2524() throws Exception {
        String message =
                "000000303230303235323435353038313536303036303030303935303030303031323033323331373535323630303030984B4A6F240C0001000030012B30303030303030313635303030303339353730303038303730303030303036303030303131303131343030313231303033303039313133373239393938383732313230363331303130E12FCDDE";
        this.testProcessRequestData(message);

        message =
                "000000303230323235323435353038313536303036303030303935303030303031323033323331373535323634303031984B4A6F24000000000000012B303030303030303136353030303033393537303030E4355586";
        this.testProcessRequestData(message);
    }

    /**
     * 跨行轉帳交易確認
     *
     * @throws Exception
     */
    @Test
    public void testCommonConfirmI2524() throws Exception {
        String message =
                "000000303230323235323432383735383831383037303030303830363030303030373035313731353035333634303031AD58794D24000000000000012B3030303030303031303030303039313033324120207FC3FDEE";
        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)轉入交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2261And2561() throws Exception {
        String message =
                "000000303230303235363135353130313136303036303030303838393030303031323038313131373339343630303030984B4A6F244C00010101F0012B30303030303030313030303030303339353730303031383838383838383939393930303630303030383839303030303131303131343539393939393939393030363136303031323334353637383939393939313233312020202020202020202020203031353030303030303039353030333030303030303032353030383132303130303036343434343630303030373030343931313139343436A3FFB78D";
        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)轉出交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2262() throws Exception {
        String message =
                "000000303230303232363235353039373839303036303030303830363030303031323038303231303436303930303030984B4A6F200C00210141F0012B3030303030303330303030303038303630303030303036303030303130303031303338313130313134353030323531343135343233343837393130303130303036363035313233393231373736393939393132333120202020202020202020202030313030303032323030323430303232303030303030353430303938333134303030303030303031303038313230313030303634343434362DCBC6D6";
        this.testProcessRequestData(message);
        message =
                "000000303230323232363235353039373839303036303030303830363030303031323038303231303436303934303031984B4A6F20000000000000012B303030303030333030303030305DE31E09";
        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)轉出交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2262And2562() throws Exception {
        String message =
                "000000303230303235363235353130363231303036303030303436313030303031333032303130393439343130303030984B4A6F24EC20110101F3012B303030303030303035363030304C393939394B30313030303030303033313030303430323930303032323931333530303038303830303030303036303030303230323430323031303934393431313131313131303131343030313937303030323030303939313539373931353035333339393939313233312020202020202020202020203030303030303134303031353030313430303030303033343030303939393339363839383938393830303831323031303030363434343436383132303130303036343434343630313041313233343536373839313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);
//        message =
//                "000000303230323235363231303033313738303036303030303830383030303031323033323331303131313734303031984B4A6F24000000000000012B30303030303030313030303030303030303030303005DBFC4F";
//        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)自轉交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2263() throws Exception {
        String message =
                "000000303230303232363335353039383739303036303030303934383030303031323038303731343435333130303030984B4A6F200C00210141F0012B303030303030303031313130303030363033393130303630303030313030303334303031313031313430303132393134313533353533333735313030313030353938303531353335353333373532303130303631312020202020202020202020203030303030303138303030303030313830303030303036323030303636383239363235353530303930303131313030343030333731343435368C27CB";
        this.testProcessRequestData(message);
        message =
                "000000303230323232363335343331353535303036303030303031373030303031323033323431303131313734303031984B4A6F20000000000000012B303030303030303230303030303A8AD6BF";
        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)自轉交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2263And2563() throws Exception {
        String message =
                "000000303230303235363935353039333135303036303030303934383030303031323035323231353332353830303030984B4A6F240880110141F4012B303030303030303033343630303039303031303030303132303030303030363637303131303131343131333331313031353536373435373935303132363020202020202020202020202020202020202020202020202020202020202020202020303130303030333530303030303033353030303030303635303030303438303030303030303030393030383132303130303036343434343635363032393836393631363936333133AB97A19A";
        this.testProcessRequestData(message);
//        message =
//                "000000303230323235363332393437353536303036303030303838393030303031323033323431303131313734303031984B4A6F24000000000000012B30303030303030313030303030303339353730303004C81598";
//        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)跨轉交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2264() throws Exception {
        String message =
                "000000303230303232363435343331353639303036303030303031373030303031323033323431303131313730303030984B4A6F200C00210101F0012B30303030303030333030303030383037303339313436313030303031303030303036363039303532363030303031313031312020202020202020202020202020202020202020202020202020202020202020202020203030303030303030303032343030323230303030303035343030313231303138303030373237313630303030303030303132333435363730C881FB0D";
        this.testProcessRequestData(message);
        message =
                "000000303230323232363435343331353639303036303030303031373030303031323033323431303131313734303031984B4A6F20000000000000012B303030303030303330303030302D38AFD0";
        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)跨轉交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2264And2564() throws Exception {
        String message =
                "000000303230303235363441413132383037303036303030303934383030303031323033323431353338323030303030984B4A6F24EC20110101F3012B3030303030303030373530303030303030303030303030303030303032313030303030303239353032353531323330303038383830303030383037303030303230313830373132313034303536363531343037303731323430303035393530323834323130313731343730383839363432303138313233312020202020202020202020203030303030303232303030303030323230303030303037383030303032313039393930303030303030303132383031383030313636323636313238303138303031363632363630313030353038383037333936393230000A9E9E1E314B8C2D9F38D03D04";
        this.testProcessRequestData(message);
        message =
                "000000303230323235363441413132383037303036303030303934383030303031323033323431353338323034303031984B4A6F24000000000000012B3030303030303030373530303030303030303030302DBB84CB";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2561() throws Exception {
        // 來自candy的電文
        String message =
                "000000303230303235363135353039323430303036303030303838393030303031323035323231303236333030303030984B4A6F244C00010101F0012B30303030303030313030303030303339353730303031383838383838383939393930303630303030383839303030303131303131343539393939393939393030363136303031323334353637383939393939313233312020202020202020202020203031353030303030303039353030333030303030303032353030383132303130303036343434343630303030373030343931313139343436B8123C47";
        this.testProcessRequestData(message);
//        message =
//                "000000303230323235363135353038313331303036303030303830383030303031323033323331353435313134303031984B4A6F24000000000000012B303030303030303130303030303033393537303030A3FFB78D";
//        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳費(稅)跨轉交易 In
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2564TransIn() throws Exception {
        // 來自simon的電文， 押碼：
        //
        String message =
                "00000030323030323536343432303130363038303730303030383038303030303039313231373133323932333030303019D33BE0244C00010101F0012B303030303030303031303030304C393233324F333131383838383838383939393938303730303030373030303030303039313231373539393939393939393030393939383838313233343536373839393939313233310000000000000000000000003031353030303030303039353030333030303030303032353030393939383838313233343536373830303437303030303430363130373634326414F9";
        // 押碼改爲
        message = "";
        this.testProcessRequestData(message);
    }

    /**
     * 晶片卡全國性繳稅跨行交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2568() throws Exception {
        String message =
                "000000303230303235363837353231353132303036303030303934383030313531323032303731383232313630303030984B4A6F24A8A01101C0F3012B30303030303030303130303030383635313733383430303030303030343030303030303034303034303030303030323032303032323630393331333936353941303930323236313530303130303331303133313230333934333031303030303330303030303030333030303030303037303939393939393939393137323530303130303030313031383030303033363636303030303030303030303030303030303030313031383030303033363636000A3745364238373833806CB85D";
        this.testProcessRequestData(message);
        message =
                "000000303230323235363837353231353132303036303030303934383030313531323032303731383232313634303031984B4A6F24000000000000012B303030303030303031303030303836353137333834A3FFB78D";
        this.testProcessRequestData(message);
    }

    /**
     * 全國性繳稅ID+ACCOUNT繳款交易
     *
     * @throws Exception
     */
    @Test
    public void testCommonRequestI2569() throws Exception {
        String message =
                "000000303230303235363930373937313831383037303030303934383030303031303035323131363431353930303030E905813D240880110141F4012B3030303030303030333436303030393030313030303031323030303030303636373030373131313631313333313130313535363734353739353031323630202020202020202020202020202020202020202020202020202020202020202020203031303030303335303030303030333530303030303036353030303034383030303030303030303930303132383031383030313636323636353630323938363936313639363331333BE9D611";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2566() throws Exception {
        String message =
                "000000303230303235363635343934303835383236303030303434303030303031303036313731353231333530303030984B4A6F0420201100041001303930303130303039393939393939393230323130363137313532313335363631343039313131373130303131345331373336373639343130393838373737373737313936353034323830323839373730393039202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202030303831323031343434343430383533F941707C";

        message =
                "000000303230303235363635353130303436303036303030303934393030303031323038313031373234313230303030984B4A6F04202011000410013838383838383036393939393939393932303233303831303137323431323636313431313031313431313031303153433030363736353032382020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020303038313230313030303634343434365705C7F8";
        this.testProcessRequestData(message);
//        message =
//                "000000303230323235363635353038303838303036303030303934393030303031323033313731303139313534303031984B4A6F040000000000000138383838383830369979B13E";
//
//        this.testProcessRequestData(message);
    }

    @Test
    public void testVACommonI2566() throws Exception {
        String message = "";

        // 以下來自Candy測試的電文
        message =
                "000000303230303235363635343934333930383037303030303934393030303031303036323831373031333630303030984B4A6F042020110004100138383838383830363939393939393939323032313036323831373031333636363134303931313137313130313031534330303637363530323820202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020203030383132303130303036343434343642FD69D0";

        this.testProcessRequestData(message);
    }

    @Test
    public void testVAConfirmI2566() throws Exception {
        String message = "";

        // 以下來自Candy測試的電文
        message =
                "000000303230323235363635353038303739303036303030303934393030303031323031303331303139313534303031984B4A6F0400000000000001383838383838303655F3F2EF";

        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonConfirmI2261() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303230303232363135353039383737303036303030303935303030303031323038303731343434323130303030984B4A6F200C00210101F0012B30303030303030323030303030303036303030303031333030303031303030303439363131303131343030303031313032352020202020202020202020202020202039393939313233313030303030303030303030303030303030303030303032343030323230303030303035343030383132303130303036343434343630303030323134353031303330323130919C2296";
        this.testProcessRequestData(message);
        message =
                "000000303230323232363135333938393832303036303030303935303030303031323033323431303131313734303031984B4A6F20000000000000012B303030303030303230303030300AFF1CC5";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonConfirmI2563() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303230323235363335343934333031383037303030303935303030303031303036323431343430323830353031984B4A6F24000000000000012B303030303030303932313130303030303030303030D4532436";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonConfirmI2564() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303230323232363435343934333436383037303030303031373030303031303036323431363539323834303031984B4A6F20000000000000012B30303030303030333030303030D5B8FB29";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonConfirmI2568() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303230323235363835343934333536383037303030303934383030303031303036323431373134343534303031984B4A6F24000000000000012B303030303030303033343630303039303031303030BDD3CA97";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonConfirmI2264() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303230323232363435343934333531383037303030303934383030303031303036323431373036303130353031984B4A6F20000000000000012B303030303030303037353030309765FB08";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI24501() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "0000003032303032343530303032303832353830373030303039343630303030313030333135313334363133303030304BE781703C000000120000092B30303030303030303030303030363031353531313131313830303030333838333D39393132313031303133303731353430322A3030202020202020202020202020202020202020202020202030325520202020202020202020202020202020202020202020202020202020202020202020202020202D5C65BF74D4FC884D5446205445535430323030303030303038313130353131343034383030373238333834303038303930303030303032383330303030303030313030303030303030303030313030303038343038343036313030303030303030303030303030303030303030303030303131303531313430343831313035313130353030303030303031303030303631303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303038343030353130204D4F202020202077BCBE8043493130303030313330393120202020202020202020F09FE87F";
        message =
                "000000303230303234353035353039343137303036303030303934363030303031323035323331373533303630303030984B4A6F3C000000120000092D30303030303030303030303030363031353531303031303430303030313231343D39393132313031303333323630303030312A3030202020202020202020202020202020202020202020202030325520202020202020202020202020202020202020202020202020202020202020202020202020204C7A807E36ED54E74D5446205445535430323030303030303038313130353131343034383030373238333834303038303930303030303032383330303030303030313030303030303030303030313030303038343038343036313030303030303030303030303030303030303030303030303131303531313430343831313035313130353030303030303031303030303631303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303038343030353130204D4F2020202020F360050A434931303030303133303931202020202020202020205884AD38                4D5446205445535430323030303030303032303333313136333232303030373238333834303038303930303030303039393930303030303030323030303030303030303033363936373133393238343037303038353937303030303030303030303030303030303030303033333131363332323030333331303333313030303031323134333635363732383234313036303030303030303030303030303030303030303030303030303030303030303030303030303030303030303039303130353130204D4F202020202077BCBE804349313030303030383330322020202020202020202055FB6539";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI24502() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "0000003032303032343530333733383138393830373030303039343630303030313030323236313535383036303030304BE781703C000000120000092B30303030303030303030303030353234313936303030303035383530363D31383031323031313033373032323030303030302A3030202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020203A11A571E4C8438230303033343039313032303030333538373831313033303933373436303039313536353638313130393030303030303536383030303030303230303030303030303030303032393631303135363834303731343830353136303030303030303030303030303030303030313130333137333734363131303331313032303030303030393332333835363436363139323430303030303030303030303030303030303030303030303030303030303030303030303030303030303030303930313930313043484E202020202077BCBE804349313835323330353838302020202020202020202001AD884C";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2451() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "0000003032303032343531303031343236323830373030303039343630303030313030333038313131373430303030304BE781701C00000012000001363031353531303037303430303030313631343D39393132313233303433363630303030352A3130202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020209660E4C941375EB05465726D3030303430323030303030303531303130373134353035323030393837363530393939303930303030303232303230303030303030303030303030303030303030303030303038343038343036313030303030303030303030303030303030303030303030303031303731343530353230313037303130373030303030303030303030303631303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303038343020202020204D4F202020202077BCBE80C6B18070";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2470() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303234373035353039343538303036303030303935303030303031323035323431333531353430303030984B4A6F04000000100000094D544620544553543032303030303030323430363238313634323333303037323833383430303830393030303030303939393030303030303035303030303030303030303035303030303834303834303631303030303030443030303030303030443030303030303030303632383136343233333036323830363238303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030333338323337392020202020202020202020202020202020202020202020202020204D533130303030303931313520202020202020202020C9BE2765";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2410() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303234313035353039383937303036303030303934363030303031323038303731363234323030303030984B4A6F3C000000120000012D30303030303030303030303030363031353531313231303330303931313337323D39393132313233303238363230303030312A3030202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020203A11A571E4C84382313233343536373830323030303031333238313032383031333734333030303030343436363636303030303030303030303030303030303031313131303030303030303030313432393533343438343037303333333333333030303030303030303030303030303030303036323830393337343330363238303632383030303030303032393939393532363530343637303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030204D4F3030303030F360050ABBF266AF";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2525() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235323534303030353734303036303030303433313030303031323033323731303139313530303030984B4A6F24AC2011010033012B30303030303030313030303030313233343536373831363830303230373939393939393939343331303030303830373030303032303134303330363136353531353030303130333033303733303030313030313535303034303030303137363530303135353030343030303030313931313535303034303030303031393130343041313230363931353533393030000A627CADF8CC8AB76432AE08FF";
        this.testProcessRequestData(message);
        message =
                "000000303230323235323534303030353734303036303030303433313030303031323033323731303139313534303031984B4A6F24000000000000012B303030303030303130303030303132333435363738A3FFB78D";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2531() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235333135353130303539303036303030303031373030303031323038313131343132313230303030984B4A6F24A82015010037012B30303030303030303132343030303034373820202030303030303037313034383637343935303034303030303230323330383131313431323132363031313039313233313131303131343131303431393939393939393939313732353030313030303031303034303030383135343934323032393030303030303030313234303031303034303030383135343930333041313232393733373039393030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);

        message =
                "000000303230323235333132303032343037303036303030303830383030303031323033323731303139313534303031984B4A6F24000000000000012B303030303030303030313330304C393939394D3333A3FFB78D";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2541() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235343135353038383737303036303030303934333030363031323035313131313039333230303030984B4A6F24A02811001013012B303030303030303030323030303130303130303031303030303030323531303031303030313230323330353131313130393332313233343536373839303132333435363630313231313031313430303633333333333333333130303136303132303031392020202020202030303030313030343030303031323134303031303034303030303132313430313054323532393939343739313030000A261DC1BE31575A903FEB7E40";
        this.testProcessRequestData(message);

//        message =
//                "000000303230323235343134303030353734303036303030303830383030303031323033323731303139313534303031984B4A6F24000000000000012B303030303030303031303030304C39303039575432E6729B0A";
//        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2542() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "0000003032303032353432333030393338303830373030303039343338303730303131303034313534353339303030304BE7817024A0A81100101B012B303030303030303030323030303130303130303031303030303030303131303031303030313031323031323130303431353435313531323334353637383930313233343536363031323031313030353030303831323738303030303934373630313230303139202020202020203030303031303034303030303132313439343339323138343332383037313236303030303231393430303054323231363836333736393030000A8A7199D6BAF0A107A87D4950";
        // 以下來自Candy測試的電文
        message =
                "000000303230303235343235353130313837303036303030303934333030363031323038313431303330303030303030984B4A6F24A0A81100101B012B303030303030303030323030303130303130303031303030303030323531303031303030313031323032333038313431303238343031323334353637383930313233343536363031323131303131343030363333333333333333313030313630313230303139202020202020203030303031303034303030303132313439343335353130313835303036313236303030303231393430303054323231363836333736393030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);
        message =
                "000000303230323235343234303030353734303036303030303830383030303031323033323730393238333534303031984B4A6F24000000000000012B303030303030303030323030303130303130303031A3FFB78D";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2505() throws Exception {
        String message =
                "000000303230303235303530303032323132303036303030303432373530303431323032323431303131313730303030984B4A6F04A0001020001301393030313030303130303030303135383030303030313031323030393432313030303030333932333932343333393730303030303030303030303030303030302B303030303030303030303030302B3030303030303030303030303031323334353630303031383031383030303035303235383037303138303030373338393530303055323230393631313339393030000A3C9E1EDCF05F64332CA84D3F";
        this.testProcessRequestData(message);
    }

    @Test
    public void testICCommonI2571() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235373135353130303731303036303030303432373530303431323038313131343237343230303030984B4A6F24A02001200013012B3030303030303134383237303039303031303030313030303030303031303132333435363732303233303831313134323734323131303131343930313136303030333932333932373336383030303030303036363030303032393030302B303030303030313438323730302B3030303030303430303030303031323334353630303132383031383030313636323636313238303138303031363632363630313041313535363734353739313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);
        message =
                "000000303230323235373131353936333131303036303030303432373530303431323033303231303131313734303031984B4A6F24000000000000012B303030303030303034353630303838383838383838F2EDB96C";
        this.testProcessRequestData(message);
    }

    @Test
    public void testICConfirmI2571() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235373141413037303539303036303030303435373530303431323033303231303131313734303030984B4A6F24A02001200013012B303030303030303034353630303930303130303031303030313030363453656C665465737432303232313230393134323431343131313230393934333030303030333932333932373336383030303030303035363030303032343030302B303030303030303034353630302B3030303030303031303030303031323334353630303030393030343030313836363934383037313236303032333031323230303030373138333434393232393030000AEB14FD717C6ABCE4A018B012";
        this.testProcessRequestData(message);

        message =
                "000000303230323235373141413037303539303036303030303435373530303431323033303231303131313734303031984B4A6F24000000000000012B303030303030303034353630303930303130303031B15E7024";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestI2543() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235343334303030353734303036303030303830383030303031323033323730393238333530303030984B4A6F24002001001010012B303030303030323030303030303132333435363738323031343033303631363533353831313031313430303530303238373930353130303131323334373031313536373839303030303030373030343030303137333132A3FFB78D";
        this.testProcessRequestData(message);
        message =
                "000000303230323235343334303030353734303036303030303830383030303031323033323730393238333534303031984B4A6F24000000000000012B303030303030323030303030303132333435363738A3FFB78D";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCLRequestFromFISC5102() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303530303531303236373635353739303036303030303935303030303030383132313631373231323830303030DDAD056302FFAC3FDF83C0012B30303030303339333538383730302B303030303030303439303336303030303031392B303030303033393332333632303030303033372B303030303030303337303336303030303030342B303030303030303033353235303030303031332B30303030303030313230303030302B30303030303030303030303030302B30303030303030303030303030302B303030303033383836383531303030303030302B303030303030303030303030303030303030302B303030303030303030303030303030303030302B30303030303030303030303030302B30303030303030303030303030302B303030303030303030303030303030303030302B303030303030303030303030303030303030302B30303030303030303030303030302B30303030303030303030303030302B303031383032373539343434303030303030302B303030303030303030303030303030303030302B3030303030303030303030303030EB5A19DE";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCLRequestFromFISC5312() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303530303533313235353035373432383037303030303935303030303031303039323231343231343030303030DDAD056300000080000000012B3030303033393138343831353030FEDB8E9B";
        message =
                "000000303530303533313235353035373536393530303030303830373030303031303039323331303530313330303030DDAD05630000000000000000";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCLRequestFromFISC5314() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303530303533313435353035373534383037303030303935303030303031303039323231343431333130303030DDAD0563000000C0000000012B30303030333931383438313530303132333435363707C54ED3";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCLRequestFromFISC5001() throws Exception {
        // 以下來自Candy測試的電文
        String message =
                "000000303538313530303135353035373634383037303030303935303030303031303039323331313231323930303030DDAD056370000000000000002B30333030303030303030302B30303135303030303030302B30303031383830353639383030";
        this.testProcessRequestData(message);
    }

    @Test
    public void testICCommonI2572() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235373235353130333234303036303030303432373530303431323038313631313137353230303030984B4A6F24A0A00120001B012B30303030303031343832373030393030313030303130303030303030313939393939393939303332303233303831363131313635393131303131343432313030303030333932333932373336383030303030303035363030303032343030302B303030303030303337363830302B303030303030313030303030303132333435363030313238303138303031363632363634323735353130333232313238303138303031363632363630313041313535363734353739313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);

//        message =
//                "000000303230323235373241413037303630303036303030303435373530303431323033303231303131313734303031984B4A6F24000000000000012B3030303030303030343536303039303031303030312ED12E5A";
//        this.testProcessRequestData(message);
    }

    @Test
    public void testICConfirmI2572() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "0000003032303232353732303133323430363830373030303034323735303034303830343234313431333435343030314BE7817024000000000000012B30303030303030333736383030303930303130303025A88C31";
        this.testProcessRequestData(message);
    }

    @Test
    public void testICCommonI2545() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235343535353130333835303036303030303432373530303431323039323731363437303230303030984B4A6F24A02811201013012B30303030303033323932313030393030313030303130303030303030313031323334353637323032333039323731363437303230313233343536373839303132333435303030313131303131343930313136303030333932333932373336383030303030303139393030303035343030302B303030303030333239323130302B3030303030303930303030303031323334353630303632343739373137353930303136373839303132333435363738392030303031383031383030303035303533303138303138303030303530353330313044313531363231363535313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);

//        message =
//                "000000303230323235343530303032333537303036303030303432373530303431323033303331303131313734303031984B4A6F24000000000000012B303030303030333239323130303930303130303031B6BFAE56";
//        this.testProcessRequestData(message);
    }

    @Test
    public void testICCommonI2546() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235343635353130333031303036303030303432373530303431323038313631303336313630303030984B4A6F24A0A81120101B012B303030303030333239323130303930303130303031303030303030303430313233343536373031323032333038313631343433323630313233343536373839303132333435303030313131303131343930313136303030333932333932373336383030303030303133323030303033363030302B303030303030323139343830302B303030303030363030303030303132333435363030363234373937313735393030313637383930313233343536373839203030303138303138303030303530353334323735353130323939303138303138303030303530353330313044313531363231363535313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);

        message =
                "000000303230323235343630303032333539303036303030303432373530303431323033303331303131313734303031984B4A6F24000000000000012B303030303030323139343830303930303130303031D85431BF";
        this.testProcessRequestData(message);
    }

    @Test
    public void testICConfirmI2545() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235343535353130333833303036303030303432373530303431323039323731363434333530303030984B4A6F24A02811201013012B30303030303033323932313030393030313030303130303030303030313031323334353637323032333039323731363434333530313233343536373839303132333435303030313131303131343930313136303030333932333932373336383030303030303139393030303035343030302B303030303030333239323130302B3030303030303930303030303031323334353630303632343739373137353930303136373839303132333435363738392030303031383031383030303035303533303138303138303030303530353330313044313531363231363535313030000A5927F173D074AFAEA3FFB78D";
        this.testProcessRequestData(message);
    }

    @Test
    public void testOBCommonI2555() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230323235353535353038373431303036303030303934333030343031323035303931363433353934303031984B4A6F24000000000000012B303030303030303039343530303935303030303031C3115565";
        this.testProcessRequestData(message);

//        message =
//                "000000303230323235353534303134323136303036303030303934333030343031323033303831303131313734303031984B4A6F24000000000000012B303030303030303039343530303935303030303031BD267E0D";
//        this.testProcessRequestData(message);
    }

    @Test
    public void testOBCommonI2556() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303235353635353130313436303036303030303934333030303031323038313430393035343330303030984B4A6F24002011100010012B30303030303030303934353030393530303030303132303231313030343134353734393635394131313031313439353031303030303330303431353632303138303832392B303030303030303039353130303030303036303030303030303030303239383238343035333137303030304F757453616C6531383038323931363431303920323431313634313039303031393433353530363530323130313030344F757453616C653138303832393136343130392030303031303030303030303030303033303030303030303033305920202020202020202020202020202020202020202020202020202020202020202030303132363031383030303337303137A3FFB78D";
        this.testProcessRequestData(message);

//        message =
//                "000000303230323235353634303135303234303036303030303934333030343031323033303831303131313734303031984B4A6F24000000000000012B3030303030303030373838303039353030303030319AF634DE";
//        this.testProcessRequestData(message);
    }

    @Test
    public void testPendingAskFromFISC2270() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "0000003032303032323730343132373537363830373030303039353030303030313030343036313632383234303030304BE781700000000000000801383037323634313635352F4B03B5";
        this.testProcessRequestData(message);
    }

    @Test
    public void testPendingAskFromFISC2120() throws Exception {
        // 以下來自kai測試的電文
        String message =
                "000000303230303231323035353038313339303036303030303935303030303031323032303730393339343130303030984B4A6F24050004000028012B30303030303031303030303030363033323230303030303730303030303030303030323130303430363030303030333032363831303534303738303738353838393738A3FFB78D";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCommonRequestIAndConfirmI2523() throws Exception {
        // 以下來自Candy測試的電文
        String messageRequestI = "<request>" +
                "<stan>5494259</stan>" +
                "<step>0</step>" +
                "<ej>144</ej>" +
                "<message>000000303230303235323335343934323539383037303030303830363030303031303036323430393431303330303030984B4A6F24AC2011000033012B3030303030303031303030303039313033324120203030303030303033303433323231414338303730303030383037303030303230323130363234303934313033363031313039313131373030383132303130303036343434343630303831323031303030323434343438383132303130303032343434343830313041313233343536373839313030000A5927F173D074AFAE87FCBBB1</message>"
                + "<sync>true</sync>" +
                "</request>";
        String messageConfirmI = "<request>" +
                "<stan>5494259</stan>" +
                "<step>0</step>" +
                "<ej>145</ej>" +
                "<message>000000303230323235323335343934323539383037303030303830363030303031303036323430393431303330353031984B4A6F24000000000000012B303030303030303130303030303931303332412020DF35A082</message>"
                + "<sync>true</sync>" +
                "</request>";
        this.testProcessRequestDataWithXML(messageRequestI);
        this.testProcessRequestDataWithXML(messageConfirmI);
    }

    /**
     * OPC電文測試
     *
     * @throws Exception
     */
    @Test
    public void testFISCRequest0101() throws Exception {
        // 以下來自Candy測試的電文
        String messageIn = "<request>" +
                "<stan>5494456</stan>" +
                "<step>0</step>" +
                "<ej>294</ej>" +
                "<message>0000003038303030313031353439343435363830373030303039353030303030313030373037313333343239303030300AE8D1100001C0000000000157C3D12D984B4A6FDDAD05634C60ADA6</message>" +
                "<sync>true</sync>" +
                "</request>";
        this.testProcessRequestDataWithXML(messageIn);
    }

    /**
     * OPC電文測試
     *
     * @throws Exception
     */
    @Test
    public void testFISCChangeKey010500() throws Exception {
        // 以下來自Candy測試的電文
        String messageIn = "<request>" +
                "<stan>5494495</stan>" +
                "<step>0</step>" +
                "<ej>353</ej>" +
                "<message>0000003038303030313035353439343439353830373030303039353030303030313030373134313432343331303030300000000000000012200000003034C794209B3214A7C724C88C60F00D9D1FFA82B44B8800BFD5</message>"
                + "<sync>true</sync>" +
                "</request>";
        this.testProcessRequestDataWithXML(messageIn);
    }

    /**
     * OPC電文測試
     *
     * @throws Exception
     */
    @Test
    public void testAA3201() throws Exception {
        // 以下來自Candy測試的電文
        String messageIn = "<request>" +
                "<stan>5494416</stan>" +
                "<step>0</step>" +
                "<ej>253</ej>" +
                "<message>0000003036303033323031353439343431363832363030303039353030303030313030373031313434373338303030300AE8D1106000000000000001343130313130393030303030303036334231333533333333353430303030303030393031323220202020202003E8A33D</message>"
                + "<sync>true</sync>" +
                "</request>";
        this.testProcessRequestDataWithXML(messageIn);
    }

    @Test
    public void testEMVCommonI2630() throws Exception {
        String message =
                "0000003032303032363330383439353834363830373030303039343630303030313030383039313130333539303030304BE781701D8201001200008B353336323638303130303030303630393D3138303132303131303337303232302020202020F9EDBC70C8EC15BF4D5446205445535430303030303030313030303038343030304349333435303330313037303136303030303333303037323833383430303835303531204D4F3630313130323232303833343539303232323038333435393032323230303030303030313030303036313030303030303834303030303030303031303030303631303030303030383430202053742E204C6F7569732020202020202020202020202020202020202077BCBE80303255202020202030303000625F2A02084082023900950500000080009A031605189C01009F02060000000222009F10120110250000044000DAC100000000000000009F1A0208409F2608147794472D10D5629F2701809F34034103029F3602001E9F370409D06AA39F530152B8DEE31B";

        // message =
        // "0000003032303032363330383439353834363830373030303039343630303030313030383039313130333539303030304BE781701D8201001200008B353336323638303130303030303630393D3138303132303131303337303232303030303030F9EDBC70C8EC15BF4D5446205445535430303030303030313030303038343030304349333435303330313037303136303030303333303037323833383430303835303531204D4F3630313130323232303833343539303232323038333435393032323230303030303030313030303036313030303030303834303030303030303031303030303631303030303030383430202053742E204C6F7569732020202020202020202020202020202020202077BCBE80303255202020202030303000625F2A02084082023900950500000080009A031605189C01009F02060000000222009F10120110250000044000DAC100000000000000009F1A0208409F2608147794472D10D5629F2701809F34034103029F3602001E9F370409D06AA39F530152B8DEE31B";
        this.testProcessRequestData(message);
    }

    @Test
    public void testAA2574() throws Exception {
        // String message =
        // "00000030323030323537343533363238393734323730303030383037303030303038303430393133353635303030303099ED22EF2400A000000018012B3030303030303030343536303038383838383838383030323031393034303931323135313030303032373030343931303035383931343237313539313131312B0C8332";

        // this.testProcessRequestData(message);

        String messageXml =
                "<request>"
                        + "<stan>5506381</stan>"
                        + "<step>0</step>"
                        + "<ej>5041</ej>"
                        + "<message>000000303230303235373435353036333831343237303030303830373030303031303130303130393036353430303030984B4A6F2400A000000018012B3030303030303134383237303039303031303030313030323032313039333031303332343430303132383031383030313636323636343237353530363330349D88189B</message>"
                        + "<sync>true</sync>"
                        + "</request>";
        this.testProcessRequestDataWithXML(messageXml);
    }

    @Test
    public void testEMVConfirmI2630() throws Exception {
        String message =
                "0000003032303232363330383439353834363830373030303039343630303030313030383039313130333539343030314BE7817004000000000000014D54462054455354CBFBE0EE";
        this.testProcessRequestData(message);
    }

    @Test
    // CIRRUS國際卡提款確認
    public void testCommonConfirmI2450() throws Exception {
        String message =
                "000000303230303234353035353039343638303036303030303934363030303031323035323431343233313530303030984B4A6F3C000000120000092D30303030303030303030303030363031353531303031303430303030313231343D39393132313031303333323630303030312A3030202020202020202020202020202020202020202020202030325520202020202020202020202020202020202020202020202020202020202020202020202020204C7A807E36ED54E74D5446205445535430323030303030303038313130353131343034383030373238333834303038303930303030303032383330303030303030313030303030303030303030313030303038343038343036313030303030303030303030303030303030303030303030303131303531313430343831313035313130353030303030303031303030303631303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303038343030353130204D4F2020202020F360050A434931303030303133303931202020202020202020209BD15E6B";
        this.testProcessRequestData(message);
//        message =
//                "000000303230323234353033323636313035303036303030303934363030303031323033323830393436323434303031984B4A6F0400000000000001433030303135202035B49812";
//        this.testProcessRequestData(message);
    }

    @Test
    // CIRRUS國際卡提款 (Combo卡)確認
    public void testCommonConfirmI2450II() throws Exception {
        String message =
                "000000303230323234353033373338313839383037303030303934363030303031303032323631353538303634303031984B4A6F040000000000000130303033343039315C3E4D2F";
        this.testProcessRequestData(message);
    }

    @Test
    // 消費扣款(固定費率)交易確認
    public void testCommonConfirmI2525() throws Exception {
        String message =
                "000000303230323235323531323734313430383037303030303433313030303031303038333131313238323334303031984B4A6F24000000000000012B3030303030303031303030303031323334353637380C66840C";
        this.testProcessRequestData(message);
    }

    @Test
    // 跨行繳款交易確認
    public void testCommonConfirmI2531() throws Exception {
        String message =
                "000000303230303235333132303032343037303036303030303830383030303031323031303731303139313530303030984B4A6F24A8201101C033012B303030303030303030313330304C393939394D33333030303030383632333734353935363830303430303030323031383131303631383538353336303131303731313037313530303130303138343737313534332020203939393939393939393137323530303130303132313030333030393131333732383037303039303030303031313530303041313233393935323633393030000ADC1B92E653621A4F75EA5B92";
        this.testProcessRequestData(message);
    }

    @Test
    // 消費扣款(變動費率)交易確認
    public void testCommonConfirmI2541() throws Exception {
        String message =
                "000000303230323235343139363636383531383037303030303934333830373031303036313831353235303634303031984B4A6F24000000000000012B303030303030303030323030303130303130303031AC67F310";
        this.testProcessRequestData(message);
    }

    @Test
    // 消費扣款沖正交易確認
    public void testCommonConfirmI2542() throws Exception {
        String message =
                "000000303230323235343235353038393235303036303030303934333030363031323035313131353431303334303031984B4A6F24000000000000012B303030303030303030323030303130303130303031E028B902";
        this.testProcessRequestData(message);
    }

    @Test
    // 消費扣款退貨交易確認
    public void testCommonConfirmI2543() throws Exception {
        String message =
                "000000303230323235343335353338343733383037303030303433313030303031303038333131313238323934303031984B4A6F24000000000000012B30303030303032303030303030313233343536373829C264C2";
        this.testProcessRequestData(message);
    }

    @Test
    public void testEMVCommonI2631() throws Exception {
        String message =
                "0000003032303032363331323530393331303830373030303039343630303030313030383039313035353035303030304BE781701C0201001200000B353336323638303130303030303630393D32343132323031313439383934343230303030306DEFC89B4BFD26664557455331373537303043493030393734353430353532323430353532323030303030303133353531353035314553503030303030323039313734353134303230393138343531313032303930303030303030303030303036313039303832343834303030303030303030303030303533323732303337393031202042617263656C6F6E612020202020202020202020202020202020202077BCBE80303030008b5F2A020978820218008407A0000000041010950580000480009A032002149C01019F02060000000000009F090200029F10120110A00003240000000000000000000000FF9F1A0207249F1E0833533459524D56469F260877D57B7B966CF61D9F2701809F33036040209F34034201009F3501149F360200209F3704BC116C029F41030064629F53015AD303EACB";
        this.testProcessRequestData(message);
    }

    @Test
    public void testEMVCommonI2633() throws Exception {
        String message =
                "0000003032303032363333363430343839343830373030303039343630303030313030383039313131333131303030304BE7817005800180100808014D54462054455354303030303030303130303030383430434935333632363830313030303030363039202020333435303330313037303136303030303333303037323833383430303830303030202020363031313032323231373037343330323232313635303138303232323030303030303031303030303631303030303030383430303030303030303130303030363130303030303038343030302020202020202020202020202020202020202020202020202020202030303030303030303030303030303030303030303030303030303030303030303030303039343638343935383436C950339A";
        this.testProcessRequestData(message);
    }

    @Test
    public void testAA11X21112() throws Exception {
        String message2 =
                "000000303230303131313235353037323138383037303030303935303030303031303130323231353532343430303030DDAD0563618C3F008000000130303130303030303030303038392B3030303030303031303030303030303030303031303030303030313030363530313238303730303134735B953BC7A28DF3000d0E4759553B6041665C486700070E5A2F443500090E5A2F44357742000b0E442B45426759633E3130313032322EF198C8";
        this.testProcessRequestData(message2);
    }

    @Test
    public void testAA1412() throws Exception {
        String message =
                "000000303230303134313235353037313837383236303030303935303030303031303130323231313034343230303030DDAD0563010C08800000000130303030303031313138303030303830373030303000070E4E324F6A0035434847334145384330304546463235433839344231463941393141414241364539453843344243344645314330303030303031B5EF74F6";
        this.testProcessRequestData(message);

    }

    @Test
    public void testAA1512() throws Exception {
        String message =
                "000000303230303135313235353037323437383037303030303935303030303031303130323630383338353130303030DDAD0563010000002000000130303030303031313131326D65BB82";
        this.testProcessRequestData(message);
    }

    @Test
    public void testCLRequestFromFISC510200() throws Exception {
        // 以下來自Candy測試的電文
        String messageIn = "<request>" +
                "<stan>5505740</stan>" +
                "<step>0</step>" +
                "<ej>4853</ej>" +
                "<message>000000303530303531303235353035373430383037303030303935303030303031303039323231313232313130303030DDAD056302FFAA3FDFFBC0012B30303030303030323033373230302B303030303031353830303332303030303030312B303030303030303031373030303030303030332B303030303030303032303030303030303030362B303030303030303138303430303030303030362B30303030303030303436313130302D30303030303030303030303030302D30303030303030303030303030302B303030303031353539363630303030303030302D303030303030303030303030303030303030302D303030303030303030303030303030303030302D30303030303030303030303030302D30303030303030303030303030302D303030303030303030303030303030303030302D303030303030303030303030303030303030302D30303030303030303030303030302D30303030303030303030303030302B303030363730343634363338313030303030302D303030303030303030303030303030303035312B303030303031353730393836303030303030322B303030303030303030363332303030303030352B30303030303030303234333530304F13BFDC</message>"
                + "<sync>true</sync>" +
                "</request>";
        this.testProcessRequestDataWithXML(messageIn);
    }

    @Test
    public void testToFEPCommuSysconf() throws Exception {
        ToFEPCommuSysconf request = new ToFEPCommuSysconf();
        request.setSysconfSubsysno((short) 1);
        request.setSysconfName("SenderIP");
        String messageIn = request.toString();
        String response = this.testProcessRequestDataWithXML(messageIn);
        ToGWCommuSysconf toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");

        request.setSysconfName("SenderPort");
        messageIn = request.toString();
        response = this.testProcessRequestDataWithXML(messageIn);
        toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");

        request.setSysconfName("ReceiveIP");
        messageIn = request.toString();
        response = this.testProcessRequestDataWithXML(messageIn);
        toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");

        request.setSysconfName("ReceivePort");
        messageIn = request.toString();
        response = this.testProcessRequestDataWithXML(messageIn);
        toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");

        request.setSysconfName("DEAD_FISC");
        messageIn = request.toString();
        response = this.testProcessRequestDataWithXML(messageIn);
        toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");

        request.setSysconfSubsysno((short) 9);
        request.setSysconfName("KeepAliveTime");
        messageIn = request.toString();
        response = this.testProcessRequestDataWithXML(messageIn);
        toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");

        request.setSysconfName("KeepAliveInterval");
        messageIn = request.toString();
        response = this.testProcessRequestDataWithXML(messageIn);
        toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuSysconf = [", toGWCommuSysconf.toString(), "]");
    }

    @Test
    public void testToFEPCommuAction() throws Exception {
        ToFEPCommuAction request = new ToFEPCommuAction();
        request.setAction(ToFEPCommuAction.ActionType.GENERATE_EJFNO);
        String messageIn = request.toString();
        String response = this.testProcessRequestDataWithXML(messageIn);
        ToGWCommuAction toGWCommuAction = ToGWCommuSysconf.fromXML(response);
        UnitTestLogger.info("toGWCommuAction = [", toGWCommuAction.toString(), "]");
    }

    @Test
    public void testToFEPCommuZone() throws Exception {
        ToFEPCommuZone request = new ToFEPCommuZone();
        request.setZoneCode("TWN");
        String messageIn = request.toString();
        String response = this.testProcessRequestDataWithXML(messageIn);
        ToGWCommuZone toGWCommuZone = ToGWCommuZone.fromXML(response);
        UnitTestLogger.info("toGWCommuZone = [", toGWCommuZone.toString(), "]");
    }

    @Test
    public void testToFEPCommuConfig() throws Exception {
        ToFEPCommuConfig request = new ToFEPCommuConfig();
        request.setConfigType(ToFEPCommuConfig.ConfigType.CMN.getValue() + ToFEPCommuConfig.ConfigType.GW.getValue());
        String messageIn = request.toString();
        String response = this.testProcessRequestDataWithXML(messageIn);
        ToGWCommuConfig toGWCommuConfig = ToGWCommuConfig.fromXML(response);
        UnitTestLogger.info("toGWCommuConfig = [", toGWCommuConfig.toString(), "]");
    }

    private String testProcessRequestData(String message) throws Exception {
        ToFEPFISCCommu request = new ToFEPFISCCommu();
        request.setEj(TxHelper.generateEj());
        request.setMessage(message);
        request.setStan(this.generateStan());
        request.setStep(0);
        request.setSync(true); // 2021-06-09 Richard add for UT only
        String messageIn = XmlUtil.toXML(request);
        return this.testProcessRequestDataWithXML(messageIn);
    }

    private String testProcessRequestDataWithXML(String messageIn) throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders
                .post("/recv/fisc")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .content(messageIn);

        // 執行請求
        ResultActions action = mockMvc.perform(builder);
        // 分析結果
        MvcResult result = action.andExpect(MockMvcResultMatchers.status().isOk()) // 執行狀態
                // .andExpect(MockMvcResultMatchers.jsonPath("message").value(logData.getMessage())) // 期望值
                // .andExpect(MockMvcResultMatchers.jsonPath("messageId").value(logData.getMessageId())) // 期望值
                // .andDo(MockMvcResultHandlers.print()) // 打印
                .andReturn();
        String resultStr = result.getResponse().getContentAsString();
        UnitTestLogger.info("resultStr = [", resultStr, "]");
        return resultStr;
    }

    private final String generateStan() {
        return generator.generate();
    }
}
