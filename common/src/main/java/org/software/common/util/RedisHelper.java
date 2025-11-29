package org.software.common.util;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class RedisHelper {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public <T> void addSet(String key, T value) {
        stringRedisTemplate.opsForSet().add(key, JSONUtil.toJsonStr(value));
    }

    public void removeSet(String key) {
        stringRedisTemplate.delete(key);
    }

    public <T> void removeSetValue(String key, T value) {
        stringRedisTemplate.opsForSet().remove(key, JSONUtil.toJsonStr(value));
    }

    public <T> Set<T> getSet(String key, Class<T> clazz, Supplier<Set<T>> function) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members != null) {
            return members.stream()
                    .map(member -> JSONUtil.toBean(member, clazz))
                    .collect(Collectors.toSet());
        }

        Set<T> set = function.get();
        members = set.stream()
                .map(JSONUtil::toJsonStr)
                .collect(Collectors.toSet());

        stringRedisTemplate.opsForSet().add(key, members.toArray(new String[0]));
        return set;
    }


    public <T> boolean isMember(String key, T value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, JSONUtil.toJsonStr(value)));
    }

    public int hasMember(String key){
        Long size = stringRedisTemplate.opsForSet().size(key);
        return size != null ? size.intValue() : 0;
    }

    /**
     * 实现分布式锁/去重，当key不存在时设置成功，已存在则返回false
     * @param key Redis键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true-设置成功，false-key已存在
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit));
    }
}
