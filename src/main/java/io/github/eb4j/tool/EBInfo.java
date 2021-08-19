package io.github.eb4j.tool;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.ExtFont;
import io.github.eb4j.EBException;
import io.github.eb4j.util.HexUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * 書籍情報表示プログラム。
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "ebinfo", mixinStandardHelpOptions = true,
description = "Show EPWING data information",
version = {"EBInfo",
        "Version " + EBDump.VERSION,
        "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.",
        "Copyright (c) 2016,2021 Hiroshi Miura"})
public final class EBInfo implements Callable<Integer> {

    /** デフォルト読み込みディレクトリ */
    private static final String DEFAULT_BOOK_DIR = ".";

    @picocli.CommandLine.Option(names = {"-m", "--multi-search"},
            description = "also output multi-search information")
    boolean multi = false;

    @picocli.CommandLine.Parameters(description = "book path", defaultValue = DEFAULT_BOOK_DIR)
    File path;


    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        show();
        return 0;
    }

    /**
     * Main function for EBInfo commmand.
     * @param args
     */
    public static void main(final String... args) {
        System.exit(new CommandLine(new EBInfo()).execute(args));
    }


    /**
     * 書籍の情報を出力します。
     */
    protected void show() throws EBException {
        String text;
        Book book = new Book(path);
        // 書籍の種類
        System.out.print("disc type: ");
        if (book.getBookType() == Book.DISC_EB) {
            text = "EB/EBG/EBXA/EBXA-C/S-EBXA";
        } else if (book.getBookType() == Book.DISC_EPWING) {
            text = "EPWING V" + book.getVersion();
        } else {
            text = "unknown";
        }
        System.out.println(text);

        // 書籍の文字セット
        System.out.print("character code: ");
        switch (book.getCharCode()) {
            case Book.CHARCODE_ISO8859_1:
                text = "ISO 8859-1";
                break;
            case Book.CHARCODE_JISX0208:
                text = "JIS X 0208";
                break;
            case Book.CHARCODE_JISX0208_GB2312:
                text = "JIS X 0208 + GB 2312";
                break;
            default:
                text = "unknown";
                break;
        }
        System.out.println(text);

        // 書籍に含まれる副本数
        System.out.print("the number of subbooks: ");
        System.out.println(book.getSubBookCount());
        System.out.println("");

        // 各副本の情報
        SubBook[] subs = book.getSubBooks();
        for (int i=0; i<subs.length; i++) {
            System.out.println("subbook " + (i+1) + ":");

            // 副本のタイトル
            System.out.println("  title: " + subs[i].getTitle());

            // 副本のディレクトリ
            System.out.println("  directory: " + subs[i].getName());

            // 対応している検索方式
            System.out.print("  search methods:");
            if (subs[i].hasWordSearch()) {
                System.out.print(" word");
            }
            if (subs[i].hasEndwordSearch()) {
                System.out.print(" endword");
            }
            if (subs[i].hasExactwordSearch()) {
                System.out.print(" exactword");
            }
            if (subs[i].hasKeywordSearch()) {
                System.out.print(" keyword");
            }
            if (subs[i].hasCrossSearch()) {
                System.out.print(" cross");
            }
            if (subs[i].hasMultiSearch()) {
                System.out.print(" multi");
            }
            if (subs[i].hasMenu()) {
                System.out.print(" menu");
            }
            if (subs[i].hasImageMenu()) {
                System.out.print(" image-menu");
            }
            if (subs[i].hasCopyright()) {
                System.out.print(" copyright");
            }
            System.out.println("");

            // 外字のサイズ
            System.out.print("  font sizes:");
            for (int j=0; j<4; j++) {
                ExtFont font = subs[i].getFont(j);
                if (font.hasFont()) {
                    System.out.print(" " + font.getFontHeight());
                }
            }
            System.out.println("");

            // 半角外字の文字コード範囲
            ExtFont font = subs[i].getFont();
            System.out.print("  narrow font characters:");
            if (font.hasNarrowFont()) {
                int code = font.getNarrowFontStart();
                String hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex + " -- ");
                code = font.getNarrowFontEnd();
                hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex);
            }
            System.out.println("");

            // 全角外字の文字コード範囲
            System.out.print("  wide font characters:");
            if (font.hasWideFont()) {
                int code = font.getWideFontStart();
                String hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex + " -- ");
                code = font.getWideFontEnd();
                hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex);
            }
            System.out.println("");

            if (multi) {
                showMulti(subs[i]);
            }
        }
    }

    /**
     * 複合検索についての情報を出力します。
     *
     * @param sub 副本
     */
    private void showMulti(final SubBook sub) {
        if (!sub.hasMultiSearch()) {
            return;
        }
        System.out.println("");
        int count = sub.getMultiCount();
        for (int i=0; i<count; i++) {
            System.out.println("  multi search " + (i + 1) + ":");
            int entry = sub.getMultiEntryCount(i);
            for (int j=0; j<entry; j++) {
                System.out.println("    label " + (j + 1) + ": "
                                   + sub.getMultiEntryLabel(i, j));
                String text = null;
                if (sub.hasMultiEntryCandidate(i, j)) {
                    text = "exist";
                } else {
                    text = "not-exist";
                }
                System.out.println("     candidates: " + text);
            }
        }
    }

}

// end of EBInfo.java
