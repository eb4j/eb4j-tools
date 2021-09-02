package io.github.eb4j.tool;

import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import io.github.eb4j.ext.UnicodeMap;
import io.github.eb4j.tool.appendix.AltDef;
import io.github.eb4j.tool.appendix.Appendix;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class EBMapTest {

    @Test
    public void testEBMapCreateAppendixObj() throws Exception {
        File mapPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/chimei.map"))
                .getFile()).getAbsoluteFile();
        EBMap ebMap = new EBMap();
        UnicodeMap unicodeMap = new UnicodeMap(mapPath);
        Appendix appendix = ebMap.constructData(unicodeMap);
        assertNotNull(appendix);
    }

    @Test
    public void testEBMapLoadAltDef() throws Exception {
        File mapPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/chimei.map"))
                .getFile()).getAbsoluteFile();
        EBMap ebMap = new EBMap();
        UnicodeMap unicodeMap = new UnicodeMap(mapPath);
        AltDef narrow = ebMap.generateAltDef(unicodeMap.getNarrowMap());
        assertEquals(narrow.getStart(), 0xA121);
        assertEquals(narrow.getEnd(), 0xA222);
        assertEquals(narrow.getAlt(0xA12D), "a");
    }

    @Test
    public void testEBMapCreateYaml() throws Exception {
        File mapPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/chimei.map"))
                .getFile()).getAbsoluteFile();
        File outPath = new File(Files.createTempDirectory("testEBMap").toFile(), "test.yml");
        // outPath.deleteOnExit();
        EBMap ebMap = new EBMap();
        UnicodeMap unicodeMap = new UnicodeMap(mapPath);
        Appendix appendix = ebMap.constructData(unicodeMap);
        ebMap.yamlFile = outPath;
        ebMap.generateYaml(appendix);
        appendix = EBAppendix.getAppendix(outPath);
        assertNotNull(appendix);
    }

}
