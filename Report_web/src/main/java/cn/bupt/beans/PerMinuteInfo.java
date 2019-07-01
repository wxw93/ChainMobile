package cn.sheep.cms.beans;

/**
 * 每分钟充值数量及充值金额
 * Created by ThinkPad on 2017/8/25.
 */
public class PerMinuteInfo {

    private int reChargeSuccCount = 0;
    private int reChargeFailCount = 0;
    private float reChargeMoney = 0;

    public PerMinuteInfo() {
    }

    public PerMinuteInfo(int reChargeSuccCount, int reChargeFailCount, float reChargeMoney) {
        this.reChargeSuccCount = reChargeSuccCount;
        this.reChargeFailCount = reChargeFailCount;
        this.reChargeMoney = reChargeMoney;
    }

    public int getReChargeSuccCount() {
        return reChargeSuccCount;
    }

    public void setReChargeSuccCount(int reChargeSuccCount) {
        this.reChargeSuccCount = reChargeSuccCount;
    }

    public int getReChargeFailCount() {
        return reChargeFailCount;
    }

    public void setReChargeFailCount(int reChargeFailCount) {
        this.reChargeFailCount = reChargeFailCount;
    }

    public float getReChargeMoney() {
        return reChargeMoney;
    }

    public void setReChargeMoney(float reChargeMoney) {
        this.reChargeMoney = reChargeMoney;
    }
}
