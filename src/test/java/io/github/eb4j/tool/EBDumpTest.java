package io.github.eb4j.tool;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Created by miurahr on 16/06/21.
 */
public class EBDumpTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    /**
     * Setup streams.
     */
    @BeforeMethod
    @SuppressWarnings("checkstyle:methodname")
    public void setUpStreams() {
        try {
            System.setOut(new PrintStream(outContent, true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            fail("System error.");
        }
    }

    /**
     * Test dump results.
     * @throws Exception when read error happened.
     */
    @Test
    @SuppressWarnings("checkstyle:methodname")
    public void testEBDump_dump() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing").getFile()).getAbsoluteFile();
        EBDump ebDump = new EBDump();
        ebDump.subindex = 0;
        ebDump.pos = 0L;
        ebDump.size = 64;
        ebDump.path = bookPath;
        ebDump.dump();
        String expected =
                "00001:000  00 03 20 00 00 00 00 00  00 00 00 00 00 00 00 00  ................\n"
                + "00001:010  00 00 00 00 00 02 00 00  00 01 01 00 00 00 00 00  ................\n"
                + "00001:020  05 00 00 00 00 03 00 00  00 01 01 00 00 00 00 00  ................\n"
                + "00001:030  91 00 00 00 00 04 00 00  00 02 02 41 55 40 00 00  ............\u5ABD..\n";
        assertEquals(outContent.toString("UTF-8"), expected);
    }

    /**
     * Clean up.
     */
    @AfterMethod
    public void cleanUpStreams() {
        System.setOut(null);
    }
}
