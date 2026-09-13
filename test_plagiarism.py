"""论文查重程序单元测试。

使用标准库 unittest 编写，覆盖：
- 相似度计算（完全相同 / 完全不同 / 部分重叠 / 比例已知）
- 边界情况（空文本、空向量、零模长）
- 分词（标点过滤、jieba / 降级两种路径）
- 文件读写（正常、缺失、编码错误、非法路径）
- 命令行参数解析（正确 / 数量错误）
- 端到端 main 主流程（正常 / 各类异常分支）

运行方式（无需第三方依赖）：
    python -m pytest test_plagiarism.py -v
或：
    python test_plagiarism.py
"""

import os
import sys
import tempfile
import unittest
from unittest import mock

from main import main, parse_args
from plagiarism import file_io, similarity
from plagiarism.exceptions import (
    ArgumentError,
    EmptyTextError,
    EncodingError,
    FileReadError,
)

ORIGINAL_SAMPLE = "今天是星期天，天气晴，今天晚上我要去看电影。"
COPY_SAMPLE = "今天是周天，天气晴朗，我晚上要去看电影。"


def _write_temp(content, encoding="utf-8"):
    """写入临时文件并返回路径，调用方负责清理。"""
    handle, path = tempfile.mkstemp(suffix=".txt")
    with os.fdopen(handle, "w", encoding=encoding) as temp:
        temp.write(content)
    return path


class TestSimilarityMath(unittest.TestCase):
    """相似度数学正确性测试。"""

    def test_identical_texts(self):
        """完全相同的文本相似度应为 1.0。"""
        sim = similarity.compute_similarity(ORIGINAL_SAMPLE, ORIGINAL_SAMPLE)
        self.assertAlmostEqual(sim, 1.0, places=6)

    def test_different_texts_low_similarity(self):
        """语义无关的两段文本相似度应很低（<0.2）。"""
        sim = similarity.compute_similarity("今天天气晴朗适合出游", "量子力学研究微观粒子运动")
        self.assertLess(sim, 0.2)

    def test_partial_overlap_known_ratio(self):
        """「中国美国」vs「中国日本」共有词「中国」，相似度应为 0.5。"""
        sim = similarity.compute_similarity("中国美国", "中国日本")
        self.assertAlmostEqual(sim, 0.5, places=6)

    def test_assignment_example_in_range(self):
        """作业示例句的相似度应处于合理区间 (0.5, 1.0]。"""
        sim = similarity.compute_similarity(ORIGINAL_SAMPLE, COPY_SAMPLE)
        self.assertGreater(sim, 0.5)
        self.assertLessEqual(sim, 1.0)


class TestSimilarityEdge(unittest.TestCase):
    """相似度边界与异常测试。"""

    def test_empty_original_raises(self):
        """原文为空应抛出 EmptyTextError。"""
        with self.assertRaises(EmptyTextError):
            similarity.compute_similarity("", "抄袭文本")

    def test_empty_copy_raises(self):
        """抄袭版仅含空白应抛出 EmptyTextError。"""
        with self.assertRaises(EmptyTextError):
            similarity.compute_similarity("原文文本", "   ")

    def test_both_empty_raises(self):
        """两者皆为空应抛出 EmptyTextError。"""
        with self.assertRaises(EmptyTextError):
            similarity.compute_similarity("", "")

    def test_cosine_empty_vector(self):
        """空向量余弦相似度应为 0.0。"""
        self.assertEqual(similarity.cosine_similarity({}, {}), 0.0)
        self.assertEqual(similarity.cosine_similarity({"a": 1}, {}), 0.0)

    def test_cosine_disjoint_vectors(self):
        """无共有词的向量余弦相似度应为 0.0。"""
        self.assertEqual(similarity.cosine_similarity({"a": 1}, {"b": 1}), 0.0)


class TestTokenizer(unittest.TestCase):
    """分词模块测试。"""

    def test_punctuation_filtered(self):
        """分词应丢弃纯标点 token。"""
        tokens = similarity.tokenize("你好，世界！")
        self.assertNotIn("，", tokens)
        self.assertNotIn("！", tokens)
        self.assertTrue(any("你好" in token or token == "你" for token in tokens))

    def test_tokenize_non_empty(self):
        """中文文本分词结果非空。"""
        self.assertTrue(len(similarity.tokenize(ORIGINAL_SAMPLE)) > 0)

    def test_tokenize_empty(self):
        """空字符串分词返回空列表。"""
        self.assertEqual(similarity.tokenize(""), [])

    def test_tokenize_fallback_without_jieba(self):
        """当 jieba 不可用时降级为字符级分词，中英文/数字混合也能正确切分。"""
        with mock.patch("plagiarism.tokenizer._jieba_available", return_value=False):
            tokens = similarity.tokenize("Hello 世界abc中 world2024end")
        self.assertNotIn(" ", tokens)
        self.assertIn("Hello", tokens)
        self.assertIn("world2024end", tokens)
        self.assertIn("世", tokens)


