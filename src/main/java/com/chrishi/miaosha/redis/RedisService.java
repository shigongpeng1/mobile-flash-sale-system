package com.chrishi.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;



    public <T> T get(KeyPrefix prefix,String key,Class<T> clazz){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            String str = jedis.get(realKey);
            T t = stringToBean(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }


    public <T> boolean set(KeyPrefix prefix,String key,T value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if(str == null || str.length()<=0){
                return false;
            }
            String realkey = prefix.getPrefix()+key;
            int seconds = prefix.expireSeconds();
            if(seconds <= 0){
                jedis.set(realkey,str);
            }else{
                jedis.setex(realkey,seconds,str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> boolean exists(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realkey = prefix.getPrefix()+key;
            return jedis.exists(realkey);
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realkey = prefix.getPrefix()+key;
            return jedis.incr(realkey);
        }finally {
            returnToPool(jedis);
        }
    }

    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realkey = prefix.getPrefix()+key;
            return jedis.decr(realkey);
        }finally {
            returnToPool(jedis);
        }
    }

    private void returnToPool(Jedis jedis){
        if(jedis != null){
            jedis.close();
        }
    }

    public static <T> T stringToBean(String str,Class<T> clazz){
        if(str == null || str.length()<=0 || clazz == null){
            return null;
        }
        if(clazz == int.class || clazz==Integer.class){
            return (T)Integer.valueOf(str);
        }else if(clazz == long.class||clazz==Long.class){
            return (T)Long.valueOf(str);
        }else if(clazz == String.class){
            return (T)str;
        }else{
            T t = JSON.toJavaObject(JSON.parseObject(str),clazz);
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    public static <T> String beanToString(T value){
        if(value == null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if(clazz == int.class || clazz == Integer.class){
            return String.valueOf(value);
        }else if(clazz == String.class){
            return (String)value;
        }else if(clazz == long.class || clazz == Long.class){
            return String.valueOf(clazz);
        }else{
            return JSON.toJSONString(value);
        }
    }
    /**
     * 删除
     */
    public boolean delete(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix()+key;
            long ret = jedis.del(key);
            return ret>0;
        }finally {
            returnToPool(jedis);
        }
    }
}
