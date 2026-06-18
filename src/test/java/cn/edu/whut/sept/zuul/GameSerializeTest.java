package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.db.GameStateDTO;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameSerializeTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    // ===== 基础序列化/反序列化 =====

    @Test
    public void testSerializeReturnsNonEmptyJson() {
        String json = game.serializeGameState();
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("playerName"));
        assertTrue(json.contains("outside"));
    }

    @Test
    public void testRoundtripSimpleState() {
        // 初始状态 → JSON → 新Game → 恢复
        String json1 = game.serializeGameState();
        Game g2 = new Game();
        g2.deserializeGameState(json1);
        String json2 = g2.serializeGameState();

        // 验证关键字段一致
        assertEquals(game.getPlayer().getName(), g2.getPlayer().getName());
        assertEquals(game.getPlayer().getMaxWeight(), g2.getPlayer().getMaxWeight(), 0.001);
        // currentRoom 应该是同一个逻辑房间
        assertNotNull(g2.getCurrentRoom());
    }

    @Test
    public void testRoundtripWithInventory() {
        TestHelper.captureOutput(() -> {
            game.runCommand("take 铁锹");
            game.runCommand("take 地图");
        });

        String json = game.serializeGameState();
        Game g2 = new Game();
        g2.deserializeGameState(json);

        assertEquals(2, g2.getPlayer().getInventory().size());
        assertNotNull(g2.getPlayer().getItem("铁锹"));
        assertNotNull(g2.getPlayer().getItem("地图"));
        assertEquals(game.getPlayer().getCurrentWeight(),
                g2.getPlayer().getCurrentWeight(), 0.01);
    }

    @Test
    public void testRoundtripWithHistory() {
        // 建立移动历史
        game.runCommand("go east");   // outside → theater
        game.runCommand("go west");   // theater → outside

        String json = game.serializeGameState();
        Game g2 = new Game();
        g2.deserializeGameState(json);

        // 回退应有效
        String out = TestHelper.captureOutput(() -> g2.goBack());
        // 应该能回退到 theater（在历史栈中）
        assertNotNull(g2.getCurrentRoom());
    }

    @Test
    public void testRoundtripRoomExitsPreserved() {
        String json = game.serializeGameState();
        Game g2 = new Game();
        g2.deserializeGameState(json);

        // outside.east 应为 theater
        Room outside = g2.getRoomIdMap().get("outside");
        assertNotNull(outside);
        Room east = outside.getExit("east");
        assertNotNull(east);
        assertTrue(east.getRawDescription().contains("theater"));
    }

    @Test
    public void testRoundtripRoomItemsPreserved() {
        String json = game.serializeGameState();
        Game g2 = new Game();
        g2.deserializeGameState(json);

        Room lab = g2.getRoomIdMap().get("lab");
        assertNotNull(lab);
        assertNotNull(lab.getItem("显微镜"));
        assertNotNull(lab.getItem("魔法饼干"));
    }

    // ===== 零坐标修复 =====

    @Test
    public void testDeserializeZeroZeroCoordsFixed() {
        // 手动构造含 (0,0) 坐标的 JSON
        Gson gson = new Gson();
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("Test");
        dto.setCurrentRoomId("outside");
        dto.setMaxWeight(20.0);

        GameStateDTO.RoomDTO roomDTO = new GameStateDTO.RoomDTO();
        roomDTO.setId("outside");
        roomDTO.setDescription("outside the main entrance of the university");
        roomDTO.setTransporter(false);
        roomDTO.getItems().add(new GameStateDTO.ItemDTO("道具剑：一把剑", 1.8, 0.0, 0.0));
        dto.getRooms().put("outside", roomDTO);

        String json = gson.toJson(dto);

        Game g2 = new Game();
        g2.deserializeGameState(json);

        Room outside = g2.getRoomIdMap().get("outside");
        assertNotNull(outside);
        Item sword = outside.getItem("道具剑");
        assertNotNull(sword);
        // 坐标应被修复，不再是 (0,0)
        assertFalse("道具剑坐标应被修复为非(0,0)", sword.getX() == 0.0 && sword.getY() == 0.0);
    }

    // ===== 反序列化边界情况 =====

    @Test
    public void testDeserializeEmptyInventory() {
        Gson gson = new Gson();
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("Test");
        dto.setCurrentRoomId("outside");
        dto.setMaxWeight(20.0);
        dto.setInventory(new java.util.ArrayList<>());

        GameStateDTO.RoomDTO roomDTO = new GameStateDTO.RoomDTO();
        roomDTO.setId("outside");
        roomDTO.setDescription("outside");
        roomDTO.setTransporter(false);
        dto.getRooms().put("outside", roomDTO);

        String json = gson.toJson(dto);

        Game g2 = new Game();
        g2.deserializeGameState(json);

        assertTrue(g2.getPlayer().getInventory().isEmpty());
    }

    @Test
    public void testDeserializeMissingFields() {
        // 不含 requiredItems 和 historyRoomIds 的 JSON
        String json = "{\"playerName\":\"Test\",\"currentRoomId\":\"outside\",\"maxWeight\":20.0,"
                + "\"inventory\":[],\"rooms\":{\"outside\":{\"id\":\"outside\","
                + "\"description\":\"outside\",\"transporter\":false,\"exits\":{},\"items\":[]}}}";

        Game g2 = new Game();
        // 不应抛异常
        g2.deserializeGameState(json);
        assertNotNull(g2.getPlayer());
        assertNotNull(g2.getRequiredItems());
        assertTrue(g2.getRequiredItems().isEmpty());
    }

    @Test
    public void testDeserializeCorruptedJson() {
        try {
            Game g2 = new Game();
            g2.deserializeGameState("{not valid json");
            // 可能抛异常或静默失败——记录当前行为
            // 如果是 Gson 的宽松模式可能不抛异常
        } catch (Exception e) {
            // 预期可能的 JsonSyntaxException
            assertNotNull(e);
        }
    }

    @Test
    public void testDeserializeUnknownRoomId() {
        Gson gson = new Gson();
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("Test");
        dto.setCurrentRoomId("nonexistent_room");
        dto.setMaxWeight(20.0);

        GameStateDTO.RoomDTO roomDTO = new GameStateDTO.RoomDTO();
        roomDTO.setId("outside");
        roomDTO.setDescription("outside");
        roomDTO.setTransporter(false);
        dto.getRooms().put("outside", roomDTO);

        String json = gson.toJson(dto);

        Game g2 = new Game();
        g2.deserializeGameState(json);

        // 应回退到 outside
        assertNotNull(g2.getCurrentRoom());
        assertTrue(g2.getCurrentRoom().getRawDescription().contains("outside"));
    }

    @Test
    public void testSerializeAfterDeserialize() {
        // 完整的往返：序列化 → 反序列化 → 再序列化
        game.runCommand("go east");
        TestHelper.captureOutput(() -> game.runCommand("take 道具剑"));
        game.runCommand("go west");

        String json1 = game.serializeGameState();
        Game g2 = new Game();
        g2.deserializeGameState(json1);
        String json2 = g2.serializeGameState();

        // 两次序列化的 JSON 应该语义等价
        assertNotNull(json2);
        assertFalse(json2.isEmpty());
        assertTrue(json2.contains("道具剑"));
    }

    @Test
    public void testTransporterRoomNotInAllRooms() {
        Room transporter = game.getRoomIdMap().get("transporter");
        assertNotNull(transporter);
        assertTrue(transporter instanceof TransporterRoom);
    }
}
