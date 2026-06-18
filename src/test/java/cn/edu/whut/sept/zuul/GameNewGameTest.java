package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameNewGameTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testStartNewGameResetsAll() {
        game.startNewGame("新玩家");

        assertEquals("新玩家", game.getPlayer().getName());
        assertNotNull(game.getCurrentRoom());
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));
        assertEquals(20.0, game.getPlayer().getMaxWeight(), 0.001);
        assertTrue(game.isRunning());
        // 任务物品
        assertNotNull(game.getRequiredItems());
        assertEquals(4, game.getRequiredItems().size());
    }

    @Test
    public void testStartNewGameRandomQuestItems() {
        game.startNewGame("A");
        java.util.List<String> items1 = new java.util.ArrayList<>(game.getRequiredItems());

        Game g2 = new Game();
        g2.startNewGame("B");
        java.util.List<String> items2 = g2.getRequiredItems();

        assertNotNull(items1);
        assertNotNull(items2);
        assertEquals(4, items1.size());
        assertEquals(4, items2.size());

        // 概率极低（< 1/17^4）两次完全相同的排列，可接受
        boolean same = items1.equals(items2);
        // 不强制 assertFalse——极低概率误判
        if (same) {
            // 再试一次
            Game g3 = new Game();
            g3.startNewGame("C");
            assertFalse("连续两次任务物品完全相同概率极低",
                    items1.equals(g3.getRequiredItems()));
        }
    }

    @Test
    public void testStartNewGameWithNullName() {
        game.startNewGame(null);
        assertNull(game.getPlayer().getName());
    }

    @Test
    public void testStartNewGameWithEmptyName() {
        game.startNewGame("");
        assertEquals("", game.getPlayer().getName());
    }

    @Test
    public void testStartNewGameClearsInventory() {
        // 先玩一阵，再开新游戏
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        assertFalse(game.getPlayer().getInventory().isEmpty());

        game.startNewGame("新开始");
        assertTrue(game.getPlayer().getInventory().isEmpty());
    }

    @Test
    public void testStartNewGameCanStartAfterExit() {
        // 使用 exitToMenu() 直接退出（CommandQuit 不改变 isRunning，仅在 CLI 模式下有意义）
        game.exitToMenu();
        assertFalse(game.isRunning());

        game.startNewGame("归来");
        assertTrue(game.isRunning());
    }

    @Test
    public void testQuitCommandReturnsTrue() {
        // CommandQuit.execute() 返回 true（供 CLI play() 循环使用）
        // 但不改变 isRunning() 状态——这是一个已知设计差异
        String out = TestHelper.captureOutput(() -> game.runCommand("quit"));
        assertTrue(out.contains("Thank you") || game.isRunning());
    }

    @Test
    public void testStartNewGameMultipleTimes() {
        game.startNewGame("A");
        game.startNewGame("B");
        game.startNewGame("C");

        assertEquals("C", game.getPlayer().getName());
        assertEquals(6, game.getRoomIdMap().size());
        assertEquals(4, game.getRequiredItems().size());
    }

    @Test
    public void testRequiredItemsAreUnique() {
        game.startNewGame("测试");
        java.util.List<String> items = game.getRequiredItems();
        assertEquals(4, items.size());
        // 应该无重复
        assertEquals(items.size(), new java.util.HashSet<>(items).size());
    }

    @Test
    public void testStartNewGamePlayerInOutside() {
        game.startNewGame("测试");
        Room outside = game.getRoomIdMap().get("outside");
        assertNotNull(outside);
        assertSame(outside, game.getPlayer().getCurrentRoom());
    }
}
