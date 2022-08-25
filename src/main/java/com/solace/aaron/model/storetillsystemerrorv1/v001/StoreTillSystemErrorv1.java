
package com.solace.aaron.model.storetillsystemerrorv1.v001;

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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "payload",
    "header"
})
@Generated("jsonschema2pojo")
public class StoreTillSystemErrorv1 {

    /**
     * Till system error as response to a store transaction.
     * 
     */
    @JsonProperty("payload")
    @JsonPropertyDescription("Till system error as response to a store transaction.")
    private Payload payload;
    /**
     * Generic message header.
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("Generic message header.")
    private Header header;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * Till system error as response to a store transaction.
     * 
     */
    @JsonProperty("payload")
    public Payload getPayload() {
        return payload;
    }

    /**
     * Till system error as response to a store transaction.
     * 
     */
    @JsonProperty("payload")
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public StoreTillSystemErrorv1 withPayload(Payload payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Generic message header.
     * 
     */
    @JsonProperty("header")
    public Header getHeader() {
        return header;
    }

    /**
     * Generic message header.
     * 
     */
    @JsonProperty("header")
    public void setHeader(Header header) {
        this.header = header;
    }

    public StoreTillSystemErrorv1 withHeader(Header header) {
        this.header = header;
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

    public StoreTillSystemErrorv1 withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StoreTillSystemErrorv1 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("payload");
        sb.append('=');
        sb.append(((this.payload == null)?"<null>":this.payload));
        sb.append(',');
        sb.append("header");
        sb.append('=');
        sb.append(((this.header == null)?"<null>":this.header));
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
        result = ((result* 31)+((this.header == null)? 0 :this.header.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.payload == null)? 0 :this.payload.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreTillSystemErrorv1) == false) {
            return false;
        }
        StoreTillSystemErrorv1 rhs = ((StoreTillSystemErrorv1) other);
        return ((((this.header == rhs.header)||((this.header!= null)&&this.header.equals(rhs.header)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.payload == rhs.payload)||((this.payload!= null)&&this.payload.equals(rhs.payload))));
    }

}
