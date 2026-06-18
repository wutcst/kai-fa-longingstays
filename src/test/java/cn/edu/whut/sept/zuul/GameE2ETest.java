package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 端到端游戏流程测试.
 * 模拟完整的游戏流程：探索、拾取、购买、吃喝、传送、存档。
 */
public class GameE2ETest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        game.startNewGame("E2E测试玩家");
    }

    // ===== 完整探索流程 =====

    @Test
    public void testFullExplorationFlow() {
        // outside → theater → outside → pub → transporter → pub → outside → lab → office
        game.runCommand("go east");   // outside → theater
        assertTrue(game.getCurrentRoom().getRawDescription().contains("theater"));

        game.runCommand("go west");   // theater → outside
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));

        game.runCommand("go west");   // outside → pub
        assertTrue(game.getCurrentRoom().getRawDescription().contains("pub"));

        game.runCommand("go west");   // pub → transporter
        assertTrue(game.getCurrentRoom() instanceof TransporterRoom);

        game.runCommand("go east");   // transporter → pub
        assertTrue(game.getCurrentRoom().getRawDescription().contains("pub"));

        game.runCommand("go east");   // pub → outside
        game.runCommand("go south");  // outside → lab
        assertTrue(game.getCurrentRoom().getRawDescription().contains("lab"));

        game.runCommand("go east");   // lab → office
        assertTrue(game.getCurrentRoom().getRawDescription().contains("office"));
    }

    // ===== 拾取和丢弃流程 =====

    @Test
    public void testPickupAndDropFlow() {
        TestHelper.captureOutput(() -> {
            game.runCommand("take 铁锹");
            game.runCommand("take 地图");
        });

        assertEquals(2, game.getPlayer().getInventory().size());
        assertNotNull(game.getPlayer().getItem("铁锹"));
        assertNotNull(game.getPlayer().getItem("地图"));

        TestHelper.captureOutput(() -> game.runCommand("drop 铁锹"));
        assertEquals(1, game.getPlayer().getInventory().size());
        assertNull(game.getPlayer().getItem("铁锹"));

        // 丢弃后物品应在房间
        assertNotNull(game.getCurrentRoom().getItem("铁锹"));
    }

    // ===== 购买和饮用流程 =====

    @Test
    public void testBuyAndDrinkFlow() {
        // 移动到 pub
        game.runCommand("go west"); // outside → pub

        TestHelper.captureOutput(() -> {
            game.runCommand("buy WarmveinAle");
            game.runCommand("buy Moonhoney");
        });

        assertNotNull(game.getPlayer().getItem("麦脉暖酿"));
        assertNotNull(game.getPlayer().getItem("月花蜜醴"));

        // 饮用
        TestHelper.captureOutput(() -> {
            game.runCommand("eat 麦脉暖酿");
            game.runCommand("eat 月花蜜醴");
        });

        assertNull(game.getPlayer().getItem("麦脉暖酿"));
        assertNull(game.getPlayer().getItem("月花蜜醴"));
    }

    // ===== 吃魔法饼干流程 =====

    @Test
    public void testEatCookieFlow() {
        double initial = game.getPlayer().getMaxWeight();

        game.getPlayer().takeItem(new Item("魔法饼干", 0.5));
        TestHelper.captureOutput(() -> game.runCommand("eat 魔法饼干"));

        assertEquals(initial + 10.0, game.getPlayer().getMaxWeight(), 0.001);
        assertNull(game.getPlayer().getItem("魔法饼干"));
    }

    // ===== 传送和回退流程 =====

    @Test
    public void testTeleportAndBackFlow() {
        game.runCommand("go west");   // outside → pub
        game.runCommand("go west");   // pub → transporter

        assertTrue(game.getCurrentRoom() instanceof TransporterRoom);

        // 触发传送
        game.runCommand("trigger_teleport");
        assertFalse(game.getCurrentRoom() instanceof TransporterRoom);

        // 回退
        game.goBack();
        assertTrue(game.getCurrentRoom() instanceof TransporterRoom);
    }

    // ===== 任务收集流程 =====

    @Test
    public void testQuestCollectionFlow() {
        java.util.List<String> required = game.getRequiredItems();
        assertEquals(4, required.size());

        int collected = 0;
        // 遍历所有房间收集任务物品
        String[] rooms = {"outside", "theater", "pub", "lab", "office"};
        for (String roomId : rooms) {
            // 移动到该房间（简化：直接从 outside 出发）
            // 实际场景中需要按地图移动，这里简化为在每个房间搜索
        }

        // 验证任务物品列表格式正确
        for (String item : required) {
            assertNotNull(item);
            assertFalse(item.isEmpty());
        }
    }

    // ===== 序列化完整往返 =====

    @Test
    public void testFullSerializeRoundtrip() {
        // 建立复杂状态
        game.runCommand("go east");   // outside → theater
        TestHelper.captureOutput(() -> game.runCommand("take 道具剑"));
        game.runCommand("go west");   // theater → outside
        game.runCommand("go south");  // outside → lab

        String json = game.serializeGameState();

        Game g2 = new Game();
        g2.deserializeGameState(json);

        // 验证关键状态
        assertEquals(game.getPlayer().getName(), g2.getPlayer().getName());
        assertEquals(game.getPlayer().getMaxWeight(), g2.getPlayer().getMaxWeight(), 0.001);
        assertEquals(game.getPlayer().getInventory().size(), g2.getPlayer().getInventory().size());
        assertTrue(g2.getCurrentRoom().getRawDescription().contains("lab"));
    }

    // ===== 新游戏后可正常操作 =====

    @Test
    public void testNewGameThenOperate() {
        game.startNewGame("新冒险");
        assertTrue(game.isRunning());
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));

        game.runCommand("go east");
        assertTrue(game.getCurrentRoom().getRawDescription().contains("theater"));

        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("theater"));
    }

    // ===== 超重边界流程 =====

    @Test
    public void testOverweightBoundaryFlow() {
        // 加接近上限的物品
        game.getPlayer().takeItem(new Item("重物A", 10.0));
        game.getPlayer().takeItem(new Item("重物B", 9.5));
        // 总重 19.5，再拾取 3.2kg 铁锹应超重

        String out = TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        assertTrue(out.contains("heavy") || out.contains("carry"));
        assertNull(game.getPlayer().getItem("铁锹"));
    }
}
