package cn.edu.whut.sept.zuul;

/**
 * 物品类.
 * 表示游戏中的物体，具有描述和重量属性.
 */
public class Item {
    private final String description;
    private final double weight; // 修改为 double 以支持小数重量

    /**
     * 创建一个物品.
     * @param description 物品的描述.
     * @param weight 物品的重量.
     */
    public Item(String description, double weight) {
        this.description = description;
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public double getWeight() {
        return weight;
    }
}

