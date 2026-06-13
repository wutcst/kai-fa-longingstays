package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;

/**
 * 该类代表游戏中的一个“房间”（场景）.
 * 一个“房间”代表了游戏世界中的一个位置.
 * 房间与其他房间之间通过出口相连.
 * 出口以“北”、“东”、“南”、“西”等方向命名.
 * 对于每个方向，房间要么连接到另一个房间，要么没有出口.
 */
public class Room
{
    private final String description;
    private final HashMap<String, Room> exits;        // 存储该房间各个方向的出口
    private final ArrayList<Item> items; // 存储房间内的物品

    /**
     * 创建一个房间，其描述由字符串 "description" 指定.
     * 一开始，这个房间没有任何出口.
     * "description" 类似于 "在厨房" 或 "在开阔的庭院".
     * @param description 房间的描述.
     */
    public Room(String description)
    {
        this.description = description;
        exits = new HashMap<>();
        items = new ArrayList<>();
    }

    /**
     * 定义房间的一个出口.
     * @param direction 出口的方向（例如 "north", "east"）.
     * @param neighbor 该方向连接的邻居房间.
     */
    public void setExit(String direction, Room neighbor)
    {
        exits.put(direction, neighbor);
    }

    /**
     * @return 房间的简短描述（例如 "在厨房"）.
     * 返回房间的简短描述，只包含：位置描述 + 出口.
     * (不显示物品信息)
     */
    public String getShortDescription()
    {
        return "You are " + description + ".\n" + getExitString();
    }

    /**
     * 向房间添加一个物品.
     * @param item 要添加的物品.
     */
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * 【新增】从房间移除指定物品.
     * @param item 要移除的物品对象.
     */
    public void removeItem(Item item) {
        items.remove(item);
    }

    /**
     * 【新增】根据名称查找房间内的物品.
     * @param name 物品名称(描述中包含该名称即可).
     * @return 找到的物品对象，如果没找到返回null.
     */
    public Item getItem(String name) {
        for (Item item : items) {
            // 这里简单的用包含关系，比如输入 "cookie" 可以匹配 "magic cookie"
            // 为了更精确，也可以用 startWith 或 equals
            if (item.getDescription().contains(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 【新增】获取房间内物品的总重量.
     */
    public double getTotalItemWeight() {
        double total = 0;
        for (Item item : items) {
            total += item.getWeight();
        }
        return total;
    }

    /**
     * 返回房间的长描述，形式为：
     * You are in the kitchen.
     * Exits: north，west
     * @return 房间的详细描述，包含出口信息和物品信息.
     */
    public String getLongDescription()
    {
        return "You are " + description + ".\n" + getExitString()+ "\n" + getItemsString();
    }

    /**
     * 返回房间所有的出口字符串，例如 "Exits: north west".
     * @return 房间所有出口方向的拼接字符串.
     */
    private String getExitString()
    {
        StringBuilder returnString = new StringBuilder("Exits:");
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            returnString.append(" ").append(exit);
        }
        return returnString.toString();
    }

    /**
     * 获取房间内所有物品的描述字符串.
     */
    private String getItemsString() {
        if (items.isEmpty()) {
            return "No items here.";
        }
        StringBuilder returnString = new StringBuilder("Items in room:");
        for (Item item : items) {
            returnString.append("\n  - ")
                    .append(item.getDescription())
                    .append(" (")
                    .append(item.getWeight())
                    .append(" kg)");
        }
        return returnString.toString();
    }

    /**
     * 返回指定方向的房间。如果该方向没有出口，则返回 null.
     * @param direction 以此方向离开当前房间.
     * @return 指定方向的房间，如果没有则返回 null.
     */
    public Room getExit(String direction)
    {
        return exits.get(direction);
    }

    /**
     * 【新增 for GUI】获取房间内物品列表.
     * @return 物品列表.
     */
    public ArrayList<Item> getItems() {
        return items;
    }
}

