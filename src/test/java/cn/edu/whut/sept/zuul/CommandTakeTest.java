package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandTakeTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        // player 在 outside，初始 maxWeight = 20
    }

    @Test
    public void testTakeExistingItem() {
        String out = TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        assertTrue(out.contains("picked up") || out.contains("铁锹"));
        // 物品应在背包中
        assertNotNull(game.getPlayer().getItem("铁锹"));
        // 物品应不在房间中
        assertNull(game.getCurrentRoom().getItem("铁锹"));
    }

    @Test
    public void testTakeNonExistentItem() {
        String out = TestHelper.captureOutput(() -> game.runCommand("take 不存在物品"));
        assertTrue(out.contains("no item"));
    }

    @Test
    public void testTakeWithoutSecondWord() {
        String out = TestHelper.captureOutput(() -> game.runCommand("take"));
        assertTrue(out.contains("Take what?"));
    }

    @Test
    public void testTakeOverweight() {
        // 设置极小的 maxWeight 来测试超重
        Player p = game.getPlayer();
        // 绕过正常途径：先捡起所有可能物品使背包满
        // 更简单的方法：直接测试 Player.takeItem
        Item heavy = new Item("巨石", 100.0);
        assertFalse(p.takeItem(heavy));
    }

    @Test
    public void testTakeMultipleItems() {
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        TestHelper.captureOutput(() -> game.runCommand("take 地图"));
        assertEquals(2, game.getPlayer().getInventory().size());
    }

    @Test
    public void testTakeItemIncreasesWeight() {
        double before = game.getPlayer().getCurrentWeight();
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        double after = game.getPlayer().getCurrentWeight();
        assertTrue("拾取后重量应增加", after > before);
    }
}
