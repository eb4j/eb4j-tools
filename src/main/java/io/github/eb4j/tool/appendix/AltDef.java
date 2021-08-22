package io.github.eb4j.tool.appendix;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Alternative character Mapping definition class.
 */
public class AltDef {
    @JsonProperty("range")
    private Range range;
    @JsonProperty("map")
    private Map<String, String> map = new HashMap<>();

    /**
     * Getter for range.
     * @return
     */
    @JsonGetter
    public Range getRange() {
        return range;
    }

    /**
     * Setter for jackson to add range.
     * @param range
     */
    @JsonSetter
    public void setRange(Range range) {
        this.range = range;
    }

    /**
     * Setter for Jackson to add map.
     * @param k key character code
     * @param v alternative text
     */
    @JsonAnySetter
    public void setRecord(String k, String v) {
        map.put(k, v);
    }

    /**
     * Getter for alternative character string from key(int).
     * @param key int char index.
     * @return alternative string.
     */
    @JsonIgnore
    public String getAlt(int key) {
        String keyString = "0x" + Integer.toHexString(key);
        return map.get(keyString);
    }

    /**
     * wrapper for containsKey for the map.
     * @param key
     * @return
     */
    public boolean containsKey(int key) {
        String keyString = "0x" + Integer.toHexString(key);
        if (!map.containsKey(keyString)) {
            return false;
        }
        return !StringUtils.isBlank(map.get(keyString));
    }

    /**
     * Getter for key list.
     * @return
     */
    @JsonIgnore
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Get length parameter (narrowLen and wideLen).
     * @param jis true when JISX0208 book.
     * @return short number of length.
     */
    @JsonIgnore
    public short getLength(final boolean jis) {
        if (jis) {
            return (short)(((range.end >> 8) - (range.start >> 8)) * 0x5e
                    + (range.end & 0xff) - (range.start & 0xff) + 1);
        } else {
            return (short)(((range.end >> 8) - (range.start >> 8)) * 0xfe
                    + (range.end & 0xff) - (range.start & 0xff) + 1);
        }
    }

    @JsonIgnore
    public int getStart() {
        return range.start;
    }

    @JsonIgnore
    public int getEnd() {
        return range.end;
    }

}
