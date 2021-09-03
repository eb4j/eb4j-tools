package io.github.eb4j.tool;

import picocli.CommandLine;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.util.HexUtil;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * 書籍データダンププログラム。
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "dump", mixinStandardHelpOptions = true, description = "Dump EPWING ebook data")
public class EBDump implements Callable<Integer> {

    /**
     * デフォルト読み込みディレクトリ
     */
    private static final String DEFAULT_BOOK_DIR = ".";


    @CommandLine.Option(names = {"-s", "--subbook"}, description = "subbook index number", defaultValue = "0")
    int subindex;

    @CommandLine.Option(names = {"-p", "--page"}, description = "page number (HEX)",
            converter = HexNumberConverter.class, defaultValue = "1")
    Long page;

    @CommandLine.Option(names = {"-o", "--offset"}, description = "offset number (HEX)",
            converter = HexNumberConverter.class)
    int off = 0;

    @CommandLine.Option(names = {"-P", "--position"}, description = "position (HEX)",
            converter = HexNumberConverter.class)
    long pos = 0L;

    @CommandLine.Option(names = {"-d", "--dump"}, description = "dump size (HEX)",
            converter = HexNumberConverter.class)
    int size = 0;

    @CommandLine.Parameters(description = "book path", defaultValue = DEFAULT_BOOK_DIR)
    File path;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        if (pos < 0) {
            pos = BookInputStream.getPosition(page, off);
        }
        dump();
        return 0;
    }

    /**
     * Main function for EBDump command.
     * @param args command line argument
     */
    public static void main(final String... args) {
        System.exit(new CommandLine(new EBDump()).execute(args));
    }

    /**
     * Parser for hex values.
     */
    static class HexNumberConverter implements CommandLine.ITypeConverter<Long> {

        /**
         * Converts the specified command line argument value to some domain object.
         *
         * @param value the command line argument String value
         * @return the resulting domain object
         */
        @Override
        public Long convert(final String value) {
            return Long.parseLong(value, 16);
        }
    }

    /**
     * 書籍のデータを出力します。
     *
     * @throws EBException ファイル読み込み中にエラーが発生した場合
     */
    protected void dump() throws EBException {
        int dumpsize;
        Book book = new Book(path);

        SubBook sub = book.getSubBook(subindex);
        if (sub == null) {
            return;
        }
        if (size <= 0) {
            dumpsize = BookInputStream.PAGE_SIZE;
        } else {
            dumpsize = size;
        }

        BookInputStream bis = sub.getTextFile().getInputStream();
        byte[] b = new byte[dumpsize];
        try {
            bis.seek(pos);
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }

        long page2 = BookInputStream.getPage(pos);
        long pos2 = pos + dumpsize;
        long start = pos - (pos & 0x0f);
        long end = pos2;
        if ((end % 16) > 0) {
            end = end + (16 - (end % 16));
        }

        StringBuilder buf = new StringBuilder();
        int idx = 0;
        long i = 0L;
        int j, k;
        int offset, high, low;
        for (i = start; i < end; i += 16) {
            if (pos + idx >= page2 * BookInputStream.PAGE_SIZE) {
                page2++;
            }
            buf.append(_toHexString(page2)).append(':');
            offset = (int) (i % BookInputStream.PAGE_SIZE);
            buf.append(_toHexString(offset)).append(' ');
            k = 0;
            for (j = 0; j < 16; j++) {
                if (j == 8) {
                    buf.append(' ');
                }
                buf.append(' ');
                if (i + j >= pos && i + j < pos2) {
                    buf.append(_toHexString(b[idx + k]));
                    k++;
                } else {
                    buf.append("  ");
                }
            }
            buf.append("  ");
            for (j = 0; j < 16; j += 2) {
                if (i + j >= pos && i + j < pos2) {
                    high = b[idx++] & 0xff;
                    if (i + j + 1 >= pos && i + j + 1 < pos2) {
                        low = b[idx++] & 0xff;
                        if (high > 0x20 && high < 0x7f
                                && low > 0x20 && low < 0x7f) {
                            // JIS X 0208
                            buf.append(ByteUtil.jisx0208ToString(b, idx - 2, 2));
                        } else if (high > 0x20 && high < 0x7f
                                && low > 0xa0 && low < 0xff) {
                            // GB 2312
                            buf.append(ByteUtil.gb2312ToString(b, idx - 2, 2));
                        } else if (high > 0xa0 && high < 0xff
                                && low > 0x20 && low < 0x7f) {
                            // 外字
                            buf.append("??");
                        } else {
                            buf.append("..");
                        }
                    } else {
                        buf.append(". ");
                    }
                } else {
                    buf.append(' ');
                    if (i + j + 1 >= pos && i + j + 1 < pos2) {
                        idx++;
                        buf.append('.');
                    } else {
                        buf.append(' ');
                    }
                }
            }
            System.out.println(buf.toString());
            System.out.flush();
            buf.delete(0, buf.length());
        }
    }

    /**
     * 指定されたbyte値を16進数表現に変換ます。
     *
     * @param hex byte値
     * @return 変換後の文字列
     */
    private String _toHexString(final byte hex) {
        return HexUtil.toHexString(hex);
    }

    /**
     * 指定されたint値を16進数表現に変換ます。
     *
     * @param hex int値
     * @return 変換後の文字列
     */
    private String _toHexString(final int hex) {
        return HexUtil.toHexString(hex, 3);
    }

    /**
     * 指定されたlong値を16進数表現に変換ます。
     *
     * @param hex long値
     * @return 変換後の文字列
     */
    private String _toHexString(final long hex) {
        return HexUtil.toHexString(hex, 5);
    }
}

// end of EBDump.java
