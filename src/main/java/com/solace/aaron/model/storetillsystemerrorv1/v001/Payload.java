
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


/**
 * Till system error as response to a store transaction.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "errorId",
    "errorDescription",
    "errorDetails",
    "originalTransactionId"
})
@Generated("jsonschema2pojo")
public class Payload {

    /**
     * The Error Id.
     * 
     */
    @JsonProperty("errorId")
    @JsonPropertyDescription("The Error Id.")
    private String errorId;
    @JsonProperty("errorDescription")
    private String errorDescription;
    @JsonProperty("errorDetails")
    private ErrorDetails errorDetails;
    /**
     * The original transaction id the error refers to.
     * 
     */
    @JsonProperty("originalTransactionId")
    @JsonPropertyDescription("The original transaction id the error refers to.")
    private String originalTransactionId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * The Error Id.
     * 
     */
    @JsonProperty("errorId")
    public String getErrorId() {
        return errorId;
    }

    /**
     * The Error Id.
     * 
     */
    @JsonProperty("errorId")
    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public Payload withErrorId(String errorId) {
        this.errorId = errorId;
        return this;
    }

    @JsonProperty("errorDescription")
    public String getErrorDescription() {
        return errorDescription;
    }

    @JsonProperty("errorDescription")
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Payload withErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    @JsonProperty("errorDetails")
    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    @JsonProperty("errorDetails")
    public void setErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Payload withErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    /**
     * The original transaction id the error refers to.
     * 
     */
    @JsonProperty("originalTransactionId")
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    /**
     * The original transaction id the error refers to.
     * 
     */
    @JsonProperty("originalTransactionId")
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public Payload withOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
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

    public Payload withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Payload.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("errorId");
        sb.append('=');
        sb.append(((this.errorId == null)?"<null>":this.errorId));
        sb.append(',');
        sb.append("errorDescription");
        sb.append('=');
        sb.append(((this.errorDescription == null)?"<null>":this.errorDescription));
        sb.append(',');
        sb.append("errorDetails");
        sb.append('=');
        sb.append(((this.errorDetails == null)?"<null>":this.errorDetails));
        sb.append(',');
        sb.append("originalTransactionId");
        sb.append('=');
        sb.append(((this.originalTransactionId == null)?"<null>":this.originalTransactionId));
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
        result = ((result* 31)+((this.errorId == null)? 0 :this.errorId.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.errorDescription == null)? 0 :this.errorDescription.hashCode()));
        result = ((result* 31)+((this.originalTransactionId == null)? 0 :this.originalTransactionId.hashCode()));
        result = ((result* 31)+((this.errorDetails == null)? 0 :this.errorDetails.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Payload) == false) {
            return false;
        }
        Payload rhs = ((Payload) other);
        return ((((((this.errorId == rhs.errorId)||((this.errorId!= null)&&this.errorId.equals(rhs.errorId)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.errorDescription == rhs.errorDescription)||((this.errorDescription!= null)&&this.errorDescription.equals(rhs.errorDescription))))&&((this.originalTransactionId == rhs.originalTransactionId)||((this.originalTransactionId!= null)&&this.originalTransactionId.equals(rhs.originalTransactionId))))&&((this.errorDetails == rhs.errorDetails)||((this.errorDetails!= null)&&this.errorDetails.equals(rhs.errorDetails))));
    }

}
