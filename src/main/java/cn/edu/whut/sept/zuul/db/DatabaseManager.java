package cn.edu.whut.sept.zuul.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理器.
 * 使用 Hibernate ORM 进行游戏存档的增删改查操作.
 * 所有游戏状态数据序列化为 JSON 文本存储.
 */
public class DatabaseManager {

    /**
     * 保存游戏存档.
     * @param saveName 存档名称.
     * @param playerName 玩家名称.
     * @param gameStateJson 游戏状态 JSON 字符串.
     * @return 存档记录 ID.
     */
    public static long saveGame(String saveName, String playerName, String gameStateJson) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return -1;
        Session session = sf.openSession();
        Transaction tx = null;
        long id = -1;
        try {
            tx = session.beginTransaction();
            GameSaveEntity entity = new GameSaveEntity(saveName, playerName, gameStateJson);
            session.save(entity);
            tx.commit();
            id = entity.getId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
        }
        return id;
    }

    /**
     * 读取指定 ID 的存档.
     * @param saveId 存档 ID.
     * @return 存档实体，未找到则返回 null.
     */
    public static GameSaveEntity loadGame(long saveId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return null;
        Session session = sf.openSession();
        try {
            return session.get(GameSaveEntity.class, saveId);
        } catch (Exception e) {
            System.err.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    /**
     * 获取所有存档列表（仅包含元信息，不包含完整的游戏状态 JSON）.
     * @return 存档实体列表.
     */
    public static List<GameSaveEntity> listSaves() {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        Session session = sf.openSession();
        try {
            Query<GameSaveEntity> query = session.createQuery(
                    "from GameSaveEntity order by createdAt desc", GameSaveEntity.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error listing saves: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            session.close();
        }
    }

    /**
     * 获取所有存档列表（轻量版，不加载 gameStateJson 大文本字段）.
     * 仅包含 id、saveName、playerName、createdAt 四个字段.
     * @return 存档实体列表（gameStateJson 字段为 null）.
     */
    @SuppressWarnings("unchecked")
    public static List<Object[]> listSavesMeta() {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return new ArrayList<>();
        Session session = sf.openSession();
        try {
            Query<Object[]> query = session.createQuery(
                    "select id, saveName, playerName, createdAt from GameSaveEntity order by createdAt desc",
                    Object[].class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error listing save metadata: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            session.close();
        }
    }

    /**
     * 根据存档名称查找存档记录.
     * @param saveName 存档名称.
     * @return 存档实体，未找到则返回 null.
     */
    public static GameSaveEntity findSaveByName(String saveName) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return null;
        Session session = sf.openSession();
        try {
            Query<GameSaveEntity> query = session.createQuery(
                    "from GameSaveEntity where saveName = :name", GameSaveEntity.class);
            query.setParameter("name", saveName);
            List<GameSaveEntity> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("Error finding save by name: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    /**
     * 更新已有的存档（用于自动存档覆盖）.
     * 使用 merge() 替代 update() 以确保在实体处于游离(Detached)状态时
     * 修改能被正确持久化到数据库.
     * @param entity 已修改的存档实体（必须包含有效 ID）.
     */
    public static void updateSave(GameSaveEntity entity) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return;
        Session session = sf.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error updating game: " + e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * 删除指定 ID 的存档.
     * @param saveId 存档 ID.
     * @return 是否删除成功.
     */
    public static boolean deleteGame(long saveId) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        if (sf == null) return false;
        Session session = sf.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            GameSaveEntity entity = session.get(GameSaveEntity.class, saveId);
            if (entity != null) {
                session.delete(entity);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error deleting game: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
}