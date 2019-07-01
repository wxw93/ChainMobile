package cn.sheep.cms.tools;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by ThinkPad on 2017/8/17.
 */
public class JedisTest {

    private Jedis jedis = null;

    @Before
    public void setup(){
        jedis = new Jedis("10.172.50.54");
        System.out.println("Server is running: "+jedis.ping());
    }


    @Test
    public void addKey(){
        jedis.select(1);

        jedis.set("name", "laoyang");
        jedis.close();
    }

    @Test
    public void xx(){
        Map<String, String> stringMap = jedis.hgetAll("B-Province-Recharge-fail-2017032311");
        for (String key : stringMap.keySet()) {
            System.out.print("key = " + key);
            String v = jedis.hget("B-Province-Recharge-fail-20170323", key);
            System.out.println(" v = " + v);
        }
        jedis.close();
    }

    @Test
    public void testHash(){

        Map<String, String> map = jedis.hgetAll("C*");
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> entry: entrySet) {
            System.out.println("key =" + entry.getKey()+"\t value = "+ entry.getValue());
        }

        System.out.println(" = End" );

    }

    @Test
    public void testLike(){
        Set<String> keys = jedis.keys("C-*");
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            System.out.println("key = " + key);
        }

        jedis.close();
    }

}
