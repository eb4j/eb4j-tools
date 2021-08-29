package io.github.eb4j.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import io.github.eb4j.ext.UnicodeEscaper;
import io.github.eb4j.tool.appendix.Appendix;
import io.github.eb4j.tool.appendix.SubAppendix;

/**
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "appendix", mixinStandardHelpOptions = true, description = "Compile EPWING furoku data")
public final class EBAppendix implements Callable<Integer>  {

    private static final String DEFAULT_APPENDIX_DIR = ".";
    /**
     * maximum length of alternation text w/o null terminate.
     */
    private static final int MAXLEN_ALTERNATION = 31;

    /**
     * maximum length of subbook name.
     */
    private static final int MAXLEN_SUBNAME = 8;

    private static final int SIZE_PAGE = 2048;

    private static final int MAX_SUBBOOKS = 50;

    protected Appendix appendix;

    /**
     * Command line options and arguments.
     */

    @CommandLine.Option(names = {"-o", "--output-directory"}, description = "output files to DIRECTORY")
    String outDir = DEFAULT_APPENDIX_DIR;

    @CommandLine.Option(names = {"--no-catalog"}, negatable = true, description = "don't output a catalog file")
    boolean catalog = false;

    @CommandLine.Option(names = {"-c", "--compat"}, description = "compatibility mode (no unicode extension)")
    boolean compat = false;

    @CommandLine.Option(names = {"--verbose"}, description = "verbose output for debug")
    boolean verbose = false;

    @picocli.CommandLine.Parameters(description = "input YAML file")
    File path;

    static Appendix getAppendix(final File file) {
        Appendix appendix;
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try {
            appendix = om.readValue(file, Appendix.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return appendix;
    }

    /**
     * Main function.
     * @param args
     */
    public static void main(final String... args) {
        System.exit(new CommandLine(new EBAppendix()).execute(args));
    }

    /**
     * Run program, or throws an exception if got error.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        if (!path.exists() || !path.isFile() || !path.getPath().endsWith(".yml")) {
            System.err.println("Input YAML file(.yml) does not exist.");
            return 1;
        }
        if (verbose) {
            System.err.println("Parsing input file:" + path.getPath());
        }
        try {
            appendix = getAppendix(path);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        if (appendix == null) {
            System.out.println("*** Failed to read specified definition. Abort...");
            return 1;
        }
        if (catalog) {
            File outFile;
            if (appendix.getType().equals("EB")) {
                outFile = new File(outDir, "catalog");
            } else {
                outFile = new File(outDir, "catalogs");
            }
            createCatalogFile(outFile);
        }
        if (appendix.getSubbook().size() > MAX_SUBBOOKS) {
            System.out.println("*** A number of subbook definitions are exceeded the limit. Abort...");
            return 1;
        }
        for (SubAppendix subbook : appendix.getSubbook()) {
            File outFile;
            if (appendix.getType().equals("EB")) {
                File outTarget = new File(outDir, subbook.name);
                boolean ignore = outTarget.mkdirs();
                outFile = new File(new File(outDir, subbook.name), "appendix");
            } else {
                File outTarget = new File(new File(outDir, subbook.name), "data");
                boolean ignore = outTarget.mkdirs();
                outFile = new File(outTarget, "furoku");
            }
            appendixCheck(subbook);
            if (verbose) {
                System.err.println("Start output to: "+ outFile.getPath());
            }
            try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
                appendixWriter(raf, subbook);
            }
        }
        return 0;
    }

    private void createCatalogFile(final File outFile) {
        // FIXME: implement me.
        System.err.println("A catalog(s) generation feature is not implemented yet.");
    }

    private void appendixWriter(final RandomAccessFile raf, final SubAppendix subbook) throws Exception {
        int narrowPage = 0;
        int widePage = 0;
        int stopPage = 0;
        UnicodeEscaper escaper = new UnicodeEscaper();
        // fill header with null bytes
        for (int i = 0; i < SIZE_PAGE; i++) {
            raf.write('\0');
        }
        // write narrow def
        if (subbook.hasNarrow()) {
            narrowPage = (int)(1 + raf.getFilePointer() / SIZE_PAGE);
            int i = subbook.narrow.getStart();
            int end = subbook.narrow.getEnd();
            if (verbose) {
                System.err.println("Write narrow character definitions (" + i + ", " + end + ")");
            }
            while (i <= end) {
                if (subbook.narrow.containsKey(i)) {
                    String altString = subbook.narrow.getAlt(i);
                    if (verbose) {
                        System.err.println("  Write (" + i + ", " + altString);
                    }
                    if (compat) {
                        byte[] altByte = altString.getBytes("EUC-JP");
                        raf.write(altByte);
                        for (int j = 0; j < 1 + MAXLEN_ALTERNATION - altByte.length; j++) {
                            raf.write('\0');
                        }
                    } else {
                        byte[] altByte = escaper.translate(altString).getBytes(StandardCharsets.US_ASCII);
                        raf.write(altByte);
                        for (int j = 0; j < 1 + MAXLEN_ALTERNATION - altByte.length; j++) {
                            raf.write('\0');
                        }
                    }
                } else {
                    for (int j= 0; j < 32; j++) {
                        raf.write('\0');
                    }
                }
                if (subbook.isEncoding("JISX0208")) {
                    if ((i & 0xff) < 0x7e) {
                        i += 1;
                    } else {
                        i += 0xa3;
                    }
                } else {
                    if ((i & 0xff) < 0xfe) {
                        i += 1;
                    } else {
                        i += 3;
                    }
                }
            }
            int padLen = (int) (SIZE_PAGE - raf.getFilePointer() % SIZE_PAGE);
            for (int j = 0; j < padLen; j++) {
                raf.write('\0');
            }
        }
        // write wide def
        if (subbook.hasWide()) {
            widePage = 1 + (int) (raf.getFilePointer() / SIZE_PAGE);
            int i = subbook.wide.getStart();
            int end = subbook.wide.getEnd();
            if (verbose) {
                System.err.println("Write wide character definitions: (" + i + ", " + end + ")");
            }
            while (i <= end) {
                if (subbook.wide.containsKey(i)) {
                    String altString = subbook.wide.getAlt(i);
                    if (verbose) {
                        System.err.println("  Write (" + i + ", " + altString);
                    }
                    if (compat) {
                        byte[] altByte = altString.getBytes("EUC-JP");
                        raf.write(altByte);
                        for (int j = 0; j < 1 + MAXLEN_ALTERNATION - altByte.length; j++) {
                            raf.write('\0');
                        }
                    } else {
                        byte[] altByte = escaper.translate(altString).getBytes(StandardCharsets.US_ASCII);
                        raf.write(altByte);
                        for (int j = 0; j < 1 + MAXLEN_ALTERNATION - altByte.length; j++) {
                            raf.write('\0');
                        }
                    }
                } else {
                    for (int j = 0; j < 32; j++) {
                        raf.write('\0');
                    }
                }
                if (subbook.isEncoding("JISX0208")) {
                    if ((i & 0xff) < 0x7e) {
                        i += 1;
                    } else {
                        i += 0xa3;
                    }
                } else {
                    if ((i & 0xff) < 0xfe) {
                        i += 1;
                    } else {
                        i += 3;
                    }
                }
            }
            int padLen = (int) (SIZE_PAGE - raf.getFilePointer() % SIZE_PAGE);
            for (int j = 0; j < padLen; j++) {
                raf.write('\0');
            }
        }
        // output stop-code
        stopPage = 1 + (int) (raf.getFilePointer() / SIZE_PAGE);
        if (subbook.hasStopCode()) {
            if (verbose) {
                System.err.println("Write stop-code...");
            }
            raf.write('\0');
            raf.write('\1');
            raf.write(subbook.getStopCodeBytes());
        }
        int padLen = (int) (SIZE_PAGE - raf.getFilePointer() % SIZE_PAGE);
        for (int j = 0; j < padLen; j++) {
            raf.write('\0');
        }

        // output index page
        if (verbose) {
            System.err.println("Write index header...");
        }
        raf.seek(0);
        raf.write('\0');
        raf.write('\3');
        if (subbook.isEncoding("JISX0208")) {
            raf.write('\0');
            raf.write('\2');
        } else {
            raf.write('\0');
            raf.write('\1');
        }
        for (int j = 0; j < 12; j++) {
            raf.write('\0');
        }
        if (subbook.hasNarrow()) {
            raf.write(ByteBuffer.allocate(4).putInt(narrowPage).array());
            for (int j = 0; j < 6; j++) {
                raf.write('\0');
            }
            raf.write(ByteBuffer.allocate(2).putShort((short)subbook.narrow.getStart()).array());
            raf.write(ByteBuffer.allocate(2).putShort(subbook.narrow.getLength(
                    subbook.isEncoding("JISX0208"))).array());
            raf.write('\0');
            raf.write('\0');
        } else {
            for (int j = 0; j< 16; j++) {
                raf.write('\0');
            }
        }
        if (subbook.hasWide()) {
            raf.write(ByteBuffer.allocate(4).putInt(widePage).array());
            for (int j = 0; j< 6; j++) {
                raf.write('\0');
            }
            raf.write(ByteBuffer.allocate(2).putShort((short)subbook.wide.getStart()).array());
            raf.write(ByteBuffer.allocate(2).putShort(subbook.wide.getLength(subbook.isEncoding("JISX0208"))).array());
            raf.write('\0');
            raf.write('\0');
        } else {
            for (int j = 0; j< 16; j++) {
                raf.write('\0');
            }
        }
        if (subbook.hasStopCode()) {
            raf.write(ByteBuffer.allocate(4).putInt(stopPage).array());
        }
        if (verbose) {
            System.err.println("...done\n");
        }
    }

    private void appendixCheck(final SubAppendix subbook) {
        if (verbose) {
            System.err.println("Start input parameter check...");
            System.err.println("- Unicode alternation: " + (subbook.unicode ? "YES" : "NO"));
        }
        if (compat && subbook.unicode) {
            System.out.println("*** unicode alternation required in definition YAML, but the tool launched with " +
                    "compat mode.");
        }
        if (subbook.name.length() > MAXLEN_SUBNAME) {
            System.out.println("*** Definition of dictionary name length is exceeded the limit.");
        }
        if (subbook.hasNarrow()) {
            if (subbook.isEncoding("JISX0208")) {
                for (int key: subbook.narrow.keySet()) {
                    if (key < subbook.narrow.getStart()
                            || subbook.narrow.getEnd() < key
                            || (key & 0xff) < 0x21
                            || 0x7e < (key & 0xff)) {
                        System.out.println("*** narrow: key is out of range: " + key);
                    }
                }
            } else {
                for (int key: subbook.narrow.keySet()) {
                    if (key < subbook.narrow.getStart()
                            || subbook.narrow.getEnd() < key
                            || (key & 0xff) < 0x01
                            || 0xfe < (key & 0xff)) {
                        System.out.println("*** narrow: key is out of range: " + key);
                    }
                }
            }
        }
        if (subbook.hasWide()) {
            if (subbook.isEncoding("JISX0208")) {
                for (int key: subbook.wide.keySet()) {
                    if (key < subbook.wide.getStart() || subbook.wide.getEnd() < key
                            || (key & 0xff) < 0x21 || 0x7f < (key & 0xff)) {
                        System.out.println("*** wide: key is out of range: " + key);
                    }
                }
            } else {
                for (int key: subbook.wide.keySet()) {
                    if (key < subbook.wide.getStart()
                            || subbook.wide.getEnd() < key
                            || (key & 0xff) < 0x01
                            || 0xfe < (key & 0xff)) {
                        System.out.println("*** narrow: key is out of range: " + key);
                    }
                }
            }
        }
        if (verbose) {
            System.err.println("Input parameter check done.");
        }
    }
}
