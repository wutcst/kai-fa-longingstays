package test; // 【修改点1】包名必须与文件夹一致

// 【修改点2】引入游戏源码包，否则找不到 Player, Room 等类
import cn.edu.whut.sept.zuul.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerTest {
    private Player player;
    private Room room;
    private Item heavyItem;
    private Item lightItem;
    private Item cookie;

    @Before
    public void setUp() {
        room = new Room("Test Room");
        // 初始负重 10.0
        player = new Player("TestPlayer", room, 10.0);

        heavyItem = new Item("Anvil", 100.0);
        lightItem = new Item("Feather", 1.0);
        cookie = new Item("magic cookie", 0.5);
    }

    @Test
    public void testTakeItemSuccess() {
        assertTrue("应该能拾取轻物品", player.takeItem(lightItem));
        assertEquals("背包重量应该增加", 1.0, player.getCurrentWeight(), 0.01);
        assertTrue("背包中应包含该物品", player.getInventory().contains(lightItem));
    }

    @Test
    public void testTakeItemOverweight() {
        assertFalse("不应拾取超重物品", player.takeItem(heavyItem));
        assertEquals("背包重量不应改变", 0.0, player.getCurrentWeight(), 0.01);
    }

    @Test
    public void testDropItem() {
        player.takeItem(lightItem);
        player.dropItem(lightItem);
        assertEquals("丢弃后重量应归零", 0.0, player.getCurrentWeight(), 0.01);
        assertFalse("背包中不应包含该物品", player.getInventory().contains(lightItem));
    }

    @Test
    public void testEatCookieIncreaseMaxWeight() {
        double initialMax = player.getMaxWeight();
        player.increaseMaxWeight(10.0);
        assertEquals("最大负重应增加", initialMax + 10.0, player.getMaxWeight(), 0.01);
    }
}