class TestFileIO(unittest.TestCase):
    """文件读写与异常测试。"""

    def test_read_normal(self):
        """正常读取应返回文件内容。"""
        path = _write_temp(ORIGINAL_SAMPLE)
        try:
            self.assertEqual(file_io.read_text(path), ORIGINAL_SAMPLE)
        finally:
            os.remove(path)

    def test_read_missing_file(self):
        """读取不存在的文件应抛出 FileReadError。"""
        with self.assertRaises(FileReadError):
            file_io.read_text("C:\\not_exist_path\\no_such_file.txt")

    def test_read_empty_path(self):
        """空路径应抛出 FileReadError。"""
        with self.assertRaises(FileReadError):
            file_io.read_text("")

    def test_read_non_utf8_encoding(self):
        """读取非 UTF-8 编码文件应抛出 EncodingError。"""
        path = _write_temp("中文内容测试", encoding="gbk")
        try:
            with self.assertRaises(EncodingError):
                file_io.read_text(path)
        finally:
            os.remove(path)

    def test_write_result(self):
        """写入答案文件后应可正确读回。"""
        path = _write_temp("")
        try:
            os.remove(path)
            file_io.write_result(path, "85.23")
            with open(path, "r", encoding="utf-8") as handle:
                self.assertEqual(handle.read(), "85.23")
        finally:
            if os.path.exists(path):
                os.remove(path)

    def test_write_empty_path(self):
        """向空路径写入应抛出 FileReadError。"""
        with self.assertRaises(FileReadError):
            file_io.write_result("", "85.23")

    def test_write_to_directory_raises(self):
        """向目录路径写入（OSError）应转换为 FileReadError。"""
        with self.assertRaises(FileReadError):
            file_io.write_result(tempfile.gettempdir(), "85.23")


class TestParseArgs(unittest.TestCase):
    """命令行参数解析测试。"""

    def test_parse_args_ok(self):
        """三个参数应被解析为 (orig, copy, ans) 三元组。"""
        self.assertEqual(
            parse_args(["main.py", "o.txt", "c.txt", "a.txt"]),
            ("o.txt", "c.txt", "a.txt"),
        )

    def test_parse_args_wrong_count(self):
        """参数数量不为 3 应抛出 ArgumentError。"""
        with self.assertRaises(ArgumentError):
            parse_args(["main.py", "o.txt", "c.txt"])


class TestMainEndToEnd(unittest.TestCase):
    """main 主流程端到端测试（含各异常分支覆盖）。"""

    def _run_main(self, orig_text, copy_text):
        orig_path = _write_temp(orig_text)
        copy_path = _write_temp(copy_text)
        ans_path = _write_temp("")
        os.remove(ans_path)
        try:
            code = main(["main.py", orig_path, copy_path, ans_path])
            result = None
            if os.path.exists(ans_path):
                with open(ans_path, "r", encoding="utf-8") as handle:
                    result = handle.read()
            return code, result
        finally:
            for path in (orig_path, copy_path, ans_path):
                if os.path.exists(path):
                    os.remove(path)

    def test_main_normal(self):
        """端到端正常流程应退出 0 且答案与计算一致（2 位小数）。"""
        expected = 100.0 * similarity.compute_similarity(ORIGINAL_SAMPLE, COPY_SAMPLE)
        code, result = self._run_main(ORIGINAL_SAMPLE, COPY_SAMPLE)
        self.assertEqual(code, 0)
        self.assertIsNotNone(result)
        self.assertAlmostEqual(float(result), expected, places=2)

    def test_main_argument_error_branch(self):
        """参数数量错误应进入 ArgumentError 分支并返回退出码 1。"""
        self.assertEqual(main(["main.py", "only_one"]), 1)

    def test_main_file_read_error_branch(self):
        """原文缺失应进入 FileReadError 分支并返回退出码 1。"""
        ans_path = _write_temp("")
        os.remove(ans_path)
        try:
            code = main(["main.py", "C:\\missing\\orig.txt", "C:\\missing\\copy.txt", ans_path])
            self.assertEqual(code, 1)
        finally:
            if os.path.exists(ans_path):
                os.remove(ans_path)

    def test_main_encoding_error_branch(self):
        """读取 GBK 文件应进入 EncodingError 分支并返回退出码 1。"""
        orig_path = _write_temp("中文内容测试", encoding="gbk")
        copy_path = _write_temp(COPY_SAMPLE)
        ans_path = _write_temp("")
        os.remove(ans_path)
        try:
            code = main(["main.py", orig_path, copy_path, ans_path])
            self.assertEqual(code, 1)
        finally:
            for path in (orig_path, copy_path, ans_path):
                if os.path.exists(path):
                    os.remove(path)

    def test_main_empty_text_error_branch(self):
        """原文为空应进入 EmptyTextError 分支并返回退出码 1。"""
        code, _ = self._run_main("", COPY_SAMPLE)
        self.assertEqual(code, 1)

    def test_main_unknown_exception_branch(self):
        """被注入的意外异常应被兜底捕获并返回退出码 1。"""
        orig_path = _write_temp(ORIGINAL_SAMPLE)
        copy_path = _write_temp(COPY_SAMPLE)
        ans_path = _write_temp("")
        os.remove(ans_path)
        try:
            with mock.patch(
                "plagiarism.similarity.compute_similarity",
                side_effect=RuntimeError("boom"),
            ):
                code = main(["main.py", orig_path, copy_path, ans_path])
            self.assertEqual(code, 1)
        finally:
            for path in (orig_path, copy_path, ans_path):
                if os.path.exists(path):
                    os.remove(path)

    def test_main_default_argv(self):
        """未显式传参时回退到 sys.argv，正常流程应退出 0。"""
        orig_path = _write_temp(ORIGINAL_SAMPLE)
        copy_path = _write_temp(COPY_SAMPLE)
        ans_path = _write_temp("")
        os.remove(ans_path)
        try:
            with mock.patch.object(
                sys, "argv", ["main.py", orig_path, copy_path, ans_path]
            ):
                code = main()
            self.assertEqual(code, 0)
        finally:
            for path in (orig_path, copy_path, ans_path):
                if os.path.exists(path):
                    os.remove(path)


if __name__ == "__main__":
    unittest.main(verbosity=2)
