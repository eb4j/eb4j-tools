package io.github.eb4j.tool.appendix;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Alternative character Mapping definition class.
 */
public class AltDef {
    @JsonProperty("range")
    private Range range;
    private Map<String, String> altMap = new HashMap<>();

    /**
     * Default constructor.
     */
    public AltDef() {
    }

    /**
     * Constructor.
     * @param range range of definition.
     * @param altMap alternative glyph map.
     */
    public AltDef(final Range range, final Map<String, String> altMap) {
        this.range = range;
        this.altMap = altMap;
    }

    /**
     * Getter for range.
     * @return range of map.
     */
    @JsonGetter
    public Range getRange() {
        return range;
    }

    /**
     * Setter for jackson to add range.
     * @param range range of map.
     */
    @JsonSetter
    public void setRange(final Range range) {
        this.range = range;
    }

    /**
     * Deserializer of map.
     * @param map alternative glyph map to deserialize.
     */
    @JsonProperty("map")
    @SuppressWarnings("unchecked")
    public void mapDeserializer(final Map<Object, Object> map) {
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

    @JsonProperty("map")
    public Map<String, String> getAltMap() {
        return altMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(e -> "0x" + e.getKey(), Map.Entry::getValue,
                        (v1, v2) -> {
                    throw new RuntimeException(String.format("Duplicate key: %s and %s", v1, v2));
                    },
                        TreeMap::new));
    }

    /**
     * Getter for alternative character string from key(int).
     * @param key int char index.
     * @return alternative string.
     */
    @JsonIgnore
    public String getAlt(final int key) {
        return altMap.get(String.format("%04X", key));
    }

    /**
     * wrapper for containsKey for the map.
     * @param key to query as integer.
     * @return true if alternative glyph exist, otherwise false.
     */
    public boolean containsKey(final int key) {
        String strKey = String.format("%04X", key);
        if (!altMap.containsKey(strKey)) {
            return false;
        }
        return !StringUtils.isBlank(altMap.get(strKey));
    }

    /**
     * Getter for key list.
     * @return key set in integer.
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
