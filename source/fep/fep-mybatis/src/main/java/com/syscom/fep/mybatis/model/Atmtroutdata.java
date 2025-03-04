package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

public class Atmtroutdata extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.TXDATE
     *
     * @mbg.generated
     */
    private String txdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.HOUR
     *
     * @mbg.generated
     */
    private String hour = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.NUM
     *
     * @mbg.generated
     */
    private Integer num;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.FLOW
     *
     * @mbg.generated
     */
    private String flow;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.CHANNEL_TYPE
     *
     * @mbg.generated
     */
    private String channelType;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.CHANNEL
     *
     * @mbg.generated
     */
    private String channel;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.KIND
     *
     * @mbg.generated
     */
    private String kind;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.CNT
     *
     * @mbg.generated
     */
    private Integer cnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMTROUTDATA.AMT
     *
     * @mbg.generated
     */
    private BigDecimal amt;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table ATMTROUTDATA
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMTROUTDATA
     *
     * @mbg.generated
     */
    public Atmtroutdata(String txdate, String hour, Integer num, String flow, String channelType, String channel, String kind, Integer cnt, BigDecimal amt) {
        this.txdate = txdate;
        this.hour = hour;
        this.num = num;
        this.flow = flow;
        this.channelType = channelType;
        this.channel = channel;
        this.kind = kind;
        this.cnt = cnt;
        this.amt = amt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMTROUTDATA
     *
     * @mbg.generated
     */
    public Atmtroutdata() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.TXDATE
     *
     * @return the value of ATMTROUTDATA.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.TXDATE
     *
     * @param txdate the value for ATMTROUTDATA.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.HOUR
     *
     * @return the value of ATMTROUTDATA.HOUR
     *
     * @mbg.generated
     */
    public String getHour() {
        return hour;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.HOUR
     *
     * @param hour the value for ATMTROUTDATA.HOUR
     *
     * @mbg.generated
     */
    public void setHour(String hour) {
        this.hour = hour;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.NUM
     *
     * @return the value of ATMTROUTDATA.NUM
     *
     * @mbg.generated
     */
    public Integer getNum() {
        return num;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.NUM
     *
     * @param num the value for ATMTROUTDATA.NUM
     *
     * @mbg.generated
     */
    public void setNum(Integer num) {
        this.num = num;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.FLOW
     *
     * @return the value of ATMTROUTDATA.FLOW
     *
     * @mbg.generated
     */
    public String getFlow() {
        return flow;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.FLOW
     *
     * @param flow the value for ATMTROUTDATA.FLOW
     *
     * @mbg.generated
     */
    public void setFlow(String flow) {
        this.flow = flow;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.CHANNEL_TYPE
     *
     * @return the value of ATMTROUTDATA.CHANNEL_TYPE
     *
     * @mbg.generated
     */
    public String getChannelType() {
        return channelType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.CHANNEL_TYPE
     *
     * @param channelType the value for ATMTROUTDATA.CHANNEL_TYPE
     *
     * @mbg.generated
     */
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.CHANNEL
     *
     * @return the value of ATMTROUTDATA.CHANNEL
     *
     * @mbg.generated
     */
    public String getChannel() {
        return channel;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.CHANNEL
     *
     * @param channel the value for ATMTROUTDATA.CHANNEL
     *
     * @mbg.generated
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.KIND
     *
     * @return the value of ATMTROUTDATA.KIND
     *
     * @mbg.generated
     */
    public String getKind() {
        return kind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.KIND
     *
     * @param kind the value for ATMTROUTDATA.KIND
     *
     * @mbg.generated
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.CNT
     *
     * @return the value of ATMTROUTDATA.CNT
     *
     * @mbg.generated
     */
    public Integer getCnt() {
        return cnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.CNT
     *
     * @param cnt the value for ATMTROUTDATA.CNT
     *
     * @mbg.generated
     */
    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMTROUTDATA.AMT
     *
     * @return the value of ATMTROUTDATA.AMT
     *
     * @mbg.generated
     */
    public BigDecimal getAmt() {
        return amt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMTROUTDATA.AMT
     *
     * @param amt the value for ATMTROUTDATA.AMT
     *
     * @mbg.generated
     */
    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMTROUTDATA
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", txdate=").append(txdate);
        sb.append(", hour=").append(hour);
        sb.append(", num=").append(num);
        sb.append(", flow=").append(flow);
        sb.append(", channelType=").append(channelType);
        sb.append(", channel=").append(channel);
        sb.append(", kind=").append(kind);
        sb.append(", cnt=").append(cnt);
        sb.append(", amt=").append(amt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMTROUTDATA
     *
     * @mbg.generated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Atmtroutdata other = (Atmtroutdata) that;
        return (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getHour() == null ? other.getHour() == null : this.getHour().equals(other.getHour()))
            && (this.getNum() == null ? other.getNum() == null : this.getNum().equals(other.getNum()))
            && (this.getFlow() == null ? other.getFlow() == null : this.getFlow().equals(other.getFlow()))
            && (this.getChannelType() == null ? other.getChannelType() == null : this.getChannelType().equals(other.getChannelType()))
            && (this.getChannel() == null ? other.getChannel() == null : this.getChannel().equals(other.getChannel()))
            && (this.getKind() == null ? other.getKind() == null : this.getKind().equals(other.getKind()))
            && (this.getCnt() == null ? other.getCnt() == null : this.getCnt().equals(other.getCnt()))
            && (this.getAmt() == null ? other.getAmt() == null : this.getAmt().equals(other.getAmt()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMTROUTDATA
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getHour() == null) ? 0 : getHour().hashCode());
        result = 31 * result + ((getNum() == null) ? 0 : getNum().hashCode());
        result = 31 * result + ((getFlow() == null) ? 0 : getFlow().hashCode());
        result = 31 * result + ((getChannelType() == null) ? 0 : getChannelType().hashCode());
        result = 31 * result + ((getChannel() == null) ? 0 : getChannel().hashCode());
        result = 31 * result + ((getKind() == null) ? 0 : getKind().hashCode());
        result = 31 * result + ((getCnt() == null) ? 0 : getCnt().hashCode());
        result = 31 * result + ((getAmt() == null) ? 0 : getAmt().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMTROUTDATA
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<HOUR>").append(this.hour).append("</HOUR>");
        sb.append("<NUM>").append(this.num).append("</NUM>");
        sb.append("<FLOW>").append(this.flow).append("</FLOW>");
        sb.append("<CHANNEL_TYPE>").append(this.channelType).append("</CHANNEL_TYPE>");
        sb.append("<CHANNEL>").append(this.channel).append("</CHANNEL>");
        sb.append("<KIND>").append(this.kind).append("</KIND>");
        sb.append("<CNT>").append(this.cnt).append("</CNT>");
        sb.append("<AMT>").append(this.amt).append("</AMT>");
        return sb.toString();
    }
}