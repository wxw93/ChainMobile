package cn.sheep.cms.beans;

/**
 * 关于省份的统计
 * Created by ThinkPad on 2017/8/19.
 */
public class ProvinceInfo {

    private String name;
    private Integer value;

    public ProvinceInfo() {
    }

    public ProvinceInfo(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
