package io.github.eb4j.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import io.github.eb4j.tool.appendix.Appendix;
import io.github.eb4j.tool.appendix.SubAppendix;

/**
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "appendix", mixinStandardHelpOptions = true, description = "Compile EPWING furoku data")
public final class EBAppendix implements Callable<Integer>  {

    private static final String DEFAULT_APPENDIX_DIR = ".";
    /**
     * maxmum length of alternation text.
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
        try {
            appendix = getAppendix(path);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return generate();
    }

    /**
     * Main function.
     * @param args
     */
    public static void main(final String... args) {
        System.exit(new CommandLine(new EBAppendix()).execute(args));
    }

    /**
     * generate appendix files.
     * @return exit vale.
     */
    protected int generate() throws Exception {
        if (catalog) {
            File outFile;
            if (appendix.type.equals("EB")) {
                outFile = new File(outDir, "catalog");
            } else {
                outFile = new File(outDir, "catalogs");
            }
            createCatalogFile(outFile);
        }
        for (SubAppendix subbook : appendix.subbook) {
            File outFile;
            if (appendix.type.equals("EB")) {
                outFile = new File(new File(outDir, subbook.name), "appendix");
            } else {
                File outTarget = new File(new File(outDir, subbook.name), "data");
                boolean ignore = outTarget.mkdirs();
                outFile = new File(outTarget, "furoku");
            }
            if (verbose) {
                System.err.println("Start parse input files.");
                System.err.println("output file:" + outFile.getPath());
            }
            appendixCheck(subbook);
            try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
                appendixWriter(raf, subbook);
            }
        }
        return 0;
    }

    private void createCatalogFile(final File outFile) {
        // FIXME: implement me.
        return;
    }

    private void appendixWriter(final RandomAccessFile raf, final SubAppendix subbook) throws Exception {
        int narrowPage = 0;
        int widePage = 0;
        int stopPage = 0;
        // fill header with null bytes
        for (int i = 0; i < SIZE_PAGE; i++) {
            raf.write('\0');
        }
        // write narrow def
        if (subbook.hasNarrow()) {
            narrowPage = (int)(1 + raf.getFilePointer() / SIZE_PAGE);
            int i = subbook.narrow.getStart();
            while (i <= subbook.narrow.getEnd()) {
                if (subbook.narrow.containsKey(i)) {
                    String altString = subbook.narrow.getAlt(i);
                    byte[] altByte = altString.getBytes("EUC-JP");
                    raf.write(altByte);
                    for (int j = 0; j < 1 + MAXLEN_ALTERNATION - altByte.length; j++) {
                        raf.write('\0');
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
            while (i <= subbook.wide.getEnd()) {
                if (subbook.wide.containsKey(i)) {
                    String altString = subbook.wide.getAlt(i);
                    byte[] altByte = altString.getBytes("EUC-JP");
                    raf.write(altByte);
                    for (int j = 0; j < 1 + MAXLEN_ALTERNATION - altByte.length; j++) {
                        raf.write('\0');
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
            raf.write('\0');
            raf.write('\1');
            raf.write(subbook.getStopCodeBytes());
            int padLen = (int) (SIZE_PAGE - raf.getFilePointer() % SIZE_PAGE);
            for (int j = 0; j < padLen; j++) {
                raf.write('\0');
            }
        }
        // output index page
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
    }

    private void appendixCheck(final SubAppendix subbook) {
        if (subbook.hasNarrow()) {
            if (subbook.isEncoding("JISX0208")) {
                for (String keyString: subbook.narrow.keySet()) {
                    int key = Integer.parseInt(keyString.substring(2), 16);
                    if (key < subbook.narrow.getStart()
                            || subbook.narrow.getEnd() < key
                            || (key & 0xff) < 0x21
                            || 0x7e < (key & 0xff)) {
                        System.out.println("narrow: key is out of range: " + key);
                    }
                }
            } else {
                for (String keyString: subbook.narrow.keySet()) {
                    int key = Integer.parseInt(keyString.substring(2), 16);
                    if (key < subbook.narrow.getStart()
                            || subbook.narrow.getEnd() < key
                            || (key & 0xff) < 0x01
                            || 0xfe < (key & 0xff)) {
                        System.out.println("narrow: key is out of range: " + key);
                    }
                }
            }
        }
        if (subbook.hasWide()) {
            if (subbook.isEncoding("JISX0208")) {
                for (String keyString: subbook.wide.keySet()) {
                    int key = Integer.parseInt(keyString.substring(2), 16);
                    if (key < subbook.wide.getStart() || subbook.wide.getEnd() < key
                            || (key & 0xff) < 0x21 || 0x7f < (key & 0xff)) {
                        System.out.println("wide: key is out of range: " + key);
                    }
                }
            } else {
                for (String keyString: subbook.wide.keySet()) {
                    int key = Integer.parseInt(keyString.substring(2), 16);
                    if (key < subbook.wide.getStart()
                            || subbook.wide.getEnd() < key
                            || (key & 0xff) < 0x01
                            || 0xfe < (key & 0xff)) {
                        System.out.println("narrow: key is out of range: " + key);
                    }
                }
            }
        }
    }
}
