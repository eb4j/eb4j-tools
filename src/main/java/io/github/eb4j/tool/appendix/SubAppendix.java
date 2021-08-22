package io.github.eb4j.tool.appendix;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubAppendix {
    private static final Pattern PATTERN = Pattern.compile("0x1f([0-9]{2})([0-9-a-f]{2})([0-9a-f]{2})",
            Pattern.CASE_INSENSITIVE);

    public String name;

    @JsonProperty("character-code")
    private String encoding;

    @JsonProperty("stop-code")
    private String stopCode;

    public AltDef narrow;
    public AltDef wide;

    @JsonSetter
    public void setStopCode(final String stopCode) {
        this.stopCode = stopCode;
    }

    @JsonGetter
    public String getStopCode() {
        return stopCode;
    }

    @JsonIgnore
    public byte[] getStopCodeBytes() {
        Matcher m = PATTERN.matcher(stopCode);
        if (!m.matches()) {
            return null;
        }
        Byte[] b = new Byte[4];
        b[0] = 0x1f;
        b[1] = Byte.parseByte(m.group(1), 16);
        b[2] = Byte.parseByte(m.group(2), 16);
        b[3] = Byte.parseByte(m.group(3), 16);
        return ArrayUtils.toPrimitive(b);
    }

    public boolean isEncoding(final String encode) {
        return encoding.equalsIgnoreCase(encode);
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public boolean hasNarrow() {
        return narrow != null;
    }

    public boolean hasWide() {
        return narrow != null;
    }

    public boolean hasStopCode() {
        return stopCode != null;
    }
}
