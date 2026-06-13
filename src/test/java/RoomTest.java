package test; // 【修改点】

import cn.edu.whut.sept.zuul.*; // 【修改点】

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RoomTest {
    private Room room;
    private Item item;

    @Before
    public void setUp() {
        room = new Room("Test Room");
        item = new Item("Apple", 0.5);
    }

    @Test
    public void testItemManagement() {
        room.addItem(item);
        assertEquals("房间总重量应正确", 0.5, room.getTotalItemWeight(), 0.01);
        assertNotNull("应该能获取到物品", room.getItem("Apple"));

        room.removeItem(item);
        assertEquals("移除后总重量应为0", 0.0, room.getTotalItemWeight(), 0.01);
        assertNull("不应再获取到物品", room.getItem("Apple"));
    }
}

