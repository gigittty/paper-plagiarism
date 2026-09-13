package plagiarism.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import plagiarism.seg.Tokenizer;

/**
 * CosineSimilarity 单元测试：覆盖完全相同、完全不相交、空文本、部分重叠与词频统计。
 */
class CosineSimilarityTest {

    @Test
    void identicalTexts_returnOne() {
        final List<String> a = Arrays.asList("我", "爱", "北京");
        final List<String> b = Arrays.asList("我", "爱", "北京");
        assertEquals(1.0, CosineSimilarity.cosine(a, b), 1e-9);
    }

    @Test
    void completelyDisjoint_returnZero() {
        final List<String> a = Arrays.asList("apple", "banana");
        final List<String> b = Arrays.asList("苹果", "香蕉");
        assertEquals(0.0, CosineSimilarity.cosine(a, b), 1e-9);
    }

    @Test
    void emptyBoth_returnOne() {
        assertEquals(1.0, CosineSimilarity.cosine(List.of(), List.of()), 1e-9);
    }

    @Test
    void oneEmpty_returnZero() {
        assertEquals(0.0, CosineSimilarity.cosine(List.of("x"), List.of()), 1e-9);
    }

    @Test
    void partialOverlap_betweenZeroAndOne() {
        final double sim = CosineSimilarity.cosine(
                Tokenizer.tokenizeByBigram("今天天气晴朗"),
                Tokenizer.tokenizeByBigram("今天天气阴沉"));
        assertTrue(sim > 0.0 && sim < 1.0, "部分重叠的相似度应落在 (0,1)");
    }

    @Test
    void termFrequency_countsCorrectly() {
        final Map<String, Integer> tf =
                CosineSimilarity.termFrequency(Arrays.asList("a", "a", "b"));
        assertEquals(2, tf.get("a"));
        assertEquals(1, tf.get("b"));
    }
}
