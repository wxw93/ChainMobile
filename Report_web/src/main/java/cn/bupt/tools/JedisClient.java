package cn.sheep.cms.tools;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by ThinkPad on 2017/8/19.
 */
public class JedisClient {


    private static JedisPool pool = null;

    static {

        Config load = ConfigFactory.load();

        JedisPoolConfig config = new JedisPoolConfig();

        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        config.setBlockWhenExhausted(true);
        config.setMaxTotal(load.getInt("redis.pool.maxActive"));
        config.setMaxIdle(load.getInt("redis.pool.maxIdle"));
        config.setTestOnBorrow(load.getBoolean("redis.pool.testOnBorrow"));
        config.setTestOnReturn(load.getBoolean("redis.pool.testOnReturn"));

        pool = new JedisPool(config, load.getString("redis.ip"), load.getInt("redis.port"), 3000, null, 0);
    }


    /**
     * 从连接池中获取Jedis客户端
     * @return
     */
    public static Jedis getJedis(){
        try {
            synchronized (JedisClient.class) {
                if (pool != null) {
                   return pool.getResource();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭Jedis实例
     * @param jedis
     */
    public static void close(Jedis jedis) {
        jedis.close();
    }


    public static void main(String[] args) {
        Jedis jedis = JedisClient.getJedis();

        Set<String> keys = jedis.keys("B*");
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()){
            System.out.println("iterator.next() = " + iterator.next());
        }

        JedisClient.close(jedis);



    }


}
