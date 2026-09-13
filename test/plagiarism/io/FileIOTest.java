package plagiarism.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import plagiarism.exceptions.FileReadException;
import plagiarism.exceptions.FileWriteException;

/**
 * FileIO 单元测试：覆盖正常读写，以及文件缺失、路径为空、写入目录等异常分支。
 */
class FileIOTest {

    @Test
    void readText_normal(@TempDir final Path dir) throws Exception {
        final Path file = dir.resolve("a.txt");
        Files.writeString(file, "今天天气晴朗");
        final String content = FileIO.readText(file.toString());
        assertTrue(content.contains("天气"));
    }

    @Test
    void readText_missingFile_throwsFileReadException() {
        assertThrows(FileReadException.class,
                () -> FileIO.readText("not_exist_12345.txt"));
    }

    @Test
    void readText_nullPath_throwsFileReadException() {
        assertThrows(FileReadException.class, () -> FileIO.readText(null));
    }

    @Test
    void writeAnswer_normal(@TempDir final Path dir) throws Exception {
        final Path file = dir.resolve("ans.txt");
        FileIO.writeAnswer(file.toString(), "95.60");
        assertEquals("95.60", Files.readString(file));
    }

    @Test
    void writeAnswer_toDirectory_throwsFileWriteException(@TempDir final Path dir) {
        assertThrows(FileWriteException.class,
                () -> FileIO.writeAnswer(dir.toString(), "95.60"));
    }

    @Test
    void writeAnswer_nullPath_throwsFileWriteException() {
        assertThrows(FileWriteException.class, () -> FileIO.writeAnswer(null, "1.0"));
    }
}
