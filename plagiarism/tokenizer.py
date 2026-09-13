"""中文文本分词模块。

优先使用 jieba 进行中文分词以获得更好的语义粒度；
若运行环境未安装 jieba，则降级为「字符级 + 英文单词级」分词，
保证程序在零第三方依赖时仍可使用，从而满足评测环境「禁止联网」的约束。

分词后的 token 仅保留「含中文汉字」或「含字母/数字」的词，
纯标点符号与空白会被丢弃，以减少无意义特征对相似度的干扰。
"""

import importlib.util
import re

# 中文汉字 Unicode 区间（基本汉字）。
_CJK_START = "一"
_CJK_END = "鿿"
# 用于丢弃纯标点 / 空白 token 的判定：只要 token 中含汉字或字母数字即保留。
_TOKEN_KEEP_RE = re.compile(r"[0-9A-Za-z一-鿿]")


def _jieba_available():
    """检测 jieba 是否可导入（仅探测，不真正导入，避免无依赖时加载失败）。"""
    return importlib.util.find_spec("jieba") is not None


def _tokenize_jieba(text):
    """使用 jieba 分词并过滤纯标点 token。"""
    import jieba  # pylint: disable=import-outside-toplevel

    tokens = []
    for raw in jieba.lcut(text):
        token = raw.strip()
        if token and _TOKEN_KEEP_RE.search(token):
            tokens.append(token)
    return tokens


def _tokenize_fallback(text):
    """无 jieba 时的降级分词：中文按字、英文/数字按词。"""
    tokens = []
    buffer = []
    for char in text:
        if _CJK_START <= char <= _CJK_END:
            if buffer:
                tokens.append("".join(buffer))
                buffer = []
            tokens.append(char)
        elif char.isalnum():
            buffer.append(char)
        else:
            if buffer:
                tokens.append("".join(buffer))
                buffer = []
    if buffer:
        tokens.append("".join(buffer))
    return [token for token in tokens if token]


def tokenize(text):
    """将文本切分为 token 列表。

    Args:
        text: 原始文本字符串。

    Returns:
        token 列表（已过滤纯标点与空白）。
    """
    if not text:
        return []
    if _jieba_available():
        return _tokenize_jieba(text)
    return _tokenize_fallback(text)
