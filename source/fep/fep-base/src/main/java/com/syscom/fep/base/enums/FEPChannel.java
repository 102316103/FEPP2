package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 表示交易發動的來源
 *
 * @author Richard
 */
public enum FEPChannel {
    Unknown(0, ""),
    ATM(1, "ATM"),
    FISC(2, "FISC"),
    WEBATM(3, "EATM"),
    CREDIT(4, "CRS"), //信用卡系統
    BRANCH(5, "BRH"),
    T24(6, "T24"),
    FEP(7, "FEP"),
    FCS(8, "FCS"),
    RM(9, "RM"),
    CBS(10, "CBS"),
    FEDI(11, "FEDI"),
    ETS(12, "ETS"),
    NETBANK(13, "NB"),
    IVR(14, "IVR"), //語音
    EAI(15, "EAI"), //Portal
    DB(16, "DB"),
    BATCH(17, "BATCH"),
    CARD(18, "CARD"),
    CGL(19, "CGL"),
    MOBILBANK(23, "MB"),
    HSM(25, "HSM"),
    SVCS(26, "SVCS"),
    HCA(39, "HCA"), //HCA
    HCE(40, "HCE"),
    EAT(41, "EAT"), //WEBATM
    ICP(43, "ICP"), //FEDI/一通路轉通匯
    VO(45, "VO"), //語音
    NAM(46, "NAM"), //全繳整批轉即時
    POS(48, "POS"), //
    NBQ(51, "NBQ"), //新一代企銀
    NBP(52, "NBP"), //新一代個銀
    NBB(53, "NBB"), //無障礙個銀
    MBQ(54, "MBQ"), //行動銀行
    MQQ(55, "MQQ"), //行網QRCODE P2P
    MSQ(56, "MSQ"), //行網QRCODE 購貨
    EOI(57, "EOI"),
    EIP(58, "EIP"),
    MFT(5, "MFT");

    private final int code;
    private final String nameS;

    private FEPChannel(int code, String nameS) {
        this.code = code;
        this.nameS = nameS;
    }

    public int getCode() {
        return code;
    }

    public String getNameS() {
        return nameS;
    }

    public static FEPChannel fromCode(int code) {
        for (FEPChannel e : values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
    }

    public static FEPChannel parse(Object nameOrCode) {
        if (nameOrCode instanceof Number) {
            return fromCode(((Number) nameOrCode).intValue());
        } else if (nameOrCode instanceof String) {
            String nameOrCodeStr = (String) nameOrCode;
            if (StringUtils.isNumeric(nameOrCodeStr)) {
                return fromCode(Integer.parseInt(nameOrCodeStr));
            }
            for (FEPChannel e : values()) {
                if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
    }

    public String toDescription() {
        return StringUtils.join(name(), "(", getCode(), ")");
    }
}