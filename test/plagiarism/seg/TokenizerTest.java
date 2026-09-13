package plagiarism.seg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tokenizer 单元测试：覆盖二元文法降级分词的各种情形，以及 jieba 分词路径。
 */
class TokenizerTest {

    @Test
    void bigram_splitsChineseIntoPairs() {
        final List<String> tokens = Tokenizer.tokenizeByBigram("天气晴朗");
        assertEquals(List.of("天气", "气晴", "晴朗"), tokens);
    }

    @Test
    void bigram_singleChineseChar_isUnigram() {
        assertEquals(List.of("天"), Tokenizer.tokenizeByBigram("天"));
    }

    @Test
    void bigram_englishWordIsSingleToken() {
        assertEquals(List.of("hello"), Tokenizer.tokenizeByBigram("hello"));
    }

    @Test
    void bigram_mixedText() {
        final List<String> tokens = Tokenizer.tokenizeByBigram("Hello世界2024!");
        assertTrue(tokens.contains("hello"));
        assertTrue(tokens.contains("世界"));
        assertTrue(tokens.contains("2024"));
        assertFalse(tokens.contains("!"));
    }

    @Test
    void bigram_emptyInput_returnsEmpty() {
        assertTrue(Tokenizer.tokenizeByBigram("").isEmpty());
    }

    @Test
    void defaultTokenizer_usesJiebaAndProducesTokens() {
        final List<String> tokens =
                Tokenizer.defaultTokenizer().tokenize("今天天气晴朗适合出游");
        assertFalse(tokens.isEmpty());
        assertFalse(tokens.contains("，"));
    }

    @Test
    void fallbackTokenizer_disabledJieba_usesBigram() {
        final List<String> tokens = new Tokenizer(false).tokenize("天气晴朗");
        assertEquals(List.of("天气", "气晴", "晴朗"), tokens);
    }
}
