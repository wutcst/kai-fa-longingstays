package cn.edu.whut.sept.zuul.db;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseManagerTest {

    private static boolean dbAvailable;
    private static Long testSaveId;

    @BeforeClass
    public static void checkDbAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            org.hibernate.SessionFactory sf = HibernateUtil.getSessionFactory();
            dbAvailable = sf != null;
        } catch (Exception e) {
            dbAvailable = false;
        }
    }

    @Before
    public void setUp() {
        org.junit.Assume.assumeTrue("Database not available", dbAvailable);
    }

    @AfterClass
    public static void cleanup() {
        if (dbAvailable && testSaveId != null && testSaveId > 0) {
            DatabaseManager.deleteGame(testSaveId);
        }
    }

    // ===== saveGame =====

    @Test
    public void testSaveGameReturnsValidId() {
        long id = DatabaseManager.saveGame("测试存档_单元测试", "测试玩家", "{\"test\": true}");
        assertTrue("saveGame should return positive id", id > 0);
        DatabaseManager.deleteGame(id); // 清理
    }

    @Test
    public void testSaveGameEmptyJson() {
        long id = DatabaseManager.saveGame("空存档测试", "玩家", "");
        assertTrue(id > 0);
        GameSaveEntity entity = DatabaseManager.loadGame(id);
        assertNotNull(entity);
        assertEquals("", entity.getGameStateJson());
        DatabaseManager.deleteGame(id);
    }

    @Test
    public void testSaveGameNullSaveName() {
        // 可能抛异常或保存成功 — 记录行为
        try {
            long id = DatabaseManager.saveGame(null, "玩家", "{}");
            if (id > 0) DatabaseManager.deleteGame(id);
        } catch (Exception e) {
            // 预期的异常
        }
    }

    // ===== loadGame =====

    @Test
    public void testLoadGameReturnsEntity() {
        long id = DatabaseManager.saveGame("读档测试", "玩家P", "{\"key\":\"value\"}");
        assertTrue(id > 0);

        GameSaveEntity entity = DatabaseManager.loadGame(id);
        assertNotNull(entity);
        assertEquals("读档测试", entity.getSaveName());
        assertEquals("玩家P", entity.getPlayerName());
        assertEquals("{\"key\":\"value\"}", entity.getGameStateJson());
        assertTrue(entity.getCreatedAt() > 0);

        DatabaseManager.deleteGame(id);
    }

    @Test
    public void testLoadGameNonExistent() {
        GameSaveEntity entity = DatabaseManager.loadGame(99999L);
        assertNull(entity);
    }

    // ===== listSaves =====

    @Test
    public void testListSavesReturnsList() {
        java.util.List<GameSaveEntity> saves = DatabaseManager.listSaves();
        assertNotNull(saves);
    }

    // ===== listSavesMeta =====

    @Test
    public void testListSavesMetaReturnsProjection() {
        long id = DatabaseManager.saveGame("元数据测试", "玩家M", "{}");
        java.util.List<Object[]> meta = DatabaseManager.listSavesMeta();
        assertNotNull(meta);

        // 检查格式: [id, saveName, playerName, createdAt]
        for (Object[] row : meta) {
            assertEquals(4, row.length);
            assertTrue(row[0] instanceof Number);
            assertTrue(row[1] instanceof String);
            assertTrue(row[2] instanceof String);
            assertTrue(row[3] instanceof Number);
        }

        DatabaseManager.deleteGame(id);
    }

    // ===== findSaveByName =====

    @Test
    public void testFindSaveByNameFound() {
        long id = DatabaseManager.saveGame("唯一存档名_测试", "玩家F", "{}");
        GameSaveEntity entity = DatabaseManager.findSaveByName("唯一存档名_测试");
        assertNotNull(entity);
        assertEquals("唯一存档名_测试", entity.getSaveName());
        DatabaseManager.deleteGame(id);
    }

    @Test
    public void testFindSaveByNameNotFound() {
        GameSaveEntity entity = DatabaseManager.findSaveByName("不存在的存档名_XYZ");
        assertNull(entity);
    }

    // ===== updateSave =====

    @Test
    public void testUpdateSaveChangesJson() {
        long id = DatabaseManager.saveGame("更新测试", "玩家U", "before");
        assertTrue(id > 0);

        GameSaveEntity entity = DatabaseManager.loadGame(id);
        assertNotNull(entity);
        entity.setGameStateJson("after");
        entity.setPlayerName("玩家U_updated");
        DatabaseManager.updateSave(entity);

        GameSaveEntity reloaded = DatabaseManager.loadGame(id);
        assertEquals("after", reloaded.getGameStateJson());
        assertEquals("玩家U_updated", reloaded.getPlayerName());

        DatabaseManager.deleteGame(id);
    }

    // ===== deleteGame =====

    @Test
    public void testDeleteGameRemovesEntity() {
        long id = DatabaseManager.saveGame("删除测试", "玩家D", "{}");
        assertTrue(DatabaseManager.deleteGame(id));
        assertNull(DatabaseManager.loadGame(id));
    }

    @Test
    public void testDeleteGameNonExistent() {
        assertFalse(DatabaseManager.deleteGame(99999L));
    }

    // ===== GameSaveEntity =====

    @Test
    public void testGameSaveEntityConstructor() {
        GameSaveEntity entity = new GameSaveEntity("存档名", "玩家名", "{\"data\":1}");
        assertEquals("存档名", entity.getSaveName());
        assertEquals("玩家名", entity.getPlayerName());
        assertEquals("{\"data\":1}", entity.getGameStateJson());
        assertTrue(entity.getCreatedAt() > 0);
        assertEquals(10, entity.getDataSize().intValue());
    }

    @Test
    public void testGameSaveEntityDefaultConstructor() {
        GameSaveEntity entity = new GameSaveEntity();
        assertNull(entity.getId());
        assertNull(entity.getSaveName());
    }

    @Test
    public void testSetGameStateJsonUpdatesDataSize() {
        GameSaveEntity entity = new GameSaveEntity();
        entity.setGameStateJson("hello");
        assertEquals(5, entity.getDataSize().intValue());
    }

    @Test
    public void testSetGameStateJsonNull() {
        GameSaveEntity entity = new GameSaveEntity();
        entity.setGameStateJson(null);
        assertEquals(0, entity.getDataSize().intValue());
    }
}
