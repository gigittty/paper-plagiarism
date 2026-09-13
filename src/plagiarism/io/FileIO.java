package plagiarism.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import plagiarism.exceptions.FileReadException;
import plagiarism.exceptions.FileWriteException;

/**
 * 文件读写工具：负责从指定路径读取原文/抄袭版文本，以及写出重复率答案。
 *
 * <p>本模块只读取/写入由命令行参数显式给出的三个路径，不访问任何其他文件，
 * 满足评测「禁止读写其他文件」的约束。
 */
public final class FileIO {

    /** 统一使用 UTF-8 编解码，避免中文乱码。 */
    private static final java.nio.charset.Charset CHARSET = StandardCharsets.UTF_8;

    private FileIO() {
    }

    /**
     * 以 UTF-8 读取文本文件全部内容。
     *
     * @param path 文件绝对路径
     * @return 文件内容
     * @throws FileReadException 路径为空、非法或读取失败时抛出
     */
    public static String readText(final String path) throws FileReadException {
        if (path == null || path.isBlank()) {
            throw new FileReadException("文件路径为空，无法读取");
        }
        final Path filePath;
        try {
            filePath = Paths.get(path);
        } catch (final InvalidPathException e) {
            throw new FileReadException("非法文件路径：" + path, e);
        }
        try {
            return Files.readString(filePath, CHARSET);
        } catch (final IOException e) {
            throw new FileReadException("读取文件失败：" + path + "（" + e.getMessage() + "）", e);
        } catch (final SecurityException e) {
            throw new FileReadException("没有读取该文件的权限：" + path, e);
        }
    }

    /**
     * 将答案（重复率字符串）以 UTF-8 写入指定文件，覆盖既有内容。
     *
     * @param path    答案文件绝对路径
     * @param content 待写入的内容
     * @throws FileWriteException 路径为空或写入失败时抛出
     */
    public static void writeAnswer(final String path, final String content) throws FileWriteException {
        if (path == null || path.isBlank()) {
            throw new FileWriteException("答案文件路径为空，无法写入");
        }
        final Path filePath;
        try {
            filePath = Paths.get(path);
        } catch (final InvalidPathException e) {
            throw new FileWriteException("非法答案文件路径：" + path, e);
        }
        try {
            Files.writeString(
                    filePath,
                    content,
                    CHARSET,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            throw new FileWriteException("写入答案失败：" + path + "（" + e.getMessage() + "）", e);
        } catch (final SecurityException e) {
            throw new FileWriteException("没有写入该文件的权限：" + path, e);
        }
    }
}
