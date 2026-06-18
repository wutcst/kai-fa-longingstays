package cn.edu.whut.sept.zuul;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemTest {

    @Test
    public void testConstructorWithCoords() {
        Item item = new Item("道具剑", 3.0, 45.5, 60.2);
        assertEquals("道具剑", item.getDescription());
        assertEquals(3.0, item.getWeight(), 0.001);
        assertEquals(45.5, item.getX(), 0.001);
        assertEquals(60.2, item.getY(), 0.001);
    }

    @Test
    public void testConstructorWithoutCoords() {
        Item item = new Item("随机位置物品", 5.0);
        assertEquals("随机位置物品", item.getDescription());
        assertEquals(5.0, item.getWeight(), 0.001);
        // 坐标应在 20~80 随机范围内
        assertTrue("X 应在 20-80 之间", item.getX() >= 20 && item.getX() <= 80);
        assertTrue("Y 应在 20-80 之间", item.getY() >= 20 && item.getY() <= 80);
    }

    @Test
    public void testSetters() {
        Item item = new Item("测试", 1.0, 0, 0);
        item.setX(10.5);
        item.setY(20.5);
        assertEquals(10.5, item.getX(), 0.001);
        assertEquals(20.5, item.getY(), 0.001);
    }

    @Test
    public void testZeroWeight() {
        Item item = new Item("羽毛", 0.0, 50, 50);
        assertEquals(0.0, item.getWeight(), 0.001);
    }

    @Test
    public void testNegativeWeight_Allowed() {
        // 当前代码允许负重量（记录此行为）
        Item item = new Item("反物质", -1.0, 50, 50);
        assertEquals(-1.0, item.getWeight(), 0.001);
    }

    @Test
    public void testNegativeCoords_Allowed() {
        // 当前代码允许负坐标（记录此行为）
        Item item = new Item("墙外物品", 1.0, -10, -10);
        assertEquals(-10.0, item.getX(), 0.001);
        assertEquals(-10.0, item.getY(), 0.001);
    }

    @Test
    public void testExtremeCoords() {
        Item item = new Item("远距", 1.0, 9999, 9999);
        assertEquals(9999.0, item.getX(), 0.001);
    }

    @Test
    public void testMultipleItemsRandomCoordsAreDifferent() {
        Item a = new Item("A", 1.0);
        Item b = new Item("B", 1.0);
        // 两个物品随机坐标不全相同的概率极高
        boolean same = a.getX() == b.getX() && a.getY() == b.getY();
        assertFalse("两个无坐标物品应该有不同的随机位置", same);
    }
}
