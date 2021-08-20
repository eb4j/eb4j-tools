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

public class AltDef {
    @JsonProperty("range")
    private Range range;
    @JsonProperty("map")
    private Map<String, String> map = new HashMap<>();

    @JsonGetter
    public Range getRange() {
        return range;
    }

    @JsonSetter
    public void setRange(Range range) {
        this.range = range;
    }

    @JsonAnySetter
    public void setRecord(String k, String v) {
        map.put(k, v);
    }

    @JsonIgnore
    public String getAlt(int key) {
        String keyString = "0x" + Integer.toHexString(key);
        return map.get(keyString);
    }

    public boolean containsKey(int key) {
        String keyString = "0x" + Integer.toHexString(key);
        if (!map.containsKey(keyString)) {
            return false;
        }
        return !StringUtils.isBlank(map.get(keyString));
    }

    @JsonIgnore
    public Set<String> keySet() {
        return map.keySet();
    }

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
