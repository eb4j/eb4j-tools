package io.github.eb4j.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.zip.Adler32;
import java.util.zip.Deflater;


import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.ExtFont;
import io.github.eb4j.EBException;
import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.EBFormat;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.io.EBZipInputStream;
import io.github.eb4j.io.EBZipConstants;

import picocli.CommandLine;

/**
 * 書籍の圧縮/伸張プログラム。
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "ebzip", mixinStandardHelpOptions = true,
description = "Compress/Decompress EPWING data (.ebz)",
version = {"EBZip",
        "Version " + EBZip.VERSION,
        "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.",
        "Copyright (c) 2016,2021 Hiroshi Miura"},
exitCodeOnInvalidInput = 1,
exitCodeOnExecutionException = 2)
public final class EBZip implements Callable<Integer> {
    /**
     * プロブラム名
     */
    private static final String PROGRAM = EBZip.class.getName();

    static final String VERSION = "2.0.0";

    /**
     * デフォルト読み込みディレクトリ
     */
    private static final String DEFAULT_BOOK_DIR = ".";

    /**
     * デフォルト出力ディレクトリ
     */
    private static final String DEFAULT_OUTPUT_DIR = ".";

    /**
     * 圧縮率表示用フォーマッタ
     */
    private static final DecimalFormat FMT = new DecimalFormat("##0.0'%'");

    /**
     * 圧縮モード
     */
    private static final int ACTION_ZIP = 0;
    /**
     * 解凍モード
     */
    private static final int ACTION_UNZIP = 1;
    /**
     * 情報モード
     */
    private static final int ACTION_INFO = 2;

    /**
     * 上書き方法
     */
    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite output files")
    boolean overwrite = false;

    /**
     * オリジナルファイル保持フラグ
     */
    @CommandLine.Option(names = {"-k", "--keep"}, description = "keep (don't delete) original files")
    boolean keep = false;

    /**
     * EBZIP圧縮レベル
     */
    @CommandLine.Option(
            names = {"-l", "--level"},
            description = "compression level: 0.." + EBZipConstants.EBZIP_MAX_LEVEL,
            defaultValue = "0") //EBZipConstants.EBZIP_DEFAULT_LEVEL
            int level;

    /**
     * 出力メッセージ抑止フラグ
     */
    @CommandLine.Option(names = {"-q", "--quiet"}, description = "suppress all warings")
    boolean quiet = false;

    /**
     * Types for skip option flags.
     */
    enum SkipTypes {
        /**
         * 外字無視フラグ
         */
        FONT,
        /**
         * 音声無視フラグ
         */
        SOUND,
        /**
         * 画像無視フラグ
         */
        GRAPHIC,
        /**
         * 動画無視フラグ
         */
        MOVIE,
    }

    /**
     * Parse skip parameter.
     */
    class SkipTypeConverter implements CommandLine.ITypeConverter<SkipTypes> {
        public SkipTypes convert(final String value) {
            switch (value) {
                case "font":
                    return SkipTypes.FONT;
                case "sound":
                    return SkipTypes.SOUND;
                case "graphic":
                    return SkipTypes.GRAPHIC;
                case "movie":
                    return SkipTypes.MOVIE;
                default:
                    return null;
            }
        }
    }

    @CommandLine.Option(
            names = {"-s", "--skip-content"},
            split = ",",
            converter = SkipTypeConverter.class,
            description = "skip content; font, graphic, sound or movie")
    List<SkipTypes> skips;

    /**
     * 出力先ディレクトリ
     */
    @CommandLine.Option(names = {"-o", "--output-directory"}, description = "output files under DIRECTORY")
    String outDir = DEFAULT_OUTPUT_DIR;

    @CommandLine.Option(names = {"-i", "--information"}, description = "list information of compressed files")
    boolean actionInfo = false;

    @CommandLine.Option(names = {"-z", "--compress"}, description = "compress files")
    boolean actionZip = false;

    @CommandLine.Option(names = {"-u", "--uncompress"}, description = "uncompress files")
    boolean actionUnzip = false;

    /**
     * 書籍読み込みディレクトリ
     */
    @CommandLine.Parameters(description = "book path", defaultValue = DEFAULT_BOOK_DIR)
    File bookDir;

    /**
     * 対象副本のリスト
     */
    @CommandLine.Option(names = {"-S", "--subbook"}, description = "target subbook")
    String[] subbooks;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        if (skips == null) {
            skips = Collections.emptyList();
        }
        exec();
        return 0;
    }

    /**
     * Main function for EBZip command.
     * @param args
     * @return
     */
    public static void main(final String... args) {
        System.exit(new CommandLine(new EBZip()).execute(args));
    }

    /**
     * コマンドを実行します。
     *
     * @throws EBException 書籍の初期化中に例外が発生した場合
     */
    void exec() throws EBException {
        int action = ACTION_ZIP;
        if (actionInfo) {
            action = ACTION_INFO;
        }
        if (actionUnzip) {
            action = ACTION_UNZIP;
        }
        if (actionZip) {
            action = ACTION_ZIP;
        }
        Book book = new Book(bookDir);
        File root = bookDir;
        SubBook[] sub = book.getSubBooks();
        EBFile file;
        for (SubBook aSub : sub) {
            if (subbooks != null) {
                boolean show = false;
                String dir = aSub.getName().toLowerCase(Locale.ENGLISH);
                for (String subbook : subbooks) {
                    if (subbook.equals(dir)) {
                        show = true;
                        break;
                    }
                }
                if (!show) {
                    continue;
                }
            }
            if (book.getBookType() == Book.DISC_EB) {
                file = aSub.getTextFile();
                _act(action, file);
                if (action == ACTION_UNZIP
                        && file.getFormat() == EBFormat.FORMAT_SEBXA) {
                    // SEBXA圧縮フラグの削除
                    _fixSEBXA(_getOutFile(file, ".org"));
                }
            } else {
                // 本文ファイル
                file = aSub.getTextFile();
                _act(action, file);
                if (file.getName().equalsIgnoreCase("honmon2")) {
                    // 音声、画像ファイル
                    if (skips.contains(SkipTypes.SOUND) && !file.getName().equalsIgnoreCase("honmon2")) {
                        file = aSub.getSoundFile();
                        if (file != null) {
                            _act(action, file);
                        }
                    }
                    if (!skips.contains(SkipTypes.GRAPHIC)) {
                        file = aSub.getGraphicFile();
                        if (file != null && !file.getName().equalsIgnoreCase("honmon2")) {
                            if (action == ACTION_ZIP) {
                                _copy(file);
                            } else {
                                _act(action, file);
                            }
                        }
                    }
                }
                // 外字ファイル
                if (!skips.contains(SkipTypes.FONT)) {
                    for (int j = 0; j < 4; j++) {
                        ExtFont font = aSub.getFont(j);
                        if (font.hasWideFont()) {
                            file = font.getWideFontFile();
                            _act(action, file);
                        }
                        if (font.hasNarrowFont()) {
                            file = font.getNarrowFontFile();
                            _act(action, file);
                        }
                    }
                }
                // 動画ファイル
                if (!skips.contains(SkipTypes.MOVIE) && action != ACTION_INFO) {
                    File[] files = aSub.getMovieFileList();
                    if (files != null) {
                        for (File file1 : files) {
                            _copy(_getOutFile(file1, null), file1);
                        }
                    }
                }
            }
        }
        if (book.getBookType() == Book.DISC_EB) {
            try {
                file = new EBFile(root, "language", EBFormat.FORMAT_PLAIN);
                _act(action, file);
            } catch (EBException ignored) {
            }
            file = new EBFile(root, "catalog", EBFormat.FORMAT_PLAIN);
            if (action == ACTION_ZIP) {
                _copy(file);
            } else {
                _act(action, file);
            }
        } else {
            file = new EBFile(root, "catalogs", EBFormat.FORMAT_PLAIN);
            if (action == ACTION_ZIP) {
                _copy(file);
            } else {
                _act(action, file);
            }
        }
    }

    /**
     * 指定されたアクションを実行します。
     *
     * @param action アクション
     * @param file   ファイル
     */
    private void _act(final int action, final EBFile file) {
        switch (action) {
            case ACTION_ZIP:
                _zip(file);
                break;
            case ACTION_UNZIP:
                _unzip(file);
                break;
            case ACTION_INFO:
                _info(file);
                break;
            default:
        }
    }

    /**
     * 指定されたファイルを圧縮します。
     * Original File:
     * +-----------------+-----------------+-...-+-------+
     * |     slice 1     |     slice 2     |     |slice N| [EOF]
     * |                 |                 |     |       |
     * +-----------------+-----------------+-...-+-------+
     * slice size        slice size            odds
     * <-------------------- file size ------------------>
     * <p>
     * Compressed file:
     * +------+---------+...+---------+---------+----------+...+-
     * |Header|index for|   |index for|index for|compressed|   |
     * |      | slice 1 |   | slice N |   EOF   |  slice 1 |   |
     * +------+---------+...+---------+---------+----------+...+-
     * index         index     index
     * size          size      size
     * <---------  index length --------->
     * <p>
     * total_slices = N = (file_size + slice_size - 1) / slice_size
     * index_length = (N + 1) * index_size
     *
     * @param file ファイル
     */
    private void _zip(final EBFile file) throws SecurityException {
        _mkdir(file);

        File f = _getOutFile(file, ".ebz");
        if (!quiet) {
            // ファイル名の出力
            System.out.println("==> compress " + file.getPath() + " <==");
            System.out.println("output to " + f.getPath());
        }

        if (f.equals(file.getFile())) {
            if (!quiet) {
                System.out.println("the input and output files are the same, skipped.");
                System.out.println("");
            }
            return;
        }

        if (f.exists() && !overwrite) {
            return;
        }

        FileChannel channel = null;
        try (BookInputStream bis = file.getInputStream()) {
            int sliceSize = BookInputStream.PAGE_SIZE << level;
            long fileSize = bis.getFileSize();
            int indexSize = calcIndexSize(fileSize);
            int totalSlice = (int) ((fileSize + sliceSize - 1) / sliceSize);
            long indexLength = (long) (totalSlice + 1) * indexSize;
            byte[] in = new byte[sliceSize];
            byte[] out = new byte[sliceSize + 1024];
            long slicePos = EBZipConstants.EBZIP_HEADER_SIZE + indexLength;

            // ヘッダとインデックスのダミーデータを書き込む
            channel = new FileOutputStream(f).getChannel();
            fillZeroHeader(channel, sliceSize, slicePos);

            long inTotalLength = 0;
            long outTotalLength = 0;
            int interval = 1024 >>> level;
            if (((totalSlice + 999) / 1000) > interval) {
                interval = (totalSlice + 999) / 1000;
            }
            Adler32 crc32 = new Adler32();
            Deflater def = new Deflater(Deflater.BEST_COMPRESSION);
            for (int i = 0; i < totalSlice; i++) {
                int inLen = readSliceData(bis, in, sliceSize, inTotalLength, crc32, file);
                int outLen = compressAndOutputSliceData(in, inLen, def, channel, sliceSize);
                long nextPos = slicePos + outLen;
                if (indexSize >= 2 && indexSize <= 5) {
                    toBigEndian(out, 0, slicePos, indexSize);
                    toBigEndian(out, indexSize, nextPos, indexSize);
                }
                // インデックス情報の書き込み
                assert channel != null;
                channel.position(EBZipConstants.EBZIP_HEADER_SIZE + (long) i * indexSize);
                channel.write(ByteBuffer.wrap(out, 0, indexSize * 2));

                inTotalLength += inLen;
                outTotalLength += outLen + indexSize;
                slicePos = nextPos;

                // 進捗の表示
                if (!quiet && (i % interval) + 1 == interval) {
                    double rate = (double) (i + 1) / (double) totalSlice * 100.0;
                    System.out.println(FMT.format(rate) + " done ("
                            + inTotalLength + " / "
                            + fileSize + " bytes)");
                }
            }
            def.end();
            outputHeader(channel, fileSize, crc32);
            long inRealFileSize = bis.getRealFileSize();
            // 結果の表示
            outTotalLength += EBZipConstants.EBZIP_HEADER_SIZE + indexSize;
            printZipResult(inTotalLength, outTotalLength, fileSize, inRealFileSize);
        } catch (EBException | IOException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
        }
        // オリジナルファイルの削除
        if (!keep) {
            _delete(file.getFile());
        }
        if (!quiet) {
            System.out.println("");
        }
    }

    private void toBigEndian(final byte[] out, final int off, final long val, final int len) {
        int shift = 0;
        for (int i = len; i > 0; i--) {
            out[off + i - 1] = (byte) ((val >>> shift) & 0xff);
            shift += 8;
        }
    }

    private int calcIndexSize(final long fileSize) {
        if (fileSize < (1L << 16)) {
            return 2;
        } else if (fileSize < (1L << 24)) {
            return 3;
        } else if (fileSize < (1L << 32)) {
            return 4;
        }
        return 5;
    }

    // ヘッダとインデックスのダミーデータを書き込む
    private void fillZeroHeader(final FileChannel channel, final int sliceSize, final long off)
            throws IOException {
        byte[] out = new byte[sliceSize];
        Arrays.fill(out, 0, out.length, (byte) 0);
        long i;
        for (i = off; i >= sliceSize; i = i - sliceSize) {
            channel.write(ByteBuffer.wrap(out, 0, sliceSize));
        }
        if (i > 0) {
            channel.write(ByteBuffer.wrap(out, 0, (int) i));
        }
    }

    private int readSliceData(final BookInputStream bis, final byte[] in, final int sliceSize,
                              final long inTotalLength, final Adler32 crc32, final EBFile file)
            throws EBException {
        bis.seek(inTotalLength);
        int inLen = bis.read(in, 0, in.length);
        if (inLen < 0) {
            System.err.println(PROGRAM
                    + ": failed to read the file ("
                    + file.getPath() + ")");
            return -1;
        } else if (inLen == 0) {
            System.err.println(PROGRAM + ": unexpected EOF ("
                    + file.getPath() + ")");
            return -1;
        } else if (inLen != in.length) {
            long fileSize = bis.getFileSize();
            if (inTotalLength + inLen != fileSize) {
                System.err.println(PROGRAM + ": unexpected EOF ("
                        + file.getPath() + ")");
                return -1;
            }
        }
        crc32.update(in, 0, inLen);

        // 最終スライスでスライスサイズに満たない場合は0で埋める
        if (inLen < sliceSize) {
            Arrays.fill(in, inLen, in.length, (byte) 0);
            inLen = sliceSize;
        }

        return inLen;
    }

    private int compressAndOutputSliceData(final byte[] in, final int inLen, final Deflater def,
                                           final FileChannel channel, final int sliceSize)
            throws IOException {
        byte[] out = new byte[sliceSize];
        def.reset();
        def.setInput(in, 0, inLen);
        def.finish();
        int outLen = 0;
        while (!def.needsInput()) {
            int n = def.deflate(out, outLen, out.length - outLen, Deflater.SYNC_FLUSH);
            outLen += n;
        }
        // 圧縮スライスがオリジナルより大きい場合はオリジナルを書き込む
        if (outLen >= sliceSize) {
            System.arraycopy(in, 0, out, 0, sliceSize);
            outLen = sliceSize;
        }
        // 圧縮したスライスデータの書き込み
        // ファイルの末尾に追加
        channel.position(channel.size());
        channel.write(ByteBuffer.wrap(out, 0, outLen));
        return outLen;
    }

    private void outputHeader(final FileChannel channel, final long fileSize, final Adler32 crc32)
            throws IOException {
        byte[] out = new byte[EBZipConstants.EBZIP_HEADER_SIZE];
        // ヘッダ情報の作成
        out[0] = (byte) 'E';
        out[1] = (byte) 'B';
        out[2] = (byte) 'Z';
        out[3] = (byte) 'i';
        out[4] = (byte) 'p';
        if (fileSize < (1L << 32)) {
            out[5] = (byte) ((1 << 4) | (level & 0x0f));
        } else {
            out[5] = (byte) ((2 << 4) | (level & 0x0f));
        }
        out[6] = (byte) 0;
        out[7] = (byte) 0;
        out[8] = (byte) 0;
        out[9] = (byte) ((fileSize >>> 32) & 0xff);
        out[10] = (byte) ((fileSize >>> 24) & 0xff);
        out[11] = (byte) ((fileSize >>> 16) & 0xff);
        out[12] = (byte) ((fileSize >>> 8) & 0xff);
        out[13] = (byte) (fileSize & 0xff);
        long crc = crc32.getValue();
        out[14] = (byte) ((crc >>> 24) & 0xff);
        out[15] = (byte) ((crc >>> 16) & 0xff);
        out[16] = (byte) ((crc >>> 8) & 0xff);
        out[17] = (byte) (crc & 0xff);
        long mtime = System.currentTimeMillis();
        out[18] = (byte) ((mtime >>> 24) & 0xff);
        out[19] = (byte) ((mtime >>> 16) & 0xff);
        out[20] = (byte) ((mtime >>> 8) & 0xff);
        out[21] = (byte) (mtime & 0xff);

        // ヘッダ情報の書き込み
        channel.position(0);
        channel.write(ByteBuffer.wrap(out, 0, EBZipConstants.EBZIP_HEADER_SIZE));
    }

    private void printZipResult(final long inTotalLength, final long outTotalLength,
                                final long fileSize, final long inRealFileSize) {
        if (!quiet) {
            System.out.println("completed (" + fileSize
                    + " / " + fileSize + " bytes)");
            if (inTotalLength != 0) {
                double rate = (double) (outTotalLength) / (double) inRealFileSize * 100.0;
                System.out.println(inRealFileSize + " -> "
                        + outTotalLength + " bytes ("
                        + FMT.format(rate) + ")");
            }
        }
    }

    /**
     * 指定されたファイルを解凍します。
     *
     * @param file ファイル
     */
    private void _unzip(final EBFile file) {
        // 無圧縮ファイルはそのままコピーする
        if (file.getFormat() == EBFormat.FORMAT_PLAIN) {
            _copy(file);
            return;
        }

        _mkdir(file);

        String suffix = null;
        if (file.getFormat() != EBFormat.FORMAT_EBZIP) {
            suffix = ".org";
        }
        File f = _getOutFile(file, suffix);
        if (!quiet) {
            // ファイル名の出力
            System.out.println("==> uncompress " + file.getPath() + " <==");
            System.out.println("output to " + f.getPath());
        }

        if (f.equals(file.getFile())) {
            if (!quiet) {
                System.out.println("the input and output files are the same, skipped.");
                System.out.println("");
            }
            return;
        }
        if (f.exists() && !overwrite) {
            return;
        }

        BookInputStream bis = null;
        FileChannel channel = null;
        try {
            bis = file.getInputStream();
            byte[] b = new byte[bis.getSliceSize()];
            channel = new FileOutputStream(f).getChannel();
            long totalLength = 0;
            int totalSlice = (int) ((bis.getFileSize()
                    + bis.getSliceSize() - 1)
                    / bis.getSliceSize());
            int interval = 1024;
            if (((totalSlice + 999) / 1000) > interval) {
                interval = (totalSlice + 999) / 1000;
            }
            Adler32 crc32 = new Adler32();
            for (int i = 0; i < totalSlice; i++) {
                // データの読み込み
                bis.seek(totalLength);
                int n = bis.read(b, 0, b.length);
                if (n < 0) {
                    System.err.println(PROGRAM
                            + ": failed to read the file ("
                            + f.getPath() + ")");
                    return;
                } else if (n == 0) {
                    System.err.println(PROGRAM + ": unexpected EOF ("
                            + f.getPath() + ")");
                    return;
                } else if (n != b.length
                        && totalLength + n != bis.getFileSize()) {
                    System.err.println(PROGRAM + ": unexpected EOF ("
                            + f.getPath() + ")");
                    return;
                }
                // CRCの更新
                if (bis instanceof EBZipInputStream) {
                    crc32.update(b, 0, n);
                }
                // データの書き込み
                channel.write(ByteBuffer.wrap(b, 0, n));
                totalLength += n;

                // 進捗の表示
                if (!quiet && (i % interval) + 1 == interval) {
                    double rate = (double) (i + 1) / (double) totalSlice * 100.0;
                    System.out.println(FMT.format(rate) + " done ("
                            + totalLength + " / "
                            + bis.getFileSize() + " bytes)");
                }
            }
            // 結果の表示
            if (!quiet) {
                System.out.println("completed (" + bis.getFileSize()
                        + " / " + bis.getFileSize() + " bytes)");
                System.out.println(bis.getRealFileSize() + " -> "
                        + totalLength + " bytes");
            }

            // CRCの確認
            if (bis instanceof EBZipInputStream) {
                if (crc32.getValue() != ((EBZipInputStream) bis).getCRC()) {
                    System.err.println(PROGRAM + ": CRC error (" + f.getPath() + ")");
                    return;
                }
            }
        } catch (EBException | IOException | SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
        }
        // オリジナルファイルの削除
        if (!keep) {
            _delete(file.getFile());
        }
        if (!quiet) {
            System.out.println("");
        }
    }

    /**
     * 指定されたファイルの情報を出力します。
     *
     * @param file ファイル
     */
    private void _info(final EBFile file) {
        // ファイル名の出力
        System.out.println("==> " + file.getPath() + " <==");
        try (BookInputStream bis = file.getInputStream()) {
            // ファイルサイズ、圧縮率の出力
            StringBuilder buf = new StringBuilder();
            String text = null;
            switch (file.getFormat()) {
                case FORMAT_PLAIN:
                    buf.append(bis.getFileSize());
                    buf.append(" bytes (not compressed)");
                    break;
                case FORMAT_EBZIP:
                    text = "ebzip level " + ((EBZipInputStream) bis).getLevel() + " compression)";
                    break;
                case FORMAT_SEBXA:
                    text = "S-EBXA compression)";
                    break;
                default:
                    text = "EPWING compression)";
            }
            if (text != null) {
                long size = bis.getFileSize();
                long real = bis.getRealFileSize();
                buf.append(size).append(" -> ");
                buf.append(real).append(" bytes (");
                if (size == 0) {
                    System.out.print("empty original file, ");
                } else {
                    double rate = (double) real / (double) size * 100.0;
                    buf.append(FMT.format(rate));
                    buf.append(", ");
                }
                buf.append(text);
            }
            System.out.println(buf.toString());
            System.out.println("");
        } catch (EBException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            System.out.println("");
        }
    }

    /**
     * 指定されたファイルからS-EBXA圧縮情報を取り除きます。
     *
     * @param file ファイル
     */
    private void _fixSEBXA(final File file) {
        if (!quiet) {
            System.out.println("==> fix " + file.getPath() + " <==");
        }

        FileChannel channel = null;
        boolean err = false;
        try {
            channel = new RandomAccessFile(file, "rw").getChannel();

            // インデックスページをメモリにマッピング
            MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE,
                    0, BookInputStream.PAGE_SIZE);

            // 0x12/0x22のインデックスの取り除き
            int indexCount = buf.get(1) & 0xff;
            int removeCount = 0;
            int inOff = 16;
            int outOff = 16;
            for (int i = 0; i < indexCount; i++) {
                int index = buf.get(inOff) & 0xff;
                if (index == 0x21 || index == 0x22) {
                    removeCount++;
                } else {
                    if (inOff != outOff) {
                        for (int j = 0; j < 16; j++) {
                            buf.put(outOff + j, buf.get(inOff + j));
                        }
                    }
                    outOff += 16;
                }
                inOff += 16;
            }
            for (int i = 0; i < removeCount; i++) {
                for (int j = 0; j < 16; j++) {
                    buf.put(outOff + j, (byte) 0);
                }
                outOff += 16;
            }
            buf.force();
        } catch (IOException | SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            err = true;
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (!quiet) {
            if (!err) {
                System.out.println("complated");
            }
            System.out.println("");
        }
    }

    /**
     * 指定ファイルに対する出力ファイルを返します。
     *
     * @param file   ファイル
     * @param suffix 拡張子
     * @return 出力ファイル
     */
    private File _getOutFile(final EBFile file, final String suffix) {
        return _getOutFile(file.getFile(), suffix);
    }

    /**
     * 指定ファイルに対する出力ファイルを返します。
     *
     * @param file   ファイル
     * @param suffix 拡張子
     * @return 出力ファイル
     */
    private File _getOutFile(final File file, final String suffix) {
        String bookDirFile;
        String inFile;
        try {
            bookDirFile = bookDir.getCanonicalPath();
            inFile = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("can't get canonical path", e);
        }

        String fname = inFile.substring(bookDirFile.length());
        if (fname.length() > 4) {
            String s = fname.substring(fname.length() - 4);
            if (s.equalsIgnoreCase(".ebz")) {
                fname = fname.substring(0, fname.length() - 4);
            } else if (s.equalsIgnoreCase(".org")) {
                fname = fname.substring(0, fname.length() - 4);
            }
        }
        if (suffix != null) {
            fname += suffix;
        }
        return new File(outDir, fname);
    }

    /**
     * 指定ファイルの出力先ディレクトリを作成します。
     *
     * @param file ファイル
     */
    private void _mkdir(final EBFile file) {
        _mkdir(_getOutFile(file, null));
    }

    /**
     * 指定ファイルの出力先ディレクトリを作成します。
     *
     * @param file ファイル
     */
    private void _mkdir(final File file) {
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            try {
                if (!dir.mkdirs()) {
                    throw new RuntimeException("can't create directory ("
                            + dir.getAbsolutePath() + ")");
                }
            } catch (SecurityException e) {
                throw new RuntimeException("can't create directory (" + e.getMessage() + ")", e);
            }
        }
    }

    /**
     * 指定ファイルを出力先にコピーします。
     *
     * @param file ファイル
     */
    private void _copy(final EBFile file) {
        _copy(file.getFile(), _getOutFile(file, null));
    }

    /**
     * 指定入力ファイルを指定出力ファイルにコピーします。
     *
     * @param file1 入力ファイル
     * @param file2 出力ファイル
     */
    private void _copy(final File file1, final File file2) {
        _mkdir(file2);
        if (!quiet) {
            // ファイル名の出力
            System.out.println("==> copy " + file1.getPath() + " <==");
            System.out.println("output to " + file2.getPath());
        }

        if (file1.equals(file2)) {
            if (!quiet) {
                System.out.println("the input and output files are the same, skipped.");
                System.out.println("");
            }
            return;
        }

        if (file2.exists() && !overwrite) {
            return;
        }

        try (FileChannel in = new FileInputStream(file1).getChannel();
             FileChannel out = new FileOutputStream(file2).getChannel()) {
            in.transferTo(0, (int) in.size(), out);
            // 結果の表示
            if (!quiet) {
                System.out.println("completed (" + in.size()
                        + " / " + out.size() + " bytes)");
            }
        } catch (IOException | SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        }
        // オリジナルファイルの削除
        if (!keep) {
            _delete(file1);
        }
        if (!quiet) {
            System.out.println("");
        }
    }

    /**
     * 指定ファイルを削除します。
     *
     * @param file ファイル
     */
    private void _delete(final File file) {
        try {
            if (!file.delete()) {
                System.err.println(PROGRAM
                        + ": failed to delete the file ("
                        + file.getPath() + ")");
            }
        } catch (SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        }
    }

}

// end of EBZip.java
