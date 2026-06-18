package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandEatTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        // 初始 player maxWeight = 20
    }

    // ===== 吃魔法饼干 =====

    @Test
    public void testEatMagicCookie() {
        // 先把饼干加入背包
        game.getPlayer().takeItem(new Item("魔法饼干", 0.5));
        double before = game.getPlayer().getMaxWeight();

        String out = TestHelper.captureOutput(() -> game.runCommand("eat 魔法饼干"));
        assertTrue(out.contains("max carry weight") || out.contains("stronger"));
        assertTrue(game.getPlayer().getMaxWeight() > before);
        // 饼干应从背包移除
        assertNull(game.getPlayer().getItem("魔法饼干"));
    }

    @Test
    public void testEatCookieTwice() {
        game.getPlayer().takeItem(new Item("魔法饼干", 0.5));
        game.getPlayer().takeItem(new Item("魔法饼干", 0.5));
        double initial = game.getPlayer().getMaxWeight();

        game.runCommand("eat 魔法饼干");
        game.runCommand("eat 魔法饼干");

        assertEquals(initial + 20.0, game.getPlayer().getMaxWeight(), 0.001);
    }

    // ===== 饮用麦脉暖酿 =====

    @Test
    public void testDrinkWarmveinAle() {
        game.getPlayer().takeItem(new Item("麦脉暖酿", 0.5));
        String out = TestHelper.captureOutput(() -> game.runCommand("eat 麦脉暖酿"));
        assertTrue(out.contains("HP +20") || out.contains("drank"));
        assertNull(game.getPlayer().getItem("麦脉暖酿"));
    }

    // ===== 饮用月花蜜醴 =====

    @Test
    public void testDrinkMoonhoney() {
        game.getPlayer().takeItem(new Item("月花蜜醴", 0.4));
        String out = TestHelper.captureOutput(() -> game.runCommand("eat 月花蜜醴"));
        assertTrue(out.contains("MP +20") || out.contains("drank"));
        assertNull(game.getPlayer().getItem("月花蜜醴"));
    }

    // ===== 不可食用物品 =====

    @Test
    public void testEatNonEdibleItem() {
        game.getPlayer().takeItem(new Item("道具剑", 3.0));
        String out = TestHelper.captureOutput(() -> game.runCommand("eat 道具剑"));
        assertTrue(out.contains("can't eat"));
        // 物品应仍在背包中
        assertNotNull(game.getPlayer().getItem("道具剑"));
    }

    // ===== 参数缺失 =====

    @Test
    public void testEatWithoutSecondWord() {
        String out = TestHelper.captureOutput(() -> game.runCommand("eat"));
        assertTrue(out.contains("Eat what?"));
    }

    // ===== 物品不在背包 =====

    @Test
    public void testEatNotOwned() {
        String out = TestHelper.captureOutput(() -> game.runCommand("eat 魔法饼干"));
        assertTrue(out.contains("don't have"));
    }

    // ===== 部分名称匹配 =====

    @Test
    public void testEatPartialNameMatch() {
        game.getPlayer().takeItem(new Item("麦脉暖酿", 0.5));
        String out = TestHelper.captureOutput(() -> game.runCommand("eat 麦脉"));
        assertTrue(out.contains("HP +20") || out.contains("drank"));
    }

    @Test
    public void testEatPartialNameMatchMoonhoney() {
        game.getPlayer().takeItem(new Item("月花蜜醴", 0.4));
        String out = TestHelper.captureOutput(() -> game.runCommand("eat 月花"));
        assertTrue(out.contains("MP +20") || out.contains("drank"));
    }
}
