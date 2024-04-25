//
// 此檔案是由 JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 所產生 
// 請參閱 <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2021.06.08 於 10:45:15 AM CST 
//


package com.syscom.fep.ws.client.t24.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Status" type="{http://temenos.com/FEP}Status" minOccurs="0" form="unqualified"/&gt;
 *         &lt;element name="OfsResponse" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="unqualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "status",
    "ofsResponse"
})
@XmlRootElement(name = "callOfsResponse")
public class CallOfsResponse {

    @XmlElement(name = "Status", namespace = "")
    protected Status status;
    @XmlElement(name = "OfsResponse", namespace = "")
    protected String ofsResponse;

    /**
     * 取得 status 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Status }
     *     
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 設定 status 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Status }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * 取得 ofsResponse 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfsResponse() {
        return ofsResponse;
    }

    /**
     * 設定 ofsResponse 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfsResponse(String value) {
        this.ofsResponse = value;
    }

}
