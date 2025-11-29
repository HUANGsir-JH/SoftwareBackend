from typing import TypedDict
import re
import hashlib
from functools import lru_cache

class SearchCacheEntry(TypedDict):
    question: str
    final_answer: str

class SearchAgentState(TypedDict):
    question: str
    is_cached: bool
    retrieved_notes: list[str]
    final_answer: str

def get_from_cache(state: SearchAgentState) -> SearchAgentState:
    normalized_query = normalize_query(state["question"])

def normalize_query(query):
    """将查询标准化，减少变体"""
    # 小写化
    query = query.lower()
    # 去除标点符号
    query = re.sub(r'[^\w\s]', '', query)
    # 去除多余空格
    query = re.sub(r'\s+', ' ', query).strip()
    # 去除常见停用词
    stopwords = ['的', '了', '吗', '呢', '吧', '啊', '呀']
    for word in stopwords:
        query = query.replace(word, '')
    return query