package plagiarism.seg;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 中文文本分词器。
 *
 * <p>优先使用 jieba（com.huaban.analysis.jieba.JiebaSegmenter）进行分词；若运行环境
 * 未提供 jieba（例如未将 jieba-analysis.jar 放入 classpath），则自动降级为基于字符
 * 二元文法（2-gram）的分词，保证程序在无第三方依赖时仍可正确运行。
 */
public final class Tokenizer {

    /** jieba 分词入口方法（若可用），否则为 null。 */
    private static final Method SENTENCE_PROCESS = resolveSentenceProcess();

    /** jieba 是否可用。 */
    private static final boolean JIEBA_AVAILABLE = SENTENCE_PROCESS != null;

    private final boolean useJieba;
    private final Object segmenter;

    /** 构造默认分词器，自动探测 jieba 是否可用。 */
    public Tokenizer() {
        this(JIEBA_AVAILABLE);
    }

    /**
     * 构造分词器（主要用于测试：可强制关闭 jieba 以验证降级路径）。
     *
     * @param preferJieba 是否优先使用 jieba（不可用时仍降级为 2-gram）
     */
    Tokenizer(final boolean preferJieba) {
        this.useJieba = preferJieba && JIEBA_AVAILABLE;
        this.segmenter = this.useJieba ? newSegmenter() : null;
    }

    /** 返回共享的默认分词器实例。 */
    public static Tokenizer defaultTokenizer() {
        return new Tokenizer();
    }

    /**
     * 对文本分词。
     *
     * @param text 待分词文本
     * @return 词元列表
     */
    public List<String> tokenize(final String text) {
        if (useJieba) {
            final List<String> words = tokenizeWithJieba(text);
            if (words != null && !words.isEmpty()) {
                return words;
            }
        }
        return tokenizeByBigram(text);
    }

    @SuppressWarnings("unchecked")
    private List<String> tokenizeWithJieba(final String text) {
        try {
            final List<String> raw = (List<String>) SENTENCE_PROCESS.invoke(segmenter, text);
            if (raw == null || raw.isEmpty()) {
                return null;
            }
            // jieba 会把空格、纯标点也作为词元返回，这里过滤掉无实际语义的标点/空白词元
            final List<String> kept = new ArrayList<>(raw.size());
            for (final String word : raw) {
                if (isMeaningful(word)) {
                    kept.add(word);
                }
            }
            return kept.isEmpty() ? null : kept;
        } catch (final ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }

    private static boolean isMeaningful(final String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        return word.chars().anyMatch(ch -> isHan((char) ch) || isAsciiWordChar((char) ch));
    }

    private static Object newSegmenter() {
        try {
            return SENTENCE_PROCESS.getDeclaringClass().getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            return null;
        }
    }

    private static Method resolveSentenceProcess() {
        try {
            final Class<?> clazz = Class.forName("com.huaban.analysis.jieba.JiebaSegmenter");
            final Method method = clazz.getMethod("sentenceProcess", String.class);
            method.setAccessible(true);
            return method;
        } catch (final ReflectiveOperationException | SecurityException e) {
            return null;
        }
    }

    /**
     * 基于字符二元文法（2-gram）的降级分词，不依赖任何第三方库。
     *
     * <p>规则：连续的汉字按相邻两字切分为二元词；连续的英文/数字按单词整体作为一个词元；
     * 标点与空白作为分隔符。该方式对中文查重稳健，且不敏感于词序。
     *
     * @param text 待分词文本
     * @return 词元列表
     */
    public static List<String> tokenizeByBigram(final String text) {
        final List<String> tokens = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return tokens;
        }
        final StringBuilder cjkRun = new StringBuilder();
        final StringBuilder asciiRun = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (isHan(ch)) {
                flushAscii(tokens, asciiRun);
                cjkRun.append(ch);
            } else if (isAsciiWordChar(ch)) {
                flushCjk(tokens, cjkRun);
                asciiRun.append(Character.toLowerCase(ch));
            } else {
                flushCjk(tokens, cjkRun);
                flushAscii(tokens, asciiRun);
            }
        }
        flushCjk(tokens, cjkRun);
        flushAscii(tokens, asciiRun);
        return tokens;
    }

    private static void flushCjk(final List<String> tokens, final StringBuilder run) {
        if (run.length() == 0) {
            return;
        }
        if (run.length() == 1) {
            tokens.add(run.toString());
        } else {
            for (int i = 0; i < run.length() - 1; i++) {
                tokens.add(run.substring(i, i + 2));
            }
        }
        run.setLength(0);
    }

    private static void flushAscii(final List<String> tokens, final StringBuilder run) {
        if (run.length() > 0) {
            tokens.add(run.toString());
            run.setLength(0);
        }
    }

    private static boolean isHan(final char ch) {
        return Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN;
    }

    private static boolean isAsciiWordChar(final char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
    }
}
