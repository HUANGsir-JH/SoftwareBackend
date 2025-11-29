from dotenv import load_dotenv
from langchain_community.embeddings import DashScopeEmbeddings
import os
from langchain_deepseek import ChatDeepSeek

load_dotenv()

dashscope_api_key = os.getenv("DASHSCOPE_API_KEY")

# ===============================================================
# 嵌入模型
# ===============================================================
embedding_model = DashScopeEmbeddings(
    model="text-embedding-v4",
    dashscope_api_key=dashscope_api_key, 
)
# ===============================================================
# 对话模型
# ===============================================================
chat_model = ChatDeepSeek(model="deepseek-chat")