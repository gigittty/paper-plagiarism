package plagiarism;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import plagiarism.exceptions.InvalidArgsException;

/**
 * Main 入口单元测试：覆盖参数解析、错误退出码与端到端查重流程。
 */
class MainTest {

    @Test
    void parseArgs_ok() throws Exception {
        assertArrayEquals(
                new String[] {"o.txt", "c.txt", "a.txt"},
                Main.parseArgs(new String[] {"o.txt", "c.txt", "a.txt"}));
    }

    @Test
    void parseArgs_wrongCount_throws() {
        assertThrows(InvalidArgsException.class,
                () -> Main.parseArgs(new String[] {"only", "two"}));
    }

    @Test
    void run_wrongArgCount_returns2() {
        assertEquals(2, Main.run(new String[] {"only", "two"}));
    }

    @Test
    void run_missingOriginal_returns1() {
        assertEquals(1, Main.run(new String[] {
                "missing_orig.txt", "samples/orig.txt", "samples/ans_x.txt"}));
    }

    @Test
    void run_integration_identical_returns0AndWrites100(@TempDir final Path dir) throws Exception {
        final Path orig = dir.resolve("orig.txt");
        final Path copy = dir.resolve("copy.txt");
        final Path ans = dir.resolve("ans.txt");
        Files.writeString(orig, "今天是星期天天气晴我要去看电影");
        Files.writeString(copy, "今天是星期天天气晴我要去看电影");
        final int code = Main.run(new String[] {
                orig.toString(), copy.toString(), ans.toString()});
        assertEquals(0, code);
        assertEquals("100.00", Files.readString(ans).trim());
    }

    @Test
    void run_integration_disjoint_returns0AndWritesLow(@TempDir final Path dir) throws Exception {
        final Path orig = dir.resolve("orig.txt");
        final Path copy = dir.resolve("copy.txt");
        final Path ans = dir.resolve("ans.txt");
        Files.writeString(orig, "apple banana cat");
        Files.writeString(copy, "苹果 香蕉 猫");
        final int code = Main.run(new String[] {
                orig.toString(), copy.toString(), ans.toString()});
        assertEquals(0, code);
        assertEquals("0.00", Files.readString(ans).trim());
    }
}
