package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerFullTest {
    private Player player;
    private Room room;

    @Before
    public void setUp() {
        room = new Room("Test Room");
        player = new Player("测试玩家", room, 20.0);
    }

    // ===== 构造函数 =====

    @Test
    public void testConstructor() {
        assertEquals("测试玩家", player.getName());
        assertSame(room, player.getCurrentRoom());
        assertEquals(20.0, player.getMaxWeight(), 0.001);
        assertEquals(0.0, player.getCurrentWeight(), 0.001);
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    public void testNullName() {
        Player p = new Player(null, room, 20.0);
        assertNull(p.getName());
    }

    // ===== 拾取 =====

    @Test
    public void testTakeItemSuccess() {
        Item item = new Item("道具剑", 3.0);
        assertTrue(player.takeItem(item));
        assertEquals(3.0, player.getCurrentWeight(), 0.001);
        assertTrue(player.getInventory().contains(item));
    }

    @Test
    public void testTakeItemOverweight() {
        Item heavy = new Item("巨石", 100.0);
        assertFalse(player.takeItem(heavy));
        assertEquals(0.0, player.getCurrentWeight(), 0.001);
        assertFalse(player.getInventory().contains(heavy));
    }

    @Test
    public void testTakeItemExactLimit() {
        player.takeItem(new Item("A", 17.0));
        assertEquals(17.0, player.getCurrentWeight(), 0.001);
        // 再加 3.0，总重刚好 20.0
        assertTrue(player.takeItem(new Item("B", 3.0)));
        assertEquals(20.0, player.getCurrentWeight(), 0.001);
    }

    @Test
    public void testTakeItemJustOverLimit() {
        player.takeItem(new Item("A", 17.1));
        assertFalse(player.takeItem(new Item("B", 3.0)));
        assertEquals(17.1, player.getCurrentWeight(), 0.001);
    }

    @Test
    public void testTakeItemSequential() {
        player.takeItem(new Item("A", 5.0));
        player.takeItem(new Item("B", 5.0));
        player.takeItem(new Item("C", 5.0));
        assertEquals(15.0, player.getCurrentWeight(), 0.001);
        assertEquals(3, player.getInventory().size());
    }

    // ===== 丢弃 =====

    @Test
    public void testDropItem() {
        Item item = new Item("道具剑", 3.0);
        player.takeItem(item);
        player.dropItem(item);
        assertEquals(0.0, player.getCurrentWeight(), 0.001);
        assertFalse(player.getInventory().contains(item));
    }

    @Test
    public void testDropItemNotInInventory() {
        // 静默无操作
        Item item = new Item("不存在", 1.0);
        player.dropItem(item);
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    public void testDropOneOfMultiple() {
        Item a = new Item("A", 1.0);
        Item b = new Item("B", 2.0);
        Item c = new Item("C", 3.0);
        player.takeItem(a);
        player.takeItem(b);
        player.takeItem(c);
        player.dropItem(b);
        assertEquals(4.0, player.getCurrentWeight(), 0.001);
        assertTrue(player.getInventory().contains(a));
        assertFalse(player.getInventory().contains(b));
        assertTrue(player.getInventory().contains(c));
    }

    // ===== getItem（模糊匹配） =====

    @Test
    public void testGetItemPartialMatch() {
        player.takeItem(new Item("麦脉暖酿", 0.5));
        player.takeItem(new Item("月花蜜醴", 0.4));
        player.takeItem(new Item("道具剑", 3.0));

        assertNotNull(player.getItem("麦脉"));
        assertTrue(player.getItem("麦脉").getDescription().contains("麦脉暖酿"));

        assertNotNull(player.getItem("月花"));
        assertTrue(player.getItem("月花").getDescription().contains("月花蜜醴"));

        assertNotNull(player.getItem("剑"));
        assertTrue(player.getItem("剑").getDescription().contains("道具剑"));
    }

    @Test
    public void testGetItemFirstMatch() {
        player.takeItem(new Item("物品A_麦脉", 1.0));
        player.takeItem(new Item("麦脉暖酿", 0.5));
        Item found = player.getItem("麦脉");
        assertNotNull(found);
        assertTrue(found.getDescription().contains("物品A_麦脉"));
    }

    @Test
    public void testGetItemNotFound() {
        assertNull(player.getItem("不存在"));
    }

    @Test
    public void testGetItemEmptyInventory() {
        assertNull(player.getItem("任意"));
    }

    // ===== getCurrentWeight =====

    @Test
    public void testGetCurrentWeightEmpty() {
        assertEquals(0.0, player.getCurrentWeight(), 0.001);
    }

    @Test
    public void testGetCurrentWeightSum() {
        player.takeItem(new Item("A", 1.5));
        player.takeItem(new Item("B", 2.5));
        assertEquals(4.0, player.getCurrentWeight(), 0.001);
    }

    // ===== 负重增加 =====

    @Test
    public void testIncreaseMaxWeight() {
        player.increaseMaxWeight(10.0);
        assertEquals(30.0, player.getMaxWeight(), 0.001);
    }

    @Test
    public void testIncreaseMaxWeightMultipleTimes() {
        player.increaseMaxWeight(10.0);
        player.increaseMaxWeight(10.0);
        player.increaseMaxWeight(10.0);
        assertEquals(50.0, player.getMaxWeight(), 0.001);
    }

    @Test
    public void testIncreaseMaxWeightNoLimit() {
        // 记录：无上限
        for (int i = 0; i < 100; i++) {
            player.increaseMaxWeight(10.0);
        }
        assertEquals(1020.0, player.getMaxWeight(), 0.001);
    }

    // ===== 位置 =====

    @Test
    public void testSetCurrentRoom() {
        Room room2 = new Room("Room 2");
        player.setCurrentRoom(room2);
        assertSame(room2, player.getCurrentRoom());
    }

    // ===== 背包字符串 =====

    @Test
    public void testGetInventoryStringEmpty() {
        String s = player.getInventoryString();
        assertTrue(s.contains("not carrying anything"));
    }

    @Test
    public void testGetInventoryStringWithItems() {
        player.takeItem(new Item("道具剑", 3.0));
        String s = player.getInventoryString();
        assertTrue(s.contains("道具剑"));
        assertTrue(s.contains("3.0"));
        assertTrue(s.contains("Total weight"));
    }

    // ===== 暴露引用问题 =====

    @Test
    public void testGetInventoryReturnsMutableReference() {
        // 记录：返回可变内部引用，可绕过重量检查
        player.getInventory().add(new Item("外部注入", 100.0));
        assertTrue(player.getCurrentWeight() >= 100.0);
    }
}
