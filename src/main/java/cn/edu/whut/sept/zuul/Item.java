package cn.edu.whut.sept.zuul;

/**
 * 物品类.
 * 表示游戏中的物体，具有描述和重量属性.
 */
public class Item {
    private final String description;
    private final double weight;
    private double x;
    private double y;

    // 【修改点】：如果创建物品时没有指定坐标，给它一个房间中央区域的随机坐标
    public Item(String description, double weight) {
        this.description = description;
        this.weight = weight;
        // 随机散落在房间中央安全区域 (X: 20~80, Y: 20~80)
        this.x = 20 + Math.random() * 60;
        this.y = 20 + Math.random() * 60;
    }

    // 室友新增的包含坐标的构造方法保持不变
    public Item(String description, double weight, double x, double y) {
        this.description = description;
        this.weight = weight;
        this.x = x;
        this.y = y;
    }

    public String getDescription() { return description; }
    public double getWeight() { return weight; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}