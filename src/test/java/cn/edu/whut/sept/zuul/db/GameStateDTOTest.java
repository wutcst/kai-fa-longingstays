package cn.edu.whut.sept.zuul.db;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateDTOTest {
    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    // ===== GameStateDTO 基础序列化 =====

    @Test
    public void testGsonRoundtrip() {
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("TestPlayer");
        dto.setCurrentRoomId("outside");
        dto.setMaxWeight(20.0);

        String json = gson.toJson(dto);
        GameStateDTO dto2 = gson.fromJson(json, GameStateDTO.class);

        assertEquals("TestPlayer", dto2.getPlayerName());
        assertEquals("outside", dto2.getCurrentRoomId());
        assertEquals(20.0, dto2.getMaxWeight(), 0.001);
    }

    @Test
    public void testDefaultConstructor() {
        GameStateDTO dto = new GameStateDTO();
        assertNotNull(dto.getInventory());
        assertNotNull(dto.getRooms());
        assertNotNull(dto.getHistoryRoomIds());
        assertNotNull(dto.getRequiredItems());
        assertTrue(dto.getInventory().isEmpty());
        assertTrue(dto.getRooms().isEmpty());
    }

    // ===== ItemDTO =====

    @Test
    public void testItemDTOSerialization() {
        GameStateDTO.ItemDTO item = new GameStateDTO.ItemDTO("道具剑", 3.0, 10.5, 20.5);
        String json = gson.toJson(item);
        assertTrue(json.contains("道具剑"));
        assertTrue(json.contains("3.0"));

        GameStateDTO.ItemDTO item2 = gson.fromJson(json, GameStateDTO.ItemDTO.class);
        assertEquals("道具剑", item2.getDescription());
        assertEquals(3.0, item2.getWeight(), 0.001);
        assertEquals(10.5, item2.getX(), 0.001);
        assertEquals(20.5, item2.getY(), 0.001);
    }

    @Test
    public void testItemDTODefaultConstructor() {
        GameStateDTO.ItemDTO item = new GameStateDTO.ItemDTO();
        assertNull(item.getDescription());
        assertEquals(0.0, item.getWeight(), 0.001);
        assertEquals(0.0, item.getX(), 0.001);
        assertEquals(0.0, item.getY(), 0.001);
    }

    @Test
    public void testItemDTOSetters() {
        GameStateDTO.ItemDTO item = new GameStateDTO.ItemDTO();
        item.setDescription("测试");
        item.setWeight(5.5);
        item.setX(30.0);
        item.setY(40.0);

        assertEquals("测试", item.getDescription());
        assertEquals(5.5, item.getWeight(), 0.001);
        assertEquals(30.0, item.getX(), 0.001);
        assertEquals(40.0, item.getY(), 0.001);
    }

    // ===== RoomDTO =====

    @Test
    public void testRoomDTOSerialization() {
        GameStateDTO.RoomDTO room = new GameStateDTO.RoomDTO();
        room.setId("pub");
        room.setDescription("in the campus pub");
        room.setTransporter(false);
        room.getExits().put("east", "outside");
        room.getExits().put("west", "transporter");

        String json = gson.toJson(room);
        assertTrue(json.contains("pub"));
        assertTrue(json.contains("campus pub"));
        assertTrue(json.contains("east"));
        assertTrue(json.contains("outside"));

        GameStateDTO.RoomDTO room2 = gson.fromJson(json, GameStateDTO.RoomDTO.class);
        assertEquals("pub", room2.getId());
        assertEquals("in the campus pub", room2.getDescription());
        assertFalse(room2.isTransporter());
        assertEquals("outside", room2.getExits().get("east"));
    }

    @Test
    public void testRoomDTOIsTransporter() {
        GameStateDTO.RoomDTO room = new GameStateDTO.RoomDTO();
        room.setId("transporter");
        room.setDescription("mysterious room");
        room.setTransporter(true);

        String json = gson.toJson(room);
        GameStateDTO.RoomDTO room2 = gson.fromJson(json, GameStateDTO.RoomDTO.class);
        assertTrue(room2.isTransporter());
    }

    @Test
    public void testRoomDTODefaultConstructor() {
        GameStateDTO.RoomDTO room = new GameStateDTO.RoomDTO();
        assertNotNull(room.getExits());
        assertNotNull(room.getItems());
        assertTrue(room.getExits().isEmpty());
        assertTrue(room.getItems().isEmpty());
        assertFalse(room.isTransporter());
    }

    @Test
    public void testRoomDTOWithItems() {
        GameStateDTO.RoomDTO room = new GameStateDTO.RoomDTO();
        room.setId("lab");
        room.getItems().add(new GameStateDTO.ItemDTO("显微镜", 4.7, 25.0, 55.0));
        room.getItems().add(new GameStateDTO.ItemDTO("魔法饼干", 0.5, 42.0, 72.0));

        String json = gson.toJson(room);
        GameStateDTO.RoomDTO room2 = gson.fromJson(json, GameStateDTO.RoomDTO.class);

        assertEquals(2, room2.getItems().size());
        assertEquals("显微镜", room2.getItems().get(0).getDescription());
        assertEquals("魔法饼干", room2.getItems().get(1).getDescription());
    }

    // ===== null 字段 =====

    @Test
    public void testNullPlayerName() {
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName(null);
        dto.setCurrentRoomId("outside");
        dto.setMaxWeight(20.0);

        String json = gson.toJson(dto);
        GameStateDTO dto2 = gson.fromJson(json, GameStateDTO.class);
        assertNull(dto2.getPlayerName());
    }

    @Test
    public void testNullInventory() {
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("Test");
        dto.setCurrentRoomId("outside");
        dto.setMaxWeight(20.0);
        dto.setInventory(null);

        String json = gson.toJson(dto);
        GameStateDTO dto2 = gson.fromJson(json, GameStateDTO.class);
        // Gson 默认将 null 列表反序列化为空列表（已知行为）
        assertNotNull(dto2.getInventory());
        assertTrue(dto2.getInventory().isEmpty());
    }

    // ===== requiredItems 字段 =====

    @Test
    public void testRequiredItemsRoundtrip() {
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("Test");
        dto.setCurrentRoomId("outside");
        dto.setMaxWeight(20.0);
        dto.getRequiredItems().add("生锈的铁锹");
        dto.getRequiredItems().add("破旧的地图");
        dto.getRequiredItems().add("空鸟巢");
        dto.getRequiredItems().add("魔法饼干");

        String json = gson.toJson(dto);
        GameStateDTO dto2 = gson.fromJson(json, GameStateDTO.class);

        assertEquals(4, dto2.getRequiredItems().size());
        assertTrue(dto2.getRequiredItems().contains("生锈的铁锹"));
    }

    // ===== historyRoomIds 字段 =====

    @Test
    public void testHistoryRoomIdsRoundtrip() {
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName("Test");
        dto.setCurrentRoomId("lab");
        dto.setMaxWeight(20.0);
        dto.getHistoryRoomIds().add("outside");
        dto.getHistoryRoomIds().add("theater");

        String json = gson.toJson(dto);
        GameStateDTO dto2 = gson.fromJson(json, GameStateDTO.class);

        assertEquals(2, dto2.getHistoryRoomIds().size());
        assertEquals("outside", dto2.getHistoryRoomIds().get(0));
        assertEquals("theater", dto2.getHistoryRoomIds().get(1));
    }

    // ===== 浮点精度 =====

    @Test
    public void testFloatPrecision() {
        GameStateDTO.ItemDTO item = new GameStateDTO.ItemDTO("精密物品", 0.1 + 0.2, 1.0 / 3.0, 0.0);
        String json = gson.toJson(item);
        GameStateDTO.ItemDTO dto = gson.fromJson(json, GameStateDTO.ItemDTO.class);

        // 0.1 + 0.2 ≈ 0.30000000000000004
        assertTrue(Math.abs(dto.getWeight() - 0.3) < 0.001);
    }
}
