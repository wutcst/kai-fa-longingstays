package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandLookTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testLookShowsRoomDescription() {
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("outside") || out.contains("entrance"));
    }

    @Test
    public void testLookShowsExits() {
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("Exits"));
        assertTrue(out.contains("east"));
        assertTrue(out.contains("south"));
        assertTrue(out.contains("west"));
    }

    @Test
    public void testLookShowsRoomItems() {
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        // outside 有铁锹、地图、空鸟巢、鹅卵石堆
        assertTrue(out.contains("Items in room") || out.contains("铁锹") || out.contains("地图"));
    }

    @Test
    public void testLookShowsInventory() {
        // 先拾取一个物品
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("铁锹"));
    }

    @Test
    public void testLookWithExtraParams() {
        // look 忽略多余参数
        String out = TestHelper.captureOutput(() -> game.runCommand("look around"));
        assertTrue(out.contains("outside") || out.contains("entrance"));
    }

    @Test
    public void testLookEmptyInventory() {
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("not carrying anything") || out.contains("carrying"));
    }

    @Test
    public void testLookInDifferentRoom() {
        game.runCommand("go east"); // outside → theater
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("theater"));
    }
}
