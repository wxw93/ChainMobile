package cn.sheep.cms.service;

import cn.sheep.cms.beans.PerMinuteInfo;
import cn.sheep.cms.beans.ProvinceInfo;
import cn.sheep.cms.tools.JedisClient;
import cn.sheep.cms.utils.Constant;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by ThinkPad on 2017/8/19.
 */
public class ReportService {



    /**
     * 从Redis中, 查询各省份充值失败量
     * @param day yyyyMMdd
     */
    public List<ProvinceInfo> provinceReChargeFail(String day){
        List<ProvinceInfo> list = new ArrayList<ProvinceInfo>();

        String key = Constant.B_RECHARGE_SUCC_PREFIX + day;

        Jedis jedis = JedisClient.getJedis();
        Map<String, String> hgetAll = jedis.hgetAll(key);

        for (String field : hgetAll.keySet()) {

            // 从缓存中读取省份的名称
            String provName = Constant.provinceCodeName.get(field);

            // 从redis中获取该省的充值失败数量
            String failCount = jedis.hget(key, field);

            list.add(new ProvinceInfo(provName, Integer.valueOf(failCount)));
        }
        jedis.close();
        return list;
    }


    public PerMinuteInfo perMinuteReChargeAndMoney(String fullTimeKey){
        PerMinuteInfo pmi = new PerMinuteInfo();

        String key = Constant.RECHARGE_PM_PREFIX+fullTimeKey;
        Jedis jedis = JedisClient.getJedis();

        Map<String, String> all = jedis.hgetAll(key);

        if (all != null && all.size() > 0) {
            String money =all.get("money");
            String succ = all.get("succ");
                String fail = all.get("succ");

            pmi.setReChargeFailCount(Integer.parseInt(fail));
            pmi.setReChargeSuccCount(Integer.parseInt(succ));
            pmi.setReChargeMoney(Float.parseFloat(money));
        }
        return pmi;
    }


}
