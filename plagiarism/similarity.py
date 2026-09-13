"""相似度计算模块。

基于「词频向量的余弦相似度」计算两篇文本的重复率。
余弦相似度对词序不敏感、对增删词稳健，非常适合检测在原文上
经过增、删、改得到的抄袭版文本。

相似度公式：
    sim = (A · B) / (||A|| * ||B||)
其中 A、B 为两篇文本的词频向量，结果落在 [0, 1]，越接近 1 越相似。
"""

from collections import Counter

from .exceptions import EmptyTextError
from .tokenizer import tokenize


def build_vector(tokens):
    """由 token 列表构建词频向量（Counter）。

    Args:
        tokens: token 列表。

    Returns:
        词频 Counter；空列表返回空 Counter。
    """
    return Counter(tokens)


def cosine_similarity(vector_a, vector_b):
    """计算两个词频向量的余弦相似度，范围 [0, 1]。

    Args:
        vector_a: 第一篇文本的词频向量（Counter）。
        vector_b: 第二篇文本的词频向量（Counter）。

    Returns:
        余弦相似度；任一向量为空或点积为 0 时返回 0.0。
    """
    if not vector_a or not vector_b:
        return 0.0
    common = set(vector_a) & set(vector_b)
    dot_product = sum(vector_a[token] * vector_b[token] for token in common)
    if dot_product == 0:
        return 0.0
    norm_a = sum(value * value for value in vector_a.values()) ** 0.5
    norm_b = sum(value * value for value in vector_b.values()) ** 0.5
    return dot_product / (norm_a * norm_b)


def compute_similarity(text_original, text_suspect):
    """计算原文与抄袭版文本的重复率（余弦相似度，范围 [0, 1]）。

    Args:
        text_original: 原文文本。
        text_suspect: 抄袭版文本。

    Returns:
        余弦相似度，范围 [0, 1]；完全相同为 1.0，完全无关接近 0.0。

    Raises:
        EmptyTextError: 当任一文本为空或仅含空白字符时抛出。
    """
    if not text_original or not text_original.strip():
        raise EmptyTextError("原文为空或仅含空白")
    if not text_suspect or not text_suspect.strip():
        raise EmptyTextError("抄袭版文本为空或仅含空白")
    return cosine_similarity(
        build_vector(tokenize(text_original)),
        build_vector(tokenize(text_suspect)),
    )
