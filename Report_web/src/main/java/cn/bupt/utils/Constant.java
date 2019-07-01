package cn.sheep.cms.utils;

import com.typesafe.config.*;

import java.util.*;

/**
 * Redis 中的数据Key 特征
 * 常量类
 * Created by ThinkPad on 2017/8/19.
 */
public class Constant {

    public static Map<String, String> provinceCodeName = null;

    /**
     * 加载配置文件中的地域编码和名称对应关系
     */
    static {
        provinceCodeName = new HashMap<String, String>();

        Config load = ConfigFactory.load();

        ConfigObject loadObject = load.getObject("province");
        Set<Map.Entry<String, ConfigValue>> entrySet = loadObject.entrySet();

        Iterator<Map.Entry<String, ConfigValue>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConfigValue> next = iterator.next();
            provinceCodeName.put(next.getKey(), next.getValue().unwrapped().toString());
        }
        System.out.println("=====读取配置文件的省市编码对应关系....共 " + provinceCodeName.size()+" 条.");
    }

    /**
     * 业务概况(按小时纬度)
     * A-YYYYMMDDHH
     *      recharge-money[充值金额]  value
     *      recharge-amount[成功+失败=总订单量]  value
     *      recharge-success[充值成功量] value
     *      recharge-duration-time[总时长] value
     *
     *      成功率 = 充值成功量/总订单量
     *      平均时长 = 总时长/总订单量
     */

    public static final String A_RECHARGE_INFO_PH_PERFIX = "A-";

    /**
     * 业务质量 -- 失败量地图分布
     *
     * B-Province-Recharge-fail-日期(YYYYMMDD)
     *     省Code1   value
     *     省Code2   value
     */
    public static final String B_RECHARGE_FAIL_PREFIX = "B-Province-Recharge-fail-";

    /**
     * 业务质量 -- 失败量地图分布
     * B-Province-Recharge-succ-日期(YYYYMMDD)
     *      省Code1   value
     *      省Code2   value
     */
    public static final String B_RECHARGE_SUCC_PREFIX = "C-";


    /**
     * 充值情况分布---每分钟(Per minute)的量
     * C-201707071615
     *      recharge-money  value
     *      recharge-fail   value
     *      recharge-succ   value
     */
    public static final String RECHARGE_PM_PREFIX = "C-";




}
