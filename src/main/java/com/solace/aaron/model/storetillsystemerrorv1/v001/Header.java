
package com.solace.aaron.model.storetillsystemerrorv1.v001;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Generic message header.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sentAt",
    "transactionId",
    "storeId"
})
@Generated("jsonschema2pojo")
public class Header {

    /**
     * Date and time when the message was sent.
     * (Required)
     * 
     */
    @JsonProperty("sentAt")
    @JsonPropertyDescription("Date and time when the message was sent.")
    private Date sentAt;
    /**
     * The transaction id.
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    @JsonPropertyDescription("The transaction id.")
    private String transactionId;
    /**
     * The store id.
     * (Required)
     * 
     */
    @JsonProperty("storeId")
    @JsonPropertyDescription("The store id.")
    private String storeId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * Date and time when the message was sent.
     * (Required)
     * 
     */
    @JsonProperty("sentAt")
    public Date getSentAt() {
        return sentAt;
    }

    /**
     * Date and time when the message was sent.
     * (Required)
     * 
     */
    @JsonProperty("sentAt")
    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Header withSentAt(Date sentAt) {
        this.sentAt = sentAt;
        return this;
    }

    /**
     * The transaction id.
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * The transaction id.
     * (Required)
     * 
     */
    @JsonProperty("transactionId")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Header withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    /**
     * The store id.
     * (Required)
     * 
     */
    @JsonProperty("storeId")
    public String getStoreId() {
        return storeId;
    }

    /**
     * The store id.
     * (Required)
     * 
     */
    @JsonProperty("storeId")
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Header withStoreId(String storeId) {
        this.storeId = storeId;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Header withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Header.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sentAt");
        sb.append('=');
        sb.append(((this.sentAt == null)?"<null>":this.sentAt));
        sb.append(',');
        sb.append("transactionId");
        sb.append('=');
        sb.append(((this.transactionId == null)?"<null>":this.transactionId));
        sb.append(',');
        sb.append("storeId");
        sb.append('=');
        sb.append(((this.storeId == null)?"<null>":this.storeId));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.sentAt == null)? 0 :this.sentAt.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.storeId == null)? 0 :this.storeId.hashCode()));
        result = ((result* 31)+((this.transactionId == null)? 0 :this.transactionId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Header) == false) {
            return false;
        }
        Header rhs = ((Header) other);
        return (((((this.sentAt == rhs.sentAt)||((this.sentAt!= null)&&this.sentAt.equals(rhs.sentAt)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.storeId == rhs.storeId)||((this.storeId!= null)&&this.storeId.equals(rhs.storeId))))&&((this.transactionId == rhs.transactionId)||((this.transactionId!= null)&&this.transactionId.equals(rhs.transactionId))));
    }

}
