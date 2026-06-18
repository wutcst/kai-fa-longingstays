package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameTeleportTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    // ===== 传送房间存在性 =====

    @Test
    public void testTransporterRoomExists() {
        Room transporter = game.getRoomIdMap().get("transporter");
        assertNotNull(transporter);
        assertTrue(transporter instanceof TransporterRoom);
    }

    @Test
    public void testTransporterNotInAllRooms() {
        for (int i = 0; i < 100; i++) {
            Room room = game.getRandomRoom();
            assertFalse("随机房间不应是传送房间", room instanceof TransporterRoom);
        }
    }

    // ===== trigger_teleport 在传送房间内 =====

    @Test
    public void testTriggerTeleportInTransporterRoom() {
        // 先移动到 pub，再移动到 transporter
        game.runCommand("go west"); // outside → pub
        game.runCommand("go west"); // pub → transporter
        assertTrue(game.getCurrentRoom() instanceof TransporterRoom);

        game.runCommand("trigger_teleport");
        // 传送后不应在 transporter
        assertFalse(game.getCurrentRoom() instanceof TransporterRoom);
    }

    @Test
    public void testTriggerTeleportPushesHistory() {
        game.runCommand("go west"); // outside → pub
        game.runCommand("go west"); // pub → transporter

        String out = TestHelper.captureOutput(() -> game.runCommand("trigger_teleport"));
        assertTrue(out.contains("魔法阵") || out.contains("传送") || out.contains("空间扭曲"));

        // goBack 应回到 transporter
        game.goBack();
        assertTrue(game.getCurrentRoom() instanceof TransporterRoom);
    }

    // ===== trigger_teleport 在普通房间内 =====

    @Test
    public void testTriggerTeleportInNormalRoom() {
        Room before = game.getCurrentRoom();
        game.runCommand("trigger_teleport");
        // 在普通房间内 trigger_teleport 不应改变位置
        assertSame(before, game.getCurrentRoom());
    }

    // ===== getRandomRoom =====

    @Test
    public void testGetRandomRoomAlwaysInAllRooms() {
        java.util.Map<String, Room> map = game.getRoomIdMap();
        for (int i = 0; i < 100; i++) {
            Room room = game.getRandomRoom();
            assertNotNull(room);
            // 返回的房间应该是 5 个普通房间之一
            boolean found = false;
            for (Room r : map.values()) {
                if (r == room) { found = true; break; }
            }
            assertTrue("随机房间应在 roomIdMap 中", found);
        }
    }

    @Test
    public void testGetRandomRoomDiversity() {
        // 100 次中至少返回 2 个不同房间
        java.util.Set<Room> seen = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(game.getRandomRoom());
        }
        assertTrue("应返回至少 2 个不同房间", seen.size() >= 2);
    }
}
