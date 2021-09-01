package io.github.eb4j.tool.appendix;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alternative character Mapping definition class.
 */
public class AltDef {
    @JsonProperty("range")
    private Range range;
    private Map<String, String> altMap = new HashMap<>();

    public AltDef() {
    }

    public AltDef(Range range, Map<String, String> altMap) {
        this.range = range;
        this.altMap = altMap;
    }

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
     * Deserializer of map.
     */
    @JsonProperty("map")
    @SuppressWarnings("unchecked")
    public void mapDeserializer(Map<Object, Object> map) {
        for (Map.Entry entry: map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            try {
                int key = Integer.parseInt(String.valueOf(entry.getKey()).substring(2), 16);
                altMap.put(String.format("%04X", key), String.valueOf(entry.getValue()));
            } catch (NumberFormatException ignore) {
            }
        }
    }


    /**
     * Getter for alternative character string from key(int).
     * @param key int char index.
     * @return alternative string.
     */
    @JsonIgnore
    public String getAlt(int key) {
        return altMap.get(String.format("%04X", key));
    }

    /**
     * wrapper for containsKey for the map.
     * @param key
     * @return
     */
    public boolean containsKey(int key) {
        String strKey = String.format("%04X", key);
        if (!altMap.containsKey(strKey)) {
            return false;
        }
        return !StringUtils.isBlank(altMap.get(strKey));
    }

    /**
     * Getter for key list.
     * @return
     */
    @JsonIgnore
    public Set<Integer> keySet() {
        return altMap.keySet().stream().map(e -> Integer.parseInt(e, 16)).collect(Collectors.toSet());
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
