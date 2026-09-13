package plagiarism.sim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余弦相似度计算：将词元列表转换为词频向量，计算两向量的余弦相似度。
 *
 * <p>余弦相似度只关心词频分布、不关心词序，因此对「增删改」「乱序」等抄袭手段稳健。
 */
public final class CosineSimilarity {

    private CosineSimilarity() {
    }

    /**
     * 计算两段文本的余弦相似度，取值范围 [0, 1]。
     *
     * @param tokensA 原文词元
     * @param tokensB 抄袭版词元
     * @return 相似度，1 表示完全一致，0 表示无任何共有词
     */
    public static double cosine(final List<String> tokensA, final List<String> tokensB) {
        final Map<String, Integer> vectorA = termFrequency(tokensA);
        final Map<String, Integer> vectorB = termFrequency(tokensB);

        if (vectorA.isEmpty() && vectorB.isEmpty()) {
            return 1.0;
        }
        if (vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        final double normA = vectorNorm(vectorA);
        final double normB = vectorNorm(vectorB);
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        // 仅在交集词上计算点积，跳过完全不相交的词，降低无效开销
        final Map<String, Integer> smaller = vectorA.size() <= vectorB.size() ? vectorA : vectorB;
        final Map<String, Integer> larger = smaller == vectorA ? vectorB : vectorA;
        double dotProduct = 0.0;
        for (final Map.Entry<String, Integer> entry : smaller.entrySet()) {
            final Integer other = larger.get(entry.getKey());
            if (other != null) {
                dotProduct += (double) entry.getValue() * other;
            }
        }
        return dotProduct / (normA * normB);
    }

    /**
     * 统计词频。
     *
     * @param tokens 词元列表
     * @return 词 -> 词频
     */
    public static Map<String, Integer> termFrequency(final List<String> tokens) {
        final Map<String, Integer> frequency = new HashMap<>();
        if (tokens == null) {
            return frequency;
        }
        for (final String token : tokens) {
            frequency.merge(token, 1, Integer::sum);
        }
        return frequency;
    }

    private static double vectorNorm(final Map<String, Integer> vector) {
        double sum = 0.0;
        for (final int value : vector.values()) {
            sum += (double) value * value;
        }
        return Math.sqrt(sum);
    }
}
