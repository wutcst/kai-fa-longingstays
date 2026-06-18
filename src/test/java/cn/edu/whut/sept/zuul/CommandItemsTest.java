package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandItemsTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testItemsShowsRoomSection() {
        String out = TestHelper.captureOutput(() -> game.runCommand("items"));
        assertTrue(out.contains("Room Items"));
        assertTrue(out.contains("Items in room") || out.contains("铁锹") || out.contains("地图"));
    }

    @Test
    public void testItemsShowsRoomTotalWeight() {
        String out = TestHelper.captureOutput(() -> game.runCommand("items"));
        assertTrue(out.contains("Total weight in room"));
    }

    @Test
    public void testItemsShowsInventorySection() {
        String out = TestHelper.captureOutput(() -> game.runCommand("items"));
        assertTrue(out.contains("Player Inventory"));
    }

    @Test
    public void testItemsEmptyInventory() {
        String out = TestHelper.captureOutput(() -> game.runCommand("items"));
        assertTrue(out.contains("not carrying anything") || out.contains("carrying"));
    }

    @Test
    public void testItemsWithInventory() {
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        String out = TestHelper.captureOutput(() -> game.runCommand("items"));
        assertTrue(out.contains("铁锹"));
        assertTrue(out.contains("Total weight"));
    }

    @Test
    public void testItemsShowsWeightStats() {
        TestHelper.captureOutput(() -> game.runCommand("take 铁锹"));
        String out = TestHelper.captureOutput(() -> game.runCommand("items"));
        // 应显示负重信息
        assertTrue(out.contains("kg") || out.contains("weight") || out.contains("Weight"));
    }
}
