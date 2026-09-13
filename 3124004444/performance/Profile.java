package perf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import plagiarism.io.FileIO;
import plagiarism.seg.Tokenizer;
import plagiarism.sim.CosineSimilarity;

/**
 * 性能分析脚本（开发期工具，不参与 main.jar 发布）。
 *
 * <p>分阶段统计「读取 -> 分词 -> 余弦」的耗时，并把大输入（约 200 倍原文）的分词耗时单独列出，
 * 以定位热点。同时对比「复用单个分词器」与「每次新建分词器」的开销，量化改进效果。
 *
 * <p>运行（已激活 jieba 依赖）：
 * <pre>
 *   javac -encoding UTF-8 -cp out;lib/jieba-analysis.jar -d build/perf performance/Profile.java
 *   java  -XX:+FlightRecorder -XX:StartFlightRecording=filename=performance/perf.jfr ^
 *          -cp out;build/perf;lib/jieba-analysis.jar perf.Profile
 * </pre>
 */
public final class Profile {

    private Profile() {
    }

    /**
     * 程序入口：执行性能分析并写出 performance/timing.csv。
     *
     * @param args 可选：[原文路径] [抄袭版路径]
     * @throws Exception 读取或写 CSV 失败时抛出
     */
    public static void main(final String[] args) throws Exception {
        final String origPath = args.length > 0 ? args[0] : "samples/orig.txt";
        final String copyPath = args.length > 1 ? args[1] : "samples/orig_add.txt";

        // 放大输入以放大热点（约 200 倍原文）
        final String base = FileIO.readText(origPath);
        final StringBuilder big = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            big.append(base);
        }
        final String bigText = big.toString();

        // ---- 冷启动：单次运行，分词含一次性 jieba 词典加载 ----
        final long t0 = System.nanoTime();
        final String o = FileIO.readText(origPath);
        final String c = FileIO.readText(copyPath);
        final long t1 = System.nanoTime();
        final Tokenizer cold = Tokenizer.defaultTokenizer();
        final List<String> to = cold.tokenize(o);
        final List<String> tc = cold.tokenize(c);
        final long t2 = System.nanoTime();
        final double similarity = CosineSimilarity.cosine(to, tc);
        final long t3 = System.nanoTime();

        final double readMs = (t1 - t0) / 1e6;
        final double tokenizeMs = (t2 - t1) / 1e6;
        final double cosineMs = (t3 - t2) / 1e6;

        // ---- 大输入分词耗时（核心热点）----
        final Tokenizer hot = Tokenizer.defaultTokenizer();
        hot.tokenize(bigText); // 预热，触发 JIT
        final long tb = System.nanoTime();
        hot.tokenize(bigText);
        final long te = System.nanoTime();
        final double bigTokenizeMs = (te - tb) / 1e6;

        // ---- 复用 vs 每次新建：量化改进 ----
        final int loops = 10;
        final Tokenizer reuse = Tokenizer.defaultTokenizer();
        final long reuseStart = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            reuse.tokenize(o);
            reuse.tokenize(c);
        }
        final long reuseEnd = System.nanoTime();

        final long newStart = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            final Tokenizer fresh = Tokenizer.defaultTokenizer();
            fresh.tokenize(o);
            fresh.tokenize(c);
        }
        final long newEnd = System.nanoTime();

        final double reuseAvg = (reuseEnd - reuseStart) / 1e6 / loops;
        final double newAvg = (newEnd - newStart) / 1e6 / loops;

        final StringBuilder csv = new StringBuilder();
        csv.append("stage,ms\n");
        csv.append(String.format("readFile,%.3f%n", readMs));
        csv.append(String.format("tokenizeSmallCold,%.3f%n", tokenizeMs));
        csv.append(String.format("tokenizeLarge200x,%.3f%n", bigTokenizeMs));
        csv.append(String.format("cosine,%.3f%n", cosineMs));

        final Path outCsv = Paths.get("performance", "timing.csv");
        Files.writeString(outCsv, csv.toString());

        System.out.println("cold run stages (ms): read=" + readMs
                + " tokenize=" + tokenizeMs + " cosine=" + cosineMs + " similarity=" + similarity);
        System.out.println("large-input tokenize (ms): " + bigTokenizeMs);
        System.out.println("avg per-pair tokenize: reuse=" + reuseAvg
                + " ms  new-each-time=" + newAvg + " ms");
        System.out.println("wrote " + outCsv.toAbsolutePath());
    }
}
