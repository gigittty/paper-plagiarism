package plagiarism;

import plagiarism.exceptions.InvalidArgsException;
import plagiarism.exceptions.PlagiarismException;
import plagiarism.io.FileIO;
import plagiarism.seg.Tokenizer;
import plagiarism.sim.CosineSimilarity;

/**
 * 论文查重程序入口。
 *
 * <p>用法：java -jar main.jar [原文文件] [抄袭版论文文件] [答案文件]
 * 程序从指定路径读取原文与抄袭版，计算重复率（百分比，保留两位小数）并写入答案文件。
 */
public final class Main {

    /** 重复率按百分比输出时的缩放系数。 */
    private static final double RATE_SCALE = 100.0;

    private Main() {
    }

    /**
     * 程序入口。
     *
     * @param args 命令行参数
     */
    public static void main(final String[] args) {
        System.exit(run(args));
    }

    /**
     * 执行查重流程，返回进程退出码（不直接调用 System.exit，便于单元测试）。
     *
     * @param args 命令行参数
     * @return 0 成功；1 运行异常；2 参数错误
     */
    static int run(final String[] args) {
        final String[] paths;
        try {
            paths = parseArgs(args);
        } catch (final InvalidArgsException e) {
            System.err.println(e.getMessage());
            return 2;
        }

        // 复用同一个分词器，避免每次调用都重新加载 jieba 词典（首加载约 0.4s）
        final Tokenizer tokenizer = Tokenizer.defaultTokenizer();
        try {
            final String original = FileIO.readText(paths[0]);
            final String copy = FileIO.readText(paths[1]);
            final double similarity = CosineSimilarity.cosine(
                    tokenizer.tokenize(original),
                    tokenizer.tokenize(copy));
            final String answer = String.format("%.2f", similarity * RATE_SCALE);
            FileIO.writeAnswer(paths[2], answer);
            return 0;
        } catch (final PlagiarismException e) {
            System.err.println("查重失败：" + e.getMessage());
            return 1;
        } catch (final Exception e) {
            System.err.println("发生未预期错误：" + e.getMessage());
            return 1;
        }
    }

    /**
     * 解析并校验命令行参数。
     *
     * @param args 原始参数
     * @return 长度为 3 的路径数组
     * @throws InvalidArgsException 参数数量不为 3 时抛出
     */
    static String[] parseArgs(final String[] args) throws InvalidArgsException {
        if (args == null || args.length != 3) {
            throw new InvalidArgsException(
                    "参数错误：需要 3 个路径参数 —— "
                            + "java -jar main.jar [原文文件] [抄袭版文件] [答案文件]");
        }
        return new String[] {args[0], args[1], args[2]};
    }
}
