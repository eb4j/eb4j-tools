package io.github.eb4j.tool;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tokyo.northside.io.FileUtils2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/** EBZip Test.
 * Created by miurahr on 16/06/24.
 */
public class EBZipTest {
    private ByteArrayOutputStream outContent;

    /**
     * Setup streams.
     */
    @BeforeMethod
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent, true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            fail("System error.");
        }
    }

    /**
     * Test EBZip results.
     * @throws Exception when read error happened.
     */
    @Test
    @SuppressWarnings("checkstyle:methodname")
    public void testEBZip_info() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile())
                .getAbsoluteFile();
        String expected = "==> " + bookPath + "/test/DATA/HONMON.ebz <==\n"
                + "10240 -> 2337 bytes (22.8%, ebzip level 0 compression)\n"
                + "\n"
                + "==> " + bookPath + "/chimei/DATA/HONMON.ebz <==\n"
                + "10240 -> 2386 bytes (23.3%, ebzip level 0 compression)\n"
                + "\n"
                + "==> " + bookPath + "/CATALOGS <==\n"
                + "2048 bytes (not compressed)\n\n";
        EBZip ebZip = new EBZip();
        ebZip.bookDir = bookPath;
        ebZip.actionInfo = true;
        ebZip.quiet = false;
        ebZip.skips = Collections.emptyList();
        ebZip.exec();
        assertEquals(outContent.toString("UTF-8"), expected);
    }


    /**
     * Test EBZip results.
     * @throws Exception when read error happened.
     */
    @Test
    @SuppressWarnings("checkstyle:methodname")
    public void testEBZip_compress() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing").getFile())
                .getAbsoluteFile();
        String outPath = Files.createTempDirectory("testEBZip_compress").toFile().getAbsolutePath();
        String expected = "==> compress " + bookPath + "/test/DATA/HONMON <==\n"
                + "output to " + outPath + "/test/DATA/HONMON.ebz\n"
                + "completed (10240 / 10240 bytes)\n"
                + "10240 -> 326 bytes (3.2%)\n\n"
                + "==> compress " + bookPath + "/chimei/DATA/HONMON <==\n"
                + "output to " + outPath + "/chimei/DATA/HONMON.ebz\n"
                + "completed (10240 / 10240 bytes)\n"
                + "10240 -> 377 bytes (3.7%)\n\n"
                + "==> copy " + bookPath + "/CATALOGS <==\n"
                + "output to " + outPath + "/CATALOGS\n"
                + "completed (2048 / 2048 bytes)\n\n";
        EBZip ebZip = new EBZip();
        ebZip.bookDir = bookPath;
        ebZip.outDir = outPath;
        ebZip.keep = true;
        ebZip.skips = Collections.emptyList();
        ebZip.actionZip = true;
        ebZip.quiet = false;
        ebZip.exec();
        assertEquals(outContent.toString("UTF-8"), expected);
        // Check whether go back as same as original
        File checkPath = Files.createTempDirectory("testEBZip_compress_check").toFile().getAbsoluteFile();
        ebZip = new EBZip();
        ebZip.actionUnzip = true;
        ebZip.keep = true;
        ebZip.quiet = false;
        ebZip.bookDir = new File(outPath);
        ebZip.outDir = checkPath.toString();
        ebZip.skips = Collections.emptyList();
        ebZip.exec();
        assertTrue(FileUtils2.contentEquals(new File(checkPath + "/test/DATA/HONMON"),
                new File(bookPath + "/test/DATA/HONMON")));
    }

     /**
     * Test EBZip results.
     * @throws Exception when read error happened.
     */
    @Test
    @SuppressWarnings("checkstyle:methodname")
    public void testEBZip_uncompress() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile())
                .getAbsoluteFile();
        String outPath = Files.createTempDirectory("testEBZip_uncompress").toFile().getAbsolutePath();
        String expectedPath = new File(this.getClass().getResource("/data/epwing").getFile())
                .getAbsolutePath();
        String expected = "==> uncompress " + bookPath + "/test/DATA/HONMON.ebz <==\n"
                + "output to " + outPath + "/test/DATA/HONMON\n"
                + "completed (10240 / 10240 bytes)\n"
                + "2337 -> 10240 bytes\n\n"
                + "==> uncompress " + bookPath + "/chimei/DATA/HONMON.ebz <==\n"
                + "output to " + outPath + "/chimei/DATA/HONMON\n"
                + "completed (10240 / 10240 bytes)\n"
                + "2386 -> 10240 bytes\n\n"
                + "==> copy " + bookPath + "/CATALOGS <==\n"
                + "output to " + outPath + "/CATALOGS\n"
                + "completed (2048 / 2048 bytes)\n\n";
        EBZip ebZip = new EBZip();
        ebZip.actionUnzip = true;
        ebZip.keep = true;
        ebZip.quiet = false;
        ebZip.outDir = outPath;
        ebZip.bookDir = bookPath;
        ebZip.skips = Collections.emptyList();
        ebZip.exec();
        assertEquals(outContent.toString("UTF-8"), expected);
        assertTrue(FileUtils2.contentEquals(new File(outPath + "/test/DATA/HONMON"),
                new File(expectedPath + "/test/DATA/HONMON")));
    }

    /**
     * Clean up.
     */
    @AfterMethod
    public void cleanUpStreams() {
        System.setOut(null);
    }
}
