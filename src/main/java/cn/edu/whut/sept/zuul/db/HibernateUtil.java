package cn.edu.whut.sept.zuul.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate 会话工厂工具类.
 */
public class HibernateUtil {

    private static volatile SessionFactory sessionFactory;
    private static volatile boolean factoryBuilt = false;

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex.getMessage());
            return null;
        }
    }

    private static SessionFactory getOrRetryFactory() {
        if (factoryBuilt) return sessionFactory;
        synchronized (HibernateUtil.class) {
            if (factoryBuilt) return sessionFactory;
            factoryBuilt = true;
            sessionFactory = buildSessionFactory();
            return sessionFactory;
        }
    }

    public static SessionFactory getSessionFactory() {
        return getOrRetryFactory();
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
