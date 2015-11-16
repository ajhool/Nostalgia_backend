//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.28 at 08:11:08 PM EDT 
//


package com.portol.common.model.dash.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RepresentationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepresentationType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:mpeg:dash:schema:mpd:2011}RepresentationBaseType">
 *       &lt;sequence>
 *         &lt;element name="BaseURL" type="{urn:mpeg:dash:schema:mpd:2011}BaseURLType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SubRepresentation" type="{urn:mpeg:dash:schema:mpd:2011}SubRepresentationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SegmentBase" type="{urn:mpeg:dash:schema:mpd:2011}SegmentBaseType" minOccurs="0"/>
 *         &lt;element name="SegmentList" type="{urn:mpeg:dash:schema:mpd:2011}SegmentListType" minOccurs="0"/>
 *         &lt;element name="SegmentTemplate" type="{urn:mpeg:dash:schema:mpd:2011}SegmentTemplateType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{urn:mpeg:dash:schema:mpd:2011}StringNoWhitespaceType" />
 *       &lt;attribute name="bandwidth" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="qualityRanking" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="dependencyId" type="{urn:mpeg:dash:schema:mpd:2011}StringVectorType" />
 *       &lt;attribute name="mediaStreamStructureId" type="{urn:mpeg:dash:schema:mpd:2011}StringVectorType" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepresentationType", propOrder = {
    "baseURL",
    "subRepresentation",
    "segmentBase",
    "segmentList",
    "segmentTemplate"
})
public class RepresentationType
    extends RepresentationBaseType
{

    @XmlElement(name = "BaseURL")
    protected List<BaseURLType> baseURL;
    @XmlElement(name = "SubRepresentation")
    protected List<SubRepresentationType> subRepresentation;
    @XmlElement(name = "SegmentBase")
    protected SegmentBaseType segmentBase;
    @XmlElement(name = "SegmentList")
    protected SegmentListType segmentList;
    @XmlElement(name = "SegmentTemplate")
    protected SegmentTemplateType segmentTemplate;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "bandwidth", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long bandwidth;
    @XmlAttribute(name = "qualityRanking")
    @XmlSchemaType(name = "unsignedInt")
    protected Long qualityRanking;
    @XmlAttribute(name = "dependencyId")
    protected List<String> dependencyId;
    @XmlAttribute(name = "mediaStreamStructureId")
    protected List<String> mediaStreamStructureId;

    /**
     * Gets the value of the baseURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the baseURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBaseURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BaseURLType }
     * 
     * 
     */
    public List<BaseURLType> getBaseURL() {
        if (baseURL == null) {
            baseURL = new ArrayList<BaseURLType>();
        }
        return this.baseURL;
    }

    /**
     * Gets the value of the subRepresentation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subRepresentation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubRepresentation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubRepresentationType }
     * 
     * 
     */
    public List<SubRepresentationType> getSubRepresentation() {
        if (subRepresentation == null) {
            subRepresentation = new ArrayList<SubRepresentationType>();
        }
        return this.subRepresentation;
    }

    /**
     * Gets the value of the segmentBase property.
     * 
     * @return
     *     possible object is
     *     {@link SegmentBaseType }
     *     
     */
    public SegmentBaseType getSegmentBase() {
        return segmentBase;
    }

    /**
     * Sets the value of the segmentBase property.
     * 
     * @param value
     *     allowed object is
     *     {@link SegmentBaseType }
     *     
     */
    public void setSegmentBase(SegmentBaseType value) {
        this.segmentBase = value;
    }

    /**
     * Gets the value of the segmentList property.
     * 
     * @return
     *     possible object is
     *     {@link SegmentListType }
     *     
     */
    public SegmentListType getSegmentList() {
        return segmentList;
    }

    /**
     * Sets the value of the segmentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link SegmentListType }
     *     
     */
    public void setSegmentList(SegmentListType value) {
        this.segmentList = value;
    }

    /**
     * Gets the value of the segmentTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link SegmentTemplateType }
     *     
     */
    public SegmentTemplateType getSegmentTemplate() {
        return segmentTemplate;
    }

    /**
     * Sets the value of the segmentTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SegmentTemplateType }
     *     
     */
    public void setSegmentTemplate(SegmentTemplateType value) {
        this.segmentTemplate = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the bandwidth property.
     * 
     */
    public long getBandwidth() {
        return bandwidth;
    }

    /**
     * Sets the value of the bandwidth property.
     * 
     */
    public void setBandwidth(long value) {
        this.bandwidth = value;
    }

    /**
     * Gets the value of the qualityRanking property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getQualityRanking() {
        return qualityRanking;
    }

    /**
     * Sets the value of the qualityRanking property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setQualityRanking(Long value) {
        this.qualityRanking = value;
    }

    /**
     * Gets the value of the dependencyId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dependencyId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDependencyId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDependencyId() {
        if (dependencyId == null) {
            dependencyId = new ArrayList<String>();
        }
        return this.dependencyId;
    }

    /**
     * Gets the value of the mediaStreamStructureId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mediaStreamStructureId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMediaStreamStructureId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMediaStreamStructureId() {
        if (mediaStreamStructureId == null) {
            mediaStreamStructureId = new ArrayList<String>();
        }
        return this.mediaStreamStructureId;
    }

}
