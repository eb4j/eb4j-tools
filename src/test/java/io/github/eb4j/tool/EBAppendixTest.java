package io.github.eb4j.tool;

import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static org.testng.Assert.assertEquals;


public class EBAppendixTest {

    @Test
    public void testEBAppendix() throws Exception {
        EBAppendix ebAppendix = new EBAppendix();
        File appendixPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/appendix.yml"))
                .getFile()).getAbsoluteFile();
        File outPath = Files.createTempDirectory("testEBAppendix").toFile();
        outPath.deleteOnExit();
        ebAppendix.path = appendixPath;
        ebAppendix.outDir = outPath.getAbsolutePath();
        ebAppendix.verbose = true;
        ebAppendix.appendix = EBAppendix.getAppendix(appendixPath);
        int result = ebAppendix.generate();
        assertEquals(result, 0);
    }

}
