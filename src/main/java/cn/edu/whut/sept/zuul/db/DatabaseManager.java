package cn.edu.whut.sept.zuul.db;

import org.hibernate.Session;
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
        Session session = HibernateUtil.getSessionFactory().openSession();
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
        Session session = HibernateUtil.getSessionFactory().openSession();
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
        Session session = HibernateUtil.getSessionFactory().openSession();
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
     * 更新已有的存档（用于自动存档覆盖）.
     * @param entity 已修改的存档实体（必须包含有效 ID）.
     */
    public static void updateSave(GameSaveEntity entity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(entity);
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
        Session session = HibernateUtil.getSessionFactory().openSession();
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