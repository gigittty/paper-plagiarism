package plagiarism;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import plagiarism.io.FileIO;
import plagiarism.seg.Tokenizer;
import plagiarism.sim.CosineSimilarity;

/**
 * 基于 samples 目录下课堂样例材料的集成测试。
 *
 * <p>样例命名形如 {@code orig.txt} / {@code orig_0.8_add.txt} / {@code orig_0.8_del.txt} /
 * {@code orig_0.8_dis_1.txt} …。若某个样例文件尚未用课堂下发的原始文本覆盖，则对应用例自动跳过
 * （而非失败），便于在缺少部分材料时仍能运行其余测试。</p>
 */
class SampleDataTest {

    /** 判断样例文件是否就绪，未就绪则跳过当前用例。 */
    private static void require(final String... files) {
        for (final String file : files) {
            assumeTrue(Files.exists(Paths.get(file)),
                    "缺少样例文件（请用课堂下发文件覆盖）：" + file);
        }
    }

    private double similarity(final String origFile, final String copyFile) throws Exception {
        final String original = FileIO.readText(origFile);
        final String copy = FileIO.readText(copyFile);
        final Tokenizer tokenizer = Tokenizer.defaultTokenizer();
        return CosineSimilarity.cosine(
                tokenizer.tokenize(original), tokenizer.tokenize(copy));
    }

    @Test
    void origVsSelf_isFullSimilarity() throws Exception {
        require("samples/orig.txt");
        final double sim = similarity("samples/orig.txt", "samples/orig.txt");
        assertTrue(Math.abs(sim - 1.0) < 1e-9, "原文与自身应完全一致，实际=" + sim);
    }

    @Test
    void origAdd_isHighSimilarity() throws Exception {
        require("samples/orig.txt", "samples/orig_0.8_add.txt");
        final double sim = similarity("samples/orig.txt", "samples/orig_0.8_add.txt");
        assertTrue(sim > 0.90, "在原文中插字后应仍保持高重复率，实际=" + sim);
    }

    @Test
    void origDel_isHighSimilarity() throws Exception {
        require("samples/orig.txt", "samples/orig_0.8_del.txt");
        final double sim = similarity("samples/orig.txt", "samples/orig_0.8_del.txt");
        assertTrue(sim > 0.60, "删减少量内容后重复率应仍偏高，实际=" + sim);
    }

    @Test
    void origDisorder_isHighSimilarity() throws Exception {
        require("samples/orig.txt", "samples/orig_0.8_dis_1.txt");
        final double sim = similarity("samples/orig.txt", "samples/orig_0.8_dis_1.txt");
        assertTrue(sim > 0.60, "仅乱序不应大幅改变词频，重复率应偏高，实际=" + sim);
    }
}
