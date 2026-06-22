package cn.edu.whut.sept.zuul;

import java.util.ArrayList;

/**
 * 玩家类.
 * 记录玩家的姓名、当前位置、随身物品以及负重状态.
 */
public class Player {
    private final String name;
    private Room currentRoom;
    private final ArrayList<Item> inventory; // 背包
    private double maxWeight; // 最大负重
    private int coins; // 金币

    /**
     * 创建玩家对象.
     * @param name 玩家姓名.
     * @param currentRoom 初始房间.
     * @param maxWeight 初始最大负重.
     */
    public Player(String name, Room currentRoom, double maxWeight) {
        this.name = name;
        this.currentRoom = currentRoom;
        this.maxWeight = maxWeight;
        this.inventory = new ArrayList<>();
        this.coins = 30; // 初始金币
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * 尝试拾取物品.
     * @param item 要拾取的物品.
     * @return 成功返回 true，超重返回 false.
     */
    public boolean takeItem(Item item) {
        if (getCurrentWeight() + item.getWeight() > maxWeight) {
            return false;
        }
        inventory.add(item);
        return true;
    }

    /**
     * 丢弃物品.
     * @param item 要丢弃的物品.
     */
    public void dropItem(Item item) {
        inventory.remove(item);
    }

    /**
     * 获取当前背包总重量.
     */
    public double getCurrentWeight() {
        double total = 0;
        for (Item item : inventory) {
            total += item.getWeight();
        }
        return total;
    }

    /**
     * 在背包中查找物品.
     */
    public Item getItem(String name) {
        for (Item item : inventory) {
            if (item.getDescription().contains(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 增加负重上限（吃魔法饼干后调用）.
     */
    public void increaseMaxWeight(double amount) {
        this.maxWeight += amount;
    }

    /**
     * 获取背包物品详情字符串.
     */
    public String getInventoryString() {
        if (inventory.isEmpty()) {
            return "You are not carrying anything.";
        }
        StringBuilder returnString = new StringBuilder("You are carrying:");
        for (Item item : inventory) {
            returnString.append("\n  - ")
                    .append(item.getDescription())
                    .append(" (")
                    .append(item.getWeight())
                    .append(" kg)");
        }
        returnString.append("\nTotal weight: ")
                .append(getCurrentWeight())
                .append("/")
                .append(maxWeight)
                .append(" kg");
        return returnString.toString();
    }

    /**
     * 【新增 for GUI】获取背包物品列表.
     */
    public ArrayList<Item> getInventory() {
        return inventory;
    }

    /**
     * 【新增 for GUI】获取最大负重.
     */
    public double getMaxWeight() {
        return maxWeight;
    }

    /**
     * 【新增 for GUI】获取玩家姓名.
     */
    public String getName() {
        return name;
    }

    /**
     * 【新增】获取金币数量.
     */
    public int getCoins() {
        return coins;
    }

    /**
     * 【新增】设置金币数量.
     */
    public void setCoins(int coins) {
        this.coins = coins;
    }

    /**
     * 【新增】花费金币.
     * @param amount 要花费的数量.
     * @return 是否花费成功（余额不足返回 false）.
     */
    public boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

    /**
     * 【新增】增加金币.
     */
    public void addCoins(int amount) {
        coins += amount;
    }
}

