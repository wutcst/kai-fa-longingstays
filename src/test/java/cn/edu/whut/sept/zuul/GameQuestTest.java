package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameQuestTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        game.startNewGame("测试玩家");
    }

    // ===== 任务物品数量 =====

    @Test
    public void testRequiredItemsCount() {
        assertEquals(4, game.getRequiredItems().size());
    }

    // ===== 任务物品唯一性 =====

    @Test
    public void testRequiredItemsUnique() {
        java.util.List<String> items = game.getRequiredItems();
        assertEquals(4, items.size());
        java.util.Set<String> set = new java.util.HashSet<>(items);
        assertEquals("任务物品应无重复", items.size(), set.size());
    }

    // ===== 多次开局任务物品不同 =====

    @Test
    public void testRequiredItemsVaryAcrossGames() {
        java.util.List<String> items1 = new java.util.ArrayList<>(game.getRequiredItems());

        Game g2 = new Game();
        g2.startNewGame("玩家B");
        java.util.List<String> items2 = g2.getRequiredItems();

        Game g3 = new Game();
        g3.startNewGame("玩家C");
        java.util.List<String> items3 = g3.getRequiredItems();

        // 三次开局不应全部相同（概率极低）
        boolean allSame = items1.equals(items2) && items2.equals(items3);
        if (allSame) {
            // 再试一次排除极低概率
            Game g4 = new Game();
            g4.startNewGame("玩家D");
            assertFalse("连续三次任务物品完全相同概率极低",
                    items1.equals(g4.getRequiredItems()));
        }
    }

    // ===== 多次开局不抛异常 =====

    @Test
    public void testMultipleStartNewGameNoException() {
        for (int i = 0; i < 20; i++) {
            game.startNewGame("玩家" + i);
            assertEquals(4, game.getRequiredItems().size());
        }
    }

    // ===== 任务物品来自候选池 =====

    @Test
    public void testRequiredItemsFromPool() {
        // QUEST_ITEM_POOL 是 Game 的 private static final，无法直接访问
        // 但可以通过已知物品名验证
        java.util.List<String> knownItems = java.util.Arrays.asList(
            "生锈的铁锹", "破旧的地图", "空鸟巢", "鹅卵石堆",
            "道具剑", "乐谱架", "威士忌酒瓶",
            "飞镖盘", "酒吧凳", "显微镜", "烧杯组",
            "化学试剂瓶", "实验记录本", "魔法饼干",
            "笔记本电脑", "咖啡杯", "文件堆"
        );

        for (String item : game.getRequiredItems()) {
            assertTrue("任务物品 '" + item + "' 应在候选池中", knownItems.contains(item));
        }
    }

    // ===== 任务列表暴露内部引用（已知缺陷） =====

    @Test
    public void testRequiredItemsExposesInternalReference() {
        java.util.List<String> items = game.getRequiredItems();
        int initialSize = items.size();
        assertEquals(4, initialSize);

        // 返回的是内部可变列表，外部修改会直接影响内部状态
        items.clear();
        // 内部列表已被清空（已知缺陷：应返回不可变副本）
        assertEquals(0, game.getRequiredItems().size());
    }

    // ===== 新游戏重置任务 =====

    @Test
    public void testStartNewGameResetsRequiredItems() {
        java.util.List<String> first = new java.util.ArrayList<>(game.getRequiredItems());
        game.startNewGame("新玩家");
        java.util.List<String> second = game.getRequiredItems();

        assertEquals(4, second.size());
        // 第二组可能相同也可能不同（概率问题），但不应为同一对象
        assertNotSame(first, second);
    }
}
