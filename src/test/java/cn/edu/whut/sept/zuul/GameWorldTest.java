package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameWorldTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testAllSixRoomsCreated() {
        java.util.Map<String, Room> map = game.getRoomIdMap();
        assertEquals(6, map.size());
        assertNotNull(map.get("outside"));
        assertNotNull(map.get("theater"));
        assertNotNull(map.get("pub"));
        assertNotNull(map.get("lab"));
        assertNotNull(map.get("office"));
        assertNotNull(map.get("transporter"));
    }

    @Test
    public void testExitsOutside() {
        Room outside = game.getRoomIdMap().get("outside");
        assertNotNull(outside.getExit("east"));
        assertNotNull(outside.getExit("south"));
        assertNotNull(outside.getExit("west"));
        assertNull(outside.getExit("north"));
    }

    @Test
    public void testExitsTheater() {
        Room theater = game.getRoomIdMap().get("theater");
        assertNotNull(theater.getExit("west"));
        assertEquals("outside", findRoomId(theater.getExit("west")));
    }

    @Test
    public void testExitsPub() {
        Room pub = game.getRoomIdMap().get("pub");
        assertNotNull(pub.getExit("east"));
        assertNotNull(pub.getExit("west"));
        assertEquals("transporter", findRoomId(pub.getExit("west")));
    }

    @Test
    public void testTransporterExit() {
        Room transporter = game.getRoomIdMap().get("transporter");
        assertNotNull(transporter.getExit("east"));
        assertEquals("pub", findRoomId(transporter.getExit("east")));
    }

    @Test
    public void testRoomsHaveItems() {
        // 除 transporter 外每个房间至少有一些物品
        Room outside = game.getRoomIdMap().get("outside");
        Room theater = game.getRoomIdMap().get("theater");
        Room pub = game.getRoomIdMap().get("pub");
        Room lab = game.getRoomIdMap().get("lab");
        Room office = game.getRoomIdMap().get("office");

        assertFalse("outside 应有物品", outside.getItems().isEmpty());
        assertFalse("theater 应有物品", theater.getItems().isEmpty());
        assertFalse("pub 应有物品", pub.getItems().isEmpty());
        assertFalse("lab 应有物品", lab.getItems().isEmpty());
        assertFalse("office 应有物品", office.getItems().isEmpty());
    }

    @Test
    public void testTransporterNotInAllRooms() {
        // 通过 getRandomRoom() 验证，返回的房间不应是 TransporterRoom
        for (int i = 0; i < 50; i++) {
            Room room = game.getRandomRoom();
            assertFalse("随机房间不应是传送房间", room instanceof TransporterRoom);
        }
    }

    @Test
    public void testPlayerInitialState() {
        assertEquals(20.0, game.getPlayer().getMaxWeight(), 0.001);
        assertTrue(game.getPlayer().getCurrentRoom().getRawDescription().contains("outside"));
    }

    /** 根据房间引用查找ID */
    private String findRoomId(Room room) {
        return game.findRoomId(room);
    }
}
