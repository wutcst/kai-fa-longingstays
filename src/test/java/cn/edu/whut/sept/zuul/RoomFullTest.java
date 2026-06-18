package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RoomFullTest {
    private Room room;

    @Before
    public void setUp() {
        room = new Room("in the campus pub");
    }

    // ===== 构造函数和描述 =====

    @Test
    public void testRawDescription() {
        assertEquals("in the campus pub", room.getRawDescription());
    }

    @Test
    public void testShortDescription() {
        String desc = room.getShortDescription();
        assertTrue(desc.contains("in the campus pub"));
    }

    // ===== 出口管理 =====

    @Test
    public void testSetAndGetExit() {
        Room room2 = new Room("test room 2");
        room.setExit("east", room2);
        assertSame(room2, room.getExit("east"));
    }

    @Test
    public void testGetNullExit() {
        assertNull(room.getExit("west"));
    }

    @Test
    public void testMultipleExits() {
        Room east = new Room("east room");
        Room west = new Room("west room");
        Room north = new Room("north room");
        room.setExit("east", east);
        room.setExit("west", west);
        room.setExit("north", north);
        assertSame(east, room.getExit("east"));
        assertSame(west, room.getExit("west"));
        assertSame(north, room.getExit("north"));
        assertNull(room.getExit("south"));
    }

    @Test
    public void testExitString() {
        room.setExit("east", new Room("east"));
        room.setExit("west", new Room("west"));
        String desc = room.getShortDescription();
        assertTrue(desc.contains("east"));
        assertTrue(desc.contains("west"));
    }

    @Test
    public void testOverwriteExit() {
        Room oldRoom = new Room("old");
        Room newRoom = new Room("new");
        room.setExit("north", oldRoom);
        room.setExit("north", newRoom);
        assertSame(newRoom, room.getExit("north"));
    }

    // ===== 物品管理 =====

    @Test
    public void testAddAndGetItem() {
        Item item = new Item("威士忌", 1.1, 50, 50);
        room.addItem(item);
        assertNotNull(room.getItem("威士忌"));
        assertSame(item, room.getItem("威士忌"));
    }

    @Test
    public void testGetItemPartialMatch() {
        room.addItem(new Item("麦脉暖酿", 0.5, 50, 50));
        room.addItem(new Item("月花蜜醴", 0.4, 50, 50));
        // 子串匹配，"麦脉"应该匹配"麦脉暖酿"
        assertNotNull(room.getItem("麦脉"));
    }

    @Test
    public void testGetItemFirstMatch() {
        room.addItem(new Item("东西A_暖酿", 0.5, 50, 50));
        room.addItem(new Item("麦脉暖酿", 0.5, 50, 50));
        Item found = room.getItem("暖酿");
        assertNotNull(found);
        assertTrue(found.getDescription().contains("东西A_暖酿"));
    }

    @Test
    public void testGetItemNotFound() {
        assertNull(room.getItem("不存在"));
    }

    @Test
    public void testRemoveItem() {
        Item item = new Item("道具剑", 3.0, 50, 50);
        room.addItem(item);
        room.removeItem(item);
        assertNull(room.getItem("道具剑"));
    }

    @Test
    public void testRemoveItemNotInRoom() {
        // 移除不存在的物品应该是静默无操作
        Item item = new Item("不存在", 1.0);
        room.removeItem(item); // 不应抛异常
    }

    @Test
    public void testTotalItemWeight() {
        room.addItem(new Item("A", 1.0, 50, 50));
        room.addItem(new Item("B", 2.0, 50, 50));
        room.addItem(new Item("C", 3.0, 50, 50));
        assertEquals(6.0, room.getTotalItemWeight(), 0.001);
    }

    @Test
    public void testTotalItemWeightEmpty() {
        assertEquals(0.0, room.getTotalItemWeight(), 0.001);
    }

    @Test
    public void testTotalWeightAfterRemove() {
        Item a = new Item("A", 1.0, 50, 50);
        room.addItem(a);
        room.addItem(new Item("B", 2.0, 50, 50));
        room.removeItem(a);
        assertEquals(2.0, room.getTotalItemWeight(), 0.001);
    }

    // ===== 长描述 =====

    @Test
    public void testLongDescriptionWithItems() {
        room.addItem(new Item("啤酒", 0.5, 50, 50));
        String desc = room.getLongDescription();
        assertTrue(desc.contains("campus pub"));
        assertTrue(desc.contains("啤酒"));
        assertTrue(desc.contains("0.5"));
    }

    @Test
    public void testLongDescriptionEmptyRoom() {
        String desc = room.getLongDescription();
        assertTrue(desc.contains("No items here"));
    }

    // ===== getItems =====

    @Test
    public void testGetItems() {
        Item a = new Item("A", 1.0);
        Item b = new Item("B", 2.0);
        room.addItem(a);
        room.addItem(b);
        assertEquals(2, room.getItems().size());
        assertTrue(room.getItems().contains(a));
        assertTrue(room.getItems().contains(b));
    }

    @Test
    public void testGetItemsEmpty() {
        assertTrue(room.getItems().isEmpty());
    }
}
