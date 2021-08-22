package io.github.eb4j.tool;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Test of appendix subcommand.
 */
public class EBAppendixTest {

    /**
     * Test loading actual definition data and compare with expected output.
     * @throws Exception when file I/O failure.
     */
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
        File furokuPath = new File(Objects.requireNonNull(this.getClass().getResource("/data/furoku"))
                .getFile()).getAbsoluteFile();
        assertTrue(FileUtils.contentEquals(new File(outPath, "chujiten/data/furoku"), furokuPath));
    }
}
