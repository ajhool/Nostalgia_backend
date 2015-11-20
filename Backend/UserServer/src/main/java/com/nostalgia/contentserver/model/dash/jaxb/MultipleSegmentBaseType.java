//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.28 at 08:11:08 PM EDT 
//


package com.nostalgia.contentserver.model.dash.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MultipleSegmentBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MultipleSegmentBaseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:mpeg:dash:schema:mpd:2011}SegmentBaseType">
 *       &lt;sequence>
 *         &lt;element name="SegmentTimeline" type="{urn:mpeg:dash:schema:mpd:2011}SegmentTimelineType" minOccurs="0"/>
 *         &lt;element name="BitstreamSwitching" type="{urn:mpeg:dash:schema:mpd:2011}URLType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="duration" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="startNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultipleSegmentBaseType", propOrder = {
    "segmentTimeline",
    "bitstreamSwitching"
})
@XmlSeeAlso({
    SegmentTemplateType.class,
    SegmentListType.class
})
public class MultipleSegmentBaseType
    extends SegmentBaseType
{

    @XmlElement(name = "SegmentTimeline")
    protected SegmentTimelineType segmentTimeline;
    @XmlElement(name = "BitstreamSwitching")
    protected URLType bitstreamSwitching;
    @XmlAttribute(name = "duration")
    @XmlSchemaType(name = "unsignedInt")
    protected Long duration;
    @XmlAttribute(name = "startNumber")
    @XmlSchemaType(name = "unsignedInt")
    protected Long startNumber;

    /**
     * Gets the value of the segmentTimeline property.
     * 
     * @return
     *     possible object is
     *     {@link SegmentTimelineType }
     *     
     */
    public SegmentTimelineType getSegmentTimeline() {
        return segmentTimeline;
    }

    /**
     * Sets the value of the segmentTimeline property.
     * 
     * @param value
     *     allowed object is
     *     {@link SegmentTimelineType }
     *     
     */
    public void setSegmentTimeline(SegmentTimelineType value) {
        this.segmentTimeline = value;
    }

    /**
     * Gets the value of the bitstreamSwitching property.
     * 
     * @return
     *     possible object is
     *     {@link URLType }
     *     
     */
    public URLType getBitstreamSwitching() {
        return bitstreamSwitching;
    }

    /**
     * Sets the value of the bitstreamSwitching property.
     * 
     * @param value
     *     allowed object is
     *     {@link URLType }
     *     
     */
    public void setBitstreamSwitching(URLType value) {
        this.bitstreamSwitching = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDuration(Long value) {
        this.duration = value;
    }

    /**
     * Gets the value of the startNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getStartNumber() {
        return startNumber;
    }

    /**
     * Sets the value of the startNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStartNumber(Long value) {
        this.startNumber = value;
    }

}
