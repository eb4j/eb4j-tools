package io.github.eb4j.tool.appendix;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

public class Appendix {
    private String title;
    private String type;
    private List<SubAppendix> subbook = new ArrayList<>();

    @JsonGetter
    public String getTitle() {
        return title;
    }

    @JsonSetter
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter
    public List<SubAppendix> getSubbook() {
        return subbook;
    }

    @JsonSetter
    public void setSubbook(List<SubAppendix> subbook) {
        this.subbook = subbook;
    }

    @Override
    public String toString() {
        return "Appendix{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", subbook=" + subbook +
                '}';
    }
}
