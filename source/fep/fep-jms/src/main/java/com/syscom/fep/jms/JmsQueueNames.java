package com.syscom.fep.jms;

import com.syscom.fep.frmcommon.jms.JmsDefinition;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * 所有的Queue名稱定義在這個類中
 *
 * @author Richard
 */
public class JmsQueueNames {
    /**
     * TestQueue
     */
    private JmsDefinition test;
    /**
     * DeadQueue
     */
    private JmsDefinition dead;
    /**
     * dead-fisc
     */
    private JmsDefinition deadFisc;
    /**
     * 用於Batch
     */
    private JmsDefinition batch;
    /**
     * 用於ATMGateway
     */
    private JmsDefinition atmMon;
    /**
     * 用於EMS Service接收EMS log
     */
    private JmsDefinition ems;
    /**
     * PYBATCH
     */
    private JmsDefinition pyBatch;
    /**
     * PYBATCHAck
     */
    private JmsDefinition pyBatchAck;
    /**
     * Nb
     */
    private JmsDefinition nb;
    /**
     * NbQ
     */
    private JmsDefinition nbQAck;

    /**
     * NbP
     */
    private JmsDefinition nbPAck;
    /**
     * mb
     */
    private JmsDefinition mb;
    /**
     * mb
     */
    private JmsDefinition mbAck;
    /**
     * ivr
     */
    private JmsDefinition ivr;
    /**
     * ivr
     */
    private JmsDefinition ivrAck;
    /**
     * eatm
     */
    private JmsDefinition eatm;
    /**
     * eatm
     */
    private JmsDefinition eatmAck;
    /**
     * hce
     */
    private JmsDefinition hce;
    /**
     * hce
     */
    private JmsDefinition hceAck;
    /**
     * cbspend
     */
    private JmsDefinition cbspend;
    private JmsDefinition mft;
    private JmsDefinition mftAck;
    private JmsDefinition twmp;
    private JmsDefinition twmpAck;
    private JmsDefinition fisc;
    private JmsDefinition mch;
    private JmsDefinition mchAck;
    private JmsDefinition eip;
    private JmsDefinition eipAck;
    private JmsDefinition eoi;
    private JmsDefinition eoiAck;
    private JmsDefinition vip;
    private JmsDefinition vipAck;
    private JmsDefinition nonvip;
    private JmsDefinition nonvipAck;

    public JmsDefinition getVip() {
        return vip;
    }

    public void setVip(JmsDefinition vip) {
        this.vip = vip;
    }

    public JmsDefinition getVipAck() {
        return vipAck;
    }

    public void setVipAck(JmsDefinition vipAck) {
        this.vipAck = vipAck;
    }

    public JmsDefinition getNonvip() {
        return nonvip;
    }

    public void setNonvip(JmsDefinition nonvip) {
        this.nonvip = nonvip;
    }

    public JmsDefinition getNonvipAck() {
        return nonvipAck;
    }

    public void setNonvipAck(JmsDefinition nonvipAck) {
        this.nonvipAck = nonvipAck;
    }

    public JmsDefinition getEip() {
        return eip;
    }

    public void setEip(JmsDefinition eip) {
        this.eip = eip;
    }

    public JmsDefinition getEipAck() {
        return eipAck;
    }

    public void setEipAck(JmsDefinition eipAck) {
        this.eipAck = eipAck;
    }

    public JmsDefinition getEoi() {
        return eoi;
    }

    public void setEoi(JmsDefinition eoi) {
        this.eoi = eoi;
    }

    public JmsDefinition getEoiAck() {
        return eoiAck;
    }

    public void setEoiAck(JmsDefinition eoiAck) {
        this.eoiAck = eoiAck;
    }

    public JmsDefinition getMchAck() {
        return mchAck;
    }

    public void setMchAck(JmsDefinition mchAck) {
        this.mchAck = mchAck;
    }

    public JmsDefinition getMch() {
        return mch;
    }

    public void setMch(JmsDefinition mch) {
        this.mch = mch;
    }

    public JmsDefinition getTest() {
        return test;
    }

    public void setTest(JmsDefinition test) {
        this.test = test;
    }

    public JmsDefinition getDead() {
        return dead;
    }

    public void setDead(JmsDefinition dead) {
        this.dead = dead;
    }

