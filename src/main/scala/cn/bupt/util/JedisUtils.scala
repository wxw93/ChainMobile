package cn.bupt.util

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{Jedis, JedisPool}

object JedisUtils {

  def getJedis():Jedis ={
    val jedisPool = new JedisPool(new GenericObjectPoolConfig(),"master")
    jedisPool.getResource
  }
}
