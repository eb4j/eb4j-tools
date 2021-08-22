package io.github.eb4j.tool.appendix;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Range {
    @JsonIgnore
    public int start;
    @JsonIgnore
    public int end;

    @JsonGetter("start")
    public String getStart() {
        return "0x" + Integer.toHexString(start);
    }

    @JsonSetter("start")
    public void setStart(String start) {
        this.start = Integer.parseInt(start.substring(2), 16);
    }

    @JsonGetter("end")
    public String getEnd() {
        return "0x" + Integer.toHexString(end);
    }

    @JsonSetter("end")
    public void setEnd(String end) {
        this.end = Integer.parseInt(end.substring(2), 16);
    }
}
