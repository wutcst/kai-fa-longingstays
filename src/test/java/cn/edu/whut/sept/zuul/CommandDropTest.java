package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandDropTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        // 先拾取道具剑以便丢弃
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
    }

    @Test
    public void testDropExistingItem() {
        String out = TestHelper.captureOutput(() -> game.runCommand("drop 铁锹"));
        assertTrue(out.contains("dropped"));
        assertNull(game.getPlayer().getItem("铁锹"));
        assertNotNull(game.getCurrentRoom().getItem("铁锹"));
    }

    @Test
    public void testDropWithCoordinates() {
        TestHelper.captureOutput(() -> game.runCommand("drop 铁锹 30 70"));
        Item item = game.getCurrentRoom().getItem("铁锹");
        assertNotNull(item);
        assertEquals(30.0, item.getX(), 0.01);
        assertEquals(70.0, item.getY(), 0.01);
    }

    @Test
    public void testDropWithoutSecondWord() {
        String out = TestHelper.captureOutput(() -> game.runCommand("drop"));
        assertTrue(out.contains("Drop what?"));
    }

    @Test
    public void testDropNonExistentItem() {
        String out = TestHelper.captureOutput(() -> game.runCommand("drop 不存在"));
        assertTrue(out.contains("don't have"));
    }

    @Test
    public void testDropReduceWeight() {
        double before = game.getPlayer().getCurrentWeight();
        TestHelper.captureOutput(() -> game.runCommand("drop 铁锹"));
        double after = game.getPlayer().getCurrentWeight();
        assertTrue("丢弃后重量应减少", after < before);
    }

    @Test
    public void testDropLastItemLeavesEmptyInventory() {
        TestHelper.captureOutput(() -> game.runCommand("drop 铁锹"));
        assertEquals(0.0, game.getPlayer().getCurrentWeight(), 0.001);
        assertTrue(game.getPlayer().getInventory().isEmpty());
    }
}
