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

    public Item(String description, double weight) {
        this.description = description;
        this.weight = weight;
        this.x = 50;
        this.y = 50;
    }

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

