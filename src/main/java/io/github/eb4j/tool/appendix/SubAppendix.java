package io.github.eb4j.tool.appendix;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    /**
     * Default constructor.
     */
    public SubAppendix() {
    }

    /**
     * Constructor
     * @param name of directory.
     * @param unicode true if yaml has unicode definition, otherwise false.
     * @param encoding JISX0208 or ISO8859_1
     * @param stopCode stop code of the book.
     * @param narrow alternative glyph map for narrow fonts.
     * @param wide alternative glyph map for wide fonts.
     */
    public SubAppendix(final String name, final Boolean unicode, final String encoding, final String stopCode,
                       final AltDef narrow, final AltDef wide) {
        this.name = name;
        this.unicode = unicode;
        this.encoding = encoding;
        this.stopCode = stopCode;
        this.narrow = narrow;
        this.wide = wide;
    }

    /**
     * Name of subBook.
     */
    @JsonProperty("name")
    public String name;

    /**
     * Flag to use Unicode character.
     */
    @JsonProperty("unicode")
    public Boolean unicode;

    /**
     * Book encoding.
     */
    @JsonProperty("character-code")
    private String encoding;

    /**
     * Stop code.
     */
    @JsonProperty("stop-code")
    private String stopCode;

    /**
     * alternative definitions for Narrow fonts.
     */
    public AltDef narrow;
    /**
     * alternative definitions for Wide fonts.
     */
    public AltDef wide;

    /**
     * Setter of stop code.
     * @param stopCode in string.
     */
    @JsonSetter
    public void setStopCode(final String stopCode) {
        this.stopCode = stopCode;
    }

    /**
     * Getter of stop code.
     * @return byte array of stop code.
     */
    @JsonGetter
    public String getStopCode() {
        return stopCode;
    }

    /**
     * Getter of stop code as byte array.
     * @return byte array of stop code.
     */
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

    @JsonSetter
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public boolean hasNarrow() {
        return narrow != null;
    }

    public boolean hasWide() {
        return wide != null;
    }

    public boolean hasStopCode() {
        return stopCode != null;
    }
}