    public JmsDefinition getDeadFisc() {
        return deadFisc;
    }

    public void setDeadFisc(JmsDefinition deadFisc) {
        this.deadFisc = deadFisc;
    }

    public JmsDefinition getBatch() {
        return batch;
    }

    public void setBatch(JmsDefinition batch) {
        this.batch = batch;
    }

    public JmsDefinition getAtmMon() {
        return atmMon;
    }

    public void setAtmMon(JmsDefinition atmMon) {
        this.atmMon = atmMon;
    }

    public JmsDefinition getEms() {
        return ems;
    }

    public void setEms(JmsDefinition ems) {
        this.ems = ems;
    }

    public JmsDefinition getPyBatch() {
        return pyBatch;
    }

    public void setPyBatch(JmsDefinition pyBatch) {
        this.pyBatch = pyBatch;
    }

    public JmsDefinition getPyBatchAck() {
        return pyBatchAck;
    }

    public void setPyBatchAck(JmsDefinition pyBatchAck) {
        this.pyBatchAck = pyBatchAck;
    }

    public JmsDefinition getNb() {
        return nb;
    }

    public void setNb(JmsDefinition nb) {
        this.nb = nb;
    }

    public JmsDefinition getNbQAck() {
        return nbQAck;
    }

    public void setNbQAck(JmsDefinition nbQAck) {
        this.nbQAck = nbQAck;
    }

    public JmsDefinition getNbPAck() {
        return nbPAck;
    }

    public void setNbPAck(JmsDefinition nbPAck) {
        this.nbPAck = nbPAck;
    }

    public JmsDefinition getMb() {
        return mb;
    }

    public void setMb(JmsDefinition mb) {
        this.mb = mb;
    }

    public JmsDefinition getMbAck() {
        return mbAck;
    }

    public void setMbAck(JmsDefinition mbAck) {
        this.mbAck = mbAck;
    }

    public JmsDefinition getIvr() {
        return ivr;
    }

    public void setIvr(JmsDefinition ivr) {
        this.ivr = ivr;
    }

    public JmsDefinition getIvrAck() {
        return ivrAck;
    }

    public void setIvrAck(JmsDefinition ivrAck) {
        this.ivrAck = ivrAck;
    }

    public JmsDefinition getEatm() {
        return eatm;
    }

    public void setEatm(JmsDefinition eatm) {
        this.eatm = eatm;
    }

    public JmsDefinition getEatmAck() {
        return eatmAck;
    }

    public void setEatmAck(JmsDefinition eatmAck) {
        this.eatmAck = eatmAck;
    }

    public JmsDefinition getHce() {
        return hce;
    }

    public void setHce(JmsDefinition hce) {
        this.hce = hce;
    }

    public JmsDefinition getHceAck() {
        return hceAck;
    }

    public void setHceAck(JmsDefinition hceAck) {
        this.hceAck = hceAck;
    }

    public JmsDefinition getCbspend() {
        return cbspend;
    }

    public void setCbspend(JmsDefinition cbspend) {
        this.cbspend = cbspend;
    }

    public JmsDefinition getMft() {
        return mft;
    }

    public void setMft(JmsDefinition mft) {
        this.mft = mft;
    }

    public JmsDefinition getMftAck() {
        return mftAck;
    }

    public void setMftAck(JmsDefinition mftAck) {
        this.mftAck = mftAck;
    }

    public JmsDefinition getTwmp() {
        return twmp;
    }

    public void setTwmp(JmsDefinition twmp) {
        this.twmp = twmp;
    }

    public JmsDefinition getTwmpAck() {
        return twmpAck;
    }

    public void setTwmpAck(JmsDefinition twmpAck) {
        this.twmpAck = twmpAck;
    }

    public JmsDefinition getFisc() {
        return fisc;
    }

    public void setFisc(JmsDefinition fisc) {
        this.fisc = fisc;
    }

    public void toString(StringBuilder sb, String configurationPropertiesPrefix) {
        Field[] fields = this.getClass().getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                JmsDefinition jmsDefinition = (JmsDefinition) ReflectionUtils.getField(field, this);
                if (jmsDefinition != null) {
                    jmsDefinition.toString(sb, StringUtils.join(configurationPropertiesPrefix, ".", field.getName()));
                }
            }
        }
    }
}
