package cn.edu.whut.sept.zuul.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏状态数据传输对象.
 * 用于将游戏状态序列化为 JSON 字符串（存储到数据库）.
 * 也用于从 JSON 字符串反序列化恢复游戏状态.
 * 所有数据以简单 Java 对象（POJO）形式存在，通过 Gson 序列化.
 */
public class GameStateDTO {

    /** 玩家名称 */
    private String playerName;

    /** 玩家当前所在房间 ID */
    private String currentRoomId;

    /** 玩家最大负重 */
    private double maxWeight;

    /** 玩家背包物品列表 */
    private List<ItemDTO> inventory;

    /** 所有房间 Map: roomId -> RoomDTO */
    private Map<String, RoomDTO> rooms;

    /** 历史记录栈中的房间 ID 列表 (从栈底到栈顶) */
    private List<String> historyRoomIds;

    public GameStateDTO() {
        inventory = new ArrayList<>();
        rooms = new LinkedHashMap<>();
        historyRoomIds = new ArrayList<>();
    }

    // ---- Inner DTOs ----

    /**
     * 物品 DTO.
     */
    public static class ItemDTO {
        private String description;
        private double weight;

        public ItemDTO() {}

        public ItemDTO(String description, double weight) {
            this.description = description;
            this.weight = weight;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
    }

    /**
     * 房间 DTO.
     */
    public static class RoomDTO {
        /** 房间唯一标识（例如 "outside", "theater"） */
        private String id;
        /** 房间描述 */
        private String description;
        /** 是否是传送房间 */
        private boolean isTransporter;
        /** 出口 Map: direction -> neighborRoomId */
        private Map<String, String> exits;
        /** 房间内物品列表 */
        private List<ItemDTO> items;

        public RoomDTO() {
            exits = new LinkedHashMap<>();
            items = new ArrayList<>();
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isTransporter() { return isTransporter; }
        public void setTransporter(boolean transporter) { isTransporter = transporter; }
        public Map<String, String> getExits() { return exits; }
        public void setExits(Map<String, String> exits) { this.exits = exits; }
        public List<ItemDTO> getItems() { return items; }
        public void setItems(List<ItemDTO> items) { this.items = items; }
    }

    // ---- Getters & Setters ----

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(String currentRoomId) { this.currentRoomId = currentRoomId; }
    public double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(double maxWeight) { this.maxWeight = maxWeight; }
    public List<ItemDTO> getInventory() { return inventory; }
    public void setInventory(List<ItemDTO> inventory) { this.inventory = inventory; }
    public Map<String, RoomDTO> getRooms() { return rooms; }
    public void setRooms(Map<String, RoomDTO> rooms) { this.rooms = rooms; }
    public List<String> getHistoryRoomIds() { return historyRoomIds; }
    public void setHistoryRoomIds(List<String> historyRoomIds) { this.historyRoomIds = historyRoomIds; }
}