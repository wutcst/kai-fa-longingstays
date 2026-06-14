package cn.edu.whut.sept.zuul.db;

import javax.persistence.*;

/**
 * 游戏存档实体类.
 * 使用 Hibernate ORM 映射到 SQLite 数据库.
 * 将游戏状态序列化为 JSON 文本存储，不依赖数据库 JSON 查询功能.
 */
@Entity
@Table(name = "game_saves")
public class GameSaveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 存档名称（例如 "主线存档1"） */
    @Column(name = "save_name", nullable = false, length = 100)
    private String saveName;

    /** 玩家昵称 */
    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    /** 游戏状态 JSON（包含所有地图、房间、物品、玩家状态等数据的序列化） */
    @Column(name = "game_state_json", nullable = false, columnDefinition = "TEXT")
    private String gameStateJson;

    /** 创建时间（时间戳） */
    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    /** 存档大小（JSON 字节数，仅供参考） */
    @Column(name = "data_size")
    private Integer dataSize;

    public GameSaveEntity() {
    }

    public GameSaveEntity(String saveName, String playerName, String gameStateJson) {
        this.saveName = saveName;
        this.playerName = playerName;
        this.gameStateJson = gameStateJson;
        this.createdAt = System.currentTimeMillis();
        this.dataSize = gameStateJson != null ? gameStateJson.length() : 0;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameStateJson() {
        return gameStateJson;
    }

    public void setGameStateJson(String gameStateJson) {
        this.gameStateJson = gameStateJson;
        this.dataSize = gameStateJson != null ? gameStateJson.length() : 0;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }
}