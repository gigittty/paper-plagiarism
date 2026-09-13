package plagiarism;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plagiarism.io.FileIO;
import plagiarism.seg.Tokenizer;
import plagiarism.sim.CosineSimilarity;

/**
 * 基于 samples 目录下样例材料的集成测试：验证算法对「增、删、乱序、同义替换」的判定符合直觉。
 * 运行前请先用课堂下发的 orig*.txt 覆盖 samples/ 下对应文件。
 */
class SampleDataTest {

    private double similarity(final String origFile, final String copyFile) throws Exception {
        final String original = FileIO.readText(origFile);
        final String copy = FileIO.readText(copyFile);
        final Tokenizer tokenizer = Tokenizer.defaultTokenizer();
        return CosineSimilarity.cosine(
                tokenizer.tokenize(original), tokenizer.tokenize(copy));
    }

    @Test
    void origAdd_similarityHigh() throws Exception {
        final double sim = similarity("samples/orig.txt", "samples/orig_add.txt");
        assertTrue(sim > 0.90, "增删少量内容应仍保持高重复率，实际=" + sim);
    }

    @Test
    void origDel_similarityHigh() throws Exception {
        final double sim = similarity("samples/orig.txt", "samples/orig_del.txt");
        assertTrue(sim > 0.85, "删除少量内容应仍保持高重复率，实际=" + sim);
    }

    @Test
    void origDisorder_similarityIsOne() throws Exception {
        final double sim = similarity("samples/orig.txt", "samples/orig_dis_1.txt");
        assertTrue(Math.abs(sim - 1.0) < 1e-9, "仅乱序不应改变词频，重复率应为 100%，实际=" + sim);
    }

    @Test
    void origReplace_lowerThanAdd() throws Exception {
        final double add = similarity("samples/orig.txt", "samples/orig_add.txt");
        final double rep = similarity("samples/orig.txt", "samples/orig_rep.txt");
        assertTrue(rep < add,
                "同义替换应使重复率低于简单增删，add=" + add + " rep=" + rep);
    }
}
