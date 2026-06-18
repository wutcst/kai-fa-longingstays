package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandBuyTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        // 玩家初始在 outside，需移动到 pub
        game.runCommand("go west"); // outside → pub
    }

    // ===== 正常购买 =====

    @Test
    public void testBuyWarmveinAleInPub() {
        String out = TestHelper.captureOutput(() -> game.runCommand("buy WarmveinAle"));
        assertTrue(out.contains("bought") || out.contains("麦脉暖酿"));
        assertNotNull(game.getPlayer().getItem("麦脉暖酿"));
    }

    @Test
    public void testBuyMoonhoneyInPub() {
        String out = TestHelper.captureOutput(() -> game.runCommand("buy Moonhoney"));
        assertTrue(out.contains("bought") || out.contains("月花蜜醴"));
        assertNotNull(game.getPlayer().getItem("月花蜜醴"));
    }

    @Test
    public void testBuyWithChinesePartialName() {
        TestHelper.captureOutput(() -> game.runCommand("buy 麦脉"));
        assertNotNull(game.getPlayer().getItem("麦脉暖酿"));
    }

    @Test
    public void testBuyWarmveinAleWeight() {
        TestHelper.captureOutput(() -> game.runCommand("buy WarmveinAle"));
        assertEquals(0.5, game.getPlayer().getCurrentWeight(), 0.01);
    }

    @Test
    public void testBuyMoonhoneyWeight() {
        TestHelper.captureOutput(() -> game.runCommand("buy Moonhoney"));
        assertEquals(0.4, game.getPlayer().getCurrentWeight(), 0.01);
    }

    // ===== 位置限制 =====

    @Test
    public void testBuyOutsidePub() {
        // 移回 outside
        game.runCommand("go east"); // pub → outside
        String out = TestHelper.captureOutput(() -> game.runCommand("buy WarmveinAle"));
        assertTrue(out.contains("only buy drinks in the pub"));
        assertNull(game.getPlayer().getItem("麦脉暖酿"));
    }

    // ===== 无效商品 =====

    @Test
    public void testBuyInvalidDrink() {
        String out = TestHelper.captureOutput(() -> game.runCommand("buy 可乐"));
        assertTrue(out.contains("doesn't sell"));
    }

    // ===== 参数缺失 =====

    @Test
    public void testBuyWithoutSecondWord() {
        String out = TestHelper.captureOutput(() -> game.runCommand("buy"));
        assertTrue(out.contains("Buy what?"));
    }

    // ===== 超重 =====

    @Test
    public void testBuyOverweight() {
        // 先让背包接近满载
        game.getPlayer().takeItem(new Item("重物A", 8.0));
        game.getPlayer().takeItem(new Item("重物B", 8.0));
        game.getPlayer().takeItem(new Item("重物C", 3.7));
        // 总重 19.7，再加 0.5 超重
        String out = TestHelper.captureOutput(() -> game.runCommand("buy WarmveinAle"));
        assertTrue(out.contains("too heavy") || out.contains("can't carry"));
        assertNull(game.getPlayer().getItem("麦脉暖酿"));
    }

    @Test
    public void testBuyMultipleSame() {
        TestHelper.captureOutput(() -> game.runCommand("buy WarmveinAle"));
        TestHelper.captureOutput(() -> game.runCommand("buy WarmveinAle"));
        // 两个物品应在背包中
        java.util.ArrayList<Item> inv = game.getPlayer().getInventory();
        long count = inv.stream().filter(i -> i.getDescription().contains("麦脉暖酿")).count();
        assertEquals(2, count);
    }
}
