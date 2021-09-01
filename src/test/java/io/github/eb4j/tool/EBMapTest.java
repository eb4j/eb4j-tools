package io.github.eb4j.tool;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Objects;

import io.github.eb4j.ext.UnicodeMap;
import io.github.eb4j.tool.appendix.AltDef;
import io.github.eb4j.tool.appendix.Appendix;

public class EBMapTest {

    @Test
    public void testEBMapCreateAppendixObj() throws Exception {
        File mapPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/chimei.map"))
                .getFile()).getAbsoluteFile();
        EBMap ebMap = new EBMap();
        UnicodeMap unicodeMap = new UnicodeMap(mapPath);
        Appendix appendix = ebMap.constructData(unicodeMap);
    }

    @Test
    public void testEBMapCreateGenerateAltDef() throws Exception {
        File mapPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/chimei.map"))
                .getFile()).getAbsoluteFile();
        EBMap ebMap = new EBMap();
        UnicodeMap unicodeMap = new UnicodeMap(mapPath);
        AltDef narrow = ebMap.generateAltDef(unicodeMap.getNarrowMap());
    }

}
