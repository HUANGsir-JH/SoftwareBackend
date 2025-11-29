import redis
import os

# 创建 Redis 连接池
pool = redis.ConnectionPool(
    host=os.getenv("REDIS_HOST"),
    port=6379,
    db=0,
    password=os.getenv("REDIS_PASSWORD"),
    decode_responses=True
)

r = redis.Redis(connection_pool=pool)

# 测试连接
try:
    r.ping()
    print("连接 Redis 成功！")
except redis.ConnectionError:
    print("连接 Redis 失败！")